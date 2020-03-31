package fr.uphf.se.kotlinresearch.arm.analyzers;

import fr.inria.coming.core.entities.AnalysisResult;

/**
 * 
 * @author Matias Martinez
 *
 * @param <T>
 */
public class UnifiedDiffResult<T> extends AnalysisResult<T> {

	public UnifiedDiffResult(T analyzed) {
		super(analyzed);
	}

}
