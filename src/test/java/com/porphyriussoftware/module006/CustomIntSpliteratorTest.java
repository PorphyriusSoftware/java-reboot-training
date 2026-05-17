package com.porphyriussoftware.module006;

// =============================================================================
// Module 6 — CustomIntSpliterator Test Suite
//
// Basic Test Suite (Gohan pre-training with Piccolo)
//   Validates fundamentals: tryAdvance, trySplit, estimateSize, characteristics,
//   and sequential stream support.
//
// Ultra Instinct Test Suite (the real fight)
//   Validates production-grade correctness: SUBSIZED semantics, split after
//   partial consumption, balanced splits, characteristics preserved through
//   splits, no aliasing, recursive split coverage, and parallel determinism.
//
// A passing basic suite means fundamentals work.
// A passing Ultra Instinct suite means the implementation is production-grade.
// =============================================================================

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

class CustomIntSpliteratorTest {

    // =========================================================================
    // Basic Test Suite — Gohan pre-training with Piccolo
    // =========================================================================

    @Test
    void testTryAdvanceConsumesElements() {
        List<Integer> source = List.of(1, 2, 3, 4, 5);
        CustomIntSpliterator spliterator = new CustomIntSpliterator(source);

        System.out.println("BEFORE: source=" + source + ", estimateSize=" + spliterator.estimateSize());

        List<Integer> result = new ArrayList<>();
        while (spliterator.tryAdvance(result::add)) {
            // keep consuming
        }

        System.out.println("AFTER:  consumed=" + result + ", estimateSize=" + spliterator.estimateSize());

        assertEquals(source, result, "tryAdvance must consume all elements in order");
    }

    @Test
    void testTrySplitProducesTwoSpliterators() {
        List<Integer> source = List.of(1, 2, 3, 4, 5, 6);
        CustomIntSpliterator spliterator = new CustomIntSpliterator(source);

        System.out.println("BEFORE: source=" + source + ", estimateSize=" + spliterator.estimateSize());

        Spliterator<Integer> split = spliterator.trySplit();

        assertNotNull(split, "trySplit must return a non-null spliterator for a 6-element list");

        List<Integer> left = new ArrayList<>();
        List<Integer> right = new ArrayList<>();

        split.forEachRemaining(left::add);
        spliterator.forEachRemaining(right::add);

        System.out.println("AFTER:  left=" + left + " (size=" + left.size() + ")" +
                           ", right=" + right + " (size=" + right.size() + ")" +
                           ", combined size=" + (left.size() + right.size()));

        assertFalse(left.isEmpty(), "Left split must contain elements");
        assertFalse(right.isEmpty(), "Right split must contain elements");

        List<Integer> combined = new ArrayList<>(left);
        combined.addAll(right);

        assertEquals(source.size(), combined.size(), "Split + remainder must cover all elements");
        assertTrue(source.containsAll(combined), "Combined elements must match the source");
    }

    @Test
    void testTrySplitReturnsNullForTooSmallInput() {
        // Start with 2 elements so the first split must succeed — proving the implementation
        // can return non-null. Only then does the null assertion on the remainder mean anything.
        List<Integer> source = List.of(1, 2);
        CustomIntSpliterator spliterator = new CustomIntSpliterator(source);

        System.out.println("BEFORE: source=" + source + ", estimateSize=" + spliterator.estimateSize());

        Spliterator<Integer> firstSplit = spliterator.trySplit();

        System.out.println("AFTER first trySplit:  firstSplit=" + firstSplit +
                           ", remainder estimateSize=" + spliterator.estimateSize());

        assertNotNull(firstSplit, "trySplit must succeed on a 2-element list — if this fails the null assertion below is meaningless");

        // Now the remainder has 1 element — too small to split
        Spliterator<Integer> secondSplit = spliterator.trySplit();

        System.out.println("AFTER second trySplit: secondSplit=" + secondSplit +
                           " (expected null — 1 element left, cannot split)");

        assertNull(secondSplit, "trySplit must return null when only 1 element remains");
    }

