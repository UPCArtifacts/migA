package fr.uphf.se.kotlinresearch.core;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.main.ComingMain;
import fr.uphf.se.kotlinresearch.core.results.MigAIntermediateResultStore;
import fr.uphf.se.kotlinresearch.output.MigAJSONSerializer;

/**
 * 
 * @author Matias Martinez
 *
 */
public class MigaMain {

	public MigAIntermediateResultStore runAnalysis(File repopath, String branch) throws Exception {
		return runAnalysis(repopath, branch, null);

	}

	public MigAIntermediateResultStore runAnalysis(File repopath, String branch, String toIgnore) throws Exception {

		ComingMain cm = new ComingMain();
		System.out.println("Running with branch: " + branch);

		cm.createEngine(new String[] { "-location", repopath.getAbsolutePath(),
				// Here our new analyzer
				"-input", MigaV2.class.getCanonicalName(),
				//
				"-mode", "nullmode",
				//
				"-parameters",
				"projectname:" + repopath.getName() + ":save_result_revision_analysis:true" + ":branch:" + branch
						+ ":outputunifieddiff:false:"
						+ ((toIgnore != null && !toIgnore.isEmpty()) ? (MigACore.COMMITS_TO_IGNORE + ":" + toIgnore)
								: "")

		});
		FinalResult finalResult = cm.start();

		MigaV2 core = (MigaV2) cm.getExperiment();

		return core.intermediateResultStore;

	}

	public Map<String, List> runExperiment(File pathToKotlinRepo) throws IOException, GitAPIException, Exception {
		return runExperiment(new File("./coming_results/"), pathToKotlinRepo);
	}

	public Map<String, List> runExperiment(File out, File pathToKotlinRepo)
			throws IOException, GitAPIException, Exception {
		Git git = Git.open(pathToKotlinRepo);
		System.out.println("Branch: ");
		String mainBranch = git.getRepository().getFullBranch();
		System.out.println(mainBranch);
		Collection<Ref> refs = git.branchList().setListMode(ListMode.ALL).call();

		List<String> branches = new ArrayList<>();
		// the master the first one
		branches.add(mainBranch);

		for (Ref ref : refs) {
			if (!ref.getName().equals(mainBranch)) {
				branches.add(ref.getName());
			}
		}

		List<String> allCommitsAnalyzed = new ArrayList();
		Map<String, List> commitsByBranch = new HashMap<>();

		Map<String, MigAIntermediateResultStore> resultsByBranch = new HashMap<>();

		JsonObject jsonRoot = new JsonObject();

		JsonArray jsonbranches = new JsonArray();
		jsonRoot.add("data", jsonbranches);

		List<String> summaryCommitsMigrationMaster = new ArrayList<String>();
		List<String> summaryCommitsMigrationUpFileMaster = new ArrayList<String>();
		List<String> summaryCommitsMigrationADDFileMaster = new ArrayList<String>();
		List<String> summaryCommitsMigrationBrach = new ArrayList<String>();
		List<String> summaryCommitsMigrationUpFileBranch = new ArrayList<String>();
		List<String> summaryCommitsMigrationADDFileBranch = new ArrayList<String>();

		MigAJSONSerializer serializer = new MigAJSONSerializer();

		for (String iBranch : branches) {

			System.out.println("--> " + iBranch);
			String alreadyAnalyzed = allCommitsAnalyzed.stream().collect(Collectors.joining(MigaV2.CHAR_JOINT_IGNORE));

			MigAIntermediateResultStore resultsBranch = runAnalysis(pathToKotlinRepo, iBranch, alreadyAnalyzed);
			assertNotNull(resultsBranch);

			resultsByBranch.put(iBranch, resultsBranch);
			// assertTrue(resultsBranch.orderCommits.size() > 0);

			allCommitsAnalyzed.addAll(resultsBranch.orderCommits);
			commitsByBranch.put(iBranch, resultsBranch.orderCommits);

			JsonObject jsonBranch = serializer.extractJSon(pathToKotlinRepo.getName(), 0, resultsBranch);
			jsonBranch.addProperty("branch", iBranch);

			jsonbranches.add(jsonBranch);

			if (mainBranch.equals(iBranch)) {
				summaryCommitsMigrationMaster.addAll(resultsBranch.commitsWithMigrationsRename);
				summaryCommitsMigrationUpFileMaster.addAll(resultsBranch.commitsWithMigrationsAddMethodRemoveMethod);
				summaryCommitsMigrationADDFileMaster
						.addAll(resultsBranch.commitsWithMigrationsRemoveMethodMethodAddFile);

			} else {

				summaryCommitsMigrationBrach.addAll(resultsBranch.commitsWithMigrationsRename);
				summaryCommitsMigrationUpFileBranch.addAll(resultsBranch.commitsWithMigrationsAddMethodRemoveMethod);
				summaryCommitsMigrationADDFileBranch
						.addAll(resultsBranch.commitsWithMigrationsRemoveMethodMethodAddFile);

			}

		}

		jsonRoot.add("master_migration_rename", transformName(summaryCommitsMigrationMaster));

		jsonRoot.add("branches_migration_rename", transformName(summaryCommitsMigrationBrach));

		jsonRoot.add("master_migration_update_java_koltin", transformName(summaryCommitsMigrationUpFileMaster));

		jsonRoot.add("branches_migration_update_java_koltin", transformName(summaryCommitsMigrationUpFileBranch));

		jsonRoot.add("master_migration_add_kotlin", transformName(summaryCommitsMigrationADDFileMaster));

		jsonRoot.add("branches_migration_add_kotlin", transformName(summaryCommitsMigrationADDFileBranch));

		jsonRoot.addProperty("nr_master_migration_rename", (summaryCommitsMigrationMaster.size()));

		jsonRoot.addProperty("nr_branches_migration_rename", (summaryCommitsMigrationBrach.size()));

		jsonRoot.addProperty("nr_master_migration_update_java_koltin", (summaryCommitsMigrationUpFileMaster.size()));

		jsonRoot.addProperty("nr_branches_migration_update_java_koltin", (summaryCommitsMigrationUpFileBranch.size()));

		jsonRoot.addProperty("nr_master_migration_add_kotlin", (summaryCommitsMigrationADDFileMaster.size()));

		jsonRoot.addProperty("nr_branches_migration_add_kotlin", (summaryCommitsMigrationADDFileBranch.size()));
		jsonRoot.addProperty("project", pathToKotlinRepo.getName());
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		serializer.storeJSon(out, getFileNameOfOutput(pathToKotlinRepo), jsonRoot);

		return commitsByBranch;
	}

	public String getFileNameOfOutput(File pathToKotlinRepo) {
		return "alldata_" + pathToKotlinRepo.getName();
	}

	private JsonElement transformName(List<String> summaryCommitsMigrationMaster) {
		JsonArray arr = new JsonArray();
		for (String string : summaryCommitsMigrationMaster) {
			arr.add(string);
		}

		return arr;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
