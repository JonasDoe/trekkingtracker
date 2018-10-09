package trekkingtracker.data;

import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersonTest {
    @Test
    void personTripTimeComparator() {
        PersonImpl p1 = createPerson("P1", Person.Category.DOGTREKKING);
        p1.setStart(createDate(2018, 1, 1, 2, 0));
        p1.setStop(plus(p1.getStart(), 10));
        p1.setFinished(true);
        PersonImpl p2 = createPerson("P2", Person.Category.DOGTREKKING);
        p2.setStart(createDate(2018, 1, 1, 4, 0));
        p2.setStop(plus(p2.getStart(), 9));
        p2.setFinished(true);
        PersonImpl p3 = createPerson("P3", Person.Category.DOGTREKKING);
        p3.setStart(createDate(2018, 1, 1, 2, 0));
        PersonImpl p4 = createPerson("P4", Person.Category.DOGTREKKING);
        p4.setStop(createDate(2018, 1, 1, 2, 0));
        p4.setFinished(true);
        PersonImpl p5 = createPerson("P5", Person.Category.DOGHIKE);
        p5.setStart(createDate(2018, 1, 1, 4, 0));
        p5.setStop(plus(p5.getStart(), 5));
        p5.setFinished(true);
        PersonImpl p6 = createPerson("P6", Person.Category.DOGHIKE);
        p6.setStart(createDate(2018, 1, 1, 5, 0));
        p6.setStop(plus(p6.getStart(), 7));
        p6.setFinished(true);
        List<Person> expected = Arrays.asList(p5, p6, p2, p1, p3, p4);
        List<Person> sorted = expected.stream().sorted(PersonTripTimeComparator.INSTANCE).collect(Collectors.toList());
        assertEquals(expected, sorted);
    }
    
    private static PersonImpl createPerson(String name, Person.Category category) {
        PersonImpl toReturn = new PersonImpl(name, LocalDate.now());
        toReturn.setCategory(category);
        return toReturn;
    }
    
    private static Instant createDate(int year, int month, int day, int hour, int minute) {
        return ZonedDateTime.of(LocalDateTime.of(year, month, day, hour, minute), ZoneId.of("CET")).toInstant();
    }
    
    private static Instant plus(Instant basis, int hous) {
        return ZonedDateTime.ofInstant(basis, ZoneId.of("CET")).plusHours(hous).toInstant();
    }
}
