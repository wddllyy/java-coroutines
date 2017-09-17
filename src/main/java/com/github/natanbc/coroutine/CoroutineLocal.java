package com.github.natanbc.coroutine;

import java.util.Map;
import java.util.Objects;

/**
 * Stores values locally on coroutines. Different coroutines accessing it will have different values
 *
 * @param <T> Type of the value stored
 */
@SuppressWarnings({"unchecked", "unused", "WeakerAccess"})
public class CoroutineLocal<T> {
    private final AsymmetricCoroutineContext ctx;

    public CoroutineLocal(AsymmetricCoroutineContext context) {
        this.ctx = Objects.requireNonNull(context, "context");
    }

    /**
     * Returns the stored value for the current coroutine or the value returned by {@link #initialValue()} if it's null
     *
     * @return The value
     */
    public final T get() {
        Map<CoroutineLocal<?>, Object> map = ctx.current().getLocalsMap();
        Object o = map.get(this);
        if(o == null) {
            o = initialValue();
            map.put(this, o);
        }
        return (T)o;
    }

    /**
     * Sets the value stored for the current coroutine
     *
     * @param t The new value
     * @return The old value
     */
    public final T set(T t) {
        return (T)ctx.current().getLocalsMap().put(this, t);
    }

    /**
     * Clears the value stored for the current coroutine. Calling {@link #get()} after removing will set the value to the return value of {@link #initialValue()}
     *
     * @return The value that was stored
     */
    public final T remove() {
        return (T)ctx.current().getLocalsMap().remove(this);
    }

    /**
     * Called when {@link #get()} is called with no value stored
     *
     * @return The value to store and return
     */
    protected T initialValue() {
        return null;
    }
}
