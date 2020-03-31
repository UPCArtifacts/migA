package fr.uphf.se.kotlinresearch.arm.analyzers;

import java.util.Date;
import java.util.List;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.FileCommit;
import fr.inria.coming.core.entities.interfaces.IRevisionPair;
import fr.uphf.se.kotlinresearch.core.MigACore;

/**
 * 
 * @author Matias Martinez
 *
 */
@SuppressWarnings("rawtypes")
public class AddedRemovedAnalyzer implements Analyzer<IRevision> {

	@Override
	public AnalysisResult analyze(IRevision revision, RevisionResult previousResults) {
		long init = (new Date()).getTime();
		AddRemoveResult result = new AddRemoveResult(revision);

		RenameAnalyzerResult renameresult = (RenameAnalyzerResult) previousResults
				.getResultFromClass(FileCommitNameAnalyzer.class);

		List<IRevisionPair<String>> childerPairs = renameresult.getAllFileCommits();// ((Commit)
																					// revision).getFileCommits();

		// For each file inside the revision
		for (IRevisionPair iRevisionPair : childerPairs) {

			// If koltin
			String currentFilename = iRevisionPair.getName();
			String previousFileName = iRevisionPair.getPreviousName();

			if (previousFileName.endsWith(".java") && isRemove(iRevisionPair)) {
				result.removedJava.add(previousFileName);
			} else if (previousFileName.endsWith(".kt") && isRemove(iRevisionPair)) {
				result.removedKotlin.add(previousFileName);
			} else if (currentFilename.endsWith(".kt") && !previousFileName.endsWith(".java")) {

				if (isAdded(iRevisionPair))
					result.addedKotlin.add(currentFilename);
				else if (isModif(iRevisionPair)) {
					result.modifKotlin.add(currentFilename);
				} else if (isDiffName(iRevisionPair))
					result.diffName.add(currentFilename);
			}
			// java
			else if (currentFilename.endsWith(".java") && !previousFileName.endsWith(".kt")) {

				if (isAdded(iRevisionPair))
					result.addedJava.add(currentFilename);
				else if (isModif(iRevisionPair))
					result.modifJava.add(currentFilename);
				else if (isDiffName(iRevisionPair))
					result.diffName.add(currentFilename);
			} else {
				// migration cases
				if (currentFilename.endsWith(".java") && previousFileName.endsWith(".kt")) {
					result.migrationKotlinToJava.add(currentFilename);
				} else if (currentFilename.endsWith(".kt") && previousFileName.endsWith(".java")) {
					result.migrationJavaToKotlin.add(currentFilename);
				}

			}

		}
		MigACore.executionsTime.add(this.getClass().getSimpleName(), new Long((new Date()).getTime() - init));

		return result;
	}

	public boolean isAdded(IRevisionPair iRevisionPair) {

		return FileCommitNameAnalyzer.isNull(iRevisionPair.getPreviousVersion())
				|| FileCommitNameAnalyzer.isNull(iRevisionPair.getPreviousName());

	}

	public boolean isDiffName(IRevisionPair iRevisionPair) {
		if (iRevisionPair instanceof FileCommit) {
			FileCommit fc = (FileCommit) iRevisionPair;
			return fc.getPreviousFileName().equals(fc.getNextFileName());
		}
		return false;

	}

	public boolean isModif(IRevisionPair iRevisionPair) {
		return !FileCommitNameAnalyzer.isNull(iRevisionPair.getPreviousVersion())
				&& !FileCommitNameAnalyzer.isNull(iRevisionPair.getNextVersion());

	}

	public boolean isRemove(IRevisionPair iRevisionPair) {

		return FileCommitNameAnalyzer.isNull(iRevisionPair.getNextVersion())
				|| FileCommitNameAnalyzer.isNull(iRevisionPair.getName());

	}
}
