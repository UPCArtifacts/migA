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
import fr.uphf.se.kotlinresearch.diff.analyzers.SingleDiff;
import fr.uphf.se.kotlinresearch.diff.analyzers.granularitylevel.JavaAbstractDiffSplitterAnalyzer;
import fr.uphf.se.kotlinresearch.diff.analyzers.granularitylevel.KotlinAbstractDiffSplitterAnalyzer;
import fr.uphf.se.kotlinresearch.output.MigAJSONSerializer;
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

	public static final String ANALYZE_HUNKS = "analyze_hunks";
	public static final String COMMITS_TO_IGNORE = "commitstoignore";
	public static String PATTERNID = "PT_ID";
	public static String SIM = "SIM";
	public static String TYPE_MT = "TYPE";
	public static String IS_BEST = "IS_BEST";
	public static String PATTERN_SIZE = "size";
	public static String PATTERN_OPS = "ops";

	public static final String ATTACH_TO_STRING_TO_TREE = "attachToStringToTree";
	public static final String OPTIMIZE_NAVIGATION = "optimize_navigation";
	public static final String NODE_TO_STRING = "toString";
	public static final String MAX_OPS_THR = "max_ops";
	public static final String MAX_NODES_THR = "max_nodes";
	public static final String MAX_OPS_IN_COMP = "max_ops_comp";
	public static final String LINE_CODE = "line_code";
	public static final String JSON_CODE_INSTANCE = "code_instance";
	/**
	 * this option allows to only compute the diff when a commit involves java and
	 * kotlin files
	 */
	public static final String COMPUTES_ONLY_COEVOLUTION = "COMPUTES_ONLY_COEVOLUTION";

	// Engine configuration
	protected MigAJSONSerializer serializer = new MigAJSONSerializer();

	public static MapList<String, Long> executionsTime = new MapList<>();

	protected long timeinit = 0;

	public static Counter counterQuery = new Counter();

	protected MigAIntermediateResultStore intermediateResultStore = new MigAIntermediateResultStore();

	public MigACore() {

		System.out.println("Starting MigaExecution of project " + ComingProperties.getProperty("projectname"));

		counterQuery.reset();

		executionsTime.clear();
		timeinit = (new Date()).getTime();

		if (!ComingProperties.properties.containsKey("max_nb_commit_analyze")) {
			ComingProperties.setProperty("max_nb_commit_analyze", "2000000");
		}

		ComingProperties.setProperty("extensions_to_consider", ".java" + File.pathSeparator + ".kt");
		ComingProperties.setProperty("save_result_revision_analysis", "false");

		// this option allows to only compute the diff when a commit involves java and
		// kotlin files
		ComingProperties.setProperty(COMPUTES_ONLY_COEVOLUTION, "true");

		ComingProperties.setProperty("includelabelvalues", "false");

		if (!ComingProperties.properties.containsKey(OPTIMIZE_NAVIGATION)) {
			ComingProperties.setProperty(OPTIMIZE_NAVIGATION, "true");
		}

		//
		this.getAnalyzers().add(new CommitDataAnalyzer());
		this.getAnalyzers().add(new FileCommitNameAnalyzer());
		this.getAnalyzers().add(new AddedRemovedAnalyzer());


		if (ComingProperties.getPropertyBoolean("outputunifieddiff")) {
			this.getAnalyzers().add(new UnifDiffAnalyzer());
		}

		// AST parsers
		this.getAnalyzers().add(new JavaTreeAnalyzer());
		this.getAnalyzers().add(new KastreeTreeAnalyzer());

		// AST Diff algorithm
		this.getAnalyzers().add(new JavaDiffAnalyzer());
		this.getAnalyzers().add(new KotlinDiffAnalyzer());

		//
		if (ComingProperties.getPropertyBoolean(ANALYZE_HUNKS)) {
			this.getAnalyzers().add(new HunkDiffAnalyzer());
		}
		// Filters of commits:

		final List<String> commitsToIgnoreAll = new ArrayList<>();
		String commitsToIgnore = ComingProperties.getProperty(COMMITS_TO_IGNORE);
		if (commitsToIgnore != null) {
			for (String c : commitsToIgnore.split("_")) {
				commitsToIgnoreAll.add(c);
			}
		}
		System.out.println("Commits to ignore: " + commitsToIgnoreAll);

		this.setFilters(new ArrayList<IFilter>());
		this.getFilters().add(new IFilter<CommitGit>() {

			@Override
			public boolean accept(CommitGit c) {
				if (c.getFullMessage().startsWith("Merge") || c.getShortMessage().startsWith("Merge")) {
					log.info("Ignoring Merge commit " + c.getName());
					return false;
				}

				if (commitsToIgnoreAll.contains(c.getName())) {
					log.info("Ignoring already analyzed commit:  " + c.getName());
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

		AnalysisResult<Map<String, Object>> message = (AnalysisResult<Map<String, Object>>) resultAllAnalyzed
				.getResultFromClass(CommitDataAnalyzer.class);

		intermediateResultStore.commitMetadata.put(commit.getName(), message.getAnalyzed());
		intermediateResultStore.orderCommits.add(commit.getName());

		AddRemoveResult armresult = (AddRemoveResult) resultAllAnalyzed.getResultFromClass(AddedRemovedAnalyzer.class);

		intermediateResultStore.armresults.put(commit.getName(), armresult);

		DiffResult<IRevision, HunkDiff> result = (DiffResult<IRevision, HunkDiff>) resultAllAnalyzed
				.getResultFromClass(HunkDiffAnalyzer.class);

		Set<String> allFiles = new java.util.HashSet<>();
		if (ComingProperties.getPropertyBoolean(ANALYZE_HUNKS)) {
			Map<String, HunkDiff> ss = result.getDiffOfFiles();

			HunkSummarization mapHunks = new HunkSummarization();
			for (String key : ss.keySet()) {
				HunkDiff hd = ss.get(key);

				for (HunkPair hp : hd.getHunkpairs()) {
					//
					mapHunks.add(key, new Pair<Integer, Integer>(countLines(hp.getLeft()), countLines(hp.getRight())));
				}

			}
			intermediateResultStore.lines.put(commit.getName(), mapHunks);
			allFiles.addAll(mapHunks.keySet());
		}
		allfindingsByActions.clear();

		// DIFF Java
		PropertiesSummary javaChanges = getChanges(resultAllAnalyzed, JavaDiffAnalyzer.class, false);
		intermediateResultStore.javaChanges.put(commit.getName(), javaChanges);
		PropertiesSummary kotlinChanges = getChanges(resultAllAnalyzed, KotlinDiffAnalyzer.class, true);
		intermediateResultStore.kotlinChanges.put(commit.getName(), kotlinChanges);

		// Save all files

		allFiles.addAll(javaChanges.keySet());
		allFiles.addAll(kotlinChanges.keySet());

		intermediateResultStore.filesOfCommits.put(commit.getName(), new ArrayList<>(allFiles));

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
		String branchName = ComingProperties.getProperty("branch");
		File outDir = new File(ComingProperties.getProperty("output") + File.separator + projectName + File.separator
				+ branchName.replace("/", "-"));
		if (!outDir.exists()) {
			outDir.mkdirs();
		}

		showExecutionTime();

		long executionTimeSeconds = ((new Date()).getTime() - this.timeinit) / 1000;

		processAllResults(projectName, outDir, executionTimeSeconds);

		System.out.println("Total execution time (sec): " + executionTimeSeconds);
		System.out.println("END-Finish running comming");

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

		serializer.saveAll(projectName, outDir, executionTimeSeconds, this.intermediateResultStore);

	}

	public List<String> getCommitAnalyzed() {
		return new ArrayList<>(this.intermediateResultStore.commitMetadata.keySet());
	}

}
