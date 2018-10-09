package trekkingtracker.ui.groups;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import trekkingtracker.data.Person;
import trekkingtracker.data.PersonTripTimeComparator;
import trekkingtracker.event.participantevents.ParticipantInputChangedEvent;
import trekkingtracker.event.participantevents.ParticipantProcessor;
import trekkingtracker.event.participantevents.ParticipantUpdateEvent;
import trekkingtracker.ui.utils.CollapsibleGridPane;
import trekkingtracker.ui.utils.UiUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Displays the rankings per {@code Category} and {@code Persons} still on track. */
public class Overview extends Group implements ParticipantProcessor {
    /** Allows to choose a {@code Category} */
    private final SingleSelectionModel<Person.Category> category;
    /** Displays the ranking for the chosen {@code Category} */
    private final ListView<Person> rankingView;
    /** Displays all participants which are still on tour (or alternatively who have already arrived) */
    private final ListView<Person> onTour;
    /** Used to format the participants' start time for better readability */
    private final Function<Instant, String> dateFormatter;
    /** All participants in all {@code Categories}, the basis for the rank calculations */
    private Collection<Person> participants = ConcurrentHashMap.newKeySet();
    /** Tells whether all participants who are still on track or all who have already arrived shall be displayed */
    private final SingleSelectionModel<Boolean> showMissing;
    
    /**
     * Creates a new {@code Overview} interface.
     *
     * @param parent
     *         this ui element will be put in
     * @param zoneId
     *         used to display start and stop dates
     */
    public Overview(Pane parent, ZoneId zoneId) {
        dateFormatter = UiUtils.getInstantFormatter(zoneId);
        CollapsibleGridPane mainPane = UiUtils.createFramedArea(parent, "Overview");
        TitledPane collapsibleContainer = mainPane.getCollapsibleContainer();
        collapsibleContainer.setExpanded(false);
        GridPane firstCol = new GridPane();
        mainPane.addColumn(0, firstCol);
        ComboBox<Person.Category> categoryComboBox = UiUtils.createCategoryComboBox();
        category = categoryComboBox.getSelectionModel();
        categoryComboBox.setEditable(false);
        firstCol.addRow(0, categoryComboBox);
        rankingView = new ListView<>();
        rankingView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        firstCol.addRow(1, rankingView);
        
        GridPane secondCol = new GridPane();
        mainPane.add(secondCol, 1, 0);
        ComboBox<Boolean> showMissingComboBox = new ComboBox<>();
        showMissing = showMissingComboBox.getSelectionModel();
        secondCol.addRow(0, showMissingComboBox);
        onTour = new ListView<>();
        secondCol.addRow(1, onTour);
        
        showMissingComboBox.setOnAction(e -> refreshOnTourView(showMissing.getSelectedItem()));
        categoryComboBox.setOnAction(e -> refreshRankingView(categoryComboBox.getSelectionModel().getSelectedItem()));
        prepareRankingView();
        prepareShowMissingComboBox(showMissingComboBox);
        prepareOnTourView();
    }
    
    /** Sets up the conversion from {@code Person} to display {@code String} in the {@link #onTour} view. */
    private void prepareOnTourView() {
        onTour.setCellFactory(rv -> {
            TextFieldListCell<Person> cell = new TextFieldListCell<>();
            cell.setConverter(UiUtils.createToStringConverter(p -> {
                if (p != null) return String.format("%s (%s) on %s since %s", p.getName(), p.getNumber(),
                        p.getCategory(),
                        dateFormatter.apply(showMissing.getSelectedItem() ? p.getStart() : p.getStop()));
                else return null;
            }));
            return cell;
        });
    }
    
    /**
     * Sets up the conversion from person to display {@code String} in the given {@code showMissingComboBox}.
     *
     * @param showMissingComboBox
     *         the conversion shall be set up for
     */
    private void prepareShowMissingComboBox(final ComboBox<Boolean> showMissingComboBox) {
        showMissingComboBox.setItems(FXCollections.observableArrayList(true, false));
        showMissingComboBox.setConverter(UiUtils.createToStringConverter(b -> {
            if (b == null) return null;
            else return b ? "Still on track" : "Already arrived";
        }));
        showMissing.selectFirst();
    }
    
