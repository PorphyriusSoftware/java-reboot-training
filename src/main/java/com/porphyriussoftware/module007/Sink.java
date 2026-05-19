package com.porphyriussoftware.module007;

/**
 * Receives elements as they flow through a pipeline stage.
 *
 * <p>A {@code Sink} is the push-side counterpart to a {@link java.util.Spliterator}.
 * While a Spliterator <em>pulls</em> elements from a source, a Sink <em>receives</em>
 * elements that are pushed into it by the pipeline driver.
 *
 * <p>A pipeline is a chain of Sinks. Each intermediate operation (filter, map, flatMap)
 * wraps a downstream Sink in a new Sink that applies its transformation before forwarding.
 * The terminal operation (toList, forEach, count) sits at the end of the chain as the
 * final Sink that produces the result.
 *
 * <p>Lifecycle of a Sink during evaluation:
 * <ol>
 *     <li>{@link #begin(long)} is called once before the first element, with the known
 *         size or {@code -1} if unknown.</li>
 *     <li>{@link #accept(Object)} is called once per element.</li>
 *     <li>{@link #end()} is called once after the last element.</li>
 * </ol>
 *
 * <p>Sinks that chain to a downstream must forward {@code begin} and {@code end} calls.
 * Use {@link CustomPipeline.ChainedSink} as a base class to get this for free.
 *
 * @param <T> the type of elements this Sink receives
 */
public interface Sink<T> {

    /**
     * Receives one element and processes it.
     *
     * <p>For an intermediate stage, this typically means applying a transformation
     * or predicate, then forwarding to the downstream Sink.
     *
     * @param element the element to process
     */
    void accept(T element);

    /**
     * Called once before the first element is pushed.
     *
     * <p>Use this to pre-size containers or prepare state. The size is exact if
     * the upstream Spliterator declared {@link java.util.Spliterator#SIZED}; otherwise
     * it is {@code -1}.
     *
     * <p>Default implementation does nothing.
     *
     * @param size the exact number of elements to be pushed, or {@code -1} if unknown
     */
    default void begin(long size) {}

    /**
     * Called once after the last element has been pushed.
     *
     * <p>Use this to finalize results or release resources.
     *
     * <p>Default implementation does nothing.
     */
    default void end() {}

    /**
     * Returns {@code true} if this Sink no longer wants to receive elements.
     *
     * <p>Short-circuiting terminal operations (e.g. {@code findFirst}) set this to
     * {@code true} once they have their answer, allowing the pipeline driver to stop
     * early without processing the remaining elements.
     *
     * <p>Default implementation returns {@code false} — consume everything.
     *
     * @return {@code true} if the pipeline driver should stop pushing elements
     */
    default boolean cancellationRequested() {
        return false;
    }
}
