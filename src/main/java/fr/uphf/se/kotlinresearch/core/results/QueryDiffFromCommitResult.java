package fr.uphf.se.kotlinresearch.core.results;

import fr.inria.coming.utils.MapList;

/**
 * Store the results of one commit, split according to some criterion. The key
 * is the identifier (File, method, and the values are the pattern instance
 * Values)
 * 
 * @author Matias Martinez
 *
 *
 */
@SuppressWarnings("serial")
public class QueryDiffFromCommitResult extends MapList<String, PatternInstanceProperties> {

}
