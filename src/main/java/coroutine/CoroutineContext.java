package coroutine;

import coroutine.impl.Contexts;

/**
 * Holds the context and state of coroutines
 *
 * Throws when used in a different thread than the one that created it
 */
public interface CoroutineContext {
    /**
     * Creates a new coroutine
     *
     * @param func The code to execute
     * @return The coroutine object
     */
    Coroutine create(CoroutineFunc func);

    /**
     * Resumes a paused coroutine or starts a fresh one. The second argument is given as the {@link CoroutineFunc#run(CoroutineContext, Object)} second argument
     * for fresh coroutines and is returned by yield() on paused coroutines.
     *
     * @param c Coroutine to resume
     * @param arg Argument to give
     * @return The argument given to yield() by the coroutine
     *
     * @throws CoroutineExecutionError on the main coroutine if a coroutine calls error()
     */
    Object resume(Coroutine c, Object arg);

    /**
     * Yields a value and returns control to the coroutine that started the current one.
     *
     * @param value Value to return on {@link #resume(Coroutine, Object)}
     * @return Value given to a subsequent resume call for the current coroutine
     */
    Object yield(Object value);

    /**
     * Stops execution and propagates up the coroutine resume stack until a point safe to throw exceptions is reached, then a
     * {@link CoroutineExecutionError} is thrown.
     *
     * @param info Info to give the {@link CoroutineExecutionError} thrown
     * @return Value given to a subsequent resume call for the current coroutine
     */
    Object error(Object info);

    /**
     * Returns the current executing {@link Coroutine}
     *
     * @return The current coroutine
     */
    Coroutine current();

    /**
     * Destroys a coroutine
     *
     * @param c The coroutine to destroy
     *
     * @throws IllegalStateException if the given coroutine is executing
     */
    void destroy(Coroutine c);

    /**
     * Destroys this context and all its coroutines
     *
     * @see #destroy(Coroutine)
     */
    void destroy();

    /**
     * Returns the CoroutineContext for the current thread.
     *
     * @return The context
     */
    static CoroutineContext getContext() {
        return Contexts.get();
    }

    /**
     * Deletes the current thread's CoroutineContext. This is a shortcut for {@code CoroutineContext.getContext().destroy();}
     */
    static void destroyContext() {
        Contexts.destroy();
    }
}
