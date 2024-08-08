package com.coledit.backend.helpers;

import com.github.difflib.patch.PatchFailedException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StringMergerTest {

    @Test
    void testMergeVariantsBasic() throws PatchFailedException {
        String original = "abcd";
        List<String> variants = List.of("cde", "acd");
        String expected = "cde";

        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testMergeVariantsWithConflicts() throws PatchFailedException {
        String original = "abcdef";
        List<String> variants = List.of("abcfef", "abdef");

        String expected = "abfef";

        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testMergeVariantsEmptyOriginal() throws PatchFailedException {
        String original = "";
        List<String> variants = List.of("abc", "def");
        String expected = "abcdef";

        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testMergeVariantsEmptyVariants() throws PatchFailedException {
        String original = "abcd";
        List<String> variants = List.of();
        String expected = "abcd";

        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testMergeVariantsIdenticalVariants() throws PatchFailedException {
        String original = "abcd";
        List<String> variants = List.of("abcd", "abcd");
        String expected = "abcd";

        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testMergeVariantsSingleVariant() throws PatchFailedException {
        String original = "abcd";
        List<String> variants = List.of("abef");
        String expected = "abef";

        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testMergeVariantsComplex1() throws PatchFailedException {
        String original = "abcdefgh";
        List<String> variants = List.of("abcfgh", "abdefgh", "abcdgh");
        String expected = "abdgh";

        // first one deletes `de`
        // second one deletes `c`
        // third one deletes `ef`

        // for `c` there is no conflict, but the deletion of `de` and `ef` is in conflict
        // and the algorithm always chooses the last edit if they have the same size
        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testMergeVariantsComplex2() throws PatchFailedException {
        String original = "abcdefgh";
        List<String> variants = List.of("abcgh", "abdefgh", "abcdgh");
        String expected = "abgh";

        // first one deletes `def`
        // second one deletes `c`
        // third one deletes `ef`

        // for `c` there is no conflict, but the deletion of `def` and `ef` is in conflict
        // and the algorithm always chooses the biggest edit
        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testMergeVariantsCompletelyDifferent() throws PatchFailedException {
        String original = "abcd";
        List<String> variants = List.of("wxyz", "mnop");
        String expected = "mnop";

        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testMergeVariantsOverlappingNonConflicting() throws PatchFailedException {
        String original = "abcdefgh";
        List<String> variants = List.of("abcxyz", "defghijk");
        String expected = "xyzijk";

        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testMergeVariantsSubsetsOfOriginal() throws PatchFailedException {
        String original = "abcdefgh";
        List<String> variants = List.of("abc", "def");
        String expected = "";

        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testMergeVariantsSupersetsOfOriginal() throws PatchFailedException {
        String original = "abc";
        List<String> variants = List.of("abcdef", "abcxyz");
        String expected = "abcdefxyz";

        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }


    @Test
    void testMergeVariantsDifferentLengths() throws PatchFailedException {
        String original = "abc";
        List<String> variants = List.of("a", "abcdef");
        String expected = "adef";

        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }
}
