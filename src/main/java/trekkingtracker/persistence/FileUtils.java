package trekkingtracker.persistence;

import trekkingtracker.data.Person;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/** Hold IO-related utility methods */
public final class FileUtils {
    /** Characters which are not allowed for fields to be stored */
    private static final Pattern forbiddenChars = Pattern.compile("[;\\n]");
    
    /** Pure util class, not intended to be instantiated */
    private FileUtils() {
    }
    
    /**
     * Checks whether the given {@code String} does not contain any invalid characters.
     *
     * @param toValidate
     *         to be checked for any invalid letters
     * @return {@code true} if the given {@code String} does not contain any invalid characters, otherwise {@code false}
     */
    public static boolean isValid(String toValidate) {
        return sanitizeName(toValidate).equals(toValidate);
    }
    
    /**
     * Strips any invalid characters by the given {@code String}.
     *
     * @param toSanitize
     *         the {@code String} invalid characters shall be removed from
     * @return the input {@code String} without any invalid characters
     */
    public static String sanitizeName(String toSanitize) {
        return forbiddenChars.matcher(toSanitize).replaceAll("");
    }
    
    /**
     * Parses a {@code Category} from a {@code String}.
     *
     * @param categoryIdentifier
     *         to be parsed
     * @return the parsed {@code Category}
     */
    static Person.Category parseCategory(String categoryIdentifier) {
        String catLower = categoryIdentifier.toLowerCase();
        if (catLower.contains("tour")) return Person.Category.TOUR;
        else if (catLower.contains("roll") || catLower.contains("wheel") || catLower.contains("barrier"))
            return Person.Category.BARRIER_FREE;
        else if (catLower.contains("hike")) return Person.Category.DOGHIKE;
        else if (catLower.contains("trekking")) return Person.Category.DOGTREKKING;
        else return Person.Category.UNKNOWN;
    }
    
    /**
     * Creates an {@code Iterator} for the lines read by a given {@code BufferedReader}. Will ignore lines starting
     * with {@code #}.
     *
     * @param lineReader
     *         the readers which supplies the lines. Must be closed from outside.
     * @return the {@code Iterator} for the lines
     */
    static Iterator<String> lineIterator(final BufferedReader lineReader) {
        return new Iterator<String>() {
            
            /** The next line to be returned, or {@code null} if the next line is not calculated yet. */
            private String next = null;
            /** {@code true} if there are not further lines available, otherwise {@code false} */
            private boolean end = false;
            
            @Override
            public boolean hasNext() {
                computeNext();
                return !end;
            }
            
            @Override
            public String next() {
                computeNext();
                if (end) throw new NoSuchElementException();
                final String toReturn = next;
                next = null;
                return toReturn;
            }
            
            /** Calculates the next line to be returned (if required) and sets {@link #next}. */
            private void computeNext() {
                if (next != null) return;
                try {
                    while (true) {
                        next = lineReader.readLine();
                        if (next == null) {
                            end = true;
                            return;
                        } else if (!next.startsWith("#")) return;
                    }
                } catch (final IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }
}
