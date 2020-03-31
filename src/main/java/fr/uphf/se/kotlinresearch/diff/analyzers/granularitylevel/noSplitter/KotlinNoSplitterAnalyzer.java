package fr.uphf.se.kotlinresearch.diff.analyzers.granularitylevel.noSplitter;

import fr.uphf.se.kotlinresearch.diff.analyzers.KotlinDiffAnalyzer;

/**
 * 
 * @author Matias Martinez
 *
 */
public class KotlinNoSplitterAnalyzer extends NoSplitterAbstractAnalyzer {

	public Class getMethod() {
		return KotlinDiffAnalyzer.class;
	}

}
