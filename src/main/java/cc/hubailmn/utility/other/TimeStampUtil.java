package cc.hubailmn.utility.other;

import java.text.SimpleDateFormat;
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
}
