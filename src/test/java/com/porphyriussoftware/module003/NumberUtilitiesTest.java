package com.porphyriussoftware.module003;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NumberUtilitiesTest {

    // ----------------------------------------------------
    // NumberUtils tests
    // ----------------------------------------------------

    @Test
    void safeParseShouldReturnIntegerForValidInput() {
        System.out.println("BEFORE: inputs=\"123\" and \"-50\", safeParse: valid numeric strings");
        Integer r1 = NumberUtils.safeParse("123");
        Integer r2 = NumberUtils.safeParse("-50");
        System.out.println("AFTER:  safeParse(\"123\")=" + r1 + ", safeParse(\"-50\")=" + r2 + " (expected 123 and -50)");
        assertEquals(123, r1);
        assertEquals(-50, r2);
    }

    @Test
    void safeParseShouldReturnNullForInvalidOrNullInput() {
        System.out.println("BEFORE: inputs=\"abc\", \"12x\", null, \"\" — all non-parseable, safeParse: return null");
        Integer r1 = NumberUtils.safeParse("abc");
        Integer r2 = NumberUtils.safeParse("12x");
        Integer r3 = NumberUtils.safeParse(null);
        Integer r4 = NumberUtils.safeParse("");
        System.out.println("AFTER:  results=" + r1 + ", " + r2 + ", " + r3 + ", " + r4 + " (all expected null)");
        assertNull(r1);
        assertNull(r2);
        assertNull(r3);
        assertNull(r4);
    }

    @Test
    void isPositiveShouldHandleNullAndZero() {
        System.out.println("BEFORE: inputs=null, 0, 1, -1, isPositive: only strictly positive integers qualify");
        boolean rNull = NumberUtils.isPositive(null);
        boolean rZero = NumberUtils.isPositive(0);
        boolean rOne  = NumberUtils.isPositive(1);
        boolean rNeg  = NumberUtils.isPositive(-1);
        System.out.println("AFTER:  isPositive(null)=" + rNull + ", isPositive(0)=" + rZero
            + ", isPositive(1)=" + rOne + ", isPositive(-1)=" + rNeg
            + " (expected false, false, true, false)");
        assertFalse(rNull);
        assertFalse(rZero);
        assertTrue(rOne);
        assertFalse(rNeg);
    }

    @Test
    void isNegativeShouldHandleNullAndZero() {
        System.out.println("BEFORE: inputs=null, 0, -1, 5, isNegative: only strictly negative integers qualify");
        boolean rNull = NumberUtils.isNegative(null);
        boolean rZero = NumberUtils.isNegative(0);
        boolean rNeg  = NumberUtils.isNegative(-1);
        boolean rPos  = NumberUtils.isNegative(5);
        System.out.println("AFTER:  isNegative(null)=" + rNull + ", isNegative(0)=" + rZero
            + ", isNegative(-1)=" + rNeg + ", isNegative(5)=" + rPos
            + " (expected false, false, true, false)");
        assertFalse(rNull);
        assertFalse(rZero);
        assertTrue(rNeg);
        assertFalse(rPos);
    }

    @Test
    void clampShouldReturnNullWhenValueOrBoundsAreNull() {
        System.out.println("BEFORE: various null combinations for value/min/max, clamp: return null if any argument is null");
        Integer r1 = NumberUtils.clamp(null, 1, 10);
        Integer r2 = NumberUtils.clamp(5, null, 10);
        Integer r3 = NumberUtils.clamp(5, 1, null);
        Integer r4 = NumberUtils.clamp(null, null, null);
        System.out.println("AFTER:  clamp(null,1,10)=" + r1 + ", clamp(5,null,10)=" + r2
            + ", clamp(5,1,null)=" + r3 + ", clamp(null,null,null)=" + r4 + " (all expected null)");
        assertNull(r1);
        assertNull(r2);
        assertNull(r3);
        assertNull(r4);
    }

    @Test
    void clampShouldRespectBoundsWhenAllInputsAreValid() {
        System.out.println("BEFORE: clamp(5,1,10)=in range, clamp(-5,1,10)=below min, clamp(50,1,10)=above max");
        Integer r1 = NumberUtils.clamp(5, 1, 10);
        Integer r2 = NumberUtils.clamp(-5, 1, 10);
        Integer r3 = NumberUtils.clamp(50, 1, 10);
        System.out.println("AFTER:  clamp(5)=" + r1 + " (expected 5), clamp(-5)=" + r2
            + " (expected 1 — clamped to min), clamp(50)=" + r3 + " (expected 10 — clamped to max)");
        assertEquals(5, r1);
        assertEquals(1, r2);
        assertEquals(10, r3);
    }


    // ----------------------------------------------------
    // NumberStreamUtils tests
    // ----------------------------------------------------

    @Test
    void filterPositiveShouldIgnoreNullsAndReturnOnlyPositive() {
        List<Integer> input = Arrays.asList(-1, 0, 5, null, 10);
        System.out.println("BEFORE: input=" + input + ", filterPositive: keep only strictly positive, skip nulls and zero");
        List<Integer> result = NumberStreamUtils.filterPositive(input);
        System.out.println("AFTER:  result=" + result + " (expected [5, 10])");
        assertEquals(List.of(5, 10), result);
    }

    @Test
    void filterPositiveShouldReturnEmptyListForNullInput() {
        System.out.println("BEFORE: input=null, filterPositive: null guard");
        List<Integer> result = NumberStreamUtils.filterPositive(null);
        System.out.println("AFTER:  result=" + result + " (expected empty)");
        assertTrue(result.isEmpty());
    }

    @Test
    void filterNegativeShouldIgnoreNullsAndReturnOnlyNegative() {
        List<Integer> input = Arrays.asList(-5, 0, 10, null, -1);
        System.out.println("BEFORE: input=" + input + ", filterNegative: keep only strictly negative, skip nulls and zero");
        List<Integer> result = NumberStreamUtils.filterNegative(input);
        System.out.println("AFTER:  result=" + result + " (expected [-5, -1])");
        assertEquals(List.of(-5, -1), result);
    }

    @Test
    void doubleAllShouldIgnoreNullsAndDoubleValues() {
        List<Integer> input = Arrays.asList(1, null, 3);
        System.out.println("BEFORE: input=" + input + " (contains null), doubleAll: multiply each non-null element by 2");
        List<Integer> result = NumberStreamUtils.doubleAll(input);
        System.out.println("AFTER:  result=" + result + " (expected [2, 6] — null skipped)");
        assertEquals(List.of(2, 6), result);
    }

    @Test
    void doubleAllShouldReturnEmptyListForNullInput() {
        System.out.println("BEFORE: input=null, doubleAll: null guard");
        List<Integer> result = NumberStreamUtils.doubleAll(null);
        System.out.println("AFTER:  result=" + result + " (expected empty)");
        assertTrue(result.isEmpty());
    }

    @Test
    void sumShouldHandleNullsAndEmptyLists() {
        System.out.println("BEFORE: inputs=null, [], [1, null, 5], sum: skip nulls, handle empty and null list");
        int r1 = NumberStreamUtils.sum(null);
        int r2 = NumberStreamUtils.sum(List.of());
        int r3 = NumberStreamUtils.sum(Arrays.asList(1, null, 5));
        System.out.println("AFTER:  sum(null)=" + r1 + " (expected 0), sum([])=" + r2
            + " (expected 0), sum([1,null,5])=" + r3 + " (expected 6)");
        assertEquals(0, r1);
        assertEquals(0, r2);
        assertEquals(6, r3);
    }

    @Test
    void averageShouldReturnNullForNullOrEmptyInput() {
        System.out.println("BEFORE: inputs=null, [], [null, null], average: return null when no computable values exist");
        Double r1 = NumberStreamUtils.average(null);
        Double r2 = NumberStreamUtils.average(List.of());
        Double r3 = NumberStreamUtils.average(Arrays.asList(null, null));
        System.out.println("AFTER:  average(null)=" + r1 + ", average([])=" + r2
            + ", average([null,null])=" + r3 + " (all expected null)");
        assertNull(r1);
        assertNull(r2);
        assertNull(r3);
    }

    @Test
    void averageShouldComputeCorrectValue() {
        System.out.println("BEFORE: inputs=[1,2,3,4,5] and [2,null,3], average: skip nulls when computing mean");
        Double r1 = NumberStreamUtils.average(List.of(1, 2, 3, 4, 5));
        Double r2 = NumberStreamUtils.average(Arrays.asList(2, null, 3));
        System.out.println("AFTER:  average([1..5])=" + r1 + " (expected 3.0), average([2,null,3])=" + r2 + " (expected 2.5)");
        assertEquals(3.0, r1);
        assertEquals(2.5, r2);
    }


    // ----------------------------------------------------
    // SmartNumberFilter tests
    // ----------------------------------------------------

    @Test
    void smartFilterShouldParseFilterClampAndDouble() {
        List<String> input = Arrays.asList("5", "-1", "20", "abc", "7");
        System.out.println("BEFORE: input=" + input + ", process(1, 10): parse strings, discard out-of-range, double survivors");
        List<Integer> result = SmartNumberFilter.process(input, 1, 10);
        System.out.println("AFTER:  result=" + result + " (expected [10, 14] — 5→10, 7→14; -1/20/abc discarded)");
        assertEquals(List.of(10, 14), result);
    }

    @Test
    void smartFilterShouldReturnEmptyListForNullInput() {
        System.out.println("BEFORE: input=null, process(1, 10): null guard");
        List<Integer> result = SmartNumberFilter.process(null, 1, 10);
        System.out.println("AFTER:  result=" + result + " (expected empty)");
        assertTrue(result.isEmpty());
    }

    @Test
    void smartFilterShouldReturnEmptyListWhenBoundsAreNull() {
        List<String> input = List.of("1", "2", "3");
        System.out.println("BEFORE: input=" + input + ", process with null bounds: return empty if any bound is null");
        List<Integer> r1 = SmartNumberFilter.process(input, null, 10);
        List<Integer> r2 = SmartNumberFilter.process(input, 1, null);
        List<Integer> r3 = SmartNumberFilter.process(input, null, null);
        System.out.println("AFTER:  results=" + r1 + ", " + r2 + ", " + r3 + " (all expected empty)");
        assertTrue(r1.isEmpty());
        assertTrue(r2.isEmpty());
        assertTrue(r3.isEmpty());
    }

    @Test
    void smartFilterShouldDiscardValuesOutsideRangeBeforeClamping() {
        List<String> input = List.of("50", "100", "0", "-5", "9");
        System.out.println("BEFORE: input=" + input + ", process(1, 10): values outside [1,10] are discarded, not clamped");
        List<Integer> result = SmartNumberFilter.process(input, 1, 10);
        System.out.println("AFTER:  result=" + result + " (expected [18] — only 9 in range, doubled to 18)");
        assertEquals(List.of(18), result);
    }

    @Test
    void smartFilterShouldHandleMixedGarbageInput() {
        List<String> input = Arrays.asList("x", "null", "", "  ", "-10", "3");
        System.out.println("BEFORE: input=" + input + ", process(1, 10): unparseable strings and out-of-range values discarded");
        List<Integer> result = SmartNumberFilter.process(input, 1, 10);
        System.out.println("AFTER:  result=" + result + " (expected [6] — only \"3\" survives, doubled to 6)");
        assertEquals(List.of(6), result);
    }
}
