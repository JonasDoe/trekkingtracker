package trekkingtracker.event.participantevents;

import trekkingtracker.event.publishing.EventHandler;

/** Indicates that the implementing class is interested in participants and relating updates. */
public interface ParticipantProcessor extends EventHandler<ParticipantEvent> {
    
    @Override
    default void handle(ParticipantEvent event) {
        if (event instanceof ParticipantInputChangedEvent) setInput((ParticipantInputChangedEvent) event);
        else if (event instanceof ParticipantUpdateEvent) updatePerson((ParticipantUpdateEvent) event);
    }
    
    /**
     * Sets the internal state which serves for displaying information.
     *
     * @param inputEvent
     *         the underlying data of all participants
     */
    void setInput(ParticipantInputChangedEvent inputEvent);
    
    /**
     * Updates the internal state and adjusts the displayed information based on the given update.
     *
     * @param updateEvent
     *         holds information about the participant (state) that was replaced and the replacement (state)
     */
    void updatePerson(ParticipantUpdateEvent updateEvent);
}
