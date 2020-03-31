package fr.uphf.se.kotlinresearch.core;

import com.github.gumtreediff.utils.Pair;

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

	public CommitDataAnalyzer() {

	}

	@Override
	public AnalysisResult<Pair<String, String>> analyze(IRevision input, RevisionResult previousResults) {
		if (input instanceof Commit) {
			Commit commit = (Commit) input;
			String message = commit.getFullMessage();
			String date = commit.getRevDate();
			return new AnalysisResult<Pair<String, String>>(new Pair<String, String>(message, date));
		} else
			return null;
	}

}
