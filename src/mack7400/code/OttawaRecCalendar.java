package mack7400.code;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.time.DayOfWeek;

/**
 * Creates a set of Google calendar events from the event schedule of all known Ottawa pools.
 * This is an attempt to solve the problem of which facility has the event I want to attend.
 * The current rec calendars are by venue only, so while it's easy to see what events are offered at what venue,
 * it's difficult if, say, I want to know the next place that has a pool time coming up.
 */
public class OttawaRecCalendar {

    static final String siteUrl = "https://ottawa.ca";

    /**
     * Attempts to parse a "day" column's info.
     * @param calendar The calendar to update.
     * @param dayOfWeek The day of the week to parse.
     * @param record The table containing the day-of-week column.
     * @param location The venue name.
     * @param url The web address being parsed, to add to any events created.
     * @throws IOException if a problem occurs.
     */
    void processDayOfWeek(BaseCalendar calendar, DayOfWeek dayOfWeek, Element record, String location, URL url) throws IOException {
        Elements tableHeader = record.getElementsByTag("th");
        if(tableHeader.isEmpty()) {
            return;
        }
        String activity = tableHeader.get(0).text();
        Elements times = record.getElementsByTag("td");
        String timeBlocks = times.get(dayOfWeek.getValue() - DayOfWeek.MONDAY.getValue()).text();
        BaseCalendar.log("Parsing timeblocks: " + timeBlocks);
        timeBlocks = timeBlocks.toLowerCase().replace("noon", "12 pm");
        String[] blocks = timeBlocks.split(",");
        for (String block : blocks) {
            BaseCalendar.log(block);
            if (!block.contains("-")) {
                BaseCalendar.log("Nope: " + block);
                continue;
            }
            String[] startEnd = block.split("-");

            if (!startEnd[0].contains("m")) {
                startEnd[0] = startEnd[0] + (startEnd[1].contains("am") ? "am" : "pm");
            }

            // Remove anything tailing past the "am"/"pm" text.
            startEnd[0] = startEnd[0].substring(0, startEnd[0].indexOf('m') + 1);
            startEnd[1] = startEnd[1].substring(0, startEnd[1].indexOf('m') + 1);

            BaseCalendar.Time start = new BaseCalendar.Time(startEnd[0]);
            BaseCalendar.Time end = new BaseCalendar.Time(startEnd[1]);
            if(end.hour < start.hour && start.hour > 12) {
                start.hour -= 12;
            }

            calendar.createWeeklyEvent(dayOfWeek, start, end, activity, location, url);
            return;
        }
    }

    /**
     * Reads all the pool-related events at this venue.
     * @param pool The pool ebelement.
     * @param calendar The calendar to update.
     * @return True if events were logged; false otherwise.
     */
    boolean processPool(Element pool, BaseCalendar calendar, Connector connector) {
        boolean worked = false;
        try {
            Connector.UrlDoc poolPage = connector.getPoolInfo(pool);
            if(poolPage == null) {
                return false;
            }
            BaseCalendar.log("------------------------------------------------------------------");
            Elements locations = poolPage.document.getElementsByAttributeValue("name", "dcterms.title");
            if(locations.isEmpty()) {
                return false;
            }
            String location = locations.get(0).attr("content");
            BaseCalendar.log("-----------------------------" + location + "-------------------------------------");

            Elements scheduleTables = poolPage.document.getElementsByTag("table");
            for (Element scheduleTable : scheduleTables) {
                String caption = scheduleTable.getElementsByTag("caption").get(0).text().toLowerCase();
                // Currently only supports pool events.
                if(!caption.contains("swim") && !caption.contains("pool") && !caption.contains("skat")) {
                    continue;
                }
                Elements records = scheduleTable.getElementsByTag("tbody").get(0).getElementsByTag("tr");
                for (Element record : records) {
                    for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                        processDayOfWeek(calendar, dayOfWeek, record, location, poolPage.url);
                    }
                    worked = true;
                }
            }

        } catch (IOException e) {
            BaseCalendar.log(e);
        }
        return worked;
    }



    public static class Connector {
        @AllArgsConstructor(access = AccessLevel.PUBLIC)
        public static class UrlDoc {
            @NonNull URL url;
            @NonNull Document document;
        }

        public Element getPoolList(int page) throws IOException {
            // String uri = OttawaRecCalendar.siteUrl + "/en/recreation-and-parks/recreation-facilities/place-listing?text=&place_facets%5B0%5D=place_type%3A4285&page=" + page;
            String uri = "https://ottawa.ca/en/recreation-and-parks/facilities/place-listing?place_facets[0]=place_type%3A4219&place_facets[1]=place_type%3A4285&place_facets[2]=place_type%3A2235821&text=&page=" + page;
            BaseCalendar.log("Connecting to " + uri);
            return Jsoup.connect(uri).get().body();
        }

        public UrlDoc getPoolInfo(Element pool) throws IOException {
            Elements addresses = pool.getElementsByTag("a");
            if (addresses.isEmpty()) {
                BaseCalendar.log("Found no address elements.");
                return null;
            }
            URL url = new URL(OttawaRecCalendar.siteUrl + addresses.get(0).attr("href"));
            return new UrlDoc(url, Jsoup.connect(url.toString()).get());
        }
    }

    static final public String docPoolListPath = "table table-bordered table-condensed cols-2";
    static final public String docPoolPath = "views-field views-field-title";
    static final public String docNextPagePath = "pager__item--next";

    public void go(BaseCalendar calendar, Connector connector) {
        try {
            BaseCalendar.log("Ottawa Rec Calendar!!");
            calendar.deleteAllEvents();
            // Scrapes all the venues at the given address.
            for(int page = 0;page<10;page++) {
                Element poolListPage = connector.getPoolList(page);
                Elements tables = poolListPage.getElementsByClass(docPoolListPath);
                if(tables.isEmpty()) {
                    BaseCalendar.log("No events found:");
                    BaseCalendar.log(poolListPage);
                    return;
                }
                Element table = tables.get(0);
                Elements pools = table.getElementsByClass(docPoolPath);
                for (Element pool : pools) {
                    if(!processPool(pool, calendar, connector)) {
                        BaseCalendar.log("Failed");
                    }
                }
                Elements nextPage = poolListPage.getElementsByClass(docNextPagePath);
                if(nextPage.isEmpty()) {
                    break;
                }
            }
        } catch (IOException e) {
            BaseCalendar.log(e);
        }
    }

    public static void main(String[] args) {
        try {
            BaseCalendar calendar = new GoogleCalendar();
            OttawaRecCalendar orc = new OttawaRecCalendar();
            orc.go(calendar, new Connector());
        } catch(GeneralSecurityException | IOException e) {
            BaseCalendar.log(e);
        }
    }

}