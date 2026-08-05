package mack7400.test;

import mack7400.code.BaseCalendar;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.net.URL;
import java.time.DayOfWeek;
import java.util.stream.Stream;

public class BaseCalendarTest {
    static class CalendarImplementation extends BaseCalendar {

        @Override
        protected void deleteAllEvents() {
        }

        @Override
        public void createWeeklyEvent(DayOfWeek dayOfWeek, BaseCalendar.Time start, BaseCalendar.Time end, String title, String location, URL url) {
        }
    }

    @Test
    void constructor() {
        new CalendarImplementation();
    }

    private static Stream<Arguments> dayOfWeekSource() {
        return Stream.of(
                Arguments.of(DayOfWeek.SUNDAY, "Sunday"),
                Arguments.of(DayOfWeek.MONDAY, "Monday"),
                Arguments.of(DayOfWeek.TUESDAY, "Tuesday"),
                Arguments.of(DayOfWeek.WEDNESDAY, "Wednesday"),
                Arguments.of(DayOfWeek.THURSDAY, "Thursday"),
                Arguments.of(DayOfWeek.FRIDAY, "Friday"),
                Arguments.of(DayOfWeek.SATURDAY, "Saturday")
        );
    }

    @ParameterizedTest
    @MethodSource("dayOfWeekSource")
    void getJavaDayOfWeek(DayOfWeek dayofWeek, String expected) {
        Assertions.assertEquals(expected, new CalendarImplementation().getJavaDayOfWeek(dayofWeek));
    }
}

