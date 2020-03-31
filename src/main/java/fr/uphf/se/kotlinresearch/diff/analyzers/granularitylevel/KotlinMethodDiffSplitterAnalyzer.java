package fr.uphf.se.kotlinresearch.diff.analyzers.granularitylevel;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.log4j.Logger;

import kastree.ast.all.Constructor;
import kastree.ast.all.Func;

/**
 * 
 * @author Matias Martinez
 *
 */
public class KotlinMethodDiffSplitterAnalyzer extends KotlinAbstractDiffSplitterAnalyzer {
	Logger log = Logger.getLogger(KotlinMethodDiffSplitterAnalyzer.class.getName());

	public KotlinMethodDiffSplitterAnalyzer() {
		// super();
		declarations = new HashSet<>(Arrays.asList(Func.class, Constructor.class

		// declarations = new HashSet<>(Arrays.asList(Func.class.getCanonicalName(),
		// Constructor.class.getCanonicalName()
		// "kastree.ast.Node$Decl$Func", "kastree.ast.Node$Decl$Constructor"
		));
	}

	@Override
	public String getGranularityName() {
		return "Method";
	}

}
