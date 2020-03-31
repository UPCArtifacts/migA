package fr.uphf.se.kotlinresearch.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.utils.Pair;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

//import fr.inria.coming.changeminer.analyzer.commitAnalyzer.HunkDifftAnalyzer;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.git.CommitGit;
import fr.inria.coming.core.engine.git.GITRepositoryInspector;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.HunkDiff;
import fr.inria.coming.core.entities.HunkPair;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.core.entities.interfaces.IFilter;
import fr.inria.coming.main.ComingProperties;
import fr.inria.coming.utils.MapList;
import fr.uphf.se.kotlinresearch.arm.analyzers.AddRemoveResult;
import fr.uphf.se.kotlinresearch.arm.analyzers.AddedRemovedAnalyzer;
import fr.uphf.se.kotlinresearch.arm.analyzers.FileCommitNameAnalyzer;
import fr.uphf.se.kotlinresearch.arm.analyzers.HunkDiffAnalyzer;
import fr.uphf.se.kotlinresearch.arm.analyzers.UnifDiffAnalyzer;
import fr.uphf.se.kotlinresearch.core.results.HunkSummarization;
import fr.uphf.se.kotlinresearch.core.results.MigAIntermediateResultStore;
import fr.uphf.se.kotlinresearch.core.results.PropertiesSummary;
import fr.uphf.se.kotlinresearch.diff.analyzers.JavaDiffAnalyzer;
import fr.uphf.se.kotlinresearch.diff.analyzers.KotlinDiffAnalyzer;
import fr.uphf.se.kotlinresearch.diff.analyzers.granularitylevel.JavaAbstractDiffSplitterAnalyzer;
import fr.uphf.se.kotlinresearch.diff.analyzers.granularitylevel.JavaStatementDiffSplitterAnalyzer;
import fr.uphf.se.kotlinresearch.diff.analyzers.granularitylevel.KotlinAbstractDiffSplitterAnalyzer;
import fr.uphf.se.kotlinresearch.diff.analyzers.granularitylevel.KotlinStatementDiffSplitterAnalyzer;
import fr.uphf.se.kotlinresearch.diff.analyzers.granularitylevel.noSplitter.JavaNoSplitterAnalyzer;
import fr.uphf.se.kotlinresearch.diff.analyzers.granularitylevel.noSplitter.KotlinNoSplitterAnalyzer;
import fr.uphf.se.kotlinresearch.features.analyzers.FeatureFromDiffResult;
import fr.uphf.se.kotlinresearch.features.analyzers.KotlinFeatureFromKastreeDiffAnalyzer;
import fr.uphf.se.kotlinresearch.patterndiscov.core.MigAJSONSerializer;
import fr.uphf.se.kotlinresearch.squarediff.entities.diff.QueryDiff;
import fr.uphf.se.kotlinresearch.squarediff.entities.diff.SingleDiff;
import fr.uphf.se.kotlinresearch.tree.analyzers.JavaTreeAnalyzer;
import fr.uphf.se.kotlinresearch.tree.analyzers.KastreeTreeAnalyzer;
import fr.uphf.se.kotlinresearch.tree.analyzers.kastreeITree.KastreeToITree;
import io.gitlab.arturbosch.detekt.api.Finding;

/**
 * 
 * @author Matias Martinez
 *
 */
public class MigACore extends GITRepositoryInspector {

	private static final String QUERY_ID = "QUERY_ID";
	public static String PATTERNID = "PT_ID";
	public static String SIM = "SIM";
	public static String TYPE_MT = "TYPE";
	public static String IS_BEST = "IS_BEST";
	public static String PATTERN_SIZE = "size";
	public static String PATTERN_OPS = "ops";

	public static final String USE_ROLE_IN_MATCHING = "useroleinmatching";
	public static final String ATTACH_TO_STRING_TO_TREE = "attachToStringToTree";
	public static final String REMOVE_MOVE_MAPPINGS = "remove_move_mappings";
	public static final String OPTIMIZE_NAVIGATION = "optimize_navigation";
	public static final String ADD_ALWAYS_INDIVIDUAL_PATTERNS = "add_all_individual";
	public static final String NODE_TO_STRING = "toString";
	public static final String LOWER_MATCHING_THR = "lower_matching_thr";
	public static final String SAME_MATCHING_THR = "same_matching_thr";
	public static final String MAX_OPS_THR = "max_ops";
	public static final String MAX_NODES_THR = "max_nodes";
	public static final String MAX_OPS_IN_COMP = "max_ops_comp";
	public static final String LINE_CODE = "line_code";
	public static final String JSON_CODE_INSTANCE = "code_instance";

