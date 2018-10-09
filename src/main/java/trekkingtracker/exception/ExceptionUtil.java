package trekkingtracker.exception;

/** Offers utility methods for {@code Exception} handling. */
public class ExceptionUtil {
    /** Pure util class, not intended to be instantiated */
    private ExceptionUtil() {
    }
    
    /**
     * Masks any {@code Exception} as (unchecked) {@code RuntimeException} and throws it.
     *
     * @param e
     *         to be masked
     * @return will never happen since always a {@code RuntimeException} will be thrown, but allows to write {@code
     * throw Utils.throwRuntime(e)} to stop a code path.
     * @throws RuntimeException
     *         masking the given {@code Exception}
     */
    public static RuntimeException throwRuntime(Exception e) throws RuntimeException {
        if (e instanceof RuntimeException) throw (RuntimeException) e;
        else {
            RuntimeException toThrow = new RuntimeException();
            e.initCause(e.getCause());
            e.setStackTrace(e.getStackTrace());
            throw toThrow;
        }
    }
    
    /**
     * Runs a given {@code Runnable} and suppresses any {@code Exceptions} that occur. Useful for notifying listeners
     * and other situations where the caller does not really care for the success.
     *
     * @param toRun
     *         to be run with any {@code Exception} suppressed
     */
    public static void suppressException(Runnable toRun) {
        try {
            toRun.run();
        } catch (RuntimeException e) { }
    }
}
