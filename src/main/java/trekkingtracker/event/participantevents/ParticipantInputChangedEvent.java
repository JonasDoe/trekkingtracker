package trekkingtracker.event.participantevents;

import trekkingtracker.Utils;
import trekkingtracker.data.Person;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/** Event which represents the (re-)setting of the underlying data model */
public class ParticipantInputChangedEvent implements ParticipantEvent {
    /** The new data model to be propagated */
    private final Collection<Person> input;
    
    /**
     * Creates a new {@code ParticipantInputChangedEvent}.
     *
     * @param input
     *         the new input - it won't be propagated directly but as a unmodifiable version. Can be {@code null},
     *         for example to indicate the initialization has failed.
     */
    public ParticipantInputChangedEvent(Collection<Person> input) {
        this.input = input != null ? Collections.unmodifiableCollection(input) : null;
    }
    
    /**
     * Creates a view containing the latest state of all given participants.
     *
     * @return the created view with the latest state of all given participants, can be {@code null}
     */
    public Set<Person> getInputView() {
        return input != null ? Utils.getLatestStateView(input) : null;
    }
    
    /**
     * Returns the new data model.
     *
     * @return the new data model, can be {@code null}
     */
    public Collection<Person> getInput() {
        return input;
    }
    
    @Override
    public String toString() {
        int size = input != null ? input.size() : 0;
        return "Input changed: " + size + " persons";
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(input);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ParticipantInputChangedEvent)) return false;
        final ParticipantInputChangedEvent that = (ParticipantInputChangedEvent) o;
        return Objects.equals(input, that.input);
    }
}
