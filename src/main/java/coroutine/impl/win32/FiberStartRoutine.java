package coroutine.impl.win32;

import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

@FunctionalInterface
interface FiberStartRoutine extends StdCallLibrary.StdCallCallback {
    void run(Pointer arg);
}
