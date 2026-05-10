# Module 4 — Predicate Composition and Generic Collection Pipelines

Module 4 introduces functional abstractions for working with predicates,
collections, and higher-order pipelines. Unlike previous modules, which focused
on specific data types, this module provides generic, reusable building blocks
that operate on any type.

The module contains three components:

- `PredicateUtils` — predicate composition helpers
- `CollectionFilter` — generic filtering and mapping utilities
- `SmartCollectionFilter` — a composed higher-order pipeline

All utilities are designed to be:

- null-safe
- deterministic
- side-effect-free
- suitable for functional pipelines
- compliant with strict Javadoc validation

---

## PredicateUtils

Provides null-safe predicate composition helpers.

### Features

- `and(p1, p2)`  
  True only when both predicates are true.

- `or(p1, p2)`  
  True when at least one predicate is true.

- `not(p)`  
  Negates the predicate.

- `xor(p1, p2)`  
  True when exactly one predicate is true.

- `alwaysTrue()`  
  Always returns true.

- `alwaysFalse()`  
  Always returns false.

### Null-Safety Rules

- Null predicates never throw exceptions.
- Null inputs return safe fallback predicates.
- Composition always produces a valid predicate.

---

## CollectionFilter

Provides generic filtering and mapping operations for lists.

### Features

- `filter(List<T>, Predicate<T>)`  
  Returns only elements matching the predicate.

- `map(List<T>, Function<T,R>)`  
  Transforms each element using the mapper.

- `filterAndMap(List<T>, Predicate<T>, Function<T,R>)`  
  First filters, then maps.

### Null-Safety Rules

- Null input → empty list
- Null predicate → empty list
- Null mapper → empty list
- No exceptions are thrown

---

## SmartCollectionFilter

A composed pipeline that applies:

1. filtering
2. mapping

This class provides a higher-level abstraction over `CollectionFilter`.

### Features

- Null-safe pipeline execution
- Deterministic behavior
- Generic type support
- No side effects

### Example

Input:

["a", "", "bbb"]


Predicate:

s -> !s.isEmpty()


Mapper:

String::length


Output:

[1, 3]


---

## Testing

Module 4 includes a senior-grade test suite covering:

- predicate algebra
- null-safety
- composition behavior
- pipeline order
- mixed input handling
- deterministic output
- functional purity

---

## Summary

Module 4 elevates the codebase from type-specific utilities to generic,
behavior-driven abstractions. These utilities form the foundation for more
advanced functional patterns in later modules.
