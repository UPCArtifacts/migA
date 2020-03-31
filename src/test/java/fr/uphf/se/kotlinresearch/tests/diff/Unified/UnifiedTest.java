package fr.uphf.se.kotlinresearch.tests.diff.Unified;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;

public class UnifiedTest {

	@Test
	public void testUnified() throws Exception {
		List<String> text1 = Arrays.asList("this is a test", "a test");
		List<String> text2 = Arrays.asList("this is a testfile", "a test");

		// generating diff information.
		Patch<String> diff = DiffUtils.diff(text1, text2);

		// generating unified diff format
		List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff("original-file.txt", "new-file.txt", text1,
				diff, 10);

		unifiedDiff.forEach(System.out::println);

		// importing unified diff format from file or here from memory to a Patch
		Patch<String> importedPatch = UnifiedDiffUtils.parseUnifiedDiff(unifiedDiff);

		// apply patch to original list
		// List<String> patchedText = DiffUtils.patch(text1, importedPatch);

		// System.out.println(patchedText);
	}

}
