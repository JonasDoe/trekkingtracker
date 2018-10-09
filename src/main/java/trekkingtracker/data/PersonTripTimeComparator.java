package trekkingtracker.data;

import java.time.Duration;
import java.util.Comparator;

/**
 * Compares two {@code Persons} by their {@code Category} and the time they took for their trip, birthday and
 * trekking category.
 */
public class PersonTripTimeComparator implements Comparator<Person> {
    /** Static instance of this {@code PersonAphabeticComparator} class. */
    public static final PersonTripTimeComparator INSTANCE = new PersonTripTimeComparator();
    /** {@code null} robust comparison of the trip times, meaning finisher will come first */
    private static final Comparator<Duration> TIME_COMPARATOR = Comparator.nullsLast(Duration::compareTo);
    
    @Override
    public int compare(final Person p1, final Person p2) {
        if (p1.equals(p2)) return 0;
        
        int catComp = p1.getCategory().compareTo(p2.getCategory());
        if (catComp != 0) return catComp;
        
        Duration p1TripTime = p1.getTripTime().orElse(null);
        Duration p2TripTime = p2.getTripTime().orElse(null);
        int tripComp = TIME_COMPARATOR.compare(p1TripTime, p2TripTime);
        if (tripComp != 0) return tripComp;
        
        int nameComp = p1.getName().compareTo(p2.getName());
        if (nameComp != 0) return nameComp;
        return p1.getBirthday().compareTo(p2.getBirthday());
    }
}
