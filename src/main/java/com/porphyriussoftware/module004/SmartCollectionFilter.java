package com.porphyriussoftware.module004;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A composed collection-processing pipeline that applies a predicate-based
 * filter followed by a mapping transformation. This class provides a higher-
 * level abstraction over {@link CollectionFilter}.
 * <p>
 * The pipeline order is always:
 * <ol>
 *     <li>filter the input list using the predicate</li>
 *     <li>map the filtered elements using the mapper</li>
 * </ol>
 * Null inputs never cause exceptions; instead, empty lists are returned.
 *
 */
public final class SmartCollectionFilter {

    private SmartCollectionFilter() {}

    /**
     * Processes the input list by filtering it with the provided predicate
     * and then mapping the filtered elements using the provided mapper.
     *
     * @param input     the input list, may be null
     * @param predicate the predicate used for filtering, may be null
     * @param mapper    the mapping function, may be null
     * @param <T>       the input element type
     * @param <R>       the output element type
     * @return a processed list; returns an empty list if the input list,
     *         predicate, or mapper is null
     */
    public static <T, R> List<R> process(
        List<T> input,
        Predicate<T> predicate,
        Function<T, R> mapper
    ) {
        return CollectionFilter.filterAndMap(input, predicate, mapper);
    }
}
