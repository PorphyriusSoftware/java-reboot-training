package com.porphyriussoftware.module007;

// =============================================================================
// Module 7 — CustomPipeline Test Suite
//
// Basic Test Suite (Gohan pre-training with Piccolo)
//   Validates fundamentals: filter, map, flatMap, toList, forEach, count,
//   findFirst, lazy evaluation, and empty pipeline behavior.
//
// Ultra Instinct Test Suite (the real fight) — to be added
//   Will validate: pipeline fusion (no intermediate collections), short-circuit
//   correctness, parallel execution, stateful stage correctness, and
//   cancellation propagation.
//
// A passing basic suite means the pipeline fundamentals work.
// A passing Ultra Instinct suite means the implementation is production-grade.
// =============================================================================

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class CustomPipelineTest {

    // =========================================================================
    // Basic Test Suite — Gohan pre-training with Piccolo
    // =========================================================================

    @Test
    void testFilterRemovesNonMatchingElements() {
        List<Integer> source = List.of(1, 2, 3, 4, 5, 6);

        System.out.println("BEFORE: source=" + source + ", filter: keep even numbers only");



        List<Integer> result = CustomPipeline.of(source)
            .filter(x -> x % 2 == 0)
            .toList();

        System.out.println("AFTER:  result=" + result + " (expected [2, 4, 6])");

        assertEquals(List.of(2, 4, 6), result,
            "filter must keep only elements matching the predicate. Got: " + result);
    }

    @Test
    void testMapTransformsAllElements() {
        List<Integer> source = List.of(1, 2, 3, 4, 5);

        System.out.println("BEFORE: source=" + source + ", map: multiply each by 10");

        List<Integer> result = CustomPipeline.of(source)
            .map(x -> x * 10)
            .toList();

        System.out.println("AFTER:  result=" + result + " (expected [10, 20, 30, 40, 50])");

        assertEquals(List.of(10, 20, 30, 40, 50), result,
            "map must apply the function to every element. Got: " + result);
    }

    @Test
    void testFilterAndMapCompose() {
        // This is pipeline fusion in action: filter and map are applied in a single pass.
        // No intermediate list is created between the two operations.
        List<Integer> source = List.of(1, 2, 3, 4, 5, 6);

        System.out.println("BEFORE: source=" + source + ", filter even, then map x*10");

        List<Integer> result = CustomPipeline.of(source)
            .filter(x -> x % 2 == 0)
            .map(x -> x * 10)
            .toList();

        System.out.println("AFTER:  result=" + result + " (expected [20, 40, 60])");

        assertEquals(List.of(20, 40, 60), result,
            "filter followed by map must compose correctly in a single fused pass. Got: " + result);
    }

    @Test
    void testFlatMapFlattensNestedCollections() {
        List<List<Integer>> source = List.of(
            List.of(1, 2),
            List.of(3, 4),
            List.of(5, 6)
        );

        System.out.println("BEFORE: source=" + source + ", flatMap: flatten inner lists");

        List<Integer> result = CustomPipeline.of(source)
            .flatMap(inner -> inner)
            .toList();

        System.out.println("AFTER:  result=" + result + " (expected [1, 2, 3, 4, 5, 6])");

        assertEquals(List.of(1, 2, 3, 4, 5, 6), result,
            "flatMap must flatten each inner collection into the output. Got: " + result);
    }

    @Test
    void testToListPreservesEncounterOrder() {
        List<Integer> source = List.of(5, 3, 1, 4, 2);

        System.out.println("BEFORE: source=" + source + ", toList: collect without reordering");

        List<Integer> result = CustomPipeline.of(source).toList();

        System.out.println("AFTER:  result=" + result + " (must match source order exactly)");

        assertEquals(source, result,
            "toList must preserve the original encounter order. Got: " + result);
    }

    @Test
    void testForEachVisitsAllElements() {
        List<Integer> source = List.of(10, 20, 30);
        List<Integer> visited = new ArrayList<>();

        System.out.println("BEFORE: source=" + source + ", forEach: collect visited elements");

        CustomPipeline.of(source).forEach(visited::add);

        System.out.println("AFTER:  visited=" + visited + " (expected [10, 20, 30])");

        assertEquals(source, visited,
            "forEach must visit every element in order. Got: " + visited);
    }

    @Test
    void testCountReturnsExactSize() {
        List<Integer> source = List.of(1, 2, 3, 4, 5);

        System.out.println("BEFORE: source=" + source + ", count: all elements");

        long count = CustomPipeline.of(source).count();

        System.out.println("AFTER:  count=" + count + " (expected 5)");

        assertEquals(5L, count,
            "count must return the exact number of elements. Got: " + count);
    }

    @Test
    void testCountAfterFilterReturnsMatchingSize() {
        List<Integer> source = List.of(1, 2, 3, 4, 5, 6, 7, 8);

        System.out.println("BEFORE: source=" + source + ", filter even, then count");

        long count = CustomPipeline.of(source)
            .filter(x -> x % 2 == 0)
            .count();

        System.out.println("AFTER:  count=" + count + " (expected 4 — elements 2,4,6,8)");

        assertEquals(4L, count,
            "count after filter must count only matching elements. Got: " + count);
    }

    @Test
    void testFindFirstReturnsFirstMatchingElement() {
        List<Integer> source = List.of(1, 2, 3, 4, 5);

        System.out.println("BEFORE: source=" + source + ", filter even, findFirst");

        Optional<Integer> result = CustomPipeline.of(source)
            .filter(x -> x % 2 == 0)
            .findFirst();

        System.out.println("AFTER:  result=" + result + " (expected Optional[2])");

        assertTrue(result.isPresent(), "findFirst must return a non-empty Optional when a match exists");
        assertEquals(2, result.get(),
            "findFirst must return the first matching element. Got: " + result.get());
    }

    @Test
    void testFindFirstOnEmptyPipelineReturnsEmpty() {
        List<Integer> source = List.of(1, 3, 5);

        System.out.println("BEFORE: source=" + source + ", filter even (no matches), findFirst");

        Optional<Integer> result = CustomPipeline.of(source)
            .filter(x -> x % 2 == 0)
            .findFirst();

        System.out.println("AFTER:  result=" + result + " (expected Optional.empty)");

        assertFalse(result.isPresent(),
            "findFirst must return an empty Optional when no element matches. Got: " + result);
    }

    @Test
    void testEmptySourceProducesEmptyResults() {
        List<Integer> source = List.of();

        System.out.println("BEFORE: source=[] (empty), running filter+map+toList");

        List<Integer> result = CustomPipeline.of(source)
            .filter(x -> x % 2 == 0)
            .map(x -> x * 10)
            .toList();

        System.out.println("AFTER:  result=" + result + " (expected [])");

        assertTrue(result.isEmpty(),
            "An empty source must produce an empty result regardless of operations. Got: " + result);
    }

    @Test
    void testLazyEvaluation() {
        // Pipeline operations must NOT execute until a terminal operation is called.
        // This test verifies that intermediate operations are purely lazy registrations.
        List<Integer> source = List.of(1, 2, 3);
        AtomicInteger mapCallCount = new AtomicInteger(0);

        System.out.println("BEFORE: building pipeline with map that counts invocations — NO terminal op yet");

        CustomPipeline<Integer> pipeline = CustomPipeline.of(source)
            .map(x -> {
                mapCallCount.incrementAndGet();
                return x * 2;
            });

        System.out.println("AFTER building pipeline: mapCallCount=" + mapCallCount.get() +
                           " (must be 0 — nothing should have run yet)");

        assertEquals(0, mapCallCount.get(),
            "map must not execute before a terminal operation is called. " +
            "Got mapCallCount=" + mapCallCount.get() + " — map ran eagerly, which breaks lazy evaluation");

        // Now trigger evaluation
        List<Integer> result = pipeline.toList();

        System.out.println("AFTER toList(): result=" + result +
                           ", mapCallCount=" + mapCallCount.get() +
                           " (must be 3 — one call per element)");

        assertEquals(3, mapCallCount.get(),
            "map must execute exactly once per element during terminal evaluation. " +
            "Got mapCallCount=" + mapCallCount.get());

        assertEquals(List.of(2, 4, 6), result,
            "toList result must reflect the mapped values. Got: " + result);
    }

    // =========================================================================
    // Ultra Instinct Test Suite — the real fight
    // =========================================================================

    // -------------------------------------------------------------------------
    // Pipeline fusion
    //
    // In a fused pipeline, map is only called on elements that survived filter —
    // not on every element. If map runs on all elements first and filter runs
    // second, that means an intermediate collection was created between them.
    // That is NOT fusion.
    // -------------------------------------------------------------------------

    @Test
    void testMapOnlyCalledOnElementsThatPassFilter() {
        List<Integer> source = List.of(1, 2, 3, 4, 5, 6);
        AtomicInteger mapCallCount = new AtomicInteger(0);

        System.out.println("BEFORE: source=" + source + ", filter even, then map (counting map invocations)");

        List<Integer> result = CustomPipeline.of(source)
            .filter(x -> x % 2 == 0)
            .map(x -> { mapCallCount.incrementAndGet(); return x * 10; })
            .toList();

        System.out.println("AFTER:  result=" + result +
                           ", mapCallCount=" + mapCallCount.get() +
                           " (expected 3 — map must only run on [2,4,6], not all 6 elements)");

        assertEquals(List.of(20, 40, 60), result,
            "Result must be correct. Got: " + result);
        assertEquals(3, mapCallCount.get(),
            "map must only be called on elements that passed filter. " +
            "Got mapCallCount=" + mapCallCount.get() + " — if 6, an intermediate collection was created (not fused)");
    }

    // -------------------------------------------------------------------------
    // Short-circuit correctness
    //
    // findFirst must stop processing as soon as it finds a match.
    // On a 10,000-element list filtering for the first even number,
    // only 2 elements should be visited — not all 10,000.
    // -------------------------------------------------------------------------

    @Test
    void testFindFirstShortCircuitsOnLargeInput() {
        List<Integer> source = new ArrayList<>();
        for (int i = 1; i <= 10_000; i++) source.add(i);

        AtomicInteger visitCount = new AtomicInteger(0);

        System.out.println("BEFORE: source.size=" + source.size() + ", filter even, findFirst — counting elements visited");

        Optional<Integer> result = CustomPipeline.of(source)
            .map(x -> { visitCount.incrementAndGet(); return x; })
            .filter(x -> x % 2 == 0)
            .findFirst();

        System.out.println("AFTER:  result=" + result +
                           ", visitCount=" + visitCount.get() +
                           " (expected Optional[2], visitCount must be << 10000)");

        assertTrue(result.isPresent(), "findFirst must find the first even number");
        assertEquals(2, result.get(), "First even number in [1..10000] is 2. Got: " + result.get());
        assertTrue(visitCount.get() < 10,
            "findFirst must short-circuit — only a handful of elements should be visited. " +
            "Got visitCount=" + visitCount.get() + " — if 10000, cancellation is not working");
    }

    // -------------------------------------------------------------------------
    // Pipeline immutability
    //
    // Branching from the same base pipeline must produce independent results.
    // If stages are shared/mutated, one branch will corrupt the other.
    // -------------------------------------------------------------------------

    @Test
    void testBranchingFromSameBaseProducesIndependentResults() {
        List<Integer> source = List.of(1, 2, 3, 4, 5, 6);
        CustomPipeline<Integer> base = CustomPipeline.of(source);

        System.out.println("BEFORE: source=" + source + ", branching into evens and odds from same base pipeline");

        List<Integer> evens = base.filter(x -> x % 2 == 0).toList();
        List<Integer> odds  = base.filter(x -> x % 2 != 0).toList();

        System.out.println("AFTER:  evens=" + evens + " (expected [2,4,6])" +
                           ", odds=" + odds + " (expected [1,3,5])");

        assertEquals(List.of(2, 4, 6), evens,
            "Even branch must contain only even numbers. Got: " + evens);
        assertEquals(List.of(1, 3, 5), odds,
            "Odd branch must contain only odd numbers. Got: " + odds +
            " — if evens filter leaked into odds, stages are being mutated (not immutable)");
    }

    // -------------------------------------------------------------------------
    // Deep chain composition
    //
    // Four operations chained in sequence. Validates that the Sink chain
    // builds correctly at any depth and all stages execute in the right order.
    // -------------------------------------------------------------------------

    @Test
    void testDeepChainOfFourOperationsComposesCorrectly() {
        List<Integer> source = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        System.out.println("BEFORE: source=" + source + ", filter>5, map*2, filter>14, map-1");

        List<Integer> result = CustomPipeline.of(source)
            .filter(x -> x > 5)           // [6,7,8,9,10]
            .map(x -> x * 2)              // [12,14,16,18,20]
            .filter(x -> x > 14)          // [16,18,20]
            .map(x -> x - 1)              // [15,17,19]
            .toList();

        System.out.println("AFTER:  result=" + result + " (expected [15,17,19])");

        assertEquals(List.of(15, 17, 19), result,
            "Deep chain must apply all four operations in order. Got: " + result);
    }

    // -------------------------------------------------------------------------
    // flatMap + downstream composition
    //
    // flatMap must interoperate correctly with downstream filter and map stages.
    // -------------------------------------------------------------------------

    @Test
    void testFlatMapThenFilterAndMap() {
        List<List<Integer>> source = List.of(
            List.of(1, 2, 3),
            List.of(4, 5, 6),
            List.of(7, 8, 9)
        );

        System.out.println("BEFORE: source=" + source + ", flatMap, filter even, map*100");

        List<Integer> result = CustomPipeline.of(source)
            .flatMap(inner -> inner)
            .filter(x -> x % 2 == 0)
            .map(x -> x * 100)
            .toList();

        System.out.println("AFTER:  result=" + result + " (expected [200,400,600,800])");

        assertEquals(List.of(200, 400, 600, 800), result,
            "flatMap followed by filter and map must compose correctly. Got: " + result);
    }

    // -------------------------------------------------------------------------
    // Terminal operation consistency
    //
    // count() and toList().size() must always agree.
    // forEach and toList must visit elements in the same order.
    // -------------------------------------------------------------------------

    @Test
    void testCountAndToListSizeAgree() {
        List<Integer> source = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        System.out.println("BEFORE: source=" + source + ", filter odd, compare count vs toList().size()");

        long count       = CustomPipeline.of(source).filter(x -> x % 2 != 0).count();
        int  toListSize  = CustomPipeline.of(source).filter(x -> x % 2 != 0).toList().size();

        System.out.println("AFTER:  count=" + count + ", toList().size()=" + toListSize + " (both must be 5)");

        assertEquals(count, toListSize,
            "count() and toList().size() must return the same value. Got count=" + count + ", toListSize=" + toListSize);
    }

    @Test
    void testForEachAndToListVisitElementsInSameOrder() {
        List<Integer> source = List.of(10, 20, 30, 40, 50);

        System.out.println("BEFORE: source=" + source + ", comparing forEach vs toList order");

        List<Integer> fromForEach = new ArrayList<>();
        CustomPipeline.of(source).forEach(fromForEach::add);

        List<Integer> fromToList = CustomPipeline.of(source).toList();

        System.out.println("AFTER:  fromForEach=" + fromForEach + ", fromToList=" + fromToList + " (must be equal — same order)");

        assertEquals(fromToList, fromForEach,
            "forEach and toList must visit elements in the same order. " +
            "Got forEach=" + fromForEach + ", toList=" + fromToList);
    }
}
