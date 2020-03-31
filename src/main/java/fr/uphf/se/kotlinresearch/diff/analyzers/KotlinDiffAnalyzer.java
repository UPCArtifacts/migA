package fr.uphf.se.kotlinresearch.diff.analyzers;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.gumtreediff.tree.ITree;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.IRevisionPair;
import fr.inria.coming.main.ComingProperties;
import fr.uphf.se.kotlinresearch.arm.analyzers.FileCommitNameAnalyzer;
import fr.uphf.se.kotlinresearch.arm.analyzers.RenameAnalyzerResult;
import fr.uphf.se.kotlinresearch.core.MigACore;
import fr.uphf.se.kotlinresearch.core.Outils;
import fr.uphf.se.kotlinresearch.squarediff.entities.diff.QueryDiff;
import fr.uphf.se.kotlinresearch.tree.analyzers.KastreeTreeAnalyzer;
import fr.uphf.se.kotlinresearch.tree.analyzers.TreeResult;
import fr.uphf.se.kotlinresearch.tree.analyzers.kastreeITree.KastreeToITree;

/**
 * 
 * @author Matias Martinez
 *
 */
public class KotlinDiffAnalyzer implements Analyzer<IRevision> {

	Logger log = Logger.getLogger(KotlinDiffAnalyzer.class.getName());

	String namespace = "kastree.ast.all.";// "kastree.ast.Node$";

	@SuppressWarnings("rawtypes")
	@Override
	public AnalysisResult analyze(IRevision revision, RevisionResult previousResults) {
		long initCommit = (new Date()).getTime();
		RenameAnalyzerResult renameresult = (RenameAnalyzerResult) previousResults
				.getResultFromClass(FileCommitNameAnalyzer.class);

		List<IRevisionPair<String>> childerPairs = renameresult.getAllFileCommits();

		log.debug("\n*** Analyzing revision: " + revision.getName());

		Map<String, QueryDiff> diffOfFiles = new HashMap<>();

		// For each file inside the revision
		for (IRevisionPair iRevisionPair : childerPairs) {
			long initFile = (new Date()).getTime();

			// log.debug("visiting " + iRevisionPair.getName());

			String currentFilename = iRevisionPair.getName();
			String previousFilename = iRevisionPair.getPreviousName();

			if (currentFilename.endsWith(".kt") && previousFilename.endsWith(".kt")) {

				if (ComingProperties.getPropertyBoolean(MigACore.OPTIMIZE_NAVIGATION)
						&& (Outils.isEmpty(iRevisionPair.getNextVersion())
								|| Outils.isEmpty(iRevisionPair.getPreviousVersion()))) {
					log.debug("[optimization]: avoiding Kotlin Tree diff, one is null: " + iRevisionPair.getName()
							+ " at " + revision.getName());
					continue;
				}

				log.debug("-revisionPair-->" + currentFilename);

				TreeResult treeResult = (TreeResult) previousResults.getResultFromClass(KastreeTreeAnalyzer.class);

				if (treeResult != null && treeResult.getTreeOfFiles() != null
						&& treeResult.getTreeOfFiles().containsKey(currentFilename)) {
					ITree treeLeft = treeResult.getTreeOfFiles().get(currentFilename)[TreeResult.LEFT];
					ITree treeRight = treeResult.getTreeOfFiles().get(currentFilename)[TreeResult.RIGHT];

					if (treeLeft != null && treeRight != null) {
						long initDiff = (new Date()).getTime();
						QueryDiff diffKotlin = new QueryDiff(MigACore.counterQuery.getIncremented(),
								treeResult.getContext(), treeLeft, treeRight, revision.getName(), currentFilename,
								"kotlin");

						diffKotlin.getAllOperations().removeIf(
								e -> (namespace + "Import").equals(e.getNode().getMetadata(KastreeToITree.TYPE)));

						diffKotlin.getRootOperations().removeIf(
								e -> (namespace + "Import").equals(e.getNode().getMetadata(KastreeToITree.TYPE)));

						MigACore.executionsTime.add("KotlinDiff", new Long((new Date()).getTime() - initDiff));

						diffOfFiles.put(currentFilename, diffKotlin);
					}
				} else {
					if (treeResult != null && treeResult.getTreeOfFiles() != null)
						log.error("No Trees for file " + currentFilename + " and previous name: " + previousFilename);
				}

			}
			MigACore.executionsTime.add(this.getClass().getSimpleName() + "_File",
					new Long((new Date()).getTime() - initFile));

		}
		MigACore.executionsTime.add(this.getClass().getSimpleName() + "_Commit",
				new Long((new Date()).getTime() - initCommit));

		return new DiffResult<IRevision, QueryDiff>(revision, diffOfFiles);
	}

}
