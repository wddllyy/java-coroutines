package coroutine;

public class CoroutineExecutionError extends RuntimeException {
    public final Object info;

    public CoroutineExecutionError(Object info) {
        super(String.valueOf(info));
        this.info = info;
    }
}
