package fr.uphf.se.kotlinresearch.diff.analyzers.granularitylevel;

import org.apache.log4j.Logger;

import spoon.reflect.code.CtBlock;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;

/**
 * 
 * @author Matias Martinez
 *
 */
public class JavaBlockDiffSplitterAnalyzer extends JavaAbstractDiffSplitterAnalyzer {
	Logger log = Logger.getLogger(JavaBlockDiffSplitterAnalyzer.class.getName());

	int blockId = 0;

	public JavaBlockDiffSplitterAnalyzer(Class sourceAnalyzer) {
		super(sourceAnalyzer);
	}

	public JavaBlockDiffSplitterAnalyzer() {
		super();
	}

	public Class getParentElementToFind() {
		return CtBlock.class;
	}

	public String getName(CtElement el) {
		if (el != null) {
			CtExecutable exec = el.getParent(CtExecutable.class);
			String exString = (exec != null) ? exec.getSimpleName() : "-NoMethodName-";

			return "block_" + exString + "_" + el.toString().hashCode() + "+" + el.getParent().toString().hashCode();
		}
		return "Unknown";
	}
}
