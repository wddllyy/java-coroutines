package coroutine;

/**
 * Code to execute on a {@link Coroutine Coroutine}
 */
public interface CoroutineFunc {
    void run(CoroutineContext ctx, Object initialArg);
}
