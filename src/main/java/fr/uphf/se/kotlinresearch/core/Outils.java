package fr.uphf.se.kotlinresearch.core;

import java.io.File;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

/**
 * 
 * @author Matias Martinez
 *
 */
public class Outils {

	public static void print(String tab, ITree node, TreeContext context) {

		System.out.println(tab + "-->" + node.toPrettyString(context));
		for (ITree c : node.getChildren()) {
			print(tab + "-", c, context);
		}

	}

	public static boolean isEmpty(Object content) {

		return content == null || content.toString().trim().isEmpty() || "null".equals(content.toString().trim());
	}

	public static String getFileName(String completeFileName) {
		int idx = completeFileName.lastIndexOf(File.separator);
		if (idx >= 0)
			return completeFileName.substring(idx + 1);
		return completeFileName;
	}

}
