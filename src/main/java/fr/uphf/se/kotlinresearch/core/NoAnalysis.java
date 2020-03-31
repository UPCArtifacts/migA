package fr.uphf.se.kotlinresearch.core;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.RevisionResult;

/**
 * 
 * @author Matias Martinez
 *
 */
public class NoAnalysis implements Analyzer<IRevision> {

	@Override
	public AnalysisResult analyze(IRevision input, RevisionResult previousResults) {
		return null;
	}

}
