package trekkingtracker.ui.groups;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import trekkingtracker.data.Person;
import trekkingtracker.data.PersonImpl;
import trekkingtracker.event.publishing.EventPublisher;
import trekkingtracker.ui.utils.DateTimePane;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

/** Offers controls to set start times for multiple {@code Persons} at once */
//TODO validate that stop comes after start
public class ParticipantStart extends AbstractGroupModification {
    /** Used to display the stop time proposal */
    private final ZoneId zoneId;
    /** Holds the stop time to be set for all selected participants */
    private DateTimePane startTimePane;
    
    /**
     * Creates a new {@code ParticipantStart} interface.
     *
     * @param parent
     *         this ui element will be put in
     * @param eventPublisher
     *         used to propagate events
     * @param zoneId
     *         used to display the stop time proposal
     */
    public ParticipantStart(Pane parent, final EventPublisher eventPublisher, ZoneId zoneId) {
        super(parent, eventPublisher, "Start");
        this.zoneId = zoneId;
        preparePane(contentPane);
    }
    
    /**
     * Sets up the pane with the non-default content for this group.
     *
     * @param contentPane
     *         the content shall be added to
     */
    private void preparePane(GridPane contentPane) {
        startTimePane = new DateTimePane(zoneId);
        contentPane.add(startTimePane, 2, 0);
        
        numbersText.focusedProperty().addListener((obs, o, n) -> {
            //update stop time proposal
            if (!o && n) startTimePane.setValue(Instant.now());
        });
    }
    
    @Override
    protected Optional<Person> createUpdate(Person toUpdate) {
        PersonImpl copy = new PersonImpl(toUpdate);
        copy.setStart(startTimePane.getValue().orElse(null));
        return Optional.of(copy);
    }
}