    @Test
    void testCharacteristicsContainOrdered() {
        // WHAT IS ORDERED?
        // ORDERED means elements have a defined encounter order — the Spliterator
        // always delivers them in the same sequence (e.g. index 0, 1, 2...).
        // Our source is a List, which guarantees order, so we must declare this.
        //
        // WHY DOES IT MATTER?
        // If ORDERED is not declared, the stream pipeline is allowed to ignore order
        // during parallel execution — elements could arrive in any sequence.
        // Declaring ORDERED forces the pipeline to respect insertion order.
        //
        // HOW TO IMPLEMENT IT?
        // In CustomIntSpliterator.characteristics(), return a bitmask that includes
        // Spliterator.ORDERED. You can combine multiple flags with |, for example:
        //
        //   return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        //
        // characteristics() returns a bitmask — each bit represents one flag.
        // We use bitwise AND (&) to check if a specific flag is set.
        // Example: if characteristics() = 0b10010 and ORDERED = 0b00010, then
        //          0b10010 & 0b00010 = 0b00010 != 0 → flag is present.
        List<Integer> source = List.of(10, 20, 30);
        CustomIntSpliterator spliterator = new CustomIntSpliterator(source);

        System.out.println("BEFORE: source=" + source + ", checking characteristics() bitmask for ORDERED flag");

        int actual = spliterator.characteristics();
        boolean hasOrdered = (actual & Spliterator.ORDERED) != 0;

        System.out.println("AFTER:  characteristics()=" + actual +
                           ", ORDERED bit=" + Spliterator.ORDERED +
                           ", hasOrdered=" + hasOrdered);

        assertTrue(hasOrdered,
            "ORDERED flag must be set. characteristics()=" + actual + " but expected bit " + Spliterator.ORDERED + " to be present.");
    }

    @Test
    void testEstimateSizeMatchesRemainingElements() {
        List<Integer> source = List.of(1, 2, 3, 4);
        CustomIntSpliterator spliterator = new CustomIntSpliterator(source);

        System.out.println("BEFORE: source=" + source + ", estimateSize=" + spliterator.estimateSize());

        assertEquals(4, spliterator.estimateSize(), "Initial estimate must match size");

        spliterator.tryAdvance(x -> {});

        System.out.println("AFTER consuming 1 element: estimateSize=" + spliterator.estimateSize() +
                           " (expected 3)");

        assertEquals(3, spliterator.estimateSize(), "Estimate must decrease after consumption");
    }

    @Test
    void testStreamSupportWorks() {
        List<Integer> source = List.of(1, 2, 3, 4, 5);
        CustomIntSpliterator spliterator = new CustomIntSpliterator(source);

        System.out.println("BEFORE: source=" + source + ", running as sequential stream via StreamSupport");

        List<Integer> result = StreamSupport.stream(spliterator, false).toList();

        System.out.println("AFTER:  result=" + result);

        assertEquals(source, result, "StreamSupport.stream must produce correct sequential results");
    }

    // =========================================================================
    // Ultra Instinct Test Suite — the real fight
    // =========================================================================

    // -------------------------------------------------------------------------
    // SUBSIZED semantics
    //
    // SUBSIZED means: after trySplit(), BOTH halves must report an exact
    // estimateSize(). And left.estimateSize() + right.estimateSize() must equal
    // the original estimateSize() before the split.
    //
    // If this fails, the ForkJoinPool cannot balance work correctly.
    // -------------------------------------------------------------------------

