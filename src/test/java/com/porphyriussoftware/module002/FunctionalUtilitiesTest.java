package com.porphyriussoftware.module002;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FunctionalUtilitiesTest {

    // ----------------------------------------------------
    // OptionalUtils tests
    // ----------------------------------------------------

    @Test
    void cleanShouldReturnDefaultForNullOrBlank() {
        assertEquals("default", OptionalUtils.clean(null, "default"));
        assertEquals("default", OptionalUtils.clean("   ", "default"));
    }

    @Test
    void cleanShouldTrimAndReturnValue() {
        assertEquals("Hello", OptionalUtils.clean("  Hello  ", "default"));
    }

    @Test
    void processShouldUppercaseAndTrim() {
        assertEquals("HELLO", OptionalUtils.process("  hello  "));
    }

    @Test
    void processShouldReturnNullForBlankOrNull() {
        assertNull(OptionalUtils.process("   "));
        assertNull(OptionalUtils.process(null));
    }

    @Test
    void processShouldHandleUnicode() {
        assertEquals("É", OptionalUtils.process("  é  "));
    }


    // ----------------------------------------------------
    // StreamUtils tests
    // ----------------------------------------------------

    @Test
    void filterStartingWithShouldReturnMatchingValues() {
        List<String> input = List.of("Alice", "Bob", "Andrew", "Charlie");
        List<String> expected = List.of("Alice", "Andrew");

        assertEquals(expected, StreamUtils.filterStartingWith(input, "A"));
    }

    @Test
    void filterStartingWithShouldHandleNullList() {
        assertTrue(StreamUtils.filterStartingWith(null, "A").isEmpty());
    }

    @Test
    void filterStartingWithShouldIgnoreNullElements() {
        List<String> input = Arrays.asList("Alice", null, "Andrew");
        List<String> expected = List.of("Alice", "Andrew");

        assertEquals(expected, StreamUtils.filterStartingWith(input, "A"));
    }

    @Test
    void filterStartingWithShouldReturnEmptyWhenNoMatches() {
        List<String> input = List.of("Bob", "Charlie");
        assertTrue(StreamUtils.filterStartingWith(input, "A").isEmpty());
    }

    @Test
    void uppercaseAllShouldUppercaseValues() {
        List<String> input = List.of("a", "b", "c");
        List<String> expected = List.of("A", "B", "C");

        assertEquals(expected, StreamUtils.uppercaseAll(input));
    }

    @Test
    void uppercaseAllShouldHandleUnicode() {
        assertEquals("É", StreamUtils.uppercaseAll(List.of("é")).get(0));
    }

    @Test
    void uppercaseAllShouldHandleNullList() {
        assertTrue(StreamUtils.uppercaseAll(null).isEmpty());
    }

    @Test
    void uppercaseAllShouldIgnoreNullElements() {
        List<String> input = Arrays.asList("a", null, "b");
        List<String> expected = List.of("A", "B");

        assertEquals(expected, StreamUtils.uppercaseAll(input));
    }

    @Test
    void sumShouldAddNumbers() {
        assertEquals(15, StreamUtils.sum(List.of(1, 2, 3, 4, 5)));
    }

    @Test
    void sumShouldHandleNullList() {
        assertEquals(0, StreamUtils.sum(null));
    }

    @Test
    void sumShouldHandleEmptyList() {
        assertEquals(0, StreamUtils.sum(List.of()));
    }


    // ----------------------------------------------------
    // SmartFilter tests
    // ----------------------------------------------------

    @Test
    void smartFilterShouldCleanTrimFilterAndUppercase() {
        List<String> input = Arrays.asList("  alice  ", null, "bob", "   ", "andrew");
        List<String> expected = List.of("ALICE", "ANDREW");

        assertEquals(expected, SmartFilter.cleanAndFilter(input, "a"));
    }

    @Test
    void smartFilterShouldHandleNullList() {
        assertTrue(SmartFilter.cleanAndFilter(null, "a").isEmpty());
    }

    @Test
    void smartFilterShouldReturnEmptyWhenNoMatches() {
        List<String> input = List.of("bob", "charlie");
        assertTrue(SmartFilter.cleanAndFilter(input, "a").isEmpty());
    }

    @Test
    void smartFilterShouldBeCaseSensitive() {
        List<String> input = List.of("Alice", "andrew");
        List<String> expected = List.of("ANDREW");

        assertEquals(expected, SmartFilter.cleanAndFilter(input, "a"));
    }
}
