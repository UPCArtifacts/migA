package fr.uphf.se.kotlinresearch.features.analyzers;

import java.util.List;
import java.util.Map;

import fr.inria.coming.core.entities.AnalysisResult;
import fr.uphf.analyze.DetectionResult;
import io.gitlab.arturbosch.detekt.api.Finding;

public class FeatureResult<T> extends AnalysisResult {

	private Map<String, List<Finding>> findings;

	public FeatureResult(T analyzed, Map<String, List<Finding>> findings) {
		super(analyzed);
		this.findings = findings;
	}

	public Map<String, List<Finding>> getFindings() {
		return this.findings;
	}

	@Override
	public String toString() {
		return DetectionResult.Companion.asJson(findings);
	}
}
