package fr.uphf.se.kotlinresearch.tests.evolution;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import fr.uphf.se.kotlinresearch.core.MigaMain;

public class TestRealCasesTest {

	@Test
	public void testPoet() throws Exception {

		MigaMain main = new MigaMain();
		String projectName = "poet-assistant";
		String pathToKotlinRepo = "/Users/matias/develop/kotlinresearch/dataset_kotlin_migration/" + projectName;
		Map<String, List> commitsByBranch = main.runExperiment(new File(pathToKotlinRepo));

		System.out.println(commitsByBranch);

	}

	@Test
	public void testStatus() throws Exception {

		MigaMain main = new MigaMain();
		String projectName = "status";
		String pathToKotlinRepo = "/Users/matias/develop/kotlinresearch/dataset_kotlin_migration/" + projectName;
		Map<String, List> commitsByBranch = main.runExperiment(new File(pathToKotlinRepo));

		System.out.println(commitsByBranch);

	}

	@Test
	public void testAndroidPasswordStore() throws Exception {

		MigaMain main = new MigaMain();
		String projectName = "Android-Password-Store";
		String pathToKotlinRepo = "/Users/matias/develop/kotlinresearch/dataset_kotlin_migration/" + projectName;
		Map<String, List> commitsByBranch = main.runExperiment(new File(pathToKotlinRepo));

		System.out.println(commitsByBranch);

	}

	@Test
	public void testAll() throws Exception {

		MigaMain main = new MigaMain();

		String pathToKotlinRepo = "/Users/matias/develop/kotlinresearch/dataset_kotlin_migration/";

		File nf = new File(pathToKotlinRepo);

		for (File project : nf.listFiles()) {
			try {
				if (project.getName().equals("Osmand")) {
					System.out.println("Skip: " + project.getName());
					continue;
				}

				String outpath = new File("./coming_results/").getAbsolutePath() + File.separator
						+ main.getFileNameOfOutput(project) + ".json";
				File outFileProject = new File(outpath);
				if (project.isDirectory()) {

					if (outFileProject.exists()) {
						System.out.println(project + " already analyzed");
						continue;
					} else {
						System.out.println(project + " to be analyzed");

					}

					System.out.println("\n*****Analyzing " + project);
					Map<String, List> commitsByBranch = main.runExperiment((project));

					System.out.println("Results of: " + project);
					System.out.println(commitsByBranch);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
