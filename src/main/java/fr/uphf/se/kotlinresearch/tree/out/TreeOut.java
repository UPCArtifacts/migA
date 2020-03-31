package fr.uphf.se.kotlinresearch.tree.out;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

public class TreeOut {
	@SuppressWarnings("unchecked")
	public JSONObject getJSONasJsonObject(TreeContext context, ITree tree) {
		// JsonObject  o = new JsonObject();
		// o.put(JSON_PROPERTIES.label.toString(), tree.getLabel());
		// o.put(JSON_PROPERTIES.type.toString(), context.getTypeLabel(tree));

		// Link Id with CtElement
		JSONArray nodeChildens = new JSONArray();
		// o.put(JSON_PROPERTIES.children.toString(), nodeChildens);

		for (ITree tch : tree.getChildren()) {
			JSONObject childJSon = getJSONasJsonObject(context, tch);
			if (childJSon != null)
				nodeChildens.add(childJSon);
		}
		// return o;
		return null;
	}
}
