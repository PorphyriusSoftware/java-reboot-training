# Module 7 — Custom Pipeline (Pipeline Fusion & the Sink Model)

## Overview

Module 7 goes one layer deeper than spliterators. While a `Spliterator` defines how elements are *pulled* from a source, the `Sink` model defines what happens to each element *once it arrives*.

This is the mechanism behind every stream pipeline you have ever written. When you chain `.filter().map().collect()`, Java does not create an intermediate list between each step. It builds a chain of `Sink` objects — each one wrapping the next — and each element travels the full chain before the next element starts. That is **pipeline fusion**.

This module teaches you:

- the difference between pull (Spliterator) and push (Sink)
- how the Sink lifecycle works: `begin`, `accept`, `end`
- how short-circuiting propagates through `cancellationRequested()`
- how intermediate operations register stages without executing them (lazy evaluation)
- how the evaluation engine builds the Sink chain and drives it
- how pipeline reusability requires storing a `Collection`, not a `Spliterator`
- how `flatMap` must explicitly check cancellation inside its inner loop
- why `accept` must return `void` to align with `Consumer<T>`

By the end of this module, you will understand exactly what happens inside a stream pipeline at the level the JDK implements it.

---

## 1. Pull vs Push

A `Spliterator` is a **pull** model. The pipeline asks it for elements:

```java
// the pipeline calls tryAdvance — it pulls the next element
spliterator.tryAdvance(element -> process(element));
```

A `Sink` is a **push** model. The pipeline driver pushes elements into it:

```java
// the driver calls accept — it pushes the element into the sink
sink.accept(element);
```

The `Spliterator` and the `Sink` meet in the evaluation engine: the driver calls `tryAdvance(sink::accept)` in a loop. The spliterator pulls one element from the source and immediately pushes it into the sink chain.

---

## 2. The Sink Lifecycle

Every `Sink` has four methods:

### `accept(T element)`

Called once per element. This is where the stage's work happens — filtering, transforming, collecting. Modelled after `Consumer<T>`, it returns `void` because it is a pure side-effect. No return value is needed.

```java
@Override
public void accept(Integer element) {
    if (element % 2 == 0) {
        downstream.accept(element);
    }
}
```

### `begin(long size)`

Called once before the first element. The `size` is the exact count if the source declared `SIZED`, or `-1` if unknown. Use it to pre-size containers.

```java
@Override
public void begin(long size) {
    if (size > 0) list = new ArrayList<>((int) size);
}
```

### `end()`

Called once after the last element. Use it to finalize results or release resources.

### `cancellationRequested()`

Returns `true` if this Sink no longer wants to receive elements. Short-circuiting terminals (like `findFirst`) override this to return `true` once they have their answer. The pipeline driver checks this flag before each `tryAdvance` and stops early.

Default implementation returns `false` — consume everything.

---

## 3. SinkWrapper — Connecting Stages

A `SinkWrapper<IN, OUT>` is a factory for one pipeline stage. Given a downstream `Sink<OUT>`, it produces an upstream `Sink<IN>` that applies the stage's operation before forwarding.

```java
@FunctionalInterface
interface SinkWrapper<IN, OUT> {
    Sink<IN> wrap(Sink<OUT> downstream);
}
```

Example — a filter stage as a SinkWrapper:

```java
SinkWrapper<Integer, Integer> filterEven = downstream -> element -> {
    if (element % 2 == 0) {
        downstream.accept(element);
    }
};
```

This is a lambda that returns a lambda. The outer lambda receives `downstream`. The inner lambda is the `Sink` — it receives each element and decides whether to forward it.

Each intermediate operation (`filter`, `map`, `flatMap`) creates a `SinkWrapper` and appends it to the stages list. Nothing executes at this point.

---

## 4. ChainedSink — Forwarding Lifecycle Calls

Every intermediate `Sink` needs to forward three lifecycle calls to its downstream: `begin`, `end`, and `cancellationRequested`. Without this forwarding, short-circuiting breaks.

Consider a `filter → findFirst` pipeline:

```
evaluate() checks:  head.cancellationRequested()
                         ↑
              filter Sink forwards it
                         ↑
              findFirst Sink returns true
```

If `filter` does not forward `cancellationRequested()`, it always returns `false` (the default), and `evaluate()` never stops early — it processes the entire source even after `findFirst` has its answer.

`ChainedSink` is an abstract base class that handles this forwarding automatically. Subclasses only need to implement `accept`:

```java
new ChainedSink<Integer, Integer>(downstream) {
    @Override
    public void accept(Integer element) {
        if (predicate.test(element)) {
            downstream.accept(element);
        }
    }
};
```

