# Module 5 — Custom Collectors (Advanced Stream Composition)

## Overview

Module 5 introduces custom collectors, the most advanced part of the Stream API. Collectors allow you to define exactly how a stream should accumulate, combine, and finish its result — including full control over parallel execution.

This module teaches you:

- how collectors work internally
- how to design correct collectors
- how to write collectors that survive parallel execution
- how to avoid aliasing, shared state, and broken combiners
- how to build your own `Collectors.toList()`, `groupingBy`, and `joining` equivalents

By the end of this module, you’ll understand collectors deeply enough to write your own high‑performance, parallel‑safe aggregation logic.

---

## 1. Collector Anatomy

A collector is defined by four components:

### Supplier
Creates the mutable container used during accumulation.

Examples:
- `ArrayList::new`
- `HashMap::new`
- `() -> new StringJoiner("-")`

Each parallel thread gets its own container.

### Accumulator
Adds one element into the container.

Examples:
- `(list, value) -> list.add(value)`
- `(map, value) -> map.merge(value, 1L, Long::sum)`

Runs many times per thread.

### Combiner
Merges two partial containers.

Example:
```java
(left, right) -> { left.addAll(right); return left; }
```

Runs heavily in parallel mode.

### Finisher
Converts the container into the final result.

Examples:
- `List::copyOf`
- `StringJoiner::toString`
- `Function.identity()`

---

## 2. Collector Design Patterns

### Pattern A — Bucket + Freeze (Immutable Results)

Used for:
- immutable lists
- immutable sets
- immutable maps

Example:
```java
Collector.of(
    ArrayList::new,
    List::add,
    (left, right) -> { left.addAll(right); return left; },
    List::copyOf
);
```

---

### Pattern B — Per‑Thread Map + Merge (Frequency, Indexing)

Used for:
- frequency maps
- grouping by key
- counting occurrences

Example:
```java
Collector.of(
    HashMap::new,
    (map, value) -> map.merge(value, 1L, Long::sum),
    (left, right) -> {
        right.forEach((k, v) -> left.merge(k, v, Long::sum));
        return left;
    }
);
```

---

### Pattern C — Map of Lists (Grouping)

Used for:
- grouping by length
- grouping by category
- grouping by status

Example:
```java
Collector.of(
    HashMap::new,
    (map, value) -> {
        int key = value.length();
        map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    },
    (left, right) -> {
        right.forEach((k, v) ->
            left.merge(k, v, (l1, l2) -> { l1.addAll(l2); return l1; })
        );
        return left;
    }
);
```

---

### Pattern D — Builder + Finisher (Joining, Aggregation)

Used for:
- joining strings
- building complex objects
- aggregating statistics

Example:
```java
Collector.of(
    () -> new StringJoiner("-"),
    StringJoiner::add,
    StringJoiner::merge,
    StringJoiner::toString
);
```

---

## 3. Parallel Stream Behavior

Parallel streams split the input into chunks:

```
Chunk A → Thread 1
Chunk B → Thread 2
Chunk C → Thread 3
Chunk D → Thread 4
```

Each chunk gets its own accumulator container.

The combiner merges these containers in an undefined order, so collectors must be:

- associative
- deterministic
- free of shared state
- free of aliasing

If your combiner is wrong, parallel execution will break your collector.

---

## 4. Common Collector Failures

### Shared mutable state
Two threads writing into the same list or map.

### Non‑associative combiner
Merging partial results incorrectly.

### Aliasing
Two keys sharing the same list instance.

### Incorrect finisher
Returning a mutable container when immutability is required.

### Incorrect accumulator
Using `containsKey` + `get` instead of `merge` or `computeIfAbsent`.

---

## 5. Ultra Instinct Requirements

Your collectors must:

- work in sequential mode
- work in parallel mode
- produce deterministic results
- avoid shared state
- avoid aliasing
- use correct combiners
- preserve encounter order where required
- freeze results when required
- scale across multiple cores

This module’s test suite is designed to break incorrect collectors immediately.

---

## 6. Exercises

### Exercise 1 — Implement `toImmutableList()`
Requirements:
- preserve encounter order
- return an unmodifiable list
- parallel‑safe

### Exercise 2 — Implement `frequencyMap()`
Requirements:
- count occurrences
- merge partial maps correctly
- no aliasing
- parallel‑safe

### Exercise 3 — Implement `groupByLength()`
Requirements:
- group strings by length
- preserve encounter order inside each group
- avoid list aliasing
- merge lists correctly

### Exercise 4 — Implement `joiningWithDash()`
Requirements:
- join with `-`
- no trailing dash
- empty stream → empty string
- associative combiner

---

## 7. Ultra Instinct Test Suite

This module includes the Ultra Instinct test suite, which validates:

- associativity
- parallel correctness
- combiner correctness
- identity finisher correctness
- no shared state
- no aliasing
- deterministic behavior

Passing this suite means your collectors are production‑grade.

---

## 8. Summary

Module 5 teaches you how to:

- design custom collectors
- reason about parallel execution
- avoid common pitfalls
- build high‑performance aggregation logic
- understand how the Stream API works internally

This is one of the most advanced parts of Java’s functional programming model — mastering it gives you deep control over stream behavior.
