package fr.uphf.se.kotlinresearch.tests.evolution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.github.gumtreediff.actions.model.Action;

import fr.inria.coming.changeminer.analyzer.commitAnalyzer.HunkDifftAnalyzer;
import fr.inria.coming.changeminer.entity.CommitFinalResult;
import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.engine.callback.IntermediateResultProcessorCallback;
import fr.inria.coming.core.engine.git.GITRepositoryInspector;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.HunkDiff;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.core.entities.interfaces.IFilter;
import fr.inria.coming.main.ComingMain;
import fr.inria.coming.main.ComingProperties;
import fr.inria.coming.utils.MapCounter;
import fr.uphf.ast.ASTNode;
import fr.uphf.se.kotlinresearch.arm.analyzers.AddRemoveResult;
import fr.uphf.se.kotlinresearch.arm.analyzers.AddedRemovedAnalyzer;
import fr.uphf.se.kotlinresearch.arm.analyzers.FileCommitNameAnalyzer;
import fr.uphf.se.kotlinresearch.core.MigACore;
import fr.uphf.se.kotlinresearch.core.NoAnalysis;
import fr.uphf.se.kotlinresearch.diff.analyzers.JavaDiffAnalyzer;
import fr.uphf.se.kotlinresearch.diff.analyzers.KotlinDiffAnalyzer;
import fr.uphf.se.kotlinresearch.diff.analyzers.SingleDiff;
import fr.uphf.se.kotlinresearch.features.analyzers.FeatureResult;
import fr.uphf.se.kotlinresearch.features.analyzers.KotlinFeatureAnalyzer;
import fr.uphf.se.kotlinresearch.features.analyzers.KotlinFeatureFromKastreeDiffAnalyzer;
import fr.uphf.se.kotlinresearch.tree.analyzers.JavaTreeAnalyzer;
import fr.uphf.se.kotlinresearch.tree.analyzers.KastreeTreeAnalyzer;
import fr.uphf.se.kotlinresearch.tree.analyzers.TreeResult;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import io.gitlab.arturbosch.detekt.api.Feature;

/**
 * 
 * @author Matias Martinez
 *
 */
public class MigAGitRunnerTest {
	Logger log = Logger.getLogger(JavaDiffAnalyzer.class.getName());
	final int idem = 0;

	@SuppressWarnings({ "unused", "rawtypes" })
	@Test
	public void testMiga_CompleteHistory1() throws Exception {

		MapCounter<String> counterRevWithProcessor = new MapCounter<>();

		String pathToKotlinRepo = "/Users/matias/develop/kotlinresearch/dataset_kotlin_migration/poet-assistant";// ComingProperties.getProperty("testrepolocation");
		File locationKotinRepo = new File(pathToKotlinRepo);
		assertTrue(locationKotinRepo.exists());
		ComingMain cm = new ComingMain();
		String[] args = new String[] { "-location", locationKotinRepo.getAbsolutePath(),
				// Here our new analyzer
				"-input", MigACore.class.getCanonicalName(),
				//
				"-mode", "nullmode",
				//
				"-parameters",
				"projectname:poet-assistant:save_result_revision_analysis:false:optimize_navigation:true" };

		System.out.println(Arrays.toString(args));
		cm.createEngine(args);
		// FOR test
		ComingProperties.setProperty("max_nb_commit_analyze", "50");

		FinalResult finalResult = cm.start();

		System.out.println("---End----");
		System.out.println("Processor with commit:\n" + counterRevWithProcessor.sorted());
	}

	@Test
	public void testMiga_full_poet_assistant_500000() throws Exception {

		String projectName = "poet-assistant";// "NyaaPantsu-android-app";
		String branch = "master";

		run(projectName, branch, "50000");

	}

	@Test
	public void testMiga_full_poet_assistant_50() throws Exception {

		String projectName = "poet-assistant";// "NyaaPantsu-android-app";
		String branch = "master";

		run(projectName, branch, "500");

	}

	@Test
	public void testMiga_full_Tusky_500000() throws Exception {

		String projectName = "openlauncher";// "FRCAndroidWidget";// "NyaaPantsu-android-app";
		String branch = "master";

		run(projectName, branch, "50000");

	}

	// Simple-Calendar
	@Test
	public void testMiga_full_SimpleCalendar_500000() throws Exception {

		String projectName = "AnkiEditor";// "FRCAndroidWidget";// "NyaaPantsu-android-app";
		String branch = "master";

		run(projectName, branch, "50000");
//runAndFilter(projectName, branch, "a93968e59b9082a744cbd975258ac552abf1a022");

	}

	@Test
	public void testMiga_full_AnkiEditorsingle1() throws Exception {

		String projectName = "AnkiEditor";// "FRCAndroidWidget";// "NyaaPantsu-android-app";
		String branch = "master";

		// run(projectName, branch, "50000");
		runAndFilter(projectName, branch,
				"928f8c0bffdfe3f06d9c1d81e84ff72d5f6b8693"/* "2ea5a9752dbd54ef48780476ff9e4e714e2c67c7" */);

	}

