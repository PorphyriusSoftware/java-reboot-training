package com.porphyriussoftware.module006;

import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * A custom Spliterator implementation used in Module 6 of the Java Reboot Training course.
 *
 * <p>This Spliterator iterates over a fixed {@link List} of integers and demonstrates
 * how to implement the core Spliterator contract:
 *
 * <ul>
 *     <li>{@link #tryAdvance(Consumer)} — delivers one element at a time</li>
 *     <li>{@link #trySplit()} — splits the remaining range in half for parallel execution</li>
 *     <li>{@link #estimateSize()} — returns the exact number of remaining elements</li>
 *     <li>{@link #characteristics()} — declares {@code ORDERED}, {@code SIZED}, and {@code SUBSIZED}</li>
 * </ul>
 *
 * <p>Splitting uses an {@code index}/{@code fence} range over a shared source list,
 * so no data is copied on split — all derived spliterators reference the same list
 * with different bounds.</p>
 */
public final class CustomIntSpliterator implements Spliterator<Integer> {

    /**
     * The underlying source list.
     */
    private final List<Integer> source;

    /**
     * The current index position within the list.
     */
    private int index;

    /**
     * The exclusive upper bound of this spliterator's range.
     */
    private final int fence;

    /**
     * Creates a new Spliterator over the given list.
     *
     * @param source the list of integers to iterate over
     */
    public CustomIntSpliterator(List<Integer> source) {
        this.source = source;
        this.index = 0;
        this.fence = source.size();
    }

    /**
     * Creates a Spliterator covering a sub-range of the given list.
     * Used internally by {@link #trySplit()} to produce the left half without copying data.
     *
     * @param source the shared source list
     * @param index  the inclusive start index of this spliterator's range
     * @param fence  the exclusive end index of this spliterator's range
     */
    private CustomIntSpliterator(List<Integer> source, int index, int fence) {
        this.source = source;
        this.index = index;
        this.fence = fence;
    }

    /**
     * Attempts to advance the Spliterator by one element.
     *
     * <p>If an element is available, it must be passed to the provided action,
     * the index must be incremented, and the method must return {@code true}.
     *
     * <p>If no elements remain, the method must return {@code false}.
     *
     * @param action the action to apply to the next element
     * @return {@code true} if an element was consumed, {@code false} otherwise
     */
    @Override
    public boolean tryAdvance(Consumer<? super Integer> action) {
        if (index < fence) {
            action.accept(source.get(index++));
            return true;
        }
        return false;
    }

    /**
     * Attempts to split this Spliterator into two parts.
     *
     * <p>The basic version may split the remaining range in half.
     * If the remaining size is too small to split, this method must return {@code null}.
     *
     * @return a new Spliterator covering part of the remaining elements, or {@code null}
     */
    @Override
    public Spliterator<Integer> trySplit() {
        if (estimateSize() <= 1) return null;

        int mid = index + (fence - index) / 2;

        CustomIntSpliterator result = new CustomIntSpliterator(source, index, mid);
        index = mid;

        return result;
    }

    /**
     * Returns an estimate of the number of remaining elements.
     *
     * @return the estimated size
     */
    @Override
    public long estimateSize() {
        return fence - index;
    }

    /**
     * Returns the characteristics of this Spliterator.
     *
     * <p>At minimum, this Spliterator must be:
     * <ul>
     *     <li>{@link Spliterator#ORDERED}</li>
     *     <li>{@link Spliterator#SIZED}</li>
     *     <li>{@link Spliterator#SUBSIZED}</li>
     * </ul>
     *
     * @return the characteristics bitmask
     */
    @Override
    public int characteristics() {
        return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
    }


}
