package coroutine.impl;

import com.sun.jna.Platform;
import coroutine.CoroutineContext;
import coroutine.impl.win32.Win32Context;

public class Contexts {
    private static final ThreadLocal<?  extends CoroutineContext> contexts = ThreadLocal.withInitial(()->{
        if(Platform.isWindows()) return new Win32Context();
        throw new UnsupportedOperationException("Only supported on windows (for now)");
    });

    public static CoroutineContext get() {
        return contexts.get();
    }

    public static void destroy() {
        CoroutineContext c = contexts.get();
        contexts.remove();
        c.destroy();
    }
}