	@Test
	public void testMiga_full_Commit() throws Exception {

		String projectName = "Rocket.Chat.Android";// "NyaaPantsu-android-app";
		String branch = "develop";

		runAndFilter(projectName, branch, "5509fb5b16fea584f9dbdd985834b9ec9efcf922");

	}

	@Test
	public void testMiga_full_poet_assistant_commitc839f27075b9fa5670bf3e5442a82fa3cac711fd0() throws Exception {

		String projectName = "poet-assistant";
		String branch = "master";
		/// To analyze the diff
		runAndFilter(projectName, branch, "839f27075b9fa5670bf3e5442a82fa3cac711fd0");

	}

	@Test
	public void testMiga_full_poet_assistant_commit7afac56d447fff3413a950efebb75e250b8d439b() throws Exception {

		String projectName = "poet-assistant";// "NyaaPantsu-android-app";
		String branch = "master";

		runAndDebug(projectName, branch, 100, "7afac56d447fff3413a950efebb75e250b8d439b");

	}

	@Test
	public void testMiga_full_poet_assistant_commitc85ae2c67147b818a34e8757dba1f217558880770_typeAccess_complete()
			throws Exception {

		String projectName = "poet-assistant";// "NyaaPantsu-android-app";
		String branch = "master";

		runAndFilter(projectName, branch, "85ae2c67147b818a34e8757dba1f217558880770");

	}

	@Test
	public void testMiga_full_poet_assistant_commit84eb40bde716f6c217ee01ac1982a3b9d2b152ef() throws Exception {

		String projectName = "poet-assistant";// "NyaaPantsu-android-app";
		String branch = "master";
		// last commit from pull july in my machine
		// "84eb40bde716f6c217ee01ac1982a3b9d2b152ef"
		runAndAfter(projectName, branch, "84eb40bde716f6c217ee01ac1982a3b9d2b152ef");

	}

	@Test
	public void testMiga_full_vector_android() throws Exception {

		String projectName = "mini-vector-android";// "NyaaPantsu-android-app";
		String branch = "limified";
		ComingProperties.setProperty("max_nb_commit_analyze", "50");

		run(projectName, branch);

	}

	@Test
	public void testMiga_NPEanalyzer() throws Exception {

		String projectName = "mini-vector-android";// "NyaaPantsu-android-app";
		String branch = "limified";
		run(projectName, branch);

	}

	@Test
	public void testMiga_NPEanalyzerbuggyRevision() throws Exception {

		// MapCounter<String> counterRevWithProcessor = new MapCounter<>();
		String projectName = "mini-vector-android";// "NyaaPantsu-android-app";
		String branch = "limified";
		runAndFilter(projectName, branch, "574569c7a9694bdefcc43cb82b84046786a9b4b0");
		// System.out.println("Processor with commit:\n" +
		// counterRevWithProcessor.sorted());
	}

	@Test
	public void testMiga_NPEanalyzer2() throws Exception {

		// MapCounter<String> counterRevWithProcessor = new MapCounter<>();
		String projectName = "NyaaPantsu-android-app";
		String branch = "master";
		run(projectName, branch);
		// System.out.println("Processor with commit:\n" +
		// counterRevWithProcessor.sorted());
	}

	public void run(String projectName, String branch) {
		this.run(projectName, branch, "50");

	}

	public void run(String projectName, String branch, String maxCommits) {
		String pathToKotlinRepo = "/Users/matias/develop/kotlinresearch/dataset_kotlin_migration/" + projectName;
		File locationKotinRepo = new File(pathToKotlinRepo);
		assertTrue(locationKotinRepo.exists());
		ComingMain cm = new ComingMain();

		cm.createEngine(new String[] { "-location", locationKotinRepo.getAbsolutePath(),
				// Here our new analyzer
				"-input", MigACore.class.getCanonicalName(),
				//
				"-mode", "nullmode",
				//
				"-parameters", "projectname:" + projectName + ":save_result_revision_analysis:true"
				// For mini-vector
						+ ":branch:" + branch + ":outputunifieddiff:true" + ":ignore_patterns:true"
						// + ":astmatcher:changedistiller"
						+ ":lower_matching_thr:0.8:same_matching_thr:0.8" });// useroleinmatching:true
		ComingProperties.setProperty("max_nb_commit_analyze", maxCommits);
		FinalResult finalResult = cm.start();

		System.out.println("---End----");
	}