	// Engine configuration
	protected MigAJSONSerializer serializer = new MigAJSONSerializer();

	public static MapList<String, Long> executionsTime = new MapList<>();

	protected long timeinit = 0;

	public static Counter counterQuery = new Counter();

	public static boolean useRole = false;

	protected List<MigAExecutionMode> executionModes = new ArrayList<>();
	/**
	 * Store of intermediate results ()
	 */
	protected MigAIntermediateResultStore intermediateResultStore = new MigAIntermediateResultStore();

	public MigACore() {

		System.out.println("Starting MigaExecution of project " + ComingProperties.getProperty("projectname"));

		counterQuery.reset();

		useRole = (ComingProperties.getPropertyBoolean(USE_ROLE_IN_MATCHING));

		executionsTime.clear();
		timeinit = (new Date()).getTime();
		ComingProperties.setProperty("storeallpatterns", "false");
		if (!ComingProperties.properties.containsKey("max_nb_commit_analyze")) {
			ComingProperties.setProperty("max_nb_commit_analyze", "2000000");
		}
		ComingProperties.setProperty("reapplydiff", "false");
		ComingProperties.setProperty("extensions_to_consider", ".java" + File.pathSeparator + ".kt");
		ComingProperties.setProperty("save_result_revision_analysis", "false");

		ComingProperties.setProperty("includelabelvalues", "false");

		this.getAnalyzers().add(new CommitDataAnalyzer());
		this.getAnalyzers().add(new FileCommitNameAnalyzer());
		this.getAnalyzers().add(new AddedRemovedAnalyzer());

		if (!ComingProperties.properties.containsKey(REMOVE_MOVE_MAPPINGS)) {
			ComingProperties.setProperty(REMOVE_MOVE_MAPPINGS, "true");
		}
		//
		if (!ComingProperties.properties.containsKey(OPTIMIZE_NAVIGATION)) {
			ComingProperties.setProperty(OPTIMIZE_NAVIGATION, "true");
		}

		if (!ComingProperties.properties.containsKey(ADD_ALWAYS_INDIVIDUAL_PATTERNS)) {
			ComingProperties.setProperty(ADD_ALWAYS_INDIVIDUAL_PATTERNS, "false");
		}
		//
		this.getAnalyzers().add(new FileCommitNameAnalyzer());
		this.getAnalyzers().add(new AddedRemovedAnalyzer());

		// this.getAnalyzers().add(new MigrationExporterAnalyzer());

		if (ComingProperties.getPropertyBoolean("outputunifieddiff")) {
			this.getAnalyzers().add(new UnifDiffAnalyzer());
		}

		// AST parsers
		this.getAnalyzers().add(new JavaTreeAnalyzer());
		this.getAnalyzers().add(new KastreeTreeAnalyzer());

		// AST Diff algorithm
		this.getAnalyzers().add(new JavaDiffAnalyzer());
		this.getAnalyzers().add(new KotlinDiffAnalyzer());

		// Change Splitters
		KotlinNoSplitterAnalyzer kotlinNoSplit = new KotlinNoSplitterAnalyzer();
		this.getAnalyzers().add(kotlinNoSplit);

		// KotlinMethodDiffSplitterAnalyzer kotlinMethod = new
		// KotlinMethodDiffSplitterAnalyzer();
		// this.getAnalyzers().add(kotlinMethod);

		// KotlinBlockDiffSplitterAnalyzer kotlinBlock = new
		// KotlinBlockDiffSplitterAnalyzer();
		// this.getAnalyzers().add(kotlinBlock);

		KotlinStatementDiffSplitterAnalyzer kotlinStmt = new KotlinStatementDiffSplitterAnalyzer();
		this.getAnalyzers().add(kotlinStmt);

		JavaNoSplitterAnalyzer javanosplit = new JavaNoSplitterAnalyzer();
		this.getAnalyzers().add(javanosplit);

		// JavaMethodDiffSplitterAnalyzer javaMethodSplit = new
		// JavaMethodDiffSplitterAnalyzer();
		// this.getAnalyzers().add(javaMethodSplit);

		// JavaBlockDiffSplitterAnalyzer javaBlockSplit = new
		// JavaBlockDiffSplitterAnalyzer();
		// this.getAnalyzers().add(javaBlockSplit);

		JavaStatementDiffSplitterAnalyzer javaStmtSplit = new JavaStatementDiffSplitterAnalyzer();
		this.getAnalyzers().add(javaStmtSplit);

		//
		this.getAnalyzers().add(new HunkDiffAnalyzer());
		// this.getAnalyzers().add(new HunkLengthDifftAnalyzer());

		// Not sure if we want to obtain the features of a file
		// inspector.getAnalyzers().add(new KotlinFeatureAnalyzer());
		this.getAnalyzers().add(new KotlinFeatureFromKastreeDiffAnalyzer());

		if (!ComingProperties.properties.containsKey(LOWER_MATCHING_THR)) {
			ComingProperties.setProperty(LOWER_MATCHING_THR, "0.8");
		}
		double lower_tr = ComingProperties.getPropertyDouble(LOWER_MATCHING_THR);

		if (!ComingProperties.properties.containsKey(SAME_MATCHING_THR)) {
			ComingProperties.setProperty(SAME_MATCHING_THR, "0.9");
		}
		double same_tr = ComingProperties.getPropertyDouble(SAME_MATCHING_THR);

		if (!ComingProperties.properties.containsKey(MAX_OPS_THR)) {
			ComingProperties.setProperty(MAX_OPS_THR, "30");
		}
		int maxops = ComingProperties.getPropertyInteger(MAX_OPS_THR);

		if (!ComingProperties.properties.containsKey(MAX_NODES_THR)) {
			ComingProperties.setProperty(MAX_NODES_THR, "500");
		}
		int maxnodes = ComingProperties.getPropertyInteger(MAX_NODES_THR);

		//
		if (!ComingProperties.properties.containsKey(MAX_OPS_IN_COMP)) {
			ComingProperties.setProperty(MAX_OPS_IN_COMP, "30");
		}

		if (!ComingProperties.properties.containsKey(ATTACH_TO_STRING_TO_TREE)) {
			ComingProperties.setProperty(ATTACH_TO_STRING_TO_TREE, "true");
		}

		this.setFilters(new ArrayList<IFilter>());
		this.getFilters().add(new IFilter<CommitGit>() {

			@Override
			public boolean accept(CommitGit c) {
				if (c.getFullMessage().startsWith("Merge") || c.getShortMessage().startsWith("Merge")) {
					log.info("Ignoring Merge commit " + c.getName());
					return false;
				}
				return true;

			}
		});
		log.info("Properties:");
		for (Object key : ComingProperties.properties.keySet()) {
			log.info(key + ": " + ComingProperties.properties.getProperty(key.toString()));
		}

	}

