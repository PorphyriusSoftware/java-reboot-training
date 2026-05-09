# Module 3 — Functional Numeric Pipelines

Module 3 introduces a set of numeric utilities designed to operate safely and
predictably inside functional pipelines. The focus is on null‑safety, strict
input validation, and deterministic transformations.

This module contains three components:

- `NumberUtils` — low‑level numeric helpers
- `NumberStreamUtils` — functional list operations
- `SmartNumberFilter` — a composed numeric processing pipeline

All utilities are designed to be:
- null‑safe
- side‑effect‑free
- predictable
- suitable for stream pipelines
- compliant with strict Javadoc validation

---

## NumberUtils

Low‑level helpers for working with `Integer` values.

### Features

- **safeParse(String)**  
  Parses a string into an `Integer`. Returns `null` for invalid or null input.

- **isPositive(Integer)**  
  Returns `true` only for strictly positive values. Null and zero return `false`.

- **isNegative(Integer)**  
  Returns `true` only for strictly negative values. Null and zero return `false`.

- **clamp(Integer, Integer, Integer)**  
  Clamps a value to the inclusive range `[min, max]`.  
  Returns `null` if the value or either bound is `null`.

### Design Notes

- No exceptions are thrown for invalid input.
- No silent coercion is performed.
- Null in → null out.

---

## NumberStreamUtils

Functional list operations for numeric pipelines.

### Features

- **filterPositive(List<Integer>)**  
  Returns only strictly positive values. Nulls are ignored.

- **filterNegative(List<Integer>)**  
  Returns only strictly negative values. Nulls are ignored.

- **doubleAll(List<Integer>)**  
  Doubles all non‑null values. Nulls are ignored.

- **sum(List<Integer>)**  
  Sums all non‑null values. Returns `0` for null or empty input.

- **average(List<Integer>)**  
  Computes the arithmetic mean of non‑null values.  
  Returns `null` for null, empty, or all‑null input.

### Design Notes

- All methods return empty lists instead of null.
- No exceptions are thrown.
- Behavior is deterministic and pipeline‑friendly.

---

## SmartNumberFilter

A composed numeric pipeline that processes a list of strings into a list of
doubled, clamped integers.

### Pipeline Steps

1. Parse strings into integers using `safeParse`.
2. Filter out:
    - null values
    - values outside the inclusive range `[min, max]`
3. Clamp the remaining values.
4. Double the clamped values.
5. Return the transformed list.

### Important Rule

Values outside the allowed range are **discarded before clamping**.  
Clamping is not used to rescue invalid inputs.

### Example

Input:

["5", "-1", "20", "abc", "7"]


Range:

min = 1, max = 10


Output:

[10, 14]


Only `5` and `7` are in range.  
`20` is discarded before clamping.

---

## Testing

Module 3 includes a senior‑grade test suite covering:

- null‑safety
- parsing correctness
- range filtering
- clamping behavior
- doubling logic
- pipeline order
- mixed garbage input
- empty and null inputs

All tests are written to enforce strict, predictable behavior.

---

## Summary

Module 3 establishes a clean, functional foundation for numeric processing:

- predictable behavior
- no silent coercion
- null‑safe utilities
- strict Javadoc compliance
- deterministic pipelines

This module prepares the groundwork for more advanced transformations in later modules.