	public void runAndFilter(String projectName, String branch, String revision) {
		String pathToKotlinRepo = "/Users/matias/develop/kotlinresearch/dataset_kotlin_migration/" + projectName;

		File locationKotinRepo = new File(pathToKotlinRepo);
		assertTrue(locationKotinRepo.exists());
		ComingMain cm = new ComingMain();

		cm.createEngine(new String[] { "-location", locationKotinRepo.getAbsolutePath(),
				// Here our new analyzer
				"-input", MigACore.class.getCanonicalName(),
				//
				"-mode", "nullmode",
				//
				"-parameters", "projectname:" + projectName + ":save_result_revision_analysis:false"
				// For mini-vector
						+ ":branch:" + branch + ":max_nb_commit_analyze:2000000" + ":outputunifieddiff:true:astmatcher:"
						+ "classicgumtree"// "mtdiff"
				// + "ChangeDistiller"
		});

		cm.getExperiment().getFilters().add(new IFilter<Commit>() {

			@Override
			public boolean accept(Commit c) {

				return c.getName().equals(revision);
			}
		});

		FinalResult finalResult = cm.start();

		System.out.println("---End----");
	}

	public void runAndDebug(String projectName, String branch, int top, String revision) {
		String pathToKotlinRepo = "/Users/matias/develop/kotlinresearch/dataset_kotlin_migration/" + projectName;
		File locationKotinRepo = new File(pathToKotlinRepo);
		assertTrue(locationKotinRepo.exists());
		ComingMain cm = new ComingMain();

		cm.createEngine(new String[] { "-location", locationKotinRepo.getAbsolutePath(),
				// Here our new analyzer
				"-input", MigACore.class.getCanonicalName(),
				//
				"-mode", "nullmode",
				//
				"-parameters", "projectname:" + projectName + ":save_result_revision_analysis:false"
				// For mini-vector
						+ ":branch:" + branch + ":max_nb_commit_analyze:" + top + ":outputunifieddiff:true" });

		cm.getExperiment().getFilters().add(new IFilter<Commit>() {

			@Override
			public boolean accept(Commit c) {

				if (c.getName().equals(revision)) {
					System.out.println("Print");
				}

				return true;
			}
		});

		FinalResult finalResult = cm.start();

		System.out.println("---End----");
	}

	public void runAndAfter(String projectName, String branch, String revision) {
		String pathToKotlinRepo = "/Users/matias/develop/kotlinresearch/dataset_kotlin_migration/" + projectName;
		File locationKotinRepo = new File(pathToKotlinRepo);
		assertTrue(locationKotinRepo.exists());
		ComingMain cm = new ComingMain();

		cm.createEngine(new String[] { "-location", locationKotinRepo.getAbsolutePath(),
				// Here our new analyzer
				"-input", MigACore.class.getCanonicalName(),
				//
				"-mode", "nullmode",
				//
				"-parameters", "projectname:" + projectName + ":save_result_revision_analysis:false"
				// For mini-vector
						+ ":branch:" + branch + ":max_nb_commit_analyze:2000000" + ":outputunifieddiff:true" });

		cm.getExperiment().getFilters().add(new IFilter<Commit>() {
			boolean start = false;

			@Override
			public boolean accept(Commit c) {

				if (c.getName().equals(revision))
					start = true;

				return start;
			}
		});

		FinalResult finalResult = cm.start();

		System.out.println("---End----");
	}

	@Test
	public void testMiga_OutOfMemoryHunk() throws Exception {

		String projectName = "IPFSDroid";
		String branch = "master";
		run(projectName, branch);
	}

	@Test
	public void testMiga_SingleMigrationCommit() throws Exception {

		MapCounter<String> counterRevWithProcessor = new MapCounter<>();

		String pathToKotlinRepo = "/Users/matias/develop/kotlinresearch/dataset_kotlin_migration/poet-assistant";// ComingProperties.getProperty("testrepolocation");
		File locationKotinRepo = new File(pathToKotlinRepo);
		assertTrue(locationKotinRepo.exists());
		ComingMain cm = new ComingMain();
		cm.createEngine(new String[] { "-location", locationKotinRepo.getAbsolutePath(),
				// Here our new analyzer
				"-input", MigACore.class.getCanonicalName(),
				//
				"-mode", "nullmode",
				//
				"-parameters", "projectname:poet-assistant:save_result_revision_analysis:false" });
		// FOR test
		ComingProperties.setProperty("max_nb_commit_analyze", "200");

		cm.getExperiment().getFilters().add(new IFilter<Commit>() {

			@Override
			public boolean accept(Commit c) {

				return c.getName().equals("d467c1c6edf05c4597a2c093674cc9bb4b7889a0");
			}
		});
		FinalResult finalResult = cm.start();

		System.out.println("---End----");
		System.out.println("Processor with commit:\n" + counterRevWithProcessor.sorted());
	}

