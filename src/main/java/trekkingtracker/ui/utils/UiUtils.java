package trekkingtracker.ui.utils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import trekkingtracker.data.Person;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Holds UI-related utility methods. */
public final class UiUtils {
    /** Default pattern to display dates. */
    public static final String DATE_DISPLAY_PATTERN = TimeTextField.TIME_FOMRAT_PATTERN + " dd.MM.yy";
    
    /** Pure util class, not intended to be instantiated */
    private UiUtils() {
    }
    
    /**
     * Retuns a {@code Function} which creates a readable {@code String} from an {@code Instant}.
     *
     * @param zoneId
     *         for the {@code Instant} argument
     * @return the formatter {@code Function}
     */
    public static Function<Instant, String> getInstantFormatter(ZoneId zoneId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_DISPLAY_PATTERN).withZone(zoneId);
        return formatter::format;
    }
    
    /**
     * Creates a {@code null}-safe {@code StringConverter} which convert's a given object into a {@code String} (but
     * returns {@code null} for the opposite direction).
     *
     * @param toString
     *         used to convert a given object into a display {@code String}.
     * @param <T>
     *         the type of the objects which will be converted
     * @return the newly created {@code StringConverter}
     */
    public static <T> StringConverter<T> createToStringConverter(Function<T, String> toString) {
        return new StringConverter<T>() {
            @Override
            public String toString(final T object) {
                if (object == null) return null;
                else return toString.apply(object);
            }
            
            @Override
            public T fromString(final String string) {
                return null;
            }
        };
    }
    
    /**
     * Creates an area with a frame and a title.
     *
     * @param container
     *         the framed area shall be put in
     * @param text
     *         the title of the framed area
     * @return the pane contained in the framed area
     */
    public static CollapsibleGridPane createFramedArea(final Pane container, final String text) {
        final TitledPane pane = new TitledPane();
        pane.setText(text);
        //        pane.setCollapsible(false);
        container.getChildren().add(pane);
        final CollapsibleGridPane grid = new CollapsibleGridPane(pane);
        pane.setContent(grid);
        // GridPane.setMargin(grid, new Insets(10, 10, 10, 10));
        return grid;
    }
    
    /**
     * Creates a new {@code Label} with the given test and the given alignment.
     *
     * @param text
     *         to be displayed in the {@code Label}
     * @param alignment
     *         of the text in the {@code Label}
     * @return the newly created {@code Label}
     */
    public static Label createLabel(String text, HPos alignment) {
        Label label = new Label(text);
        GridPane.setHalignment(label, alignment);
        GridPane.setMargin(label, new Insets(0, 10, 0, 10));
        return label;
    }
    
    /**
     * Creates a new read-only {@code ComboBox} offering the {@code Categories} as options.
     *
     * @return the {@code ComboBox} offering the {@code Categories} as options
     */
    public static ComboBox<Person.Category> createCategoryComboBox() {
        ComboBox<Person.Category> categoryComboBox = new ComboBox<>(
                FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(Person.Category.values())));
        categoryComboBox.setEditable(false);
        categoryComboBox.getSelectionModel().selectFirst();
        return categoryComboBox;
    }
    
    /**
     * Runs a non-UI job.
     *
     * @param backgroundJob
     *         non-UI job to be run
     */
    public static void backgroundJob(final Runnable backgroundJob) {
        new Thread(backgroundJob).start();
    }
    
    /**
     * Runs a UI job.
     *
     * @param uiJob
     *         UI job to be run
     */
    public static void uiJob(final Runnable uiJob) {
        Platform.runLater(uiJob);
    }
    
    /**
     * Runs a given non-UI job which returns a result. This result will be applied to a UI job afterwards. A typical
     * use case is a slow data fetch which will update the UI with the data once fetched. This way the UI won't
     * freeze while the data gets fetched.
     *
     * @param backgroundJob
     *         (probably slow) non-UI job supplying result for the subsequent UI job
     * @param uiUpdateJob
     *         UI job who process (i.e. displays) the result
     * @param <T>
     *         the result to be fetched by the non-UI job and to be processed by the UI job
     */
    public static <T> void backgroundJob(final Supplier<T> backgroundJob, final Consumer<T> uiUpdateJob) {
        new Thread(() -> {
            T result = backgroundJob.get();
            uiJob(() -> uiUpdateJob.accept(result));
        }).start();
    }
    
    /**
     * Runs a non-UI job and afterwards an unrelated UI job
     *
     * @param backgroundJob
     *         non-UI job to be run first
     * @param uiUpdateJob
     *         UI job to be run once the background job is finished
     */
    public static void backgroundJob(final Runnable backgroundJob, final Runnable uiUpdateJob) {
        new Thread(() -> {
            backgroundJob.run();
            uiJob(uiUpdateJob);
        }).start();
    }
    
    /**
     * Creates a new {@code ComboBox} which allows to choose {@code Persons} and make proposals based on the entered
     * text.
     *
     * @param contentSupplier
     *         used to restore the original content of the {@code ComboBox}
     * @param selectionCallback
     *         optional listener which will be called when the user chooses an item
     * @return the newly created {@code ComboBox}
     */
    public static ComboBox<Person> createPersonComboBox(final Supplier<Collection<Person>> contentSupplier,
                                                        Consumer<Person> selectionCallback) {
        ComboBox<Person> personComboBox = new ComboBox<>();
        DateTimeFormatter birthdayFormatter = DateTimeFormatter.ofPattern("d.M.YY");
        StringConverter<Person> converter = UiUtils.createToStringConverter(p -> {
            LocalDate birthday = p.getBirthday();
            return birthday != null ? p.getName() + " (" + birthdayFormatter.format(birthday) + ")" : p.getName();
        });
        personComboBox.setConverter(converter);
        UiUtils.addAutoCompleteFeature(personComboBox, contentSupplier,
                (typedText, itemToCompare) -> converter.toString(itemToCompare)
                                                       .toLowerCase()
                                                       .contains(typedText.toLowerCase()),
                p -> {if (p != null) selectionCallback.accept(p);});
        return personComboBox;
    }
    
    /**
     * Adds the auto-complete feature to the given {@code ComboBox}.
     *
     * @param comboBox
     *         the feature shall be added to
     * @param contentSupplier
     *         used to restore the original content of the {@code ComboBox}
     * @param comparatorMethod
     *         to match the entered text with the {@code ComboBoxe's} items
     * @param selectionCallback
     *         optional listener which will be called when the user chooses an item, can be {@code
     *         null}.
     * @param <T>
     *         the items hold by the {@code ComboBox)
     */
    // inspired by https://stackoverflow.com/questions/19924852/autocomplete-combobox-in-javafx/27384068
    public static <T> void addAutoCompleteFeature(final ComboBox<T> comboBox,
                                                  final Supplier<Collection<T>> contentSupplier,
                                                  final BiPredicate<String, T> comparatorMethod,
                                                  final Consumer<T> selectionCallback) {
        comboBox.setEditable(true);
        AtomicReference<T> lastSelected = new AtomicReference<>();
        final SingleSelectionModel<T> selectionModel = comboBox.getSelectionModel();
        
        // register event handlers
        if (selectionCallback != null)
            enableMouseSupport(comboBox, selectionCallback.andThen(t -> comboBox.hide()), lastSelected);
        comboBox.addEventHandler(KeyEvent.KEY_PRESSED, t -> comboBox.hide());
        // nasty fix to prevent the combo box's selection to disappear as soon as it lt loses focus and the
        // StringConverter does not support fromString conversion
        selectionModel.selectedItemProperty().addListener((obs, oldP, newP) -> {
            if (newP == null && oldP != null) try {
                comboBox.getSelectionModel().select(oldP);
            } catch (Exception e) {
                int a = 0; //TODO for breakpoint ... exception happens from time to time
            }
        });
        comboBox.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
    
            /**
             * If text does not change -> don't update the {@code ComboBox's} text field and therefore don't
             * "destroy" {@code STRG+A} commands etc.
             */
            private String lastText = null;
    
            @Override
            public void handle(final KeyEvent event) {
                KeyCode key = event.getCode();
                if (key == KeyCode.UP) lastSelected.set(selectionModel.getSelectedItem());
                else if (key == KeyCode.DOWN) {
                    lastSelected.set(selectionModel.getSelectedItem());
                    if (!comboBox.isShowing()) comboBox.show();
                } else if (key == KeyCode.BACK_SPACE || key == KeyCode.DELETE)
                    updateComboBox(comboBox.getEditor().getCaretPosition());
                else if (key == KeyCode.ENTER) {
                    T last = lastSelected.get();
                    if (last != null) selectionModel.select(last);
                    else selectionModel.selectFirst();
                    if (!selectionModel.isEmpty() && selectionCallback != null)
                        selectionCallback.accept(selectionModel.getSelectedItem());
                } else if (key == KeyCode.ESCAPE) {
                    lastText = null;
                    comboBox.getEditor().setText("");
                    updateComboBox(0);
                } else updateComboBox(comboBox.getEditor().getCaretPosition());
            }
    
            /**
             * Updates the {@code ComboBox's} content and its text field.
             *
             * @param caretPos
             *         indicates the position of the text field's caret to be set
             */
            private void updateComboBox(Integer caretPos) {
                final String enteredText = comboBox.getEditor().getText();
                if (Objects.equals(lastText, enteredText)) return;
                
                // filter for matching items
                final ObservableList<T> list = FXCollections.observableArrayList();
                for (final T aData : contentSupplier.get()) {
                    if (aData != null && comboBox.getEditor().getText() != null && comparatorMethod.test(
                            comboBox.getEditor().getText(), aData)) {
                        list.add(aData);
                    }
                }
                
                comboBox.setItems(list);
                comboBox.getEditor().setText(enteredText);
                comboBox.getEditor().positionCaret(caretPos);
                if (!list.isEmpty()) comboBox.show();
                lastText = comboBox.getEditor().getText();
            }
        });
    }
    
    /**
     * To be called by {@link #addAutoCompleteFeature(ComboBox, Supplier, BiPredicate, Consumer)}. Allows that an
     * item can be selected with a mouse click. Additionally, hovering with a mouse over an option to
     * choose it with {@code ENTER} will made possible.
     *
     * @param comboBox
     *         the mouse support shall be added to
     * @param selectionCallback
     *         the mouse click shall be propagated to
     * @param lastSelected
     *         stores the item the mouse is hovering over
     * @param <T>
     *         the data item stored in the given {@code ComboBox}
     */
    private static <T> void enableMouseSupport(ComboBox<T> comboBox, Consumer<T> selectionCallback,
                                               AtomicReference<T> lastSelected) {
        // support for mouse clicks, the same way would not work for ENTER
        comboBox.setCellFactory(lv -> {
            ListCell<T> cell = new ListCell<T>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : comboBox.getConverter().toString(item));
                }
            };
            cell.setOnMousePressed(e -> selectionCallback.accept(cell.getItem()));
            // Mouse movement updates internal stored selection (analog to KEYUP and KEYDOWN)
            cell.setOnMouseMoved(e -> lastSelected.set(cell.getItem()));
            return cell;
        });
    }
}
