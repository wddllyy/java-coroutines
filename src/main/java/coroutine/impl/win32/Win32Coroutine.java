package coroutine.impl.win32;

import coroutine.Coroutine;
import coroutine.CoroutineFunc;

import java.util.function.Supplier;

class Win32Coroutine implements Coroutine {
    final FiberStartRoutine code;
    final Fiber fiber;
    private Object data;

    Win32Coroutine(CoroutineFunc func, long stackSize, Win32Context ctx, Supplier<Object> buffer) {
        this.code = a->func.run(ctx, buffer.get());
        this.fiber = Fiber.createFiber(stackSize, this.code);
    }

    Win32Coroutine(Fiber f) {
        this.code = null;
        this.fiber = f;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public void setData(Object data) {
        this.data = data;
    }
}
