package fr.uphf.se.kotlinresearch.core.results;

import java.util.HashMap;

/**
 * 
 * @author Matias Martinez
 *
 */
public class GranularityResultSummary extends HashMap<String, PatternResultSummary> {
	public static String COMMIT = "commit";
	public static String METHOD = "method";
	public static String FILE = "file";
	public static String BLOCK = "block";
	public static String STATEMENT = "stmt";

	public void init(String key) {
		this.put(key, new PatternResultSummary());
	}

}
