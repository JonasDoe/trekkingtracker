package trekkingtracker.persistence;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {
    
    @Test
    void readCsv() throws Exception {
        String testFile = getClass().getClassLoader().getResource("with_comment_lines.csv").getFile();
        /* file's content:
         * #header
         * 1;2;3
         * #3;4;5
         * 5;6;7
         */
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(testFile)))) {
            Iterable<String> lines = () -> FileUtils.lineIterator(reader);
            String read = StreamSupport.stream(lines.spliterator(), false).collect(Collectors.joining("\n"));
            assertEquals("1;2;3\n5;6;7", read);
        }
    }
    
    @Test
    public void validateName() {
        assertTrue(FileUtils.isValid("Anton Meier"));
        assertTrue(FileUtils.isValid("Anton \"AM\" Meier"));
        assertTrue(FileUtils.isValid("Meier, Anton"));
        assertFalse(FileUtils.isValid("Meier; Anton"));
        assertFalse(FileUtils.isValid("Meier\nAnton"));
    }
}
