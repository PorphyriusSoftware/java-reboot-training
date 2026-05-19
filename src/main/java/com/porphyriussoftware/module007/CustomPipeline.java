package com.porphyriussoftware.module007;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A lazy, composable pipeline over a sequence of elements.
 *
 * <p>This class demonstrates how Java's Stream API works internally:
 * intermediate operations are recorded but not executed immediately.
 * Execution only happens when a terminal operation is called — this is
 * called <strong>lazy evaluation</strong>.
 *
 * <p>Each intermediate operation (filter, map, flatMap) adds a
 * {@link SinkWrapper} to an ordered list of stages. A SinkWrapper knows
 * how to wrap a downstream {@link Sink} into an upstream {@link Sink},
 * applying its operation in the process. This wrapping is
 * <strong>pipeline fusion</strong> — no intermediate collections are created.
 *
 * <p>When a terminal operation is called, {@code evaluate()} builds the
 * Sink chain from terminal back to source, then drives the Spliterator:
 *
 * <pre>
 *   Source Spliterator
 *       ↓ tryAdvance
 *   filter Sink      ← wraps map Sink
 *       ↓ accept (if predicate passes)
 *   map Sink         ← wraps terminal Sink
 *       ↓ accept
 *   terminal Sink    ← collects result
 * </pre>
 *
 * <p>Diagram shows a {@code filter → map → toList()} pipeline.
 * Each element travels the full chain before the next element starts.
 * This is what the JDK calls a <em>single-pass, fused pipeline</em>.
 *
 * <p>Unlike {@link java.util.stream.Stream}, this pipeline is reusable.
 * Because the source is stored as a {@link Collection}, each terminal
 * operation calls {@link Collection#spliterator()} to get a fresh cursor.
 * Branching from the same base pipeline (two different filter calls on the
 * same instance) therefore produces fully independent results.
 *
 * @param <T> the type of elements in this pipeline
 */
public final class CustomPipeline<T> {

    /**
     * Connects two pipeline stages.
     *
     * <p>Given a downstream {@link Sink} of type {@code OUT}, a SinkWrapper
     * produces an upstream {@link Sink} of type {@code IN} that applies this
     * stage's operation before forwarding to the downstream.
     *
     * <p>Example — a filter stage:
     * <pre>
     *   SinkWrapper&lt;Integer, Integer&gt; filterStage = downstream -&gt; element -&gt; {
     *       if (predicate.test(element)) {
     *           downstream.accept(element);
     *       }
     *   };
     * </pre>
     *
     * @param <IN>  the type of elements entering this stage
     * @param <OUT> the type of elements leaving this stage
     */
    @FunctionalInterface
    interface SinkWrapper<IN, OUT> {

        /**
         * Wraps a downstream Sink in an upstream Sink for this stage.
         *
         * @param downstream the next Sink in the chain
         * @return a new Sink that applies this stage's operation and forwards to downstream
         */
        Sink<IN> wrap(Sink<OUT> downstream);
    }

    /**
     * Base class for intermediate Sinks that forward lifecycle calls to a downstream Sink.
     *
     * <p>Every intermediate pipeline stage (filter, map, flatMap) needs to forward
     * three calls to its downstream: {@link Sink#begin(long)}, {@link Sink#end()},
     * and {@link Sink#cancellationRequested()}. Without forwarding, a short-circuiting
     * terminal (e.g. {@code findFirst}) would never propagate its cancellation signal
     * back up the chain, and the pipeline driver would keep pushing elements forever.
     *
     * <p>Subclasses only need to implement {@link Sink#accept(Object)} — all other
     * forwarding is handled here.
     *
     * <p>Example — a filter stage:
     * <pre>
     *   new ChainedSink&lt;Integer, Integer&gt;(downstream) {
     *       {@literal @}Override
     *       public void accept(Integer element) {
     *           if (predicate.test(element)) {
     *               downstream.accept(element);
     *           }
     *       }
     *   };
     * </pre>
     *
     * @param <T> the type of elements entering this Sink
     * @param <D> the type of elements the downstream Sink expects
     */
    public abstract static class ChainedSink<T, D> implements Sink<T> {

        /**
         * The next Sink in the pipeline chain.
         */
        protected final Sink<D> downstream;

        /**
         * Creates a ChainedSink that forwards lifecycle calls to the given downstream.
         *
         * @param downstream the next Sink in the chain
         */
        protected ChainedSink(Sink<D> downstream) {
            this.downstream = downstream;
        }

        /**
         * Forwards {@code begin} to the downstream Sink.
         *
         * @param size the number of elements to be pushed, or {@code -1} if unknown
         */
        @Override
        public void begin(long size) {
            downstream.begin(size);
        }

        /**
         * Forwards {@code end} to the downstream Sink.
         */
        @Override
        public void end() {
            downstream.end();
        }

        /**
         * Forwards {@code cancellationRequested} to the downstream Sink.
         *
         * <p>This is what makes short-circuiting work. When a terminal Sink
         * (e.g. {@code findFirst}) has found its answer, it returns {@code true}
         * here. Without this forwarding, the pipeline driver would only check the
         * head Sink — which is an intermediate stage — and that stage would always
         * return {@code false} (the default), so the loop would never stop early.
         *
         * @return {@code true} if the downstream no longer wants to receive elements
         */
        @Override
        public boolean cancellationRequested() {
            return downstream.cancellationRequested();
        }

    }

    /**
     * The source of elements for this pipeline.
     *
     * <p>Stored as a {@link Collection} rather than a {@link Spliterator} so that
     * each call to {@code evaluate()} can obtain a fresh cursor via
     * {@link Collection#spliterator()}. This makes the pipeline reusable and
     * allows branching from the same base instance without one branch corrupting
     * the other.
     */
    private final Collection<? extends T> source;

    /**
     * The ordered list of pipeline stages, from first to last.
     * Types are erased internally, as in the JDK — each stage connects
     * the output type of the previous stage to the input type of the next.
     */
    @SuppressWarnings("rawtypes")
    private final List<SinkWrapper> stages;

    /**
     * Creates a pipeline with the given source and stage list.
     *
     * @param source the element source
     * @param stages the ordered list of pipeline stages
     */
    @SuppressWarnings("rawtypes")
    private CustomPipeline(Collection<? extends T> source, List<SinkWrapper> stages) {
        this.source = source;
        this.stages = stages;
    }

    // -------------------------------------------------------------------------
    // Factory methods
    // -------------------------------------------------------------------------

    /**
     * Creates a new pipeline over the elements of the given collection.
     *
     * <p>The collection is stored by reference — no copy is made. Each terminal
     * operation obtains a fresh {@link Spliterator} from the collection, so the
     * pipeline can be evaluated multiple times and supports branching.
     *
     * @param <T>    the element type
     * @param source the source collection
     * @return a new pipeline over the collection's elements
     */
    public static <T> CustomPipeline<T> of(Collection<? extends T> source) {
        return new CustomPipeline<>(source, new ArrayList<>());
    }

    // -------------------------------------------------------------------------
    // Intermediate operations
    // Each returns a NEW CustomPipeline with the stage appended.
    // Nothing executes here — this is lazy registration only.
    // -------------------------------------------------------------------------

    /**
     * Returns a pipeline that only passes elements matching the predicate.
     *
     * <p>This is a lazy, intermediate operation. The predicate is not evaluated
     * until a terminal operation drives the pipeline.
     *
     * <p>The SinkWrapper for filter:
     * <pre>
     *   downstream -&gt; element -&gt; {
     *       if (predicate.test(element)) downstream.accept(element);
     *   }
     * </pre>
     *
     * @param predicate the condition an element must satisfy to pass through
     * @return a new pipeline that filters elements
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public CustomPipeline<T> filter(Predicate<? super T> predicate) {
        SinkWrapper sinkWrapper = downstream -> new ChainedSink(downstream) {
            @Override
            public void accept(Object element) {
                if (predicate.test((T) element)) {
                    downstream.accept((T) element);
                }
            }
        };

        List<SinkWrapper> newStages = new ArrayList<>(stages);
        newStages.add(sinkWrapper);
        return new CustomPipeline<>(source, newStages);
    }

    /**
     * Returns a pipeline that applies the mapper to every element.
     *
     * <p>This is a lazy, intermediate operation. The mapper is not invoked
     * until a terminal operation drives the pipeline.
     *
     * <p>The SinkWrapper for map:
     * <pre>
     *   downstream -&gt; element -&gt; downstream.accept(mapper.apply(element))
     * </pre>
     *
     * @param <R>    the type of elements produced by the mapper
     * @param mapper the function to apply to each element
     * @return a new pipeline that transforms elements
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <R> CustomPipeline<R> map(Function<? super T, ? extends R> mapper) {
        SinkWrapper sinkWrapper = downstream -> new ChainedSink(downstream) {
            @Override
            public void accept(Object element) {
                downstream.accept(mapper.apply((T) element));
            }
        };

        List<SinkWrapper> newStages = new ArrayList<>(stages);
        newStages.add(sinkWrapper);
        return new CustomPipeline<>((Collection<? extends R>) source, newStages);
    }

    /**
     * Returns a pipeline that replaces each element with the elements of the
     * collection produced by the mapper.
     *
     * <p>This is a lazy, intermediate operation. The mapper is not invoked
     * until a terminal operation drives the pipeline.
     *
     * <p>The SinkWrapper for flatMap:
     * <pre>
     *   downstream -&gt; element -&gt; {
     *       for (R inner : mapper.apply(element)) {
     *           downstream.accept(inner);
     *       }
     *   }
     * </pre>
     *
     * @param <R>    the type of elements in the mapped collections
     * @param mapper the function that maps each element to a collection
     * @return a new pipeline that flattens the mapped collections
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <R> CustomPipeline<R> flatMap(Function<? super T, ? extends Collection<R>> mapper) {
        SinkWrapper sinkWrapper = downstream -> new ChainedSink(downstream) {
            @Override
            public void accept(Object element) {
                for (R inner : mapper.apply((T) element)) {
                    if (downstream.cancellationRequested()) break;
                    downstream.accept(inner);
                }
            }
        };

        List<SinkWrapper> newStages = new ArrayList<>(stages);
        newStages.add(sinkWrapper);
        return new CustomPipeline<>((Collection<? extends R>) source, newStages);
    }

    // -------------------------------------------------------------------------
    // Terminal operations
    // These trigger evaluation — the Sink chain is built and the Spliterator
    // is driven from here.
    // -------------------------------------------------------------------------

    /**
     * Collects all elements into a {@link List} and returns it.
     *
     * <p>This is a terminal operation. It triggers evaluation of the full pipeline.
     *
     * @return a list containing all elements that passed through the pipeline
     */
    public List<T> toList() {
        List<T> result = new ArrayList<>();
        evaluate(result::add);
        return result;
    }

    /**
     * Performs the given action for each element in the pipeline.
     *
     * <p>This is a terminal operation. It triggers evaluation of the full pipeline.
     *
     * @param action the action to perform on each element
     */
    public void forEach(Consumer<? super T> action) {
        evaluate(action::accept);
    }

    /**
     * Returns the number of elements in the pipeline.
     *
     * <p>This is a terminal operation. It triggers evaluation of the full pipeline.
     *
     * @return the count of elements that passed through the pipeline
     */
    public long count() {
        long[] result = {0};
        evaluate(element -> result[0]++);
        return result[0];
    }

    /**
     * Returns the first element in the pipeline, or an empty Optional if none.
     *
     * <p>This is a short-circuiting terminal operation. It triggers evaluation
     * but stops as soon as the first element is found, without processing the rest.
     *
     * <p>Short-circuiting works because this Sink overrides
     * {@link Sink#cancellationRequested()} to return {@code true} once it has
     * stored an element. The pipeline driver checks this flag after each
     * {@code tryAdvance} and stops pushing elements when it returns {@code true}.
     * {@link ChainedSink} propagates the flag up the chain so intermediate stages
     * also see the cancellation signal.
     *
     * @return an Optional containing the first element, or empty if the pipeline is empty
     */
    public Optional<T> findFirst() {
        boolean[] found = {false};
        Object[]  result = {null};
        Sink<T> sink = new Sink<>() {
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
        };

        evaluate(sink);
        return Optional.ofNullable((T) result[0]);
    }

    // -------------------------------------------------------------------------
    // Internal evaluation engine
    // -------------------------------------------------------------------------

    /**
     * Builds the Sink chain and drives the Spliterator to completion.
     *
     * <p>This is the core of pipeline fusion. The chain is built in reverse —
     * starting from the terminal Sink and wrapping backwards through the stages
     * list until the head Sink is reached. The head Sink is the entry point
     * for elements coming from the Spliterator.
     *
     * <p>A fresh {@link Spliterator} is obtained from the source collection on
     * every call. This makes the pipeline reusable: calling a terminal operation
     * twice, or branching from the same base pipeline, always starts from the
     * beginning of the source.
     *
     * <p>Evaluation sequence:
     * <ol>
     *     <li>Build Sink chain: {@code terminalSink ← stageN ← ... ← stage1}</li>
     *     <li>Call {@code headSink.begin(spliterator.estimateSize())}</li>
     *     <li>Drive: call {@code spliterator.tryAdvance(headSink::accept)} in a loop
     *         until it returns {@code false} or {@code headSink.cancellationRequested()}
     *         returns {@code true}.</li>
     *     <li>Call {@code headSink.end()}</li>
     * </ol>
     *
     * @param terminalSink the terminal Sink at the end of the chain
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void evaluate(Sink<T> terminalSink) {
        Sink head = terminalSink;
        for (int i = stages.size() - 1; i >= 0; i--) {
            head = stages.get(i).wrap(head);
        }

        Spliterator spliterator = source.spliterator();
        head.begin(spliterator.estimateSize());
        while (!head.cancellationRequested() && spliterator.tryAdvance(head::accept)) {}
        head.end();
    }

}