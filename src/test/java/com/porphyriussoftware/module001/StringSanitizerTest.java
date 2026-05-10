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
        assertEquals("Hello", StringSanitizer.sanitize("   Hello   "));
    }

    // Ensures multiple internal spaces collapse into a single space.
    @Test
    void collapsesMultipleSpaces() {
        assertEquals("John Doe", StringSanitizer.sanitize("John    Doe"));
    }

    // Verifies control characters are treated as whitespace separators.
    @Test
    void removesNonPrintableCharactersAsSpaces() {
        String dirty = "Hello\u0007World"; // BEL control char
        assertEquals("Hello World", StringSanitizer.sanitize(dirty));
    }

    // Confirms Unicode is normalized to NFC form.
    @Test
    void normalizesUnicode() {
        String composed = "José";
        String decomposed = "Jose\u0301";
        assertEquals(composed, StringSanitizer.sanitize(decomposed));
    }

    // Ensures null input returns null.
    @Test
    void returnsNullForNullInput() {
        assertNull(StringSanitizer.sanitize(null));
    }

    // Ensures empty/whitespace-only input returns null after sanitization.
    @Test
    void returnsNullForEmptyAfterCleaning() {
        assertNull(StringSanitizer.sanitize("     \n\t   "));
    }

    // Validates combined behavior: control chars → spaces, whitespace collapse, Unicode normalization.
    @Test
    void handlesMixedGarbage() {
        String input = "   Jo\u0007se\u0301   \n\n   ";
        assertEquals("Jo sé", StringSanitizer.sanitize(input));
    }

    // Ensures sanitizer does not throw on malformed or partial Unicode sequences.
    @Test
    void doesNotThrowOnWeirdUnicode() {
        assertDoesNotThrow(() -> StringSanitizer.sanitize("\uD83D"));
    }
}
