package com.porphyriussoftware.module004;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utility class providing null-safe filtering and mapping operations for lists.
 * <p>
 * All methods return new lists and never modify the input list. Null inputs
 * result in empty output lists. Predicates and mapper functions are also
 * validated for null safety.
 * </p>
 */
public final class CollectionFilter {

    private CollectionFilter() {}

    /**
     * Returns a new list containing only the elements that satisfy the given
     * predicate.
     *
     * @param input     the input list, may be null
     * @param predicate the predicate used for filtering, may be null
     * @param <T>       the element type
     * @return a filtered list; returns an empty list if the input list or
     *         predicate is null
     */
    public static <T> List<T> filter(List<T> input, Predicate<T> predicate) {

        if(predicate==null) {
            return List.of();
        }
        return Optional.ofNullable(input)
            .orElse(List.of())
            .stream()
            .filter(predicate)
            .toList();
    }

    /**
     * Returns a new list containing the results of applying the mapper
     * function to each element of the input list.
     *
     * @param input  the input list, may be null
     * @param mapper the mapping function, may be null
     * @param <T>    the input element type
     * @param <R>    the output element type
     * @return a mapped list; returns an empty list if the input list or
     *         mapper is null
     */
    public static <T, R> List<R> map(List<T> input, Function<T, R> mapper) {
        if(mapper==null) {
            return List.of();
        }

        return Optional.ofNullable(input)
            .orElse(List.of())
            .stream()
            .map(mapper)
            .toList();
    }

    /**
     * Returns a new list produced by first filtering the input list using the
     * provided predicate, then mapping the remaining elements using the mapper.
     *
     * @param input     the input list, may be null
     * @param predicate the predicate used for filtering, may be null
     * @param mapper    the mapping function, may be null
     * @param <T>       the input element type
     * @param <R>       the output element type
     * @return a filtered and mapped list; returns an empty list if the input
     *         list, predicate, or mapper is null
     */
    public static <T, R> List<R> filterAndMap(
        List<T> input,
        Predicate<T> predicate,
        Function<T, R> mapper
    ) {
        if(input==null||predicate==null||mapper==null) {

            return List.of();
        }
        return map(filter(input, predicate), mapper);
    }
}
