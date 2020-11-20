package fr.uphf.se.kotlinresearch.tests.evolution;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.lib.Ref;
import org.junit.Test;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.main.ComingMain;
import fr.inria.coming.main.ComingProperties;
import fr.uphf.se.kotlinresearch.core.MigACore;
import fr.uphf.se.kotlinresearch.core.MigaV2;
import fr.uphf.se.kotlinresearch.core.results.MigAIntermediateResultStore;
import fr.uphf.se.kotlinresearch.diff.analyzers.JavaDiffAnalyzer;

/**
 * 
 * @author Matias Martinez
 *
 */
public class MigAGitRunnerTest2 {
	Logger log = Logger.getLogger(JavaDiffAnalyzer.class.getName());
	final int idem = 0;

	public void run(String projectName, String branch, String maxCommits) throws Exception {
		String pathToKotlinRepo = "/Users/matias/develop/kotlinresearch/dataset_kotlin_migration/" + projectName;
		File locationKotinRepo = new File(pathToKotlinRepo);
		assertTrue(locationKotinRepo.exists());
		ComingMain cm = new ComingMain();
		System.out.println("Running with branch: " + branch);

		Git git = Git.open(new File(pathToKotlinRepo));
		System.out.println("Branch: ");
		System.out.println(git.getRepository().getFullBranch());

		Collection<Ref> refs = git.branchList().setListMode(ListMode.ALL).call();

		for (Ref ref : refs) {

			if (ref.getName().equals(git.getRepository().getFullBranch())) {
				System.out.println("idem");
			} else
				System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
		}

		cm.createEngine(new String[] { "-location", locationKotinRepo.getAbsolutePath(),
				// Here our new analyzer
				"-input", MigaV2.class.getCanonicalName(),
				//
				"-mode", "nullmode",
				//
				"-parameters",
				"projectname:" + projectName + ":save_result_revision_analysis:true" + ":branch:" + branch
						+ ":outputunifieddiff:false:" + MigACore.COMMITS_TO_IGNORE
						+ ":088ddb17897a062e3ed6c3950385051f1f7a7228"

		});
		ComingProperties.setProperty("max_nb_commit_analyze", maxCommits);
		FinalResult finalResult = cm.start();

		MigACore core = (MigACore) cm.getExperiment();
		System.out.println("Commits analyzed: ");
		System.out.println(core.getCommitAnalyzed());
		System.out.println("---End----");
	}

	public MigAIntermediateResultStore run(String branch, String maxCommits) throws Exception {
		String pathToKotlinRepo = "/Users/matias/develop/code/testMigrationProjectKotlin";
		File locationKotinRepo = new File(pathToKotlinRepo);
		assertTrue(locationKotinRepo.exists());
		ComingMain cm = new ComingMain();
		System.out.println("Running with branch: " + branch);

		Git git = Git.open(new File(pathToKotlinRepo));
		System.out.println("Branch: ");
		System.out.println(git.getRepository().getFullBranch());

		Collection<Ref> refs = git.branchList().setListMode(ListMode.ALL).call();

		for (Ref ref : refs) {

			if (ref.getName().equals(git.getRepository().getFullBranch())) {
				System.out.println("idem");
			} else
				System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
		}

		cm.createEngine(new String[] { "-location", locationKotinRepo.getAbsolutePath(),
				// Here our new analyzer
				"-input", MigaV2.class.getCanonicalName(),
				//
				"-mode", "nullmode",
				//
				"-parameters",
				"projectname:" + "testLocal" + ":save_result_revision_analysis:true" + ":branch:" + branch
						+ ":outputunifieddiff:false:" + MigACore.COMMITS_TO_IGNORE
						+ ":088ddb17897a062e3ed6c3950385051f1f7a7228"

		});
		ComingProperties.setProperty("max_nb_commit_analyze", maxCommits);
		FinalResult finalResult = cm.start();

		MigaV2 core = (MigaV2) cm.getExperiment();

		return core.intermediateResultStore;

	}

	@Test
	public void testMiga_full_poet_assistant_500000() throws Exception {

		String projectName = "poet-assistant";// "NyaaPantsu-android-app";
		String branch = "master";

		run(projectName, branch, "50000");

	}

