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
        System.out.println("BEFORE: inputs=null and \"   \", clean: return fallback when input is null or blank");
        String r1 = OptionalUtils.clean(null, "default");
        String r2 = OptionalUtils.clean("   ", "default");
        System.out.println("AFTER:  clean(null)=\"" + r1 + "\", clean(\"   \")=\"" + r2 + "\" (both expected \"default\")");
        assertEquals("default", r1);
        assertEquals("default", r2);
    }

    @Test
    void cleanShouldTrimAndReturnValue() {
        System.out.println("BEFORE: input=\"  Hello  \", clean: trim and return non-blank value");
        String result = OptionalUtils.clean("  Hello  ", "default");
        System.out.println("AFTER:  result=\"" + result + "\" (expected \"Hello\")");
        assertEquals("Hello", result);
    }

    @Test
    void processShouldUppercaseAndTrim() {
        System.out.println("BEFORE: input=\"  hello  \", process: trim and uppercase");
        String result = OptionalUtils.process("  hello  ");
        System.out.println("AFTER:  result=\"" + result + "\" (expected \"HELLO\")");
        assertEquals("HELLO", result);
    }

    @Test
    void processShouldReturnNullForBlankOrNull() {
        System.out.println("BEFORE: inputs=\"   \" and null, process: return null when input is blank or null");
        String r1 = OptionalUtils.process("   ");
        String r2 = OptionalUtils.process(null);
        System.out.println("AFTER:  process(\"   \")=" + r1 + ", process(null)=" + r2 + " (both expected null)");
        assertNull(r1);
        assertNull(r2);
    }

    @Test
    void processShouldHandleUnicode() {
        System.out.println("BEFORE: input=\"  é  \", process: trim and uppercase Unicode character");
        String result = OptionalUtils.process("  é  ");
        System.out.println("AFTER:  result=\"" + result + "\" (expected \"É\")");
        assertEquals("É", result);
    }


    // ----------------------------------------------------
    // StreamUtils tests
    // ----------------------------------------------------

    @Test
    void filterStartingWithShouldReturnMatchingValues() {
        List<String> input = List.of("Alice", "Bob", "Andrew", "Charlie");
        System.out.println("BEFORE: input=" + input + ", filterStartingWith prefix=\"A\"");
        List<String> result = StreamUtils.filterStartingWith(input, "A");
        System.out.println("AFTER:  result=" + result + " (expected [Alice, Andrew])");
        assertEquals(List.of("Alice", "Andrew"), result);
    }

    @Test
    void filterStartingWithShouldHandleNullList() {
        System.out.println("BEFORE: input=null, filterStartingWith: null guard");
        List<String> result = StreamUtils.filterStartingWith(null, "A");
        System.out.println("AFTER:  result=" + result + " (expected empty)");
        assertTrue(result.isEmpty());
    }

    @Test
    void filterStartingWithShouldIgnoreNullElements() {
        List<String> input = Arrays.asList("Alice", null, "Andrew");
        System.out.println("BEFORE: input=" + input + " (contains null), filterStartingWith prefix=\"A\": null elements skipped");
        List<String> result = StreamUtils.filterStartingWith(input, "A");
        System.out.println("AFTER:  result=" + result + " (expected [Alice, Andrew])");
        assertEquals(List.of("Alice", "Andrew"), result);
    }

    @Test
    void filterStartingWithShouldReturnEmptyWhenNoMatches() {
        List<String> input = List.of("Bob", "Charlie");
        System.out.println("BEFORE: input=" + input + ", filterStartingWith prefix=\"A\" (no matches expected)");
        List<String> result = StreamUtils.filterStartingWith(input, "A");
        System.out.println("AFTER:  result=" + result + " (expected empty)");
        assertTrue(result.isEmpty());
    }

    @Test
    void uppercaseAllShouldUppercaseValues() {
        List<String> input = List.of("a", "b", "c");
        System.out.println("BEFORE: input=" + input + ", uppercaseAll: uppercase each element");
        List<String> result = StreamUtils.uppercaseAll(input);
        System.out.println("AFTER:  result=" + result + " (expected [A, B, C])");
        assertEquals(List.of("A", "B", "C"), result);
    }

    @Test
    void uppercaseAllShouldHandleUnicode() {
        System.out.println("BEFORE: input=[\"é\"], uppercaseAll: uppercase Unicode character");
        String result = StreamUtils.uppercaseAll(List.of("é")).get(0);
        System.out.println("AFTER:  result=\"" + result + "\" (expected \"É\")");
        assertEquals("É", result);
    }

    @Test
    void uppercaseAllShouldHandleNullList() {
        System.out.println("BEFORE: input=null, uppercaseAll: null guard");
        List<String> result = StreamUtils.uppercaseAll(null);
        System.out.println("AFTER:  result=" + result + " (expected empty)");
        assertTrue(result.isEmpty());
    }

    @Test
    void uppercaseAllShouldIgnoreNullElements() {
        List<String> input = Arrays.asList("a", null, "b");
        System.out.println("BEFORE: input=" + input + " (contains null), uppercaseAll: null elements skipped");
        List<String> result = StreamUtils.uppercaseAll(input);
        System.out.println("AFTER:  result=" + result + " (expected [A, B])");
        assertEquals(List.of("A", "B"), result);
    }

    @Test
    void sumShouldAddNumbers() {
        List<Integer> input = List.of(1, 2, 3, 4, 5);
        System.out.println("BEFORE: input=" + input + ", sum: add all integers");
        int result = StreamUtils.sum(input);
        System.out.println("AFTER:  result=" + result + " (expected 15)");
        assertEquals(15, result);
    }

    @Test
    void sumShouldHandleNullList() {
        System.out.println("BEFORE: input=null, sum: null guard");
        int result = StreamUtils.sum(null);
        System.out.println("AFTER:  result=" + result + " (expected 0)");
        assertEquals(0, result);
    }

    @Test
    void sumShouldHandleEmptyList() {
        System.out.println("BEFORE: input=[] (empty), sum: empty guard");
        int result = StreamUtils.sum(List.of());
        System.out.println("AFTER:  result=" + result + " (expected 0)");
        assertEquals(0, result);
    }


    // ----------------------------------------------------
    // SmartFilter tests
    // ----------------------------------------------------

    @Test
    void smartFilterShouldCleanTrimFilterAndUppercase() {
        List<String> input = Arrays.asList("  alice  ", null, "bob", "   ", "andrew");
        System.out.println("BEFORE: input=" + input + ", cleanAndFilter prefix=\"a\": trim, drop nulls/blank, filter by prefix, uppercase");
        List<String> result = SmartFilter.cleanAndFilter(input, "a");
        System.out.println("AFTER:  result=" + result + " (expected [ALICE, ANDREW])");
        assertEquals(List.of("ALICE", "ANDREW"), result);
    }

    @Test
    void smartFilterShouldHandleNullList() {
        System.out.println("BEFORE: input=null, cleanAndFilter: null guard");
        List<String> result = SmartFilter.cleanAndFilter(null, "a");
        System.out.println("AFTER:  result=" + result + " (expected empty)");
        assertTrue(result.isEmpty());
    }

    @Test
    void smartFilterShouldReturnEmptyWhenNoMatches() {
        List<String> input = List.of("bob", "charlie");
        System.out.println("BEFORE: input=" + input + ", cleanAndFilter prefix=\"a\" (no matches expected)");
        List<String> result = SmartFilter.cleanAndFilter(input, "a");
        System.out.println("AFTER:  result=" + result + " (expected empty)");
        assertTrue(result.isEmpty());
    }

    @Test
    void smartFilterShouldBeCaseSensitive() {
        List<String> input = List.of("Alice", "andrew");
        System.out.println("BEFORE: input=" + input + ", cleanAndFilter prefix=\"a\" (case-sensitive — \"Alice\" starts with uppercase A)");
        List<String> result = SmartFilter.cleanAndFilter(input, "a");
        System.out.println("AFTER:  result=" + result + " (expected [ANDREW] — \"Alice\" rejected, case-sensitive match)");
        assertEquals(List.of("ANDREW"), result);
    }
}