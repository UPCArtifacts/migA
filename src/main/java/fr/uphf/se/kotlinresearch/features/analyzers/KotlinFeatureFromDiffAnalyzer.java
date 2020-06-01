package fr.uphf.se.kotlinresearch.features.analyzers;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.jetbrains.kotlin.psi.KtElement;

import com.github.gumtreediff.actions.model.Action;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.uphf.ast.ASTNode;
import fr.uphf.feature.FeatureDetector;
import fr.uphf.se.kotlinresearch.diff.analyzers.KotlinDiffAnalyzer;
import fr.uphf.se.kotlinresearch.diff.analyzers.SingleDiff;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import io.gitlab.arturbosch.detekt.api.Finding;

/**
 * 
 * @author Matias Martinez
 *
 */
@Deprecated // We dont use this AST representation
public class KotlinFeatureFromDiffAnalyzer implements Analyzer<IRevision> {

	Logger log = Logger.getLogger(KotlinFeatureFromDiffAnalyzer.class.getName());

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public AnalysisResult analyze(IRevision revision, RevisionResult previousResults) {
		long init = (new Date()).getTime();
		log.debug("\n*** Analyzing revision: " + revision.getName());

		DiffResult<IRevision, SingleDiff> dk = (DiffResult<IRevision, SingleDiff>) previousResults
				.getResultFromClass(KotlinDiffAnalyzer.class);

		Map<String, List<Finding>> result = new HashMap<>();

		for (String file : dk.getDiffOfFiles().keySet()) {

			SingleDiff diff = dk.getDiffOfFiles().get(file);

			List<Action> actions = diff.getRootOperations();

			log.debug("file " + file + " #Root actions " + actions.size());
			for (Action action : actions) {

				ASTNode ast = (ASTNode) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);

				KtElement element = ast.getKtelement();

				List<Finding> findings = FeatureDetector.Companion.extractAll(element);
				log.debug("Action: " + action.getName() + " element " + action.getNode().getLabel() + " type "
						+ ast.getType() + " ast label " + ast.getLabel());
				log.debug("Finding in diff: " + findings.stream().map(e -> e.getId()).collect(Collectors.toList()));

			}

		}
		return new FeatureResult<>(revision, result);
	}

}
