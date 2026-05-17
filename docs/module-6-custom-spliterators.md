# Module 6 — Custom Spliterators & Pipeline Fusion

## Overview

Module 6 goes one layer deeper than collectors. While collectors define *what to do* with elements, Spliterators define *how elements are delivered* to the pipeline in the first place.

Every stream you have ever used has a Spliterator underneath it. When you call `list.stream()`, Java creates a `Spliterator<T>` over that list. The stream pipeline never touches the data directly — it talks to the Spliterator.

This module teaches you:

- how Spliterators deliver elements one at a time
- how parallel streams use Spliterators to split work across threads
- how to implement `tryAdvance`, `trySplit`, `estimateSize`, and `characteristics` correctly
- what `ORDERED`, `SIZED`, and `SUBSIZED` mean and why they matter
- how to use the `index/fence` pattern to split without copying data
- how pipeline fusion works internally

By the end of this module, you will understand how the stream engine drives data through a pipeline — and how to plug your own data source into it.

---

## 1. The Spliterator Contract

A Spliterator has one job: **deliver elements, one at a time, and optionally split itself in half for parallel work.**

It exposes four methods:

### `tryAdvance(Consumer<T> action)`

Processes the next element by passing it to the action, advances the internal cursor, and returns `true`. Returns `false` if no elements remain.

This is the heartbeat of the Spliterator. Sequential streams call it in a loop until it returns `false`.

```java
@Override
public boolean tryAdvance(Consumer<? super Integer> action) {
    if (index < fence) {
        action.accept(source.get(index++));
        return true;
    }
    return false;
}
```

---

### `trySplit()`

Splits the remaining range in half. Returns a new Spliterator covering the left half, and updates itself to cover the right half. Returns `null` if the remaining range is too small to split.

This is what makes parallel streams possible. The `ForkJoinPool` calls `trySplit()` recursively — splitting, splitting, splitting — until each piece is small enough for one thread.

```java
@Override
public Spliterator<Integer> trySplit() {
    if (estimateSize() <= 1) return null;

    int mid = index + (fence - index) / 2;

    CustomIntSpliterator left = new CustomIntSpliterator(source, index, mid);
    index = mid;

    return left;
}
```

---

### `estimateSize()`

Returns an estimate of the number of elements remaining. If the Spliterator declares `SIZED`, this must be exact.

The `ForkJoinPool` uses this to decide when to stop splitting. If you lie here, you get unbalanced work distribution.

```java
@Override
public long estimateSize() {
    return fence - index;
}
```

---

### `characteristics()`

Returns a bitmask of flags describing the data. The stream pipeline reads these flags to decide what optimizations are safe to apply.

```java
@Override
public int characteristics() {
    return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
}
```

---

## 2. Characteristics Flags

Each flag is a bit in an `int` bitmask. You combine them with `|` and check them with `&`.

| Flag | What it promises |
|------|-----------------|
| `ORDERED` | Elements have a defined encounter order |
| `SIZED` | `estimateSize()` returns the exact count |
| `SUBSIZED` | Both halves after `trySplit()` are also `SIZED` |
| `SORTED` | Elements are sorted (requires `ORDERED`) |
| `DISTINCT` | No duplicates |
| `NONNULL` | No null elements |
| `IMMUTABLE` | Source will not change during traversal |
| `CONCURRENT` | Source can be modified concurrently |

### Why this matters

- Declaring `SIZED` without keeping `estimateSize()` accurate causes unbalanced parallel splits.
- Forgetting `SUBSIZED` means the `ForkJoinPool` cannot trust the sizes of the split halves.
- Declaring `ORDERED` when order is not guaranteed makes the pipeline pay ordering costs it did not need to pay.
- Forgetting `ORDERED` when order IS guaranteed means parallel execution can reorder your elements.

**Getting these wrong is the number one source of subtle parallel bugs.**

---

## 3. The `index / fence` Pattern

The naive approach to splitting is to create a `subList` view for each half:

```java
// naive — creates a new List object per split
return new CustomIntSpliterator(source.subList(index, mid));
```

This works, but it allocates a new `SubList` object on every split. In a recursive split tree over 10,000 elements, that is thousands of unnecessary allocations.

The production pattern stores `index` (inclusive start) and `fence` (exclusive end) as fields, and shares the same source list reference across all splits:

```java
// production — zero allocations beyond the spliterator object itself
private CustomIntSpliterator(List<Integer> source, int index, int fence) {
    this.source = source;
    this.index  = index;
    this.fence  = fence;
}
```

All derived Spliterators read from the same list — they just have different `index` and `fence` values.

