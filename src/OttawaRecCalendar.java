import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

public class OttawaRecCalendar {

    final String siteUrl = "https://ottawa.ca";

    void processDayOfWeek(Element pool, BaseCalendar calendar, DayOfWeek dayOfWeek, Element record, String location, URL url) throws IOException {
        Elements tableHeader = record.getElementsByTag("th");
        if(tableHeader.isEmpty()) {
            return;
        }
        String activity = tableHeader.get(0).text();
        Elements times = record.getElementsByTag("td");
        String timeBlocks = times.get(dayOfWeek.getValue() - DayOfWeek.MONDAY.getValue()).text();
        BaseCalendar.log("Parsing timeblocks: " + timeBlocks);
        timeBlocks = timeBlocks.toLowerCase().replaceAll("noon", "12 pm");
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

            BaseCalendar.Time start = new BaseCalendar.Time(startEnd[0]);
            BaseCalendar.Time end = new BaseCalendar.Time(startEnd[1]);
            if(end.hour < start.hour && start.hour > 12) {
                start.hour -= 12;
            }

            calendar.createWeeklyEvent(dayOfWeek, start, end, activity, location, url);
            return;
        }
    }

    boolean processPool(Element pool, BaseCalendar calendar) {
        boolean worked = false;
        try {
            Elements addresses = pool.getElementsByTag("a");
            if (addresses.isEmpty()) {
                return false;
            }
            URL url = new URL(siteUrl + addresses.get(0).attr("href"));
            Document poolPage = Jsoup.connect(url.toString()).get();
            BaseCalendar.log("------------------------------------------------------------------");
            String location = poolPage.getElementsByAttributeValue("name", "dcterms.title").get(0).attr("content");
            BaseCalendar.log("-----------------------------" + location + "-------------------------------------");

            Elements scheduleTables = poolPage.getElementsByTag("table");
            for (Element scheduleTable : scheduleTables) {
                String caption = scheduleTable.getElementsByTag("caption").get(0).text();
                if(!caption.contains("swim") && !caption.contains("pool") && !caption.contains("skat")) {
                    continue;
                }
                Elements records = scheduleTable.getElementsByTag("tbody").get(0).getElementsByTag("tr");
                for (Element record : records) {
                    for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                        processDayOfWeek(pool, calendar, dayOfWeek, record, location, url);
                    }
                    worked = true;
                }
            }

        } catch (IOException e) {
            BaseCalendar.log(e);
        }
        return worked;
    }

    void go() {
        try {
            BaseCalendar calendar = new GoogleCalendar();
            BaseCalendar.log("Ottawa Rec Calendar!!");
            calendar.deleteAllEvents();
            for(int page = 0;page<10;page++) {
                Document poolListPage = Jsoup.connect(siteUrl + "/en/recreation-and-parks/recreation-facilities/place-listing?text=&place_facets%5B0%5D=place_type%3A4285&page=" + page).get();
                Element table = poolListPage.body().getElementsByClass("table table-bordered table-condensed cols-2").get(0);
                Elements pools = table.getElementsByClass("views-field views-field-title");
                for (Element pool : pools) {
                    if(!processPool(pool, calendar)) {
                        BaseCalendar.log("Failed");
                    }
                }
                Elements nextPage = poolListPage.body().getElementsByClass("pager__item--next");
                if(nextPage.isEmpty()) {
                    break;
                }
            }
        } catch (IOException | GeneralSecurityException e) {
            BaseCalendar.log(e);
        }
    }

    public static void main(String[] args) {
        OttawaRecCalendar orc = new OttawaRecCalendar();
        orc.go();
    }

}