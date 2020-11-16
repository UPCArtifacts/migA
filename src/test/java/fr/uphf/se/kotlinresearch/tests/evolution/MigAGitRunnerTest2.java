package fr.uphf.se.kotlinresearch.tests.evolution;

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

	public void run(String branch, String maxCommits) throws Exception {
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

//		MigACore core = (MigACore) cm.getExperiment();
//		System.out.println("Commits analyzed: ");
//		System.out.println(core.getCommitAnalyzed());
//		System.out.println("---End----");
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

		run(branch, "50000");

	}

}
