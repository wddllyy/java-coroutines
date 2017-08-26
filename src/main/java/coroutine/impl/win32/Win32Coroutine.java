package coroutine.impl.win32;

import coroutine.Coroutine;
import coroutine.CoroutineFunc;

import java.util.function.Supplier;

class Win32Coroutine implements Coroutine {
    final FiberStartRoutine code;
    final Fiber fiber;

    Win32Coroutine(CoroutineFunc func, long stackSize, Win32Context ctx, Supplier<Object> buffer) {
        this.code = a->func.run(ctx, buffer.get());
        this.fiber = Fiber.createFiber(stackSize, this.code);
    }
}
