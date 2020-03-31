package fr.uphf.se.kotlinresearch.tests.diff.KotlinJSON;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import fr.uphf.ast.ASTNode;
import fr.uphf.se.kotlinresearch.core.ASTConverted;
import fr.uphf.se.kotlinresearch.squarediff.entities.diff.SingleDiff;

/**
 * Diff of Kotlin AST based on the version that provides JSON from Kotlin. We
 * should not use this version
 * 
 * @author Matias Martinez
 *
 */
public class KotlinJSONDiffTest {
	TreeContext context = new TreeContext();

	@Test
	public void testLoad1() throws IOException {

		// File.separator + "tmp" + File.separator + "teste.kt";
		Path p = Paths.get("./src/test/resources/case1/MyKotlinTest_s.kt");
		String fileContent = new String(Files.readAllBytes(p));

		ASTNode root = fr.uphf.analyze.Helper.getASTasJson(fileContent, p.getFileName().toString());

		String ast = fr.uphf.analyze.Helper.getASTasStringJson(root);
		System.out.println(ast);

		ITree tree = ASTConverted.getTree(context, root.getChild(2));
		tree.refresh();
		System.out.println("-->" + tree.getHeight());
		System.out.println("tree: " + tree);
		assertNotNull(tree);

		assertTrue(tree.getHeight() > 1);

	}

	@Test
	public void testComp1() throws IOException {

		String paths = new File("./src/test/resources/case1/MyKotlinTest_s.kt").getAbsolutePath();
		String patht = new File("./src/test/resources/case1/MyKotlinTest_t.kt").getAbsolutePath();

		// File.separator + "tmp" + File.separator + "teste.kt";

		Path p1 = Paths.get("./src/test/resources/case1/MyKotlinTest_s.kt");
		String fileContent1 = new String(Files.readAllBytes(p1));

		Path p2 = Paths.get("./src/test/resources/case1/MyKotlinTest_t.kt");
		String fileContent2 = new String(Files.readAllBytes(p2));

		ASTNode root1 = fr.uphf.analyze.Helper.getASTasJson(fileContent1, p1.getFileName().toString());

		ASTNode root2 = fr.uphf.analyze.Helper.getASTasJson(fileContent2, p2.getFileName().toString());

		ITree trees = getTree(root1);

		ITree treet = getTree(root2);

		assertTrue(trees.getHeight() > 1);
		assertTrue(treet.getHeight() > 1);

		String ast1 = fr.uphf.analyze.Helper.getASTasStringJson(root1);
		String ast2 = fr.uphf.analyze.Helper.getASTasStringJson(root2);

		assertNotEquals(ast1, ast2);

		SingleDiff diff = new SingleDiff(this.context, trees, treet);

		assertTrue(diff.getAllOperations().size() > 0);

		assertTrue(diff.getRootOperations().size() > 0);
		int i = 0;
		for (Action ai : diff.getRootOperations()) {
			try {
				System.out.println("--> " + ++i + "/ " + diff.getRootOperations().size() + " " + ai);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		assertEquals(1, diff.getRootOperations().size());

	}

	@Test
	public void testComp2() throws IOException {

		String paths = new File("./src/test/resources/case2/f1_s.kt").getAbsolutePath();
		String patht = new File("./src/test/resources/case2/f1_t.kt").getAbsolutePath();

		run(paths, patht);
	}

	public SingleDiff run(String paths, String patht) throws IOException {

		// File.separator + "tmp" + File.separator + "teste.kt";

		Path p1 = Paths.get(paths);
		String fileContent1 = new String(Files.readAllBytes(p1));

		Path p2 = Paths.get(patht);
		String fileContent2 = new String(Files.readAllBytes(p2));

		ASTNode root1 = fr.uphf.analyze.Helper.getASTasJson(fileContent1, p1.getFileName().toString());

		ASTNode root2 = fr.uphf.analyze.Helper.getASTasJson(fileContent2, p2.getFileName().toString());

		// System.out.println("\n----AST S:\n " + t.getStringAST());
		// System.out.println("\n----AST t:\n " + t2.getStringAST());

		ITree trees = getTree(root1);
		ITree treet = getTree(root2);

		// System.out.println("\n----Tree S:\n " + trees.toPrettyString(context));
		// System.out.println("\n----Tree t:\n " + treet.toPrettyString(context));

		assertTrue(trees.getHeight() > 1);
		assertTrue(treet.getHeight() > 1);

		SingleDiff diff = new SingleDiff(this.context, trees, treet);
		assertTrue(diff.getAllOperations().size() > 0);

		assertTrue(diff.getRootOperations().size() > 0);
		int i = 0;
		for (Action ai : diff.getRootOperations()) {
			try {
				i++;
				System.out.println("--> " + i);
				//
				System.out.println("Info: " + ai.getName() + " " + ai.getNode());
				//
				System.out.println("--> " + i + "/ " + diff.getRootOperations().size() + " " + ai);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// assertEquals(1, diff.getRootOperations().size());
		return diff;
	}

	@Test
	@Ignore
	// TODO:seed on change in source, execute and assert result
	public void testComp3() throws IOException {

		String paths = "./src/test/resources/case3/f1_s.kt";
		String patht = "./src/test/resources/case3/f1_t.kt";

		run(paths, patht);
	}

	@Test
	public void testComp4_changeinmain() throws IOException {

		String paths = "./src/test/resources/case4/f1_s.kt";
		String patht = "./src/test/resources/case4/f1_t.kt";

		SingleDiff diff = run(paths, patht);

		TreeContext context = diff.getContext();

		for (Action action : diff.getRootOperations()) {

			ITree affectedNode = action.getNode();

			System.out.println("-->" + affectedNode.toPrettyString(context));

		}

		assertTrue(diff.getRootOperations().size() > 0);
		// One update
		assertEquals(1, diff.getRootOperations().size());

	}

	public ITree getTree(ASTNode astNode) {

		ITree tree = ASTConverted.getRootTree(context, astNode);

		assertNotNull(tree);
		return tree;
	}

}
