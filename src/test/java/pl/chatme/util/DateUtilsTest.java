package pl.chatme.util;

import org.junit.jupiter.api.Test;

import java.time.format.DateTimeParseException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class DateUtilsTest {

    @Test
    public void shouldProperlyConvertDateWithSupportedFormat() {

        //given
        var dateInString = "24.05.2020 15:31:55";

        //when
        var converted = DateUtils.convertStringDateToOffsetTime(dateInString);

        //then
        assertThat(converted, not(nullValue()));
        assertThat(converted.getDayOfMonth(), equalTo(24));
        assertThat(converted.getMonthValue(), equalTo(5));
        assertThat(converted.getYear(), equalTo(2020));
    }

    @Test
    public void exceptionDateTimeParseExceptionShouldBeThrownWhenDateIsInUnsupportedFormat() {

        //given
        var dateInString = "2020.12.20 15:31:55";

        //when
        //then
        assertThrows(DateTimeParseException.class, () -> DateUtils.convertStringDateToOffsetTime(dateInString));
    }


}
