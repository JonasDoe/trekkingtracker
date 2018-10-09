package trekkingtracker.event.publishing;

/**
 * Handles an event.
 *
 * @param <E>
 *         the type of the event which is handled
 */
@FunctionalInterface
public interface EventHandler<E> {
    void handle(E event);
}