```
source = [1, 2, 3, 4, 5, 6, 7, 8]

Original:   index=0, fence=8

After split:
  left:     index=0, fence=4   ← returned
  right:    index=4, fence=8   ← original, updated
```

No data is copied. This is exactly how `ArrayList`'s own Spliterator works internally.

### Why this does not cause memory leaks

The source list already exists in memory. Spliterators do not keep it alive any longer than it would have been — they all go out of scope together when the stream pipeline finishes. The list is then garbage collected normally.

---

## 4. How Parallel Streams Use Spliterators

When you call `StreamSupport.stream(spliterator, true)`, the `ForkJoinPool` takes over:

```
1. Call trySplit() on the root → get left half
2. Call trySplit() on each half → get quarter halves
3. Keep splitting until trySplit() returns null (too small) or the pool has enough tasks
4. Assign each leaf to a thread
5. Each thread calls tryAdvance() in a loop on its leaf
6. Results are combined bottom-up
```

The quality of `trySplit()` directly determines how evenly the work is distributed. A lopsided split — where one half is much larger than the other — means one thread finishes instantly while another does all the work.

---

## 5. Pipeline Fusion

When you write:

```java
list.stream()
    .filter(x -> x > 5)
    .map(x -> x * 2)
    .collect(toList())
```

Java does not create intermediate lists between each step. Instead it builds a chain of `Sink` objects — each one wraps the next. The terminal operation (`collect`) drives the pipeline by asking the Spliterator for elements via `tryAdvance`, and each element travels the full chain before the next one starts.

This is **pipeline fusion** — the operations are fused into a single pass over the data.

Understanding this explains:

- Why stateful operations (`sorted`, `distinct`) force a pipeline break — they must see all elements before producing any
- Why short-circuiting operations (`findFirst`, `limit`) can stop the Spliterator early
- Why adding more intermediate operations does not add more passes over the data

---

## 6. Common Spliterator Failures

### Wrong `mid` calculation after partial consumption

```java
// BUG: mid is a count, but used as an absolute index
int mid = (fence - index) / 2;
source.subList(index, mid); // wrong when index > 0

// CORRECT: mid must be an absolute position
int mid = index + (fence - index) / 2;
```

### Violating `SUBSIZED`

After `trySplit()`, both halves must have accurate `estimateSize()`. If the sizes do not sum to the original, the `ForkJoinPool` cannot balance work.

### Aliasing between split halves

If both halves share the same cursor (the `index` field), advancing one will skip elements in the other. Each spliterator must have its own independent `index`.

### Claiming `ORDERED` then delivering out of order

If your `trySplit()` gives the left half elements that come after the right half's elements, you have violated `ORDERED`. Always ensure the left half covers the lower index range.

---

## 7. Exercises

### Exercise 1 — Implement `tryAdvance`
Requirements:
- pass the current element to the action
- increment the cursor
- return `true` if an element was consumed, `false` otherwise
- respect `fence` — do not read past it

### Exercise 2 — Implement `trySplit`
Requirements:
- return `null` if fewer than 2 elements remain
- split the remaining range in half using the `index/fence` pattern
- update `index` to the midpoint
- return a new Spliterator covering `[index, mid)`

### Exercise 3 — Implement `estimateSize`
Requirements:
- return `fence - index`
- must be exact (you declared `SIZED`)
- must decrease as `tryAdvance` is called

### Exercise 4 — Implement `characteristics`
Requirements:
- return `ORDERED | SIZED | SUBSIZED`
- both halves after `trySplit()` must also return the same flags

---

## 8. Ultra Instinct Test Suite

This module's Ultra Instinct suite validates:

- `SUBSIZED` contract — `left.estimateSize() + right.estimateSize() == original`
- split correctness after partial consumption — the mid-index bug trap
- balanced splits — neither half is more than 2× the other
- characteristics preserved through splits
- no aliasing between split halves — draining one must not affect the other
- recursive split coverage — all elements collected with no drops or duplicates
- parallel stream determinism — same sorted result across 10 repeated runs
- parallel stream correctness on large input — 10,000 elements, no duplicates, no missing

Passing this suite means the implementation is production-grade.

---

## 9. Summary

Module 6 teaches you how to:

- understand what drives every stream pipeline at the lowest level
- implement all four Spliterator methods correctly
- use the `index/fence` pattern for zero-copy splitting
- declare the right characteristics flags and understand the consequences
- reason about parallel work distribution
- avoid the most common Spliterator bugs

This knowledge is the foundation for Module 7, where you will build custom intermediate pipeline operations using the same `Sink` mechanism that the JDK uses internally.
