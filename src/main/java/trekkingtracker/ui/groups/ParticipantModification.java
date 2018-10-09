package trekkingtracker.ui.groups;

import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import trekkingtracker.Utils;
import trekkingtracker.data.Person;
import trekkingtracker.data.PersonAphabeticComparator;
import trekkingtracker.data.PersonImpl;
import trekkingtracker.event.participantevents.ParticipantInputChangedEvent;
import trekkingtracker.event.participantevents.ParticipantProcessor;
import trekkingtracker.event.participantevents.ParticipantUpdateEvent;
import trekkingtracker.event.publishing.EventPublisher;
import trekkingtracker.event.requestevents.ParticipantUpdateRequest;
import trekkingtracker.ui.MainApp;
import trekkingtracker.ui.utils.CollapsibleGridPane;
import trekkingtracker.ui.utils.DateTimePane;
import trekkingtracker.ui.utils.UiUtils;

import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/** Class which allows the modification of all attributes of a {@code Participant}. */
public class ParticipantModification extends Group implements ParticipantProcessor {
    /** Holds the proposed participants */
    private final ComboBox<Person> participantsComboBox;
    /** Used to restore the {@code ComboBox's} content after it has been reduced to filtered proposals */
    private final Collection<Person> participantsComboContent = new ConcurrentSkipListSet<>(
            PersonAphabeticComparator.INSTANCE);
    /** Used to propagate events */
    private final EventPublisher eventPublisher;
    /** Allows to modify a participant's {@code Category} */
    private final ComboBox<Person.Category> categoryComboBox;
    /** Allows to set and modify a participant's start time */
    private final DateTimePane startTimePane;
    /** Allows to decide whether a participant's stop time should be overridden */
    private final CheckBox useStopDate;
    /** Allows to decide whether a participant will counted as finisher or not */
    private final CheckBox isFinisherCheckBox;
    /** Lists all versions of a participant's data */
    private final ListView<Person> personHistoryView;
    /** Allows to collapse this ui element */
    private final TitledPane collapsibleContainer;
    /** Tells whether the start number displayed is a proposal or not */
    private final Label proposalHint;
    /** Holds all assigned start numbers */
    private Map<Integer, Person> usedStartNumbers = new ConcurrentHashMap<>();
    /** Allows to set and modify a participant's start number */
    private final TextField startNumberField;
    /** The last start number which was used, so proposals can just increment this number */
    private int lastStartNumber = 0;
    /** The current participant selected for modification */
    private Person currentPerson = null;
    /** Stores all settings for a participant is is disabled if the content is invalid */
    private final Button storeButton;
    /** Allows to set and modify a participant's stop time */
    private final DateTimePane stopTimePane;
    /** Holds the state history of all participants to display the selected participant's state history */
    private final Map<Person, List<Person>> allStatesData = new ConcurrentHashMap<>();
    
