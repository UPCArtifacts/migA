package fr.uphf.se.kotlinresearch.patterndiscov.analyzer;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.uphf.se.kotlinresearch.core.MigACore;
import fr.uphf.se.kotlinresearch.squarediff.entities.diff.QueryDiff;

/**
 * 
 * @author Matias Martinez
 *
 */
public class GenericPatternAnalyzer implements Analyzer<IRevision> {
	Logger log = Logger.getLogger(GenericPatternAnalyzer.class.getName());

	// private PatternPool pool = new PatternPool();

	protected String language = "";

	protected String granularity = "";

	/**
	 * Analyzer used to take the results
	 */
	protected Class diffResultSource = null;

	public GenericPatternAnalyzer(double threshold, double threshold_same, int maxPatternSizeRoots,
			int maxPatternAllNodes, String language, String granularity, Class diffResultSource) {

		this.language = language;
		this.granularity = granularity;
		this.diffResultSource = diffResultSource;
	}

	@SuppressWarnings("unchecked")
	@Override
	public AnalysisResult analyze(IRevision input, RevisionResult previousResults) {
		long initCommit = (new Date()).getTime();

		DiffResult<IRevision, List<QueryDiff>> dkotlin = (DiffResult<IRevision, List<QueryDiff>>) previousResults
				.getResultFromClass(diffResultSource);

		for (String fileKotlin : dkotlin.getDiffOfFiles().keySet()) {

			long initFile = (new Date()).getTime();

			List<QueryDiff> diffsByMethod = dkotlin.getDiffOfFiles().get(fileKotlin);

			log.debug("Element: " + fileKotlin + " with " + diffsByMethod.size() + " group element");
			for (QueryDiff singleFileDiff : diffsByMethod) {
				long initDiff = (new Date()).getTime();

				// TODO

				MigACore.executionsTime.add(getName() + "_" + this.language + "_Diff",
						new Long((new Date()).getTime() - initDiff));
			}
			MigACore.executionsTime.add(this.getClass().getSimpleName() + "_" + this.language + "_File",
					new Long((new Date()).getTime() - initFile));

		}
		MigACore.executionsTime.add(this.getClass().getSimpleName() + "_" + this.language + "_Commit",
				new Long((new Date()).getTime() - initCommit));

		return null;
	}

	public String getName() {
		return (this.getClass().getSimpleName() == null || this.getClass().getSimpleName().isEmpty())
				? "GenericPatternAnalyzer"
				: this.getClass().getSimpleName();
	}

	public String getLanguage() {
		return language;
	}

	public Class getDiffResultSource() {
		return diffResultSource;
	}

	@Override
	public String key() {

		return Analyzer.super.key() + "_" + language + "_" + granularity;
	}

}
