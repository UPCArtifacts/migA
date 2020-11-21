package fr.uphf.se.kotlinresearch.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.utils.Pair;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fr.inria.coming.main.ComingProperties;
import fr.inria.coming.utils.MapList;
import fr.uphf.se.kotlinresearch.arm.analyzers.AddRemoveResult;
import fr.uphf.se.kotlinresearch.core.CommitDataAnalyzer;
import fr.uphf.se.kotlinresearch.core.MigACore;
import fr.uphf.se.kotlinresearch.core.results.MigAIntermediateResultStore;
import fr.uphf.se.kotlinresearch.core.results.PatternInstanceProperties;
import fr.uphf.se.kotlinresearch.core.results.QueryDiffFromCommitResult;

/**
 * 
 * @author Matias Martinez
 *
 */
public class MigAJSONSerializer {
	Logger log = Logger.getLogger(MigAJSONSerializer.class.getName());
	Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public JsonElement getHunkCommit(MapList<String, Pair<Integer, Integer>> hunks) {
		JsonArray arr = new JsonArray();
		for (String file : hunks.keySet()) {
			JsonObject root = new JsonObject();
			arr.add(root);

			root.addProperty("file", file);
			JsonArray hunksjson = getHunkFile(file, hunks);
			root.add("hunks", hunksjson);
		}
		return arr;
	}

	public JsonArray getHunkFile(String file, MapList<String, Pair<Integer, Integer>> hunks) {
		JsonArray hunksjson = new JsonArray();
		if (!hunks.containsKey(file)) {
			return hunksjson;
		}

		for (Pair<Integer, Integer> pair : hunks.get(file)) {

			JsonObject p = new JsonObject();
			p.addProperty("l", pair.first);
			p.addProperty("r", pair.second);
			hunksjson.add(p);
		}
		return hunksjson;
	}

	public ITree getTree(JsonObject itreeJson, TreeContext generalContext) {

		ITree root1 = generalContext.createTree(itreeJson.get("typeid").getAsInt(),
				itreeJson.get("label").getAsString(), itreeJson.get("type").getAsString());
		root1.setId(itreeJson.get("id").getAsInt());

		if (itreeJson.has("posParent")) {
			root1.setMetadata("posParent", itreeJson.get("posParent").getAsBigInteger());
		}

		JsonArray childenJSon = itreeJson.get("children").getAsJsonArray();
		List<ITree> childrenITree = new ArrayList<>();
		for (JsonElement jsonElement : childenJSon) {
			ITree child = getTree(jsonElement.getAsJsonObject(), generalContext);
			childrenITree.add(child);
		}

		Collections.sort(childrenITree, (e1, e2) -> ((BigInteger) e1.getMetadata("posParent"))
				.compareTo((BigInteger) e2.getMetadata("posParent")));

		root1.setChildren(childrenITree);

		return root1;
	}

	public JsonObject itreeToJson(ITree node, TreeContext context) {

		JsonObject root = new JsonObject();
		// root.addProperty("pos", pattern.getPos());
		root.addProperty("typeid", node.getType());
		root.addProperty("type", context.getTypeLabel(node.getType()));
		root.addProperty("label", node.getLabel());
		root.addProperty("id", node.getId());

		JsonArray childrenjson = new JsonArray();

		root.add("children", childrenjson);
		int posInParent = 0;
		for (ITree child : node.getChildren()) {
			JsonObject childJson = itreeToJson(child, context);
			childJson.addProperty("posParent", posInParent);
			childrenjson.add(childJson);
			posInParent++;
		}
		return root;
	}

	public void storeJSon(File folder, String name, JsonElement toStore) {

		String ppjson = gson.toJson(toStore);

		String out = folder.getAbsolutePath();
		String outpath = out + File.separator + name + ".json";
		// log.info("Saving json at:\n" + outpath);
		try {
			FileWriter fw = new FileWriter(new File(outpath));
			fw.write(ppjson);
			fw.flush();
			fw.close();
		} catch (IOException e) {

			e.printStackTrace();
			log.error(e);
		}

	}

	public void saveAll(String projectName, File outDir, long executionTimeSeconds,
			MigAIntermediateResultStore results) {

		JsonObject main = extractJSon(projectName, executionTimeSeconds, results);
		System.out.println("Save results in dir: " + outDir);
		// Save on disk
		this.storeJSon(outDir, "info-" + projectName, main);

	}

