package trekkingtracker.data;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Represents a participant. Use this interface to prevent modifications.
 */
public interface Person {
    /** The walking category this {@code Person} take part in */
    enum Category {
        /** The short route */
        TOUR,
        /** The middle route */
        DOGHIKE,
        /** The long route */
        DOGTREKKING,
        /** Route for handicapped participants */
        BARRIER_FREE,
        /** Other or unknown route */
        UNKNOWN
    }
    
    /**
     * Returns the name
     *
     * @return the name
     */
    String getName();
    
    /**
     * Returns the {@code Person's} birthay.
     *
     * @return the {@code Person's} birthay
     */
    LocalDate getBirthday();
    
    /**
     * Returns the trip category this {@code Person} take part in.
     *
     * @return the trip category this {@code Person} take part in
     */
    Category getCategory();
    
    /**
     * Returns the {@code Person's} starting number.
     *
     * @return the {@code Person's} starting number
     */
    Integer getNumber();
    
    /**
     * Returns the {@code Person's} starting time.
     *
     * @return the {@code Person's} starting time
     */
    Instant getStart();
    
    /**
     * Returns the time the {@code Person} finished its trip.
     *
     * @return the time the {@code Person} finiehsd its trip
     */
    Instant getStop();
    
    /**
     * Tells whether the {@code Person} has finished or not.
     *
     * @return {@code true} if the {@code Person} counts as finisher, otherwise {@code false}
     */
    boolean isFinisher();
    
    /**
     * Return the time the {@code Person} took to finish its trip.
     *
     * @return the time taken, in case there's a start and a stop
     */
    Optional<Duration> getTripTime();
    
    /**
     * Tells whether the given {@code Person} equals this one in all aspects
     *
     * @param p
     *         to be compare with this {@code Person}
     * @return {@code true} if the given {@code Person} equals this one in all aspects, otherwise {@code false}
     */
    boolean deepEquals(final Person p);
    
}

