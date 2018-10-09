package trekkingtracker.event;

import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.Test;
import trekkingtracker.event.publishing.ThreadAwareEventPublisher;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class EventPublisherTest {
    @Test
    void eventPublisher() throws Exception {
        ThreadAwareEventPublisher toTest = new ThreadAwareEventPublisher();
        AtomicBoolean notTriggered = new AtomicBoolean();
        AtomicInteger listenerResult = new AtomicInteger();
        AtomicBoolean objectListenerTriggered = new AtomicBoolean();
        Object lock = new Object();
        new JFXPanel(); // initializes JavaFX environment
        String event = "event";
        
        toTest.addEventListenerUi(String.class, s -> {
            assertEquals(event, s);
            assertEquals(1, listenerResult.get());
            listenerResult.set(2);
            synchronized (lock) { lock.notifyAll();}
        });
        toTest.addEventListenerNonUi(String.class, s -> {
            assertEquals(event, s);
            assertEquals(0, listenerResult.get());
            listenerResult.set(1);
        });
        toTest.addEventListenerNonUi(Integer.class, s -> notTriggered.set(true));
        toTest.addEventListenerUi(Object.class, s -> {
            objectListenerTriggered.set(true);
            synchronized (lock) { lock.notifyAll();}
        });
        
        toTest.publish(event);
        synchronized (lock) {
            int loop = 0;
            while (listenerResult.get() != 2 || !objectListenerTriggered.get()) {
                lock.wait(1000);
                assertTrue(loop++ < 4);
            }
        }
        
        assertEquals(2, listenerResult.get());
        assertTrue(objectListenerTriggered.get());
        assertFalse(notTriggered.get());
    }
}
