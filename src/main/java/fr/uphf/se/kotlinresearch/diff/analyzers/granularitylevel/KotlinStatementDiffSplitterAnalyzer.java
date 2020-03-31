package fr.uphf.se.kotlinresearch.diff.analyzers.granularitylevel;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.log4j.Logger;

import kastree.ast.all.Stmt;

/**
 * 
 * @author Matias Martinez
 *
 */
public class KotlinStatementDiffSplitterAnalyzer extends KotlinAbstractDiffSplitterAnalyzer {
	Logger log = Logger.getLogger(KotlinStatementDiffSplitterAnalyzer.class.getName());

	public KotlinStatementDiffSplitterAnalyzer() {
		super();

		declarations = new HashSet<>(Arrays.asList(Stmt.class));
	}

	@Override
	public String getGranularityName() {
		return "Block";
	}

}
