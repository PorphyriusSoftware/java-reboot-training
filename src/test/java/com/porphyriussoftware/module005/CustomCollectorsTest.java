package com.porphyriussoftware.module005;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class CustomCollectorsTest {
    // ------------------------------------------------------------
    //  toImmutableList()
    // ------------------------------------------------------------

    @Test
    void toImmutableList_parallelSafety() {
        List<Integer> input = IntStream.range(0, 10_000).boxed().toList();
        System.out.println("BEFORE: input.size=" + input.size() + ", toImmutableList via parallelStream: result must match source and be immutable");
        List<Integer> result = input.parallelStream()
            .collect(CustomCollectors.toImmutableList());
        System.out.println("AFTER:  result.size=" + result.size() + " (expected 10000, same order as input)");
        assertEquals(input, result);
        System.out.println("EXPECTING: UnsupportedOperationException — list must be immutable");
        assertThrows(UnsupportedOperationException.class, () -> result.add(99));
        System.out.println("AFTER:  UnsupportedOperationException thrown as expected");
    }

    @Test
    void toImmutableList_noSharedMutableState() {
        List<String> input = List.of("a", "b", "c");
        System.out.println("BEFORE: input=" + input + ", toImmutableList: two collections from same stream must produce independent instances");
        List<String> r1 = input.stream().collect(CustomCollectors.toImmutableList());
        List<String> r2 = input.stream().collect(CustomCollectors.toImmutableList());
        System.out.println("AFTER:  r1=" + r1 + ", r2=" + r2 + " (must not be the same object reference)");
        assertNotSame(r1, r2);
    }

    // ------------------------------------------------------------
    //  frequencyMap()
    // ------------------------------------------------------------

    @Test
    void frequencyMap_parallelCorrectness() {
        List<String> input = IntStream.range(0, 100_000).mapToObj(i -> "x").toList();
        System.out.println("BEFORE: input.size=" + input.size() + " (all \"x\"), frequencyMap via parallelStream: must count all elements correctly");
        Map<String, Long> freq = input.parallelStream()
            .collect(CustomCollectors.frequencyMap());
        System.out.println("AFTER:  freq.get(\"x\")=" + freq.get("x") + " (expected 100000)");
        assertEquals(100_000L, freq.get("x"));
    }

    @Test
    void frequencyMap_combinerMustMergeCorrectly() {
        List<String> input = List.of("a", "b", "a", "b", "a");
        System.out.println("BEFORE: input=" + input + ", frequencyMap via parallelStream: combiner must merge partial maps without losing counts");
        Map<String, Long> freq = input.parallelStream()
            .collect(CustomCollectors.frequencyMap());
        System.out.println("AFTER:  freq.get(\"a\")=" + freq.get("a") + " (expected 3), freq.get(\"b\")=" + freq.get("b") + " (expected 2)");
        assertEquals(3L, freq.get("a"));
        assertEquals(2L, freq.get("b"));
    }

    @Test
    void frequencyMap_noSharedStateBetweenCollections() {
        System.out.println("BEFORE: two separate frequencyMap collections of [\"a\",\"a\"]: results must be independent objects");
        Map<String, Long> r1 = Stream.of("a", "a").collect(CustomCollectors.frequencyMap());
        Map<String, Long> r2 = Stream.of("a", "a").collect(CustomCollectors.frequencyMap());
        System.out.println("AFTER:  r1=" + r1 + ", r2=" + r2 + " (must not be the same object reference)");
        assertNotSame(r1, r2);
    }

    // ------------------------------------------------------------
    //  groupByLength()
    // ------------------------------------------------------------

    @Test
    void groupByLength_parallelGrouping() {
        List<String> input = List.of("hi", "yo", "cat", "dog", "sun", "sky");
        System.out.println("BEFORE: input=" + input + ", groupByLength via parallelStream: group strings by their length");
        Map<Integer, List<String>> grouped = input.parallelStream()
            .collect(CustomCollectors.groupByLength());
        System.out.println("AFTER:  grouped.get(2)=" + grouped.get(2) + " (expected [hi, yo]), grouped.get(3)=" + grouped.get(3) + " (expected [cat, dog, sun, sky])");
        assertEquals(List.of("hi", "yo"), grouped.get(2));
        assertEquals(List.of("cat", "dog", "sun", "sky"), grouped.get(3));
    }

    @Test
    void groupByLength_combinerMustMergeLists() {
        List<String> input = List.of("aa", "bb", "cc", "dd");
        System.out.println("BEFORE: input=" + input + ", groupByLength via parallelStream: combiner must merge partial lists into one");
        Map<Integer, List<String>> grouped = input.parallelStream()
            .collect(CustomCollectors.groupByLength());
        System.out.println("AFTER:  grouped.get(2).size=" + grouped.get(2).size() + " (expected 4 — all 4 strings grouped under key 2)");
        assertEquals(4, grouped.get(2).size());
    }

    @Test
    void groupByLength_listsMustBeIndependent() {
        System.out.println("BEFORE: two groupByLength collections of [\"hi\",\"yo\"]: inner lists must be independent instances");
        Map<Integer, List<String>> g1 = Stream.of("hi", "yo").collect(CustomCollectors.groupByLength());
        Map<Integer, List<String>> g2 = Stream.of("hi", "yo").collect(CustomCollectors.groupByLength());
        System.out.println("AFTER:  g1.get(2)=" + g1.get(2) + ", g2.get(2)=" + g2.get(2) + " (must not be the same list reference)");
        assertNotSame(g1.get(2), g2.get(2));
    }

    // ------------------------------------------------------------
    //  joiningWithDash()
    // ------------------------------------------------------------

    @Test
    void joiningWithDash_parallelCorrectness() {
        List<String> input = IntStream.range(0, 10_000).mapToObj(i -> "x").toList();
        System.out.println("BEFORE: input.size=" + input.size() + " (all \"x\"), joiningWithDash via parallelStream: must produce same result as sequential");
        String result = input.parallelStream().collect(CustomCollectors.joiningWithDash());
        String expected = String.join("-", Collections.nCopies(10_000, "x"));
        System.out.println("AFTER:  result.length=" + result.length() + ", expected.length=" + expected.length() + " (must match)");
        assertEquals(expected, result);
    }

    @Test
    void joiningWithDash_noTrailingDash() {
        System.out.println("BEFORE: input=[\"a\",\"b\",\"c\"], joiningWithDash: join with dash, no trailing dash");
        String result = Stream.of("a", "b", "c").collect(CustomCollectors.joiningWithDash());
        System.out.println("AFTER:  result=\"" + result + "\" (expected \"a-b-c\")");
        assertEquals("a-b-c", result);
    }

    @Test
    void joiningWithDash_emptyStream() {
        System.out.println("BEFORE: input=empty stream, joiningWithDash: empty stream must produce empty string");
        String result = Stream.<String>empty().collect(CustomCollectors.joiningWithDash());
        System.out.println("AFTER:  result=\"" + result + "\" (expected \"\")");
        assertEquals("", result);
    }

    // ------------------------------------------------------------
    //  Meta tests — the ones that break bad collectors
    // ------------------------------------------------------------

    @Test
    void collectorsMustNotLeakMutableInternalState() {
        List<String> input = List.of("a", "b", "c");
        System.out.println("BEFORE: input=" + input + ", toImmutableList: result must not expose mutable internal state");
        List<String> result = input.stream().collect(CustomCollectors.toImmutableList());
        System.out.println("AFTER:  result=" + result);
        System.out.println("EXPECTING: UnsupportedOperationException — adding to result must throw");
        assertThrows(UnsupportedOperationException.class, () -> result.add("x"));
        System.out.println("AFTER:  UnsupportedOperationException thrown as expected");
    }

    @Test
    void collectorsMustBeAssociative() {
        List<String> input = List.of("a", "b", "c", "d");
        System.out.println("BEFORE: input=" + input + ", joiningWithDash sequential vs parallel: associative combiner must produce identical results");
        String s1 = input.stream().collect(CustomCollectors.joiningWithDash());
        String s2 = input.parallelStream().collect(CustomCollectors.joiningWithDash());
        System.out.println("AFTER:  sequential=\"" + s1 + "\", parallel=\"" + s2 + "\" (must be equal)");
        assertEquals(s1, s2);
    }

    @Test
    void collectorsMustNotUseSharedStaticState() {
        List<String> input = List.of("a", "b", "c");
        System.out.println("BEFORE: input=" + input + ", joiningWithDash twice: static shared state would cause one run to affect the other");
        String r1 = input.stream().collect(CustomCollectors.joiningWithDash());
        String r2 = input.stream().collect(CustomCollectors.joiningWithDash());
        System.out.println("AFTER:  r1=\"" + r1 + "\", r2=\"" + r2 + "\" (must be equal — no shared static state)");
        assertEquals(r1, r2);
    }
}