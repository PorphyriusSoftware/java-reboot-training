package com.porphyriussoftware.module004;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class CollectionUtilitiesTest {

    // ------------------------------------------------------------
    // PredicateUtils tests
    // ------------------------------------------------------------

    @Test
    void alwaysTrueShouldAlwaysReturnTrue() {
        Predicate<Object> p = PredicateUtils.alwaysTrue();
        assertTrue(p.test(null));
        assertTrue(p.test("x"));
        assertTrue(p.test(123));
    }

    @Test
    void alwaysFalseShouldAlwaysReturnFalse() {
        Predicate<Object> p = PredicateUtils.alwaysFalse();
        assertFalse(p.test(null));
        assertFalse(p.test("x"));
        assertFalse(p.test(123));
    }

    @Test
    void andShouldReturnFalseIfEitherPredicateIsNull() {
        Predicate<String> p = PredicateUtils.and(null, s -> true);
        assertFalse(p.test("x"));

        p = PredicateUtils.and(s -> true, null);
        assertFalse(p.test("x"));
    }

    @Test
    void andShouldReturnTrueOnlyWhenBothAreTrue() {
        Predicate<Integer> p = PredicateUtils.and(i -> i > 0, i -> i < 10);
        assertTrue(p.test(5));
        assertFalse(p.test(-1));
        assertFalse(p.test(50));
    }

    @Test
    void orShouldReturnFalseIfBothAreNull() {
        Predicate<String> p = PredicateUtils.or(null, null);
        assertFalse(p.test("x"));
    }

    @Test
    void orShouldReturnTheOtherPredicateIfOneIsNull() {
        Predicate<String> p = PredicateUtils.or(null, s -> true);
        assertTrue(p.test("x"));

        p = PredicateUtils.or(s -> false, null);
        assertFalse(p.test("x"));
    }

    @Test
    void orShouldReturnTrueIfEitherPredicateIsTrue() {
        Predicate<Integer> p = PredicateUtils.or(i -> i > 10, i -> i < 0);
        assertTrue(p.test(20));
        assertTrue(p.test(-5));
        assertFalse(p.test(5));
    }

    @Test
    void notShouldNegatePredicate() {
        Predicate<String> p = PredicateUtils.not(s -> s.isEmpty());
        assertTrue(p.test("x"));
        assertFalse(p.test(""));
    }

    @Test
    void notShouldReturnAlwaysTrueIfPredicateIsNull() {
        Predicate<String> p = PredicateUtils.not(null);
        assertTrue(p.test("anything"));
    }

    @Test
    void xorShouldReturnTrueOnlyWhenExactlyOneIsTrue() {
        Predicate<Integer> p = PredicateUtils.xor(i -> i > 0, i -> i % 2 == 0);

        assertTrue(p.test(1));    // true XOR false
        assertFalse(p.test(2));   // true XOR true
        assertTrue(p.test(-2));   // false XOR true
        assertTrue(p.test(0));    // false XOR true
        assertFalse(p.test(-3));  // false XOR false
    }

    @Test
    void xorShouldReturnAlwaysFalseIfEitherPredicateIsNull() {
        Predicate<Integer> p = PredicateUtils.xor(null, i -> true);
        assertFalse(p.test(5));
    }


    // ------------------------------------------------------------
    // CollectionFilter tests
    // ------------------------------------------------------------

    @Test
    void filterShouldReturnEmptyListForNullInput() {
        assertTrue(CollectionFilter.filter(null, x -> true).isEmpty());
    }

    @Test
    void filterShouldReturnEmptyListForNullPredicate() {
        assertTrue(CollectionFilter.filter(List.of("a", "b"), null).isEmpty());
    }

    @Test
    void filterShouldReturnOnlyMatchingElements() {
        List<Integer> result = CollectionFilter.filter(List.of(1, 2, 3, 4), i -> i % 2 == 0);
        assertEquals(List.of(2, 4), result);
    }

    @Test
    void mapShouldReturnEmptyListForNullInput() {
        assertTrue(CollectionFilter.map(null, Object::toString).isEmpty());
    }

    @Test
    void mapShouldReturnEmptyListForNullMapper() {
        assertTrue(CollectionFilter.map(List.of("a", "b"), null).isEmpty());
    }

    @Test
    void mapShouldTransformElements() {
        List<Integer> result = CollectionFilter.map(List.of("a", "bbb"), String::length);
        assertEquals(List.of(1, 3), result);
    }

    @Test
    void filterAndMapShouldReturnEmptyListIfAnyArgumentIsNull() {
        assertTrue(CollectionFilter.filterAndMap(null, x -> true, Object::toString).isEmpty());
        assertTrue(CollectionFilter.filterAndMap(List.of("a"), null, Object::toString).isEmpty());
        assertTrue(CollectionFilter.filterAndMap(List.of("a"), x -> true, null).isEmpty());
    }

    @Test
    void filterAndMapShouldApplyFilterThenMap() {
        List<Integer> result = CollectionFilter.filterAndMap(
            List.of("a", "bbb", ""),
            s -> !s.isEmpty(),
            String::length
        );
        assertEquals(List.of(1, 3), result);
    }


    // ------------------------------------------------------------
    // SmartCollectionFilter tests
    // ------------------------------------------------------------

    @Test
    void smartFilterShouldReturnEmptyListForNullInput() {
        assertTrue(SmartCollectionFilter.process(null, x -> true, Object::toString).isEmpty());
    }

    @Test
    void smartFilterShouldReturnEmptyListForNullPredicate() {
        assertTrue(SmartCollectionFilter.process(List.of("a"), null, Object::toString).isEmpty());
    }

    @Test
    void smartFilterShouldReturnEmptyListForNullMapper() {
        assertTrue(SmartCollectionFilter.process(List.of("a"), x -> true, null).isEmpty());
    }

    @Test
    void smartFilterShouldApplyFilterThenMap() {
        List<Integer> result = SmartCollectionFilter.process(
            List.of("x", "", "abcd"),
            s -> !s.isEmpty(),
            String::length
        );
        assertEquals(List.of(1, 4), result);
    }

    @Test
    void smartFilterShouldHandleMixedGarbage() {
        List<String> input = Arrays.asList("a", "", "bbb", null, "cc");


        List<Integer> result = SmartCollectionFilter.process(
            input,
            s -> s != null && !s.isEmpty(),
            String::length
        );

        assertEquals(List.of(1, 3, 2), result);
    }
}
