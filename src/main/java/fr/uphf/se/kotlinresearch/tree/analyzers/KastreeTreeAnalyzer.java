package fr.uphf.se.kotlinresearch.tree.analyzers;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.gumtreediff.tree.ITree;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.IRevisionPair;
import fr.inria.coming.main.ComingProperties;
import fr.uphf.se.kotlinresearch.arm.analyzers.AddRemoveResult;
import fr.uphf.se.kotlinresearch.arm.analyzers.AddedRemovedAnalyzer;
import fr.uphf.se.kotlinresearch.arm.analyzers.FileCommitNameAnalyzer;
import fr.uphf.se.kotlinresearch.arm.analyzers.RenameAnalyzerResult;
import fr.uphf.se.kotlinresearch.core.MigACore;
import fr.uphf.se.kotlinresearch.core.Outils;
import fr.uphf.se.kotlinresearch.tree.analyzers.kastreeITree.KastreeParser;

/**
 * 
 * @author Matias Martinez
 *
 */
public class KastreeTreeAnalyzer implements Analyzer<IRevision> {

	KastreeParser kp = new KastreeParser();

	Logger log = Logger.getLogger(KastreeTreeAnalyzer.class.getName());

	@SuppressWarnings("rawtypes")
	@Override
	public AnalysisResult analyze(IRevision revision, RevisionResult previousResults) {
		long init = (new Date()).getTime();
		RenameAnalyzerResult renameresult = (RenameAnalyzerResult) previousResults
				.getResultFromClass(FileCommitNameAnalyzer.class);

		AddRemoveResult arm = (AddRemoveResult) previousResults.getResultFromClass(AddedRemovedAnalyzer.class);

		List<IRevisionPair<String>> childerPairs = renameresult.getAllFileCommits();

		log.debug("\n*** Analyzing revision: " + revision.getName());

		if (ComingProperties.getPropertyBoolean(MigACore.COMPUTES_ONLY_COEVOLUTION) && arm.modifKotlin.size() > 0
				&& (arm.modifJava.isEmpty() && arm.addedJava.isEmpty() && arm.removedJava.isEmpty())) {

			System.out.println("Ignoring commit that only change Kotlin code " + revision.getName());
			return new TreeResult(revision, null, null);
		}

		Map<String, ITree[]> treeOfFiles = new HashMap<>();

		// For each file inside the revision
		for (IRevisionPair iRevisionPair : childerPairs) {

			String currentFilename = iRevisionPair.getName();

			if (ComingProperties.getPropertyBoolean(MigACore.OPTIMIZE_NAVIGATION)) {
				// Check not empty one sides:
				if (Outils.isEmpty(iRevisionPair.getNextVersion())
						|| Outils.isEmpty(iRevisionPair.getPreviousVersion())) {
					log.debug("[optimization]: avoiding Kotlin Tree creation, one is null: " + iRevisionPair.getName()
							+ " at " + revision.getName());
					continue;
				} else {
					// let's check migration:
					if (arm.migrationJavaToKotlin.contains(currentFilename)
							|| arm.migrationKotlinToJava.contains(currentFilename)) {
						log.debug("[optimization]: avoiding Java Tree creation, it's a migration: "
								+ iRevisionPair.getName() + " at " + revision.getName());
						continue;
					}

				}

			}

			// log.debug("visiting " + iRevisionPair.getName());

			ITree treeLeft = null;
			ITree treeRight = null;

			if (iRevisionPair.getPreviousName().endsWith(".kt")) {
				// TODO:
				// currentFilename = iRevisionPair.getPreviousName();
				// log.debug("-revisionPair-->" + filename);

				String leftKotlinFile = iRevisionPair.getPreviousVersion().toString();

				if (leftKotlinFile != null && !leftKotlinFile.trim().isEmpty())
					treeLeft = kp.getITree(leftKotlinFile);

			}
			if (iRevisionPair.getName().endsWith(".kt")) {
				// if already exists, we keep right
				// currentFilename = iRevisionPair.getName();
				String rightKotlinFile = iRevisionPair.getNextVersion().toString();

				if (rightKotlinFile != null && !rightKotlinFile.trim().isEmpty())
					treeRight = kp.getITree(rightKotlinFile);

			}
			if (treeLeft != null || treeRight != null) {
				ITree[] treesOfRevision = new ITree[2];
				treesOfRevision[TreeResult.LEFT] = treeLeft;
				treesOfRevision[TreeResult.RIGHT] = treeRight;
				treeOfFiles.put(currentFilename, treesOfRevision);
			}

		}
		MigACore.executionsTime.add(this.getClass().getSimpleName(), new Long((new Date()).getTime() - init));

		return new TreeResult(revision, treeOfFiles, this.kp.getKtt().getContext());
	}

}
