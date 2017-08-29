package coroutine.impl.fallback;

import coroutine.Coroutine;
import coroutine.CoroutineFunc;
import coroutine.impl.Supplier;

import java.util.concurrent.Semaphore;

class FallbackCoroutine implements Coroutine {
    //using wait/notify was deadlocking
    final Semaphore semaphore = new Semaphore(0);
    final Thread thread;
    boolean stop = false;
    private Object data;

    FallbackCoroutine(final FallbackContext ctx, final CoroutineFunc func, final Supplier buffer) {
        thread = new Thread(ctx.group, null, ctx.group.getName() + "-Coroutine-" + ctx.nextId.getAndIncrement(), ctx.stackSize) {
            @Override
            public void run() {
                try {
                    func.run(ctx, buffer.get());
                } catch(Stop ignored) {
                } catch(Throwable t) {
                    ctx.error(t);
                }
            }
        };
        thread.setDaemon(ctx.daemon);
        thread.setPriority(ctx.threadPriority);
    }

    FallbackCoroutine() {
        this.thread = null;
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

    static class Stop extends RuntimeException {

    }
}
