package com.github.natanbc.coroutine;

import java.util.Map;
import java.util.Objects;

@SuppressWarnings({"unchecked", "unused", "WeakerAccess"})
public class CoroutineLocal<T> {
    private final CoroutineContext ctx;

    public CoroutineLocal(CoroutineContext context) {
        this.ctx = Objects.requireNonNull(context, "context");
    }

    public final T get() {
        Map<CoroutineLocal<?>, Object> map = ctx.current().getLocalsMap();
        Object o = map.get(this);
        if(o == null) {
            o = initialValue();
            map.put(this, o);
        }
        return (T)o;
    }

    public final T set(T t) {
        return (T)ctx.current().getLocalsMap().put(this, t);
    }

    public final T remove() {
        return (T)ctx.current().getLocalsMap().remove(this);
    }

    protected T initialValue() {
        return null;
    }
}