	public JsonObject extractJSon(String projectName, long executionTimeSeconds, MigAIntermediateResultStore results) {
		JsonObject main = new JsonObject();
		main.addProperty("project", projectName);
		main.addProperty("execution_time_sec", executionTimeSeconds);
		main.addProperty("nrcommits", results.orderCommits.size());
		JsonArray root = new JsonArray();
		main.add("commits", root);
		// addExecutionTime(main);

		// Commits with migrations:
		JsonArray commitsWithMigrationsRename = new JsonArray();

		for (String commit : results.commitsWithMigrationsRename) {
			commitsWithMigrationsRename.add(commit);
		}
		main.add("commitsWithMigrationsRename", commitsWithMigrationsRename);

		int i = 1;
		for (String commit : results.orderCommits) {
			// general info commit:
			JsonObject commitjson = new JsonObject();
			root.add(commitjson);
			commitjson.addProperty("nr", i);
			commitjson.addProperty("commit", commit);

			// Save ARM
			AddRemoveResult arm = results.armresults.get(commit);
			JsonObject armjson = amrToJson(arm);
			commitjson.add("arm", armjson);

			JsonArray files = new JsonArray();
			commitjson.add("file", files);

			// Save ARM
			Map<String, Object> message = results.commitMetadata.get(commit);
			commitjson.addProperty("message", String.valueOf(message.get(CommitDataAnalyzer.MESSAGE)));
			commitjson.addProperty("date", String.valueOf(message.get(CommitDataAnalyzer.DATE)));
			commitjson.addProperty("dateint", String.valueOf(message.get(CommitDataAnalyzer.DATEINT)));
			commitjson.addProperty("author", String.valueOf(message.get(CommitDataAnalyzer.AUTHOR)));
			commitjson.addProperty("email", String.valueOf(message.get(CommitDataAnalyzer.EMAIL)));

			if (ComingProperties.getPropertyBoolean("includeBranches")) {
				JsonArray branches = new JsonArray();

				for (String branch : ((List<String>) message.get(CommitDataAnalyzer.BRANCHES)))
					branches.add(branch);

				commitjson.add("branches", branches);
			}

			JsonArray parents = new JsonArray();

			for (String parent : ((List<String>) message.get(CommitDataAnalyzer.PARENTS)))
				parents.add(parent);

			commitjson.add("parents", parents);

			MapList<String, Pair<Integer, Integer>> hunks = results.lines.get(commit);
			MapList<String, String> featuresCommits = results.features.get(commit);

			MapList<String, String> javaChangesCommit = results.javaChanges.get(commit);
			MapList<String, String> kotlinChangesCommit = results.kotlinChanges.get(commit);

			JsonElement changeSplitted = results.jsonChangeSplit.get(commit);
			// Change splitted:
			commitjson.add("change_splitted", changeSplitted);

			if (results.filesOfCommits.containsKey(commit)) {

				for (String fileCommit : results.filesOfCommits.get(commit)) {
					JsonObject rootfileCommit = new JsonObject();
					rootfileCommit.addProperty("file", fileCommit);
					files.add(rootfileCommit);
					// hunk

					if (ComingProperties.getPropertyBoolean(MigACore.ANALYZE_HUNKS)) {
						JsonElement hunk = getHunkCommit(hunks);
						commitjson.add("lines", hunk);

						JsonArray hunksjson = getHunkFile(fileCommit, hunks);
						rootfileCommit.add("hunks", hunksjson);
					}
					//
					// Features
					if (featuresCommits != null) {
						JsonElement feat = getFeaturesCommit(featuresCommits);
						commitjson.add("features", feat);

						JsonArray featjson = getFeaturesFIle(fileCommit, featuresCommits);

						rootfileCommit.add("features", featjson);

					}

					// java changes:

					JsonElement javachjson = getChangesFile(fileCommit, javaChangesCommit);

					rootfileCommit.add("javachanges", javachjson);

					// kotlin changes:

					JsonElement kotlinchjson = getChangesFile(fileCommit, kotlinChangesCommit);

					rootfileCommit.add("kotlinchanges", kotlinchjson);

				}
			}
			// end
			i++;
		}
		return main;
	}

	private void addExecutionTime(JsonObject main) {
		JsonObject executionTimes = new JsonObject();
		main.add("executionTime", executionTimes);

		int total = 0;
		for (String analyzer : MigACore.executionsTime.keySet()) {

			List<Long> values = MigACore.executionsTime.get(analyzer);
			long v = 0;
			for (Long long1 : values) {
				v += long1;
			}
			total += v;
			v /= (long) (values.size());
			Collections.sort(values);
			executionTimes.addProperty("avg_" + analyzer, (v));

			JsonArray js = new JsonArray();
			for (Long long1 : values) {
				js.add(long1);
			}
			executionTimes.add("all_" + analyzer, js);

		}
		executionTimes.addProperty("total", total);

	}

