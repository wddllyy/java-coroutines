package com.github.natanbc.coroutine;

import com.github.natanbc.coroutine.impl.Contexts;

public abstract class SymmetricCoroutineContext {
    /**
     * Returns the SymmetricCoroutineContext for the current thread.
     *
     * @return The context
     */
    public static SymmetricCoroutineContext getContext() {
        return Contexts.getSymmetric();
    }

    /**
     * Deletes the current thread's SymmetricCoroutineContext. This is a shortcut for {@code SymmetricCoroutineContext.getContext().destroy();}
     */
    public static void destroyContext() {
        Contexts.destroySymmetric();
    }

    /**
     * Creates a new coroutine
     *
     * @param func The code to execute
     * @return The coroutine object
     *
     * @param <A> Argument the coroutine receives when resumed
     */
    public abstract <A> Coroutine<A, A> create(SymmetricCoroutineFunc<A> func);

    /**
     * Resumes a paused coroutine or starts a fresh one. The second argument is given as the {@link SymmetricCoroutineFunc#run(SymmetricCoroutineFunc.Context, Object)} second argument
     * for fresh coroutines and is returned by resume() on paused coroutines.
     *
     * @param c   Coroutine to resume
     * @param arg Argument to give
     * @return The argument given to yield() by the coroutine
     * @throws CoroutineExecutionError on the main coroutine if a coroutine calls error()
     *
     * @param <A> Argument the coroutine receives when resumed
     */
    public abstract <A> A resume(Coroutine<A, A> c, A arg);

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
     */
    public abstract <A> Coroutine<A, A> current();

    /**
     * Destroys a coroutine
     *
     * @param c The coroutine to destroy
     * @throws IllegalStateException if the given coroutine is executing
     *
     * @param <A> Argument the coroutine receives when resumed
     */
    public abstract <A> void destroy(Coroutine<A, A> c);

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

    /**
     * Returns the caller coroutine
     *
     * @param <A> Argument the coroutine receives
     *
     * @return The caller coroutine
     */
    public abstract <A> Coroutine<A, A> caller();
}
