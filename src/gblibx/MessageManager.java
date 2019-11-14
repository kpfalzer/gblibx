
package gblibx;

import java.util.HashMap;
import java.util.Map;

import static gblibx.Util.expectNonNull;
import static gblibx.Util.expectNull;

public abstract class MessageManager {
    protected MessageManager() {
        this(new MultiLogger());
    }

    protected MessageManager(MultiLogger logger) {
        __logger = logger;
        expectNull(__theOne);
        __theOne = this;
    }

    public MultiLogger logger() {
        return __logger;
    }

    /**
     * Check if we would log the debug (severity) messages.
     * Useful precondition to qualify expensive debug-related calculations,
     * so we don't waste time doing if they won't be logged.
     *
     * @return true if we log debug level.
     */
    public static boolean doLogDebug() {
        return __theOne.logger().doLogMessage(Logger.ELevel.eDebug);
    }

    public static void debug(String key, Object... args) {
        MultiLogger.debug(__getMessage(key, args));
    }

    public static void info(String key, Object... args) {
        MultiLogger.info(__getMessage(key, args));
    }

    public static void warning(String key, Object... args) {
        MultiLogger.warning(__getMessage(key, args));
    }

    public static void error(String key, Object... args) {
        MultiLogger.error(__getMessage(key, args));
    }

    public static void fatal(String key, Object... args) {
        MultiLogger.fatal(__getMessage(key, args));
    }

    private static String __getMessage(String key, Object... args) {
        String format = expectNonNull(__MESSAGES.get(key));
        return String.format("%s  (%s)", String.format(format, args), key);
    }


    private final MultiLogger __logger;
    private static MessageManager __theOne = null;

    private static Map<String, String> __MESSAGES = new HashMap<>();

    protected static Map<String, String> _add(String key, String format) {
        __MESSAGES.put(key, format);
        return __MESSAGES;
    }
}
