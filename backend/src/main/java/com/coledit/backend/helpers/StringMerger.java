package com.coledit.backend.helpers;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StringMerger {

    public static String mergeVariants(String original, List<String> variants) throws PatchFailedException {
        List<List<Character>> decomposedVariants = variants.stream().map(StringMerger::stringToList)
                .collect(Collectors.toList());
        return listToString(mergeListVariants(stringToList(original), decomposedVariants));
    }

    private static List<Character> stringToList(String string) {
        return string.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
    }

    private static String listToString(List<Character> list) {
        return list.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    private static List<Character> mergeListVariants(List<Character> original,
            List<List<Character>> variants)
            throws PatchFailedException {
        List<Patch<Character>> patches = variants.stream().map(variant -> DiffUtils.diff(original, variant))
                .collect(Collectors.toList());

        List<AbstractDelta<Character>> mergedDeltas = patches.stream().flatMap(patch -> patch.getDeltas().stream())
                .collect(Collectors.toList());

        // Resolve conflicts
        List<AbstractDelta<Character>> resolvedDeltas = resolveConflicts(mergedDeltas);

        // Create a new patch with the resolved deltas
        Patch<Character> mergedPatch = new Patch<>();
        resolvedDeltas.forEach(mergedPatch::addDelta);

        // Apply the merged patch to the original document
        List<Character> mergedDocument = new ArrayList<>(original);
        try {
            mergedDocument = DiffUtils.patch(mergedDocument, mergedPatch);
        } catch (PatchFailedException e) {
            System.err.println("Failed to apply merged patch: " + e.getMessage());
        }

        return mergedDocument;
    }

    private static List<AbstractDelta<Character>> resolveConflicts(List<AbstractDelta<Character>> deltas) {
        List<AbstractDelta<Character>> resolvedDeltas = new ArrayList<>();

        for (AbstractDelta<Character> delta : deltas) {
            boolean conflictResolved = false;

            for (AbstractDelta<Character> resolvedDelta : resolvedDeltas) {
                if (hasIntersection(delta, resolvedDelta)) {
                    // Resolve the conflict by taking either delta or resolvedDelta
                    AbstractDelta<Character> selectedDelta = selectDelta(delta, resolvedDelta);
                    resolvedDeltas.remove(resolvedDelta);
                    resolvedDeltas.add(selectedDelta);
                    conflictResolved = true;
                    break;
                }
            }

            if (!conflictResolved) {
                resolvedDeltas.add(delta);
            }
        }

        return resolvedDeltas;
    }

    private static boolean hasIntersection(AbstractDelta<Character> delta1, AbstractDelta<Character> delta2) {
        // checking if there is an intersection between the deltas
        int start1 = delta1.getSource().getPosition();
        int end1 = start1 + delta1.getSource().size();
        int start2 = delta2.getSource().getPosition();
        int end2 = start2 + delta2.getSource().size();

        return (start1 < end2 && end1 > start2) || (start2 < end1 && end2 > start1);
    }

    private static AbstractDelta<Character> selectDelta(AbstractDelta<Character> delta1,
            AbstractDelta<Character> delta2) {
        // selecting the delta with the bigger size
        return delta1.getSource().size() >= delta2.getSource().size() ? delta1 : delta2;
    }
}
