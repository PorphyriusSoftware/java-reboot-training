package com.porphyriussoftware.module003;



/**
 * Utility methods for working with {@link Integer} values in a null‑safe,
 * functional‑pipeline‑friendly way. All methods avoid throwing exceptions
 * for invalid or missing input and instead return {@code null} or
 * {@code false} where appropriate.
 *
 * <p>This class is not meant to be instantiated.</p>
 */
public final class NumberUtils {

    private NumberUtils() {}

    /**
     * Attempts to parse the given string into an {@link Integer}.
     * <p>
     * If the input is {@code null} or cannot be parsed as a valid integer,
     * this method returns {@code null} instead of throwing an exception.
     * This makes it safe to use inside functional pipelines.
     *
     * @param value the string to parse, may be null
     * @return the parsed integer, or {@code null} if parsing fails
     */
    public static Integer safeParse(String value){
        if(value == null || value.isEmpty()){
            return null;
        }
        try{
           return Integer.valueOf(value);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * Determines whether the given number is strictly positive.
     * <p>
     * If the input is {@code null}, this method returns {@code false}.
     * This avoids null checks in calling code and keeps predicates simple.
     *
     * @param value the number to test, may be null
     * @return {@code true} if the value is non-null and greater than zero;
     *         {@code false} otherwise
     */
    public static boolean isPositive(Integer value){
        if(value == null){
            return false;
        }
        return value>0;

    }

    /**
     * Determines whether the given number is strictly negative.
     * <p>
     * If the input is {@code null}, this method returns {@code false}.
     *
     * @param value the number to test, may be null
     * @return {@code true} if the value is non-null and less than zero;
     *         {@code false} otherwise
     */
    public static boolean isNegative(Integer value) {
        if(value == null){
            return false;
        }
        return value<0;
    }

    /**
     * Clamps the given value to the inclusive range [{@code min}, {@code max}].
     * <p>
     * If {@code value} is {@code null}, or if either bound ({@code min} or
     * {@code max}) is {@code null}, this method returns {@code null}.
     * Clamping requires valid numeric bounds, and null inputs cannot be
     * compared safely.
     *
     * <p>This method does not throw exceptions and is safe to use inside
     * functional pipelines.</p>
     *
     * @param value the number to clamp, may be null
     * @param min the lower bound (inclusive), may be null
     * @param max the upper bound (inclusive), may be null
     * @return the clamped value, or {@code null} if the input or bounds are null
     */
    public static Integer clamp(Integer value, Integer min, Integer max){

        if(value == null || min == null || max == null){
            return null;
        }

        if(value >=min&& value<=max){
            return value;
        }


        if(value<min){
            return min;
        }

        return max;

    }
}
