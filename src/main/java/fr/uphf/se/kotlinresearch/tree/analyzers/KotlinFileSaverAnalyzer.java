package fr.uphf.se.kotlinresearch.tree.analyzers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.core.entities.interfaces.FileCommit;
import fr.inria.coming.core.entities.interfaces.IRevisionPair;
import fr.uphf.se.kotlinresearch.arm.analyzers.AddRemoveResult;
import fr.uphf.se.kotlinresearch.arm.analyzers.AddedRemovedAnalyzer;

/**
 * (?)
 * 
 * @author Matias Martinez
 *
 */
@Deprecated
public class KotlinFileSaverAnalyzer implements Analyzer<IRevision> {

	Logger log = Logger.getLogger(KotlinFileSaverAnalyzer.class.getName());

	@SuppressWarnings("rawtypes")
	@Override
	public AnalysisResult analyze(IRevision revision, RevisionResult previousResults) {

		List<FileCommit> childerPairs = ((Commit) revision).getFileCommits();

		log.info("\n*** Analyzing revision: " + revision.getName());

		// ARM
		AddRemoveResult arresult = (AddRemoveResult) previousResults.getResultFromClass(AddedRemovedAnalyzer.class);
		log.info("\nAR result: " + arresult);

		// For each file inside the revision
		for (IRevisionPair iRevisionPair : childerPairs) {

			log.debug("visiting " + iRevisionPair.getName());

			if (!arresult.modifKotlin.contains(iRevisionPair.getPreviousName())
					&& !arresult.modifKotlin.contains(iRevisionPair.getName())) {
				continue;
			}
			log.info("\n*** Saving revision: " + revision.getName());

			String filename = iRevisionPair.getName();
			if (iRevisionPair.getPreviousName().endsWith(".kt")) {
				filename = iRevisionPair.getPreviousName();
				log.debug("-revisionPair-->" + filename);

				String leftKotlinFile = iRevisionPair.getPreviousVersion().toString();

				if (leftKotlinFile != null && !leftKotlinFile.trim().isEmpty()) {

					saveFile(revision, iRevisionPair, leftKotlinFile, true);
				}
			}
			if (iRevisionPair.getName().endsWith(".kt")) {
				// if already exists, we keep right
				filename = iRevisionPair.getName();
				String rightKotlinFile = iRevisionPair.getNextVersion().toString();

				if (rightKotlinFile != null && !rightKotlinFile.trim().isEmpty()) {
					saveFile(revision, iRevisionPair, rightKotlinFile, false);
				}
			}

		}
		return null;
	}

	public void saveFile(IRevision revision, IRevisionPair iRevisionPair, String leftKotlinFile, boolean isleft) {
		try {
			// log.info("Saving pre");
			String pathr = "./out/" + revision.getName() + File.separator
					+ iRevisionPair.getName().replace(".kt", (isleft) ? "_s.kt" : "_t.kt");
			File fl = new File(pathr);
			fl.getParentFile().mkdirs();
			FileWriter fw = new FileWriter(pathr);
			fw.write(leftKotlinFile);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
