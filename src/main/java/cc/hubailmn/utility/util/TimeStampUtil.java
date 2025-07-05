package cc.hubailmn.utility.util;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TimeStampUtil {

    private TimeStampUtil() {
        throw new UnsupportedOperationException("This is a utility class.");
    }

    public static String formatTimestamp(long timestamp) {
        return formatTimestamp(timestamp, "MMMM d, yyyy h:mm a");
    }

    public static String formatTimestamp(long millisecond, String pattern) {
        Date date = new Date(millisecond);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    public String getDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a"));
    }
}
