package pl.chatme.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class DateUtils {

    private static final String DATE_PATTERN = "dd.MM.yyyy HH:mm:ss";

    public static OffsetDateTime convertStringDateToOffsetTime(String time) {
        var zone = TimeZone.getDefault();

        var localDateTime = LocalDateTime.parse(time, DateTimeFormatter.ofPattern(DATE_PATTERN));
        var zoneOffset = zone.toZoneId().getRules().getOffset(localDateTime);
        return localDateTime.atOffset(zoneOffset);
    }

}
