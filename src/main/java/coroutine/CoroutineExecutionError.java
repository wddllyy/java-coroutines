package coroutine;

/**
 * Thrown on the first resume() call when a coroutine calls error()
 */
@SuppressWarnings("WeakerAccess")
public class CoroutineExecutionError extends RuntimeException {
    public final Object info;

    public CoroutineExecutionError(Object info) {
        super(String.valueOf(info));
        this.info = info;
    }
}
