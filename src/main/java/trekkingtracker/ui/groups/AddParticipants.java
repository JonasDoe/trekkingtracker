package trekkingtracker.ui.groups;

import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import trekkingtracker.data.Person;
import trekkingtracker.data.PersonImpl;
import trekkingtracker.event.participantevents.ParticipantInputChangedEvent;
import trekkingtracker.event.participantevents.ParticipantProcessor;
import trekkingtracker.event.participantevents.ParticipantUpdateEvent;
import trekkingtracker.event.publishing.EventPublisher;
import trekkingtracker.event.requestevents.ParticipantInputChangeRequest;
import trekkingtracker.event.requestevents.ParticipantInputResetRequest;
import trekkingtracker.event.requestevents.ParticipantUpdateRequest;
import trekkingtracker.ui.MainApp;
import trekkingtracker.ui.utils.CollapsibleGridPane;
import trekkingtracker.ui.utils.UiUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/** Allows the user to add participants, especially for the initial state */
public class AddParticipants extends Group implements ParticipantProcessor {
    /** The extension of the initial participants file */
    private final String fileExtension;
    /** The last folder an initial participants file was selected from */
    private File lastChosenFolder = new File(System.getProperty("user.dir"));
    /** The chosen {@code Charset} of the initial participants */
    private final SingleSelectionModel<Charset> charsetSelection;
    /** Used to propagate events */
    private final EventPublisher eventPublisher;
    /** Allows to collapse this ui element */
    private final TitledPane collapsibleContainer;
    /** Allows to choose the birthday of a person to be added */
    private final DatePicker birthdayPicker;
    /** Allows to chosose the name of a person to be added */
    private final TextField nameField;
    /** If {@code true} re-loading a file will trigger a warning before */
    private boolean initialized = false;
    
    /**
     * Creates a new {@code AddParticipants}.
     *
     * @param parent
     *         contains this ui element
     * @param eventPublisher
     *         used to propagate events
     * @param fileExtension
     *         suffix of the initial data file, e.g. {@code .csv}
     */
    public AddParticipants(Pane parent, final EventPublisher eventPublisher, String fileExtension) {
        this.eventPublisher = requireNonNull(eventPublisher);
        this.fileExtension = requireNonNull(fileExtension);
        CollapsibleGridPane mainPane = UiUtils.createFramedArea(parent, "Add participants");
        collapsibleContainer = mainPane.getCollapsibleContainer();
        collapsibleContainer.setExpanded(false);
        GridPane loadFilePane = new GridPane();
        mainPane.addColumn(0, loadFilePane);
        
        loadFilePane.addRow(0, UiUtils.createLabel("Load participants file", HPos.LEFT));
        Button loadButton = new Button("Select initial participants file");
        loadButton.setTooltip(new Tooltip(
                "Loads the file containing the registered participants and resets any " + "already loaded data."));
        loadButton.setOnAction(e -> this.openFileDialog());
        loadFilePane.addRow(1, loadButton);
        ComboBox<Charset> charsetComboBox = new ComboBox<>();
        loadFilePane.add(charsetComboBox, 0, 2);
        charsetSelection = charsetComboBox.getSelectionModel();
        
        Separator separator = new Separator();
        separator.setPadding(new Insets(0, 10, 0, 10));
        separator.setOrientation(Orientation.VERTICAL);
        mainPane.addColumn(1, separator);
        
        GridPane addParticipant = new GridPane();
        mainPane.addColumn(2, addParticipant);
        addParticipant.addRow(0, UiUtils.createLabel("Add participant", HPos.LEFT));
        nameField = new TextField();
        addParticipant.addRow(1, nameField);
        birthdayPicker = new DatePicker();
        addParticipant.addRow(2, birthdayPicker);
        ComboBox<Person.Category> categoryComboBox = UiUtils.createCategoryComboBox();
        addParticipant.addRow(3, categoryComboBox);
        Button addParticipantButton = new Button("Add participant");
        addParticipant.addRow(4, addParticipantButton);
        addParticipantButton.setDisable(true);
        nameField.textProperty().addListener(
                (obs, o, n) -> addParticipantButton.setDisable(n.isEmpty() || birthdayPicker.getValue() == null));
        birthdayPicker.valueProperty().addListener(e -> addParticipantButton.setDisable(
                (nameField.getText().isEmpty() || birthdayPicker.getValue() == null)));
        
        prepareAddParticipantButton(addParticipantButton, categoryComboBox);
        prepareCharSetComboBox(charsetComboBox);
    }
    
