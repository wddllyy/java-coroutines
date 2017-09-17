package com.github.natanbc.coroutine;

import java.util.Objects;

/**
 * Code to execute on a {@link Coroutine Coroutine}
 *
 * @param <A> Argument the coroutine receives when resumed
 * @param <R> Value the coroutine yields
 */
public interface AsymmetricCoroutineFunc<A, R> {
    void run(Context<A, R> ctx, A initialArg);

    /**
     * Type safe alternative to {@link AsymmetricCoroutineContext}
     *
     * @param <A> Argument the coroutine receives when resumed
     * @param <R> Value the coroutine yields
     */
    @SuppressWarnings({"unchecked", "unused"})
    class Context<A, R> {
        private final AsymmetricCoroutineContext ctx;

        public Context(AsymmetricCoroutineContext ctx) {
            this.ctx = Objects.requireNonNull(ctx, "context");
        }

        public A yield(R value) {
            return (A)ctx.yield(value);
        }

        public A error(Object data) {
            return (A)ctx.error(data);
        }

        public <AA, RR> RR resume(Coroutine<AA, RR> c, AA arg) {
            return ctx.resume(c, arg);
        }

        public Coroutine<A, R> current() {
            return ctx.current();
        }

        public <AA, RR> Coroutine<AA, RR> create(AsymmetricCoroutineFunc<AA, RR> func) {
            return ctx.create(func);
        }

        public <AA, RR> void destroy(Coroutine<AA, RR> c) {
            ctx.destroy(c);
        }

        public void destroy() {
            ctx.destroy();
        }
    }
}
