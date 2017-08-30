package com.github.natanbc.coroutine;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a coroutine.
 * <p>
 * Coroutines are like threads, but not scheduled by the kernel and have full control of when they stop executing
 * and when a different one starts executing.
 * <p>
 * Always keep a reference to this object while the respective coroutine is executing, otherwise the JVM might crash.
 *
 * @param <A> Argument the coroutine receives when resumed
 * @param <R> Value the coroutine yields
 */
public abstract class Coroutine<A, R> {
    private Map<CoroutineLocal<?>, Object> locals;

    Map<CoroutineLocal<?>, Object> getLocalsMap() {
        if(locals == null) locals = new HashMap<>();
        return locals;
    }
}
