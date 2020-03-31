/*
 * This file is part of GumTree.
 *
 * GumTree is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GumTree is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GumTree.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2015-2016 Georg Dotzler <georg.dotzler@fau.de>
 * Copyright 2015-2016 Marius Kamp <marius.kamp@fau.de>
 */
package fr.uphf.se.kotlinresearch.diff.heuristic.mtdiff;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;

import com.github.gumtreediff.matchers.CompositeMatcher;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.heuristic.cd.ChangeDistillerBottomUpMatcher;
import com.github.gumtreediff.matchers.heuristic.cd.ChangeDistillerLeavesMatcher;
import com.github.gumtreediff.matchers.heuristic.cd.ChangeDistillerParallelLeavesMatcher;
import com.github.gumtreediff.matchers.heuristic.gt.GreedyBottomUpMatcher;
import com.github.gumtreediff.matchers.heuristic.gt.GreedySubtreeMatcher;
import com.github.gumtreediff.matchers.optimal.rted.RtedMatcher;
import com.github.gumtreediff.matchers.optimizations.CrossMoveMatcherThetaF;
import com.github.gumtreediff.matchers.optimizations.IdenticalSubtreeMatcherThetaA;
import com.github.gumtreediff.matchers.optimizations.InnerNodesMatcherThetaD;
import com.github.gumtreediff.matchers.optimizations.LcsOptMatcherThetaB;
import com.github.gumtreediff.matchers.optimizations.LeafMoveMatcherThetaE;
import com.github.gumtreediff.matchers.optimizations.UnmappedLeavesMatcherThetaC;
import com.github.gumtreediff.tree.ITree;

import fr.uphf.se.kotlinresearch.diff.heuristic.mtdiff.intern.LabelConfiguration;
import fr.uphf.se.kotlinresearch.diff.heuristic.mtdiff.intern.TreeMatcherConfiguration;

public class MtDiffOptimizedVersions {

	public static class CdabcdefSeq extends CompositeMatcher {

		/**
		 * Instantiates the sequential ChangeDistiller version with Theta A-F.
		 *
		 * @param src   the src
		 * @param dst   the dst
		 * @param store the store
		 */
		public CdabcdefSeq(ITree src, ITree dst, MappingStore store) {
			super(src, dst, store, new Matcher[] { new IdenticalSubtreeMatcherThetaA(src, dst, store),
					new ChangeDistillerLeavesMatcher(src, dst, store),
					new ChangeDistillerBottomUpMatcher(src, dst, store), new LcsOptMatcherThetaB(src, dst, store),
					new UnmappedLeavesMatcherThetaC(src, dst, store), new InnerNodesMatcherThetaD(src, dst, store),
					new LeafMoveMatcherThetaE(src, dst, store), new CrossMoveMatcherThetaF(src, dst, store) });
		}
	}

	public static class CdabcdefPar extends CompositeMatcher {

		/**
		 * Instantiates the parallel ChangeDistiller version with Theta A-F.
		 *
		 * @param src   the src
		 * @param dst   the dst
		 * @param store the store
		 */
		public CdabcdefPar(ITree src, ITree dst, MappingStore store) {
			super(src, dst, store, new Matcher[] { new IdenticalSubtreeMatcherThetaA(src, dst, store),
					new ChangeDistillerParallelLeavesMatcher(src, dst, store),
					new ChangeDistillerBottomUpMatcher(src, dst, store), new LcsOptMatcherThetaB(src, dst, store),
					new UnmappedLeavesMatcherThetaC(src, dst, store), new InnerNodesMatcherThetaD(src, dst, store),
					new LeafMoveMatcherThetaE(src, dst, store), new CrossMoveMatcherThetaF(src, dst, store)

			});
		}
	}

	public static class MtDiff extends CompositeMatcher {

		/**
		 * Instantiates MTDIFF.
		 *
		 * @param src   the src
		 * @param dst   the dst
		 * @param store the store
		 */
		public MtDiff(ITree src, ITree dst, MappingStore store) {
			this(src, dst, store, null);
		}

