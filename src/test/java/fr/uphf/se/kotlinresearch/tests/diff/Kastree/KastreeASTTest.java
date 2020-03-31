package fr.uphf.se.kotlinresearch.tests.diff.Kastree;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.kotlin.psi.KtElement;
import org.junit.Test;

import com.github.gumtreediff.tree.ITree;

import fr.uphf.se.kotlinresearch.tree.analyzers.kastreeITree.KastreeParser;
import kastree.ast.Node;
import kastree.ast.Visitor;
import kastree.ast.all.File;

/**
 * Diff based on Kastree ASTs
 * 
 * @author Matias Martinez
 *
 */
public class KastreeASTTest {

	KastreeParser kp = new KastreeParser();

	@Test
	public void testLoad1() throws IOException {

		Path p = Paths.get("./src/test/resources/case1/MyKotlinTest_s.kt");
		String fileContent = new String(Files.readAllBytes(p));
		System.out.println(fileContent);

		KastreeParser kp = new KastreeParser();
		ITree treeFile = kp.getITree(fileContent);
		// System.out.println(treeFile.toTreeString());
		System.out.println(kp.print(treeFile));
	}

	@Test
	public void testGetRoleFromAST() throws IOException {

		Path p = Paths.get("./src/test/resources/case1/MyKotlinTest_s.kt");
		String fileContent = new String(Files.readAllBytes(p));
		KastreeParser kp = new KastreeParser();
		File fileKastree = kp.getKastreeASTComplet(fileContent);

		final List<Object> roles = new ArrayList<>();

		Visitor visitorVisual = new Visitor() {

			@Override
			protected void visit(Node nodeChild, Node nodeParent) {
				super.visit(nodeChild, nodeParent);
				if (nodeChild != null) {

					Object role = nodeChild.getMetadata().get("role");
					if (role != null)
						roles.add(role);
				}
			}

		};

		visitorVisual.visit(fileKastree);
		System.out.println("Roles:\n" + roles);
		assertTrue(roles.size() > 0);

	}

	@Test
	public void testKtElement() throws IOException {

		Path p = Paths.get("./src/test/resources/case1/MyKotlinTest_s.kt");
		String fileContent = new String(Files.readAllBytes(p));
		KastreeParser kp = new KastreeParser();
		File fileKastree = kp.getKastreeASTComplet(fileContent);

		final List<Object> ktElements = new ArrayList<>();

		Visitor visitorVisual = new Visitor() {

			@Override
			protected void visit(Node nodeChild, Node nodeParent) {
				super.visit(nodeChild, nodeParent);
				if (nodeChild != null) {

					if (nodeChild.getKtEl() != null) {
						assertTrue(nodeChild.getKtEl() instanceof KtElement);
						ktElements.add(nodeChild.getKtEl());
					}
				}
			}

		};
		visitorVisual.visit(fileKastree);
		System.out.println("Roles:\n" + ktElements);
		assertTrue(ktElements.size() > 0);
	}

	@Test
	public void testLoadc4_1failing() throws IOException {

		Path p = Paths.get("./src/test/resources/case4/f1_t.kt");
		String fileContent = new String(Files.readAllBytes(p));
		System.out.println(fileContent);

		KastreeParser kp = new KastreeParser();
		ITree treeFile = kp.getITree(fileContent);
		// System.out.println(treeFile.toTreeString());
		System.out.println(kp.print(treeFile));
	}

}
