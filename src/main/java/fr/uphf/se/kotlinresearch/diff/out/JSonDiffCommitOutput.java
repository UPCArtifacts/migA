package fr.uphf.se.kotlinresearch.diff.out;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import com.github.gumtreediff.actions.model.Action;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.IOutput;
import fr.inria.coming.main.ComingProperties;
import fr.inria.coming.utils.MapList;
import fr.uphf.se.kotlinresearch.diff.analyzers.KotlinDiffAnalyzer;
import fr.uphf.se.kotlinresearch.diff.analyzers.SingleDiff;

/**
 * 
 * @author Matias Martinez
 *
 */
public class JSonDiffCommitOutput implements IOutput {

	@Override
	public void generateFinalOutput(FinalResult finalResult) {
		// Nothing, for the moment..
	}

	@Override
	public void generateRevisionOutput(RevisionResult resultAllAnalyzed) {

		DiffResult<IRevision, SingleDiff> result = (DiffResult<IRevision, SingleDiff>) resultAllAnalyzed
				.getResultFromClass(KotlinDiffAnalyzer.class);

		MapList<String, SingleDiff> resultsByLanguage = new MapList<>();

		for (SingleDiff ri : result.getAll()) {
			resultsByLanguage.add(ri.getRevisionType(), ri);
		}
		IRevision revision = resultAllAnalyzed.getRelatedRevision();

		String revisionId = revision.getName();
		JsonObject root = new JsonObject();

		root.addProperty("id_revision", revisionId);

		JsonArray typesJSon = new JsonArray();
		root.add("rev_by_lang", typesJSon);

		for (String ilang : resultsByLanguage.keySet()) {

			JsonObject langJsonRev = new JsonObject();
			typesJSon.add(langJsonRev);

			langJsonRev.addProperty("lang", ilang);

			JsonArray revByCommitJson = new JsonArray();
			langJsonRev.add("diffs", revByCommitJson);

			List<SingleDiff> revisionsofLang = resultsByLanguage.get(ilang);

			for (SingleDiff singleDiff : revisionsofLang) {

				JsonObject jsonOfReview = new JsonObject();
				jsonOfReview.addProperty("name", singleDiff.getRevisionName());
				revByCommitJson.add(jsonOfReview);

				List<Action> actions = singleDiff.getRootOperations();
				JsonArray changesJson = new JsonArray();
				jsonOfReview.add("changes", changesJson);
				for (Action iaction : actions) {
					JsonObject changeJSon = new JsonObject();
					changesJson.add(changeJSon);
					changeJSon.addProperty("action", iaction.getName());
					// TODO: add the node pointed by the action to JSONformat
					// Cast the action for every case and write the nodes that correspond
					// changeJSon.addProperty("node_src", iaction.getName());
					// changeJSon.addProperty("node_target", iaction.getName());

					// We can put more info, as much as possible

				}

			}

		}

		FileWriter fw;
		try {
			String fileName = ComingProperties.getProperty("output") + File.separator + "outkotlin_analysis"
					+ revisionId + ".json";
			fw = new FileWriter(fileName);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(root.toString());
			String prettyJsonString = gson.toJson(je);
			System.out.println("\nJSON Code Change Frequency: (file stored at " + fileName + ")\n");
			System.out.println(prettyJsonString);
			fw.write(prettyJsonString);

			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
