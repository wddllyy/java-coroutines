package coroutine.impl;

import com.sun.jna.Platform;
import coroutine.CoroutineContext;
import coroutine.impl.fallback.FallbackContext;
import coroutine.impl.win32.Win32Context;

@SuppressWarnings("WeakerAccess")
public class Contexts {
    public static final boolean USE_FALLBACK = !Platform.isWindows() || System.getProperty("coroutine.usefallback", null) != null;

    private static final ThreadLocal<CoroutineContext> contexts = new ThreadLocal<CoroutineContext>() {
        @Override
        protected CoroutineContext initialValue() {
            if(USE_FALLBACK) return new FallbackContext();
            return new Win32Context();
        }
    };

    public static CoroutineContext get() {
        return contexts.get();
    }

    public static void remove() {
        contexts.remove();
    }

    public static void destroy() {
        contexts.get().destroy();
    }

    public static CoroutineContext getFallback() {
        CoroutineContext ctx = get();
        if(ctx instanceof Win32Context) {
            ctx.destroy();
            ctx = new FallbackContext();
            contexts.set(ctx);
        }
        return ctx;
    }
}
