package gblibx;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

    public static ZonedDateTime ldt2utc(LocalDateTime ldt) {
        return ldt.atZone(LOCAL_ZONE).withZoneSameInstant(UTC_ZONE);
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
        final ZonedDateTime nowutc = ldt2utc(LocalDateTime.now());
        os.printf("nowutc: %s\n", nowutc.format(DateTimeFormatter.ISO_INSTANT));//toString());
    }
}
