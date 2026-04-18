package org.w21parser;

import java.time.Instant;

public class DateUtils {
    public static long toTimestamp(String isoDate) {
        return Instant.parse(isoDate).toEpochMilli();
    }
}
