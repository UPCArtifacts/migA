package fr.uphf.se.kotlinresearch.arm.analyzers;

import java.util.List;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.interfaces.IRevisionPair;

/**
 * 
 * @author Matias Martinez
 *
 */
public class RenameAnalyzerResult extends AnalysisResult<IRevision> {

	List<IRevisionPair<String>> allFileCommits;
	List<IRevisionPair<String>> merged;

	public RenameAnalyzerResult(IRevision analyzed, List<IRevisionPair<String>> result,
			List<IRevisionPair<String>> merged) {
		super(analyzed);
		this.allFileCommits = result;
		this.merged = merged;

	}

	public List<IRevisionPair<String>> getAllFileCommits() {
		return allFileCommits;
	}

	public void setAllFileCommits(List<IRevisionPair<String>> allFileCommits) {
		this.allFileCommits = allFileCommits;
	}

	public List<IRevisionPair<String>> getMerged() {
		return merged;
	}

	public void setMerged(List<IRevisionPair<String>> merged) {
		this.merged = merged;
	}

}
