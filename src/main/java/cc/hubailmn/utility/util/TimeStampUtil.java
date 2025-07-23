package cc.hubailmn.utility.util;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

public class TimeStampUtil {

    private TimeStampUtil() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static String formatTimestamp(long timestamp) {
        return formatTimestamp(timestamp, "MMMM d, yyyy h:mm a", TimeZone.getDefault());
    }

    public static String formatTimestamp(long millisecond, String pattern, TimeZone timeZone) {
        Date date = new Date(millisecond);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(timeZone);
        return sdf.format(date);
    }

    public static String formatTimestamp(long millisecond, String pattern) {
        return formatTimestamp(millisecond, pattern, TimeZone.getDefault());
    }

    public static String getDate(String pattern, ZoneId zoneId) {
        return LocalDateTime.now(zoneId).format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String getDate() {
        return getDate("MMMM d, yyyy h:mm a", ZoneId.systemDefault());
    }
}
