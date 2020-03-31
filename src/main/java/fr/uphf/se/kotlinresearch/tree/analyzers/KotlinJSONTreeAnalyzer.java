package fr.uphf.se.kotlinresearch.tree.analyzers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.Commit;
import fr.inria.coming.core.entities.interfaces.FileCommit;
import fr.inria.coming.core.entities.interfaces.IRevisionPair;
import fr.uphf.se.kotlinresearch.core.ASTConverted;

/**
 * 
 * @author Matias Martinez
 *
 */
@Deprecated // It would be use the Kastree AST
public class KotlinJSONTreeAnalyzer implements Analyzer<IRevision> {

	Logger log = Logger.getLogger(KotlinJSONTreeAnalyzer.class.getName());

	@SuppressWarnings("rawtypes")
	@Override
	public AnalysisResult analyze(IRevision revision, RevisionResult previousResults) {

		List<FileCommit> childerPairs = ((Commit) revision).getFileCommits();

		log.info("\n*** Analyzing revision: " + revision.getName());

		Map<String, ITree[]> treeOfFiles = new HashMap<>();
		TreeContext context = new TreeContext();
		// For each file inside the revision
		for (IRevisionPair iRevisionPair : childerPairs) {

			log.debug("visiting " + iRevisionPair.getName());

			ITree treeLeft = null;
			ITree treeRight = null;
			String filename = iRevisionPair.getName();
			if (iRevisionPair.getPreviousName().endsWith(".kt")) {
				filename = iRevisionPair.getPreviousName();
				log.debug("-revisionPair-->" + filename);

				String leftKotlinFile = iRevisionPair.getPreviousVersion().toString();

				if (leftKotlinFile != null && !leftKotlinFile.trim().isEmpty())
					treeLeft = ASTConverted.getRootTree(context,
							fr.uphf.analyze.Helper.getASTasJson(leftKotlinFile, filename));

			}
			if (iRevisionPair.getName().endsWith(".kt")) {
				// if already exists, we keep right
				filename = iRevisionPair.getName();
				String rightKotlinFile = iRevisionPair.getNextVersion().toString();

				if (rightKotlinFile != null && !rightKotlinFile.trim().isEmpty())
					treeRight = ASTConverted.getRootTree(context,
							fr.uphf.analyze.Helper.getASTasJson(rightKotlinFile, filename));

			}
			if (treeLeft != null || treeRight != null) {
				ITree[] treesOfRevision = new ITree[2];
				treesOfRevision[TreeResult.LEFT] = treeLeft;
				treesOfRevision[TreeResult.RIGHT] = treeRight;
				treeOfFiles.put(filename, treesOfRevision);
			}

		}
		return new TreeResult(revision, treeOfFiles, context);
	}

}
