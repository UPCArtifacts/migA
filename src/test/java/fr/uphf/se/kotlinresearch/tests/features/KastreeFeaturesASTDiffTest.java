package fr.uphf.se.kotlinresearch.tests.features;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.jetbrains.kotlin.psi.KtElement;
import org.junit.Ignore;
import org.junit.Test;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import fr.uphf.feature.FeatureDetector;
import fr.uphf.se.kotlinresearch.diff.analyzers.SingleDiff;
import fr.uphf.se.kotlinresearch.tree.analyzers.kastreeITree.KastreeParser;
import io.gitlab.arturbosch.detekt.api.Finding;
import kastree.ast.Node;

/**
 * Diff based on Kastree ASTs
 * 
 * @author Matias Martinez
 *
 */
public class KastreeFeaturesASTDiffTest {

	KastreeParser kp = new KastreeParser();

	@Test
	public void testComp1() throws IOException {

		String paths = new File("./src/test/resources/case1/MyKotlinTest_s.kt").getAbsolutePath();
		String patht = new File("./src/test/resources/case1/MyKotlinTest_t.kt").getAbsolutePath();

		SingleDiff diff = this.runDiffKastreeAst(paths, patht);

		assertTrue(diff.getAllOperations().size() > 0);

		assertTrue(diff.getRootOperations().size() > 0);

		int i = 0;

		assertEquals(1, diff.getRootOperations().size());
		showFeatures(diff);
	}

	@Test
	public void testComp2() throws IOException {

		String paths = new File("./src/test/resources/case2/f1_s.kt").getAbsolutePath();
		String patht = new File("./src/test/resources/case2/f1_t.kt").getAbsolutePath();

		runDiffKastreeAst(paths, patht);
	}

