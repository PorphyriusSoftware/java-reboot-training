package com.porphyriussoftware.module002;

import java.util.Locale;
import java.util.Optional;

/**
 * Utility class providing null-safe string normalization using Java Optional.
 * <p>
 * This class centralizes trimming, blank-checking, and uppercase transformation
 * to ensure consistent behavior across all functional pipelines.
 *
 * <p>This class is not meant to be instantiated.</p>
 */
public final class OptionalUtils {

    private OptionalUtils() {}

    /**
     * Cleans a string by trimming whitespace and returning a default value when
     * the input is null or blank.
     *
     * @param input        the raw input string, may be null
     * @param defaultValue the fallback value returned when input is null or blank
     * @return a trimmed non-blank string, or {@code defaultValue} when input is null/blank
     */
    public static String clean(String input, String defaultValue) {
        return Optional.ofNullable(input)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .orElse(defaultValue);
    }

    /**
     * Processes a string by trimming whitespace, validating non-blank content,
     * and converting it to uppercase using {@link Locale#ROOT}.
     * <p>
     * Returns {@code null} when the input is null or blank.
     *
     * @param input the raw input string, may be null
     * @return an uppercased non-blank string, or {@code null} when input is null/blank
     */
    public static String process(String input) {
        return Optional.ofNullable(input)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(s->s.toUpperCase(Locale.ROOT))
            .orElse(null);
    }
}

