package fr.uphf.se.kotlinresearch.tests.diff.Kastree;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.github.gumtreediff.tree.ITree;

import fr.uphf.se.kotlinresearch.tree.analyzers.kastreeITree.KastreeParser;
import fr.uphf.se.kotlinresearch.tree.analyzers.kastreeITree.KastreeToITree;
import kastree.ast.Node;
import kastree.ast.all.File;

/**
 * 
 * @author Matias Martinez
 *
 */
public class KastreeToITreeTest {

	@Test
	public void testKtElement() throws IOException {

		Path p = Paths.get("./src/test/resources/case1/MyKotlinTest_s.kt");
		String fileContent = new String(Files.readAllBytes(p));
		KastreeParser kp = new KastreeParser();
		File fileKastree = kp.getKastreeASTComplet(fileContent);

		KastreeToITree conv = new KastreeToITree();
		ITree tree = conv.transformToITree(fileKastree);
		assertNotNull(tree);
		assertTrue(tree.getHeight() > 1);

		checkWithNode(tree);

	}

	private void checkWithNode(ITree tree) {

		Object kastreenode = tree.getMetadata("kastree_node");
		assertNotNull(kastreenode);

		assertTrue(kastreenode instanceof Node);

		for (ITree t : tree.getChildren()) {
			checkWithNode(t);
		}

	}

}
