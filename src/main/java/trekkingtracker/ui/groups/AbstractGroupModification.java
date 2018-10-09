package trekkingtracker.ui.groups;

import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import trekkingtracker.Utils;
import trekkingtracker.data.Person;
import trekkingtracker.event.participantevents.ParticipantInputChangedEvent;
import trekkingtracker.event.participantevents.ParticipantProcessor;
import trekkingtracker.event.participantevents.ParticipantUpdateEvent;
import trekkingtracker.event.publishing.EventPublisher;
import trekkingtracker.event.requestevents.ParticipantUpdateRequest;
import trekkingtracker.ui.MainApp;
import trekkingtracker.ui.utils.CollapsibleGridPane;
import trekkingtracker.ui.utils.UiUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Offers controls to set a value for multiple {@code Persons} at once. To define what values shall be set the
 * {@link #contentPane} is available which allows to add custom UI widgets to this {@code Group}.
 */
public abstract class AbstractGroupModification extends Group implements ParticipantProcessor {
    
    /** Allowed input for the start numbers field */
    private final Pattern NUMBERS_INPUT_PATTERN = Pattern.compile("[0-9,]*");
    /** Tooltip text to be displayed when no start numbers are entered */
    private static final String DEFAULT_TEST_HINT = "Enter the participants' start numbers you want to modify at once";
    protected final TextField numbersText;
    /** Tells which entered numbers can be matched to participants */
    private final Tooltip numbersFoundTooltip;
    /** Used to propagate events */
    protected final EventPublisher eventPublisher;
    /** Allows this ui element to collapse */
    private final TitledPane collapsibleContainer;
    /** Holds all participants which a start number by this start number */
    private Map<Integer, Person> startNumbersToPersons = new ConcurrentHashMap<>();
    /** Holds the custom content of this group, i.e. the content which is not related to the number input */
    protected final GridPane contentPane;
    
    /**
     * Creates a new {@code ParticipantStop} interface.
     *
     * @param parent
     *         this ui element will be put in
     * @param eventPublisher
     *         used to propagate events
     * @param description
     *         the text of the button and the group
     */
    public AbstractGroupModification(Pane parent, final EventPublisher eventPublisher, String description) {
        this.eventPublisher = requireNonNull(eventPublisher);
        CollapsibleGridPane mainPane = UiUtils.createFramedArea(parent, description);
        collapsibleContainer = mainPane.getCollapsibleContainer();
        collapsibleContainer.setExpanded(false);
        contentPane = new GridPane();
        mainPane.addColumn(2, contentPane);
        mainPane.add(UiUtils.createLabel("Comma-separated starter numbers", HPos.RIGHT), 0, 0);
        numbersText = new TextField();
        mainPane.add(numbersText, 1, 0);
        
        numbersFoundTooltip = new Tooltip(DEFAULT_TEST_HINT);
        //        numbersFoundTooltip.setShowDuration(Duration.INDEFINITE); only in J9+
        
        final Button storeModificationButton = new Button(description);
        mainPane.add(storeModificationButton, 3, 0);
        
        prepareNumbersText();
        prepareModifiyButton(storeModificationButton);
    }
    
    /** Sets up multiple features of the {@link #numbersText}. */
    private void prepareNumbersText() {
        numbersText.setTooltip(numbersFoundTooltip);
        
        //Allow only numbers and commas
        numbersText.setTextFormatter(new TextFormatter<>(c -> {
            String controlNewText = c.getControlNewText();
            if (controlNewText.isEmpty()) return c;
            return NUMBERS_INPUT_PATTERN.matcher(controlNewText).matches() ? c : null;
        }));
        
        numbersText.focusedProperty().addListener((obs, o, n) -> {
            // show tooltip with persons matching the start numbers
            if (n) {
                Bounds boundsInScene = numbersText.localToScreen(numbersText.getBoundsInLocal());
                numbersFoundTooltip.show(numbersText, boundsInScene.getMinX(), boundsInScene.getMaxY() + 10);
            } else {
                numbersFoundTooltip.hide();
                numbersFoundTooltip.setText(DEFAULT_TEST_HINT);
            }
        });
        
        // update tooltip when input changes
        numbersText.textProperty().addListener((obs, o, n) -> {
            MatchingResult matchingPersons = new MatchingResult(startNumbersToPersons, numbersText.getText());
            numbersFoundTooltip.setText(matchingPersons.found.toString());
        });
    }
    
    /**
     * Sets the on-click-behavior of the {@code storeStopButton} (i.e. communicate the stop times for all persons).
     *
     * @param storeModificationButton
     *         the behavior shall be set up for
     */
    private void prepareModifiyButton(final Button storeModificationButton) {
        // on click find matching persons and set their start numbers
        storeModificationButton.setOnAction(
                e -> storeModification(new MatchingResult(startNumbersToPersons, numbersText.getText())));
    }
    
    protected void storeModification(MatchingResult matchingPersons) {
        List<Integer> notFound = matchingPersons.notFound;
        if (!notFound.isEmpty()) MainApp.printInfo(String.format("Could not find participants for %s. ", notFound));
        String remaining = notFound.stream().map(Object::toString).collect(Collectors.joining(","));
        numbersText.setText(remaining);
        List<Person> foundPersons = matchingPersons.found;
        foundPersons.forEach(p -> {
            Optional<Person> update = createUpdate(p);
            if (update.isPresent()) eventPublisher.publish(new ParticipantUpdateRequest(p, update.get()));
            else MainApp.printInfo(String.format("Update for %s cancelled.", p));
        });
    }
    
    /**
     * Creates an (updated) {@code Person} for the given match.
     *
     * @param toUpdate
     *         the information about a matching {@code Person} an updated shall be created for
     * @return the updated {@code Person}, if the update shall be done
     */
    protected abstract Optional<Person> createUpdate(Person toUpdate);
    
    @Override
    public void setInput(final ParticipantInputChangedEvent inputEvent) {
        Collection<Person> participants = inputEvent.getInputView();
        if (participants == null) {
            collapsibleContainer.setExpanded(false);
            return;
        }
        startNumbersToPersons = Utils.getStartNumberToParticipantMap(participants);
        collapsibleContainer.setExpanded(true);
    }
    
    @Override
    public void updatePerson(final ParticipantUpdateEvent updateEvent) {
        Person oldP = updateEvent.getOldValue();
        Person newP = updateEvent.getNewValue();
        if (oldP != null) startNumbersToPersons.remove(oldP.getNumber());
        Integer number = newP.getNumber();
        if (number != null) startNumbersToPersons.put(number, newP);
    }
    
    /**
     * Holds reads and all persons found for the given start numbers and all numbers no persons could be found
     * for
     */
    protected class MatchingResult {
        /**
         * All {@code Persons} identified by a given number
         */
        public List<Integer> notFound = new ArrayList<>();
        /**
         * All number which could not matched with a {@code Person}
         */
        public List<Person> found;
        
        /**
         * Creates  a new {@code MatchingResult}
         *
         * @param startNumbersToPersons
         *         all known {@code Persons} by their start numbers
         * @param toParse
         *         containing a comma-separated list of all start numbers the the matching {@code Person} shall be
         *         identified for (by the given map)
         */
        public MatchingResult(Map<Integer, Person> startNumbersToPersons, String toParse) {
            found = new ArrayList<>(startNumbersToPersons.size());
            if (toParse == null || toParse.isEmpty()) return;
            for (String number : toParse.split(",")) {
                Integer startNumber = Integer.parseInt(number);
                Person matchingPerson = startNumbersToPersons.get(startNumber);
                if (matchingPerson == null) notFound.add(startNumber);
                else found.add(matchingPerson);
            }
        }
    }
}