package fr.uphf.se.kotlinresearch.tests.evolution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.lib.Ref;
import org.junit.Test;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.main.ComingMain;
import fr.inria.coming.main.ComingProperties;
import fr.uphf.se.kotlinresearch.arm.analyzers.AddRemoveResult;
import fr.uphf.se.kotlinresearch.core.MigACore;
import fr.uphf.se.kotlinresearch.core.MigaMain;
import fr.uphf.se.kotlinresearch.core.results.MigAIntermediateResultStore;
import fr.uphf.se.kotlinresearch.diff.analyzers.JavaDiffAnalyzer;

/**
 * 
 * @author Matias Martinez
 *
 */
public class MigAGitRunnerLocalRepositoryTest {

	final String pathToKotlinRepo = "/Users/matias/develop/code/testMigrationProjectKotlin";

	Logger log = Logger.getLogger(JavaDiffAnalyzer.class.getName());
	final int idem = 0;

	public List<String> retrieveBranchInfo() throws Exception {
		Git git = Git.open(new File(pathToKotlinRepo));
		System.out.println("Branch: ");
		System.out.println(git.getRepository().getFullBranch());
		List<String> branches = new ArrayList<>();
		Collection<Ref> refs = git.branchList().setListMode(ListMode.ALL).call();

		for (Ref ref : refs) {
			branches.add(ref.getName());
			if (ref.getName().equals(git.getRepository().getFullBranch())) {
				System.out.println("idem");
			} else
				System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
		}
		return branches;
	}

	public void runBranches(String projectName, String maxCommits) throws Exception {
		File locationKotinRepo = new File(pathToKotlinRepo);
		assertTrue(locationKotinRepo.exists());

		Git git = Git.open(new File(pathToKotlinRepo));
		System.out.println("Branch: ");
		String fullBranch = git.getRepository().getFullBranch();
		System.out.println(fullBranch);

		ComingMain cm = runCommand(projectName, fullBranch, maxCommits, locationKotinRepo, "toIgnore");

		MigACore core = (MigACore) cm.getExperiment();
		System.out.println("Commits analyzed: ");
		System.out.println(core.getCommitAnalyzed());

		String toAvoid = core.getCommitAnalyzed().stream().collect(Collectors.joining("_"));

		//
		Collection<Ref> refs = git.branchList().setListMode(ListMode.ALL).call();

		for (Ref ref : refs) {

			if (ref.getName().equals(fullBranch)) {
				System.out.println("idem");
			} else
				System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());

			ComingMain cb = runCommand(projectName, ref.getName(), maxCommits, locationKotinRepo, "toIgnore");
			MigACore core1 = (MigACore) cm.getExperiment();
			System.out.println("Commits analyzed: ");
			System.out.println(core1.getCommitAnalyzed());

		}

