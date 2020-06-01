package fr.uphf.se.kotlinresearch.diff.analyzers.granularitylevel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import fr.uphf.se.kotlinresearch.diff.analyzers.KotlinDiffAnalyzer;
import fr.uphf.se.kotlinresearch.diff.analyzers.QueryDiff;
import fr.uphf.se.kotlinresearch.diff.analyzers.SingleDiff;
import fr.uphf.se.kotlinresearch.tree.analyzers.kastreeITree.KastreeToITree;
import kastree.ast.Node;
import kastree.ast.all.BinaryOp;
import kastree.ast.all.Block;
import kastree.ast.all.BodyFunc;
import kastree.ast.all.Brace;
import kastree.ast.all.Call;
import kastree.ast.all.DeclStmt;
import kastree.ast.all.ExprStmt;
import kastree.ast.all.Structured;
import kastree.ast.all.Token;
import kastree.ast.all.UnaryOp;

/**
 * 
 * @author Matias Martinez
 *
 */
public abstract class KotlinAbstractDiffSplitterAnalyzer implements Analyzer<IRevision> {
	Logger log = Logger.getLogger(KotlinAbstractDiffSplitterAnalyzer.class.getName());

	// protected Set<String> declarations = new HashSet();
	protected Set<Class> declarations = new HashSet<>();

	@SuppressWarnings("unchecked")
	@Override
	public AnalysisResult analyze(IRevision input, RevisionResult previousResults) {
		long initCommit = (new Date()).getTime();

		DiffResult<IRevision, QueryDiff> dkotlin = (DiffResult<IRevision, QueryDiff>) previousResults
				.getResultFromClass(KotlinDiffAnalyzer.class);

		Map<String, List<QueryDiff>> group = new HashMap<>();
		Map<String, Object> keyStringToObject = new HashMap<>();

		for (String fileKotlin : dkotlin.getDiffOfFiles().keySet()) {

			long initFile = (new Date()).getTime();

			SplittedActions result = new SplittedActions();
			TreeContext context = null;
			SingleDiff fileKotlinDiff = dkotlin.getDiffOfFiles().get(fileKotlin);

			for (Action action : fileKotlinDiff.getRootOperations()) {
				context = fileKotlinDiff.getContext();
				// Parent according with the split
				ITree parentSplitElement = getSplitElement(action.getNode());

				// Find suffix corresponding to the method
				ITree methodElement = null;
				if (!(this instanceof KotlinMethodDiffSplitterAnalyzer)) {
					KotlinMethodDiffSplitterAnalyzer methodsplit = new KotlinMethodDiffSplitterAnalyzer();
					methodElement = methodsplit.getSplitElement(action.getNode());
				}

				if (parentSplitElement != null) {

					String key = getNameOfNodeKotlin(fileKotlinDiff, parentSplitElement) + "###"
							+ getHash(parentSplitElement)
							+ ((methodElement != null) ? "@@@" + methodElement.getLabel() : "");

					result.getActionsByEntityType().add(key, action);

					keyStringToObject.put(key, parentSplitElement);

				} else {
					log.debug("Kotlin element without Declaration parent ");
					result.getActionsByEntityType().add("nosplitparent", action);
				}

			}
			List<QueryDiff> diffsFromMethod = new ArrayList<QueryDiff>();

			for (String key : result.getActionsByEntityType().keySet()) {
				long initMethod = (new Date()).getTime();

				List<Action> actionsmethods = result.getActionsByEntityType().get(key);
				// The diff of one method
				QueryDiff diffGrouped = new QueryDiff(MigACore.counterQuery.getIncremented(), context, input.getName(),
						fileKotlin, "kotlin", actionsmethods);
				diffGrouped.metadata.put("split", key);

				MigACore.executionsTime.add("Kotlin" + getGranularityName() + "_Diff",
						new Long((new Date()).getTime() - initMethod));

				diffGrouped.metadata.put("method", key);
				diffGrouped.metadata.put("generator", this.getClass().getSimpleName());
				diffsFromMethod.add(diffGrouped);

			}
			group.put(fileKotlin, diffsFromMethod);

			MigACore.executionsTime.add(this.getClass().getSimpleName() + "_File",
					new Long((new Date()).getTime() - initFile));
		}
		MigACore.executionsTime.add(this.getClass().getSimpleName() + "_Commit",
				new Long((new Date()).getTime() - initCommit));

		return new DiffResult<IRevision, List<QueryDiff>>(input, group);
	}

