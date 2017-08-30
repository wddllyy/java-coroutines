package com.github.natanbc.coroutine.impl.fallback;

import com.github.natanbc.coroutine.Coroutine;
import com.github.natanbc.coroutine.CoroutineFunc;
import com.github.natanbc.coroutine.impl.Supplier;

import java.util.concurrent.Semaphore;

@SuppressWarnings("unchecked")
class FallbackCoroutine extends Coroutine {
    //using wait/notify was deadlocking
    final Semaphore semaphore = new Semaphore(0);
    final Thread thread;
    boolean stop = false;

    FallbackCoroutine(final FallbackContext ctx, final CoroutineFunc func, final Supplier buffer) {
        thread = new Thread(ctx.group, null, ctx.group.getName() + "-Coroutine-" + ctx.nextId.getAndIncrement(), ctx.stackSize) {
            @Override
            public void run() {
                try {
                    func.run(new CoroutineFunc.Context(ctx), buffer.get());
                } catch(Stop ignored) {
                } catch(Throwable t) {
                    ctx.error(t);
                }
                ctx.buffer = null;
                (ctx.current = ctx.stack.pop()).semaphore.release();
            }
        };
        thread.setDaemon(ctx.daemon);
        thread.setPriority(ctx.threadPriority);
    }

    FallbackCoroutine() {
        this.thread = null;
    }

    static class Stop extends RuntimeException {

    }
}
