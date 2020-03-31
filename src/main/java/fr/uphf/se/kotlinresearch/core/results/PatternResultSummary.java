package fr.uphf.se.kotlinresearch.core.results;

/**
 * 
 * @author Matias Martinez
 *
 */
public class PatternResultSummary {

	CommitPatternResultSimplification fullResultSummary = new CommitPatternResultSimplification();
	CommitPatternResultSimplification partialResultSummary = new CommitPatternResultSimplification();

	public PatternResultSummary() {

	}

	public CommitPatternResultSimplification getFullResultSummary() {
		return fullResultSummary;
	}

	public CommitPatternResultSimplification getPartialResultSummary() {
		return partialResultSummary;
	}

}