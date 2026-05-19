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
        System.out.println("BEFORE: predicate=alwaysTrue, test: null, \"x\", 123");
        boolean r1 = p.test(null);
        boolean r2 = p.test("x");
        boolean r3 = p.test(123);
        System.out.println("AFTER:  test(null)=" + r1 + ", test(\"x\")=" + r2 + ", test(123)=" + r3 + " (all expected true)");
        assertTrue(r1);
        assertTrue(r2);
        assertTrue(r3);
    }

    @Test
    void alwaysFalseShouldAlwaysReturnFalse() {
        Predicate<Object> p = PredicateUtils.alwaysFalse();
        System.out.println("BEFORE: predicate=alwaysFalse, test: null, \"x\", 123");
        boolean r1 = p.test(null);
        boolean r2 = p.test("x");
        boolean r3 = p.test(123);
        System.out.println("AFTER:  test(null)=" + r1 + ", test(\"x\")=" + r2 + ", test(123)=" + r3 + " (all expected false)");
        assertFalse(r1);
        assertFalse(r2);
        assertFalse(r3);
    }

    @Test
    void andShouldReturnFalseIfEitherPredicateIsNull() {
        System.out.println("BEFORE: and(null, alwaysTrue) and and(alwaysTrue, null): null predicate makes the whole AND false");
        Predicate<String> p1 = PredicateUtils.and(null, s -> true);
        Predicate<String> p2 = PredicateUtils.and(s -> true, null);
        boolean r1 = p1.test("x");
        boolean r2 = p2.test("x");
        System.out.println("AFTER:  and(null,true).test(\"x\")=" + r1 + ", and(true,null).test(\"x\")=" + r2 + " (both expected false)");
        assertFalse(r1);
        assertFalse(r2);
    }

    @Test
    void andShouldReturnTrueOnlyWhenBothAreTrue() {
        Predicate<Integer> p = PredicateUtils.and(i -> i > 0, i -> i < 10);
        System.out.println("BEFORE: predicate=and(i>0, i<10), test: 5 (both true), -1 (first false), 50 (second false)");
        boolean r1 = p.test(5);
        boolean r2 = p.test(-1);
        boolean r3 = p.test(50);
        System.out.println("AFTER:  test(5)=" + r1 + " (expected true), test(-1)=" + r2 + " (expected false), test(50)=" + r3 + " (expected false)");
        assertTrue(r1);
        assertFalse(r2);
        assertFalse(r3);
    }

    @Test
    void orShouldReturnFalseIfBothAreNull() {
        System.out.println("BEFORE: or(null, null): both predicates null — result is always false");
        Predicate<String> p = PredicateUtils.or(null, null);
        boolean result = p.test("x");
        System.out.println("AFTER:  or(null,null).test(\"x\")=" + result + " (expected false)");
        assertFalse(result);
    }

    @Test
    void orShouldReturnTheOtherPredicateIfOneIsNull() {
        System.out.println("BEFORE: or(null, alwaysTrue) and or(alwaysFalse, null): non-null predicate is used as-is");
        Predicate<String> p1 = PredicateUtils.or(null, s -> true);
        Predicate<String> p2 = PredicateUtils.or(s -> false, null);
        boolean r1 = p1.test("x");
        boolean r2 = p2.test("x");
        System.out.println("AFTER:  or(null,true).test(\"x\")=" + r1 + " (expected true), or(false,null).test(\"x\")=" + r2 + " (expected false)");
        assertTrue(r1);
        assertFalse(r2);
    }

    @Test
    void orShouldReturnTrueIfEitherPredicateIsTrue() {
        Predicate<Integer> p = PredicateUtils.or(i -> i > 10, i -> i < 0);
        System.out.println("BEFORE: predicate=or(i>10, i<0), test: 20 (first true), -5 (second true), 5 (both false)");
        boolean r1 = p.test(20);
        boolean r2 = p.test(-5);
        boolean r3 = p.test(5);
        System.out.println("AFTER:  test(20)=" + r1 + " (expected true), test(-5)=" + r2 + " (expected true), test(5)=" + r3 + " (expected false)");
        assertTrue(r1);
        assertTrue(r2);
        assertFalse(r3);
    }

    @Test
    void notShouldNegatePredicate() {
        Predicate<String> p = PredicateUtils.not(s -> s.isEmpty());
        System.out.println("BEFORE: predicate=not(isEmpty), test: \"x\" (not empty), \"\" (empty)");
        boolean r1 = p.test("x");
        boolean r2 = p.test("");
        System.out.println("AFTER:  not(isEmpty).test(\"x\")=" + r1 + " (expected true), not(isEmpty).test(\"\")=" + r2 + " (expected false)");
        assertTrue(r1);
        assertFalse(r2);
    }

    @Test
    void notShouldReturnAlwaysTrueIfPredicateIsNull() {
        System.out.println("BEFORE: not(null): null predicate negated — result is always true");
        Predicate<String> p = PredicateUtils.not(null);
        boolean result = p.test("anything");
        System.out.println("AFTER:  not(null).test(\"anything\")=" + result + " (expected true)");
        assertTrue(result);
    }

    @Test
    void xorShouldReturnTrueOnlyWhenExactlyOneIsTrue() {
        Predicate<Integer> p = PredicateUtils.xor(i -> i > 0, i -> i % 2 == 0);
        System.out.println("BEFORE: predicate=xor(i>0, i%2==0), test: 1(T,F), 2(T,T), -2(F,T), 0(F,T), -3(F,F)");
        boolean r1 = p.test(1);
        boolean r2 = p.test(2);
        boolean r3 = p.test(-2);
        boolean r4 = p.test(0);
        boolean r5 = p.test(-3);
        System.out.println("AFTER:  xor(1)=" + r1 + "(T), xor(2)=" + r2 + "(F), xor(-2)=" + r3 + "(T), xor(0)=" + r4 + "(T), xor(-3)=" + r5 + "(F) (expected T,F,T,T,F)");
        assertTrue(r1);
        assertFalse(r2);
        assertTrue(r3);
        assertTrue(r4);
        assertFalse(r5);
    }

    @Test
    void xorShouldReturnAlwaysFalseIfEitherPredicateIsNull() {
        System.out.println("BEFORE: xor(null, alwaysTrue): null predicate makes the whole XOR always false");
        Predicate<Integer> p = PredicateUtils.xor(null, i -> true);
        boolean result = p.test(5);
        System.out.println("AFTER:  xor(null,true).test(5)=" + result + " (expected false)");
        assertFalse(result);
    }


    // ------------------------------------------------------------
    // CollectionFilter tests
    // ------------------------------------------------------------

    @Test
    void filterShouldReturnEmptyListForNullInput() {
        System.out.println("BEFORE: input=null, filter: null guard");
        List<Object> result = CollectionFilter.filter(null, x -> true);
        System.out.println("AFTER:  result=" + result + " (expected empty)");
        assertTrue(result.isEmpty());
    }

    @Test
    void filterShouldReturnEmptyListForNullPredicate() {
        System.out.println("BEFORE: input=[\"a\",\"b\"], predicate=null, filter: null predicate guard");
        List<String> result = CollectionFilter.filter(List.of("a", "b"), null);
        System.out.println("AFTER:  result=" + result + " (expected empty)");
        assertTrue(result.isEmpty());
    }

    @Test
    void filterShouldReturnOnlyMatchingElements() {
        List<Integer> input = List.of(1, 2, 3, 4);
        System.out.println("BEFORE: input=" + input + ", filter: keep even numbers only");
        List<Integer> result = CollectionFilter.filter(input, i -> i % 2 == 0);
        System.out.println("AFTER:  result=" + result + " (expected [2, 4])");
        assertEquals(List.of(2, 4), result);
    }

    @Test
    void mapShouldReturnEmptyListForNullInput() {
        System.out.println("BEFORE: input=null, map: null guard");
        List<String> result = CollectionFilter.map(null, Object::toString);
        System.out.println("AFTER:  result=" + result + " (expected empty)");
        assertTrue(result.isEmpty());
    }

    @Test
    void mapShouldReturnEmptyListForNullMapper() {
        System.out.println("BEFORE: input=[\"a\",\"b\"], mapper=null, map: null mapper guard");
        List<Object> result = CollectionFilter.map(List.of("a", "b"), null);
        System.out.println("AFTER:  result=" + result + " (expected empty)");
        assertTrue(result.isEmpty());
    }

    @Test
    void mapShouldTransformElements() {
        List<String> input = List.of("a", "bbb");
        System.out.println("BEFORE: input=" + input + ", map: String::length");
        List<Integer> result = CollectionFilter.map(input, String::length);
        System.out.println("AFTER:  result=" + result + " (expected [1, 3])");
        assertEquals(List.of(1, 3), result);
    }

    @Test
    void filterAndMapShouldReturnEmptyListIfAnyArgumentIsNull() {
        System.out.println("BEFORE: filterAndMap with null input, null predicate, and null mapper — each should return empty");
        List<Object> r1 = CollectionFilter.filterAndMap(null, x -> true, Object::toString);
        List<Object> r2 = CollectionFilter.filterAndMap(List.of("a"), null, Object::toString);
        List<Object> r3 = CollectionFilter.filterAndMap(List.of("a"), x -> true, null);
        System.out.println("AFTER:  nullInput=" + r1 + ", nullPredicate=" + r2 + ", nullMapper=" + r3 + " (all expected empty)");
        assertTrue(r1.isEmpty());
        assertTrue(r2.isEmpty());
        assertTrue(r3.isEmpty());
    }

    @Test
    void filterAndMapShouldApplyFilterThenMap() {
        List<String> input = List.of("a", "bbb", "");
        System.out.println("BEFORE: input=" + input + ", filterAndMap: keep non-empty, then map to length");
        List<Integer> result = CollectionFilter.filterAndMap(input, s -> !s.isEmpty(), String::length);
        System.out.println("AFTER:  result=" + result + " (expected [1, 3] — empty string filtered out)");
        assertEquals(List.of(1, 3), result);
    }


    // ------------------------------------------------------------
    // SmartCollectionFilter tests
    // ------------------------------------------------------------

    @Test
    void smartFilterShouldReturnEmptyListForNullInput() {
        System.out.println("BEFORE: input=null, SmartCollectionFilter.process: null guard");
        List<Object> result = SmartCollectionFilter.process(null, x -> true, Object::toString);
        System.out.println("AFTER:  result=" + result + " (expected empty)");
        assertTrue(result.isEmpty());
    }

    @Test
    void smartFilterShouldReturnEmptyListForNullPredicate() {
        System.out.println("BEFORE: input=[\"a\"], predicate=null, SmartCollectionFilter.process: null predicate guard");
        List<Object> result = SmartCollectionFilter.process(List.of("a"), null, Object::toString);
        System.out.println("AFTER:  result=" + result + " (expected empty)");
        assertTrue(result.isEmpty());
    }

    @Test
    void smartFilterShouldReturnEmptyListForNullMapper() {
        System.out.println("BEFORE: input=[\"a\"], mapper=null, SmartCollectionFilter.process: null mapper guard");
        List<Object> result = SmartCollectionFilter.process(List.of("a"), x -> true, null);
        System.out.println("AFTER:  result=" + result + " (expected empty)");
        assertTrue(result.isEmpty());
    }

    @Test
    void smartFilterShouldApplyFilterThenMap() {
        List<String> input = List.of("x", "", "abcd");
        System.out.println("BEFORE: input=" + input + ", process: keep non-empty, then map to length");
        List<Integer> result = SmartCollectionFilter.process(input, s -> !s.isEmpty(), String::length);
        System.out.println("AFTER:  result=" + result + " (expected [1, 4] — empty string filtered out)");
        assertEquals(List.of(1, 4), result);
    }

    @Test
    void smartFilterShouldHandleMixedGarbage() {
        List<String> input = Arrays.asList("a", "", "bbb", null, "cc");
        System.out.println("BEFORE: input=" + input + " (contains null and empty), process: filter non-null non-empty, map to length");
        List<Integer> result = SmartCollectionFilter.process(
            input,
            s -> s != null && !s.isEmpty(),
            String::length
        );
        System.out.println("AFTER:  result=" + result + " (expected [1, 3, 2] — null and empty skipped)");
        assertEquals(List.of(1, 3, 2), result);
    }
}