	Map<Action, List<Finding>> allfindingsByActions = new HashMap<Action, List<Finding>>();

	@Override
	public void processEndRevision(Commit commit, RevisionResult resultAllAnalyzed) {

		super.processEndRevision(commit, resultAllAnalyzed);

		if (resultAllAnalyzed.isEmpty()) {
			// return;
		}

		AnalysisResult<Pair<String, String>> message = (AnalysisResult<Pair<String, String>>) resultAllAnalyzed
				.getResultFromClass(CommitDataAnalyzer.class);

		intermediateResultStore.messages.put(commit.getName(), message.getAnalyzed());
		intermediateResultStore.orderCommits.add(commit.getName());

		AddRemoveResult armresult = (AddRemoveResult) resultAllAnalyzed.getResultFromClass(AddedRemovedAnalyzer.class);

		intermediateResultStore.armresults.put(commit.getName(), armresult);

		DiffResult<IRevision, HunkDiff> result = (DiffResult<IRevision, HunkDiff>) resultAllAnalyzed
				.getResultFromClass(HunkDiffAnalyzer.class);

		Map<String, HunkDiff> ss = result.getDiffOfFiles();

		HunkSummarization map = new HunkSummarization();
		for (String key : ss.keySet()) {
			HunkDiff hd = ss.get(key);

			for (HunkPair hp : hd.getHunkpairs()) {
				//
				map.add(key, new Pair<Integer, Integer>(countLines(hp.getLeft()), countLines(hp.getRight())));
			}

		}
		intermediateResultStore.lines.put(commit.getName(), map);

		//// FEATURES

//		FeatureResult resultff = (FeatureResult) resultAllAnalyzed
//				.getResultFromClass(KotlinFeatureFromKastreeDiffAnalyzer.class);
//
//		Map<String, List<Finding>> resultFeat = (Map<String, List<Finding>>) resultff.getFindings();
//		PropertiesSummary featureTrans = new PropertiesSummary();
//
//		for (String key : resultFeat.keySet()) {
//			List<Finding> ff = resultFeat.get(key);
//			for (Finding finding : ff) {
//				if (featureTrans.get(key) == null || !featureTrans.get(key).contains(finding.getName()))
//					featureTrans.add(key, finding.getId() + "-" + finding.getName());
//			}
//		}

		allfindingsByActions.clear();

		FeatureFromDiffResult resultff = (FeatureFromDiffResult) resultAllAnalyzed
				.getResultFromClass(KotlinFeatureFromKastreeDiffAnalyzer.class);

		Map<String, Map<Action, List<Finding>>> resultFeat = (Map<String, Map<Action, List<Finding>>>) resultff
				.getFindings();
		PropertiesSummary featureTrans = new PropertiesSummary();

		for (String fileKey : resultFeat.keySet()) {

			Map<Action, List<Finding>> findingsByActions = resultFeat.get(fileKey);

			for (Action action : findingsByActions.keySet()) {

				String actionString = action.getName();
				List<Finding> ff = findingsByActions.get(action);
				for (Finding finding : ff) {
					if (featureTrans.get(fileKey) == null || !featureTrans.get(fileKey).contains(finding.getName()))
						featureTrans.add(fileKey, actionString + "-" + finding.getId() + "-" + finding.getName());
				}
				allfindingsByActions.put(action, ff);
			}
		}

		intermediateResultStore.features.put(commit.getName(), featureTrans);
		///

		// DIFF Java
		PropertiesSummary javaChanges = getChanges(resultAllAnalyzed, JavaDiffAnalyzer.class, false);
		intermediateResultStore.javaChanges.put(commit.getName(), javaChanges);
		PropertiesSummary kotlinChanges = getChanges(resultAllAnalyzed, KotlinDiffAnalyzer.class, true);
		intermediateResultStore.kotlinChanges.put(commit.getName(), kotlinChanges);

		// Split:
		JsonObject allSplit = new JsonObject();

		JsonArray fileJavaSplited = getChangesSplitted(resultAllAnalyzed, JavaNoSplitterAnalyzer.class, false);
		allSplit.add("JavaFile", fileJavaSplited);

		JsonArray stmtJavaSplited = getChangesSplitted(resultAllAnalyzed, JavaStatementDiffSplitterAnalyzer.class,
				false);
		allSplit.add("JavaStmt", stmtJavaSplited);

		/////
		JsonArray fileKotlinSplited = getChangesSplitted(resultAllAnalyzed, KotlinNoSplitterAnalyzer.class, true);
		allSplit.add("KotlinFile", fileKotlinSplited);

		JsonArray stmtKotlinSplited = getChangesSplitted(resultAllAnalyzed, KotlinStatementDiffSplitterAnalyzer.class,
				true);
		allSplit.add("KotlinStmt", stmtKotlinSplited);

		intermediateResultStore.jsonChangeSplit.put(commit.getName(), allSplit);

		// Save all files
		Set<String> allFiles = new java.util.HashSet<>();
		allFiles.addAll(javaChanges.keySet());
		allFiles.addAll(kotlinChanges.keySet());
		allFiles.addAll(featureTrans.keySet());
		allFiles.addAll(map.keySet());

		intermediateResultStore.filesOfCommits.put(commit.getName(), new ArrayList<>(allFiles));

	}

