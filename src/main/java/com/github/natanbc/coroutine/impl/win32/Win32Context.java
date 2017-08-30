package com.github.natanbc.coroutine.impl.win32;

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

@SuppressWarnings({"unchecked", "WeakerAccess"})
public class Win32Context extends CoroutineContext {
    public final Fiber mainFiber = Fiber.convertThreadToFiber();
    public final Win32Coroutine mainCoroutine = new Win32Coroutine(mainFiber);
    public final Thread owner = Thread.currentThread();
    public final List<Win32Coroutine> coroutines = new LinkedList<>();
    public long stackSize = 1024 * 1024;
    public Object buffer;
    public Object ex;
    public Stack<Win32Coroutine> stack = new Stack<>();
    public Win32Coroutine current = mainCoroutine;

    @Override
    public <A, R> Coroutine<A, R> create(CoroutineFunc<A, R> func) {
        if(Thread.currentThread() != owner) throw new IllegalStateException("Contexts are thread local");
        Win32Coroutine c = new Win32Coroutine(func, stackSize, this, new Supplier() {
            @Override
            public Object get() {
                return buffer;
            }
        });
        coroutines.add(c);
        return c;
    }

    @Override
    public <A, R> R resume(Coroutine<A, R> c, A arg) {
        if(Thread.currentThread() != owner) throw new IllegalStateException("Contexts are thread local");
        Win32Coroutine co = (Win32Coroutine) c;
        if(co.code == null) {
            error("Cannot resume main coroutine");
        }
        if(stack.contains(co)) {
            error("Cannot resume coroutine already running");
        }
        buffer = arg;
        stack.push(current);
        current = co;
        co.fiber.switchTo();
        if(ex != null) {
            Object o = ex;
            ex = null;
            error(o);
        }
        return (R)buffer;
    }

    @Override
    public <A, R> A yield(R value) {
        if(Thread.currentThread() != owner) throw new IllegalStateException("Contexts are thread local");
        if(stack.isEmpty()) {
            throw new IllegalArgumentException("Cannot yield from main coroutine");
        }
        buffer = value;
        current = stack.pop();
        current.fiber.switchTo();
        return (A)buffer;
    }

    @Override
    public <A> A error(Object info) {
        if(Thread.currentThread() != owner) throw new IllegalStateException("Contexts are thread local");
        if(stack.isEmpty()) throw new CoroutineExecutionError(info);
        ex = info;
        return yield(null);
    }

    @Override
    public <A, R> Coroutine<A, R> current() {
        return current;
    }

    @Override
    public void destroy(Coroutine c) {
        Win32Coroutine co = (Win32Coroutine) c;
        if(co.code == null) {
            error("Cannot destroy main coroutine");
        }
        if(stack.contains(co) || current == co) {
            error("Cannot delete a running coroutine");
        }
        if(!coroutines.contains(co)) {
            error("Coroutine belongs to a different context or was already deleted");
        }
        coroutines.remove(co);
        co.fiber.delete();
    }


    @Override
    public void destroy() {
        if(!stack.isEmpty()) {
            error("Cannot destroy context while coroutines are still running");
        }
        Contexts.remove();
        stack.clear();
        for(ListIterator<Win32Coroutine> it = coroutines.listIterator(); it.hasNext(); ) {
            Win32Coroutine c = it.next();
            it.remove();
            c.fiber.delete();
        }
        Fiber.convertFiberToThread();
    }

    @Override
    public int alive() {
        return coroutines.size();
    }
}
