package com.porphyriussoftware.module001;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link StringSanitizer}.
 *
 * <p>This suite verifies the behavior of the sanitizer under a consistent,
 * predictable rule set:
 *
 * <ul>
 *   <li><strong>Unicode normalization (NFC):</strong> combining sequences such as
 *       {@code "e" + "\\u0301"} are normalized into composed characters like {@code "é"}.</li>
 *
 *   <li><strong>Control characters treated as whitespace:</strong>
 *       All characters in the {@code \\p{Cntrl}} category (e.g., BEL, NULL, ESC)
 *       are replaced with a single space. This prevents accidental word merging
 *       while avoiding complex heuristics.</li>
 *
 *   <li><strong>Whitespace collapsing:</strong>
 *       Any sequence of spaces, tabs, or newlines collapses into a single space.</li>
 *
 *   <li><strong>Trimming:</strong>
 *       Leading and trailing whitespace is removed.</li>
 *
 *   <li><strong>Nullability:</strong>
 *       If sanitization removes all meaningful content, the method returns {@code null}.</li>
 * </ul>
 *
 * <p>The tests below validate each rule independently and in combination,
 * ensuring the sanitizer behaves consistently across typical and edge‑case inputs.
 */
class StringSanitizerTest {

    // Ensures leading and trailing whitespace is removed.
    @Test
    void trimsWhitespace() {
        String input = "   Hello   ";
        System.out.println("BEFORE: input=\"" + input + "\", sanitize: trim leading/trailing whitespace");
        String result = StringSanitizer.sanitize(input);
        System.out.println("AFTER:  result=\"" + result + "\" (expected \"Hello\")");
        assertEquals("Hello", result);
    }

    // Ensures multiple internal spaces collapse into a single space.
    @Test
    void collapsesMultipleSpaces() {
        String input = "John    Doe";
        System.out.println("BEFORE: input=\"" + input + "\", sanitize: collapse multiple internal spaces into one");
        String result = StringSanitizer.sanitize(input);
        System.out.println("AFTER:  result=\"" + result + "\" (expected \"John Doe\")");
        assertEquals("John Doe", result);
    }

    // Verifies control characters are treated as whitespace separators.
    @Test
    void removesNonPrintableCharactersAsSpaces() {
        String dirty = "HelloWorld"; // BEL control char
        System.out.println("BEFORE: input=\"Hello\\u0007World\" (BEL control char between words), sanitize: control chars become spaces");
        String result = StringSanitizer.sanitize(dirty);
        System.out.println("AFTER:  result=\"" + result + "\" (expected \"Hello World\")");
        assertEquals("Hello World", result);
    }

    // Confirms Unicode is normalized to NFC form.
    @Test
    void normalizesUnicode() {
        String composed   = "José";
        String decomposed = "José";
        System.out.println("BEFORE: input=\"Jose\\u0301\" (e + combining acute accent, decomposed form), sanitize: NFC normalization");
        String result = StringSanitizer.sanitize(decomposed);
        System.out.println("AFTER:  result=\"" + result + "\" (expected composed form \"José\")");
        assertEquals(composed, result);
    }

    // Ensures null input returns null.
    @Test
    void returnsNullForNullInput() {
        System.out.println("BEFORE: input=null, sanitize: null guard");
        String result = StringSanitizer.sanitize(null);
        System.out.println("AFTER:  result=" + result + " (expected null)");
        assertNull(result);
    }

    // Ensures empty/whitespace-only input returns null after sanitization.
    @Test
    void returnsNullForEmptyAfterCleaning() {
        String input = "     \n\t   ";
        System.out.println("BEFORE: input=\"     \\n\\t   \" (whitespace only), sanitize: returns null when nothing meaningful remains");
        String result = StringSanitizer.sanitize(input);
        System.out.println("AFTER:  result=" + result + " (expected null)");
        assertNull(result);
    }

    // Validates combined behavior: control chars → spaces, whitespace collapse, Unicode normalization.
    @Test
    void handlesMixedGarbage() {
        String input = "   José   \n\n   ";
        System.out.println("BEFORE: input=\"   Jo\\u0007se\\u0301   \\n\\n   \" (leading/trailing spaces, BEL control char, decomposed accent, newlines), sanitize: full pipeline");
        String result = StringSanitizer.sanitize(input);
        System.out.println("AFTER:  result=\"" + result + "\" (expected \"Jo sé\")");
        assertEquals("Jo sé", result);
    }

    // Ensures sanitizer does not throw on malformed or partial Unicode sequences.
    @Test
    void doesNotThrowOnWeirdUnicode() {
        System.out.println("BEFORE: input=\"\\uD83D\" (lone high surrogate, malformed Unicode sequence)");
        System.out.println("EXPECTING: no exception — sanitizer must survive malformed sequences without throwing");
        assertDoesNotThrow(() -> StringSanitizer.sanitize("\uD83D"));
        System.out.println("AFTER:  no exception thrown");
    }
}