    @Test
    void testSubSizedSumsToOriginalBeforeSplit() {
        List<Integer> source = List.of(1, 2, 3, 4, 5, 6, 7, 8);
        CustomIntSpliterator spliterator = new CustomIntSpliterator(source);

        long originalSize = spliterator.estimateSize();

        System.out.println("BEFORE: source=" + source + ", estimateSize=" + originalSize);

        Spliterator<Integer> left = spliterator.trySplit();

        assertNotNull(left, "trySplit must succeed on an 8-element list");

        long leftSize = left.estimateSize();
        long rightSize = spliterator.estimateSize();

        System.out.println("AFTER:  left.estimateSize=" + leftSize +
                           ", right.estimateSize=" + rightSize +
                           ", sum=" + (leftSize + rightSize) +
                           " (must equal original " + originalSize + ")");

        assertEquals(originalSize, leftSize + rightSize,
            "SUBSIZED contract: left.estimateSize() + right.estimateSize() must equal original estimateSize(). " +
            "Got left=" + leftSize + ", right=" + rightSize + ", expected sum=" + originalSize);
    }

    // -------------------------------------------------------------------------
    // Split after partial consumption
    //
    // This is the classic mid-index trap. After consuming some elements,
    // the split must still divide the REMAINING elements correctly.
    //
    // The bug to catch: calculating mid as half the remaining COUNT and using
    // it directly as the subList end index. When index > 0, that produces a
    // wrong (or negative) range.
    //
    // Example with source=[1..8], after consuming 4:
    //   index=4, remaining=[5,6,7,8]
    //   Wrong:   mid = (8-4)/2 = 2  →  subList(4, 2)  →  IllegalArgumentException
    //   Correct: mid = 4 + (8-4)/2 = 6  →  subList(4, 6) = [5,6], right = [7,8]
    // -------------------------------------------------------------------------

    @Test
    void testSplitAfterPartialConsumption() {
        List<Integer> source = List.of(1, 2, 3, 4, 5, 6, 7, 8);
        CustomIntSpliterator spliterator = new CustomIntSpliterator(source);

        System.out.println("BEFORE: source=" + source + ", consuming first 4 elements...");

        for (int i = 0; i < 4; i++) {
            spliterator.tryAdvance(x -> {});
        }

        System.out.println("BEFORE split: consumed [1,2,3,4], remaining estimateSize=" + spliterator.estimateSize());

        Spliterator<Integer> left = spliterator.trySplit();

        assertNotNull(left, "trySplit must succeed when 4 elements remain");

        List<Integer> leftResult = new ArrayList<>();
        List<Integer> rightResult = new ArrayList<>();

        left.forEachRemaining(leftResult::add);
        spliterator.forEachRemaining(rightResult::add);

        System.out.println("AFTER:  left=" + leftResult + " (expected [5,6])" +
                           ", right=" + rightResult + " (expected [7,8])");

        // After consuming [1,2,3,4], remaining is [5,6,7,8].
        // A correct half-split must produce left=[5,6] and right=[7,8] — in order.
        assertEquals(List.of(5, 6), leftResult,
            "Left half must contain the first half of remaining elements in order. Got: " + leftResult);
        assertEquals(List.of(7, 8), rightResult,
            "Right half must contain the second half of remaining elements in order. Got: " + rightResult);
    }

    // -------------------------------------------------------------------------
    // Balanced split
    //
    // Neither half should be more than 2x the other in size.
    // A wildly unbalanced split defeats the purpose of parallel execution —
    // one thread does all the work while the other finishes instantly.
    // -------------------------------------------------------------------------

