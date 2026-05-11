package com.porphyriussoftware.module005;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Provides custom collectors used in Module 5 of the Java Reboot Training course.
 * <p>
 * This class focuses on demonstrating how to construct collectors using the
 * {@link Collector} interface, including the supplier, accumulator, combiner,
 * and finisher functions. The collectors defined here are intentionally left
 * without implementations so that students can complete them as part of the
 * module exercises.
 * </p>
 * <p>
 * All collectors must satisfy the requirements described in their respective
 * documentation blocks. Implementations must also comply with strict Javadoc
 * validation rules enforced by the Maven Javadoc Plugin configuration:
 * <ul>
 *     <li>{@code doclint=all}</li>
 *     <li>{@code -Xwerror}</li>
 *     <li>{@code failOnError=true}</li>
 * </ul>
 *
 */
public final class CustomCollectors {

    /**
     * Private constructor to prevent instantiation.
     */
    private CustomCollectors() {
    }

    /**
     * Returns a collector that accumulates input elements into an immutable {@link List}.
     * <p>
     * Requirements:
     * <ul>
     *     <li>Elements must be collected in encounter order.</li>
     *     <li>The resulting list must be unmodifiable.</li>
     *     <li>Attempts to modify the returned list must result in an
     *     {@link UnsupportedOperationException}.</li>
     * </ul>
     *
     * @param <T> the type of input elements
     * @return a collector that produces an immutable list
     */
    public static <T> Collector<T, ?, List<T>> toImmutableList() {
        return Collector
            .<T, ArrayList<T>, List<T>>of(
                ArrayList::new,
                List::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                },
                List::copyOf);

    }

    /**
     * Returns a collector that computes a frequency map of the input elements.
     * <p>
     * Requirements:
     * <ul>
     *     <li>Each distinct element becomes a key in the resulting map.</li>
     *     <li>The associated value is the number of occurrences of that element.</li>
     *     <li>The resulting map does not need to preserve encounter order.</li>
     * </ul>
     *
     * @param <T> the type of input elements
     * @return a collector that produces a map of element frequencies
     */
    public static <T> Collector<T, ?, Map<T, Long>> frequencyMap() {
        return Collector
            .<T, Map<T, Long>, Map<T, Long>>of(
                HashMap::new,
//                (map, element) -> { //Leaving this here, to remind me of what I first thought for this solution
//                    if (!map.containsKey(element)) {
//                        map.put(element, 1L);
//                    } else {
//                        map.put(element, map.get(element) + 1);
//                    }
//                },
                (map, value) -> map.merge(value, 1L, Long::sum),
                (left, right) -> {
                    right.forEach((k, v) -> left.merge(k, v, Long::sum));
                    return left;
                },
                Function.identity()
            );
    }

    /**
     * Returns a collector that groups strings by their length.
     * <p>
     * Requirements:
     * <ul>
     *     <li>The key of the resulting map is the string length.</li>
     *     <li>The value is a list of all strings with that length.</li>
     *     <li>Encounter order must be preserved within each list.</li>
     * </ul>
     *
     * @return a collector that groups strings by length
     */
    public static Collector<String, ?, Map<Integer, List<String>>> groupByLength() {
        return Collector.<String,Map<Integer, List<String>>, Map<Integer, List<String>>>of(
            HashMap::new,
            (map, element) -> {
                int length = element.length();
//                if (!map.containsKey(length)) { //Leaving this here, because new map methods do all this work. Those Java guys are making us lazy?
//                    List<String> list = new ArrayList<>();
//                    list.add(element);
//                    map.put(length, list);
//                } else {
//                    map.get(length).add(element);
//                }
                map.computeIfAbsent(length, k -> new ArrayList<>()).add(element);

            },
            (left, right) -> {
                right.forEach((k, v) ->
                    left.merge(k, v, (strings, strings2) -> {
                        strings.addAll(strings2);
                        return strings;
                    }));
                return left;
            },
            Function.identity()
        );
    }

    /**
     * Returns a collector that joins strings using a dash ({@code "-"}).
     * <p>
     * Requirements:
     * <ul>
     *     <li>Input {@code ["a", "b", "c"]} must produce {@code "a-b-c"}.</li>
     *     <li>No trailing dash may appear in the result.</li>
     *     <li>An empty stream must produce an empty string.</li>
     * </ul>
     *
     * @return a collector that joins strings with a dash
     */
    public static Collector<String, ?, String> joiningWithDash() {
        return Collector.of(
            () -> new StringJoiner("-"),
            StringJoiner::add,                     // Accumulator (Consumer)
            StringJoiner::merge,
            StringJoiner::toString
        );
    }
}
