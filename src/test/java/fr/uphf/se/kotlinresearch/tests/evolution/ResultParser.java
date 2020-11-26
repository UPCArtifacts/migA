package fr.uphf.se.kotlinresearch.tests.evolution;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ResultParser {

	public void parse(String folderWithResults) throws IOException {
		File dir = new File(folderWithResults);

		List<String> branchesRename = new ArrayList<>();
		List<String> masterRename = new ArrayList<>();

		List<String> branchesAddKotlin = new ArrayList<>();
		List<String> masterAddKotkin = new ArrayList<>();

		List<String> branchesUpdate = new ArrayList<>();
		List<String> masterUpdate = new ArrayList<>();

		List<String> noTraditionalMigration = new ArrayList<>();

		for (File aProjectFile : dir.listFiles()) {

			if (!aProjectFile.getName().endsWith(".json")) {
				continue;
			}

			// create a reader
			Reader reader = Files.newBufferedReader(Paths.get(aProjectFile.toPath().toUri()));

			JsonElement jelement = new JsonParser().parse(reader);
			JsonObject jobject = jelement.getAsJsonObject();

			int nrMigMasterRename = jobject.get("nr_master_migration_rename").getAsInt();

			int nrBranchesRename = jobject.get("nr_branches_migration_rename").getAsInt();

			int upsmaster = jobject.get("nr_master_migration_update_java_koltin").getAsInt();
			int upsbranches = jobject.get("nr_branches_migration_update_java_koltin").getAsInt();

			int addmaster = jobject.get("nr_master_migration_add_kotlin").getAsInt();

			int addbranches = jobject.get("nr_branches_migration_add_kotlin").getAsInt();

			System.out.println("-->" + aProjectFile.getName() + " nr mig in master " + nrMigMasterRename
					+ " nr mig in branches " + nrBranchesRename + " up master " + upsmaster + " ups branches"
					+ upsbranches + " add master " + addmaster + " add branch " + addbranches);

			if (nrBranchesRename > 0) {
				branchesRename.add(aProjectFile.getName());
			}

			if (nrMigMasterRename > 0) {
				masterRename.add(aProjectFile.getName());
			}

			if (upsmaster > 0) {
				masterUpdate.add(aProjectFile.getName());
			}

			if (upsbranches > 0) {
				branchesUpdate.add(aProjectFile.getName());
			}

			if (addmaster > 0) {
				masterAddKotkin.add(aProjectFile.getName());
			}

			if (addbranches > 0) {
				branchesAddKotlin.add(aProjectFile.getName());
			}

			if (nrBranchesRename == 0 && nrMigMasterRename == 0
					&& (upsmaster + upsbranches + addmaster + addbranches) > 0) {
				noTraditionalMigration.add(aProjectFile.getName());
			}

		}
		//
		System.out.println("---");
		System.out.println("Projects with mig in master " + masterRename.size() + " " + masterRename);
		System.out.println("Projects with mig in branches " + branchesRename.size() + " " + branchesRename);
		System.out.println("---");
		System.out.println("Projects with ups in master " + masterUpdate.size() + " " + masterUpdate);
		System.out.println("Projects with ups in branches " + branchesUpdate.size() + " " + branchesUpdate);
		System.out.println("---");
		System.out.println("Projects with add in master " + masterAddKotkin.size() + " " + masterAddKotkin);
		System.out.println("Projects with add in branches " + branchesAddKotlin.size() + " " + branchesAddKotlin);

		System.out.println("---");
		System.out.println(
				"project with not traditional " + noTraditionalMigration.size() + " " + noTraditionalMigration);
	}

	public static void main(String[] arg) throws IOException {

		ResultParser parser = new ResultParser();
		String location = "/Users/matias/develop/kotlinresearch/kotlinmigrationdiff-research/migAResults/migration25112020/";
		parser.parse(location);
	}

}
