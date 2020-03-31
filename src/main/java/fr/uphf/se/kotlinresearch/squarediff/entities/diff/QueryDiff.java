package fr.uphf.se.kotlinresearch.squarediff.entities.diff;

import java.util.List;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

/**
 * A diff that will be used as query (Also temporal name)
 * 
 * @author Matias Martinez
 *
 */
public class QueryDiff extends SingleDiff {

	protected int identifier = -1;

	public QueryDiff(int id, TreeContext context, String revisionName, String revisionFileName, String revisionType,
			List<Action> actionsmethods) {
		super(context, revisionName, revisionFileName, revisionType, actionsmethods);
		this.identifier = id;
	}

	public QueryDiff(int id, TreeContext context, ITree rootSpoonLeft, ITree rootSpoonRight, String revisionName,
			String revisionFileName, String revisionType) {
		super(context, rootSpoonLeft, rootSpoonRight, revisionName, revisionFileName, revisionType);
		this.identifier = id;
	}

	public QueryDiff(TreeContext context, ITree t1, ITree t2) {
		super(context, t1, t2);
	}

	public int getIdentifier() {
		return identifier;
	}

}
