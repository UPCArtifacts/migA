package fr.uphf.se.kotlinresearch.tests.evolution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ResultParser {

	public void parse(String folderWithResults) throws IOException {

		int totalRenameMaster = 0;
		int totalRenameBranch = 0;

		int totalUpdateMaster = 0;
		int totalUpdateBranch = 0;

		int totalAddMaster = 0;
		int totalAddBranch = 0;

		File dir = new File(folderWithResults);

		Set<String> branchesRename = new HashSet<>();
		Set<String> masterRename = new HashSet<>();

		Set<String> emailsMasterRename = new HashSet<>();
		Set<String> emailsBranchesRename = new HashSet<>();

		Set<String> emailsAllUpdateAdd = new HashSet<>();

		Set<String> branchesAddKotlin = new HashSet<>();
		Set<String> masterAddKotkin = new HashSet<>();

		Set<String> branchesUpdate = new HashSet<>();
		Set<String> masterUpdate = new HashSet<>();

		Set<String> noTraditionalMigration = new HashSet<>();

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

			totalRenameMaster += nrMigMasterRename;
			totalRenameBranch += nrBranchesRename;

			int upsmaster = jobject.get("nr_master_migration_update_java_koltin").getAsInt();
			int upsbranches = jobject.get("nr_branches_migration_update_java_koltin").getAsInt();

			totalUpdateMaster += upsmaster;
			totalUpdateBranch += upsbranches;

			int addmaster = jobject.get("nr_master_migration_add_kotlin").getAsInt();

			int addbranches = jobject.get("nr_branches_migration_add_kotlin").getAsInt();

			totalAddMaster += addmaster;
			totalAddBranch += addbranches;

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

			//
			Set<String> emailsMaster = getAllEmails(jobject, "master_migration_rename");
			Set<String> emailBranches = getAllEmails(jobject, "branches_migration_rename");

			emailsMasterRename.addAll(emailsMaster);
			emailsBranchesRename.addAll(emailBranches);

			Set<String> emailsUpMaster = getAllEmails(jobject, "master_migration_update_java_koltin");
			Set<String> emailUpBranches = getAllEmails(jobject, "branches_migration_update_java_koltin");

			emailsAllUpdateAdd.addAll(emailsUpMaster);
			emailsAllUpdateAdd.addAll(emailUpBranches);

			Set<String> emailsAddMaster = getAllEmails(jobject, "master_migration_add_kotlin");
			Set<String> emailUpAddBranches = getAllEmails(jobject, "branches_migration_add_kotlin");

			emailsAllUpdateAdd.addAll(emailsAddMaster);
			emailsAllUpdateAdd.addAll(emailUpAddBranches);

		}
		//
		System.out.println("---");
		System.out.println("Projects with mig in master " + masterRename.size() + " " + masterRename);
		System.out.println("Projects with mig in branches " + branchesRename.size() + " " + branchesRename);
		System.out.println("_");
		System.out.println("Total mig in master " + totalRenameMaster);
		System.out.println("Total mig in branches " + totalRenameBranch);
		System.out.println("--> emails master:  " + emailsMasterRename.size() + " " + emailsMasterRename);
		System.out.println("--> emails branches:  " + emailsBranchesRename.size() + " " + emailsBranchesRename);

		Set<String> emailsExclusiveBranches = new HashSet<>(emailsBranchesRename);
		emailsExclusiveBranches.removeAll(emailsMasterRename);
		System.out.println(
				"--> emails exclusive branches:  " + emailsExclusiveBranches.size() + " " + emailsExclusiveBranches);

		// Retrieve mails from last experiment
		Set<String> mailsSent = retrieveSentMails(new File(
				"/Users/matias/develop/kotlinresearch/kotlinmigrationdiff-research/migAResults/emails/emails-migration-all_first_submissions.csv"));
		//
		Set<String> emailsAllRename = new HashSet<>();
		emailsAllRename.addAll(emailsMasterRename);
		emailsAllRename.addAll(emailsBranchesRename);
		Set<String> emailsNew = new HashSet<>();
		for (String aData : emailsAllRename) {

			String emailData = aData.split(",")[0];
			if (!mailsSent.contains(emailData)) {
				emailsNew.add(aData);
			}

		}
		System.out.println("All emails: " + emailsAllRename.size());
		System.out.println("New emails: " + emailsNew.size());

		if (false) {
			Set<String> emailsExclusiveOtherMig = new HashSet<>(emailsAllUpdateAdd);
			emailsExclusiveBranches.removeAll(emailsMasterRename);
			emailsExclusiveBranches.removeAll(emailsBranchesRename);

			// System.out.println("--> emails exclusive other Migrations: " +
			// emailsExclusiveOtherMig.size() + " "
			// + emailsExclusiveOtherMig);

			System.out.println("---");
			System.out.println("Projects with ups in master " + masterUpdate.size() + " " + masterUpdate);
			System.out.println("Projects with ups in branches " + branchesUpdate.size() + " " + branchesUpdate);
			System.out.println("_");
			System.out.println("Total ups in master " + totalUpdateMaster);
			System.out.println("Total ups in branches " + totalUpdateBranch);

			System.out.println("---");
			System.out.println("Projects with add in master " + masterAddKotkin.size() + " " + masterAddKotkin);
			System.out.println("Projects with add in branches " + branchesAddKotlin.size() + " " + branchesAddKotlin);
			System.out.println("_");
			System.out.println("Total add in master " + totalAddMaster);
			System.out.println("Total add in branches " + totalAddBranch);

			System.out.println("---");
			System.out.println(
					"project with not traditional " + noTraditionalMigration.size() + " " + noTraditionalMigration);
		}

	}

	private Set<String> getAllEmails(JsonObject jobject, String key) {
		Set<String> commitsMaster = retrieveCommits(jobject, key);
		Set<String> emailsMaster = filterEmails(jobject, commitsMaster);
		return emailsMaster;
	}

	public Set<String> retrieveCommits(JsonObject jobject, String key) {
		Set<String> commits = new HashSet<>();
		JsonArray commitsArray = jobject.get(key).getAsJsonArray();
		for (JsonElement jsonElement : commitsArray) {
			commits.add(jsonElement.getAsString());
		}
		return commits;
	}

	public Set<String> filterEmails(JsonObject rootjson, Set<String> commits) {
		Set<String> emails = new HashSet<>();

		String projectName = rootjson.get("project").getAsString();
		for (JsonElement jsonbranch : rootjson.get("data").getAsJsonArray()) {
			JsonArray commitsArray = jsonbranch.getAsJsonObject().get("commits").getAsJsonArray();

			for (JsonElement commitJSon : commitsArray) {
				String commit = commitJSon.getAsJsonObject().get("commit").getAsString();
				if (commits.contains(commit)) {
					String anEmail = commitJSon.getAsJsonObject().get("email").getAsString();
					String author = commitJSon.getAsJsonObject().get("email").getAsString();

					String row = anEmail.trim() + ",0," + author + "," + projectName;
					emails.add(row);
				}
			}

		}

		return emails;

	}

	public Set<String> retrieveSentMails(File mailsFile) throws FileNotFoundException {

		BufferedReader reader;
		Set<String> lines = new HashSet<>();

		try {
			reader = new BufferedReader(new FileReader(mailsFile));
			String line = reader.readLine();
			while (line != null) {
				line = reader.readLine();
				lines.add(line.split(",")[0]);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}

	public static void main(String[] arg) throws IOException {

		ResultParser parser = new ResultParser();
		String location = "/Users/matias/develop/kotlinresearch/kotlinmigrationdiff-research/migAResults/migration25112020/";
		parser.parse(location);
	}

}
