package trekkingtracker.event.requestevents;

import trekkingtracker.data.Person;
import trekkingtracker.event.participantevents.ParticipantInputChangedEvent;

import java.util.Collection;
import java.util.Objects;

/** Request for change of the input */
public class ParticipantInputChangeRequest implements ParticipantEventRequest {
    /** The requested event, for delegation */
    private final ParticipantInputChangedEvent asEvent;
    
    /**
     * Creates a new {@code ParticipantInputChangeRequest}.
     *
     * @param input
     *         the new input - it won't be propagated directly but as a unmodifiable version. Can be {@code null},
     *         for example to indicate the initialization has failed.
     */
    public ParticipantInputChangeRequest(Collection<Person> input) {
        asEvent = new ParticipantInputChangedEvent(input);
    }
    
    /**
     * Returns the requested event.
     *
     * @return the requested event
     */
    public ParticipantInputChangedEvent asEvent() {
        return asEvent;
    }
    
    /**
     * Returns the new data model.
     *
     * @return the new data model, can be {@code null}
     */
    public Collection<Person> getInput() {
        return asEvent.getInput();
    }
    
    @Override
    public String toString() {
        int size = asEvent.getInput() != null ? asEvent.getInput().size() : 0;
        return "Input changed: " + size + " persons";
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ParticipantInputChangeRequest)) return false;
        final ParticipantInputChangeRequest that = (ParticipantInputChangeRequest) o;
        return Objects.equals(asEvent, that.asEvent);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(asEvent);
    }
}
