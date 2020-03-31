package fr.uphf.se.kotlinresearch.tree.analyzers.kastreeITree;

import org.apache.log4j.Logger;
import org.jetbrains.kotlin.psi.KtElement;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.tree.TreeUtils;

import kastree.ast.Node;
import kastree.ast.Visitor;
import kastree.ast.VisitorLabel;
import kastree.ast.all.File;
import kastree.ast.psi.Parser;

/**
 * 
 * @author Matias Martinez
 *
 */
public class KastreeParser {

	KastreeToITree ktt = new KastreeToITree();
	Parser par = new Parser();
	Logger log = Logger.getLogger(KastreeParser.class.getName());

	Visitor visitorParentChild = new Visitor() {

		@Override
		protected void visit(Node nodeChild, Node nodeParent) {
			super.visit(nodeChild, nodeParent);

			if (nodeChild != null && nodeChild != nodeParent) {
				nodeParent.getChildren().add(nodeChild);
				nodeChild.setParent(nodeParent);
			}
		}

	};

	Visitor visitorVisual = new Visitor() {

		@Override
		protected void visit(Node nodeChild, Node nodeParent) {
			super.visit(nodeChild, nodeParent);
			if (nodeChild != null) {
				System.out.println("\n***-name-> " + nodeChild.getMetadata().get("name") + " " + nodeChild.getClass()
						+ "\n-role-> " + nodeChild.getMetadata().get("role")
				//
						+ "\n-ktelement-> "
						+ ((nodeChild.getKtEl() != null) ? ((KtElement) nodeChild.getKtEl()).getClass().getName()
								: null)

				);

			}
		}

	};

	public ITree getITree(String fileContent) {

		try {
			File fileKastree = getKastreeASTComplet(fileContent);

			ITree treeFile = ktt.transformToITree(fileKastree);

			return treeFile;
		} catch (Exception e) {
			log.error("Error parsing the kotlin content (we return null)");
			e.printStackTrace();
			return null;
		}
	}

	public File getKastreeASTComplet(String fileContent) {
		File fileKastree = par.parseFile(fileContent, true);

		visitorParentChild.visit(fileKastree);

		VisitorLabel labeler = new VisitorLabel();
		labeler.visit(fileKastree);

		// VisitorRole vRoler = new VisitorRole();
		// vRoler.visit(fileKastree);
		return fileKastree;
	}

	public String print(ITree tree) {

		StringBuilder b = new StringBuilder();
		for (ITree t : TreeUtils.preOrder(tree))
			b.append(indent(t) + toPrettyString(t, this.ktt.context) + "\n");
		return b.toString();

	}

	private String indent(ITree t) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < t.getDepth(); i++)
			b.append("\t");
		return b.toString();
	}

	public String toPrettyString(ITree tree, TreeContext ctx) {

		return ctx.getTypeLabel(tree) + "_" + tree.getType() + ": " + tree.getLabel();

	}

	public KastreeToITree getKtt() {
		return ktt;
	}

	public void setKtt(KastreeToITree ktt) {
		this.ktt = ktt;
	}
}
