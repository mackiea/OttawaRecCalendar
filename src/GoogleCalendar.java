import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.time.DayOfWeek;
import java.util.Collections;
import java.util.List;

/* class to demonstrate use of Calendar events list API */
public class GoogleCalendar extends BaseCalendar {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES =
            Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = GoogleCalendar.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    String calendarId = "6662daf58b5217313682dde286e8bec3c34b288e5c10e9485e66e02ca6e3946d@group.calendar.google.com";

    Calendar service;

    @Override
    void deleteAllEvents() throws IOException {
        List<Event> items;
        do {
            Events events = service.events().list(calendarId)
                    .setTimeMin(new DateTime(0))
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            items = events.getItems();
            if (items.isEmpty()) {
                System.out.println("No upcoming events found.");
            } else {
                System.out.println("Zappable events");
                for (Event event : items) {
                    service.events().delete(calendarId, event.getId()).execute();
                    DateTime start = event.getStart().getDateTime();
                    if (start == null) {
                        start = event.getStart().getDate();
                    }
                    System.out.printf("%s (%s)\n", event.getSummary(), start);
                }
            }
        } while (!items.isEmpty());
    }


    @Override
    void createWeeklyEvent(DayOfWeek javaDayOfWeek, Time start, Time end, String title, String location, URL url) throws IOException {
        log(javaDayOfWeek + "|" + start + "|" + end + "|" + title + "|" + location + "|" + url);
        Event newEvent = new Event()
                .setSummary(title + ": " + location)
                .setLocation(location)
                .setDescription(url.toString() + ": " + getJavaDayOfWeek(javaDayOfWeek) + " @ " + start + "-" + end)
                .setRecurrence(Collections.singletonList("RRULE:FREQ=WEEKLY;UNTIL=20240101T000000Z"));
        DateTime startDt = new DateTime(getPreviousDateByDayOfWeek(javaDayOfWeek, start));
        newEvent.setStart(new EventDateTime().setDateTime(startDt).setTimeZone("Canada/Eastern"));

        DateTime endDt = new DateTime(getPreviousDateByDayOfWeek(javaDayOfWeek, end));
        newEvent.setEnd(new EventDateTime().setDateTime(endDt).setTimeZone("Canada/Eastern"));
        BaseCalendar.log("!!!" + startDt + "!!!" + endDt);

        service.events().insert(calendarId, newEvent).execute();
    }

    GoogleCalendar() throws GeneralSecurityException, IOException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        service =
                new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        GoogleCalendar gc = new GoogleCalendar();
        gc.deleteAllEvents();
        /*
        gc.createWeeklyEvent(
                DayOfWeek.TUESDAY,
                new Time(3, 0),
                new Time(12, 20),
                "MrTuesday",
                "MrLocation",
                new URL("http://mrUrl.com")
        );
        gc.createWeeklyEvent(
                DayOfWeek.SUNDAY,
                new Time(3, 0),
                new Time(12, 20),
                "MrSunday",
                "MrLocation",
                new URL("http://mrUrl.com")
        );
        gc.createWeeklyEvent(
                DayOfWeek.SATURDAY,
                new Time(3, 0),
                new Time(12, 20),
                "MrMonday",
                "MrLocation",
                new URL("http://mrUrl.com")
        );
         */

    }

}