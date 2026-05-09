package com.porphyriussoftware;


import java.util.List;
import java.util.Objects;

/**
 * A composed numeric processing pipeline that transforms a list of string
 * inputs into a list of doubled, clamped, positive integers. This class
 * orchestrates the lower-level utilities in {@link NumberUtils} and
 * {@link NumberStreamUtils} to provide a clean, functional workflow.
 *
 * <p>The processing pipeline performs the following steps:</p>
 *
 * <ol>
 *     <li>Parse each string into an {@link Integer} using
 *         {@link NumberUtils#safeParse(String)}.</li>
 *     <li>Filter out {@code null} values and values that are not strictly
 *         within the inclusive range [{@code min}, {@code max}].</li>
 *     <li>Clamp the remaining values using
 *         {@link NumberUtils#clamp(Integer, Integer, Integer)}.</li>
 *     <li>Double each clamped value using
 *         {@link NumberStreamUtils#doubleAll(java.util.List)}.</li>
 *     <li>Return the transformed list.</li>
 * </ol>
 *
 * <p>Values outside the allowed range are discarded <em>before</em> clamping.
 * Clamping is not used to rescue invalid inputs. This ensures predictable,
 * domain-safe behavior and avoids silent coercion.</p>
 *
 * <p>This class is not meant to be instantiated.</p>
 */
public class SmartNumberFilter {

    private SmartNumberFilter() {}

    /**
     * Processes the given list of string inputs through the numeric pipeline.
     * <p>
     * Steps:
     * <ul>
     *     <li>Parse strings into integers.</li>
     *     <li>Filter out nulls and values outside the inclusive range
     *         [{@code min}, {@code max}].</li>
     *     <li>Clamp remaining values.</li>
     *     <li>Double the clamped values.</li>
     * </ul>
     *
     * <p>If the input list is {@code null}, an empty list is returned.</p>
     *
     * @param values the list of string inputs to process, may be null
     * @param min the lower bound for valid values, may be null
     * @param max the upper bound for valid values, may be null
     * @return a list of doubled, clamped integers that were originally
     *         within the allowed range; never {@code null}
     */
    public final static List<Integer> process(List<String> values, Integer min, Integer max) {

        if(values==null || min==null || max==null){
            return List.of();
        }

        return NumberStreamUtils.doubleAll(values
            .stream()
            .map(NumberUtils::safeParse)
            .filter(Objects::nonNull)
            .filter(e -> e >= min && e <= max)
            .map(n -> NumberUtils.clamp(n, min, max))
            .toList());
    }
}
