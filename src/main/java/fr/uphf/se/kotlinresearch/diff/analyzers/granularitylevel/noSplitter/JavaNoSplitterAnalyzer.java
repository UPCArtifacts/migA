package fr.uphf.se.kotlinresearch.diff.analyzers.granularitylevel.noSplitter;

import fr.uphf.se.kotlinresearch.diff.analyzers.JavaDiffAnalyzer;

/**
 * 
 * @author Matias Martinez
 *
 */
public class JavaNoSplitterAnalyzer extends NoSplitterAbstractAnalyzer {

	public Class getMethod() {
		return JavaDiffAnalyzer.class;
	}

}
