package fr.uphf.se.kotlinresearch.tests.patterndiscov;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.engine.filespair.FilesPairNavigation;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.main.ComingProperties;
import fr.uphf.se.kotlinresearch.core.MigACore;
import fr.uphf.se.kotlinresearch.diff.analyzers.JavaDiffAnalyzer;
import fr.uphf.se.kotlinresearch.diff.analyzers.SingleDiff;
import fr.uphf.se.kotlinresearch.diff.analyzers.granularitylevel.JavaBlockDiffSplitterAnalyzer;
import fr.uphf.se.kotlinresearch.diff.analyzers.granularitylevel.JavaMethodDiffSplitterAnalyzer;
import fr.uphf.se.kotlinresearch.tree.analyzers.JavaTreeAnalyzer;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ASTChangeSpliterTest {

	@SuppressWarnings("rawtypes")
	@Test
	public void testOneSingleChangeBlock() throws IOException {

		String c1b = (" class BehaviorCall implements Call{\n"
				+ "final AtomicReference failureRef = new AtomicReference<>();\n"
				+ "final CountDownLatch latch = new CountDownLatch(1);\n" + "enqueue(new Callback<T>() {\n"
				+ "  @Override public void onResponse(Response<T> response) {\n" + "     responseRef.set(response);\n"
				+ "     latch.countDown();\n" + "   }\n" + "}\n" + ")\n" + "\n" + "}");

		String c2b = ("class BehaviorCall implements Call {\n"
				+ "final AtomicReference failureRef = new AtomicReference<>();\n"
				+ "final CountDownLatch latch = new CountDownLatch(1);\n" + "enqueue(new Callback<T>() {\n"

				+ "  @Override public void onResponse(Response<T> response) {\n" + "     responseRef.set(response);\n"

				// added
				+ "System.out.println(response);" + "latch.countDown();\n" + "}\n" + "}\n" + ")\n" + "}");

		MigACore core = new MigACore();
		File left = File.createTempFile("left", ".java");

		FileWriter fwl = new FileWriter(left);
		fwl.write(c1b);
		fwl.close();

		File right = File.createTempFile("right", ".java");

		FileWriter fwr = new FileWriter(right);
		fwr.write(c2b);
		fwr.close();

		FilesPairNavigation navpair = new FilesPairNavigation(left, right);

		ComingProperties.setProperty("save_result_revision_analysis", "true");

		navpair.setAnalyzers(core.getAnalyzers());

		if (!core.getAnalyzers().stream().filter(e -> e instanceof JavaBlockDiffSplitterAnalyzer).findAny()
				.isPresent()) {
			Analyzer treejavaanalyzer = core.getAnalyzers().stream().filter(e -> e instanceof JavaTreeAnalyzer)
					.findAny().get();
			int indexTree = core.getAnalyzers().indexOf(treejavaanalyzer);

			core.getAnalyzers().add(indexTree + 1, new JavaBlockDiffSplitterAnalyzer());
		}

		FinalResult finalResult = navpair.analyze();
		System.out.println(finalResult);

		assertEquals(1, finalResult.getAllResults().values().size());

		RevisionResult result = (RevisionResult) finalResult.getAllResults().values().stream().findFirst().get();

		AnalysisResult analysisJavaBlock = result.getResultFromClass(JavaBlockDiffSplitterAnalyzer.class);

		DiffResult<IRevision, List<SingleDiff>> resultBlocks = (DiffResult<IRevision, List<SingleDiff>>) analysisJavaBlock;

		assertNotNull(resultBlocks);

		assertEquals(1, resultBlocks.getAll().size());

		List<SingleDiff> diffsGrouped = resultBlocks.getAll().stream().findFirst().get();
		assertTrue(diffsGrouped.size() > 0);
		assertEquals(1, diffsGrouped.size());

		// Now inside the group
		SingleDiff firstGroup = diffsGrouped.get(0);

		assertEquals(1, firstGroup.getRootOperations().size());

	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testOneTwoChangesChangeTwoBlocks() throws IOException {

		String c1b = (" class BehaviorCall implements Call{\n"
				+ "final AtomicReference failureRef = new AtomicReference<>();\n"
				+ "final CountDownLatch latch = new CountDownLatch(1);\n" + "enqueue(new Callback<T>() {\n"
				+ "  @Override public void onResponse(Response<T> response) {\n" +

				" if (reponse != null){System.out.println(failureRef); } " + "     responseRef.set(response);\n"
				+ "     latch.countDown();\n" + "   }\n" + "}\n" + ")\n" + "\n" + "}");

		String c2b = ("class BehaviorCall implements Call {\n"
				+ "final AtomicReference failureRef = new AtomicReference<>();\n"
				+ "final CountDownLatch latch = new CountDownLatch(1);\n" + "enqueue(new Callback<T>() {\n"

				+ "  @Override public void onResponse(Response<T> response) {\n"
				+ " if (reponse != null){System.out.println(latch); } "

				// Updated
				+ "     responseRef.set(response);\n"

				// added
				+ "System.out.println(response);" + "latch.countDown();\n" + "}\n" + "}\n" + ")\n" + "}");

		MigACore core = new MigACore();
		File left = File.createTempFile("left", ".java");

		FileWriter fwl = new FileWriter(left);
		fwl.write(c1b);
		fwl.close();

		File right = File.createTempFile("right", ".java");

		FileWriter fwr = new FileWriter(right);
		fwr.write(c2b);
		fwr.close();

		FilesPairNavigation navpair = new FilesPairNavigation(left, right);

		ComingProperties.setProperty("save_result_revision_analysis", "true");

		navpair.setAnalyzers(core.getAnalyzers());

		if (!core.getAnalyzers().stream().filter(e -> e instanceof JavaBlockDiffSplitterAnalyzer).findAny()
				.isPresent()) {
			Analyzer treejavaanalyzer = core.getAnalyzers().stream().filter(e -> e instanceof JavaTreeAnalyzer)
					.findAny().get();
			int indexTree = core.getAnalyzers().indexOf(treejavaanalyzer);

			core.getAnalyzers().add(indexTree + 1, new JavaBlockDiffSplitterAnalyzer());
		}

		if (!core.getAnalyzers().stream().filter(e -> e instanceof JavaMethodDiffSplitterAnalyzer).findAny()
				.isPresent()) {
			Analyzer treejavaanalyzer = core.getAnalyzers().stream().filter(e -> e instanceof JavaTreeAnalyzer)
					.findAny().get();
			int indexTree = core.getAnalyzers().indexOf(treejavaanalyzer);

			core.getAnalyzers().add(indexTree + 1, new JavaMethodDiffSplitterAnalyzer());
		}

		FinalResult finalResult = navpair.analyze();
		System.out.println(finalResult);

		assertEquals(1, finalResult.getAllResults().values().size());

		RevisionResult result = (RevisionResult) finalResult.getAllResults().values().stream().findFirst().get();

		AnalysisResult analysisJavaBlock = result.getResultFromClass(JavaBlockDiffSplitterAnalyzer.class);

		DiffResult<IRevision, List<SingleDiff>> resultBlocks = (DiffResult<IRevision, List<SingleDiff>>) analysisJavaBlock;

		assertNotNull(resultBlocks);

		assertEquals(1, resultBlocks.getAll().size());

		List<SingleDiff> diffsGrouped = resultBlocks.getAll().stream().findFirst().get();
		assertTrue(diffsGrouped.size() > 0);
		// two modified blocks
		assertEquals(2, diffsGrouped.size());

		// Now inside the group
		// SingleFileDiff firstGroup = diffsGrouped.get(0);
		// One change on each block
		assertEquals(1, diffsGrouped.get(0).getRootOperations().size());
		assertEquals(1, diffsGrouped.get(1).getRootOperations().size());

		//

		AnalysisResult analysisMethod = result.getResultFromClass(JavaMethodDiffSplitterAnalyzer.class);

		DiffResult<IRevision, List<SingleDiff>> resultMethod = (DiffResult<IRevision, List<SingleDiff>>) analysisMethod;

		assertNotNull(resultMethod);

		assertEquals(1, resultMethod.getAll().size());

		List<SingleDiff> diffsMethodGrouped = resultMethod.getAll().stream().findFirst().get();
		assertTrue(diffsMethodGrouped.size() > 0);
		// one modified method
		assertEquals(1, diffsMethodGrouped.size());
		assertEquals(2, diffsMethodGrouped.get(0).getRootOperations().size());
	}

	@SuppressWarnings("rawtypes")
	@Test
	@Ignore
	public void testOneTwoChangesChangeTwoBlocksWrongAssert() throws IOException {

		String c1b = (" class BehaviorCall implements Call{\n"
				+ "final AtomicReference failureRef = new AtomicReference<>();\n"
				+ "final CountDownLatch latch = new CountDownLatch(1);\n" + "enqueue(new Callback<T>() {\n"
				+ "  @Override public void onResponse(Response<T> response) {\n" +

				" if (reponse != null){System.out.println(failureRef); } " + "     responseRef.set(response);\n"
				+ "     latch.countDown();\n" + "   }\n" + "}\n" + ")\n" + "\n" + "}");

		String c2b = ("class BehaviorCall implements Call {\n"
				+ "final AtomicReference failureRef = new AtomicReference<>();\n"
				+ "final CountDownLatch latch = new CountDownLatch(1);\n" + "enqueue(new Callback<T>() {\n"

				+ "  @Override public void onResponse(Response<T> response) {\n"
				+ " if (reponse != null){System.out.println(null); } "

				//
				+ "     responseRef.set(response);\n"

				// added
				+ "System.out.println(response);" + "latch.countDown();\n" + "}\n" + "}\n" + ")\n" + "}");

		MigACore core = new MigACore();
		File left = File.createTempFile("left", ".java");

		FileWriter fwl = new FileWriter(left);
		fwl.write(c1b);
		fwl.close();

		File right = File.createTempFile("right", ".java");

		FileWriter fwr = new FileWriter(right);
		fwr.write(c2b);
		fwr.close();

		FilesPairNavigation navpair = new FilesPairNavigation(left, right);

		ComingProperties.setProperty("save_result_revision_analysis", "true");

		navpair.setAnalyzers(core.getAnalyzers());

		if (!core.getAnalyzers().stream().filter(e -> e instanceof JavaBlockDiffSplitterAnalyzer).findAny()
				.isPresent()) {
			Analyzer treejavaanalyzer = core.getAnalyzers().stream().filter(e -> e instanceof JavaTreeAnalyzer)
					.findAny().get();
			int indexTree = core.getAnalyzers().indexOf(treejavaanalyzer);

			core.getAnalyzers().add(indexTree + 1, new JavaBlockDiffSplitterAnalyzer());
		}

		if (!core.getAnalyzers().stream().filter(e -> e instanceof JavaMethodDiffSplitterAnalyzer).findAny()
				.isPresent()) {
			Analyzer treejavaanalyzer = core.getAnalyzers().stream().filter(e -> e instanceof JavaTreeAnalyzer)
					.findAny().get();
			int indexTree = core.getAnalyzers().indexOf(treejavaanalyzer);

			core.getAnalyzers().add(indexTree + 1, new JavaMethodDiffSplitterAnalyzer());
		}

		FinalResult finalResult = navpair.analyze();
		System.out.println(finalResult);

		assertEquals(1, finalResult.getAllResults().values().size());

		RevisionResult result = (RevisionResult) finalResult.getAllResults().values().stream().findFirst().get();

		DiffResult<IRevision, SingleDiff> diffResult = (DiffResult<IRevision, SingleDiff>) result
				.getResultFromClass(JavaDiffAnalyzer.class);

		assertNotNull(diffResult);
		assertEquals(1, diffResult.getAll().size());

		SingleDiff diff = diffResult.getAll().get(0);
		System.out.println(diff.getRootOperations());
		System.out.println(diff.getAllOperations());
		assertEquals(2, diff.getRootOperations().size());

		AnalysisResult analysisJavaBlock = result.getResultFromClass(JavaBlockDiffSplitterAnalyzer.class);

		DiffResult<IRevision, List<SingleDiff>> resultBlocks = (DiffResult<IRevision, List<SingleDiff>>) analysisJavaBlock;

		assertNotNull(resultBlocks);

		assertEquals(1, resultBlocks.getAll().size());

		List<SingleDiff> diffsGrouped = resultBlocks.getAll().stream().findFirst().get();
		assertTrue(diffsGrouped.size() > 0);
		// two modified blocks
		assertEquals(2, diffsGrouped.size());

		// Now inside the group
		// SingleFileDiff firstGroup = diffsGrouped.get(0);
		// One change on each block
		assertEquals(1, diffsGrouped.get(0).getRootOperations().size());
		assertEquals(1, diffsGrouped.get(1).getRootOperations().size());

		//

		AnalysisResult analysisMethod = result.getResultFromClass(JavaMethodDiffSplitterAnalyzer.class);

		DiffResult<IRevision, List<SingleDiff>> resultMethod = (DiffResult<IRevision, List<SingleDiff>>) analysisMethod;

		assertNotNull(resultMethod);

		assertEquals(1, resultMethod.getAll().size());

		List<SingleDiff> diffsMethodGrouped = resultMethod.getAll().stream().findFirst().get();
		assertTrue(diffsMethodGrouped.size() > 0);
		// one modified method
		assertEquals(1, diffsMethodGrouped.size());
		assertEquals(2, diffsMethodGrouped.get(0).getRootOperations().size());
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testOneTwoChangesChangeTwoMethods() throws IOException {
		MigACore core = new MigACore();
		FilesPairNavigation navpair = getTwoChangesTwoMethods();

		ComingProperties.setProperty("save_result_revision_analysis", "true");

		navpair.setAnalyzers(core.getAnalyzers());

		if (!core.getAnalyzers().stream().filter(e -> e instanceof JavaBlockDiffSplitterAnalyzer).findAny()
				.isPresent()) {
			Analyzer treejavaanalyzer = core.getAnalyzers().stream().filter(e -> e instanceof JavaTreeAnalyzer)
					.findAny().get();
			int indexTree = core.getAnalyzers().indexOf(treejavaanalyzer);

			core.getAnalyzers().add(indexTree + 1, new JavaBlockDiffSplitterAnalyzer());
		}

		if (!core.getAnalyzers().stream().filter(e -> e instanceof JavaMethodDiffSplitterAnalyzer).findAny()
				.isPresent()) {
			Analyzer treejavaanalyzer = core.getAnalyzers().stream().filter(e -> e instanceof JavaTreeAnalyzer)
					.findAny().get();
			int indexTree = core.getAnalyzers().indexOf(treejavaanalyzer);

			core.getAnalyzers().add(indexTree + 1, new JavaMethodDiffSplitterAnalyzer());
		}

		FinalResult finalResult = navpair.analyze();
		System.out.println(finalResult);
		assertEquals(1, finalResult.getAllResults().values().size());

		RevisionResult result = (RevisionResult) finalResult.getAllResults().values().stream().findFirst().get();

		DiffResult<IRevision, SingleDiff> diffResult = (DiffResult<IRevision, SingleDiff>) result
				.getResultFromClass(JavaDiffAnalyzer.class);

		assertNotNull(diffResult);
		assertEquals(1, diffResult.getAll().size());

		SingleDiff diff = diffResult.getAll().get(0);
		System.out.println(diff.getRootOperations());
		System.out.println(diff.getAllOperations());
		// 4 Changes
		assertEquals(4, diff.getRootOperations().size());

		AnalysisResult analysisJavaBlock = result.getResultFromClass(JavaBlockDiffSplitterAnalyzer.class);

		DiffResult<IRevision, List<SingleDiff>> resultBlocks = (DiffResult<IRevision, List<SingleDiff>>) analysisJavaBlock;

		assertNotNull(resultBlocks);

		assertEquals(1, resultBlocks.getAll().size());

		List<SingleDiff> diffsGrouped = resultBlocks.getAll().stream().findFirst().get();
		assertTrue(diffsGrouped.size() > 0);
		// two modified blocks
		assertEquals(4, diffsGrouped.size());

		// Now inside the group

		// One change on each block
		assertEquals(1, diffsGrouped.get(0).getRootOperations().size());
		assertEquals(1, diffsGrouped.get(1).getRootOperations().size());

		//

		AnalysisResult analysisMethod = result.getResultFromClass(JavaMethodDiffSplitterAnalyzer.class);

		DiffResult<IRevision, List<SingleDiff>> resultMethod = (DiffResult<IRevision, List<SingleDiff>>) analysisMethod;

		assertNotNull(resultMethod);

		assertEquals(1, resultMethod.getAll().size());

		List<SingleDiff> diffsMethodGrouped = resultMethod.getAll().stream().findFirst().get();
		assertTrue(diffsMethodGrouped.size() > 0);
		// one modified method
		assertEquals(2, diffsMethodGrouped.size());
		assertEquals(2, diffsMethodGrouped.get(0).getRootOperations().size());
		assertEquals(2, diffsMethodGrouped.get(1).getRootOperations().size());
	}

	public FilesPairNavigation getTwoChangesTwoMethods() throws IOException {
		String c1b = (" class BehaviorCall implements Call{\n" + "AtomicReference failureRef2 = null;"
				+ "final AtomicReference failureRef = new AtomicReference<>();\n" + "public void m1(){"
				+ "if(true){failureRef2 = failureRef2;} else{failureRef=failureRef2;}" + "}" + "public void m2(){"
				+ "if(false){failureRef2 = failureRef2;} else{failureRef=failureRef2;}" + "}"

				+ "}");

		String c2b = "class BehaviorCall implements Call {\n" + "AtomicReference failureRef2 = null;"
				+ "final AtomicReference failureRef = new AtomicReference<>();\n" + "public void m1(){"
				+ "if(true){failureRef2 = failureRef;} else{failureRef=failureRef;}" + "}" + "public void m2(){"
				+ "if(false){failureRef2 = failureRef;} else{failureRef=failureRef;}" + "}";

		File left = File.createTempFile("left", ".java");

		FileWriter fwl = new FileWriter(left);
		fwl.write(c1b);
		fwl.close();

		File right = File.createTempFile("right", ".java");

		FileWriter fwr = new FileWriter(right);
		fwr.write(c2b);
		fwr.close();

		FilesPairNavigation navpair = new FilesPairNavigation(left, right);
		return navpair;
	}

}
