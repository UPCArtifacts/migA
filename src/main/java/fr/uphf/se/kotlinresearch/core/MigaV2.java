package fr.uphf.se.kotlinresearch.core;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.core.engine.git.GITRepositoryInspector;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.RevisionDataset;
import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.main.ComingProperties;
import fr.uphf.se.kotlinresearch.arm.analyzers.AddRemoveResult;
import fr.uphf.se.kotlinresearch.arm.analyzers.AddedRemovedAnalyzer;
import fr.uphf.se.kotlinresearch.arm.analyzers.FileCommitNameAnalyzer;
import fr.uphf.se.kotlinresearch.arm.analyzers.RenameAnalyzerResult;
import fr.uphf.se.kotlinresearch.core.results.MigAIntermediateResultStore;
import fr.uphf.se.kotlinresearch.diff.analyzers.JavaDiffAnalyzer;
import fr.uphf.se.kotlinresearch.diff.analyzers.KotlinDiffAnalyzer;
import fr.uphf.se.kotlinresearch.output.MigAJSONSerializer;
import fr.uphf.se.kotlinresearch.tree.analyzers.JavaTreeAnalyzer;
import fr.uphf.se.kotlinresearch.tree.analyzers.KastreeTreeAnalyzer;
import fr.uphf.se.kotlinresearch.tree.analyzers.TreeResult;

/**
 * 
 * @author Matias Martinez
 *
 */
public class MigaV2 extends GITRepositoryInspector {

	@SuppressWarnings({ "rawtypes", "unchecked" })

	public MigAIntermediateResultStore intermediateResultStore = new MigAIntermediateResultStore();
	protected MigAJSONSerializer serializer = new MigAJSONSerializer();

	public FinalResult analyze() {

		RevisionDataset data = loadDataset();
		Iterator it = this.getNavigationStrategy().orderOfNavigation(data);

		int i = 1;

		int size = data.size();

		for (Iterator<Commit> iterator = it; iterator.hasNext();) {

			Commit oneRevision = iterator.next();

			if (null == oneRevision) {
				return processEnd();
			}

			log.info("\n***********\nAnalyzing " + i + "/" + size + " " + oneRevision.getName());

			if (!(accept(oneRevision))) {
				continue;
			}

			// Get the information of the commit
			AnalysisResult<Map<String, Object>> resultDataCommit = (new CommitDataAnalyzer()).analyze(oneRevision,
					null);

			// Analyze the renames
			RenameAnalyzerResult resultsFileRenameAnalyzed = (RenameAnalyzerResult) (new FileCommitNameAnalyzer())
					.analyze(oneRevision, null);

			// Analyze the Add remove files
			AddRemoveResult resultsAddRemove = (new AddedRemovedAnalyzer()).analyze(oneRevision,
					resultsFileRenameAnalyzed);

			// Update both

			boolean computeDiffOfJava = false;
			boolean computeDiffKotlin = false;

			if (resultsAddRemove.modifJava.size() > 0 && (resultsAddRemove.modifKotlin.size() > 0)) {
				computeDiffOfJava = true;
				computeDiffKotlin = true;
			} else

			// Add Kotlin and update Java by removing code

			if (resultsAddRemove.modifJava.size() > 0 && (resultsAddRemove.addedKotlin.size() > 0)) {
				computeDiffOfJava = true;
				// The file was added
				computeDiffKotlin = false;
			}

			///
			TreeResult resultsJavaTrees = null;
			DiffResult resultJavaDiffs = null;
			if (computeDiffOfJava) {

				resultsJavaTrees = new JavaTreeAnalyzer().analyze(oneRevision, resultsFileRenameAnalyzed,
						resultsAddRemove);

				resultJavaDiffs = new JavaDiffAnalyzer().analyze(oneRevision, resultsFileRenameAnalyzed,
						resultsJavaTrees);

			}
			TreeResult resultsKotlinTrees = null;
			DiffResult resultKotlinDiffs = null;
			if (computeDiffKotlin) {

				resultsKotlinTrees = new KastreeTreeAnalyzer().analyze(oneRevision, resultsFileRenameAnalyzed,
						resultsAddRemove);

				resultKotlinDiffs = new KotlinDiffAnalyzer().analyze(oneRevision, resultsFileRenameAnalyzed,
						resultsKotlinTrees);

			}

			///

			/// -----
			// if Kotlin and Java
			/// Compute changes
			/// detect types of migration
			/// save migrations file

			//

			///

			// Save results:
			intermediateResultStore.commitMetadata.put(oneRevision.getName(), resultDataCommit.getAnalyzed());
			intermediateResultStore.orderCommits.add(oneRevision.getName());

			intermediateResultStore.armresults.put(oneRevision.getName(), resultsAddRemove);

			if (resultsAddRemove.migrationJavaToKotlin.size() > 0)
				intermediateResultStore.commitsWithMigrationsRename.add(oneRevision.getName());

			i++;
		}

		return processEnd();
	}

	@Override
	public FinalResult processEnd() {
		FinalResult finalResult = super.processEnd();

		String projectName = ComingProperties.getProperty("projectname");
		String branchName = ComingProperties.getProperty("branch");
		File outDir = new File(ComingProperties.getProperty("output") + File.separator + projectName + File.separator
				+ branchName.replace("/", "-"));
		if (!outDir.exists()) {
			outDir.mkdirs();
		}

		serializer.saveAll(projectName, outDir, 0, this.intermediateResultStore);

		System.out.println("MigaV2: END-Finish running comming");

		return finalResult;

	}

}
