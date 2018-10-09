package trekkingtracker;

import trekkingtracker.data.Person;
import trekkingtracker.data.PersonAphabeticComparator;
import trekkingtracker.ui.MainApp;

import java.util.*;

/** Hold general util methods */
public final class Utils {
    /** Pure util class, not intended to be instantiated */
    private Utils() {
    }
    
    /**
     * Creates a {@code Map} from start number to participant and reports all duplicates.
     *
     * @param participants
     *         the {@code Map} shall be created for
     * @return the {@code Map} from start number to participant
     */
    public static Map<Integer, Person> getStartNumberToParticipantMap(Collection<Person> participants) {
        Map<Integer, Person> startNumbersToPersons = new HashMap<>(participants.size());
        for (Person participant : participants) {
            Integer number = participant.getNumber();
            if (number != null) {
                Person alreadyExisting = startNumbersToPersons.computeIfAbsent(number, x -> participant);
                if (!alreadyExisting.equals(participant)) MainApp.printInfo(
                        String.format("Starting number %d is in use by %s and %s. Please fix that immediately!",
                                alreadyExisting.getNumber(), participant, alreadyExisting));
            }
        }
        return startNumbersToPersons;
    }
    
    /**
     * Creates a view containing the latest state of all given participants.
     *
     * @param participants
     *         who shall be callapsed to their latest states
     * @return the created view
     */
    public static Set<Person> getLatestStateView(Collection<Person> participants) {
        Set<Person> participantsView = new TreeSet<>(PersonAphabeticComparator.INSTANCE);
        for (Person participant : participants) {
            participantsView.remove(participant);
            participantsView.add(participant);
        }
        return Collections.unmodifiableSet(participantsView);
    }
}
