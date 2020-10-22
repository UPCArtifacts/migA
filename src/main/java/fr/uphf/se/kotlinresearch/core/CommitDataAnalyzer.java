package fr.uphf.se.kotlinresearch.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.Commit;

/**
 * 
 * @author Matias Martinez
 *
 */
public class CommitDataAnalyzer implements Analyzer<IRevision> {

	public static final String PARENTS = "PARENTS";
	public static final String BRANCHES = "BRANCHES";
	public static final String DATEINT = "DATEINT";
	public static final String DATE = "DATE";
	public static final String MESSAGE = "MESSAGE";
	public static final String AUTHOR = "AUTHOR";
	public static final String EMAIL = "EMAIL";

	public CommitDataAnalyzer() {

	}

	@Override
	public AnalysisResult<Map<String, Object>> analyze(IRevision input, RevisionResult previousResults) {
		if (input instanceof Commit) {
			Commit commit = (Commit) input;
			String message = commit.getFullMessage();
			String date = commit.getRevDate();
			long dateint = commit.getAuthorInfo().getWhen().getTime();// commit.getRevCommitTime();
			List<String> branches = commit.getBranches();
			List<String> parents = commit.getParents();

			Map<String, Object> data = new HashMap<>();
			data.put(MESSAGE, message);
			data.put(DATE, date);
			data.put(DATEINT, dateint);

			data.put(BRANCHES, branches);
			data.put(PARENTS, parents);

			data.put(AUTHOR, commit.getAuthorInfo().getName());
			data.put(EMAIL, commit.getAuthorInfo().getEmailAddress());

			return new AnalysisResult<Map<String, Object>>(data);
		} else
			return null;
	}

}
