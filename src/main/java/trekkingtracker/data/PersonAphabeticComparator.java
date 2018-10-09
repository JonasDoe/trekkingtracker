package trekkingtracker.data;

import java.util.Comparator;

/** Compares two {@code Persons} by their name, birthday and trekking category. */
public class PersonAphabeticComparator implements Comparator<Person> {
    /** Static instance of this {@code PersonAphabeticComparator} class. */
    public static final PersonAphabeticComparator INSTANCE = new PersonAphabeticComparator();
    
    @Override
    public int compare(final Person p1, final Person p2) {
        if (p1.equals(p2)) return 0;
        int nameComp = p1.getName().compareToIgnoreCase(p2.getName());
        if (nameComp != 0) return nameComp;
        int bdComp = p1.getBirthday().compareTo(p2.getBirthday());
        if (bdComp != 0) return bdComp;
        return p1.getCategory().compareTo(p2.getCategory());
    }
}
