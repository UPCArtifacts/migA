package fr.uphf.se.kotlinresearch.tests.diff.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.github.gumtreediff.tree.ITree;

import fr.uphf.se.kotlinresearch.tree.analyzers.JavaTreeAnalyzer;
import gumtree.spoon.AstComparator;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtElement;

/**
 * 
 * @author Matias Martinez
 *
 */
public class JavaTreeParserTest {

	@Test
	public void testPosition() {

		JavaTreeAnalyzer jtree = new JavaTreeAnalyzer();
		String c1 = "" + "class X {\n" + "public void foo() {\n" + " int x = 0;\n" + "}" + "};";
		ITree root = jtree.getTree(c1);
		boolean found = false;
		for (ITree t : root.getDescendants()) {
			CtElement el = (CtElement) t.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			if (!(el.getPosition() instanceof NoSourcePosition)) {
				found = true;
				System.out.println("-With pos-> " + el);
			} else {
				System.out.println("-Without pos-> " + el);
			}
			// System.out.println(el.getPosition().getLine());

		}
		assertTrue(found);

	}

	@Test
	public void test_bug_Possition_from_String() {
		String c1 = "" + "class X {\n" + "public void foo() {\n" + " int x = 0;\n" + "}" + "};";

		String c2 = "" + "class X {\n" + "public void foo() {\n" + " int x = 1;\n" + "}" + "};";

		AstComparator diff = new AstComparator();
		Diff editScript = diff.compare(c1, c2);
		assertTrue(editScript.getRootOperations().size() == 1);

		List<Operation> rootOperations = editScript.getRootOperations();

		assertEquals(1, rootOperations.size());

		SourcePosition position = rootOperations.get(0).getSrcNode().getPosition();
		assertTrue(!(position instanceof NoSourcePosition));

		assertTrue(position.getLine() > 0);
		assertEquals(3, position.getLine());

	}
}
