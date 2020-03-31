package fr.uphf.se.kotlinresearch.diff.analyzers.granularitylevel;

import org.apache.log4j.Logger;

import com.github.gumtreediff.tree.ITree;

import gumtree.spoon.builder.SpoonGumTreeBuilder;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.visitor.filter.LineFilter;

/**
 * 
 * @author Matias Martinez
 *
 */
public class JavaStatementDiffSplitterAnalyzer extends JavaAbstractDiffSplitterAnalyzer {
	Logger log = Logger.getLogger(JavaStatementDiffSplitterAnalyzer.class.getName());

	public Class getParentElementToFind() {
		return CtStatement.class;
	}

	public String getName(CtElement el) {
		if (el != null) {
			CtExecutable exec = el.getParent(CtExecutable.class);
			String exString = (exec != null) ? exec.getSimpleName() : "-NoMethodName-";

			return "block_" + exString + "_" + el.toString().hashCode() + "+" + el.getParent().toString().hashCode();
		}
		return "Unknown";
	}

	@Override
	public boolean mustFirst() {
		return false;
	}

	LineFilter filter = new LineFilter();

	@Override
	public ITree getSplitElement(CtElement assoiatedSpoon) {
		if (assoiatedSpoon == null)
			return null;

		CtElement parent = assoiatedSpoon.getParent(filter);
		if (parent != null) {
			ITree itree = (ITree) parent.getAllMetadata().get(SpoonGumTreeBuilder.GUMTREE_NODE);
			return itree;
		}
		return null;
	}
}
