package fr.uphf.se.kotlinresearch.tree.analyzers;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

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
import gumtree.spoon.AstComparator;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;

/**
 * 
 * @author Matias Martinez
 *
 */
public class JavaTreeAnalyzer implements Analyzer<IRevision> {

	Logger log = Logger.getLogger(JavaTreeAnalyzer.class.getName());
	AstComparator comparator = new AstComparator();
	SpoonGumTreeBuilder scanner = new SpoonGumTreeBuilder();

	@SuppressWarnings("rawtypes")
	@Override
	public AnalysisResult analyze(IRevision revision, RevisionResult previousResults) {

		RenameAnalyzerResult renameresult = (RenameAnalyzerResult) previousResults
				.getResultFromClass(FileCommitNameAnalyzer.class);

		AddRemoveResult arm = (AddRemoveResult) previousResults.getResultFromClass(AddedRemovedAnalyzer.class);

		return analyze(revision, renameresult, arm);
	}

	public TreeResult analyze(IRevision revision, RenameAnalyzerResult renameresult, AddRemoveResult arm) {
		long init = (new Date()).getTime();

		List<IRevisionPair<String>> childerPairs = renameresult.getAllFileCommits();

		log.debug("\n*** Analyzing revision: " + revision.getName());

		Map<String, ITree[]> treeOfFiles = new HashMap<>();

		if (ComingProperties.getPropertyBoolean(MigACore.COMPUTES_ONLY_COEVOLUTION) && arm.modifJava.size() > 0
				&& (arm.modifKotlin.isEmpty() && arm.addedKotlin.isEmpty() && arm.removedKotlin.isEmpty())) {

			System.out.println("Ignoring commit that only change Java code " + revision.getName());
			return new TreeResult(revision, treeOfFiles, getContext());
		}

		// For each file inside the revision
		for (IRevisionPair iRevisionPair : childerPairs) {
			ITree treeRight = null;
			ITree treeLeft = null;

			String currentFileName = iRevisionPair.getName();

			if (ComingProperties.getPropertyBoolean(MigACore.OPTIMIZE_NAVIGATION)) {
				// Check not empty one sides:
				if (Outils.isEmpty(iRevisionPair.getNextVersion())
						|| Outils.isEmpty(iRevisionPair.getPreviousVersion())) {
					log.debug("[optimization]: avoiding Java Tree creation, one is null: " + iRevisionPair.getName()
							+ " at " + revision.getName());
					continue;
				} else {
					// let's check migration:
					if (arm.migrationJavaToKotlin.contains(currentFileName)
							|| arm.migrationKotlinToJava.contains(currentFileName)) {
						log.debug("[optimization]: avoiding Java Tree creation, it's a migration: "
								+ iRevisionPair.getName() + " at " + revision.getName());
						continue;
					}

				}

			}

			if (iRevisionPair.getPreviousName().endsWith(".java")) {

				String leftJavaFile = iRevisionPair.getPreviousVersion().toString();
				if (leftJavaFile != null && !leftJavaFile.trim().isEmpty())
					treeLeft = getTree(leftJavaFile);

			}
			if (iRevisionPair.getName().endsWith(".java")) {
				String rightJavaFile = iRevisionPair.getNextVersion().toString();

				if (rightJavaFile != null && !rightJavaFile.trim().isEmpty())
					treeRight = getTree(rightJavaFile);

			}

			if (treeLeft != null || treeRight != null) {
				ITree[] result = new ITree[2];
				result[TreeResult.LEFT] = treeLeft;
				result[TreeResult.RIGHT] = treeRight;

				treeOfFiles.put(currentFileName, result);
			}
		}
		MigACore.executionsTime.add(this.getClass().getSimpleName(), new Long((new Date()).getTime() - init));

		return new TreeResult(revision, treeOfFiles, getContext());
	}

	public TreeContext getContext() {
		return scanner.getTreeContext();
	}

	public ITree getTree(String rightJavaFile) {
		try {
			CtType<?> ctType = comparator.getCtType(rightJavaFile);
			return getTree(ctType);
		} catch (Exception e) {
			log.error("Problems parsing Java code with spoon");
		}
		return null;
	}

	public ITree getTree(CtType<?> ctType) {
		ITree tree = scanner.getTree(ctType);
		setRoleInMetadata(tree);
		for (ITree des : tree.getDescendants()) {
			setRoleInMetadata(des);
		}
		return tree;
	}

	public void setRoleInMetadata(ITree tree) {
		Object mt = tree.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
		if (mt != null) {
			CtElement el = (CtElement) mt;
			tree.setMetadata("role", el.getRoleInParent().toString());
		}
	}

	public static SourcePosition getParentSourcePosition(CtElement el) {
		if (el == null || el.getPosition() == null)
			return null;

		if (!(el.getPosition() instanceof NoSourcePosition)) {
			return el.getPosition();
		} else
			return getParentSourcePosition(el.getParent());

	}

}
