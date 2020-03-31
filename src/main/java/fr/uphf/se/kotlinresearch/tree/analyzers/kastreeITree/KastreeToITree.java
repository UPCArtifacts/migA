package fr.uphf.se.kotlinresearch.tree.analyzers.kastreeITree;

import java.util.Set;

import org.apache.log4j.Logger;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.tree.TreeUtils;
import com.google.common.collect.Sets;

import kastree.ast.Node;
import kastree.ast.Writer;

/**
 * 
 * @author Matias Martinez
 *
 */
public class KastreeToITree {

	public static final String TYPE = "type";
	public static final String NAME = "name";
	public static final String ROLE = "role";
	public static final String KASTREE_NODE = "kastree_node";
	// Content of the node, recursively
	public static final String CONTENT = "content";
	protected TreeContext context = new TreeContext();

	Logger log = Logger.getLogger(KastreeToITree.class.getName());

	public ITree transformWithRoot(Node kNode) {
		ITree rootT = new Tree(-1, "");

		return rootT;
	}

	public ITree transformToITree(Node kNode) {

		ITree treeFile = this.transform(kNode);

		TreeUtils.computeDepth(treeFile);
		TreeUtils.computeHeight(treeFile);
		TreeUtils.computeSize(treeFile);
		TreeUtils.preOrderNumbering(treeFile);

		return treeFile;
	}

	private ITree transform(Node kNode) {

		if (kNode == null) {
			return null;
		}

		Object nameMetadata = kNode.getMetadata().get(NAME);
		String label = (nameMetadata != null) ? nameMetadata.toString() : "";

		String typeClass = kNode.getClass().getName();

		ITree currentNode = context.createTree(typeClass.hashCode(), label, typeClass);

		currentNode.setMetadata(KASTREE_NODE, kNode);
		// For test
		currentNode.setMetadata(TYPE, typeClass);

		try {
			String contentString = Writer.Companion.write(kNode, null);
			currentNode.setMetadata(CONTENT, contentString);
		} catch (Exception e) {
			log.error("error when getting the string of a Kastree node: " + e.getMessage());
			e.printStackTrace();
		}

		Object roleMetadata = kNode.getMetadata().get(ROLE);
		String role = (roleMetadata != null) ? roleMetadata.toString() : "";

		currentNode.setMetadata(ROLE, role);

		for (Node childNode : kNode.getChildren()) {

			ITree child = transform(childNode);
			if (child != null) {
				currentNode.addChild(child);
			}
		}

		formatSpecialCases(typeClass, currentNode);

		return currentNode;

	}

	String BINARYOPCONTANT = "kastree.ast.all.BinaryOp";// "kastree.ast.Node$Expr$BinaryOp";

	private void formatSpecialCases(String typeClass, ITree currentNode) {

		if (typeClass.equals(BINARYOPCONTANT)) {
			processBinaryOp(currentNode);

		}

	}

	final Set<String> assignment = Sets.newHashSet("ASSN", "MUL_ASSN", "DIV_ASSN", "MOD_ASSN", "ADD_ASSN", "SUB_ASSN");
	final Set<String> calls = Sets.newHashSet("DOT", "DOT_SAFE", "SAFE");
	final String ASSIGNMENT_STR = "ASSIGNMENT";
	final String CALL = "MethodInvocation";
	final String DOT = "DOT";

	private void processBinaryOp(ITree currentNode) {
		ITree token = currentNode.getChild(1);
		if (token != null) {
			String operation = token.getLabel();

			if (assignment.contains(operation)) {
				// We do it for registring the type...
				ITree fake = context.createTree(ASSIGNMENT_STR.hashCode(), "", ASSIGNMENT_STR);
				currentNode.setType(fake.getType());
				currentNode.setMetadata(TYPE, ASSIGNMENT_STR);
			} else if (calls.contains(operation)) {
				// Now we check the right part to see if it's a method inv.
				ITree isCall = currentNode.getChild(2);
				if (isCall != null) {

					String PATH_TO_CALL_OBJECT = "kastree.ast.all.Call";
					String typeLable = context.getTypeLabel(isCall);
					String newType = typeLable;

					if (typeLable.equals(PATH_TO_CALL_OBJECT)) {
						newType = CALL;
					} else if (typeLable.equals("kastree.ast.all.Name")) {
						newType = "Property";
					}

					// String newType = (context.getTypeLabel(isCall));

					// We do it for registring the type...
					ITree fake = context.createTree(newType.hashCode(), "", newType);
					currentNode.setType(fake.getType());
					currentNode.setMetadata(TYPE, newType);
				}
			}

		}
	}

	public TreeContext getContext() {
		return context;
	}

	public void setContext(TreeContext context) {
		this.context = context;
	}

}