`begin`, `end`, and `cancellationRequested` are all forwarded to `downstream` by `ChainedSink` without any extra work from the subclass.

---

## 5. Lazy Evaluation

Intermediate operations do **not** execute when you call them. They register a stage and return a new pipeline:

```java
CustomPipeline<Integer> pipeline = CustomPipeline.of(source)
    .filter(x -> x % 2 == 0)   // registers a filter stage — nothing runs
    .map(x -> x * 10);          // registers a map stage — nothing runs

// Nothing has happened to source yet.
// The map lambda has not been called once.

List<Integer> result = pipeline.toList(); // NOW everything runs
```

This is identical to how `java.util.stream.Stream` works. Lazy evaluation means:

- building a pipeline is cheap — it is just a list of stage descriptors
- you can branch from the same pipeline without re-processing the source
- short-circuiting terminals can stop the source before it is fully consumed

---

## 6. Pipeline Fusion

When `evaluate()` is called, it builds the Sink chain in **reverse** order — starting from the terminal Sink and wrapping backwards through the stages list:

```
stages = [filter, map]
terminal = toList Sink

Step 1: head = toList Sink
Step 2: head = map.wrap(toList Sink)      → map Sink wrapping toList
Step 3: head = filter.wrap(map Sink)      → filter Sink wrapping map

Result: filter → map → toList (head is filter)
```

The driver then pushes each element into `head`:

```
element 1 → filter Sink → (passes) → map Sink → toList Sink
element 2 → filter Sink → (fails, dropped)
element 3 → filter Sink → (passes) → map Sink → toList Sink
```

Each element travels the full chain before the next element starts. There is no intermediate list between filter and map. This is pipeline fusion.

---

## 7. The Evaluation Engine

```java
private void evaluate(Sink<T> terminalSink) {
    // Step 1 — build the Sink chain in reverse
    Sink head = terminalSink;
    for (int i = stages.size() - 1; i >= 0; i--) {
        head = stages.get(i).wrap(head);
    }

    // Step 2 — get a fresh cursor and signal start
    Spliterator spliterator = source.spliterator();
    head.begin(spliterator.estimateSize());

    // Step 3 — drive: push elements until source is empty or cancelled
    while (!head.cancellationRequested() && spliterator.tryAdvance(head::accept)) {}

    // Step 4 — signal end
    head.end();
}
```

Two things to notice:

**`head::accept` as a `Consumer`** — `tryAdvance` expects a `Consumer<T>` (void accept). Because `Sink.accept` returns `void`, the method reference matches exactly. In older designs where `accept` returned `boolean`, the compiler had to silently discard the return value — a type mismatch that compiled but lied about the contract.

**The loop condition checks cancellation first** — if `head.cancellationRequested()` is already `true`, `tryAdvance` is never called. This matters when the terminal immediately signals cancellation before processing any elements.

---

## 8. Short-Circuiting

Short-circuiting terminals override `cancellationRequested()` to return `true` once they have their answer.

`findFirst` uses a `boolean found` flag — not the stored value — as its sentinel:

```java
boolean[] found = {false};
Object[]  result = {null};

new Sink<T>() {
    @Override
    public void accept(T element) {
        if (!found[0]) {
            found[0] = true;
            result[0] = element;
        }
    }

    @Override
    public boolean cancellationRequested() {
        return found[0];
    }
}
```

**Why `found` and not `result[0] != null`?**

Using null as a sentinel breaks when the first element is `null`. If `result[0] = null` and the element is `null`, `result[0]` does not change, `cancellationRequested()` returns `false`, and the pipeline keeps running — then stores the *second* element as if it were the first. The `found` flag is independent of the element's value.

---

## 9. Pipeline Reusability — Collection vs Spliterator

A `Spliterator` is a cursor. Once consumed, it is empty. Storing a `Spliterator` as the source means the second terminal operation always sees an empty pipeline.

Storing a `Collection` instead means every terminal operation calls `source.spliterator()` to get a fresh cursor:

```java
// each call to evaluate() gets its own cursor — independent of prior runs
Spliterator spliterator = source.spliterator();
```

This makes branching from the same base pipeline safe:

```java
CustomPipeline<Integer> base = CustomPipeline.of(source);

List<Integer> evens = base.filter(x -> x % 2 == 0).toList(); // fresh cursor
List<Integer> odds  = base.filter(x -> x % 2 != 0).toList(); // fresh cursor
```

If `source` were stored as a `Spliterator`, `evens` would consume it and `odds` would always return an empty list.

---

## 10. flatMap and Cancellation

`flatMap` replaces each outer element with an inner collection. A naive implementation uses `Collection.forEach`:

```java
// WRONG — cannot stop early
mapper.apply(element).forEach(downstream::accept);
```

