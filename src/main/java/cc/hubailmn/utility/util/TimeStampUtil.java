package cc.hubailmn.utility.util;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

public class TimeStampUtil {

    private static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("Asia/Riyadh");
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Asia/Riyadh");

    private TimeStampUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String formatTimestamp(long timestamp) {
        return formatTimestamp(timestamp, "MMMM d, yyyy h:mm a", DEFAULT_TIME_ZONE);
    }

    public static String formatTimestamp(long millisecond, String pattern, TimeZone timeZone) {
        Date date = new Date(millisecond);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(timeZone);
        return sdf.format(date);
    }

    public static String formatTimestamp(long millisecond, String pattern) {
        return formatTimestamp(millisecond, pattern, DEFAULT_TIME_ZONE);
    }

    public static String getDate(String pattern, ZoneId zoneId) {
        return LocalDateTime.now(zoneId).format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String getDate() {
        return getDate("MMMM d, yyyy h:mm a", DEFAULT_ZONE_ID);
    }
}
