package fr.uphf.se.kotlinresearch.tests.features;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.kotlin.psi.KtElement;
import org.jetbrains.kotlin.psi.KtFile;
import org.junit.Test;

import fr.uphf.feature.FeatureDetector;
import fr.uphf.se.kotlinresearch.tree.analyzers.kastreeITree.KastreeParser;
import io.gitlab.arturbosch.detekt.api.Feature;
import io.gitlab.arturbosch.detekt.api.Finding;
import kastree.ast.Node;
import kastree.ast.Visitor;
import kastree.ast.all.File;

/**
 * 
 * @author Matias Martinez
 *
 */
public class KotlinFeaturesFromKastreeASTTest {

	@Test
	public void testFeaturesFromKastreeAst() throws IOException {

		Path p = Paths.get("./src/test/resources/case1/MyKotlinTest_s.kt");
		String fileContent = new String(Files.readAllBytes(p));
		KastreeParser kp = new KastreeParser();
		File fileKastree = kp.getKastreeASTComplet(fileContent);

		assertNotNull(fileKastree);

		KtFile ktFile = (KtFile) fileKastree.getKtEl();

		assertNotNull(ktFile);

		List<Finding> featuresFound = FeatureDetector.Companion.extractAll(ktFile);

		assertNotNull(featuresFound);

		System.out.println("Features:\n" + featuresFound);

		assertTrue(featuresFound.size() > 0);

		for (Finding finding : featuresFound) {
			System.out.println("feature: " + finding.getId() + " , name: " + finding.getName() + " , message: "
					+ finding.getMessage() + " , entity: " + finding.getEntity());
		}

		List<Feature> features = featuresFound.stream().map(Feature.class::cast).collect(Collectors.toList());
		assertTrue(features.size() > 0);
	}

	@Test
	public void testFeaturesByElement() throws IOException {

		Path p = Paths.get("./src/test/resources/case1/MyKotlinTest_s.kt");
		String fileContent = new String(Files.readAllBytes(p));
		KastreeParser kp = new KastreeParser();
		File fileKastree = kp.getKastreeASTComplet(fileContent);

		Map<Node, List<Finding>> featuresByNode = new java.util.HashMap<>();
		Visitor visitorVisual = new Visitor() {

			@Override
			protected void visit(Node nodeChild, Node nodeParent) {
				super.visit(nodeChild, nodeParent);
				if (nodeChild != null) {

					if (nodeChild.getKtEl() != null) {
						assertTrue(nodeChild.getKtEl() instanceof KtElement);

						List<Finding> featuresFound = FeatureDetector.Companion
								.extractAll((KtElement) nodeChild.getKtEl());
						if (featuresFound.size() > 0) {
							featuresByNode.put(nodeChild, featuresFound);
						}

					}
				}
			}

		};
		visitorVisual.visit(fileKastree);

		System.out.println("Map:\n" + featuresByNode.keySet().size());

		assertTrue(featuresByNode.keySet().size() > 1);

	}
}
