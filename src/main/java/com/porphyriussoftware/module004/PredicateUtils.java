package com.porphyriussoftware.module004;

import java.util.function.Predicate;

/**
 * Utility class providing null-safe predicate composition helpers.
 * <p>
 * All methods in this class return new {@link java.util.function.Predicate}
 * instances that combine or transform the provided predicates. Null inputs
 * never cause exceptions; instead, safe fallback predicates are returned.
 * </p>
 */
public final class PredicateUtils {

    private PredicateUtils() {
    }

    /**
     * Returns a predicate that evaluates to {@code true} only when both
     * provided predicates evaluate to {@code true}.
     *
     * @param first  the first predicate, may be null
     * @param second the second predicate, may be null
     * @param <T>    the input type
     * @return a combined predicate, or a predicate that always returns false
     * if either input predicate is null
     */
    public static <T> Predicate<T> and(Predicate<T> first, Predicate<T> second) {
        return t -> {
            if (first == null || second == null) {
                return false;
            }
            return first.test(t) && second.test(t);
        };
    }

    /**
     * Returns a predicate that evaluates to {@code true} when at least one
     * of the provided predicates evaluates to {@code true}.
     *
     * @param first  the first predicate, may be null
     * @param second the second predicate, may be null
     * @param <T>    the input type
     * @return a combined predicate; if both predicates are null, returns a
     * predicate that always returns false
     */
    public static <T> Predicate<T> or(Predicate<T> first, Predicate<T> second) {
        return t -> {
            if (first == null && second == null) {
                return false;
            }
            if (first == null) {
                return second.test(t);
            }
            if (second == null) {
                return first.test(t);
            }
            return first.test(t) || second.test(t);
        };
    }

    /**
     * Returns a predicate that negates the result of the provided predicate.
     *
     * @param predicate the predicate to negate, may be null
     * @param <T>       the input type
     * @return a negated predicate; if the input predicate is null, returns a
     * predicate that always returns true
     */
    public static <T> Predicate<T> not(Predicate<T> predicate) {
        if (predicate == null) {
            return t -> true;
        }
        return predicate.negate();
    }

    /**
     * Returns a predicate that evaluates to {@code true} only when exactly
     * one of the provided predicates evaluates to {@code true}.
     *
     * @param first  the first predicate, may be null
     * @param second the second predicate, may be null
     * @param <T>    the input type
     * @return an XOR predicate; if either predicate is null, returns a
     * predicate that always returns false
     */
    public static <T> Predicate<T> xor(Predicate<T> first, Predicate<T> second) {
        return t -> {
            if (first == null || second == null) {
                return false;
            }

            if (first.test(t) && !second.test(t)) {
                return true;
            }

            if (!first.test(t) && second.test(t)) {
                return true;
            }

            return false;
        };
    }

    /**
     * Returns a predicate that always evaluates to {@code true}.
     *
     * @param <T> the input type
     * @return a predicate that always returns true
     */
    public static <T> Predicate<T> alwaysTrue() {
        return t -> true;
    }

    /**
     * Returns a predicate that always evaluates to {@code false}.
     *
     * @param <T> the input type
     * @return a predicate that always returns false
     */
    public static <T> Predicate<T> alwaysFalse() {
        return t -> false;
    }
}
