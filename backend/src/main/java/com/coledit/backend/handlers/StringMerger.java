package com.coledit.backend.handlers;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.ChangeDelta;
import com.github.difflib.patch.Chunk;
import com.github.difflib.patch.DeleteDelta;
import com.github.difflib.patch.InsertDelta;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class StringMerger {
    // public static void main(String[] args) throws PatchFailedException {
    // System.out.println("Beginning time");

    // long startTime = System.nanoTime(); // Start timing

    // System.out.println(ContentMerger.mergeVariants("abcd", List.of("cde",
    // "acd")));

    // long endTime = System.nanoTime(); // End timing

    // System.out.println("End time");

    // // Calculate duration
    // long duration = endTime - startTime;
    // System.out.println("Duration in nanoseconds: " + duration);
    // System.out.println("Duration in milliseconds: " + duration / 1000000.0); //
    // Convert nanoseconds to milliseconds

    // // Optionally, print duration in seconds
    // System.out.println("Duration in seconds: " + duration / 1000000000.0); //
    // Convert nanoseconds to seconds
    // }

    public static String mergeVariants(String original, List<String> variants) throws PatchFailedException {
        List<List<Character>> decomposedVariants = variants.stream().map(ContentMerger::stringToList)
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
        System.out.println("merged");
        mergedDeltas.forEach(System.out::println);

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
        Map<String, AbstractDelta<Character>> deltaMap = new HashMap<>();

        for (AbstractDelta<Character> delta : deltas) {
            String key = delta.getSource().getPosition() + "-" + delta.getTarget().getPosition();
            if (!deltaMap.containsKey(key)) {
                deltaMap.put(key, delta);
            }
        }

        return new ArrayList<>(deltaMap.values());
    }

}
