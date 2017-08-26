package coroutine;

import coroutine.impl.Contexts;

public interface CoroutineContext {
    Coroutine create(CoroutineFunc func);
    Object resume(Coroutine c, Object arg);
    Object yield(Object value);
    Object error(Object info);
    Coroutine current();
    void destroy(Coroutine c);
    void destroy();

    static CoroutineContext getContext() {
        return Contexts.get();
    }

    static void destroyContext() {
        Contexts.destroy();
    }
}
