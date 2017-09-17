package com.github.natanbc.coroutine.impl;

import com.github.natanbc.coroutine.Coroutine;
import com.github.natanbc.coroutine.AsymmetricCoroutineContext;
import com.github.natanbc.coroutine.CoroutineExecutionError;
import com.github.natanbc.coroutine.AsymmetricCoroutineFunc;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"unchecked", "WeakerAccess"})
public class AsymmetricContextImpl extends AsymmetricCoroutineContext {
    public final AtomicInteger nextId = new AtomicInteger(1);
    public final List<CoroutineImpl> coroutines = new LinkedList<>();
    public final ThreadGroup group = new ThreadGroup(Thread.currentThread().getThreadGroup(), Thread.currentThread().getName() + " Coroutines");
    public int threadPriority = 5;
    public boolean daemon = false;
    public long stackSize = 1024 * 1024;
    public Object buffer;
    public Object ex;
    public CoroutineImpl current = new CoroutineImpl();

    @Override
    public <A, R> Coroutine<A, R> create(AsymmetricCoroutineFunc<A, R> func) {
        CoroutineImpl f = new CoroutineImpl(this, func, new Supplier() {
            @Override
            public Object get() {
                return buffer;
            }
        });
        coroutines.add(f);
        return f;
    }

    @Override
    public <A, R> R resume(Coroutine<A, R> c, A arg) {
        CoroutineImpl f = (CoroutineImpl) c;
        if(f.thread == null) {
            error("Cannot resume main coroutine");
            throw new AssertionError();
        }
        for(CoroutineImpl crt = current; crt != null; crt = crt.caller) {
            if(crt == f) error("Cannot resume coroutine already running");
        }
        if(f.stop) {
            error("Cannot resume dead coroutine");
            throw new AssertionError();
        }
        CoroutineImpl crt;
        synchronized(this) {
            crt = current;
            current = f;
            buffer = arg;
            f.caller = crt;
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
    public <A, R> A yield(R value) {
        if(current.caller == null) {
            error("Cannot yield from main coroutine");
        }
        CoroutineImpl f;
        synchronized(this) {
            buffer = value;
            f = current;
            (current = current.caller).semaphore.release();
        }
        try {
            f.semaphore.acquire();
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if(current.stop) throw new CoroutineImpl.Stop();
        return (A)buffer;
    }

    @Override
    public <A> A error(Object info) {
        if(current.caller == null) throw new CoroutineExecutionError(info);
        ex = info;
        return yield(null);
    }

    @Override
    public Coroutine current() {
        return current;
    }

    @Override
    public void destroy(Coroutine c) {
        CoroutineImpl co = (CoroutineImpl) c;
        if(co.thread == null) {
            error("Cannot destroy main coroutine");
        }
        for(CoroutineImpl crt = current; crt != null; crt = crt.caller) {
            if(crt == co) error("Cannot delete a running coroutine");
        }
        if(!coroutines.contains(co)) {
            error("Coroutine belongs to a different context or was already deleted");
        }
        coroutines.remove(co);
        co.stop = true;
        co.semaphore.release();
    }

    @Override
    public void destroy() {
        if(current.caller != null) {
            error("Cannot destroy context while coroutines are still running");
        }
        Contexts.asymmetric.remove();
        current.stop = true;
        current.semaphore.release();
        for(ListIterator<CoroutineImpl> it = coroutines.listIterator(); it.hasNext(); ) {
            CoroutineImpl c = it.next();
            it.remove();
            c.stop = true;
            c.semaphore.release();
        }
    }

    @Override
    public int alive() {
        return coroutines.size();
    }
}
