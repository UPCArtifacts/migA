package fr.uphf.se.kotlinresearch.diff;

import com.github.gumtreediff.tree.ITree;

import fr.uphf.se.kotlinresearch.squarediff.entities.diff.QueryDiff;
import fr.uphf.se.kotlinresearch.tree.analyzers.JavaTreeAnalyzer;
import spoon.reflect.declaration.CtClass;

/**
 * Used in test
 * 
 * @author Matias Martinez
 *
 */
public class SingleDiffComparator {

	JavaTreeAnalyzer treeAnalyzer = new JavaTreeAnalyzer();

	public QueryDiff compare(CtClass c1, CtClass c2) {

		ITree t1 = treeAnalyzer.getTree(c1);
		ITree t2 = treeAnalyzer.getTree(c2);
		QueryDiff diff = new QueryDiff(treeAnalyzer.getContext(), t1, t2);
		return diff;
	}
}
