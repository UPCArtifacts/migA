package fr.uphf.se.kotlinresearch.features.analyzers;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.core.entities.interfaces.FileCommit;
import fr.uphf.feature.FeatureDetector;
import io.gitlab.arturbosch.detekt.api.Finding;

public class KotlinFeatureAnalyzer implements Analyzer<IRevision> {

	Logger log = Logger.getLogger(KotlinFeatureAnalyzer.class.getName());

	@SuppressWarnings("rawtypes")
	@Override
	public AnalysisResult analyze(IRevision revision, RevisionResult previousResults) {
		long init = (new Date()).getTime();
		List<FileCommit> childerPairs = ((Commit) revision).getFileCommits();

		log.debug("\n*** Analyzing revision: " + revision.getName());

		Map<String, List<Finding>> result = new HashMap<>();

		// For each file inside the revision
		for (FileCommit iRevisionPair : childerPairs) {

			String filename = iRevisionPair.getName();
			if (filename.endsWith(".kt")) {

				log.debug("-revisionPair-->" + filename);

				String rightKotlinFile = iRevisionPair.getNextVersion().toString();

				List<Finding> findings = FeatureDetector.Companion
						.extractAll(fr.uphf.analyze.Helper.compileTo(rightKotlinFile, filename));

				result.put(filename, findings);

			} else if (filename.isEmpty() && iRevisionPair.getPreviousFileName().endsWith(".kt")) {

				result.put(iRevisionPair.getPreviousFileName(), null);
			}

		}
		return new FeatureResult<>(revision, result);
	}

}