    /**
     * Creates a new {@code ParticipantModification} interface.
     *
     * @param parent
     *         this ui element will be put in
     * @param eventPublisher
     *         used to propagate events
     * @param zoneId
     *         used to display the start and stop time infos
     */
    public ParticipantModification(final Pane parent, final EventPublisher eventPublisher, ZoneId zoneId) {
        this.eventPublisher = requireNonNull(eventPublisher);
        final CollapsibleGridPane mainPane = UiUtils.createFramedArea(parent, "Modify participant properties");
        collapsibleContainer = mainPane.getCollapsibleContainer();
        collapsibleContainer.setExpanded(false);
        GridPane settingModifikation = new GridPane();
        mainPane.addColumn(0, settingModifikation);
        
        GridPane firstPane = new GridPane();
        settingModifikation.addRow(0, firstPane);
        participantsComboBox = UiUtils.createPersonComboBox(
                () -> Collections.unmodifiableCollection(participantsComboContent), this::loadPerson);
        firstPane.add(participantsComboBox, 0, 0);
        storeButton = new Button("Store values");
        firstPane.add(storeButton, 1, 0);
        
        GridPane secondPane = new GridPane();
        secondPane.setMinWidth(500);
        settingModifikation.addRow(1, secondPane);
        startNumberField = new TextField();
        startNumberField.setMaxWidth(50);
        GridPane numberFieldPane = new GridPane();
        numberFieldPane.addRow(0, startNumberField);
        proposalHint = UiUtils.createLabel(("(Proposal)"), HPos.LEFT);
        proposalHint.setVisible(false);
        numberFieldPane.add(proposalHint, 3, 0);
        secondPane.add(numberFieldPane, 1, 0);
        categoryComboBox = UiUtils.createCategoryComboBox();
        secondPane.add(UiUtils.createLabel("Number", HPos.RIGHT), 0, 0);
        //        secondPane.add(startNumberField, 1, 0);
        secondPane.add(categoryComboBox, 2, 0);
        secondPane.setPrefWidth(DateTimePane.PREFERRED_WITH);
        
        secondPane.add(UiUtils.createLabel("Start", HPos.RIGHT), 0, 1);
        startTimePane = new DateTimePane(zoneId);
        secondPane.add(startTimePane, 1, 1);
        secondPane.add(UiUtils.createLabel("Stop", HPos.RIGHT), 0, 2);
        stopTimePane = new DateTimePane(zoneId);
        stopTimePane.setDisable(true);
        secondPane.add(stopTimePane, 1, 2);
        useStopDate = new CheckBox("Modify stop time");
        useStopDate.setTooltip(new Tooltip("With this option set the stop time gets overridden."));
        useStopDate.setSelected(false);
        GridPane.setMargin(useStopDate, new Insets(0, 10, 0, 10));
        secondPane.add(useStopDate, 2, 2);
        
        isFinisherCheckBox = new CheckBox("Finisher");
        isFinisherCheckBox.setSelected(false);
        useStopDate.selectedProperty().addListener((obs, o, n) -> {
            stopTimePane.setDisable(!n);
            if (!currentPerson.isFinisher()) isFinisherCheckBox.setSelected(n);
        });
        
        GridPane.setMargin(isFinisherCheckBox, new Insets(0, 10, 0, 10));
        secondPane.add(isFinisherCheckBox, 2, 3);
        
        prepareNumberField();
        prepareStoreButton();
        
        GridPane versioning = new GridPane();
        mainPane.addColumn(1, versioning);
        
        versioning.addRow(0, new Label("Changes made"));
        personHistoryView = new ListView<>();
        versioning.addRow(1, personHistoryView);
        prepareHistoryView(zoneId);
        
        GridPane.setMargin(settingModifikation, new Insets(0, 10, 0, 0));
    }
    
