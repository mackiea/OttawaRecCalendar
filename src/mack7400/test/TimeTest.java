package mack7400.test;

import mack7400.code.BaseCalendar;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class TimeTest {
    static Stream<Arguments> constructorValues () {
        return Stream.of(
                Arguments.of("3:30 pm", new BaseCalendar.Time(15, 30))
        );
    }

    @ParameterizedTest
    @MethodSource("constructorValues")
    void constructor(String input, BaseCalendar.Time expected) {
        Assertions.assertEquals(expected, new BaseCalendar.Time(input));
    }
}