    @Test
    void testSplitIsReasonablyBalanced() {
        List<Integer> source = IntStream.rangeClosed(1, 100).boxed().toList();
        CustomIntSpliterator spliterator = new CustomIntSpliterator(source);

        System.out.println("BEFORE: source.size=" + source.size() + ", splitting in half...");

        Spliterator<Integer> left = spliterator.trySplit();

        assertNotNull(left, "trySplit must succeed on a 100-element list");

        long leftSize = left.estimateSize();
        long rightSize = spliterator.estimateSize();
        double ratio = (double) Math.max(leftSize, rightSize) / Math.min(leftSize, rightSize);

        System.out.println("AFTER:  left.size=" + leftSize +
                           ", right.size=" + rightSize +
                           ", ratio=" + String.format("%.2f", ratio) +
                           " (must be <= 2.00)");

        assertTrue(leftSize * 2 >= rightSize,
            "Split is too unbalanced: left=" + leftSize + " is less than half of right=" + rightSize);
        assertTrue(rightSize * 2 >= leftSize,
            "Split is too unbalanced: right=" + rightSize + " is less than half of left=" + leftSize);
    }

    // -------------------------------------------------------------------------
    // Characteristics preserved through splits
    //
    // After trySplit(), both halves must still report the same characteristics
    // as the original. ORDERED, SIZED, and SUBSIZED must not be lost.
    // -------------------------------------------------------------------------

    @Test
    void testCharacteristicsPreservedAfterSplit() {
        List<Integer> source = List.of(1, 2, 3, 4, 5, 6);
        CustomIntSpliterator spliterator = new CustomIntSpliterator(source);

        int originalCharacteristics = spliterator.characteristics();
        int required = Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;

        System.out.println("BEFORE: source=" + source +
                           ", characteristics=" + originalCharacteristics +
                           ", required flags (ORDERED|SIZED|SUBSIZED)=" + required);

        Spliterator<Integer> left = spliterator.trySplit();

        assertNotNull(left, "trySplit must succeed on a 6-element list");

        int leftCharacteristics = left.characteristics();
        int rightCharacteristics = spliterator.characteristics();

        System.out.println("AFTER:  left.characteristics=" + leftCharacteristics +
                           " (has required=" + ((leftCharacteristics & required) == required) + ")" +
                           ", right.characteristics=" + rightCharacteristics +
                           " (has required=" + ((rightCharacteristics & required) == required) + ")");

        assertEquals(required, leftCharacteristics & required,
            "Left half must preserve ORDERED|SIZED|SUBSIZED. Got characteristics=" + leftCharacteristics);
        assertEquals(required, rightCharacteristics & required,
            "Right half must preserve ORDERED|SIZED|SUBSIZED. Got characteristics=" + rightCharacteristics);
    }

    // -------------------------------------------------------------------------
    // No aliasing between split halves
    //
    // The two spliterators must operate independently. Advancing one must not
    // affect the cursor position or content of the other.
    // -------------------------------------------------------------------------

    @Test
    void testNoAliasingBetweenSplitHalves() {
        List<Integer> source = List.of(10, 20, 30, 40, 50, 60);
        CustomIntSpliterator spliterator = new CustomIntSpliterator(source);

        Spliterator<Integer> left = spliterator.trySplit();

        assertNotNull(left, "trySplit must succeed on a 6-element list");

        System.out.println("BEFORE: split done, left.estimateSize=" + left.estimateSize() +
                           ", right.estimateSize=" + spliterator.estimateSize() +
                           " — draining left half completely...");

        while (left.tryAdvance(x -> {})) { /* drain */ }

        long rightSizeAfterLeftDrained = spliterator.estimateSize();

        System.out.println("AFTER draining left: right.estimateSize=" + rightSizeAfterLeftDrained +
                           " (must be > 0 — right must be unaffected by left being drained)");

        assertTrue(rightSizeAfterLeftDrained > 0,
            "Right half must still have elements after left half is fully consumed — the two must not share a cursor");

        List<Integer> rightElements = new ArrayList<>();
        spliterator.forEachRemaining(rightElements::add);

        System.out.println("AFTER collecting right: rightElements=" + rightElements);

        assertFalse(rightElements.isEmpty(),
            "Right half must produce elements independently of the left half");
    }

    // -------------------------------------------------------------------------
    // Recursive splitting covers all elements
    //
    // Split recursively until trySplit() returns null, then collect all
    // elements from every leaf spliterator. The combined result must contain
    // exactly the source elements — no drops, no duplicates.
    // -------------------------------------------------------------------------

