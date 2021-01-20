package fr.uphf.se.kotlinresearch.core;

import java.io.File;
import java.util.List;
import java.util.Map;

public class MigaMainSeveralRepos {

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage <path to results> <path to folder that contains repos>");

		} else {
			File fout = new File(args[0]);
			File frepo = new File(args[1]);

			if (!fout.exists()) {
				fout.mkdirs();
			}

			MigaMain main = new MigaMain();

			for (File project : frepo.listFiles()) {
				try {
					if (project.getName().equals("Osmand")) {
						System.out.println("Skip: " + project.getName());
						continue;
					}

					String outpath = fout + File.separator + main.getFileNameOfOutput(project) + ".json";
					File outFileProject = new File(outpath);
					if (project.isDirectory()) {

						if (outFileProject.exists()) {
							System.out.println(project + " already analyzed");
							continue;
						} else {
							System.out.println(project + " to be analyzed");

						}

						System.out.println("\n*****Analyzing " + project);
						Map<String, List> commitsByBranch = main.runExperiment(fout, project);

						System.out.println("End analysis of: " + project);
						// System.out.println(commitsByBranch);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
