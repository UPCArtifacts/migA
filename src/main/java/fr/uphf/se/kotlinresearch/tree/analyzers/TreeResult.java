package fr.uphf.se.kotlinresearch.tree.analyzers;

import java.util.Map;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import fr.inria.coming.changeminer.entity.IRevision;

/**
 * 
 * @author Matias Martinez
 *
 */
public class TreeResult extends fr.inria.coming.core.entities.AnalysisResult<IRevision> {

	public static final int LEFT = 0;
	public static final int RIGHT = 1;

	private Map<String, ITree[]> treeOfFiles = null;
	private TreeContext context;

	public TreeResult(IRevision analyzed, Map<String, ITree[]> treeOfFiles, TreeContext context) {
		super(analyzed);
		this.treeOfFiles = treeOfFiles;
		this.context = context;
	}

	public Map<String, ITree[]> getTreeOfFiles() {
		return treeOfFiles;
	}

	public void setTreeOfFiles(Map<String, ITree[]> treeOfFiles) {
		this.treeOfFiles = treeOfFiles;
	}

	public TreeContext getContext() {
		return context;
	}

	public void setContext(TreeContext context) {
		this.context = context;
	}

}