	public static String getNameOfNodeKotlin(SingleDiff diff, ITree node) {

		if (node == null)
			return "noinfo";

		ITree parent = node;
		String nodeInfo = "";
		for (int i = 1; i <= 10; i++) {

			if (parent != null) {
				Node otype = (Node) parent.getMetadata(KastreeToITree.KASTREE_NODE);
				String content = getKotlinSignature(otype);
				nodeInfo += (!nodeInfo.isEmpty() && !content.isEmpty()) ? "_" : "";

				nodeInfo += content;

				parent = parent.getParent();
			}

		}
		return nodeInfo;
	}

	private static String getKotlinSignature(Node otype) {
		String res = "";
		if (otype == null)
			return "";

		if (otype instanceof ExprStmt) {
			res = getKotlinSignature(((ExprStmt) otype).getExpr());
		} else if (otype instanceof DeclStmt) {
			res = getKotlinSignature(((DeclStmt) otype).getDecl());
		} else if (otype instanceof Brace) {
			// nothing
			// res = getKotlinSignature((otype).getParent());
		} else if (otype instanceof Block) {
			// res = getKotlinSignature((otype).getParent());
		} else if (otype instanceof BodyFunc) {
			// res = getKotlinSignature((otype).getParent());
		} else if (otype instanceof BinaryOp) {
			BinaryOp bop = (BinaryOp) otype;

			res = otype.getClass().getSimpleName()
					+ ((bop.getRhs() instanceof Call) ? "MethodInvocation" : getKotlinSignature((bop).getOper()));
		} else if (otype instanceof UnaryOp) {
			UnaryOp bop = (UnaryOp) otype;

			res = otype.getClass().getSimpleName() + bop.getOper().getToken().getStr();
		} else if (otype instanceof Token) {
			Token r = (Token) otype;
			res = r.getToken().getStr();
		} else if (otype instanceof Structured) {
			Structured r = (Structured) otype;
			res = otype.getClass().getSimpleName() + r.getForm().name();
		} else {
			res = otype.getClass().getSimpleName();
		}
		return res;
	}

	public String getLabel(ITree parentSplitElement) {
		if (parentSplitElement == null)
			return "No-label";
		String label = parentSplitElement.getLabel();
		if (label == null || label.isEmpty()) {

			Object content = parentSplitElement.getMetadata(KastreeToITree.CONTENT);
			if (content != null) {
				String result = "";
				Node ktelement = (Node) parentSplitElement.getMetadata(KastreeToITree.KASTREE_NODE);
				if (ktelement != null) {
					result += ktelement.getKtEl().toString();
				}

				return result + content.toString().hashCode();
			}
			return getLabel(parentSplitElement.getParent());
		} else
			return parentSplitElement.getLabel();
	}

	public abstract String getGranularityName();

	public String getHash(ITree parent) {
		String parentMetadata = (String) parent.getMetadata(KastreeToITree.CONTENT);
		if (parentMetadata == null)
			return "Unknown";

		String key = "" + parentMetadata.toString().hashCode();

		if (parent.getParent() != null) {
			String parentkey = (String) parent.getParent().getMetadata(KastreeToITree.CONTENT);
			if (parentkey != null) {
				key += "+" + parentkey.toString().hashCode();
			}
		}
		return key;
	}

	public ITree getSplitElement(ITree node) {
		if (node != null) {
			Object otype = node.getMetadata(KastreeToITree.KASTREE_NODE);// KastreeToITree.TYPE
			if (otype != null) {
				// String key = otype.toString();
				// if (getDeclarations().contains(key)) {
				// return node;
				// }

				if (isInstance(otype)) {
					return node;
				}
			}

			if (node.getParent() == null)
				return null;// node;
			else
				return getSplitElement(node.getParent());

		}
		return null;
	}

	private boolean isInstance(Object otype) {
		for (Class c : this.declarations) {
			if (c.isInstance(otype)) {
				return true;
			}
		}

		return false;
	}

}
