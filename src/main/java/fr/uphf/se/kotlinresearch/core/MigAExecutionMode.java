package fr.uphf.se.kotlinresearch.core;

import com.google.gson.JsonArray;

import fr.inria.coming.core.engine.Analyzer;
import fr.uphf.se.kotlinresearch.core.results.PatternResultSummary;
import fr.uphf.se.kotlinresearch.patterndiscov.analyzer.GenericPatternAnalyzer;

/**
 * 
 * @author Matias Martinez
 *
 */
@SuppressWarnings("rawtypes")
public class MigAExecutionMode {

	private String language;

	private Analyzer divisor;
	private String granularity;
	private GenericPatternAnalyzer patternAnalyzer;

	private PatternResultSummary resultOfMode = new PatternResultSummary();

	// Here we store the data
	public JsonArray codeInstances = new JsonArray();

	public MigAExecutionMode(String language, String granularity, Analyzer divisor, double threshold,
			double threshold_same, int maxPatternSizeRoots, int maxPatternAllNodes) {
		super();
		this.language = language;
		this.divisor = divisor;
		this.granularity = granularity;

		this.patternAnalyzer = new GenericPatternAnalyzer(threshold, threshold_same, maxPatternSizeRoots,
				maxPatternAllNodes, language, granularity, divisor.getClass());
	}

	public Class getDivisionClass() {
		return divisor.getClass();
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Analyzer getDivisor() {
		return divisor;
	}

	public void setDivisor(Analyzer divisor) {
		this.divisor = divisor;
	}

	public String getGranularity() {
		return granularity;
	}

	public GenericPatternAnalyzer getPatternAnalyzer() {
		return patternAnalyzer;
	}

	public PatternResultSummary getResultOfMode() {
		return resultOfMode;
	}

}
