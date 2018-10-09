package trekkingtracker.ui;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.Test;
import trekkingtracker.ui.utils.UiUtils;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiUtilsTest {
    
    @Test
    void uiJobsTest() throws Exception {
        AtomicBoolean wasInNonUi = new AtomicBoolean();
        AtomicBoolean wasInUi = new AtomicBoolean();
        Object lock = new Object();
        new JFXPanel(); // initializes JavaFX environment
        UiUtils.backgroundJob(() -> {
            assertFalse(Platform.isFxApplicationThread());
            wasInNonUi.set(true);
            return true;
        }, r -> {
            assertTrue(Platform.isFxApplicationThread());
            assertTrue(r);
            wasInUi.set(true);
            synchronized (lock) { lock.notifyAll();}
        });
        synchronized (lock) {
            int loop = 0;
            while (!wasInUi.get()) {
                lock.wait(1000);
                assertTrue(loop++ < 2);
            }
        }
        assertTrue(wasInNonUi.get());
    }
    
    @Test
    public void twoJobsTest() throws Exception {
        AtomicBoolean wasInUi = new AtomicBoolean();
        AtomicBoolean nonUiResult = new AtomicBoolean();
        Object lock = new Object();
        new JFXPanel(); // initializes JavaFX environment
        UiUtils.backgroundJob(() -> {
            assertFalse(Platform.isFxApplicationThread());
            nonUiResult.set(true);
        }, () -> {
            assertTrue(Platform.isFxApplicationThread());
            assertTrue(nonUiResult.get());
            wasInUi.set(true);
            synchronized (lock) { lock.notifyAll();}
        });
        synchronized (lock) {
            int loop = 0;
            while (!wasInUi.get()) {
                lock.wait(1000);
                assertTrue(loop++ < 2);
            }
        }
        assertTrue(wasInUi.get());
    }
}