	public SingleDiff runDiffKastreeAst(String paths, String patht) throws IOException {

		Path p1 = Paths.get(paths);
		String fileContent1 = new String(Files.readAllBytes(p1));

		Path p2 = Paths.get(patht);
		String fileContent2 = new String(Files.readAllBytes(p2));

		ITree trees = kp.getITree(fileContent1);
		ITree treet = kp.getITree(fileContent2);

		System.out.println("\nTree 1 \n" + kp.print(trees));
		System.out.println("\nTree 2 \n" + kp.print(treet));

		SingleDiff diff = new SingleDiff(kp.getKtt().getContext(), trees, treet);
		assertTrue(diff.getAllOperations().size() > 0);

		assertTrue(diff.getRootOperations().size() > 0);

		System.out.println("\nResults: roots: " + diff.getRootOperations().size() + " , all: "
				+ diff.getAllOperations().size() + "\n ");

		int i = 0;
		for (Action ai : diff.getRootOperations()) {
			try {
				i++;
				System.out.println("--> " + i + "/ " + diff.getRootOperations().size() + " " + ai);
				//
				System.out
						.println("Info: " + ai.getName() + " " + ai.getNode().toPrettyString(kp.getKtt().getContext()));
				//
				System.out.println("\n");
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

		runDiffKastreeAst(paths, patht);
	}

	@Test
	public void testComp4_changeinmain() throws IOException {

		String paths = "./src/test/resources/case4/f1_s.kt";
		String patht = "./src/test/resources/case4/f1_t.kt";

		SingleDiff diff = runDiffKastreeAst(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		// One update
		assertEquals(1, diff.getRootOperations().size());
		showFeatures(diff);
	}

	public void showChanges(SingleDiff diff) {
		TreeContext context = diff.getContext();

		for (Action action : diff.getRootOperations()) {

			ITree affectedNode = action.getNode();

			System.out.println("-->" + affectedNode.toPrettyString(context));

		}
	}

	@Test
	public void testImportAndParameter() throws IOException {

		/* A new import added and one argument has its value changed */

		String paths = "./src/test/resources/kotlin_real_cases/case1/MainActivity_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case1/MainActivity_t.kt";

		SingleDiff diff = runDiffKastreeAst(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(2, diff.getRootOperations().size());
		showFeatures(diff);
	}

	@Test
	public void testModifiersRemoved() throws IOException {

		/* Four modifiers public removed */

		String paths = "./src/test/resources/kotlin_real_cases/case2/ApplicationComponent_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case2/ApplicationComponent_t.kt";

		SingleDiff diff = runDiffKastreeAst(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(4, diff.getRootOperations().size());
		showFeatures(diff);
	}

	@Test
	public void testRenamingClassName() throws IOException {

		/* Class name changed */

		String paths = "./src/test/resources/kotlin_real_cases/case4/OtherActivity_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case4/OtherActivity_t.kt";

		SingleDiff diff = runDiffKastreeAst(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(1, diff.getRootOperations().size());
		showFeatures(diff);
	}

	@Test
	@Ignore
	public void testChangVariableTypeAndValue() throws IOException {

		/* Class name changed */

		String paths = "./src/test/resources/kotlin_real_cases/case5/Dog_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case5/Dog_t.kt";

		SingleDiff diff = runDiffKastreeAst(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(2, diff.getRootOperations().size());
		showFeatures(diff);
	}

	@Test
	// Failing
	public void testChangingVarToVal() throws IOException {

		/* Making a property be read-only */

		String paths = "./src/test/resources/kotlin_real_cases/case3/KotlinActivity_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case3/KotlinActivity_t.kt";

		SingleDiff diff = runDiffKastreeAst(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(1, diff.getRootOperations().size());
		showFeatures(diff);
	}

	@Test
	public void testChangePackageName() throws IOException {

		/* Package name changed */

		String paths = "./src/test/resources/kotlin_real_cases/case6/SimpleApp_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case6/SimpleApp_t.kt";

		SingleDiff diff = runDiffKastreeAst(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(1, diff.getRootOperations().size());

		showFeatures(diff);

	}

	private void showFeatures(SingleDiff diff) {
		// kastree_node
		List<Action> actions = diff.getRootOperations();
		for (Action action : actions) {

			ITree tree = action.getNode();

			Node kastreenode = (Node) tree.getMetadata("kastree_node");
			assertNotNull(kastreenode);
			KtElement element = (KtElement) kastreenode.getKtEl();
			assertNotNull(element);
			List<Finding> findings = FeatureDetector.Companion.extractAll(element);
			System.out.println("# Feature: " + findings.size());
			if (findings.size() > 0) {
				System.out.println("Feature found: " + findings);
			}
		}
	}

	@Test
	// @Ignore
	public void testInsertPackageChangeArgument() throws IOException {

		/* Package name changed */

		String paths = "./src/test/resources/kotlin_real_cases/case7/KotlinExampleActivity_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case7/KotlinExampleActivity_t.kt";

		SingleDiff diff = runDiffKastreeAst(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(3, diff.getRootOperations().size());

	}

	@Test
	public void testChangeFunctionNameAndArgumentValue() throws IOException {

		/*
		 * Renaming function and change argument: Class Literal Expression, changing
		 * target class
		 */

		String paths = "./src/test/resources/kotlin_real_cases/case8/MainActivity_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case8/MainActivity_t.kt";

		SingleDiff diff = runDiffKastreeAst(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(2, diff.getRootOperations().size());
		showFeatures(diff);
	}

	@Test
	public void testChangeFunctionArgumentOrder() throws IOException {

		/* Changing the order of two arguments in a function call */

		String paths = "./src/test/resources/kotlin_real_cases/case9/ItemRepository_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case9/ItemRepository_t.kt";

		SingleDiff diff = runDiffKastreeAst(paths, patht);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(2, diff.getRootOperations().size());
		showFeatures(diff);
	}

	@Test
	// failing
	public void testChangeAnnotationNames() throws IOException {

		/* Changing Annotation names */

		String paths = "./src/test/resources/kotlin_real_cases/case10/SimpleAdapter_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case10/SimpleAdapter_t.kt";

		SingleDiff diff = runDiffKastreeAst(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(1, diff.getRootOperations().size());

		// TODO: does not consider annotations
		// assertEquals(4, diff.getRootOperations().size());
		showFeatures(diff);
	}

	@Test
	public void testChangingReturnExpressionComplexStringTemplate() throws IOException {

		/* Changing return expression with complex template string */

		String paths = "./src/test/resources/kotlin_real_cases/case11/helloWorld_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case11/helloWorld_t.kt";

		SingleDiff diff = runDiffKastreeAst(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		// assertEquals(9, diff.getRootOperations().size());
		showFeatures(diff);
	}

	@Test
	public void testChangingReturnExpression() throws IOException {

		/* Changing return expression to return another dot qualified expression */

		String paths = "./src/test/resources/kotlin_real_cases/case12/helloWorld_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case12/helloWorld_t.kt";

		SingleDiff diff = runDiffKastreeAst(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		// assertEquals(8, diff.getRootOperations().size());
		showFeatures(diff);
	}

	@Test
	public void testSpecifyingPropertyType() throws IOException {

		/* Specifying Property Type */

		String paths = "./src/test/resources/kotlin_real_cases/case13/OtherActivity_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case13/OtherActivity_t.kt";

		SingleDiff diff = runDiffKastreeAst(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(3, diff.getRootOperations().size());
		showFeatures(diff);
	}

	@Test
	public void testChangingBlockForOneExpressionFunc() throws IOException {

		/* Changing block expression for one expression func */

		String paths = "./src/test/resources/kotlin_real_cases/case14/GreetingController_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case14/GreetingController_t.kt";

		SingleDiff diff = runDiffKastreeAst(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(5, diff.getRootOperations().size());
		showFeatures(diff);
	}

	@Test
	// failing
	public void testChangingArgumentAndAddginArgument() throws IOException {

		/* Adding a empty list of arguments and changing a value of other argument */

		String paths = "./src/test/resources/kotlin_real_cases/case15/KotlinExampleActivity_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case15/KotlinExampleActivity_t.kt";

		SingleDiff diff = runDiffKastreeAst(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(4, diff.getRootOperations().size());
		showFeatures(diff);
	}

	@Test
	public void testChangingPropertyAndReturnType() throws IOException {

		/* Changing property and return type */

		String paths = "./src/test/resources/kotlin_real_cases/case16/MyExample_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case16/MyExample_t.kt";

		SingleDiff diff = runDiffKastreeAst(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(2, diff.getRootOperations().size());

		showFeatures(diff);
	}
}