    @Test
    void testRecursiveSplitCoversAllElements() {
        List<Integer> source = IntStream.rangeClosed(1, 64).boxed().toList();
        CustomIntSpliterator root = new CustomIntSpliterator(source);

        System.out.println("BEFORE: source.size=" + source.size() + ", splitting recursively until null...");

        List<Integer> collected = new ArrayList<>();
        recursiveCollect(root, collected);

        Collections.sort(collected);

        System.out.println("AFTER:  collected.size=" + collected.size() +
                           ", first=" + collected.get(0) +
                           ", last=" + collected.get(collected.size() - 1) +
                           " (expected size=" + source.size() + ", first=1, last=64)");

        assertEquals(source.size(), collected.size(),
            "Recursive split must produce exactly source.size() elements. Got " + collected.size());
        assertEquals(source, collected,
            "Recursive split must produce all original elements with no drops or duplicates");
    }

    private void recursiveCollect(Spliterator<Integer> spliterator, List<Integer> out) {
        Spliterator<Integer> left = spliterator.trySplit();
        if (left != null) {
            recursiveCollect(left, out);
            recursiveCollect(spliterator, out);
        } else {
            spliterator.forEachRemaining(out::add);
        }
    }

    // -------------------------------------------------------------------------
    // Parallel stream determinism
    //
    // A parallel stream backed by this Spliterator must produce the same
    // sorted result on every run. Non-determinism here means the combiner or
    // splitter has shared mutable state.
    //
    // @RepeatedTest runs the test 10 times to catch race conditions that would
    // pass on a single execution.
    // -------------------------------------------------------------------------

    @RepeatedTest(10)
    void testParallelStreamProducesDeterministicResult() {
        List<Integer> source = IntStream.rangeClosed(1, 1000).boxed().toList();
        CustomIntSpliterator spliterator = new CustomIntSpliterator(source);

        System.out.println("BEFORE: source.size=" + source.size() + ", running parallel stream + sort...");

        List<Integer> result = StreamSupport.stream(spliterator, true)
            .sorted()
            .collect(Collectors.toList());

        System.out.println("AFTER:  result.size=" + result.size() +
                           ", first=" + result.get(0) +
                           ", last=" + result.get(result.size() - 1) +
                           " (must match source sorted — any mismatch means shared state)");

        assertEquals(source.size(), result.size(),
            "Parallel stream must produce exactly source.size() elements");
        assertEquals(source, result,
            "Parallel stream sorted result must match the source — non-determinism means shared state");
    }

    // -------------------------------------------------------------------------
    // Parallel stream correctness on large input
    //
    // 10,000 elements, parallel stream, no sorting assumption.
    // Validates that all elements arrive exactly once.
    // -------------------------------------------------------------------------

    @Test
    void testParallelStreamOnLargeInputProducesAllElements() {
        List<Integer> source = IntStream.rangeClosed(1, 10_000).boxed().toList();
        CustomIntSpliterator spliterator = new CustomIntSpliterator(source);

        System.out.println("BEFORE: source.size=" + source.size() + ", running parallel stream (no sort)...");

        List<Integer> result = StreamSupport.stream(spliterator, true)
            .collect(Collectors.toList());

        long distinctCount = result.stream().distinct().count();

        System.out.println("AFTER:  result.size=" + result.size() +
                           ", distinct=" + distinctCount +
                           " (expected size=" + source.size() + ", distinct=" + source.size() + " — no drops, no duplicates)");

        assertEquals(source.size(), result.size(),
            "Parallel stream must produce exactly " + source.size() + " elements. Got " + result.size());
        assertEquals(new HashSet<>(source), new HashSet<>(result),
            "Parallel stream must contain exactly the source elements — no duplicates, no missing");
    }
}