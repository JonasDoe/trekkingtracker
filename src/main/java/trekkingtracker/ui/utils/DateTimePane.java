package trekkingtracker.ui.utils;

import javafx.scene.control.DatePicker;
import javafx.scene.layout.GridPane;

import java.time.*;
import java.util.Objects;
import java.util.Optional;

/** Holds {@code Controls} for a time and a date input */
public class DateTimePane extends GridPane {
    /** The prefered size of this {@code DateTimePane} */
    public static final double PREFERRED_WITH = 180;
    /** The {@code Control} for the date input */
    private final DatePicker datePicker;
    /** The {@code Control} for the time input */
    private final TimeTextField timeField;
    /** The time zone the entered date and time relate to */
    private final ZoneId zoneId;
    
    /**
     * Creates a new {@code DateTimePane}.
     *
     * @param zoneId
     *         the time zone the entered date and time relate to*
     */
    public DateTimePane(ZoneId zoneId) {
        this.zoneId = Objects.requireNonNull(zoneId);
        timeField = new TimeTextField();
        add(timeField, 1, 1);
        datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());
        add(datePicker, 2, 1);
        setPrefWidth(PREFERRED_WITH);
    }
    
    /**
     * Sets the content (date and time).
     *
     * @param toSet
     *         the date and time to set in the {@code Controls}
     */
    public void setValue(Instant toSet) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(toSet, zoneId);
        timeField.setValue(zdt.toLocalTime());
        datePicker.setValue(zdt.toLocalDate());
    }
    
    /**
     * Returns the content (date and time).
     *
     * @return the date and time hold be the {@code Controls}.
     */
    public Optional<Instant> getValue() {
        LocalDate date = datePicker.getValue();
        Optional<LocalTime> time = timeField.getValue();
        return date != null && time.isPresent() ? Optional.of(
                ZonedDateTime.of(date, time.get(), zoneId).toInstant()) : Optional.empty();
    }
}
