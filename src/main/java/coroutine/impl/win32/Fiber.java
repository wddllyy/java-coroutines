package coroutine.impl.win32;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

import java.util.concurrent.atomic.AtomicBoolean;

class Fiber {
    private static FiberNatives natives;

    private final AtomicBoolean valid = new AtomicBoolean(true);
    private final Pointer ptr;

    private Fiber(Pointer ptr) {
        this.ptr = ptr;
    }

    static Fiber createFiber(long stackSize, FiberStartRoutine routine) {
        if(stackSize < 4096) throw new IllegalArgumentException("stackSize < 4096");
        if(routine == null) throw new IllegalArgumentException("routine == null");
        return new Fiber(natives().CreateFiber(stackSize, routine, null));
    }

    static Fiber convertThreadToFiber() {
        return new Fiber(natives().ConvertThreadToFiber(null));
    }

    static void convertFiberToThread() {
        natives().ConvertFiberToThread();
    }

    private static FiberNatives natives() {
        if(natives == null) {
            synchronized(Fiber.class) {
                if(natives == null) {
                    if(!Platform.isWindows()) {
                        throw new IllegalStateException("Fibers are only available in windows");
                    }
                    natives = Native.loadLibrary("kernel32", FiberNatives.class);
                }
            }
        }
        return natives;
    }

    void delete() {
        if(valid.compareAndSet(true, false)) natives().DeleteFiber(ptr);
    }

    void switchTo() {
        if(!valid.get()) throw new IllegalStateException("Already deleted");
        natives().SwitchToFiber(ptr);
    }

    private interface FiberNatives extends StdCallLibrary {
        Pointer CreateFiber(long dwStackSize, FiberStartRoutine lpStartAddress, Pointer lpParameter);

        void DeleteFiber(Pointer lpFiber);

        Pointer ConvertThreadToFiber(Pointer lpParameter);

        void ConvertFiberToThread();

        void SwitchToFiber(Pointer lpFiber);
    }
}
