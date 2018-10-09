package trekkingtracker.event.publishing;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class BasicEventPublisher implements EventPublisher {
    
    /** Holds all listeners */
    private final Map<Class<?>, Collection<EventHandler<?>>> eventListeners = new ConcurrentHashMap<>();
    
    @Override
    public <T> void addEventListener(Class<T> eventType, EventHandler<T> toAdd) {
        eventListeners.computeIfAbsent(eventType, x -> new ConcurrentLinkedQueue<>()).add(toAdd);
    }
    
    @Override
    public void removeEventListener(EventHandler<?> toRemove) {
        eventListeners.values().removeIf(l -> l.equals(toRemove));
    }
    
    @Override
    public <T> void removeEventListener(final Class<T> eventType, final EventHandler<T> toRemove) {
        Collection<EventHandler<?>> nonUiHandlers = eventListeners.get(eventType);
        if (nonUiHandlers != null) nonUiHandlers.remove(toRemove);
    }
    
    @Override
    public <T> void publish(T event) {
        Collection<EventHandler<T>> listeners = getEventListeners(event);
        listeners.forEach(l -> { try {l.handle(event);} catch (Exception e) {/*no op*/} });
    }
    
    /**
     * Determines which of the given listeners match for the given event type.
     *
     * @param event
     *         the listeners shall be determined for
     * @param <T>
     *         the type of event the listeners are searched for
     * @return all matching listeners, including the ones which are registered for subtypes of the given event
     */
    private <T> Collection<EventHandler<T>> getEventListeners(T event) {
        return eventListeners.entrySet().stream().filter(e -> e.getKey().isAssignableFrom(event.getClass())).flatMap(
                e -> e.getValue().stream()).map(l -> (EventHandler<T>) l).collect(Collectors.toList());
    }
}