	@Test
	public void testLocal() throws Exception {

		String branch = "master";

		MigAIntermediateResultStore resultsMainBranch = run(branch, "50000");

		// moving method b39726ca3dba9f6d5c504184b697ded5847d55fd

		assertTrue(resultsMainBranch.armresults.get("b39726ca3dba9f6d5c504184b697ded5847d55fd").modifJava.size() > 0);
		assertTrue(resultsMainBranch.armresults.get("b39726ca3dba9f6d5c504184b697ded5847d55fd").modifKotlin.size() > 0);

		assertTrue(resultsMainBranch.commitsWithMigrationsAddMethodRemoveMethod
				.contains("b39726ca3dba9f6d5c504184b697ded5847d55fd"));

		// Migr of MyBean
		assertTrue(resultsMainBranch.commitsWithMigrationsRename.contains("09d7d506aa5e912ce1002850e48525ea81383ccb"));

		assertTrue(resultsMainBranch.commitMetadata.get("09d7d506aa5e912ce1002850e48525ea81383ccb").get("AUTHOR")
				.equals("martinezmatias"));

		assertTrue(resultsMainBranch.armresults.get("09d7d506aa5e912ce1002850e48525ea81383ccb").migrationJavaToKotlin
				.size() > 0);

		assertTrue(resultsMainBranch.armresults.get("09d7d506aa5e912ce1002850e48525ea81383ccb").migrationJavaToKotlin
				.contains("MyBean.kt"));
		assertFalse(resultsMainBranch.armresults.get("09d7d506aa5e912ce1002850e48525ea81383ccb").migrationJavaToKotlin
				.contains("Hello.kt"));

		/// Mig of Hello
		assertTrue(resultsMainBranch.commitsWithMigrationsRename.contains("a6ea2092050a96792faf5feb5ad02a3660653e6d"));

		assertTrue(resultsMainBranch.armresults.get("a6ea2092050a96792faf5feb5ad02a3660653e6d").migrationJavaToKotlin
				.size() > 0);
		assertTrue(resultsMainBranch.armresults.get("a6ea2092050a96792faf5feb5ad02a3660653e6d").migrationJavaToKotlin
				.contains("Hello.kt"));

		// Updates

		assertFalse(resultsMainBranch.commitsWithMigrationsRename.contains("5348c8410df6a2c41aa97b906edfd030322360f7"));

		assertTrue(resultsMainBranch.armresults.get("5348c8410df6a2c41aa97b906edfd030322360f7").migrationJavaToKotlin
				.isEmpty());
		assertTrue(resultsMainBranch.armresults.get("5348c8410df6a2c41aa97b906edfd030322360f7").modifJava.size() > 0);

		//
		assertFalse(resultsMainBranch.commitsWithMigrationsRename.contains("5ee3c7e8ea45b250a77c20e21df4de7709b7b2ea"));

		assertTrue(resultsMainBranch.armresults.get("5ee3c7e8ea45b250a77c20e21df4de7709b7b2ea").migrationJavaToKotlin
				.isEmpty());
		assertTrue(resultsMainBranch.armresults.get("5ee3c7e8ea45b250a77c20e21df4de7709b7b2ea").addedJava.size() > 0);

		//

		assertFalse(resultsMainBranch.commitsWithMigrationsRename.contains("3016bcfb98ca919a36eed33ed31cced1da0c2fbf"));

		// Let's check a branch

		MigAIntermediateResultStore resultsbmigBranch = run("bmig", "50000");
		// the new one
		assertTrue(resultsbmigBranch.commitsWithMigrationsRename.contains("3016bcfb98ca919a36eed33ed31cced1da0c2fbf"));

		assertTrue(resultsbmigBranch.armresults.get("3016bcfb98ca919a36eed33ed31cced1da0c2fbf").migrationJavaToKotlin
				.contains("Core.kt"));

		// Also the previous
		assertTrue(resultsbmigBranch.commitsWithMigrationsRename.contains("09d7d506aa5e912ce1002850e48525ea81383ccb"));

		// ----

	}

}
