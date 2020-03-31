package fr.uphf.se.kotlinresearch.features.analyzers;

import java.util.List;
import java.util.Map;

import com.github.gumtreediff.actions.model.Action;

import fr.inria.coming.core.entities.AnalysisResult;
import io.gitlab.arturbosch.detekt.api.Finding;

/**
 * 
 * @author Matias Martinez
 *
 * @param <T>
 */
public class FeatureFromDiffResult<T> extends AnalysisResult {

	private Map<String, Map<Action, List<Finding>>> findings;

	public FeatureFromDiffResult(T analyzed, Map<String, Map<Action, List<Finding>>> findings) {
		super(analyzed);
		this.findings = findings;
	}

	public Map<String, Map<Action, List<Finding>>> getFindings() {
		return this.findings;
	}

	@Override
	public String toString() {
		// return DetectionResult.Companion.asJson(findings);
		return findings.toString();
	}
}