`Collection.forEach` is a plain loop with no stop condition. If a downstream short-circuit terminal (like `findFirst`) signals cancellation mid-way through the inner collection, `forEach` keeps pushing every remaining inner element anyway — it just can't stop.

The correct implementation uses an explicit loop with a cancellation check:

```java
// CORRECT — stops as soon as downstream signals cancellation
for (R inner : mapper.apply((T) element)) {
    if (downstream.cancellationRequested()) break;
    downstream.accept(inner);
}
```

The trade-off: we can stop at element boundaries within the inner collection, but we cannot stop mid-element. One overshoot per inner element is the minimum — and that is acceptable.

---

## 11. Common Pipeline Failures

### Not forwarding `cancellationRequested()` in intermediate stages
Short-circuiting never propagates. `findFirst` finds its answer but the driver keeps pushing thousands of elements.

### Storing a `Spliterator` as the source instead of the `Collection`
The first terminal operation consumes the cursor. Every subsequent terminal operation — and every branch — sees an empty pipeline.

### Mutating the stages list instead of copying it
Two branches built from the same base pipeline share the same `List<SinkWrapper>`. Adding a stage to one branch corrupts the other.

### Using `null` as a not-found sentinel in `findFirst`
If the first element is `null`, the check `result[0] == null` is true even after storing it. Cancellation never fires. The pipeline keeps running and returns the wrong element.

### Using `Collection.forEach` in `flatMap`
The inner loop cannot be stopped early. Short-circuiting from a downstream terminal has no effect inside the inner collection.

### Returning a non-void value from `accept`
`Sink.accept` is a push operation — a side-effect with no meaningful return. Returning `boolean` creates a false contract and prevents using the method reference as a `Consumer<T>` without silent type coercion.

---

## 12. Exercises

### Exercise 1 — Implement `filter`
Requirements:
- register a `SinkWrapper` that tests the predicate before forwarding
- return a new `CustomPipeline` with the stage appended — do not mutate the current stages list
- nothing executes at registration time

### Exercise 2 — Implement `map`
Requirements:
- register a `SinkWrapper` that applies the mapper and forwards the result
- the output type changes from `T` to `R`

### Exercise 3 — Implement `flatMap`
Requirements:
- register a `SinkWrapper` that iterates the inner collection
- check `downstream.cancellationRequested()` before each inner element
- use a for-each loop, not `Collection.forEach`

### Exercise 4 — Implement `toList`, `forEach`, `count`
Requirements:
- each creates a terminal `Sink` and calls `evaluate()`
- `toList` — add each element to an `ArrayList`
- `forEach` — delegate directly to the consumer
- `count` — increment a counter per element

### Exercise 5 — Implement `findFirst`
Requirements:
- use a `boolean[] found` flag as the sentinel — not null
- override `cancellationRequested()` to return `found[0]`
- return `Optional.ofNullable` of the stored value

### Exercise 6 — Implement `evaluate`
Requirements:
- build the Sink chain in reverse order
- call `begin` with the spliterator's estimated size
- drive with `while (!head.cancellationRequested() && spliterator.tryAdvance(head::accept))`
- call `end` after the loop

---

## 13. Ultra Instinct Test Suite

This module's Ultra Instinct suite validates:

- **Pipeline fusion** — `map` is called only on elements that passed `filter`, not on all elements. If `map` runs on all 6 elements when only 3 passed, an intermediate collection was created.
- **Short-circuit correctness** — `findFirst` on a 10,000-element list visits only a handful of elements, not all 10,000.
- **Pipeline immutability** — branching from the same base pipeline produces independent results. If the even-filter leaks into the odd-filter branch, stages are being mutated.
- **Deep chain composition** — four operations chained in sequence produce the correct result in the correct order.
- **flatMap + downstream composition** — `flatMap` followed by `filter` and `map` composes correctly.
- **Terminal consistency** — `count()` and `toList().size()` always agree. `forEach` and `toList` visit elements in the same order.

Passing this suite means the pipeline implementation is production-grade.

---

## 14. Summary

Module 7 teaches you how to:

- model the push side of a stream pipeline using `Sink`
- connect pipeline stages with `SinkWrapper` without executing anything
- propagate short-circuit signals up the chain via `cancellationRequested()`
- build the evaluation engine that fuses all stages into a single pass
- store a `Collection` instead of a `Spliterator` to support reuse and branching
- stop `flatMap` early by checking cancellation inside the inner loop
- align `accept` with `Consumer<T>` by returning `void`

This is the same mechanism the JDK uses internally. Understanding it means you can read the JDK source for `ReferencePipeline`, `AbstractPipeline`, and `Sink` — and recognize every pattern you built here.