package trekkingtracker.event.requestevents;

import trekkingtracker.event.publishing.EventHandler;

/**
 * Marker interface for all participant-related validation and persistence requests being propagated in this
 * application as events.
 */
public interface ParticipantOperator extends EventHandler<ParticipantEventRequest> {}
