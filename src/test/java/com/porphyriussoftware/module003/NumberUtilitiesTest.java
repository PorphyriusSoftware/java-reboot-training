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
        assertEquals(123, NumberUtils.safeParse("123"));
        assertEquals(-50, NumberUtils.safeParse("-50"));
    }

    @Test
    void safeParseShouldReturnNullForInvalidOrNullInput() {
        assertNull(NumberUtils.safeParse("abc"));
        assertNull(NumberUtils.safeParse("12x"));
        assertNull(NumberUtils.safeParse(null));
        assertNull(NumberUtils.safeParse(""));
    }

    @Test
    void isPositiveShouldHandleNullAndZero() {
        assertFalse(NumberUtils.isPositive(null));
        assertFalse(NumberUtils.isPositive(0));
        assertTrue(NumberUtils.isPositive(1));
        assertFalse(NumberUtils.isPositive(-1));
    }

    @Test
    void isNegativeShouldHandleNullAndZero() {
        assertFalse(NumberUtils.isNegative(null));
        assertFalse(NumberUtils.isNegative(0));
        assertTrue(NumberUtils.isNegative(-1));
        assertFalse(NumberUtils.isNegative(5));
    }

    @Test
    void clampShouldReturnNullWhenValueOrBoundsAreNull() {
        assertNull(NumberUtils.clamp(null, 1, 10));
        assertNull(NumberUtils.clamp(5, null, 10));
        assertNull(NumberUtils.clamp(5, 1, null));
        assertNull(NumberUtils.clamp(null, null, null));
    }

    @Test
    void clampShouldRespectBoundsWhenAllInputsAreValid() {
        assertEquals(5, NumberUtils.clamp(5, 1, 10));
        assertEquals(1, NumberUtils.clamp(-5, 1, 10));
        assertEquals(10, NumberUtils.clamp(50, 1, 10));
    }


    // ----------------------------------------------------
    // NumberStreamUtils tests
    // ----------------------------------------------------

    @Test
    void filterPositiveShouldIgnoreNullsAndReturnOnlyPositive() {
        List<Integer> input = Arrays.asList(-1, 0, 5, null, 10);
        List<Integer> expected = List.of(5, 10);

        assertEquals(expected, NumberStreamUtils.filterPositive(input));
    }

    @Test
    void filterPositiveShouldReturnEmptyListForNullInput() {
        assertTrue(NumberStreamUtils.filterPositive(null).isEmpty());
    }

    @Test
    void filterNegativeShouldIgnoreNullsAndReturnOnlyNegative() {
        List<Integer> input = Arrays.asList(-5, 0, 10, null, -1);
        List<Integer> expected = List.of(-5, -1);

        assertEquals(expected, NumberStreamUtils.filterNegative(input));
    }

    @Test
    void doubleAllShouldIgnoreNullsAndDoubleValues() {
        List<Integer> input = Arrays.asList(1, null, 3);
        List<Integer> expected = List.of(2, 6);

        assertEquals(expected, NumberStreamUtils.doubleAll(input));
    }

    @Test
    void doubleAllShouldReturnEmptyListForNullInput() {
        assertTrue(NumberStreamUtils.doubleAll(null).isEmpty());
    }

    @Test
    void sumShouldHandleNullsAndEmptyLists() {
        assertEquals(0, NumberStreamUtils.sum(null));
        assertEquals(0, NumberStreamUtils.sum(List.of()));
        assertEquals(6, NumberStreamUtils.sum(Arrays.asList(1, null, 5)));
    }

    @Test
    void averageShouldReturnNullForNullOrEmptyInput() {
        assertNull(NumberStreamUtils.average(null));
        assertNull(NumberStreamUtils.average(List.of()));
        assertNull(NumberStreamUtils.average(Arrays.asList(null, null)));
    }

    @Test
    void averageShouldComputeCorrectValue() {
        assertEquals(3.0, NumberStreamUtils.average(List.of(1, 2, 3, 4, 5)));
        assertEquals(2.5, NumberStreamUtils.average(Arrays.asList(2, null, 3)));
    }


    // ----------------------------------------------------
    // SmartNumberFilter tests
    // ----------------------------------------------------

    @Test
    void smartFilterShouldParseFilterClampAndDouble() {
        List<String> input = Arrays.asList("5", "-1", "20", "abc", "7");

        // Only 5 and 7 are within [1, 10]; 20 is discarded BEFORE clamping.
        List<Integer> expected = List.of(10, 14);

        assertEquals(expected, SmartNumberFilter.process(input, 1, 10));
    }

    @Test
    void smartFilterShouldReturnEmptyListForNullInput() {
        assertTrue(SmartNumberFilter.process(null, 1, 10).isEmpty());
    }

    @Test
    void smartFilterShouldReturnEmptyListWhenBoundsAreNull() {
        List<String> input = List.of("1", "2", "3");
        assertTrue(SmartNumberFilter.process(input, null, 10).isEmpty());
        assertTrue(SmartNumberFilter.process(input, 1, null).isEmpty());
        assertTrue(SmartNumberFilter.process(input, null, null).isEmpty());
    }

    @Test
    void smartFilterShouldDiscardValuesOutsideRangeBeforeClamping() {
        List<String> input = List.of("50", "100", "0", "-5", "9");

        // Only 9 is in range [1, 10]
        List<Integer> expected = List.of(18);

        assertEquals(expected, SmartNumberFilter.process(input, 1, 10));
    }

    @Test
    void smartFilterShouldHandleMixedGarbageInput() {
        List<String> input = Arrays.asList("x", "null", "", "  ", "-10", "3");

        List<Integer> expected = List.of(6); // only "3" survives

        assertEquals(expected, SmartNumberFilter.process(input, 1, 10));
    }
}
