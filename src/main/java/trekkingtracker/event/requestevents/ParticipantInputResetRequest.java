package trekkingtracker.event.requestevents;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/** Request to re-read the given start input */
public class ParticipantInputResetRequest implements ParticipantEventRequest {
    /** The file containing the new input */
    private final File startInput;
    /** The charset to parse the file with */
    private final Charset cs;
    
    /**
     * Creates a new {@code ParticipantInputChangeRequest}.
     *
     * @param startInput
     *         the file containing the new input
     * @param cs
     *         the charset to parse the input file with
     */
    public ParticipantInputResetRequest(File startInput, Charset cs) {
        this.startInput = requireNonNull(startInput);
        this.cs = requireNonNull(cs);
    }
    
    /**
     * Returns the file containing the new input.
     *
     * @return the file containing the new input
     */
    public File getStartInput() {
        return startInput;
    }
    
    /**
     * Returns the charset to parse the input file with.
     *
     * @return the charset to parse the input file with
     */
    public Charset getCharset() {
        return cs;
    }
    
    @Override
    public String toString() {
        return "Reset with " + startInput + "'s content";
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ParticipantInputResetRequest)) return false;
        final ParticipantInputResetRequest that = (ParticipantInputResetRequest) o;
        return Objects.equals(startInput, that.startInput) && Objects.equals(cs, that.cs);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(startInput, cs);
    }
}
