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
        long millis = timestamp * 1000L;
        Date date = new Date(millis);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public String getDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm"));
    }
}
