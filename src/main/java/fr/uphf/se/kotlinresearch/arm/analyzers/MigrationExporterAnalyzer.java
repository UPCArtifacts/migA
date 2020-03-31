package fr.uphf.se.kotlinresearch.arm.analyzers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.IRevisionPair;
import fr.inria.coming.main.ComingProperties;

public class MigrationExporterAnalyzer implements Analyzer<IRevision> {

	File outDir = null;

	public MigrationExporterAnalyzer() {
		String projectName = ComingProperties.getProperty("projectname");
		outDir = new File(ComingProperties.getProperty("output") + File.separator + projectName);
		if (!outDir.exists()) {
			outDir.mkdirs();
		}
	}

	@Override
	public AnalysisResult analyze(IRevision iRevision, RevisionResult previousResults) {

		AddRemoveResult armresult = (AddRemoveResult) previousResults.getResultFromClass(AddedRemovedAnalyzer.class);

		RenameAnalyzerResult renameresult = (RenameAnalyzerResult) previousResults
				.getResultFromClass(FileCommitNameAnalyzer.class);

		List<IRevisionPair<String>> childerPairs = renameresult.getAllFileCommits();

		// For each file inside the revision
		for (IRevisionPair iRevisionPair : childerPairs) {
			try {
				// If koltin
				String currentFilename = iRevisionPair.getName();
				String previousFileName = iRevisionPair.getPreviousName();

				if (armresult.migrationJavaToKotlin.contains(currentFilename)) {
					save(iRevision, iRevisionPair, "migrationJavaToKotlin");
				}

				if (armresult.migrationKotlinToJava.contains(currentFilename)) {
					save(iRevision, iRevisionPair, "migrationKotlinToJava");
				}

				// commitjson.add("migr_kotlin_java", getAll(
				// arm.migrationKotlinToJava;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public void save(IRevision iRevision, IRevisionPair iRevisionPair, String type) throws IOException {

		String nameCommit = iRevision.getName();
		String context = iRevisionPair.getNextVersion().toString();
		String nameFile = iRevisionPair.getName();

		saveFile(type, nameCommit, context, nameFile);

		context = iRevisionPair.getPreviousVersion().toString();
		nameFile = iRevisionPair.getPreviousName();

		saveFile(type, nameCommit, context, nameFile);
	}

	public void saveFile(String type, String nameCommit, String context, String nameFile) throws IOException {
		File dir = new File(outDir + File.separator + type + File.separator + nameCommit);
		dir.mkdirs();

		File file = new File(dir + File.separator + nameFile);

		FileWriter fw = new FileWriter(file);

		fw.append(context);

		fw.close();
	}

}
