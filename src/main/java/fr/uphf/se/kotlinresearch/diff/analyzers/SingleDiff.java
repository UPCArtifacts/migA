package fr.uphf.se.kotlinresearch.diff.analyzers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.kotlin.org.jline.utils.Log;

import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import fr.inria.coming.main.ComingProperties;

/**
 * (I define a new Diff (i.e., not use that one from GTSpoon) because the
 * operations points to CtElements, which we dont have in our Kotlin model).
 * Thus, the diff is a list of GT actions.
 * 
 * @author Matias Martinez
 *
 */
public class SingleDiff {
	/**
	 * Actions over all tree nodes (CtElements)
	 */
	protected List<Action> allOperations;

	/**
	 * Actions over the changes roots.
	 */
	protected List<Action> rootOperations;
	/**
	 * the mapping of this diff
	 */
	protected MappingStore _mappingsComp;
	/**
	 * Context of the spoon diff.
	 */
	protected TreeContext context;
	/**
	 * The name of the revision
	 */
	protected String revisionName;

	/**
	 * The name of the revision
	 */
	protected String revisionFileName;
	/**
	 * the type of the revision: Java, Kotlin, etc.
	 */
	protected String revisionType;

	public Map<String, Object> metadata = new HashMap<>();

	static {
		// default 0.3
		// it seems that default value is really bad
		// 0.1 one failing much more changes
		// 0.2 one failing much more changes
		// 0.3 one failing test_t_224542
		// 0.4 fails for issue31
		// 0.5 fails for issue31
		// 0.6 OK
		// 0.7 1 failing
		// 0.8 2 failing
		// 0.9 two failing tests with more changes
		// see GreedyBottomUpMatcher.java in Gumtree
		System.setProperty("gumtree.match.bu.sim", "0.9");

		// default 1
		// MIN_HEIGHT
		System.setProperty("gumtree.match.gt.minh", "1");

		// Used by AbstractBottomUpMatcher
		// SIZE_THRESHOLD default 1000
		// System.setProperty("gt.bum.szt", "1000");
		// SIM_THRESHOLD default 0.5
		System.setProperty("gt.bum.smt", "0.9");

		// System.setProperty("gumtree.match.bu.size","10");

		// CD
		// LABEL_SIM_THRESHOLD = Double.parseDouble(System.getProperty("gt.cd.lsim",
		// "0.5"));

	}

	public SingleDiff(TreeContext context, ITree rootSpoonLeft, ITree rootSpoonRight) {
		this(context, rootSpoonLeft, rootSpoonRight, "no-info", "no-info", "no-info");
	}

	public SingleDiff(List<Action> allOperations, List<Action> rootOperations, MappingStore _mappingsComp,
			TreeContext context) {
		super();
		this.allOperations = allOperations;
		this.rootOperations = rootOperations;
		this._mappingsComp = _mappingsComp;
		this.context = context;
	}

	/**
	 * Used when diff results are already calculated
	 * 
	 * @param context
	 * @param revisionName
	 * @param revisionFileName
	 * @param revisionType
	 * @param actionsmethods
	 */
	public SingleDiff(TreeContext context, String revisionName, String revisionFileName, String revisionType,
			List<Action> actionsmethods) {
		this.revisionName = revisionName;
		this.revisionFileName = revisionFileName;
		this.revisionType = revisionType;
		this.context = context;
		this.rootOperations = actionsmethods;
		this.allOperations = null;
		this._mappingsComp = null;
	}

	public SingleDiff(TreeContext context, ITree rootSpoonLeft, ITree rootSpoonRight, String revisionName,
			String revisionFileName, String revisionType) {

		this.revisionName = revisionName;
		this.revisionFileName = revisionFileName;
		this.revisionType = revisionType;
		this.context = context;

		final MappingStore mappingsComp = new MappingStore();

		Matcher matcher = null;

		String matchername = ComingProperties.getProperty("astmatcher");
		if (matchername == null || matchername.equals("classicgumtree"))
			matcher = new CompositeMatchers.ClassicGumtree(rootSpoonLeft, rootSpoonRight, mappingsComp);
		else if (matchername.equals("GumtreeTopDown".toLowerCase()))
			matcher = new CompositeMatchers.GumtreeTopDown(rootSpoonLeft, rootSpoonRight, mappingsComp);
		else if (matchername.equals("CompleteGumtreeMatcher".toLowerCase()))
			matcher = new CompositeMatchers.CompleteGumtreeMatcher(rootSpoonLeft, rootSpoonRight, mappingsComp);
		else if (matchername.equals("ChangeDistiller".toLowerCase()))
			matcher = new CompositeMatchers.ChangeDistiller(rootSpoonLeft, rootSpoonRight, mappingsComp);
		else if (matchername.equals("XyMatcher".toLowerCase()))
			matcher = new CompositeMatchers.XyMatcher(rootSpoonLeft, rootSpoonRight, mappingsComp);
		else
			// default
			matcher = new CompositeMatchers.ClassicGumtree(rootSpoonLeft, rootSpoonRight, mappingsComp);

		matcher.match();

		int with = 0, without = 0;

		for (ITree il : rootSpoonLeft.breadthFirst()) {
			if (mappingsComp.hasSrc(il) && mappingsComp.getDst(il) != null) {
				with++;

			} else {
				Log.debug("No mapping l: " + il.toShortString());
				without++;

			}
		}
		Log.debug("Result left with: " + with + " without" + without);
		//

		with = 0;
		without = 0;
		for (ITree ir : rootSpoonRight.breadthFirst()) {
			if (mappingsComp.hasDst(ir) && mappingsComp.getSrc(ir) != null) {
				with++;

			} else {
				Log.debug("No mapping l: " + ir.toShortString());
				without++;

			}
		}
		Log.debug("Result right with: " + with + " without" + without);

		final ActionGenerator actionGenerator = new ActionGenerator(rootSpoonLeft, rootSpoonRight,
				matcher.getMappings());
		actionGenerator.generate();

		SingleActionClassifier actionClassifier = new SingleActionClassifier(matcher.getMappingsAsSet(),
				actionGenerator.getActions());

		this.rootOperations = actionClassifier.getRootActions();
		this.allOperations = actionGenerator.getActions();

		this._mappingsComp = mappingsComp;

	}

	public List<Action> getAllOperations() {
		return allOperations;
	}

	public List<Action> getRootOperations() {
		return rootOperations;
	}

	@Override
	public String toString() {
		return "DiffK [rootOperations=" + rootOperations + "]";
	}

	public TreeContext getContext() {
		return this.context;
	}

	public String getRevisionName() {
		return revisionName;
	}

	public void setRevisionName(String revisionName) {
		this.revisionName = revisionName;
	}

	public String getRevisionType() {
		return revisionType;
	}

	public void setRevisionType(String revisionType) {
		this.revisionType = revisionType;
	}

	public String getRevisionFileName() {
		return revisionFileName;
	}

	public void setRevisionFileName(String revisionFileName) {
		this.revisionFileName = revisionFileName;
	}

	public MappingStore getMappingsComp() {
		return _mappingsComp;
	}

	public void setAllOperations(List<Action> allOperations) {
		this.allOperations = allOperations;
	}

	public void setRootOperations(List<Action> rootOperations) {
		this.rootOperations = rootOperations;
	}
}