	private JsonElement getPatterns(String fileCommit, QueryDiffFromCommitResult patternsOfCommits) {
		List<PatternInstanceProperties> patterns = patternsOfCommits.get(fileCommit);

		if (patterns != null) {

			JsonArray allpatterns = new JsonArray();

			for (Map<String, Object> map : patterns) {
				JsonObject pattern = new JsonObject();
				allpatterns.add(pattern);
				for (String k : map.keySet()) {
					if (k != null) {
						Object content = map.get(k);
						if (k.equals(MigACore.JSON_CODE_INSTANCE)) {
							if (content != null && content instanceof JsonElement) {
								pattern.add(k, (JsonElement) content);
							}
						} else
							pattern.addProperty(k, content.toString());
					}
				}
			}
			return allpatterns;
		}

		return null;
	}

	@Deprecated
	private JsonElement getFeaturesCommit(MapList<String, String> featuresCommits) {
		JsonArray root = new JsonArray();
		for (String file : featuresCommits.keySet()) {
			JsonObject filejson = new JsonObject();
			root.add(filejson);
			filejson.addProperty("file", file);

			JsonArray featjson = getFeaturesFIle(file, featuresCommits);

			filejson.add("features", featjson);

		}
		return root;
	}

	public JsonArray getFeaturesFIle(String file, MapList<String, String> featuresCommits) {

		JsonArray featjson = new JsonArray();
		if (!featuresCommits.containsKey(file)) {
			return featjson;
		}

		List<String> feats = featuresCommits.get(file);

		for (String fi : feats) {
			featjson.add(fi);
		}
		return featjson;
	}

	public JsonElement getChangesFile(String file, MapList<String, String> featuresCommits) {
		JsonObject root = new JsonObject();
		JsonArray featjson = new JsonArray();
		if (!featuresCommits.containsKey(file)) {
			root.addProperty("nr", 0);
			root.add("actions", featjson);
			return root;
		}
		List<String> feats = featuresCommits.get(file);

		root.addProperty("nr", feats.size());
		root.add("actions", featjson);
		for (String fi : feats) {
			featjson.add(fi);
		}
		return root;
	}

	public void save(String projectName, File outDir, List<Pair<String, AddRemoveResult>> armresults) {
		JsonElement json = getARMJson(armresults);
		this.storeJSon(outDir, "arm-" + projectName, json);

	}

	public JsonElement getARMJson(List<Pair<String, AddRemoveResult>> armresults) {
		JsonArray root = new JsonArray();

		int number = 0;
		for (Pair<String, AddRemoveResult> pair : armresults) {
			String commit = pair.first;
			AddRemoveResult arm = pair.second;

			jsonOfAMR(root, number, commit, arm);

			number++;
		}

		return root;

	}

	public void jsonOfAMR(JsonArray root, int number, String commit, AddRemoveResult arm) {
		JsonObject commitjson = new JsonObject();
		root.add(commitjson);
		commitjson.addProperty("nr", number);
		commitjson.addProperty("commit", commit);
		// commitjson.addProperty("commit", commit.getName());
		// commitjson.addProperty("data", commit.getRevDate());

		// amrToJson(arm, commitjson);
	}

	public JsonObject amrToJson(AddRemoveResult arm) {
		JsonObject commitjson = new JsonObject();
		commitjson.addProperty("nr_addedJava", arm.addedJava.size());
		commitjson.addProperty("nr_modifJava", arm.modifJava.size());
		commitjson.addProperty("nr_removedJava", arm.removedJava.size());

		commitjson.addProperty("nr_addedKotlin", arm.addedKotlin.size());
		commitjson.addProperty("nr_modifKotlin", arm.modifKotlin.size());
		commitjson.addProperty("nr_removedKotlin", arm.removedKotlin.size());

		commitjson.addProperty("nr_migr_java_kotlin", (arm.migrationJavaToKotlin).size());
		commitjson.addProperty("nr_migr_kotlin_java", (arm.migrationKotlinToJava).size());

		commitjson.add("addedJava", getAll(arm.addedJava));
		commitjson.add("modifJava", getAll(arm.modifJava));
		commitjson.add("removedJava", getAll(arm.removedJava));

		commitjson.add("addedKotlin", getAll(arm.addedKotlin));
		commitjson.add("modifKotlin", getAll(arm.modifKotlin));
		commitjson.add("removedKotlin", getAll(arm.removedKotlin));

		commitjson.add("migr_java_kotlin", getAll(arm.migrationJavaToKotlin));
		commitjson.add("migr_kotlin_java", getAll(arm.migrationKotlinToJava));
		return commitjson;
	}

	public JsonArray getAll(List<String> strs) {
		JsonArray arr = new JsonArray();

		for (String element : strs) {
			arr.add(element);
		}
		return arr;
	}

}
