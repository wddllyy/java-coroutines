package coroutine;

/**
 * Represents a coroutine.
 *
 * Coroutines are like threads, but not scheduled by the kernel and have full control of when they stop executing
 * and when a different one starts executing.
 *
 * Always keep a reference to this object while the respective coroutine is executing, otherwise the JVM might crash.
 */
public interface Coroutine {
    /**
     * Returns the current value stored in a coroutine local storage
     *
     * @return The value
     */
    Object getData();

    /**
     * Sets the current coroutine local storage value
     *
     * @param data The new value
     * @return The old value
     */
    Object setData(Object data);
}