    @Override
    public void setInput(final ParticipantInputChangedEvent inputEvent) {
        Set<Person> input = inputEvent.getInputView();
        initialized = input != null;
        if (input != null && input.size() > 0) {
            MainApp.printInfo(String.format("Loaded %d persons.", input.size()));
            collapsibleContainer.setExpanded(false);
        } else {
            MainApp.printInfo(
                    "Could not load restore any data. Please load select a file with the registered participants.");
            collapsibleContainer.setExpanded(true);
        }
    }
    
    @Override
    public void updatePerson(final ParticipantUpdateEvent updateEvent) {
        if (updateEvent.getOldValue() == null) {
            MainApp.printInfo(updateEvent.getNewValue() + " added.");
            nameField.setText("");
            birthdayPicker.setValue(null);
        }
    }
    
    /** Allows the user to select a file containing the initial participants. */
    private void openFileDialog() {
        if (initialized) {
            Alert alreadyLoadedAlert = new Alert(Alert.AlertType.CONFIRMATION);
            alreadyLoadedAlert.setTitle("Participants are already loaded");
            alreadyLoadedAlert.setHeaderText(
                    "The participants have been loaded already. Loading them again resets all actions and " +
                            "changes that have been made since since the last initial load.");
            alreadyLoadedAlert.setContentText(
                    "Do you want drop the changes and re-load the participants nevertheless?");
            ButtonType overrideDecision = alreadyLoadedAlert.showAndWait().orElse(null);
            if (overrideDecision == null || overrideDecision == ButtonType.CANCEL) return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Participants Table");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(String.format("Table Files (%s)", fileExtension), fileExtension));
        if (lastChosenFolder.canRead()) fileChooser.setInitialDirectory(lastChosenFolder);
        else {
            lastChosenFolder = new File(System.getProperty("user.dir"));
            if (lastChosenFolder.canRead()) fileChooser.setInitialDirectory(lastChosenFolder);
        }
        File chosen = fileChooser.showOpenDialog(getScene().getWindow());
        if (chosen == null) return;
        if (!chosen.exists()) {
            String error = String.format("%s does not exist.", chosen);
            MainApp.printInfo(error);
            throw new IllegalArgumentException(error);
        } else eventPublisher.publish(new ParticipantInputResetRequest(chosen, charsetSelection.getSelectedItem()));
        lastChosenFolder = chosen.getParentFile();
    }
    
    /**
     * Sets up the behavior of the {@code Add Participant} button click: Add participant if already initialized,
     * otherwise ask for confirmation and initialze.
     *
     * @param addParticipantButton
     *         the button whose behavior shall be set up
     * @param categoryComboBox
     *         {@code Category} the participant shall be registered for
     */
    private void prepareAddParticipantButton(final Button addParticipantButton,
                                             final ComboBox<Person.Category> categoryComboBox) {
        addParticipantButton.setOnAction(e -> {
            //boolean initialized = participantStore.isInitialized();
            PersonImpl created = new PersonImpl(nameField.getText(), birthdayPicker.getValue());
            created.setCategory(categoryComboBox.getValue());
            if (created.getName().isEmpty() || created.getBirthday() == null) return; //should not happen here
            if (initialized) eventPublisher.publish(new ParticipantUpdateRequest(null, created));
            else {
                Alert newInit = new Alert(Alert.AlertType.CONFIRMATION);
                newInit.setTitle("Initialize with one person?");
                newInit.setHeaderText(
                        "There are no initial participants loaded yet, so you are about to initialize the program\n" + "with only one participant and therefore will have to add all other participants\n " + "in the same manner. In general, it's recommended to load an initial file instead.");
                newInit.setContentText("Do you plan to enter all participants this way and there want continue?");
                ButtonType newInitDecision = newInit.showAndWait().orElse(null);
                if (newInitDecision != null && !newInitDecision.equals(ButtonType.CANCEL)) eventPublisher.publish(
                        new ParticipantInputChangeRequest(Collections.singleton(created)));
            }
        });
    }
    
    /**
     * Sets the content of the {@code ComboBox} which allows to select the {@code Charset} of the initial file.
     *
     * @param charsetComboBox
     *         to be set up
     */
    private void prepareCharSetComboBox(final ComboBox<Charset> charsetComboBox) {
        charsetComboBox.setEditable(false);
        charsetComboBox.setItems(
                FXCollections.observableArrayList(StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1));
        charsetSelection.selectFirst();
        charsetComboBox.setTooltip(new Tooltip(
                "The encoding of the participants' file. If you face wrongly displayed letters, " + "you'll probably "
                        + "have to change the encoding."));
        charsetComboBox.setConverter(UiUtils.createToStringConverter(cs -> {
            if (cs.equals(StandardCharsets.ISO_8859_1)) return "Windows (Latin-1)";
            else if (cs.equals(StandardCharsets.UTF_8)) return "Linux (UTF-8)";
            else return cs.toString();
        }));
    }
    
}
