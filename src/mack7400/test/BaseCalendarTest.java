package mack7400.test;

import mack7400.code.BaseCalendar;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.Date;
import java.util.GregorianCalendar;
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
    void getJavaDayOfWeek(DayOfWeek dayOfWeek, String expected) {
        Assertions.assertEquals(expected, new CalendarImplementation().getJavaDayOfWeek(dayOfWeek));
    }

    private static Stream<Arguments> getPreviousDateByDayOfWeekSource() {
        return Stream.of(
                Arguments.of(
                        DayOfWeek.SUNDAY,
                        new BaseCalendar.Time(10, 0),
                        new GregorianCalendar(2026, GregorianCalendar.AUGUST, 2, 10, 0).getTime()
                ),
                Arguments.of(
                        DayOfWeek.MONDAY,
                        new BaseCalendar.Time(10, 0),
                        new GregorianCalendar(2026, GregorianCalendar.AUGUST, 3, 10, 0).getTime()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getPreviousDateByDayOfWeekSource")
    void getPreviousDateByDayOfWeekTest(DayOfWeek dayOfWeek, BaseCalendar.Time time, Date expected) {
        LocalDate now = LocalDate.of(2026, Month.AUGUST, 5);
        Assertions.assertEquals(expected, new CalendarImplementation().getPreviousDateByDayOfWeek(dayOfWeek, time, now));
    }
}

