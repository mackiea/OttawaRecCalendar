import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

/**
 * Basic implementation-agnostic calendar superclass.
 */
public abstract class BaseCalendar {
    public BaseCalendar() {
    }

    String getJavaDayOfWeek(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case SUNDAY -> "Sunday";
            case MONDAY -> "Monday";
            case TUESDAY -> "Tuesday";
            case WEDNESDAY -> "Wednesday";
            case THURSDAY -> "Thursday";
            case FRIDAY -> "Friday";
            case SATURDAY -> "Saturday";
        };
    }

    static class Time {
        int hour;
        int minute;

        Time(String time) {
            int offset = time.contains("pm") ? 12 : 0;
            time = time.replace("pm", "").replace("am", "").replace((char) 8239, ' ').trim();
            time = time.replace(" h", "");
            if (time.contains(":")) {
                String[] hourmin = time.split(":");
                hour = Integer.parseInt(hourmin[0]);
                minute = Integer.parseInt(hourmin[1]);
            } else if (time.contains(";")) {    // A typo!
                String[] hourmin = time.split(";");
                hour = Integer.parseInt(hourmin[0]);
                minute = Integer.parseInt(hourmin[1]);
            } else {
                hour = Integer.parseInt(time);
                minute = 0;
            }
            hour = (hour == 12) ? offset : hour + offset;
        }

        @Override
        public String toString() {
            if (minute <= 9) {
                return hour + ":0" + minute;
            } else {
                return hour + ":" + minute;
            }
        }

    }

    Date getPreviousDateByDayOfWeek(DayOfWeek dayOfWeek, Time time) {
        LocalDate now = LocalDate.now(); // Today, now
        int offset = dayOfWeek.getValue() - now.getDayOfWeek().getValue();
        LocalDate then = now.plusDays(offset);
        if (then.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            then = then.minusWeeks(1);
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, then.getDayOfYear());
        cal.set(Calendar.HOUR_OF_DAY, time.hour);
        cal.set(Calendar.MINUTE, time.minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Empties the calendar.
     * @throws IOException if a problem occurs.
     */
    abstract void deleteAllEvents() throws IOException;

    /**
     * Creates a weekly, eternal event.
     * @param dayOfWeek The weekday the event runs.
     * @param start The start time.
     * @param end The end time.
     * @param title The name.
     * @param location Where the event is being held.
     * @param url A link to the website the event is created from, for manual verification adn context.
     * @throws IOException if a problem occurs.
     */
    abstract void createWeeklyEvent(DayOfWeek dayOfWeek, Time start, Time end, String title, String location, URL url) throws IOException;

    static void log(Object o) {
        System.out.println(o.toString());
    }
}
