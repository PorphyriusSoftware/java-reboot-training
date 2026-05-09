# Module 1 — String Sanitizer

Module 1 introduces the `StringSanitizer` utility, a foundational class focused
on predictable string normalization. This module establishes the baseline
patterns used in later modules: deterministic behavior, clear rules, and
test-driven development.

---

## StringSanitizer

### Overview
`StringSanitizer` provides a deterministic way to clean and normalize strings.
It removes control characters, trims whitespace, collapses repeated spaces, and
ensures consistent output across all inputs.

---

## Behavior Summary

### `sanitize(String input)`
- Returns `null` when input is `null`
- Trims leading and trailing whitespace
- Removes control characters (`\n`, `\t`, etc.)
- Collapses multiple spaces into a single space
- Returns an empty string only when the sanitized content is empty

---

## Examples

| Input                     | Output        |
|--------------------------|---------------|
| `"  hello  "`            | `"hello"`     |
| `"a\tb\nc"`              | `"abc"`       |
| `"   "`                  | `""`          |
| `null`                   | `null`        |
| `"hello    world"`       | `"hello world"` |

---

## Tests

`StringSanitizerTest` validates:

- null handling
- whitespace trimming
- control character removal
- space collapsing
- empty-string behavior
- Unicode stability

This module establishes the baseline for clean, predictable string processing.
