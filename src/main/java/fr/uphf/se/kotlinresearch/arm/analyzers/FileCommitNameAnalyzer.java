package fr.uphf.se.kotlinresearch.arm.analyzers;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jetbrains.kotlin.org.jline.utils.Log;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.FileCommit;
import fr.inria.coming.core.entities.interfaces.IRevisionPair;
import fr.uphf.se.kotlinresearch.core.MigACore;
import fr.uphf.se.kotlinresearch.core.Outils;

/**
 * 
 * @author Matias Martinez
 *
 */
@SuppressWarnings("rawtypes")
public class FileCommitNameAnalyzer implements Analyzer<IRevision> {

	@Override
	public AnalysisResult analyze(IRevision revision, RevisionResult previousResults) {
		long init = (new Date()).getTime();
		// List<FileCommit> childerPairs = ((Commit) revision).getFileCommits();
		List<IRevisionPair> childerPairs = revision.getChildren();
		Log.debug("\nCommit " + revision.getName());
		Map<String, IRevisionPair<String>> pre = new java.util.HashMap<String, IRevisionPair<String>>();
		Map<String, IRevisionPair<String>> post = new java.util.HashMap<String, IRevisionPair<String>>();

		List<IRevisionPair<String>> result = new ArrayList<>();
		List<IRevisionPair<String>> merged = new ArrayList<>();

		// For each file inside the revision
		for (IRevisionPair<String> iRevisionPair : childerPairs) {

			Log.debug("Prev: " + iRevisionPair.getPreviousName());
			Log.debug("Post: " + iRevisionPair.getName());
			Log.debug("--");

			if (isNull(iRevisionPair.getPreviousName())) {

				String currentName = Outils.getFileName(iRevisionPair.getName());
				post.put(getFileName(currentName), iRevisionPair);
			} else if (isNull(iRevisionPair.getName())) {
				String previousName = Outils.getFileName(iRevisionPair.getPreviousName());
				pre.put(getFileName(previousName), iRevisionPair);
			} else
				result.add(iRevisionPair);

		}

		for (String ipre : pre.keySet()) {
			IRevisionPair<String> preFc = pre.get(ipre);
			if (post.containsKey(ipre)) {
				// merge

				IRevisionPair<String> postFc = post.get(ipre);
				preFc.setName(postFc.getName());

				preFc.setNextVersion(postFc.getNextVersion());
				Log.debug("MERGING " + ipre);
				// add
				result.add(preFc);
				merged.add(preFc);
				// remove from post
				post.remove(ipre);
			} else {

				result.add(preFc);
			}
		}

		// Adding the not linked from post
		result.addAll(post.values());

		// System.out.println("Final number fileCommits: " + result.size());
		// System.out.println("Unpairs " + pre.keySet());
		// System.out.println("Unpairs " + post.keySet());
		MigACore.executionsTime.add(this.getClass().getSimpleName(), new Long((new Date()).getTime() - init));

		return new RenameAnalyzerResult(revision, result, merged);
	}

	public String getFileName(String completeFileName) {

		int idx = completeFileName.lastIndexOf(File.separator);

		String s = completeFileName.substring(idx + 1);

		int idp = s.indexOf(".");
		if (idp == -1)
			return s;

		return s.substring(0, idp);
	}

	public static boolean isNull(Object previousName) {
		return previousName == null || previousName.toString().trim().isEmpty() || previousName.toString().isEmpty()
				|| "null".equals(previousName.toString())
				|| previousName.toString().toLowerCase().contains("/dev/null");
	}

	public boolean isAdded(IRevisionPair iRevisionPair) {

		return (iRevisionPair.getPreviousVersion() == null
				|| iRevisionPair.getPreviousVersion().toString().trim().isEmpty());

	}

	public boolean isDiffName(IRevisionPair iRevisionPair) {
		if (iRevisionPair instanceof FileCommit) {
			FileCommit fc = (FileCommit) iRevisionPair;
			return fc.getPreviousFileName().equals(fc.getNextFileName());
		}
		return false;

	}

	public boolean isModif(IRevisionPair iRevisionPair) {
		return (iRevisionPair.getPreviousVersion() != null
				&& !iRevisionPair.getPreviousVersion().toString().trim().isEmpty())
				&& (iRevisionPair.getNextVersion() != null
						&& !iRevisionPair.getNextVersion().toString().trim().isEmpty());

	}

	public boolean isRemove(IRevisionPair iRevisionPair) {
		return (iRevisionPair.getNextVersion() == null || iRevisionPair.getNextVersion().toString().trim().isEmpty());

	}
}
