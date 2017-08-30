package com.github.natanbc.coroutine;

/**
 * Thrown when a coroutine calls {@link CoroutineContext#error(Object)}
 */
@SuppressWarnings("WeakerAccess")
public class CoroutineExecutionError extends RuntimeException {
    public final Object info;

    public CoroutineExecutionError(Object info) {
        super(String.valueOf(info));
        this.info = info;
    }
}
