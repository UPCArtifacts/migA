package fr.uphf.se.kotlinresearch.diff.analyzers.granularitylevel;

import org.apache.log4j.Logger;

import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtNamedElement;

/**
 * 
 * @author Matias Martinez
 *
 */
public class JavaMethodDiffSplitterAnalyzer extends JavaAbstractDiffSplitterAnalyzer {
	Logger log = Logger.getLogger(JavaMethodDiffSplitterAnalyzer.class.getName());

	public JavaMethodDiffSplitterAnalyzer(Class sourceAnalyzer) {
		super(sourceAnalyzer);
	}

	public JavaMethodDiffSplitterAnalyzer() {
		super();
	}

	public Class getParentElementToFind() {
		return CtExecutable.class;
	}

	public String getName(CtElement el) {
		if (el instanceof CtNamedElement) {
			String keyName = (el != null) ? ((CtNamedElement) el).getSimpleName() : "Unknown";
			return keyName;
		}
		return "Unknown";
	}

}
