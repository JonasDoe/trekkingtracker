package trekkingtracker.event.requestevents;

/** Requests the intialization/restoring of the persisted data. */
public class ParticipantsInitRequest implements ParticipantEventRequest {
    /** Instance of this request */
    public static final ParticipantsInitRequest INSTANCE = new ParticipantsInitRequest();
}
