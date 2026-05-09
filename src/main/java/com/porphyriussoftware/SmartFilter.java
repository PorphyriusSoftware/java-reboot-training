package com.porphyriussoftware;

import java.util.List;


/**
 * High-level functional pipeline combining {@link StreamUtils} and {@link OptionalUtils}.
 * <p>
 * This class demonstrates functional composition:
 * <ol>
 *     <li>Clean and normalize values</li>
 *     <li>Filter by prefix</li>
 *     <li>Uppercase results</li>
 * </ol>
 */
public class SmartFilter {

    private SmartFilter() {}

    /**
     * Cleans, filters, and uppercases values in a single composed pipeline.
     * <p>
     * Behavior:
     * <ul>
     *     <li>null list → empty list</li>
     *     <li>null/blank elements → removed</li>
     *     <li>prefix matching is case-sensitive</li>
     * </ul>
     *
     * @param input  the list of raw strings, may be null or contain nulls
     * @param filter the prefix to match (case-sensitive)
     * @return an immutable list of cleaned, filtered, uppercased values
     */
    public static List<String> cleanAndFilter(List<String> input, String filter) {
        return StreamUtils.uppercaseAll(StreamUtils.filterStartingWith(input, filter));
    }
}