	public JsonArray getChangesSplitted(RevisionResult resultAllAnalyzed, Class class1, boolean kotlin) {

		DiffResult javadiffr = (DiffResult) resultAllAnalyzed.getResultFromClass(class1);
		Map<String, List<QueryDiff>> diffOfFilesJava = javadiffr.getDiffOfFiles();

		JsonArray all = new JsonArray();

		for (String key : diffOfFilesJava.keySet()) {
			List<QueryDiff> diffs = diffOfFilesJava.get(key);

			if (diffs.size() > 0) {
				JsonObject jsonFile = new JsonObject();
				jsonFile.addProperty("file", key);
				JsonArray diffArray = new JsonArray();
				jsonFile.add("diffs", diffArray);
				boolean add = false;
				//
				for (QueryDiff diff : diffs) {

					JsonObject jsonDiff = new JsonObject();
					diffArray.add(jsonDiff);
					Object sp = diff.metadata.get("split");
					jsonDiff.addProperty("name", (sp != null) ? sp.toString() : "");

					JsonArray changesArray = new JsonArray();
					jsonDiff.add("changes", changesArray);

					for (Action act : diff.getRootOperations()) {
						add = true;
						if (kotlin)
							changesArray.add(getActionKotlinString(diff, act));
						else
							changesArray.add(getActionJavaString(diff, act));
					}
				}
				if (add) {
					all.add(jsonFile);
				}

			}
		}
		return all;
	}