	@Test
	public void testMiga_ComingExc() throws Exception {

		MapCounter<String> counterRevWithProcessor = new MapCounter<>();

		String pathToKotlinRepo = "/Users/matias/develop/kotlinresearch/dataset_kotlin_migration/Rocket.Chat.Android";// ComingProperties.getProperty("testrepolocation");
		File locationKotinRepo = new File(pathToKotlinRepo);
		assertTrue(locationKotinRepo.exists());
		ComingMain cm = new ComingMain();
		cm.createEngine(new String[] { "-location", locationKotinRepo.getAbsolutePath(),
				// Here our new analyzer
				"-input", MigACore.class.getCanonicalName(),
				//
				"-mode", "nullmode",
				//
				"-parameters", "projectname:poet-assistant:save_result_revision_analysis:false:branch:develop" });
		// FOR test
		ComingProperties.setProperty("max_nb_commit_analyze", "20000");

		cm.getExperiment().getFilters().add(new IFilter<Commit>() {

			@Override
			public boolean accept(Commit c) {
				// System.out.println(c.getName());
				return c.getName().equals("b95ff6d63ab7fe5d672d5031f7b01496c4e9189d");
			}
		});
		FinalResult finalResult = cm.start();

		System.out.println("---End----");
		System.out.println("Processor with commit:\n" + counterRevWithProcessor.sorted());
	}

	@Test
	public void testMiga_ParserKotlin() throws Exception {

		MapCounter<String> counterRevWithProcessor = new MapCounter<>();

		String pathToKotlinRepo = "/Users/matias/develop/kotlinresearch/dataset_kotlin_migration/PassAndroid";// ComingProperties.getProperty("testrepolocation");
		File locationKotinRepo = new File(pathToKotlinRepo);
		assertTrue(locationKotinRepo.exists());
		ComingMain cm = new ComingMain();
		cm.createEngine(new String[] { "-location", locationKotinRepo.getAbsolutePath(),
				// Here our new analyzer
				"-input", MigACore.class.getCanonicalName(),
				//
				"-mode", "nullmode",
				//
				"-parameters", "projectname:poet-assistant:save_result_revision_analysis:false" });
		// FOR test
		ComingProperties.setProperty("max_nb_commit_analyze", "20000");

		cm.getExperiment().getFilters().add(new IFilter<Commit>() {

			@Override
			public boolean accept(Commit c) {
				// System.out.println(c.getName());
				return c.getName().equals("b95ff6d63ab7fe5d672d5031f7b01496c4e9189d");
			}
		});
		FinalResult finalResult = cm.start();

		System.out.println("---End----");
		System.out.println("Processor with commit:\n" + counterRevWithProcessor.sorted());
	}

