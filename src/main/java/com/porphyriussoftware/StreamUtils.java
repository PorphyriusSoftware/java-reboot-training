package com.porphyriussoftware;

import java.util.*;

/**
 * Utility class providing functional list transformations using Java Streams.
 * <p>
 * This implementation intentionally reuses {@link OptionalUtils} to normalize
 * values before filtering or mapping, ensuring consistent behavior across modules.
 */
public class StreamUtils{

    private StreamUtils() {}

    /**
     * Returns all cleaned, non-blank strings that start with the given prefix.
     * <p>
     * Behavior:
     * <ul>
     *     <li>null list → treated as empty</li>
     *     <li>null elements → cleaned to empty string and removed</li>
     *     <li>values are normalized using {@link OptionalUtils#clean(String, String)}</li>
     * </ul>
     *
     * @param input         the list of raw strings, may be null or contain nulls
     * @param startingWith  the prefix to match (case-sensitive)
     * @return an immutable list of cleaned strings starting with the prefix
     */
    public static List<String> filterStartingWith(List<String> input, String startingWith) {
        //TODO:should it be case insensitive?
        return Optional.ofNullable(input)
            .orElse(List.of())
            .stream()
            .map(s->OptionalUtils.clean(s,""))
            .filter(s->!s.isEmpty())
            .filter(s -> s.startsWith(startingWith))
            .toList();
    }

    /**
     * Uppercases all non-null, non-blank values using {@link OptionalUtils#process(String)}.
     * <p>
     * Behavior:
     * <ul>
     *     <li>null list → empty list</li>
     *     <li>null elements → removed</li>
     *     <li>blank elements → removed</li>
     * </ul>
     *
     * @param input the list of raw strings, may be null or contain nulls
     * @return an immutable list of uppercase strings
     */
    public static List<String> uppercaseAll(List<String> input) {
        return Optional.ofNullable(input)
            .orElse(List.of())
            .stream()
            .map(OptionalUtils::process)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * Computes the sum of all integers in the list.
     * <p>
     * Behavior:
     * <ul>
     *     <li>null list → returns 0</li>
     *     <li>empty list → returns 0</li>
     * </ul>
     *
     * @param input the list of integers, may be null
     * @return the sum of all values, or 0 when input is null/empty
     */
    public static Integer sum(List<Integer> input) {
        return Optional.ofNullable(input)
            .orElse(List.of())
            .stream()
            .mapToInt(Integer::intValue)
            .sum();
    }
}
