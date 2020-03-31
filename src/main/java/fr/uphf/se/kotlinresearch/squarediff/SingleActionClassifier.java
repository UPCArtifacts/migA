package fr.uphf.se.kotlinresearch.squarediff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;

/**
 * Action Classifier (Similar to that one from GT-spoon but without reference to
 * Spoon Objects). It will be deprecated with the new version of GT.
 *
 * @author Matias Martinez, matias.martinez@inria.fr
 */
public class SingleActionClassifier {
	// /
	// ROOT CLASSIFIER
	// /
	private List<ITree> srcUpdTrees = new ArrayList<>();
	private List<ITree> dstUpdTrees = new ArrayList<>();
	private List<ITree> srcMvTrees = new ArrayList<>();
	private List<ITree> dstMvTrees = new ArrayList<>();
	private List<ITree> srcDelTrees = new ArrayList<>();
	private List<ITree> dstAddTrees = new ArrayList<>();
	private Map<ITree, Action> originalActionsSrc = new HashMap<>();
	private Map<ITree, Action> originalActionsDst = new HashMap<>();
	Logger log = Logger.getLogger(SingleActionClassifier.class.getName());

	public SingleActionClassifier(Set<Mapping> rawMappings, List<Action> actions) {
		clean();
		MappingStore mappings = new MappingStore(rawMappings);
		for (Action action : actions) {
			final ITree original = action.getNode();
			if (action instanceof Delete) {
				srcDelTrees.add(original);
				originalActionsSrc.put(original, action);
			} else if (action instanceof Insert) {
				dstAddTrees.add(original);
				originalActionsDst.put(original, action);
			} else if (action instanceof Update) {
				ITree dest = mappings.getDst(original);
				srcUpdTrees.add(original);
				dstUpdTrees.add(dest);
				originalActionsSrc.put(original, action);
			} else if (action instanceof Move) {
				ITree dest = mappings.getDst(original);
				if (dest == null) {
					// System.out.println("Dest null");
					// ITree srcM = mappings.getSrc(original);
					// System.out.println();
					// log.error("Move with node not mapped " + action.getNode().getType());
					continue;
				}
				srcMvTrees.add(original);
				dstMvTrees.add(dest);
				originalActionsDst.put(dest, action);
			}
		}
	}

	/**
	 * This method retrieves ONLY the ROOT actions
	 */
	public List<Action> getRootActions() {
		final List<Action> rootActions = srcUpdTrees.stream().map(t -> originalActionsSrc.get(t))
				.collect(Collectors.toList());

		rootActions.addAll(srcDelTrees.stream() //
				.filter(t -> !srcDelTrees.contains(t.getParent()) && !srcUpdTrees.contains(t.getParent())) //
				.map(t -> originalActionsSrc.get(t)) //
				.collect(Collectors.toList()));

		rootActions.addAll(dstAddTrees.stream() //
				.filter(t -> !dstAddTrees.contains(t.getParent()) && !dstUpdTrees.contains(t.getParent())) //
				.map(t -> originalActionsDst.get(t)) //
				.collect(Collectors.toList()));

		rootActions.addAll(dstMvTrees.stream() //
				.filter(t -> !dstMvTrees.contains(t.getParent())) //
				.map(t -> originalActionsDst.get(t)) //
				.collect(Collectors.toList()));

		rootActions.removeAll(Collections.singleton(null));
		return rootActions;
	}

	private void clean() {
		srcUpdTrees.clear();
		dstUpdTrees.clear();
		srcMvTrees.clear();
		dstMvTrees.clear();
		srcDelTrees.clear();
		dstAddTrees.clear();
		originalActionsSrc.clear();
		originalActionsDst.clear();
	}
}
