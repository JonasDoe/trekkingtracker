package trekkingtracker.event.publishing;

/** Propagates events of any type to listeners for these event types. */
public interface EventPublisher {
    /**
     * Registers a new event listener for the given event type.
     *
     * @param eventType
     *         the listener will listen to all events of this event type (and all its subtypes)
     * @param <T>
     *         the type of event the listener shall be registered for
     */
    <T> void addEventListener(Class<T> eventType, EventHandler<T> toAdd);
    
    /**
     * Unregisters the listener for all event types.
     *
     * @param toRemove
     *         the listener to be unregistered
     */
    void removeEventListener(EventHandler<?> toRemove);
    
    /**
     * Unregisters the listener from the given event type
     *
     * @param eventType
     *         the event type the listener shall be unregistered for. Note there's no guaranteed effect an event type
     *         which differs from the one the listener was registered with in the first place, especially no "subtype
     *         blacklisting".
     * @param toRemove
     *         the listener to be unregistered
     * @param <T>
     *         the type of event the listener shall be unregistered for
     */
    <T> void removeEventListener(Class<T> eventType, EventHandler<T> toRemove);
    
    /**
     * Propagates a given event to all listeners to this event (and it's subtypes).
     *
     * @param event
     *         to be propagated
     * @param <T>
     *         the type of the event which is propagated
     */
    <T> void publish(T event);
}
