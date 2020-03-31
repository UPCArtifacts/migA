package fr.uphf.se.kotlinresearch.diff.analyzers.granularitylevel.noSplitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.DiffResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.uphf.se.kotlinresearch.squarediff.entities.diff.QueryDiff;

/**
 * 
 * @author Matias Martinez
 *
 */
public abstract class NoSplitterAbstractAnalyzer implements Analyzer<IRevision> {
	Logger log = Logger.getLogger(NoSplitterAbstractAnalyzer.class.getName());

	@SuppressWarnings("unchecked")
	@Override
	public AnalysisResult analyze(IRevision input, RevisionResult previousResults) {

		DiffResult<IRevision, QueryDiff> dkotlin = (DiffResult<IRevision, QueryDiff>) previousResults
				.getResultFromClass(getMethod());

		Map<String, List<QueryDiff>> group = new HashMap<>();

		for (String javaFile : dkotlin.getDiffOfFiles().keySet()) {

			QueryDiff fileKotlinDiff = dkotlin.getDiffOfFiles().get(javaFile);
			fileKotlinDiff.metadata.put("split", javaFile);

			List<QueryDiff> diffsFromMethod = new ArrayList<QueryDiff>();

			diffsFromMethod.add(fileKotlinDiff);

			group.put(javaFile, diffsFromMethod);

		}

		return new DiffResult<IRevision, List<QueryDiff>>(input, group);
	}

	public abstract Class getMethod();

}
