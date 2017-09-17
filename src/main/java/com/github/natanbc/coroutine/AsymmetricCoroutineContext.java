package com.github.natanbc.coroutine;

import com.github.natanbc.coroutine.impl.Contexts;

/**
 * Holds the context and state of asymmetric coroutines
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class AsymmetricCoroutineContext {
    /**
     * Returns the AsymmetricCoroutineContext for the current thread.
     *
     * @return The context
     */
    public static AsymmetricCoroutineContext getContext() {
        return Contexts.getAsymmetric();
    }

    /**
     * Deletes the current thread's AsymmetricCoroutineContext. This is a shortcut for {@code AsymmetricCoroutineContext.getContext().destroy();}
     */
    public static void destroyContext() {
        Contexts.destroyAsymmetric();
    }

    /**
     * Creates a new coroutine
     *
     * @param func The code to execute
     * @return The coroutine object
     *
     * @param <A> Argument the coroutine receives when resumed
     * @param <R> Value the coroutine yields
     */
    public abstract <A, R> Coroutine<A, R> create(AsymmetricCoroutineFunc<A, R> func);

    /**
     * Resumes a paused coroutine or starts a fresh one. The second argument is given as the {@link AsymmetricCoroutineFunc#run(AsymmetricCoroutineFunc.Context, Object)} second argument
     * for fresh coroutines and is returned by yield() on paused coroutines.
     *
     * @param c   Coroutine to resume
     * @param arg Argument to give
     * @return The argument given to yield() by the coroutine
     * @throws CoroutineExecutionError on the main coroutine if a coroutine calls error()
     *
     * @param <A> Argument the coroutine receives when resumed
     * @param <R> Value the coroutine yields
     */
    public abstract <A, R> R resume(Coroutine<A, R> c, A arg);

    /**
     * Yields a value and returns control to the coroutine that started the current one.
     *
     * @param value Value to return on {@link #resume(Coroutine, Object)}
     * @return Value given to a subsequent resume call for the current coroutine
     *
     * @param <A> Argument the coroutine receives when resumed
     * @param <R> Value the coroutine yields
     */
    public abstract <A, R> A yield(R value);

    /**
     * Stops execution and propagates up the coroutine resume stack until a point safe to throw exceptions is reached, then a
     * {@link CoroutineExecutionError} is thrown.
     *
     * @param info Info to give the {@link CoroutineExecutionError} thrown
     * @return Value given to a subsequent resume call for the current coroutine
     *
     * @param <A> Argument the coroutine receives when resumed
     */
    public abstract <A> A error(Object info);

    /**
     * Returns the current executing {@link Coroutine}
     *
     * @return The current coroutine
     *
     * @param <A> Argument the coroutine receives when resumed
     * @param <R> Value the coroutine yields
     */
    public abstract <A, R> Coroutine<A, R> current();

    /**
     * Destroys a coroutine
     *
     * @param c The coroutine to destroy
     * @throws IllegalStateException if the given coroutine is executing
     *
     * @param <A> Argument the coroutine receives when resumed
     * @param <R> Value the coroutine yields
     */
    public abstract <A, R> void destroy(Coroutine<A, R> c);

    /**
     * Destroys this context and all its coroutines
     *
     * @see #destroy(Coroutine)
     */
    public abstract void destroy();

    /**
     * Returns the number of coroutines alive
     *
     * @return how many coroutines have been created but not destroyed
     */
    public abstract int alive();
}
