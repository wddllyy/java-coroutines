package com.github.natanbc.coroutine.impl.win32;

import com.github.natanbc.coroutine.Coroutine;
import com.github.natanbc.coroutine.CoroutineFunc;
import com.github.natanbc.coroutine.impl.Supplier;
import com.sun.jna.Pointer;

@SuppressWarnings("unchecked")
class Win32Coroutine extends Coroutine {
    final FiberStartRoutine code;
    final Fiber fiber;

    Win32Coroutine(final CoroutineFunc func, long stackSize, final Win32Context ctx, final Supplier buffer) {
        this.code = new FiberStartRoutine() {
            @Override
            public void run(Pointer arg) {
                func.run(new CoroutineFunc.Context(ctx), buffer.get());
            }
        };
        this.fiber = Fiber.createFiber(stackSize, this.code);
    }

    Win32Coroutine(Fiber f) {
        this.code = null;
        this.fiber = f;
    }
}
