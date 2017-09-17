package com.github.natanbc.coroutine.impl;

import com.github.natanbc.coroutine.Coroutine;
import com.github.natanbc.coroutine.CoroutineExecutionError;
import com.github.natanbc.coroutine.SymmetricCoroutineContext;
import com.github.natanbc.coroutine.SymmetricCoroutineFunc;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"unchecked", "WeakerAccess"})
public class SymmetricContextImpl extends SymmetricCoroutineContext {
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
    public <A> Coroutine<A, A> create(SymmetricCoroutineFunc<A> func) {
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
    public <A> A resume(Coroutine<A, A> c, A arg) {
        CoroutineImpl f = (CoroutineImpl) c;
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
            if(f.thread != null && f.thread.getState() == Thread.State.NEW) {
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
        return (A)buffer;
    }

    @Override
    public <A> A error(Object info) {
        if(current.thread == null) throw new CoroutineExecutionError(info);
        ex = info;
        return (A)resume(current.caller, null);
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
        if(!coroutines.contains(co)) {
            error("Coroutine belongs to a different context or was already deleted");
        }
        coroutines.remove(co);
        co.stop = true;
        co.semaphore.release();
    }

    @Override
    public void destroy() {
        Contexts.symmetric.remove();
        current.stop = true;
        current.semaphore.release();
        for(Iterator<CoroutineImpl> it = coroutines.listIterator(); it.hasNext(); ) {
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

    @Override
    public <A> Coroutine<A, A> caller() {
        return current.caller;
    }
}
