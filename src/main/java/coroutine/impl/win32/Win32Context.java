package coroutine.impl.win32;

import coroutine.Coroutine;
import coroutine.CoroutineContext;
import coroutine.CoroutineExecutionError;
import coroutine.CoroutineFunc;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

@SuppressWarnings("WeakerAccess")
public class Win32Context implements CoroutineContext {
    public long stackSize = 1024*1024;
    public final Fiber mainFiber = Fiber.convertThreadToFiber();
    public final Win32Coroutine mainCoroutine = new Win32Coroutine(mainFiber);
    public final Thread owner = Thread.currentThread();
    public final List<Win32Coroutine> coroutines = new LinkedList<>();
    public Object buffer;
    public Object ex;
    public Stack<Win32Coroutine> stack = new Stack<>();
    public Win32Coroutine current = mainCoroutine;

    @Override
    public Coroutine create(CoroutineFunc func) {
        if(Thread.currentThread() != owner) throw new IllegalStateException("Contexts are thread local");
        return new Win32Coroutine(func, stackSize, this, ()->buffer);
    }

    @Override
    public Object resume(Coroutine c, Object arg) {
        if(Thread.currentThread() != owner) throw new IllegalStateException("Contexts are thread local");
        Win32Coroutine co = (Win32Coroutine)c;
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
            if(stack.isEmpty()) throw new CoroutineExecutionError(o);
            return error(o);
        }
        return buffer;
    }

    @Override
    public Object yield(Object value) {
        if(Thread.currentThread() != owner) throw new IllegalStateException("Contexts are thread local");
        if(stack.isEmpty()) {
            throw new IllegalArgumentException("Cannot yield from main coroutine");
        }
        buffer = value;
        current = stack.pop();
        current.fiber.switchTo();
        return buffer;
    }

    @Override
    public Object error(Object data) {
        if(Thread.currentThread() != owner) throw new IllegalStateException("Contexts are thread local");
        if(stack.isEmpty()) throw new CoroutineExecutionError(data);
        ex = data;
        return yield(null);
    }

    @Override
    public Coroutine current() {
        return current;
    }

    @Override
    public void destroy(Coroutine c) {
        Win32Coroutine co = (Win32Coroutine)c;
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
        stack.clear();
        coroutines.removeIf(c->{
            c.fiber.delete();
            return true;
        });
        Fiber.convertFiberToThread();
    }
}
