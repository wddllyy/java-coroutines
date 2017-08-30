package com.github.natanbc.coroutine.impl.fallback;

import com.github.natanbc.coroutine.Coroutine;
import com.github.natanbc.coroutine.CoroutineContext;
import com.github.natanbc.coroutine.CoroutineFunc;
import com.github.natanbc.coroutine.impl.Contexts;
import com.github.natanbc.coroutine.impl.Supplier;
import com.github.natanbc.coroutine.CoroutineExecutionError;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"unchecked", "WeakerAccess"})
public class FallbackContext extends CoroutineContext {
    public final AtomicInteger nextId = new AtomicInteger(1);
    public final List<FallbackCoroutine> coroutines = new LinkedList<>();
    public final ThreadGroup group = new ThreadGroup(Thread.currentThread().getThreadGroup(), Thread.currentThread().getName() + " Coroutines");
    public int threadPriority = 5;
    public boolean daemon = false;
    public long stackSize = 1024 * 1024;
    public Object buffer;
    public Object ex;
    public Stack<FallbackCoroutine> stack = new Stack<>();
    public FallbackCoroutine current = new FallbackCoroutine();

    @Override
    public <A, R> Coroutine<A, R> create(CoroutineFunc<A, R> func) {
        FallbackCoroutine f = new FallbackCoroutine(this, func, new Supplier() {
            @Override
            public Object get() {
                return buffer;
            }
        });
        coroutines.add(f);
        return f;
    }

    @Override
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public <A, R> R resume(Coroutine<A, R> c, A arg) {
        FallbackCoroutine f = (FallbackCoroutine) c;
        if(f.thread == null) {
            error("Cannot resume main coroutine");
            throw new AssertionError();
        }
        if(stack.contains(f)) {
            error("Cannot resume coroutine already running");
            throw new AssertionError();
        }
        FallbackCoroutine crt;
        synchronized(this) {
            crt = current;
            current = f;
            buffer = arg;
            stack.push(crt);
            if(f.thread.getState() == Thread.State.NEW) {
                f.thread.start();
            } else {
                f.semaphore.release();
            }
        }
        try {
            crt.semaphore.acquire();
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if(ex != null) {
            Object e = ex;
            ex = null;
            throw e instanceof CoroutineExecutionError ? (CoroutineExecutionError) e : new CoroutineExecutionError(e);
        }
        return (R)buffer;
    }

    @Override
    @SuppressWarnings("SynchronizeOnNonFinalField")
    public <A, R> A yield(R value) {
        if(stack.isEmpty()) {
            error("Cannot yield from main coroutine");
        }
        FallbackCoroutine f;
        synchronized(this) {
            buffer = value;
            f = current;
            (current = stack.pop()).semaphore.release();
        }
        try {
            f.semaphore.acquire();
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if(current.stop) throw new FallbackCoroutine.Stop();
        return (A)buffer;
    }

    @Override
    public <A> A error(Object info) {
        if(stack.isEmpty()) throw new CoroutineExecutionError(info);
        ex = info;
        return yield(null);
    }

    @Override
    public Coroutine current() {
        return current;
    }

    @Override
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void destroy(Coroutine c) {
        FallbackCoroutine co = (FallbackCoroutine) c;
        if(co.thread == null) {
            error("Cannot destroy main coroutine");
        }
        if(stack.contains(co) || current == co) {
            error("Cannot delete a running coroutine");
        }
        if(!coroutines.contains(co)) {
            error("Coroutine belongs to a different context or was already deleted");
        }
        coroutines.remove(co);
        co.stop = true;
        co.semaphore.release();
    }

    @Override
    @SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter", "SynchronizeOnNonFinalField"})
    public void destroy() {
        if(!stack.isEmpty()) {
            error("Cannot destroy context while coroutines are still running");
        }
        Contexts.remove();
        stack.clear();
        current.stop = true;
        stack.push(current);
        current.semaphore.release();
        for(ListIterator<FallbackCoroutine> it = coroutines.listIterator(); it.hasNext(); ) {
            FallbackCoroutine c = it.next();
            it.remove();
            c.stop = true;
            stack.push(c);
            c.semaphore.release();
        }
    }

    @Override
    public int alive() {
        return coroutines.size();
    }
}
