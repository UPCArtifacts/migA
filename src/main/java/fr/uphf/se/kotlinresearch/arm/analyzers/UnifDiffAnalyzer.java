package fr.uphf.se.kotlinresearch.arm.analyzers;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.IRevisionPair;
import fr.inria.coming.main.ComingProperties;
import fr.uphf.se.kotlinresearch.core.MigACore;
import fr.uphf.se.kotlinresearch.core.Outils;

/**
 * 
 * @author Matias Martinez
 *
 */
public class UnifDiffAnalyzer implements Analyzer<IRevision> {
	protected Logger log = Logger.getLogger(UnifDiffAnalyzer.class.getName());

	File outDir = null;

	public UnifDiffAnalyzer() {
		String projectName = ComingProperties.getProperty("projectname");
		outDir = new File(ComingProperties.getProperty("output") + File.separator + projectName);
		if (!outDir.exists()) {
			outDir.mkdirs();
		}
	}

	@Override
	public AnalysisResult analyze(IRevision revision, RevisionResult previousResults) {
		long init = (new Date()).getTime();
		RenameAnalyzerResult renameresult = (RenameAnalyzerResult) previousResults
				.getResultFromClass(FileCommitNameAnalyzer.class);

		List<IRevisionPair<String>> childerPairs = renameresult.getAllFileCommits();

		for (IRevisionPair<String> fileFromRevision : childerPairs) {
			try {
				if ((Outils.isEmpty(fileFromRevision.getPreviousVersion())
						|| Outils.isEmpty(fileFromRevision.getNextVersion()))
						|| (!fileFromRevision.getPreviousName().endsWith(".java")
								//
								&& (!fileFromRevision.getPreviousName().endsWith(".kt")))) {
					continue;
				}

				try {
					String extensionL = fileFromRevision.getPreviousName().split("\\.")[1];
					String extensionR = fileFromRevision.getName().split("\\.")[1];

					if (!extensionL.equals(extensionR)) {
						continue;
					}
				} catch (Exception e) {
					// System.err.println(fileFromRevision.getPreviousName() + " " +
					// fileFromRevision.getNextFileName());
					log.error(e);

					continue;
				}

				// System.out.println("-----------Unify");

				String[] preVersion = fileFromRevision.getPreviousVersion().split("\\r?\\n");
				String[] postVersion = fileFromRevision.getNextVersion().split("\\r?\\n");

				List<String> pre = Arrays.asList(preVersion);

				List<String> post = Arrays.asList(postVersion);

				// generating diff information.
				Patch<String> diff = DiffUtils.diff(pre, post);
				// System.out.println("\n-----deltas ");
				// generating unified diff format
				List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(fileFromRevision.getName(),
						fileFromRevision.getName(), pre, diff, 5);

				// unifiedDiff.forEach(System.out::println);
				File dir = new File(outDir + File.separator + "unifdiff" + File.separator + revision.getName());
				dir.mkdirs();
				File file = new File(dir + File.separator + fileFromRevision.getName() + ".diff");

				FileWriter fw = new FileWriter(file);
				for (String delta : unifiedDiff) {
					fw.append(delta);
					fw.append("\n");
				}
				fw.close();
				log.debug("Saving diff at " + file.getAbsolutePath());
				// importing unified diff format from file or here from memory to a Patch
				// Patch<String> importedPatch = UnifiedDiffUtils.parseUnifiedDiff(unifiedDiff);

				// System.out.println("\n----applyed ");
				// apply patch to original list
				// List<String> patchedText = DiffUtils.patch(pre, importedPatch);

				// patchedText.forEach(System.out::println);

				//

//				DiffRowGenerator generator = DiffRowGenerator.create().showInlineDiffs(true).inlineDiffByWord(true)
//						.oldTag(f -> "~").newTag(f -> "**").build();
//				List<DiffRow> rows = generator.generateDiffRows(pre, post);

				// System.out.println("|original|new|");
				// System.out.println("|--------|---|");
				// for (DiffRow row : rows) {
				// System.out.println("|" + row.getOldLine() + "|" + row.getNewLine() + "|");
				// }

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		MigACore.executionsTime.add(this.getClass().getSimpleName(), new Long((new Date()).getTime() - init));

		return new UnifiedDiffResult<IRevision>(revision);
	}
}
