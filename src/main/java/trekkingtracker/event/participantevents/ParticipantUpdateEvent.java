package trekkingtracker.event.participantevents;

import trekkingtracker.data.Person;

import java.util.Objects;

/** Holds an update about a single participant */
public class ParticipantUpdateEvent implements ParticipantEvent {
    /** The previous state */
    private final Person newValue;
    /** The new state */
    private final Person oldValue;
    
    /**
     * Creates a new {@code ParticipantUpdateEvent}.
     *
     * @param oldValue
     *         the previous state of the participant, or {@code null} if there was no previous one (i.e. the
     *         participant was newly created)
     * @param newValue
     *         the the current state of the participant, or {@code null} if there was no current one anymore (i.e.
     *         the participant was deleted)
     * @throws IllegalArgumentException
     *         if the old and new state must relate to two different participants
     */
    public ParticipantUpdateEvent(Person oldValue, Person newValue) {
        if (oldValue != null && newValue != null && !oldValue.equals(newValue)) throw new IllegalArgumentException(
                "Old and new state must relate to the equal participant.");
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    
    /**
     * Returns the old state of the participant.
     *
     * @return the old state, can be {@code null}
     */
    public Person getOldValue() {
        return oldValue;
    }
    
    /**
     * Returns the new state of the participant.
     *
     * @return the new state, can be {@code null}
     */
    public Person getNewValue() {
        return newValue;
    }
    
    @Override
    public String toString() {
        return "Update " + oldValue + "->" + newValue;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ParticipantUpdateEvent)) return false;
        final ParticipantUpdateEvent that = (ParticipantUpdateEvent) o;
        return Objects.equals(oldValue, that.oldValue) && Objects.equals(newValue, that.newValue);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(oldValue, newValue);
    }
}
