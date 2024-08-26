package com.coledit.backend.helpers;

import com.github.difflib.patch.PatchFailedException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StringMergerTest {

    @Test
    void testSimpleConflict() throws PatchFailedException {
        String original = "abcd";
        List<String> variants = List.of("cde", "acd");
        String expected = "cde";

        // first one deletes `ab`, adds `e`
        // second one deletes `b`

        // the conflict is between the deletion of `b` and `ab`, the longer one is
        // chosen by the algorithm
        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testSimpleConflictReversed() throws PatchFailedException {
        String original = "abcd";
        List<String> variants = List.of("acd", "cde");
        String expected = "cde";

        // first one deletes `b`
        // second one deletes `ab`, adds `e`

        // the conflict is between the deletion of `b` and `ab`, the longer one is
        // chosen by the algorithm
        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testSimpleNonConflict() throws PatchFailedException {
        String original = "abcdef";
        List<String> variants = List.of("abcfef", "abdef");
        String expected = "abfef";

        // first one changes `d` to a `f`
        // second one deletes `c`

        // the opperations are non-conflicting, both are kept
        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testMergeVariantsEmptyOriginal() throws PatchFailedException {
        String original = "";
        List<String> variants = List.of("abc", "def");
        String expected = "abcdef";

        // first one inserts `abc`
        // second one deletes `def`

        // insert is a non-conflicting operation, thus both are appended to the
        // original,
        // the order of appending is the same as the order in the variants list
        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testMergeVariantsEmptyVariants() throws PatchFailedException {
        String original = "abcd";
        List<String> variants = List.of();
        String expected = "abcd";

        // no one does any modifications to the original string
        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testMergeVariantsIdenticalVariants() throws PatchFailedException {
        String original = "abcd";
        List<String> variants = List.of("abcd", "abcd");
        String expected = "abcd";

        // first one leaves the string as is
        // second one also does the same
        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testMergeVariantsSingleVariant() throws PatchFailedException {
        String original = "abcd";
        List<String> variants = List.of("abef");
        String expected = "abef";

        // first one changes `cd` to `ef`
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

        // for `c` there is no conflict, but the deletion of `de` and `ef` is in
        // conflict
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

        // for `c` there is no conflict, but the deletion of `def` and `ef` is in
        // conflict
        // and the algorithm always chooses the biggest edit
        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testMergeVariantsCompletelyDifferent() throws PatchFailedException {
        String original = "abcd";
        List<String> variants = List.of("wxyz", "mnop");
        String expected = "mnop";

        // first one changes everything to `wxyz`
        // second one changes everything to `mnop``

        // algorithms chooses last one in the list variants
        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testMergeVariantsOverlappingNonConflicting() throws PatchFailedException {
        String original = "abcdefgh";
        List<String> variants = List.of("abcxyz", "defghijk");
        String expected = "xyzijk";

        // first one changes `defgh` to `xyz`
        // second one deletes `abc` and adds `ijk` at the end

        // no conflicts, everything is kept
        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testMergeVariantsOverlappingNoConflict() throws PatchFailedException {
        String original = "abcdefgh";
        List<String> variants = List.of("abcxyztr", "defghijk");
        String expected = "xyztrijk";

        // first one changes `defgh` to `xyz`, adds `tr` at the end
        // second one deletes `abc` and adds `ijk` at the end

        // no conflicts, everything is kept
        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testMergeVariantsOverlappingSmallConflict() throws PatchFailedException {
        String original = "abcdefgh";
        List<String> variants = List.of("abcxyz", "ghijk");
        String expected = "ghijk";

        // first one changes `defgh` to `xyz`
        // second one deletes `abcdef` and adds `ijk` at the end

        // `abcdef` is bigger in size than `defgh` being conflicted, only first
        // is kept
        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testMergeVariantsSubsetsOfOriginal() throws PatchFailedException {
        String original = "abcdefgh";
        List<String> variants = List.of("abc", "def");
        String expected = "";

        // first one deletes `defgh`
        // second one deletes `abc` and `gh`

        // `defgh` and `gh` in conflict, bigger change is chosen
        // along non-conflicting delete of `abc`
        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testMergeVariantsSupersetsOfOriginal() throws PatchFailedException {
        String original = "abc";
        List<String> variants = List.of("abcdef", "abcxyz");
        String expected = "abcdefxyz";

        // first one inserts `def`
        // second one inserts `xyz`

        // insert is non conflicting, both are appened based on the order
        // in the variants list
        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }

    @Test
    void testMergeDifferentLengthChanges() throws PatchFailedException {
        String original = "abc";
        List<String> variants = List.of("a", "abcdef");
        String expected = "adef";

        // first one deletes `bc`
        // second one adds `def`

        // non conflicting, all are kept
        String result = StringMerger.mergeVariants(original, variants);

        assertEquals(expected, result);
    }
}
