package fr.uphf.se.kotlinresearch.diff.analyzers.granularitylevel;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.log4j.Logger;

import kastree.ast.all.Block;
import kastree.ast.all.BlockBody;
import kastree.ast.all.ExprBody;

/**
 * 
 * @author Matias Martinez
 *
 */
public class KotlinBlockDiffSplitterAnalyzer extends KotlinAbstractDiffSplitterAnalyzer {
	Logger log = Logger.getLogger(KotlinBlockDiffSplitterAnalyzer.class.getName());

	public KotlinBlockDiffSplitterAnalyzer() {
		super();

		declarations = new HashSet<>(Arrays.asList(Block.class, BlockBody.class, ExprBody.class

		// declarations = new HashSet<>(Arrays.asList(Block.class.getCanonicalName(),
		// BlockBody.class.getCanonicalName(),
		// ExprBody.class.getCanonicalName()
		// "kastree.ast.Node$Block", "kastree.ast.Node$Decl$Func$Body$Block",
		// "kastree.ast.Node$Decl$Func$Body$Expr"
		));
	}

	@Override
	public String getGranularityName() {
		return "Block";
	}

}
