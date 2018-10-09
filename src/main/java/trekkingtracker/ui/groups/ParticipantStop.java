package trekkingtracker.ui.groups;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import trekkingtracker.data.Person;
import trekkingtracker.data.PersonImpl;
import trekkingtracker.event.publishing.EventPublisher;
import trekkingtracker.event.requestevents.ParticipantUpdateRequest;
import trekkingtracker.ui.MainApp;
import trekkingtracker.ui.utils.DateTimePane;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Offers controls to set stop times for multiple {@code Persons} at once */
//TODO validate that stop comes after start
public class ParticipantStop extends AbstractGroupModification {
    /** The number of milliseconds the stop time can lie in the past before the confirmation dialog is triggered. */
    private static final long CONFIRMATION_THRESHOLD_MILLIS = 5 * 60 * 100;
    /** Used to display the stop time proposal */
    private final ZoneId zoneId;
    /** Holds the stop time to be set for all selected participants */
    private DateTimePane stopTimePane;
    /** The format of the stop time to be displayed in the confirmation dialog */
    private final DateTimeFormatter confirmationTimeFormat;
    /**
     * Allows do decide whether all participants a stop time will be assigned to will count as finishers
     */
    private CheckBox finishersCheckBox;
    
    /**
     * Creates a new {@code ParticipantStop} interface.
     *
     * @param parent
     *         this ui element will be put in
     * @param eventPublisher
     *         used to propagate events
     * @param zoneId
     *         used to display the stop time proposal
     */
    public ParticipantStop(Pane parent, final EventPublisher eventPublisher, ZoneId zoneId) {
        super(parent, eventPublisher, "Stop");
        this.zoneId = zoneId;
        confirmationTimeFormat = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy").withZone(zoneId);
        preparePane(contentPane);
    }
    
    /**
     * Sets up the pane with the non-default content for this group.
     *
     * @param contentPane
     *         the content shall be added to
     */
    private void preparePane(GridPane contentPane) {
        stopTimePane = new DateTimePane(zoneId);
        contentPane.add(stopTimePane, 2, 0);
        
        finishersCheckBox = new CheckBox("Count as finishers");
        finishersCheckBox.setSelected(true);
        contentPane.add(finishersCheckBox, 2, 1);
        
        numbersText.focusedProperty().addListener((obs, o, n) -> {
            //update stop time proposal
            if (!o && n) stopTimePane.setValue(Instant.now());
        });
    }
    
    @Override
    protected void storeModification(MatchingResult matchingPersons) {
        List<Integer> notFound = matchingPersons.notFound;
        if (!notFound.isEmpty()) MainApp.printInfo(String.format("Could not find participants for %s. ", notFound));
        List<Person> foundPersons = matchingPersons.found;
        if (foundPersons.isEmpty()) return;
        
        /* Show dialog to confirm the stop time if it lies to much in the past. Additional, the whole operation can
        be cancelled */
        Instant stopTime = stopTimePane.getValue().orElse(null);
        Instant now = Instant.now();
        if (stopTime != null && now.toEpochMilli() - stopTime.toEpochMilli() > CONFIRMATION_THRESHOLD_MILLIS) {
            String optionNow = String.format("Now (%s)", confirmationTimeFormat.format(now));
            String optionSelected = String.format("Selected (%s)", confirmationTimeFormat.format(stopTime));
            ChoiceDialog<String> confirmation = new ChoiceDialog<>(optionNow, optionSelected);
            confirmation.setTitle("Confirm time to set");
            confirmation.setHeaderText(String.format(
                    "The selected time lies more than %d minutes in the past. Which time do you want to set?",
                    CONFIRMATION_THRESHOLD_MILLIS / (60 * 1000)));
            confirmation.setContentText("Time to assign");
            String choice = confirmation.showAndWait().orElse(null);
            if (choice == null) return;
            else if (choice.equals(optionNow)) stopTime = now;
        }
        
        String remaining = notFound.stream().map(Object::toString).collect(Collectors.joining(","));
        numbersText.setText(remaining);
        Instant stopTimeToUse = stopTime;
        foundPersons.forEach(p -> {
            PersonImpl copy = new PersonImpl(p);
            copy.setStop(stopTimeToUse);
            copy.setFinished(finishersCheckBox.isSelected());
            eventPublisher.publish(new ParticipantUpdateRequest(p, copy));
        });
        finishersCheckBox.setSelected(true);
    }
    
    @Override
    protected Optional<Person> createUpdate(Person toUpdate) {
        PersonImpl copy = new PersonImpl(toUpdate);
        Instant stopTime = stopTimePane.getValue().orElse(null);
        copy.setStop(stopTime);
        copy.setFinished(finishersCheckBox.isSelected());
        return Optional.of(copy);
    }
}
