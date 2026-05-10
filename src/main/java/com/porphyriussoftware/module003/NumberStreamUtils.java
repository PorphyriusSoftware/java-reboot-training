package com.porphyriussoftware.module003;

import com.porphyriussoftware.module002.StreamUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Functional stream-based utilities for working with lists of {@link Integer}
 * values in a null-safe and predictable way. All methods avoid throwing
 * exceptions for invalid input and instead return empty results or {@code null}
 * where appropriate. This makes the utilities safe to use inside composed
 * functional pipelines.
 *
 * <p>This class is not meant to be instantiated.</p>
 */
public final class NumberStreamUtils {

    private NumberStreamUtils() {}

    /**
     * Returns a new list containing only the strictly positive values from the
     * given input list. {@code null} values are ignored.
     *
     * <p>If the input list is {@code null}, an empty list is returned.</p>
     *
     * @param values the list of integers to filter, may be null
     * @return a list containing only positive integers, never {@code null}
     */
    public static List<Integer> filterPositive(List<Integer> values){

        return Optional
            .ofNullable(values)
            .orElse(List.of())
            .stream()
            .filter(NumberUtils::isPositive)
            .toList();
    }

    /**
     * Returns a new list containing only the strictly negative values from the
     * given input list. {@code null} values are ignored.
     *
     * <p>If the input list is {@code null}, an empty list is returned.</p>
     *
     * @param values the list of integers to filter, may be null
     * @return a list containing only negative integers, never {@code null}
     */
    public static List<Integer> filterNegative(List<Integer> values){
        return Optional
            .ofNullable(values)
            .orElse(List.of())
            .stream()
            .filter(NumberUtils::isNegative)
            .toList();
    }

    /**
     * Returns a new list where each non-null value in the input list is doubled.
     * {@code null} values are ignored and not included in the output.
     *
     * <p>If the input list is {@code null}, an empty list is returned.</p>
     *
     * @param values the list of integers to transform, may be null
     * @return a list of doubled values, never {@code null}
     */
    public static List<Integer> doubleAll(List<Integer> values){
        return Optional
            .ofNullable(values)
            .orElse(List.of())
            .stream()
            .filter(Objects::nonNull)
            .map(s->s*2)
            .toList();
    }

    /**
     * Computes the sum of all non-null values in the given list.
     *
     * <p>If the input list is {@code null} or contains no non-null values,
     * this method returns {@code 0}. This avoids the need for null checks
     * in calling code and keeps the method safe for use in pipelines.</p>
     *
     * @param values the list of integers to sum, may be null
     * @return the sum of all non-null integers in the list
     */
    public static Integer sum(List<Integer> values){
        return StreamUtils.sum(values);
    }

    /**
     * Computes the arithmetic average of all non-null values in the given list.
     *
     * <p>If the input list is {@code null} or contains no non-null values,
     * this method returns {@code null}. Returning {@code null} instead of
     * throwing an exception preserves functional-pipeline safety and avoids
     * division-by-zero errors.</p>
     *
     * @param values the list of integers to average, may be null
     * @return the average as a {@link Double}, or {@code null} if no values exist
     */
    public static Double average(List<Integer> values){
        List<Integer> cleanedValues = Optional.ofNullable(values)
            .orElse(List.of())
            .stream()
            .filter(Objects::nonNull)
            .toList();
        if(cleanedValues.isEmpty()){
            return null;
        }


        return (double) StreamUtils.sum(cleanedValues)/cleanedValues.size();
    }
}
