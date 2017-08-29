package coroutine.impl.win32;

import com.sun.jna.Pointer;
import coroutine.Coroutine;
import coroutine.CoroutineFunc;
import coroutine.impl.Supplier;

class Win32Coroutine implements Coroutine {
    final FiberStartRoutine code;
    final Fiber fiber;
    private Object data;

    Win32Coroutine(final CoroutineFunc func, long stackSize, final Win32Context ctx, final Supplier buffer) {
        this.code = new FiberStartRoutine() {
            @Override
            public void run(Pointer arg) {
                func.run(ctx, buffer.get());
            }
        };
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
    public Object setData(Object data) {
        Object o = this.data;
        this.data = data;
        return o;
    }
}
