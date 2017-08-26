package coroutine;

public interface CoroutineFunc {
    void run(CoroutineContext ctx, Object initialArg);
}
