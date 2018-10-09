package trekkingtracker.exception;

import java.util.function.Supplier;

/**
 * An {@code Supplier} which converts an checked {@code Exceptions} to a unchecked one.
 *
 * @param <T>
 *         the type of the supplied object @code
 * @param <E>
 *         the type of {@code Exception} which can be thrown
 */
@FunctionalInterface
public interface UncheckedSupplier<T, E extends Exception> extends Supplier<T> {
    @Override
    default T get() {
        try {
            return getThrows();
        } catch (Exception e) {
            throw ExceptionUtil.throwRuntime(e);
        }
    }
    
    /**
     * Like {@link #get()}, but can throw an checked {@code Exception} instead of wrapping it into an unchecked one.
     *
     * @return a result
     * @throws E
     *         if getting the result fails
     */
    T getThrows() throws E;
}