    /**
     * Sets up the {@link #personHistoryView} so it reacts to selection changes and allows double clicks to restore a
     * previous state.
     *
     * @param zoneId
     *         used to display a participant's start and stop time
     */
    private void prepareHistoryView(final ZoneId zoneId) {
        Function<Instant, String> startStopFormatter = UiUtils.getInstantFormatter(zoneId);//two lines for closure
        Function<Instant, String> nullSafeStartStopFormatter = i -> i != null ? startStopFormatter.apply(i) : "null";
        personHistoryView.setTooltip(new Tooltip("Double click on an entry to restore the participant's state"));
        personHistoryView.setCellFactory(rv -> {
            TextFieldListCell<Person> cell = new TextFieldListCell<>();
            cell.setConverter(UiUtils.createToStringConverter(p -> {
                if (p != null) return String.format(
                        "Start-Number: %s, category: %s, start: %s, stop: %s, isFinisher: %s", p.getNumber(),
                        p.getCategory(), nullSafeStartStopFormatter.apply(p.getStart()),
                        nullSafeStartStopFormatter.apply(p.getStop()), p.isFinisher());
                else return null;
            }));
            
            cell.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    Person clickedVersion = cell.getItem();
                    Person latestVersion = personHistoryView.getItems().get(personHistoryView.getItems().size() - 1);
                    if (latestVersion == null || latestVersion.deepEquals(clickedVersion)) return;
                    
                    // make sure that restoring a previous version won't assign a start number which is already in
                    // use by another person
                    Integer clickedVersionNumber = clickedVersion.getNumber();
                    if (clickedVersionNumber != null) {
                        Person usingNumber = usedStartNumbers.get(clickedVersionNumber);
                        if (usingNumber != null && !usingNumber.equals(clickedVersion)) {
                            MainApp.printInfo(String.format(
                                    "Can't restore version because the start number %d is already in use by %s.",
                                    clickedVersionNumber, usingNumber));
                            return;
                        }
                    }
                    eventPublisher.publish(new ParticipantUpdateRequest(latestVersion, clickedVersion));
                    MainApp.printInfo(String.format("Restored older version of %s.", clickedVersion));
                }
            });
            return cell;
        });
    }
    
    /**
     * Loads a participant into the ui and therefore to be exposed for modifications.
     *
     * @param toLoad
     *         the participant to be loaded into the ui
     */
    private void loadPerson(Person toLoad) {
        currentPerson = toLoad;
        Integer currentPersonNumber = toLoad.getNumber();
        proposalHint.setVisible(currentPersonNumber == null);
        
        Integer proposedNumber;
        if (currentPersonNumber == null) {
            proposedNumber = lastStartNumber + 1;
            while (usedStartNumbers.containsKey(proposedNumber)) proposedNumber++;
        } else proposedNumber = currentPersonNumber;
        
        Instant proposedStartTime = nowIfAbsent(toLoad.getStart());
        Instant proposedStopTime = nowIfAbsent(toLoad.getStop());
        final String no = Integer.toString(proposedNumber);
        startNumberField.setText(no);
        startTimePane.setValue(proposedStartTime);
        if (proposedStopTime != null) stopTimePane.setValue(proposedStopTime);
        categoryComboBox.getSelectionModel().select(toLoad.getCategory());
        useStopDate.setSelected(false);
        stopTimePane.setDisable(proposedStopTime != null);
        isFinisherCheckBox.setSelected(toLoad.isFinisher());
        List<Person> history = allStatesData.get(toLoad);
        personHistoryView.setItems(FXCollections.observableArrayList(history));
        personHistoryView.refresh();
        personHistoryView.scrollTo(history.size() - 1);
        personHistoryView.getSelectionModel().selectLast();
    }
    
    /**
     * Returns the given {@code Instant}, or the current time if it is {@code null}.
     *
     * @param existing
     *         to be checked for {@code null}
     * @return the given {@code Instant}, or the current time if it is {@code null}
     */
    private Instant nowIfAbsent(Instant existing) {
        return existing == null ? Instant.now() : existing;
    }
    
    /** Sets the behavior or the store button, i.e propagate the changes of the selected {@code Person}. */
    private void prepareStoreButton() {
        storeButton.setOnAction(e -> {
            lastStartNumber = Integer.parseInt(startNumberField.getText());
            if (numberIsBlocked(lastStartNumber)) MainApp.printInfo(
                    String.format("Start number %d is already in use.", lastStartNumber));
            else if (!timesAreValid()) MainApp.printInfo("Start time must lie before the stop time.");
            else {
                PersonImpl newPerson = new PersonImpl(currentPerson);
                newPerson.setCategory(categoryComboBox.getValue());
                newPerson.setNumber(lastStartNumber);
                newPerson.setStart(startTimePane.getValue().orElse(null));
                if (useStopDate.isSelected()) newPerson.setStop(stopTimePane.getValue().orElse(null));
                newPerson.setFinished(isFinisherCheckBox.isSelected());
                eventPublisher.publish(new ParticipantUpdateRequest(currentPerson, newPerson));
            }
        });
    }
    
    /**
     * Checks whether the start and stop times are valid, i.e. not {@code null} and in the correct order.
     *
     * @return {@code true} if the given start and stop times are valid, otherwise {@code false}
     */
    private boolean timesAreValid() {
        Instant start = startTimePane.getValue().orElse(null);
        Instant stop = useStopDate.isSelected() ? stopTimePane.getValue().orElse(null) : currentPerson.getStop();
        return (start == null || stop == null || stop.isAfter(start));
    }
    
    /**
     * Checks whether a given start number is already used by another participant.
     *
     * @param number
     *         the start number to check
     * @return {@code true} if the start number is already in use, otherwise {@code false}
     */
    private boolean numberIsBlocked(int number) {
        Person existingPerson = usedStartNumbers.get(number);
        return existingPerson != null && !Objects.equals(existingPerson, currentPerson);
    }
    
    private void prepareNumberField() {
        DecimalFormat format = new DecimalFormat("#.0");
        startNumberField.setTextFormatter(new TextFormatter<>(c -> {
            if (c.getControlNewText().isEmpty()) return c;
            ParsePosition parsePosition = new ParsePosition(0);
            Object object = format.parse(c.getControlNewText(), parsePosition);
            if (object == null || parsePosition.getIndex() < c.getControlNewText().length()) return null;
            else return c;
        }));
        startNumberField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.isEmpty()) // disable store button if there's no start number entered
                storeButton.setDisable(true);
            else { // disable store button if start number is already in use
                int enteredNumber = Integer.parseInt(newText);
                boolean isBlocked = numberIsBlocked(enteredNumber);
                if (isBlocked && !storeButton.isDisable()) MainApp.printInfo(
                        String.format("Start number %d is already in use.", enteredNumber));
                storeButton.setDisable(isBlocked);
            }
        });
    }
    
    @Override
    public void setInput(final ParticipantInputChangedEvent inputEvent) {
        Collection<Person> participants = inputEvent.getInputView();
        if (participants == null) {
            collapsibleContainer.setExpanded(false);
            return;
        }
        participantsComboContent.clear();
        participantsComboContent.addAll(participants);
        usedStartNumbers = Utils.getStartNumberToParticipantMap(participants);
        // Clear list first, otherwise a longer list than before causes an ArrayOutOfBoundsException for some reason
        participantsComboBox.setItems(FXCollections.emptyObservableList());
        participantsComboBox.setItems(FXCollections.observableArrayList(participants));
        SingleSelectionModel<Person> selectionModel = participantsComboBox.getSelectionModel();
        selectionModel.selectFirst();
        
        allStatesData.clear();
        for (Person participant : inputEvent.getInput())
            allStatesData.computeIfAbsent(participant, x -> new ArrayList<>()).add(participant);
        
        loadPerson(selectionModel.getSelectedItem());
        collapsibleContainer.setExpanded(true);
    }
    
    @Override
    public void updatePerson(final ParticipantUpdateEvent updateEvent) {
        Person oldP = updateEvent.getOldValue();
        Person newP = updateEvent.getNewValue();
        Person existingWithNumber = usedStartNumbers.get(newP.getNumber());
        if (existingWithNumber != null && !existingWithNumber.equals(newP)) {
            MainApp.printInfo(String.format("Starting number %d is in use by %s and %s. Please fix that immediately!",
                    newP.getNumber(), newP, oldP));
            return;
        }
        if (oldP != null) {
            usedStartNumbers.remove(oldP.getNumber());
            participantsComboContent.remove(oldP);
        }
        
        usedStartNumbers.put(newP.getNumber(), newP);
        allStatesData.computeIfAbsent(newP, x -> new ArrayList<>()).add(newP);
        
        participantsComboContent.add(newP);
        if (oldP == null) participantsComboBox.getItems().add(newP);
        if (currentPerson.equals(oldP)) {
            //participantsComboBox.getSelectionModel().select(currentPerson);
            currentPerson = newP;
            loadPerson(currentPerson);
        }
    }
}