	public PropertiesSummary getChanges(RevisionResult resultAllAnalyzed, Class class1, boolean koltin) {
		DiffResult javadiffr = (DiffResult) resultAllAnalyzed.getResultFromClass(class1);
		Map<String, SingleDiff> diffOfFilesJava = javadiffr.getDiffOfFiles();

		PropertiesSummary changesRootJava = new PropertiesSummary();

		for (String ff : diffOfFilesJava.keySet()) {

			SingleDiff diff = diffOfFilesJava.get(ff);
			for (Action act : diff.getRootOperations()) {
				if (koltin)
					changesRootJava.add(ff, getActionKotlinString(diff, act));
				else
					changesRootJava.add(ff, getActionJavaString(diff, act));
			}

		}
		return changesRootJava;
	}

	public String getActionKotlinString(SingleDiff diff, Action action) {

		String nodeInfo = KotlinAbstractDiffSplitterAnalyzer.getNameOfNodeKotlin(diff, action.getNode());
		String result = action.getName() + "-" + nodeInfo;

		if (ComingProperties.getPropertyBoolean("includelabelvalues")) {

			String label = "";
			if (!action.getNode().getLabel().trim().isEmpty())
				label = action.getNode().getLabel();
			else {
				Object content = action.getNode().getMetadata(KastreeToITree.CONTENT);
				if (content != null) {
					label = content.toString();
				}
			}
			// End:
			result = result + "-" + label;
			if (action instanceof Update) {
				Update up = (Update) action;
				result += "-" + up.getValue();
			}

		}
		//

		// New: adding features
		String findings = "";
		List<Finding> findingsAction = this.allfindingsByActions.get(action);

		if (findingsAction != null) {

			findingsAction.sort((e1, e2) -> e1.getName().compareTo(e2.getName()));

			for (Finding finding : findingsAction) {
				findings += (finding.getId() + "+");
			}

			if (findings.length() > 0) {

				findings = findings.substring(0, findings.length() - 1);
				result += "-@@" + findings;
			}

		}

		return result;

	}

	public String getActionJavaString(SingleDiff diff, Action action) {

		String nodeInfo = JavaAbstractDiffSplitterAnalyzer.getNameOfJavaNode(diff, action.getNode());
		String result = action.getName() + "-" + nodeInfo;

		if (ComingProperties.getPropertyBoolean("includelabelvalues")) {

			String label = "";
			if (!action.getNode().getLabel().trim().isEmpty())
				label = action.getNode().getLabel();

			result = result + "-" + label;
			if (action instanceof Update) {
				Update up = (Update) action;
				result += "-" + up.getValue();
			}
		}
		return result;

	}

	private static int countLines(String str) {
		String[] lines = str.split("\r\n|\r|\n");
		return lines.length;
	}

	@Override
	public FinalResult processEnd() {
		FinalResult finalResult = super.processEnd();

		String projectName = ComingProperties.getProperty("projectname");
		File outDir = new File(ComingProperties.getProperty("output") + File.separator + projectName);
		if (!outDir.exists()) {
			outDir.mkdirs();
		}

		showExecutionTime();

		long executionTimeSeconds = ((new Date()).getTime() - this.timeinit) / 1000;

		processAllResults(projectName, outDir, executionTimeSeconds);

		System.out.println("Total execution time (sec): " + executionTimeSeconds);
		System.out.println("END-Finish running comming");

		for (MigAExecutionMode executionMode : executionModes) {

			serializer.storeJSon(outDir,
					"codeInstances_" + executionMode.getLanguage() + "_" + executionMode.getGranularity(),
					executionMode.codeInstances);
		}
		return finalResult;

	}

	private void showExecutionTime() {
		int total = 0;
		for (String analyzer : this.executionsTime.keySet()) {

			List<Long> values = this.executionsTime.get(analyzer);
			long v = 0;
			for (Long long1 : values) {
				v += long1;
			}
			total += v;
			v /= (long) (values.size());
			Collections.sort(values);
			log.debug("Analyzer: " + analyzer + " avg time (sec): " + v // + ": " + values
			);
		}
		System.out.println("total time -sum- (sec) " + total / 1000);
	}

	public void processAllResults(String projectName, File outDir, long executionTimeSeconds) {

		serializer.saveAll(projectName, outDir, executionTimeSeconds, this.intermediateResultStore,
				this.executionModes);

	}

}
