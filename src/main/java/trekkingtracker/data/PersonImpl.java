package trekkingtracker.data;

import trekkingtracker.persistence.FileUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Default, mutable implementation a participant.
 */
public class PersonImpl implements Person {
    
    /** The name */
    private final String name;
    /** The {@code Person's} birthday */
    private final LocalDate birthday;
    /** The trekking category */
    private Category category;
    /** The number the participant got when starting */
    private Integer number;
    /** The start time */
    private Instant start;
    /** The time of arrival, i.e. when the {@code Person} finished its trip */
    private Instant stop;
    
    /** {@code true} if the {@code Person} counts as finisher, otherwise {@code false} */
    private boolean finisher = false;
    
    /**
     * Creates a new {@code Person}.
     *
     * @param name
     *         the name
     * @param birthday
     *         the {@code Person's} birthday
     * @throws IllegalArgumentException
     *         if the name contains invalid characters (@see FileUtils#isValid(String))
     */
    public PersonImpl(final String name, final LocalDate birthday) {
        //TODO validity check does not really belong here
        if (!FileUtils.isValid(name)) throw new IllegalArgumentException(name + " contains invalid characters.");
        this.name = name;
        this.birthday = Objects.requireNonNull(birthday);
    }
    
    /**
     * Copy constructor for {@code Person}.
     *
     * @param toCopy
     *         whose field values shall be copied
     */
    public PersonImpl(Person toCopy) {
        this.name = toCopy.getName();
        this.birthday = toCopy.getBirthday();
        this.category = toCopy.getCategory();
        this.number = toCopy.getNumber();
        this.start = toCopy.getStart();
        this.stop = toCopy.getStop();
        this.finisher = toCopy.isFinisher();
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public LocalDate getBirthday() {
        return birthday;
    }
    
    @Override
    public Category getCategory() {
        return category;
    }
    
    @Override
    public Integer getNumber() {
        return number;
    }
    
    @Override
    public Instant getStart() {
        return start;
    }
    
    @Override
    public Instant getStop() {
        return stop;
    }
    
    @Override
    public boolean isFinisher() {
        return (start != null && stop != null) && finisher;
    }
    
    @Override
    public Optional<Duration> getTripTime() {
        return isFinisher() ? Optional.of(
                Duration.ofMillis(stop.toEpochMilli() - start.toEpochMilli())) : Optional.empty();
    }
    
    /**
     * Set the {@code Category} of the trip.
     *
     * @param category
     *         the {@code Category} of the trip
     */
    public void setCategory(Category category) {
        this.category = category;
    }
    
    /**
     * Sets the {@code Person's} starting number
     *
     * @param number
     *         the starting number to be set
     */
    public void setNumber(final int number) {
        this.number = number;
    }
    
    /**
     * Sets the {@code Person's} starting time.
     *
     * @param start
     *         the starting time to be set
     */
    public void setStart(final Instant start) {
        this.start = start;
    }
    
    /**
     * Sets the time the {@code Person} finished its trip.
     *
     * @param stop
     *         the time of arrival to be set
     */
    public void setStop(final Instant stop) {
        this.stop = stop;
    }
    
    /**
     * Sets whether the {@code Person} has finished or not.
     *
     * @param finisher
     *         {@code true} if the {@code Person} counts as finisher, otherwise {@code false}. Note that
     *         {@link #isFinisher()} will return {@code false} even when this value is set to {@code true} as long as
     *         {@link #start} or {@link #stop} are not set.
     */
    public void setFinished(final boolean finisher) {
        this.finisher = finisher;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o instanceof Person) {
            final Person person = (Person) o;
            return Objects.equals(name, person.getName()) && Objects.equals(birthday, person.getBirthday());
        } else return false;
    }
    
    @Override
    public boolean deepEquals(final Person p) {
        return equals(p) && Objects.equals(number, p.getNumber()) && Objects.equals(start,
                p.getStart()) && Objects.equals(stop, p.getStop()) && Objects.equals(category,
                p.getCategory()) && Objects.equals(number, p.getNumber()) && Objects.equals(isFinisher(),
                p.isFinisher()) && Objects.equals(getTripTime(), p.getTripTime());
    }
}

