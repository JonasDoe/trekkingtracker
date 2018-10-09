package trekkingtracker.event.requestevents;

import trekkingtracker.data.Person;
import trekkingtracker.event.participantevents.ParticipantUpdateEvent;

import java.util.Objects;

/** Request for an participant update */
public class ParticipantUpdateRequest implements ParticipantEventRequest {
    /** The requested event, for delegation */
    private final ParticipantUpdateEvent asEvent;
    
    /**
     * Creates a new {@code ParticipantUpdateRequest}.
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
    public ParticipantUpdateRequest(Person oldValue, Person newValue) {
        asEvent = new ParticipantUpdateEvent(oldValue, newValue);
    }
    
    /**
     * Returns the requested event.
     *
     * @return the requested event
     */
    public ParticipantUpdateEvent asEvent() {
        return new ParticipantUpdateEvent(getOldValue(), getNewValue());
    }
    
    /**
     * Returns the old state of the participant.
     *
     * @return the old state, can be {@code null}
     */
    public Person getOldValue() {
        return asEvent.getOldValue();
    }
    
    /**
     * Returns the new state of the participant.
     *
     * @return the new state, can be {@code null}
     */
    public Person getNewValue() {
        return asEvent.getNewValue();
    }
    
    @Override
    public String toString() {
        return "Update ParticipantEventRequest " + getOldValue() + "->" + getNewValue();
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ParticipantUpdateRequest)) return false;
        final ParticipantUpdateRequest that = (ParticipantUpdateRequest) o;
        return Objects.equals(asEvent, that.asEvent);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(asEvent);
    }
}
