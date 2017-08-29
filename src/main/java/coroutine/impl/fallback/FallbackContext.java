package coroutine.impl.fallback;

import coroutine.Coroutine;
import coroutine.CoroutineContext;
import coroutine.CoroutineExecutionError;
import coroutine.CoroutineFunc;
import coroutine.impl.Supplier;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

@SuppressWarnings("WeakerAccess")
public class FallbackContext extends CoroutineContext {
    public final List<FallbackCoroutine> coroutines = new LinkedList<>();
    public Object buffer;
    public Object ex;
    public Stack<FallbackCoroutine> stack = new Stack<>();
    public FallbackCoroutine current = new FallbackCoroutine();

    @Override
    public Coroutine create(CoroutineFunc func) {
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
    public Object resume(Coroutine c, Object arg) {
        FallbackCoroutine f = (FallbackCoroutine)c;
        if(f.thread == null) {
            return error("Cannot resume main coroutine");
        }
        if(stack.contains(f)) {
            error("Cannot resume coroutine already running");
        }
        FallbackCoroutine crt = current;
        current = f;
        buffer = arg;
        stack.push(crt);
        if(f.thread.getState() == Thread.State.NEW) {
            f.thread.start();
        } else {
            synchronized(f) {
                f.notifyAll();
            }
        }
        waitOn(crt);
        if(ex != null) {
            Object e = ex;
            ex = null;
            throw e instanceof CoroutineExecutionError ? (CoroutineExecutionError)e : new CoroutineExecutionError(e);
        }
        return buffer;
    }

    @Override
    @SuppressWarnings("SynchronizeOnNonFinalField")
    public Object yield(Object value) {
        if(stack.isEmpty()) {
            error("Cannot yield from main coroutine");
        }
        buffer = value;
        FallbackCoroutine f = current;
        current = stack.pop();
        synchronized(current) {
            current.notifyAll();
        }
        waitOn(f);
        if(current.stop) throw new FallbackCoroutine.Stop();
        return buffer;
    }

    @Override
    public Object error(Object info) {
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
        FallbackCoroutine co = (FallbackCoroutine)c;
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
        synchronized(co) {
            c.notifyAll();
        }
    }

    @Override
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void destroy() {
        if(!stack.isEmpty()) {
            error("Cannot destroy context while coroutines are still running");
        }
        stack.clear();
        for(ListIterator<FallbackCoroutine> it = coroutines.listIterator(); it.hasNext();) {
            FallbackCoroutine c = it.next();
            it.remove();
            c.stop = true;
            synchronized(c) {
                c.notifyAll();
            }
        }
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private static void waitOn(Object o) {
        try {
            synchronized(o) {
                o.wait();
            }
        } catch(InterruptedException ignored) {}
    }
}