    /**
     * Sets up the conversion from {@code Person} to display {@code String} in the {@link #rankingView} view and allows
     * {@code CTRL+C} to copy the ranking data.
     */
    private void prepareRankingView() {
        /* Label providing */
        rankingView.setCellFactory(rv -> {
            TextFieldListCell<Person> cell = new TextFieldListCell<>();
            cell.setConverter(UiUtils.createToStringConverter(p -> {
                if (p != null) {
                    String tripTimeString = getTripTimeString(p);
                    // String tripTimeString = tripTime != null ? tripTime.toHoursPart() + ":" + tripTime
                    // .toMinutesPart() + ":" + tripTime.toSecondsPart() : "-"; only available in J9+
                    return String.format("%s (%s) with %s", p.getName(), p.getNumber(), tripTimeString);
                } else return null;
            }));
            return cell;
        });
        /* STRG+C support*/
        final KeyCombination copy = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        rankingView.addEventHandler(KeyEvent.KEY_RELEASED, (EventHandler<Event>) event -> {
            if (event instanceof KeyEvent && copy.match((KeyEvent) event)) {
                final ClipboardContent content = new ClipboardContent();
                ObservableList<Person> selected = rankingView.getSelectionModel().getSelectedItems();
                content.putString(selected.stream()
                                          .filter(Objects::nonNull)
                                          .map(p -> p.getName() + ',' + getTripTimeString(p))
                                          .collect(Collectors.joining("\n")));
                clipboard.setContent(content);
            }
        });
    }
    
    private String getTripTimeString(Person p) {
        Duration tripTime = p.getTripTime().orElse(null);
        String tripTimeString;
        if (tripTime == null) tripTimeString = "-";
        else {
            long timeInSeconds = tripTime.getSeconds();
            long hours = timeInSeconds / (3600);
            int minutes = (int) ((timeInSeconds % (3600)) / 60);
            int seconds = (int) (timeInSeconds % 60);
            tripTimeString = String.format("%d:%d:%d", hours, minutes, seconds);
        }
        return tripTimeString;
    }
    
    /**
     * Refreshes the content of the {@link #rankingView} based on the chosen {@code Category}.
     *
     * @param category
     *         the chosen {@code Category}
     */
    private void refreshRankingView(final Person.Category category) {
        UiUtils.backgroundJob(() -> participants.stream()
                                                .filter(p -> p.getCategory() == category)
                                                .sorted(PersonTripTimeComparator.INSTANCE)
                                                .collect(Collectors.toList()), l -> {
            rankingView.setItems(FXCollections.observableList(l));
            rankingView.refresh();
        });
    }
    
    /**
     * Refreshes the content of the {@link #rankingView} based on the chosen {@code Category}.
     */
    private void refreshOnTourView(boolean showNotArrived) {
        UiUtils.backgroundJob(() -> participants.stream()
                                                .filter(p -> p.getStart() != null)
                                                .filter(p -> showNotArrived == (p.getStop() == null))
                                                .sorted(Comparator.comparing(Person::getStart))
                                                .collect(Collectors.toList()), l -> {
            onTour.setItems(FXCollections.observableList(l));
            onTour.refresh();
        });
    }
    
    @Override
    public void setInput(final ParticipantInputChangedEvent inputEvent) {
        Collection<Person> input = inputEvent.getInputView();
        if (input == null) return;
        this.participants = ConcurrentHashMap.newKeySet(input.size());
        this.participants.addAll(input);
        refreshRankingView(category.getSelectedItem());
        refreshOnTourView(showMissing.getSelectedItem());
    }
    
    @Override
    public void updatePerson(final ParticipantUpdateEvent updateEvent) {
        Person oldP = updateEvent.getOldValue();
        Person newP = updateEvent.getNewValue();
        if (oldP != null) participants.remove(oldP);
        participants.add(newP);
        Person.Category selectedCategory = category.getSelectedItem();
        if (newP.getCategory() == selectedCategory) refreshRankingView(selectedCategory);
        if (oldP == null || (oldP.getStop() != newP.getStop() || oldP.getStart() != newP.getStart())) refreshOnTourView(
                showMissing.getSelectedItem());
    }
}
