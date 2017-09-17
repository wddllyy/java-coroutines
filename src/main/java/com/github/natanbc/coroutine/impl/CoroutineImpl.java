package com.github.natanbc.coroutine.impl;

import com.github.natanbc.coroutine.Coroutine;
import com.github.natanbc.coroutine.AsymmetricCoroutineFunc;
import com.github.natanbc.coroutine.SymmetricCoroutineFunc;

import java.util.concurrent.Semaphore;

@SuppressWarnings("unchecked")
class CoroutineImpl extends Coroutine {
    //using wait/notify was deadlocking
    final Semaphore semaphore = new Semaphore(0);
    final Thread thread;
    boolean stop = false;
    CoroutineImpl caller;

    CoroutineImpl(final AsymmetricContextImpl ctx, final AsymmetricCoroutineFunc func, final Supplier buffer) {
        thread = new Thread(ctx.group, null, ctx.group.getName() + "-Coroutine-" + ctx.nextId.getAndIncrement(), ctx.stackSize) {
            @Override
            public void run() {
                try {
                    func.run(new AsymmetricCoroutineFunc.Context(ctx), buffer.get());
                } catch(Stop ignored) {
                } catch(Throwable t) {
                    ctx.error(t);
                }
                stop = true;
                ctx.buffer = null;
                (ctx.current = ctx.current.caller).semaphore.release();
            }
        };
        thread.setDaemon(ctx.daemon);
        thread.setPriority(ctx.threadPriority);
    }

    CoroutineImpl(final SymmetricContextImpl ctx, final SymmetricCoroutineFunc func, final Supplier buffer) {
        thread = new Thread(ctx.group, null, ctx.group.getName() + "-Coroutine-" + ctx.nextId.getAndIncrement(), ctx.stackSize) {
            @Override
            public void run() {
                try {
                    func.run(new SymmetricCoroutineFunc.Context(ctx), buffer.get());
                } catch(Stop ignored) {
                } catch(Throwable t) {
                    ctx.error(t);
                }
                stop = true;
                ctx.buffer = null;
                (ctx.current = ctx.current.caller).semaphore.release();
            }
        };
        thread.setDaemon(ctx.daemon);
        thread.setPriority(ctx.threadPriority);
    }

    CoroutineImpl() {
        this.thread = null;
    }

    static class Stop extends RuntimeException {

    }
}
