package gblibx;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Useful methods for localizing date/time.
 */
public class GbDateTime {
    public static LocalDateTime tznow() {
        return LocalDateTime.now(LOCAL_ZONE);
    }

    public static LocalDateTime utcnow() {
        return LocalDateTime.now(UTC_ZONE);
    }

    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    public static final String LZ_PROP = "gblibx.GbDateTime.LOCAL_ZONE";
    public static final ZoneId LOCAL_ZONE =
            ZoneId.of(System.getProperty(LZ_PROP, "America/Los_Angeles"));

    public static void main(String[] argv) {
        final DateTimeFormatter dtf = Util.DATE_TIME_FORMATTER;
        final PrintStream os = System.out;
        os.printf("LocalDateTime.now(): %s\n", LocalDateTime.now().format(dtf));
        os.printf("zoned: %s\n", tznow().format(dtf));
        os.printf("utc: %s\n", utcnow().format(dtf));
    }
}
