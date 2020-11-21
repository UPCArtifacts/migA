package fr.uphf.se.kotlinresearch.core.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;

import fr.inria.coming.utils.MapList;
import fr.uphf.se.kotlinresearch.arm.analyzers.AddRemoveResult;

/**
 * 
 * @author Matias Martinez
 *
 */
public class MigAIntermediateResultStore {

	public MigAIntermediateResultStore() {
	}

	// Intermediate results:
	public Map<String, AddRemoveResult> armresults = new HashMap<>();

	public Map<String, HunkSummarization> lines = new HashMap<>();

	public Map<String, Map<String, Object>> commitMetadata = new HashMap<>();

	public List<String> orderCommits = new ArrayList<>();

	public MapList<String, String> filesOfCommits = new MapList<>();

	public CommitPropertySummary features = new CommitPropertySummary();

	public CommitPropertySummary javaChanges = new CommitPropertySummary();

	public CommitPropertySummary kotlinChanges = new CommitPropertySummary();

	public Map<String, JsonElement> jsonChangeSplit = new HashMap();

	public List<String> commitsWithMigrationsRename = new ArrayList<>();

	//
	public List<String> commitsWithMigrationsRemoveMethodMethodAddFile = new ArrayList<>();
	public List<String> commitsWithMigrationsAddMethodRemoveMethod = new ArrayList<>();

	public Map<String, AddRemoveResult> getArmresults() {
		return armresults;
	}

	public Map<String, HunkSummarization> getLines() {
		return lines;
	}

	public List<String> getOrderCommits() {
		return orderCommits;
	}

	public MapList<String, String> getFilesOfCommits() {
		return filesOfCommits;
	}

	public CommitPropertySummary getFeatures() {
		return features;
	}

	public CommitPropertySummary getJavaChanges() {
		return javaChanges;
	}

	public CommitPropertySummary getKotlinChanges() {
		return kotlinChanges;
	}

}
