package mack7400.test;

import mack7400.code.BaseCalendar;
import mack7400.code.OttawaRecCalendar;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.util.stream.Stream;

public class OttawaRecCalendarTest {
    static Stream<Arguments> goParameters() {
        return Stream.of(
            Arguments.of("6-8 am", new BaseCalendar.Time(6, 0), new BaseCalendar.Time(8, 0)),
                Arguments.of("10 am - noon Play Free", new BaseCalendar.Time(10, 0), new BaseCalendar.Time(12, 0))
        );
    }


    @ParameterizedTest
    @MethodSource("goParameters")
    void goTest(String timeBlock, BaseCalendar.Time expectedStart, BaseCalendar.Time expectedEnd) throws IOException {
        OttawaRecCalendar orc = new OttawaRecCalendar();

        OttawaRecCalendar.Connector connector = Mockito.mock(OttawaRecCalendar.Connector.class);
        Element poolList = Mockito.mock(Element.class);
        Mockito.when(poolList.getElementsByClass(OttawaRecCalendar.docPoolListPath)).thenReturn(new Elements(poolList));
        Mockito.when(poolList.getElementsByClass(OttawaRecCalendar.docNextPagePath)).thenReturn(new Elements());

        // Pool info.
        Element address = Mockito.mock(Element.class);
        Element pool = Mockito.mock(Element.class);
        Mockito.when(pool.getElementsByTag("a")).thenReturn(new Elements(address));
        Mockito.when(poolList.getElementsByClass(OttawaRecCalendar.docPoolPath)).thenReturn(new Elements(pool));

        Element schedule = Mockito.mock(Element.class);
        Mockito.when(connector.getPoolList(Mockito.anyInt())).thenReturn(poolList);
        Document poolInfo = Mockito.mock(Document.class);
        Element location = Mockito.mock(Element.class);
        Mockito.when(location.attr("content")).thenReturn("location");
        Mockito.when(poolInfo.getElementsByAttributeValue("name", "dcterms.title")).thenReturn(new Elements(location));

        Mockito.when(poolInfo.getElementsByTag("table")).thenReturn(new Elements(schedule));
        URL url = Mockito.mock(URL.class);
        Mockito.when(connector.getPoolInfo(Mockito.any()))
                .thenReturn(new OttawaRecCalendar.Connector.UrlDoc(url, poolInfo));

        Element caption = Mockito.mock(Element.class);
        Mockito.when(caption.text()).thenReturn("swim");
        Mockito.when(schedule.getElementsByTag("caption")).thenReturn(new Elements(caption));

        Element tbody = Mockito.mock(Element.class);
        Mockito.when(schedule.getElementsByTag("tbody")).thenReturn(new Elements(tbody));

        Element record = Mockito.mock(Element.class);
        Mockito.when(tbody.getElementsByTag("tr")).thenReturn(new Elements(record));
        Element header = Mockito.mock(Element.class);
        Mockito.when(header.text()).thenReturn("Title");

        Mockito.when(record.getElementsByTag("th")).thenReturn(new Elements(header));

        Elements times = new Elements();
        for(int i=0;i<7;i++) {
            Element time = Mockito.mock(Element.class);
            Mockito.when(time.text()).thenReturn(timeBlock);
            times.add(time);
        }
        Mockito.when(record.getElementsByTag("td")).thenReturn(times);

        BaseCalendar calendar = Mockito.mock(BaseCalendar.class);
        orc.go(calendar, connector);


        // Validate.
        for(DayOfWeek i : DayOfWeek.values()) {
            Mockito.verify(calendar).createWeeklyEvent(
                    i,
                    expectedStart,
                    expectedEnd,
                    "Title",
                    "location",
                    url);
        }

    }
}
