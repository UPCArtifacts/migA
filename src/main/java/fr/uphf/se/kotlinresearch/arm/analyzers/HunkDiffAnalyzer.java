package fr.uphf.se.kotlinresearch.arm.analyzers;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.compare.rangedifferencer.RangeDifference;

import fr.inria.coming.changeminer.entity.GranuralityType;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.HunkDiff;
import fr.inria.coming.core.entities.HunkPair;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.IRevisionPair;
import fr.inria.coming.core.filter.diff.syntcomparison.Fragmentable;
import fr.inria.coming.core.filter.diff.syntcomparison.FragmentableComparator;
import fr.inria.coming.core.filter.diff.syntcomparison.LineComparator;
import fr.inria.coming.main.ComingProperties;
import fr.uphf.se.kotlinresearch.core.MigACore;

/**
 *
 *
 * @author Matias Martinez, matias.martinez@inria.fr
 *
 */
public class HunkDiffAnalyzer implements Analyzer<IRevision> {
	FragmentableComparator comparator = new LineComparator();

	Logger log = Logger.getLogger(HunkDiffAnalyzer.class.getName());

	protected GranuralityType granularity;

	/**
	 * 
	 * @param typeLabel     node label to mine
	 * @param operationType operation type to mine
	 */
	public HunkDiffAnalyzer() {
		granularity = GranuralityType.valueOf(ComingProperties.getProperty("GRANULARITY"));
	}

	/**
	 * Analyze a commit finding instances of changes return a Map<FileCommit, List>
	 */
	@SuppressWarnings("rawtypes")
	public AnalysisResult<IRevision> analyze(IRevision revision) {

		return null;
	}

	@Override
	public AnalysisResult analyze(IRevision revision, RevisionResult previousResults) {
		long init = (new Date()).getTime();
		RenameAnalyzerResult renameresult = (RenameAnalyzerResult) previousResults
				.getResultFromClass(FileCommitNameAnalyzer.class);

		List<IRevisionPair<String>> childerPairs = renameresult.getAllFileCommits();

		Map<String, HunkDiff> diffOfFiles = new HashMap<>();

		for (IRevisionPair<String> fileFromRevision : childerPairs) {

			HunkDiff hunks = getNumberChanges(fileFromRevision.getPreviousVersion(), fileFromRevision.getNextVersion());

			if (hunks != null) {
				diffOfFiles.put(fileFromRevision.getName(), hunks);
			}
		}
		MigACore.executionsTime.add(this.getClass().getSimpleName(), new Long((new Date()).getTime() - init));

		// TODO: refactor
		return new DiffResult<IRevision, HunkDiff>(revision, diffOfFiles);
	}

	protected HunkDiff getNumberChanges(String previousVersion, String nextVersion) {
		HunkDiff ranges = new HunkDiff();
		try {
			Fragmentable fPreviousVersion = comparator.createFragmentable(previousVersion);
			Fragmentable fNextVersion = comparator.createFragmentable(nextVersion);
			RangeDifference[] results = comparator.compare(fPreviousVersion, fNextVersion);

			for (RangeDifference diffInfo : results) {
				if (diffInfo.kind() != RangeDifference.NOCHANGE /* && diffInfo.kind() != RangeDifference.ANCESTOR */) {
					// TODO: for the moment, ignoring here hunk filtering
					// int length = diff.rightEnd() - diff.rightStart();
					// if (length <= ComingProperties.getPropertyInteger("max_lines_per_hunk"))
					ranges.add(diffInfo);
					String left = "";
					String right = "";

					if (diffInfo.ancestorStart() == 0 && diffInfo.ancestorEnd() == 0
							|| diffInfo.ancestorStart() == 1 && diffInfo.ancestorLength() == 0)
						continue;

					for (int i = diffInfo.ancestorStart(); i < diffInfo.ancestorEnd(); i++) {
						// System.out.println(diffInfo);
						left += fPreviousVersion.getFragment(i) + "\n";
					}

					for (int i = diffInfo.rightStart(); i < diffInfo.rightEnd(); i++) {

						right += fNextVersion.getFragment(i) + "\n";
					}

					ranges.getHunkpairs().add(new HunkPair(left, right));

				}
			}
		} catch (Throwable e) {
			log.error("Problems computing hunk diff: " + e);
			e.printStackTrace();
		}
		return ranges;
	}

}