		System.out.println("---End----");
	}

	public ComingMain runCommand(String projectName, String branch, String maxCommits, File locationKotinRepo,
			String toIgnore) {
		ComingMain cm = new ComingMain();

		cm.createEngine(new String[] { "-location", locationKotinRepo.getAbsolutePath(),
				// Here our new analyzer
				"-input", MigACore.class.getCanonicalName(),
				//
				"-mode", "nullmode",
				//
				"-parameters", "projectname:" + projectName + ":save_result_revision_analysis:true" + ":branch:"
						+ branch + ":outputunifieddiff:false:" + MigACore.COMMITS_TO_IGNORE + ":" + toIgnore

		});
		ComingProperties.setProperty("max_nb_commit_analyze", maxCommits);
		FinalResult finalResult = cm.start();
		return cm;
	}

	@Test
	public void testLocal() throws Exception {
		File out = new File("./coming_results/");
		String branch = "master";
		MigaMain main = new MigaMain();

		List<String> branches = retrieveBranchInfo();
		assertEquals(3, branches.size());
		System.out.println("Branches: " + branches);

		MigAIntermediateResultStore resultsMainBranch = main.runAnalysis(out, new File(pathToKotlinRepo), branch);

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

		String movingMethodToNewFile = "b5e9408216796e9eedb1efc77fa1405166c1e3da";

		assertTrue(resultsMainBranch.commitsWithMigrationsRemoveMethodMethodAddFile.contains(movingMethodToNewFile));

		// Let's check a branch

		MigAIntermediateResultStore resultNotMergedsbmigBranch = main.runAnalysis(out, new File(this.pathToKotlinRepo),
				"bmig");
		// the new one
		assertTrue(resultNotMergedsbmigBranch.commitsWithMigrationsRename
				.contains("3016bcfb98ca919a36eed33ed31cced1da0c2fbf"));

		assertTrue(resultNotMergedsbmigBranch.armresults
				.get("3016bcfb98ca919a36eed33ed31cced1da0c2fbf").migrationJavaToKotlin.contains("Core.kt"));

		// branch add file to be migrated
		assertTrue(resultNotMergedsbmigBranch.armresults.get("b2ae3f0c149c09560e74336aa89116e390a3d930").addedJava
				.contains("Core.java"));

		assertTrue(resultNotMergedsbmigBranch.orderCommits.contains("b2ae3f0c149c09560e74336aa89116e390a3d930"));
		assertTrue(resultNotMergedsbmigBranch.orderCommits.contains("3016bcfb98ca919a36eed33ed31cced1da0c2fbf"));
		assertTrue(resultNotMergedsbmigBranch.orderCommits.contains("09d7d506aa5e912ce1002850e48525ea81383ccb"));
		// Also the previous
		assertTrue(resultNotMergedsbmigBranch.commitsWithMigrationsRename
				.contains("09d7d506aa5e912ce1002850e48525ea81383ccb"));
		// check results from branch are not in the main
		assertFalse(resultsMainBranch.orderCommits.contains("b2ae3f0c149c09560e74336aa89116e390a3d930"));
		assertFalse(resultsMainBranch.orderCommits.contains("3016bcfb98ca919a36eed33ed31cced1da0c2fbf"));
		assertTrue(resultNotMergedsbmigBranch.orderCommits.contains("09d7d506aa5e912ce1002850e48525ea81383ccb"));

		// ----

		// Merged branch

		MigAIntermediateResultStore resultsMergedBranch = main.runAnalysis(new File(this.pathToKotlinRepo),
				"bmignewfeature");

		String shaMigratedAndMerged = "1a6611e29102af3ec4f0a1df6078a5e0dfde3424";
		assertTrue(resultsMergedBranch.commitsWithMigrationsRename.contains(shaMigratedAndMerged));

		assertTrue(resultsMainBranch.commitsWithMigrationsRename.contains(shaMigratedAndMerged));

		assertFalse(resultNotMergedsbmigBranch.commitsWithMigrationsRename.contains(shaMigratedAndMerged));

		assertTrue(resultsMergedBranch.orderCommits.contains(shaMigratedAndMerged));

		AddRemoveResult addRemoveResultMigratedIntegrated = resultsMergedBranch.armresults.get(shaMigratedAndMerged);
		assertTrue(addRemoveResultMigratedIntegrated.migrationJavaToKotlin.contains("NFeat.kt"));

		// Now in main
		assertTrue(resultsMainBranch.armresults.get(shaMigratedAndMerged).migrationJavaToKotlin.contains("NFeat.kt"));

		// now in not merged, the commit does not exit
		assertNull(resultNotMergedsbmigBranch.armresults.get(shaMigratedAndMerged));

		// update in merged
		assertFalse(resultsMergedBranch.orderCommits.contains("416890c7f771ba551ca6e9cfe7c8f7a62993cb41"));
		assertTrue(resultsMainBranch.orderCommits.contains("416890c7f771ba551ca6e9cfe7c8f7a62993cb41"));
		//

	}

	@Test
	public void testLocalBranches() throws Exception {

		List<String> branches = retrieveBranchInfo();
		assertEquals(3, branches.size());
		File out = new File("./coming_results/");
		MigaMain main = new MigaMain();

		for (String iBranch : branches) {
			MigAIntermediateResultStore resultsBranch = main.runAnalysis(out, new File(this.pathToKotlinRepo), iBranch);
			assertNotNull(resultsBranch);
			assertTrue(resultsBranch.orderCommits.size() > 0);
		}

	}

	@Test
	public void testLocalBranchesIgnore() throws Exception {

		MigaMain main = new MigaMain();

		File out = new File("./coming_results/");

		String commitToIgnore = "b39726ca3dba9f6d5c504184b697ded5847d55fd";
		MigAIntermediateResultStore resultsBranch = main.runAnalysis(out, new File(this.pathToKotlinRepo), "master",
				commitToIgnore);
		assertNotNull(resultsBranch);
		assertTrue(resultsBranch.orderCommits.size() > 0);

		assertNull(resultsBranch.armresults.get(commitToIgnore));

		assertFalse(resultsBranch.orderCommits.contains(commitToIgnore));

		assertFalse(resultsBranch.orderCommits.contains(commitToIgnore));
		// Others commits
		assertTrue(resultsBranch.orderCommits.contains("1a6611e29102af3ec4f0a1df6078a5e0dfde3424"));
		assertNotNull(resultsBranch.armresults.get("1a6611e29102af3ec4f0a1df6078a5e0dfde3424"));
		assertTrue(resultsBranch.orderCommits.contains("fd7299d1ca4c656b43fc9a5e630d458606ec4429"));
		assertNotNull(resultsBranch.armresults.get("fd7299d1ca4c656b43fc9a5e630d458606ec4429"));

		// Now let's check that this commit is not discarded if the property is not null

		resultsBranch = main.runAnalysis(out, new File(this.pathToKotlinRepo), "master", "");
		assertNotNull(resultsBranch);
		assertTrue(resultsBranch.orderCommits.size() > 0);

		assertNotNull(resultsBranch.armresults.get(commitToIgnore));

		assertTrue(resultsBranch.orderCommits.contains(commitToIgnore));

		assertTrue(resultsBranch.orderCommits.contains(commitToIgnore));

	}

	@Test
	public void testAllCommitsAllBranches() throws Exception {

		Git git = Git.open(new File(pathToKotlinRepo));
		System.out.println("Branch: ");
		String mainBranch = git.getRepository().getFullBranch();
		System.out.println(mainBranch);
		Collection<Ref> refs = git.branchList().setListMode(ListMode.ALL).call();

		//
		List<String> branches = new ArrayList<>();
		// the master the first one
		branches.add(mainBranch);

		for (Ref ref : refs) {
			if (!ref.getName().equals(mainBranch)) {
				branches.add(ref.getName());
			}
		}

		assertEquals(3, branches.size());

	}

	@Test
	public void testAllCommitsAllBranchesRef() throws Exception {

		MigaMain main = new MigaMain();

		Map<String, List> commitsByBranch = main.runExperiment(new File(this.pathToKotlinRepo));

		System.out.println(commitsByBranch);

		for (String key : commitsByBranch.keySet()) {
			System.out.println(key + " " + commitsByBranch.get(key));

		}

		assertTrue(commitsByBranch.get("refs/heads/master").size() > 3);
		// All commits were merged into master
		assertTrue(commitsByBranch.get("refs/heads/bmignewfeature").isEmpty());
		// Two commits in branched not merged
		assertTrue(commitsByBranch.get("refs/heads/bmig").size() == 2);
	}

}
