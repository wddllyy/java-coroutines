package coroutine.impl.fallback;

import coroutine.Coroutine;
import coroutine.CoroutineFunc;
import coroutine.impl.Supplier;

class FallbackCoroutine implements Coroutine {
    private Object data;
    final Thread thread;
    boolean stop = false;

    FallbackCoroutine(final FallbackContext ctx, final CoroutineFunc func, final Supplier buffer) {
        thread = new Thread() {
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
