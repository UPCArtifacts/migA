package fr.uphf.se.kotlinresearch.diff.analyzers.granularitylevel;

import com.github.gumtreediff.actions.model.Action;

import fr.inria.coming.utils.MapList;
/**
 * 
 * @author Matias Martinez
 *
 */
public class SplittedActions {

	MapList<String, Action> actionsByTypeOfActions = new MapList<>();

	MapList<String, Action> actionsByEntityType = new MapList<>();

	public MapList<String, Action> getActionsByTypeOfActions() {
		return actionsByTypeOfActions;
	}

	public void setActionsByTypeOfActions(MapList<String, Action> actionsByTypeOfActions) {
		this.actionsByTypeOfActions = actionsByTypeOfActions;
	}

	public MapList<String, Action> getActionsByEntityType() {
		return actionsByEntityType;
	}

	public void setActionsByEntityType(MapList<String, Action> actionsByMethod) {
		this.actionsByEntityType = actionsByMethod;
	}

}
