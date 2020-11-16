package fr.uphf.se.kotlinresearch.diff.analyzers;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.gumtreediff.actions.model.Action;
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
import fr.uphf.se.kotlinresearch.tree.analyzers.JavaTreeAnalyzer;
import fr.uphf.se.kotlinresearch.tree.analyzers.TreeResult;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;

public class JavaDiffAnalyzer implements Analyzer<IRevision> {

	Logger log = Logger.getLogger(JavaDiffAnalyzer.class.getName());

	@SuppressWarnings("rawtypes")
	@Override
	public AnalysisResult analyze(IRevision revision, RevisionResult previousResults) {
		RenameAnalyzerResult renameresult = (RenameAnalyzerResult) previousResults
				.getResultFromClass(FileCommitNameAnalyzer.class);
		TreeResult treeResult = (TreeResult) previousResults.getResultFromClass(JavaTreeAnalyzer.class);

		return analyze(revision, renameresult, treeResult);
	}

	public DiffResult analyze(IRevision revision, RenameAnalyzerResult renameresult, TreeResult treeResult) {
		List<IRevisionPair<String>> childerPairs = renameresult.getAllFileCommits();

		log.debug("\n*** Analyzing revision: " + revision.getName());
		long init = (new Date()).getTime();

		Map<String, QueryDiff> diffOfFiles = new HashMap<>();

		// For each file inside the revision
		for (IRevisionPair iRevisionPair : childerPairs) {
			String currentFilename = iRevisionPair.getName();
			String previousName = iRevisionPair.getPreviousName();

			long initFile = (new Date()).getTime();

			// if Java code
			if (currentFilename.endsWith(".java") && previousName.endsWith(".java")) {

				if (ComingProperties.getPropertyBoolean(MigACore.OPTIMIZE_NAVIGATION)
						&& (Outils.isEmpty(iRevisionPair.getNextVersion())
								|| Outils.isEmpty(iRevisionPair.getPreviousVersion()))) {
					log.debug("[optimization]: avoiding Java Tree diff, one is null: " + iRevisionPair.getName()
							+ " at " + revision.getName());
					continue;
				}

				if (treeResult != null && treeResult.getTreeOfFiles() != null
						&& treeResult.getTreeOfFiles().containsKey(currentFilename)) {
					ITree treeLeft = treeResult.getTreeOfFiles().get(currentFilename)[TreeResult.LEFT];
					ITree treeRight = treeResult.getTreeOfFiles().get(currentFilename)[TreeResult.RIGHT];

					if (treeLeft != null && treeRight != null) {
						long initDiff = (new Date()).getTime();
						QueryDiff diffJava = new QueryDiff(MigACore.counterQuery.getIncremented(),
								treeResult.getContext(), treeLeft, treeRight, revision.getName(), currentFilename,
								"java");
						MigACore.executionsTime.add("JavaDiff", new Long((new Date()).getTime() - initDiff));

						//
						if (ComingProperties.getPropertyBoolean(MigACore.ATTACH_TO_STRING_TO_TREE)) {
							attachToStringToTree(diffJava);
						}
						//

						diffOfFiles.put(currentFilename, diffJava);
					}
				} else {
					if (treeResult != null && treeResult.getTreeOfFiles() != null)
						log.error("No Trees for file " + currentFilename + " and previous name: " + previousName);
				}
			}
			MigACore.executionsTime.add(this.getClass().getSimpleName() + "_File",
					new Long((new Date()).getTime() - initFile));
		}
		MigACore.executionsTime.add(this.getClass().getSimpleName() + "_Commit",
				new Long((new Date()).getTime() - init));

		return new DiffResult<IRevision, QueryDiff>(revision, diffOfFiles);
	}

	private void attachToStringToTree(QueryDiff diffJava) {

		for (Action action : diffJava.getRootOperations()) {

			CtElement attachedElement = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			action.getNode().setMetadata(MigACore.NODE_TO_STRING,
					(attachedElement != null) ? attachedElement.toString() : "-null-");

			SourcePosition position = JavaTreeAnalyzer.getParentSourcePosition(attachedElement);

			if (position != null) {
				action.getNode().setMetadata(MigACore.LINE_CODE, position.getLine());
			}

		}

	}

}
