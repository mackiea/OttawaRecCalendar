import java.net.URL;
import java.time.DayOfWeek;

/**
 * A simple calendar for testing and fixing bugs.
 */
public class DebugCalendar extends BaseCalendar {
    public DebugCalendar() {
    }
    @Override
    void deleteAllEvents() {

    }

    @Override
    void createWeeklyEvent(DayOfWeek dayOfWeek, Time start, Time end, String title, String location, URL url) {

    }
}
