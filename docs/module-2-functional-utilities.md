# Module 2 — Functional Utilities

Module 2 introduces a set of functional-style utilities built around Java's
`Optional` and `Stream` APIs. The goal is to write expressive, null-safe,
pipeline-driven transformations without imperative branching.

This module contains three utilities:

- `OptionalUtils` — null-safe string cleaning and transformation
- `StreamUtils` — functional list processing
- `SmartFilter` — a composed pipeline that fuses both utilities

All utilities are fully covered by `FunctionalUtilitiesTest`, which includes
core behavior tests and extended edge-case scenarios.

---

## OptionalUtils

### `clean(String input, String defaultValue)`
Normalizes a string by trimming whitespace and returning a default value when
the input is null or blank.

**Examples**
- `"  hello  "` → `"hello"`
- `"   "` → `"default"`
- `null` → `"default"`

### `process(String input)`
Trims, validates, and uppercases a string. Returns `null` when the input is
null or blank.

**Examples**
- `"  hello  "` → `"HELLO"`
- `"   "` → `null`
- `"é"` → `"É"`

---

## StreamUtils

### `filterStartingWith(List<String> input, String prefix)`
Cleans each element using `OptionalUtils.clean`, removes blanks, and returns
only values that start with the given prefix.

### `uppercaseAll(List<String> input)`
Uppercases all non-null, non-blank values using `OptionalUtils.process`.

### `sum(List<Integer> input)`
Returns the sum of all integers in the list. Null lists are treated as empty.

---

## SmartFilter

### `cleanAndFilter(List<String> input, String prefix)`
A composed pipeline:

1. Clean and normalize values
2. Filter by prefix
3. Uppercase results

This demonstrates functional composition across modules.

---

## Tests

`FunctionalUtilitiesTest` includes:

- core behavior tests
- null-list handling
- null-element handling
- blank-string handling
- Unicode behavior
- case-sensitivity rules
- no-match scenarios
- SmartFilter integration tests

This suite ensures predictable, production-grade behavior across all utilities.
