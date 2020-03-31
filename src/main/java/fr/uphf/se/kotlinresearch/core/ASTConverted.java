package fr.uphf.se.kotlinresearch.core;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.tree.TreeUtils;
import com.github.gumtreediff.tree.hash.StaticHashGenerator.Md5HashGenerator;

import fr.uphf.ast.ASTNode;
import gumtree.spoon.builder.SpoonGumTreeBuilder;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ASTConverted {

	static Md5HashGenerator hashGenerator = new Md5HashGenerator();

	public static ITree getRootTree(TreeContext context, ASTNode node) {

		final ITree root = context.createTree(-1, "", "root");

		ITree child = getTree(context, node);
		root.addChild(child);

		root.refresh();

		setupTree(root);

		return root;

	}

	public static void setupTree(final ITree root) {
		TreeUtils.computeDepth(root);
		TreeUtils.computeHeight(root);
		TreeUtils.computeSize(root);
		TreeUtils.preOrderNumbering(root);

		hashGenerator.hash(root);
	}

	public static ITree getTree(TreeContext context, ASTNode astNode) {

		ITree node = context.createTree(astNode.getType().hashCode(), astNode.getLabel(), astNode.getType());
		node.setMetadata(SpoonGumTreeBuilder.SPOON_OBJECT, astNode);

		for (ASTNode astChild : astNode.getChildren()) {
			ITree tree = getTree(context, astChild);
			if (tree != null)
				node.addChild(tree);
		}

		return node;
	}
}
