package fr.uphf.se.kotlinresearch.features.analyzers;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.jetbrains.kotlin.psi.KtElement;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.ITree;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.uphf.feature.FeatureDetector;
import fr.uphf.se.kotlinresearch.core.MigACore;
import fr.uphf.se.kotlinresearch.diff.analyzers.KotlinDiffAnalyzer;
import fr.uphf.se.kotlinresearch.diff.analyzers.SingleDiff;
import io.gitlab.arturbosch.detekt.api.Finding;
import kastree.ast.Node;

/**
 * 
 * @author Matias Martinez
 *
 */
public class KotlinFeatureFromKastreeDiffAnalyzer implements Analyzer<IRevision> {

	Logger log = Logger.getLogger(KotlinFeatureFromKastreeDiffAnalyzer.class.getName());

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public AnalysisResult analyze(IRevision revision, RevisionResult previousResults) {
		long init = (new Date()).getTime();
		log.debug("\n*** Analyzing revision: " + revision.getName());

		DiffResult<IRevision, SingleDiff> dk = (DiffResult<IRevision, SingleDiff>) previousResults
				.getResultFromClass(KotlinDiffAnalyzer.class);

		Map<String, Map<Action, List<Finding>>> result = new HashMap<>();

		for (String file : dk.getDiffOfFiles().keySet()) {

			SingleDiff diff = dk.getDiffOfFiles().get(file);

			List<Action> actions = diff.getRootOperations();

			log.debug("file " + file + " #Root actions " + actions.size());

			Map<Action, List<Finding>> findingsPerAction = new HashMap<Action, List<Finding>>();

			for (Action action : actions) {

				ITree tree = action.getNode();

				Node kastreenode = (Node) tree.getMetadata("kastree_node");

				KtElement element = (KtElement) kastreenode.getKtEl();

				try {
					if (element == null) {
						log.error("Error: not KtElement associated to kastree node "
								+ kastreenode.getClass().getCanonicalName());

					} else {

						List<Finding> findings = FeatureDetector.Companion.extractAll(element);

						log.debug("Finding in diff: "
								+ findings.stream().map(e -> e.getId()).collect(Collectors.toList()));
						// featOfFile.addAll(findings);

						//
						findingsPerAction.put(action, findings);
					}
				} catch (Exception e) {
					log.error("Problems when extracting features from diff");
					e.printStackTrace();
				}

			}
			result.put(file, findingsPerAction);

		}
		MigACore.executionsTime.add(this.getClass().getSimpleName(), new Long((new Date()).getTime() - init));

		return new FeatureFromDiffResult<>(revision, result);
	}

}
