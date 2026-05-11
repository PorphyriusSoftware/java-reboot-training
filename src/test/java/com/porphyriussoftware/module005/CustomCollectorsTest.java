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

        List<Integer> result = input.parallelStream()
            .collect(CustomCollectors.toImmutableList());

        assertEquals(input, result);
        assertThrows(UnsupportedOperationException.class, () -> result.add(99));
    }

    @Test
    void toImmutableList_noSharedMutableState() {
        List<String> input = List.of("a", "b", "c");

        List<String> r1 = input.stream().collect(CustomCollectors.toImmutableList());
        List<String> r2 = input.stream().collect(CustomCollectors.toImmutableList());

        assertNotSame(r1, r2);
    }

    // ------------------------------------------------------------
    //  frequencyMap()
    // ------------------------------------------------------------

    @Test
    void frequencyMap_parallelCorrectness() {
        List<String> input = IntStream.range(0, 100_000)
            .mapToObj(i -> "x")
            .toList();

        Map<String, Long> freq = input.parallelStream()
            .collect(CustomCollectors.frequencyMap());

        assertEquals(100_000L, freq.get("x"));
    }

    @Test
    void frequencyMap_combinerMustMergeCorrectly() {
        List<String> input = List.of("a", "b", "a", "b", "a");

        Map<String, Long> freq = input.parallelStream()
            .collect(CustomCollectors.frequencyMap());

        assertEquals(3L, freq.get("a"));
        assertEquals(2L, freq.get("b"));
    }

    @Test
    void frequencyMap_noSharedStateBetweenCollections() {
        Map<String, Long> r1 = Stream.of("a", "a").collect(CustomCollectors.frequencyMap());
        Map<String, Long> r2 = Stream.of("a", "a").collect(CustomCollectors.frequencyMap());

        assertNotSame(r1, r2);
    }

    // ------------------------------------------------------------
    //  groupByLength()
    // ------------------------------------------------------------

    @Test
    void groupByLength_parallelGrouping() {
        List<String> input = List.of("hi", "yo", "cat", "dog", "sun", "sky");

        Map<Integer, List<String>> grouped = input.parallelStream()
            .collect(CustomCollectors.groupByLength());

        assertEquals(List.of("hi", "yo"), grouped.get(2));
        assertEquals(List.of("cat", "dog", "sun", "sky"), grouped.get(3));
    }

    @Test
    void groupByLength_combinerMustMergeLists() {
        List<String> input = List.of("aa", "bb", "cc", "dd");

        Map<Integer, List<String>> grouped = input.parallelStream()
            .collect(CustomCollectors.groupByLength());

        assertEquals(4, grouped.get(2).size());
    }

    @Test
    void groupByLength_listsMustBeIndependent() {
        Map<Integer, List<String>> g1 = Stream.of("hi", "yo")
            .collect(CustomCollectors.groupByLength());

        Map<Integer, List<String>> g2 = Stream.of("hi", "yo")
            .collect(CustomCollectors.groupByLength());

        assertNotSame(g1.get(2), g2.get(2));
    }

    // ------------------------------------------------------------
    //  joiningWithDash()
    // ------------------------------------------------------------

    @Test
    void joiningWithDash_parallelCorrectness() {
        List<String> input = IntStream.range(0, 10_000)
            .mapToObj(i -> "x")
            .toList();

        String result = input.parallelStream()
            .collect(CustomCollectors.joiningWithDash());

        assertEquals(
            String.join("-", Collections.nCopies(10_000, "x")),
            result
        );
    }

    @Test
    void joiningWithDash_noTrailingDash() {
        String result = Stream.of("a", "b", "c")
            .collect(CustomCollectors.joiningWithDash());

        assertEquals("a-b-c", result);
    }

    @Test
    void joiningWithDash_emptyStream() {
        String result = Stream.<String>empty()
            .collect(CustomCollectors.joiningWithDash());

        assertEquals("", result);
    }

    // ------------------------------------------------------------
    //  Meta tests — the ones that break bad collectors
    // ------------------------------------------------------------

    @Test
    void collectorsMustNotLeakMutableInternalState() {
        List<String> input = List.of("a", "b", "c");

        List<String> result = input.stream()
            .collect(CustomCollectors.toImmutableList());

        assertThrows(UnsupportedOperationException.class, () -> result.add("x"));
    }

    @Test
    void collectorsMustBeAssociative() {
        List<String> input = List.of("a", "b", "c", "d");

        String s1 = input.stream().collect(CustomCollectors.joiningWithDash());
        String s2 = input.parallelStream().collect(CustomCollectors.joiningWithDash());

        assertEquals(s1, s2);
    }

    @Test
    void collectorsMustNotUseSharedStaticState() {
        List<String> input = List.of("a", "b", "c");

        String r1 = input.stream().collect(CustomCollectors.joiningWithDash());
        String r2 = input.stream().collect(CustomCollectors.joiningWithDash());

        assertEquals(r1, r2);
    }
}
