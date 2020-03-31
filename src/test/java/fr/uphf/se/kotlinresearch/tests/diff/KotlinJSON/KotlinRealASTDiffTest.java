package fr.uphf.se.kotlinresearch.tests.diff.KotlinJSON;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.tree.TreeUtils;
import com.google.gson.JsonObject;

import fr.uphf.ast.ASTNode;
import fr.uphf.se.kotlinresearch.core.ASTConverted;
import fr.uphf.se.kotlinresearch.squarediff.entities.diff.SingleDiff;
import gumtree.spoon.builder.Json4SpoonGenerator;

/**
 * 
 * @author Matias Martinez
 *
 */
public class KotlinRealASTDiffTest {
	TreeContext context = new TreeContext();

	public String toTreeString(ITree itree) {
		StringBuilder b = new StringBuilder();
		for (ITree t : TreeUtils.preOrder(itree))
			b.append(indent(t) + // t.toPrettyString(context)
					context.getTypeLabel(t) + "@@" + ((t.getLabel() != null) ? t.getLabel() : "") + "\n");
		return b.toString();
	}

	private String indent(ITree t) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < t.getDepth(); i++)
			b.append("\t");
		return b.toString();
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

		Json4SpoonGenerator jsongen = new Json4SpoonGenerator();

		JsonObject js = jsongen.getJSONasJsonObject(context, trees);
		JsonObject jt = jsongen.getJSONasJsonObject(context, treet);

		String ss = this.toTreeString(trees);
		System.out.println("ss: \n" + ss);

		System.out.println("st: \n" + this.toTreeString(treet));

		System.out.println("js: " + js);
		System.out.println("jt: " + jt);

		// System.out.println("\n----Tree S:\n " + trees.toPrettyString(context));
		// System.out.println("\n----Tree t:\n " + treet.toPrettyString(context));

		assertTrue(trees.getHeight() > 1);
		assertTrue(treet.getHeight() > 1);

		String ast1 = fr.uphf.analyze.Helper.getASTasStringJson(root1);
		String ast2 = fr.uphf.analyze.Helper.getASTasStringJson(root2);

		SingleDiff diff = new SingleDiff(this.context, trees, treet);
		assertTrue(diff.getAllOperations().size() > 0);

		assertTrue(diff.getRootOperations().size() > 0);
		int i = 0;
		for (Action ai : diff.getRootOperations()) {
			try {
				i++;
				System.out.println("--> " + i + "/ " + diff.getRootOperations().size());
				//
				System.out.println("Info: " + ai.getName() + " " + diff.getContext().getTypeLabel(ai.getNode()));
				//
				// System.out.println("--> " + i + " " + ai);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// assertEquals(1, diff.getRootOperations().size());
		return diff;
	}

	public ITree getTree(ASTNode astNode) {

		ITree tree = ASTConverted.getRootTree(context, astNode);

		assertNotNull(tree);
		return tree;
	}

	@Test
	public void test_case1_ImportAndParameter() throws IOException {

		/* A new import added and one argument has its value changed */

		String paths = "./src/test/resources/kotlin_real_cases/case1/MainActivity_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case1/MainActivity_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(2, diff.getRootOperations().size());

		Optional<Action> actionImport = diff.getRootOperations().stream()
				.filter(e -> context.getTypeLabel(e.getNode()).equals("KtImportDirective")).findAny();

		assertTrue(actionImport.isPresent());

		Update uacim = (Update) actionImport.get();
		ITree paramTree = (ITree) actionImport.get().getNode();

		assertNotNull(paramTree);

		assertEquals("kotlinx.android.synthetic.main.*", paramTree.getLabel());
		assertEquals("kotlinx.android.synthetic.main.activity_main.*", uacim.getValue());

		Optional<Action> actionName = diff.getRootOperations().stream()
				.filter(e -> context.getTypeLabel(e.getNode()).equals("KtNameReferenceExpression")).findAny();

		assertTrue(actionName.isPresent());

		Update uacname = (Update) actionName.get();
		ITree paramname = (ITree) actionName.get().getNode();

		assertEquals("activity_main", uacname.getValue());

		// System.out.println("parents: " + paramname.getParents());
		for (ITree p : paramname.getParents()) {
			System.out.println(p.toPrettyString(context));
		}
	}

	@Test
	public void test_case2_ModifiersRemoved() throws IOException {

		/* Four modifiers public removed */

		String paths = "./src/test/resources/kotlin_real_cases/case2/ApplicationComponent_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case2/ApplicationComponent_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(4, diff.getRootOperations().size());

	}

	@Test
	public void test_case3_ChangingVarToVal() throws IOException {

		/* Making a property be read-only */

		String paths = "./src/test/resources/kotlin_real_cases/case3/KotlinActivity_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case3/KotlinActivity_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(1, diff.getRootOperations().size());

		Optional<Action> actionName = diff.getRootOperations().stream()
				.filter(e -> context.getTypeLabel(e.getNode()).equals("KtPropertyKeyword")).findAny();

		assertTrue(actionName.isPresent());

		Update uacname = (Update) actionName.get();
		ITree paramname = (ITree) actionName.get().getNode();

		assertEquals("val", uacname.getValue());
		assertEquals("var", paramname.getLabel());

		// System.out.println("parents: " + paramname.getParents());
		for (ITree p : paramname.getParents()) {
			System.out.println(p.toPrettyString(context));
		}

	}

	@Test
	public void test_case4_RenamingClassName() throws IOException {

		/* Class name changed */

		String paths = "./src/test/resources/kotlin_real_cases/case4/OtherActivity_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case4/OtherActivity_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(1, diff.getRootOperations().size());

		assertTrue(diff.getRootOperations().get(0) instanceof Update);
		Update up = (Update) diff.getRootOperations().get(0);

		assertEquals("SecondActivity", up.getNode().getLabel());
		assertEquals("OtherActivity", up.getValue());

	}

	@Test
	// @Ignore
	public void test_case5_ChangVariableTypeAndValue() throws IOException {

		/* Class name changed */

		String paths = "./src/test/resources/kotlin_real_cases/case5/Dog_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case5/Dog_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		// assertEquals(2, diff.getRootOperations().size());

		TreeContext context = diff.getContext();

		detailledPrint(diff, context);

		Optional<Action> deleteName = diff.getAllOperations().stream().filter(e -> e.getNode().getLabel().equals("mm"))
				.findAny();

		assertTrue(deleteName.isPresent());
		assertTrue(deleteName.get() instanceof Delete);

		Optional<Action> insertNull = diff.getRootOperations().stream()
				.filter(e -> e.getNode().getLabel().equals("null")).findAny();

		assertTrue(insertNull.isPresent());

		assertTrue(insertNull.get() instanceof Insert);

		// TODO: remove type in ast KtStringTemplateExpression

	}

	public void detailledPrint(SingleDiff diff, TreeContext context) {
		System.out.println("\nRoot node: ");
		for (Action action : diff.getRootOperations()) {

			ITree affectedNode = action.getNode();

			System.out.println(action.getName() + "-->" + affectedNode.toPrettyString(context) + "| value: "
					+ affectedNode.getLabel()
					+ ((action instanceof Update) ? (" new value: " + ((Update) action).getValue()) : "") //
					+ "\n\t\t| parent: "
					+ affectedNode.getParents().stream().map(e -> "(" + e.toPrettyString(context) + ") ")
							.collect(Collectors.toList())
					+ "\n\t\t| children: " + affectedNode.getDescendants().stream()
							.map(e -> "(" + e.toPrettyString(context) + ")").collect(Collectors.toList()));

		}
	}

	@Test
	public void test_case_6_ChangePackageName() throws IOException {

		/* Package name changed */

		String paths = "./src/test/resources/kotlin_real_cases/case6/SimpleApp_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case6/SimpleApp_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(1, diff.getRootOperations().size());

	}

	@Test
	// @Ignore
	public void test_case7_InsertPackageChangeArgument() throws IOException {

		/* Package name changed */

		String paths = "./src/test/resources/kotlin_real_cases/case7/KotlinExampleActivity_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case7/KotlinExampleActivity_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		detailledPrint(diff, context);

		assertTrue(diff.getRootOperations().size() > 0);
		// assertEquals(2, diff.getRootOperations().size());

	}

	@Test
	public void test_case8_ChangeFunctionNameAndArgumentValue() throws IOException {

		/*
		 * Renaming function and change argument: Class Literal Expression, changing
		 * target class
		 */

		String paths = "./src/test/resources/kotlin_real_cases/case8/MainActivity_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case8/MainActivity_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		detailledPrint(diff, context);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(2, diff.getRootOperations().size());

	}

	@Test
	public void test_case9_ChangeFunctionArgumentOrder() throws IOException {

		/* Changing the order of two arguments in a function call */

		String paths = "./src/test/resources/kotlin_real_cases/case9/ItemRepository_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case9/ItemRepository_t.kt";

		SingleDiff diff = run(paths, patht);

		assertTrue(diff.getRootOperations().size() > 0);
		// assertEquals(2, diff.getRootOperations().size());

		detailledPrint(diff, context);

	}

	@Test
	public void test_case10_ChangeAnnotationNames() throws IOException {

		/* Changing Annotation names */

		String paths = "./src/test/resources/kotlin_real_cases/case10/SimpleAdapter_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case10/SimpleAdapter_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(4, diff.getRootOperations().size());

		detailledPrint(diff, context);
	}

	@Test
	public void test_case11_ChangingReturnExpressionComplexStringTemplate() throws IOException {

		/* Changing return expression with complex template string */

		String paths = "./src/test/resources/kotlin_real_cases/case11/helloWorld_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case11/helloWorld_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		// assertEquals(9, diff.getRootOperations().size());

		detailledPrint(diff, context);

	}

	@Test
	public void test_case12_ChangingReturnExpression() throws IOException {

		/* Changing return expression to return another dot qualified expression */

		String paths = "./src/test/resources/kotlin_real_cases/case12/helloWorld_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case12/helloWorld_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		// assertEquals(8, diff.getRootOperations().size());

		detailledPrint(diff, context);

	}

	@Test
	public void testSpecifyingPropertyType() throws IOException {

		/* Specifying Property Type */

		String paths = "./src/test/resources/kotlin_real_cases/case13/OtherActivity_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case13/OtherActivity_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(3, diff.getRootOperations().size());

	}

	@Test
	public void testChangingBlockForOneExpressionFunc() throws IOException {

		/* Changing block expression for one expression func */

		String paths = "./src/test/resources/kotlin_real_cases/case14/GreetingController_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case14/GreetingController_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(5, diff.getRootOperations().size());

	}

	@Test
	public void testChangingArgumentAndAddginArgument() throws IOException {

		/* Adding a empty list of arguments and changing a value of other argument */

		String paths = "./src/test/resources/kotlin_real_cases/case15/KotlinExampleActivity_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case15/KotlinExampleActivity_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(5, diff.getRootOperations().size());

	}

	@Test
	public void testChangingPropertyAndReturnType() throws IOException {

		/* Changing property and return type */

		String paths = "./src/test/resources/kotlin_real_cases/case16/MyExample_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case16/MyExample_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(2, diff.getRootOperations().size());

	}

	private void showChanges(SingleDiff diff) {
		TreeContext context = diff.getContext();

		for (Action action : diff.getRootOperations()) {

			ITree affectedNode = action.getNode();

			System.out.println(action.getName() + "-->" + affectedNode.toPrettyString(context));

		}
	}

}
