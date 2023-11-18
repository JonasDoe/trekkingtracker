package trekkingtracker.event.publishing;

import trekkingtracker.ui.utils.UiUtils;

import java.util.Objects;

/**
 * {@code EventPublisher} which distinguishes between background thread and ui thread event listeners. Background
 * listeners will be run first (in a background thread), afterward ui-thread listeners in the ui thread.
 */
public class ThreadAwareEventPublisher implements EventPublisher {
    /** Holds all listeners from non-ui threads */
    private final BasicEventPublisher nonUiPublisher = new BasicEventPublisher();
    /** Holds all listeners from ui threads */
    private final BasicEventPublisher uiPublisher = new BasicEventPublisher();
    
    /**
     * Registers a new background thread event listener for the given event type.
     *
     * @param eventType
     *         the listener will listen to all events of this event type (and all its subtypes)
     * @param toAdd
     *         the listener to be registered
     * @param <T>
     *         the type of event the listener shall be registered for
     */
    @Override
    public <T> void addEventListener(final Class<T> eventType, final EventHandler<T> toAdd) {
        addEventListenerNonUi(eventType, toAdd);
    }
    
    /**
     * Registers a new background thread event listener for the given event type.
     *
     * @param eventType
     *         the listener will listen to all events of this event type (and all its subtypes)
     * @param toAdd
     *         the listener to be registered
     * @param <T>
     *         the type of event the listener shall be registered for
     */
    public <T> void addEventListenerNonUi(Class<T> eventType, EventHandler<T> toAdd) {
        addEventListener(eventType, toAdd, false);
    }
    
    /**
     * Registers a new ui thread event listener for the given event type.
     *
     * @param eventType
     *         the listener will listen to all events of this event type (and all its subtypes)
     * @param toAdd
     *         the listener to be registered
     * @param <T>
     *         the type of event the listener shall be registered for
     */
    public <T> void addEventListenerUi(Class<T> eventType, EventHandler<T> toAdd) {
        addEventListener(eventType, toAdd, true);
    }
    
    /**
     * Registers a new ui thread event listener for the given event type.
     *
     * @param eventType
     *         the listener will listen to all events of this event type (and all its subtypes)
     * @param toAdd
     *         the listener to be registered
     * @param uiListener
     *         {@code true} if the listener shall be called in an ui thread, otherwise {@code false}
     * @param <T>
     *         the type of event the listener shall be registered for
     */
    public <T> void addEventListener(Class<T> eventType, final EventHandler<T> toAdd, boolean uiListener) {
        Objects.requireNonNull(toAdd);
        if (uiListener) uiPublisher.addEventListener(eventType, toAdd);
        else nonUiPublisher.addEventListener(eventType, toAdd);
    }
    
    @Override
    public void removeEventListener(EventHandler<?> toRemove) {
        nonUiPublisher.removeEventListener(toRemove);
        uiPublisher.removeEventListener(toRemove);
    }
    
    @Override
    public <T> void removeEventListener(final Class<T> eventType, final EventHandler<T> toRemove) {
        nonUiPublisher.removeEventListener(eventType, toRemove);
        uiPublisher.removeEventListener(eventType, toRemove);
    }
    
    @Override
    public <T> void publish(T event) {
        UiUtils.backgroundJob(() -> nonUiPublisher.publish(event), () -> uiPublisher.publish(event));
    }
}