		/**
		 * Instantiates MTDIFF.
		 *
		 * @param src             the src
		 * @param dst             the dst
		 * @param store           the store
		 * @param executorService the executor service
		 */
		public MtDiff(ITree src, ITree dst, MappingStore store, ExecutorService executorService) {
			super(src, dst, store, new Matcher[] { new IdenticalSubtreeMatcherThetaA(src, dst, store),
					new MtDiffOptimizedMatcher(src, dst, store), new LcsOptMatcherThetaB(src, dst, store),
					new UnmappedLeavesMatcherThetaC(src, dst, store), new InnerNodesMatcherThetaD(src, dst, store),
					new LeafMoveMatcherThetaE(src, dst, store), new CrossMoveMatcherThetaF(src, dst, store)

			});
			TreeMatcherConfiguration configuration = null;
			configuration = new TreeMatcherConfiguration(0.88f, 0.37f, 0.0024f);

			HashSet<Integer> labelsForValueCompare = new HashSet<Integer>();
			labelsForValueCompare.addAll(Arrays.asList(new Integer[] { org.eclipse.jdt.core.dom.ASTNode.WILDCARD_TYPE,
					org.eclipse.jdt.core.dom.ASTNode.PREFIX_EXPRESSION,
					org.eclipse.jdt.core.dom.ASTNode.POSTFIX_EXPRESSION,
					org.eclipse.jdt.core.dom.ASTNode.INFIX_EXPRESSION, org.eclipse.jdt.core.dom.ASTNode.ASSIGNMENT, }));

			final HashSet<Integer> labelsForRealCompare = new HashSet<Integer>();

			HashSet<Integer> labelsForIntCompare = new HashSet<Integer>();
			labelsForIntCompare.addAll(Arrays.asList(new Integer[] {}));

			HashSet<Integer> labelsForStringCompare = new HashSet<Integer>();
			labelsForStringCompare.addAll(Arrays.asList(new Integer[] { org.eclipse.jdt.core.dom.ASTNode.SIMPLE_NAME,
					org.eclipse.jdt.core.dom.ASTNode.SIMPLE_TYPE, org.eclipse.jdt.core.dom.ASTNode.STRING_LITERAL,
					org.eclipse.jdt.core.dom.ASTNode.ARRAY_TYPE, org.eclipse.jdt.core.dom.ASTNode.PARAMETERIZED_TYPE,
					org.eclipse.jdt.core.dom.ASTNode.QUALIFIED_TYPE, org.eclipse.jdt.core.dom.ASTNode.QUALIFIED_NAME,
					org.eclipse.jdt.core.dom.ASTNode.NUMBER_LITERAL, org.eclipse.jdt.core.dom.ASTNode.CHARACTER_LITERAL,
					org.eclipse.jdt.core.dom.ASTNode.NUMBER_LITERAL,

			}));

			HashSet<Integer> labelsForBoolCompare = new HashSet<Integer>();
			labelsForBoolCompare
					.addAll(Arrays.asList(new Integer[] { org.eclipse.jdt.core.dom.ASTNode.BOOLEAN_LITERAL }));

			LabelConfiguration labelConfiguration = new LabelConfiguration(org.eclipse.jdt.core.dom.ASTNode.SIMPLE_NAME,
					org.eclipse.jdt.core.dom.ASTNode.COMPILATION_UNIT,
					org.eclipse.jdt.core.dom.ASTNode.TYPE_DECLARATION, org.eclipse.jdt.core.dom.ASTNode.SIMPLE_TYPE,
					org.eclipse.jdt.core.dom.ASTNode.MODIFIER, labelsForValueCompare, labelsForRealCompare,
					labelsForIntCompare, labelsForStringCompare, labelsForBoolCompare);

			((MtDiffOptimizedMatcher) matchers[1]).initMtDiff(executorService, configuration, labelConfiguration);
		}
	}

	public static class Gtbcdef extends CompositeMatcher {

		/**
		 * Instantiates GumTree with Theta B-F.
		 *
		 * @param src   the src
		 * @param dst   the dst
		 * @param store the store
		 */
		public Gtbcdef(ITree src, ITree dst, MappingStore store) {
			super(src, dst, store, new Matcher[] { new GreedySubtreeMatcher(src, dst, store),
					new GreedyBottomUpMatcher(src, dst, store), new LcsOptMatcherThetaB(src, dst, store),
					new UnmappedLeavesMatcherThetaC(src, dst, store), new InnerNodesMatcherThetaD(src, dst, store),
					new LeafMoveMatcherThetaE(src, dst, store), new CrossMoveMatcherThetaF(src, dst, store) });
		}
	}

	public static class Rtedacdef extends CompositeMatcher {

		/**
		 * Instantiates RTED with Theta A-F.
		 *
		 * @param src   the src
		 * @param dst   the dst
		 * @param store the store
		 */
		public Rtedacdef(ITree src, ITree dst, MappingStore store) {
			super(src, dst, store, new Matcher[] { new IdenticalSubtreeMatcherThetaA(src, dst, store),
					new RtedMatcher(src, dst, store), new LcsOptMatcherThetaB(src, dst, store),
					new UnmappedLeavesMatcherThetaC(src, dst, store), new InnerNodesMatcherThetaD(src, dst, store),
					new LeafMoveMatcherThetaE(src, dst, store), new CrossMoveMatcherThetaF(src, dst, store)

			});
		}
	}

}
