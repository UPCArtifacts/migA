package fr.uphf.se.kotlinresearch.diff.analyzers.granularitylevel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.uphf.se.kotlinresearch.core.MigACore;
import fr.uphf.se.kotlinresearch.diff.analyzers.JavaDiffAnalyzer;
import fr.uphf.se.kotlinresearch.squarediff.entities.diff.QueryDiff;
import fr.uphf.se.kotlinresearch.squarediff.entities.diff.SingleDiff;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;

/**
 * 
 * @author Matias Martinez
 *
 */
public abstract class JavaAbstractDiffSplitterAnalyzer implements Analyzer<IRevision> {
	Logger log = Logger.getLogger(JavaAbstractDiffSplitterAnalyzer.class.getName());

	Class sourceAnalyzer = JavaDiffAnalyzer.class;

	public JavaAbstractDiffSplitterAnalyzer(Class sourceAnalyzer) {
		super();
		this.sourceAnalyzer = sourceAnalyzer;
	}

	public JavaAbstractDiffSplitterAnalyzer() {
		super();
		sourceAnalyzer = JavaDiffAnalyzer.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public AnalysisResult analyze(IRevision revision, RevisionResult previousResults) {
		long initCommit = (new Date()).getTime();

		DiffResult<IRevision, QueryDiff> dkotlin = (DiffResult<IRevision, QueryDiff>) previousResults
				.getResultFromClass(sourceAnalyzer);

		Map<String, List<QueryDiff>> group = new HashMap<>();

		for (String javaFile : dkotlin.getDiffOfFiles().keySet()) {
			long initFile = (new Date()).getTime();

			SplittedActions result = new SplittedActions();
			TreeContext context = null;
			SingleDiff fileKotlinDiff = dkotlin.getDiffOfFiles().get(javaFile);
			// Here we divide the actions
			for (Action action : fileKotlinDiff.getRootOperations()) {

				context = fileKotlinDiff.getContext();

				CtElement assoiatedSpoon = (CtElement) action.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);

				if (assoiatedSpoon != null) {
					ITree itreeFromSplitted = getSplitElement(assoiatedSpoon);
					if (itreeFromSplitted != null) {
						ITree methodElement = null;
						// Get Info of parent
						if (!(this instanceof JavaMethodDiffSplitterAnalyzer)) {
							JavaMethodDiffSplitterAnalyzer methodsplit = new JavaMethodDiffSplitterAnalyzer();
							methodElement = methodsplit.getSplitElement(assoiatedSpoon);
						}
						// Get element of tree:
						CtElement treeSpoon = (CtElement) itreeFromSplitted
								.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);

						String hash = getHash(treeSpoon);
						String keyName = getNameOfJavaNode(fileKotlinDiff, itreeFromSplitted) + "###" + hash
								+ ((methodElement != null) ? "@@@" + methodElement.getLabel() : "");

						result.getActionsByEntityType().add(keyName, action);
					} else {
						result.getActionsByEntityType().add("nosplitparent", action);
					}
				}

			}
			List<QueryDiff> diffsFromMethod = new ArrayList<QueryDiff>();

			for (String elementSplitted : result.getActionsByEntityType().keySet()) {
				long initMethod = (new Date()).getTime();

				List<Action> actionsmethods = result.getActionsByEntityType().get(elementSplitted);
				// The diff of one grouped element
				QueryDiff diffGrouped = new QueryDiff(MigACore.counterQuery.getIncremented(), context,
						revision.getName(), javaFile, "java", actionsmethods);

				diffGrouped.metadata.put("split", elementSplitted);

				MigACore.executionsTime.add("Java" + getGranularityName() + "_Diff",
						new Long((new Date()).getTime() - initMethod));

				diffGrouped.metadata.put("original_element", elementSplitted);
				diffGrouped.metadata.put("generator", this.getClass().getSimpleName());

				diffsFromMethod.add(diffGrouped);

			}
			group.put(javaFile, diffsFromMethod);
			MigACore.executionsTime.add(this.getClass().getSimpleName() + "_File",
					new Long((new Date()).getTime() - initFile));
		}
		MigACore.executionsTime.add(this.getClass().getSimpleName() + "_Commit",
				new Long((new Date()).getTime() - initCommit));

		return new DiffResult<IRevision, List<QueryDiff>>(revision, group);
	}

	public String getHash(CtElement treeSpoon) {
		return treeSpoon.toString().hashCode() + "+" + treeSpoon.getParent().toString().hashCode();
	}

	public ITree getSplitElement(CtElement assoiatedSpoon) {
		CtElement parent = getHigherParent(assoiatedSpoon);
		if (parent != null) {
			ITree itree = (ITree) parent.getAllMetadata().get(SpoonGumTreeBuilder.GUMTREE_NODE);
			return itree;
		}
		return null;
	}

	public CtElement getHigherParent(CtElement associatedSpoon) {
		if (associatedSpoon == null)
			return null;

		CtElement parent = associatedSpoon.getParent(getParentElementToFind());

		if (parent != null && parent instanceof CtClass) {
			return getHigherParent(parent.getParent());
		}

		if (mustFirst()) {
			return parent;
		}

		if (parent != null) {

			CtElement grandParent = getHigherParent(parent);
			if (grandParent != null) {
				return grandParent;
			} else {
				return parent;
			}
		}
		return null;
	}

	public String getGranularityName() {
		return getParentElementToFind().getSimpleName();
	}

	public abstract Class getParentElementToFind();

	public abstract String getName(CtElement el);

	public boolean mustFirst() {
		return true;
	}

	public static String getNameOfJavaNode(SingleDiff diff, ITree node) {

		if (node == null)
			return "noinfo";

		ITree parent = node;
		String nodeInfo = "";
		for (int i = 1; i <= 4; i++) {

			if (parent != null) {

				nodeInfo += (!nodeInfo.isEmpty()) ? "_" : "";

				nodeInfo += getSignature(diff, parent);

				parent = parent.getParent();
			}

		}
		return nodeInfo;

	}

	public static String getSignature(SingleDiff diff, ITree node) {
		String st = cleanType(diff.getContext().getTypeLabel(node));

		return st;
	}

	public static String cleanType(String type) {

		return type.replace("kastree.ast.all.", ""); // type.replace("kastree.ast.Node$", "");
	}
}
