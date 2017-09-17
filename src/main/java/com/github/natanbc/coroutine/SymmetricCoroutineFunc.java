package com.github.natanbc.coroutine;

import java.util.Objects;

/**
 * Code to execute on a {@link Coroutine Coroutine}
 *
 * @param <A> Argument the coroutine receives when resumed
 */
public interface SymmetricCoroutineFunc<A> {
    void run(Context<A> ctx, A initialArg);

    /**
     * Type safe alternative to {@link AsymmetricCoroutineContext}
     *
     * @param <A> Argument the coroutine receives when resumed
     */
    @SuppressWarnings({"unchecked", "unused"})
    class Context<A> {
        private final SymmetricCoroutineContext ctx;

        public Context(SymmetricCoroutineContext ctx) {
            this.ctx = Objects.requireNonNull(ctx, "context");
        }


        public A error(Object data) {
            return (A)ctx.error(data);
        }

        public <AA> AA resume(Coroutine<AA, AA> c, AA arg) {
            return ctx.resume(c, arg);
        }

        public Coroutine<A, A> current() {
            return ctx.current();
        }

        public <AA> Coroutine<AA, AA> create(SymmetricCoroutineFunc<AA> func) {
            return ctx.create(func);
        }

        public <AA> void destroy(Coroutine<AA, AA> c) {
            ctx.destroy(c);
        }

        public void destroy() {
            ctx.destroy();
        }

        public Coroutine<A, A> caller() {
            return ctx.caller();
        }
    }
}