	@Test
	public void testMiga_WrongAddJava() throws Exception {

		MapCounter<String> counterRevWithProcessor = new MapCounter<>();
		String projectName = "Simple-Draw";
		String pathToKotlinRepo = "/Users/matias/develop/kotlinresearch/dataset_kotlin_migration/" + projectName;
		File locationKotinRepo = new File(pathToKotlinRepo);
		assertTrue(locationKotinRepo.exists());
		ComingMain cm = new ComingMain();
		cm.createEngine(new String[] { "-location", locationKotinRepo.getAbsolutePath(),
				// Here our new analyzer
				"-input", MigACore.class.getCanonicalName(),
				//
				"-mode", "nullmode",
				//
				"-parameters", "projectname:" + projectName + ":save_result_revision_analysis:false" });
		// FOR test
		ComingProperties.setProperty("max_nb_commit_analyze", "1200");

		cm.getExperiment().getFilters().add(new IFilter<Commit>() {

			@Override
			public boolean accept(Commit c) {

				return c.getName().equals("ef19eafa1f98ffb501f12e414ba95e968071396f");
			}
		});
		FinalResult finalResult = cm.start();

		System.out.println("---End----");
		System.out.println("Processor with commit:\n" + counterRevWithProcessor.sorted());
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testNamesFileCommits() throws Exception {

		ComingMain cm = new ComingMain();
		/// Users/matias/develop/kotlinresearch/dataset_kotlin_migration/jalkametri-android

		String pathToKotlinRepo = "/Users/matias/develop/kotlinresearch/dataset_kotlin_migration/poet-assistant";// ComingProperties.getProperty("testrepolocation");
		File locationKotinRepo = new File(pathToKotlinRepo);
		assertTrue(locationKotinRepo.exists());

		// cm.run
		boolean created = cm.createEngine(new String[] { "-location", locationKotinRepo.getAbsolutePath(),
				// Here our new analyzer
				"-mode", FileCommitNameAnalyzer.class.getCanonicalName(),
				// "
				"-parameters", "consider_rename:false:file_complete_name:true:save_result_revision_analysis:false" });

		Object finalResult = cm.start();

	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testLoadKotlinAnalyzersCompleteHistory1() throws Exception {

		ComingMain cm = new ComingMain();
		/// Users/matias/develop/kotlinresearch/dataset_kotlin_migration/jalkametri-android

		String pathToKotlinRepo = "/Users/matias/develop/kotlinresearch/dataset_kotlin_migration/poet-assistant";// ComingProperties.getProperty("testrepolocation");
		File locationKotinRepo = new File(pathToKotlinRepo);
		assertTrue(locationKotinRepo.exists());

		// cm.run
		boolean created = cm.createEngine(new String[] { "-location", locationKotinRepo.getAbsolutePath(),
				// Here our new analyzer
				"-mode",
				JavaTreeAnalyzer.class.getName() + File.pathSeparator + KastreeTreeAnalyzer.class.getName()
						+ File.pathSeparator + AddedRemovedAnalyzer.class.getName() + File.pathSeparator
						+ HunkDifftAnalyzer.class.getName() + File.pathSeparator + KotlinDiffAnalyzer.class.getName()
						+ File.pathSeparator + JavaDiffAnalyzer.class.getName(),
				//
				"-parameters", "save_result_revision_analysis:false" });

		cm.registerIntermediateCallback(new IntermediateResultProcessorCallback() {

			@SuppressWarnings("unchecked")
			@Override
			public void handleResult(RevisionResult result) {

				log.info("Results of revision: " + result.getRelatedRevision().getName());

				log.info("Analyzers " + result.keySet());

				// Hunks
				DiffResult<IRevision, HunkDiff> rhunk = (DiffResult<IRevision, HunkDiff>) result
						.getResultFromClass(HunkDifftAnalyzer.class);

				for (String k : rhunk.getDiffOfFiles().keySet()) {

					HunkDiff hks = rhunk.getDiffOfFiles().get(k);

					if (hks.getHunkpairs().size() > 0) {
						System.out.println(k + " hunks " + hks.size());
					}

				}
				// ARM
				AddRemoveResult arresult = (AddRemoveResult) result.getResultFromClass(AddedRemovedAnalyzer.class);
				log.info("\nAR result: " + arresult);

				//
				DiffResult<IRevision, SingleDiff> kotlinDiff = (DiffResult<IRevision, SingleDiff>) result
						.getResultFromClass(KotlinDiffAnalyzer.class);

				if (kotlinDiff.getDiffOfFiles().keySet().size() != arresult.modifKotlin.size()) {
					System.err.println("Different numbers:\n" + kotlinDiff.getDiffOfFiles().keySet() + "\n"
							+ arresult.modifKotlin);

				}

				log.info("\nK diff: " + kotlinDiff.getAll().size());

				DiffResult<IRevision, SingleDiff> javaDiff = (DiffResult<IRevision, SingleDiff>) result
						.getResultFromClass(JavaDiffAnalyzer.class);
				log.info("\nJ diff: " + javaDiff.getAll().size());

			}
		});

		assertTrue(created);

		/// Start the engine:

		Object finalResult = cm.start();

		///

		KotlinDiffAnalyzer cmanalyzer = (KotlinDiffAnalyzer) cm.getExperiment().getAnalyzers().stream()
				.filter(e -> e instanceof KotlinDiffAnalyzer).findFirst().get();

		assertNotNull(cmanalyzer);

		CommitFinalResult commitResult = (CommitFinalResult) finalResult;

		assertTrue(commitResult.getAllResults().values().size() > 0);

		for (Commit iCommit : commitResult.getAllResults().keySet()) {

			RevisionResult resultofCommit = commitResult.getAllResults().get(iCommit);
			// Get the results produced by our analyzer
			AnalysisResult featureResult = resultofCommit.get(KotlinDiffAnalyzer.class.getSimpleName());

			assertTrue(featureResult instanceof DiffResult);
			DiffResult<String, SingleDiff> fresults = (DiffResult) featureResult;
			assertNotNull(fresults);

		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testAnalysisSingleCommit1() throws Exception {

		ComingMain cm = new ComingMain();

		String pathToKotlinRepo = "/Users/matias/develop/kotlinresearch/dataset_kotlin_migration/poet-assistant";// ComingProperties.getProperty("testrepolocation");
		File locationKotinRepo = new File(pathToKotlinRepo);
		assertTrue(locationKotinRepo.exists());

		// cm.run
		boolean created = cm.createEngine(new String[] { "-location", locationKotinRepo.getAbsolutePath(),
				// Here our new analyzer
				"-mode", NoAnalysis.class.getName()// HunkDifftAnalyzer.class.getName()
				//
				// "-parameters", "save_result_revision_analysis:false"
		});

		assertTrue(created);

		Object finalResult = cm.start();

		CommitFinalResult commitResult = (CommitFinalResult) finalResult;

		Commit selectedCommit = commitResult.keySet().stream()
				.filter(e -> e.getName().equals("02b9c605486e233dea54a75d8bcea739916639aa")).findFirst().get();

		System.out.println("Commit c: " + selectedCommit);

		GITRepositoryInspector inspector = new MigACore();

		RevisionResult resultAllAnalyzed = new RevisionResult(selectedCommit);
		for (Analyzer anAnalyzer : inspector.getAnalyzers()) {

			AnalysisResult resultAnalyzer = anAnalyzer.analyze(selectedCommit, resultAllAnalyzed);
			resultAllAnalyzed.put(anAnalyzer.getClass().getSimpleName(), resultAnalyzer);

		}

		// log.debug("D->" + resultAllAnalyzed);

		DiffResult<IRevision, SingleDiff> dk = (DiffResult<IRevision, SingleDiff>) resultAllAnalyzed
				.getResultFromClass(KotlinDiffAnalyzer.class);

		SingleDiff diffWotdAdapter = dk.getDiffOfFiles().get("WotdAdapter.kt");
		assertNotNull(diffWotdAdapter);

		List<Action> ddroots = diffWotdAdapter.getRootOperations();
		assertNotNull(ddroots);
		assertEquals(2, ddroots.size());

		for (Action iaction : ddroots) {
			log.debug("Op " + iaction.getClass().getName());
			log.debug("--> " + diffWotdAdapter.getContext().getTypeLabel(iaction.getNode().getType()));
			ASTNode metadata = (ASTNode) iaction.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			log.debug("ast node: " + metadata);
		}

		FeatureResult featureResult = (FeatureResult) resultAllAnalyzed.getResultFromClass(KotlinFeatureAnalyzer.class);

		log.debug("Feature results\n--> " + featureResult.getFindings());

		for (Object file : featureResult.getFindings().keySet()) {
			List<Feature> featuresFile = (List<Feature>) featureResult.getFindings().get(file);
			log.debug("File " + file + " features "
					+ featuresFile.stream().map(e -> e.getId()).collect(Collectors.toList()));

			for (Feature xfeature : featuresFile) {
				log.debug("---> feature : " + xfeature.getId() + " " + xfeature.getEntity().getName());
			}

		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testMigArSingleCommitWithKotlin1() throws Exception {

		ComingMain cm = new ComingMain();

		String pathToKotlinRepo = "/Users/matias/develop/kotlinresearch/dataset_kotlin_migration/poet-assistant";// ComingProperties.getProperty("testrepolocation");
		File locationKotinRepo = new File(pathToKotlinRepo);
		assertTrue(locationKotinRepo.exists());

		// cm.run
		boolean created = cm.createEngine(new String[] { "-location", locationKotinRepo.getAbsolutePath(),
				// Here our new analyzer
				"-mode", NoAnalysis.class.getName()// HunkDifftAnalyzer.class.getName()
				//
				// "-parameters", "save_result_revision_analysis:false"
		});

		assertTrue(created);

		Object finalResult = cm.start();

		CommitFinalResult commitResult = (CommitFinalResult) finalResult;

		Commit selectedCommit = commitResult.keySet().stream()
				.filter(e -> e.getName().equals("02b9c605486e233dea54a75d8bcea739916639aa")).findFirst().get();

		System.out.println("Commit c: " + selectedCommit);

		GITRepositoryInspector inspector = new MigACore();

		RevisionResult resultAllAnalyzed = new RevisionResult(selectedCommit);
		for (Analyzer anAnalyzer : inspector.getAnalyzers()) {

			AnalysisResult resultAnalyzer = anAnalyzer.analyze(selectedCommit, resultAllAnalyzed);
			resultAllAnalyzed.put(anAnalyzer.getClass().getSimpleName(), resultAnalyzer);

		}

		// KastreeTreeAnalyzer

		TreeResult treesKotlin = (TreeResult) resultAllAnalyzed.getResultFromClass(KastreeTreeAnalyzer.class);

		assertNotNull(treesKotlin);

		assertEquals(6, treesKotlin.getTreeOfFiles().size());

		// JavaTreeAnalyzer

		TreeResult treesJava = (TreeResult) resultAllAnalyzed.getResultFromClass(JavaTreeAnalyzer.class);

		assertNotNull(treesJava);

		assertEquals(0, treesJava.getTreeOfFiles().size());

		/// Kotlin Diff

		DiffResult<IRevision, SingleDiff> dk = (DiffResult<IRevision, SingleDiff>) resultAllAnalyzed
				.getResultFromClass(KotlinDiffAnalyzer.class);

		SingleDiff diffWotdAdapter = dk.getDiffOfFiles().get("WotdAdapter.kt");
		assertNotNull(diffWotdAdapter);

		List<Action> ddroots = diffWotdAdapter.getRootOperations();
		assertNotNull(ddroots);
		assertEquals(2, ddroots.size());

		for (Action iaction : ddroots) {
			log.debug("Op " + iaction.getClass().getName());
			log.debug("--> " + diffWotdAdapter.getContext().getTypeLabel(iaction.getNode().getType()));
			ASTNode metadata = (ASTNode) iaction.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			log.debug("ast node: " + metadata);
		}

		////

		FeatureResult featureResult = (FeatureResult) resultAllAnalyzed
				.getResultFromClass(KotlinFeatureFromKastreeDiffAnalyzer.class);

		log.debug("Feature results\n--> " + featureResult.getFindings());

		for (Object file : featureResult.getFindings().keySet()) {
			List<Feature> featuresFile = (List<Feature>) featureResult.getFindings().get(file);
			log.debug("File " + file + " features "
					+ featuresFile.stream().map(e -> e.getId()).collect(Collectors.toList()));

			for (Feature xfeature : featuresFile) {
				log.debug("---> feature : " + xfeature.getId() + " " + xfeature.getEntity().getName());
			}

		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testMigArSingleCommitWithKotlin2() throws Exception {

		ComingMain cm = new ComingMain();

		String pathToKotlinRepo = "/Users/matias/develop/kotlinresearch/dataset_kotlin_migration/poet-assistant";// ComingProperties.getProperty("testrepolocation");
		File locationKotinRepo = new File(pathToKotlinRepo);
		assertTrue(locationKotinRepo.exists());

		// cm.run
		boolean created = cm.createEngine(new String[] { "-location", locationKotinRepo.getAbsolutePath(),
				// Here our new analyzer
				"-mode", NoAnalysis.class.getName()// HunkDifftAnalyzer.class.getName()
				//
				// "-parameters", "save_result_revision_analysis:false"
		});

		assertTrue(created);

		Object finalResult = cm.start();

		CommitFinalResult commitResult = (CommitFinalResult) finalResult;

		Commit selectedCommit = commitResult.keySet().stream()
				.filter(e -> e.getName().equals("0fa8bade587e799bb84fee6e32fddd417529754a")).findFirst().get();

		System.out.println("Commit c: " + selectedCommit);

		GITRepositoryInspector inspector = new MigACore();

		RevisionResult resultAllAnalyzed = new RevisionResult(selectedCommit);
		for (Analyzer anAnalyzer : inspector.getAnalyzers()) {

			AnalysisResult resultAnalyzer = anAnalyzer.analyze(selectedCommit, resultAllAnalyzed);
			resultAllAnalyzed.put(anAnalyzer.getClass().getSimpleName(), resultAnalyzer);

		}

		// KastreeTreeAnalyzer

		TreeResult treesKotlin = (TreeResult) resultAllAnalyzed.getResultFromClass(KastreeTreeAnalyzer.class);

		assertNotNull(treesKotlin);

		assertEquals(2, treesKotlin.getTreeOfFiles().size());

		// JavaTreeAnalyzer

		TreeResult treesJava = (TreeResult) resultAllAnalyzed.getResultFromClass(JavaTreeAnalyzer.class);

		assertNotNull(treesJava);

		assertEquals(0, treesJava.getTreeOfFiles().size());

		/// Kotlin Diff

		DiffResult<IRevision, SingleDiff> dk = (DiffResult<IRevision, SingleDiff>) resultAllAnalyzed
				.getResultFromClass(KotlinDiffAnalyzer.class);

		SingleDiff diffWotdAdapter = dk.getDiffOfFiles().get("ReaderFragment.kt");
		assertNotNull(diffWotdAdapter);

		List<Action> ddroots = diffWotdAdapter.getRootOperations();
		assertNotNull(ddroots);
		assertEquals(1, ddroots.size());

		for (Action iaction : ddroots) {
			log.debug("Op " + iaction.getClass().getName());
			log.debug("--> " + diffWotdAdapter.getContext().getTypeLabel(iaction.getNode().getType()));
			ASTNode metadata = (ASTNode) iaction.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			log.debug("ast node: " + metadata);
		}

		////

		FeatureResult featureResult = (FeatureResult) resultAllAnalyzed
				.getResultFromClass(KotlinFeatureFromKastreeDiffAnalyzer.class);

		log.debug("Feature results\n--> " + featureResult.getFindings());

		for (Object file : featureResult.getFindings().keySet()) {
			List<Feature> featuresFile = (List<Feature>) featureResult.getFindings().get(file);
			log.debug("File " + file + " features "
					+ featuresFile.stream().map(e -> e.getId()).collect(Collectors.toList()));

			for (Feature xfeature : featuresFile) {
				log.debug("---> feature : " + xfeature.getId() + " " + xfeature.getEntity().getName());
			}

		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testMigArSingleCommitWithKotlinAndJava1() throws Exception {

		ComingMain cm = new ComingMain();

		String pathToKotlinRepo = "/Users/matias/develop/kotlinresearch/dataset_kotlin_migration/poet-assistant";// ComingProperties.getProperty("testrepolocation");
		File locationKotinRepo = new File(pathToKotlinRepo);
		assertTrue(locationKotinRepo.exists());

		// cm.run
		boolean created = cm.createEngine(new String[] { "-location", locationKotinRepo.getAbsolutePath(),
				// Here our new analyzer
				"-mode", NoAnalysis.class.getName()// HunkDifftAnalyzer.class.getName()
				//
				// "-parameters", "save_result_revision_analysis:false"
		});

		assertTrue(created);

		Object finalResult = cm.start();

		CommitFinalResult commitResult = (CommitFinalResult) finalResult;

		Commit selectedCommit = commitResult.keySet().stream()
				.filter(e -> e.getName().equals("f9f17e905307c358736381b89ee5596e87090de4")).findFirst().get();

		System.out.println("Commit c: " + selectedCommit);

		GITRepositoryInspector inspector = new MigACore();

		RevisionResult resultAllAnalyzed = new RevisionResult(selectedCommit);
		for (Analyzer anAnalyzer : inspector.getAnalyzers()) {

			AnalysisResult resultAnalyzer = anAnalyzer.analyze(selectedCommit, resultAllAnalyzed);
			resultAllAnalyzed.put(anAnalyzer.getClass().getSimpleName(), resultAnalyzer);

		}

		// KastreeTreeAnalyzer

		TreeResult treesKotlin = (TreeResult) resultAllAnalyzed.getResultFromClass(KastreeTreeAnalyzer.class);

		assertNotNull(treesKotlin);

		assertEquals("size: " + treesKotlin.getTreeOfFiles().keySet(), 6, treesKotlin.getTreeOfFiles().keySet().size());

		// JavaTreeAnalyzer

		TreeResult treesJava = (TreeResult) resultAllAnalyzed.getResultFromClass(JavaTreeAnalyzer.class);

		assertNotNull(treesJava);

		assertEquals(1, treesJava.getTreeOfFiles().size());

		/// Kotlin Diff

		DiffResult<IRevision, SingleDiff> dk = (DiffResult<IRevision, SingleDiff>) resultAllAnalyzed
				.getResultFromClass(KotlinDiffAnalyzer.class);

		SingleDiff diffWotdAdapter = dk.getDiffOfFiles().get("ReaderFragment.kt");
		assertNotNull(diffWotdAdapter);

		List<Action> ddroots = diffWotdAdapter.getRootOperations();
		assertNotNull(ddroots);
		// assertEquals(1, ddroots.size());

		for (Action iaction : ddroots) {
			log.debug("Op " + iaction.getClass().getName());
			log.debug("--> " + diffWotdAdapter.getContext().getTypeLabel(iaction.getNode().getType()));
			ASTNode metadata = (ASTNode) iaction.getNode().getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
			log.debug("ast node: " + metadata);
		}

		////

		FeatureResult featureResult = (FeatureResult) resultAllAnalyzed
				.getResultFromClass(KotlinFeatureFromKastreeDiffAnalyzer.class);

		log.debug("Feature results\n--> " + featureResult.getFindings());

		for (Object file : featureResult.getFindings().keySet()) {
			List<Feature> featuresFile = (List<Feature>) featureResult.getFindings().get(file);
			log.debug("File " + file + " features "
					+ featuresFile.stream().map(e -> e.getId()).collect(Collectors.toList()));

			for (Feature xfeature : featuresFile) {
				log.debug("---> feature : " + xfeature.getId() + " " + xfeature.getEntity().getName());
			}

		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testAnalysisSingleCommit_buggyCase_NPE_MOVE() throws Exception {

		ComingMain cm = new ComingMain();

		String pathToKotlinRepo = "/Users/matias/develop/kotlinresearch/dataset_kotlin_migration/poet-assistant";// ComingProperties.getProperty("testrepolocation");
		File locationKotinRepo = new File(pathToKotlinRepo);
		assertTrue(locationKotinRepo.exists());

		// cm.run
		boolean created = cm.createEngine(new String[] { "-location", locationKotinRepo.getAbsolutePath(),
				// Here our new analyzer
				"-mode", NoAnalysis.class.getName()// HunkDifftAnalyzer.class.getName()
				//
				// "-parameters", "save_result_revision_analysis:false"
		});

		assertTrue(created);

		Object finalResult = cm.start();

		CommitFinalResult commitResult = (CommitFinalResult) finalResult;

		Commit selectedCommit = commitResult.keySet().stream()
				.filter(e -> e.getName().equals("f9f17e905307c358736381b89ee5596e87090de4")).findFirst().get();

		System.out.println("Commit c: " + selectedCommit);

		GITRepositoryInspector inspector = new MigACore();

		RevisionResult resultAllAnalyzed = new RevisionResult(selectedCommit);
		for (Analyzer anAnalyzer : inspector.getAnalyzers()) {

			AnalysisResult resultAnalyzer = anAnalyzer.analyze(selectedCommit, resultAllAnalyzed);
			resultAllAnalyzed.put(anAnalyzer.getClass().getSimpleName(), resultAnalyzer);

		}

		// log.debug("D->" + resultAllAnalyzed);

		DiffResult<IRevision, SingleDiff> dk = (DiffResult<IRevision, SingleDiff>) resultAllAnalyzed
				.getResultFromClass(KotlinDiffAnalyzer.class);

		// SingleFileDiff diffWotdAdapter =
		// dk.getDiffOfFiles().get("ViewShownCompletable.kt");
		// assertNotNull(diffWotdAdapter);

//		FeatureResult featureResult = (FeatureResult) resultAllAnalyzed.getResultFromClass(KotlinFeatureAnalyzer.class);

	}

}
