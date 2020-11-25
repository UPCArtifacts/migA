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
		List<String> branches = new ArrayList<>();
		for (File aProjectFile : dir.listFiles()) {

			if (!aProjectFile.getName().endsWith(".json")) {
				continue;
			}

			// create a reader
			Reader reader = Files.newBufferedReader(Paths.get(aProjectFile.toPath().toUri()));

			JsonElement jelement = new JsonParser().parse(reader);
			JsonObject jobject = jelement.getAsJsonObject();

			int nrMigMaster = jobject.get("nr_master_migration_rename").getAsInt();

			int nrBranches = jobject.get("nr_branches_migration_rename").getAsInt();

			System.out.println("-->" + aProjectFile.getName() + " nr mig in master " + nrMigMaster
					+ " nr mig in branches " + nrBranches);

			if (nrBranches > 0) {
				branches.add(aProjectFile.getName());
			}

		}
		System.out.println("Projects with mig in branches " + branches.size() + " " + branches);

	}

	public static void main(String[] arg) throws IOException {

		ResultParser parser = new ResultParser();
		parser.parse("/Users/matias/develop/kotlinresearch/kotlinmigrationdiff-research/migAResults/"
				// + "migrations22112020/"
				+ "migrations23112020/");
	}

}
