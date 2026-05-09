package com.porphyriussoftware;

import java.text.Normalizer;

/**
 * Utility class for cleaning and normalizing user-provided text.
 *
 * Sanitization rules:
 * <ul>
 *   <li>Unicode is normalized using NFC form.</li>
 *   <li>Control characters (\\p{Cntrl}) are replaced with spaces.</li>
 *   <li>All whitespace sequences collapse into a single space.</li>
 *   <li>Leading and trailing whitespace is trimmed.</li>
 *   <li>If the final result is empty, {@code null} is returned.</li>
 * </ul>
 *
 * <p>This sanitizer is intentionally simple and deterministic.
 * It avoids complex heuristics and ensures predictable output
 * suitable for names, labels, and general user input.</p>
 *
 * <p>This class is not meant to be instantiated.</p>
 */
public final class StringSanitizer {

    private StringSanitizer() {}

    /**
     * Sanitizes the given input string according to the rules described above.
     *
     * @param input the raw user-provided text, or {@code null}
     * @return the sanitized string, or {@code null} if no meaningful content remains
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }

        // Normalize Unicode first
        String result = Normalizer.normalize(input, Normalizer.Form.NFC);

        // 1) Remove control chars → treat as spaces
        result = result.replaceAll("\\p{Cntrl}+", " ");

        // 3) Collapse whitespace, and trim the whole string. Outer spaces
        result = result.replaceAll("\\s+", " ").trim();

        return result.isEmpty() ? null : result;
    }


}
