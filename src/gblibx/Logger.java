
package gblibx;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static gblibx.Util.expectNever;
import static gblibx.Util.isNonNull;

// messages of form: W-17sep19-10:02:35:
public abstract class Logger {
    public static enum ELevel {
        eDebug('D'),
        eInfo('I'),
        eWarning('W'),
        eError('E'),
        eFatal('F'),
        eMessage('M')   //always
        ;

        ELevel(char abbrev) {
            this.abbrev = abbrev;
        }

        final char abbrev;
    }

    public static interface Print {
        public Print print(ELevel svr, String message);
    }

    public ELevel getLevel() {
        return __level;
    }

    public void setLevel(char level) {
        switch (level) {
            case 'D':
                setLevel(ELevel.eDebug);
                break;
            case 'I':
                setLevel(ELevel.eInfo);
                break;
            case 'W':
                setLevel(ELevel.eWarning);
                break;
            case 'E':
                setLevel(ELevel.eError);
                break;
            case 'F':
                setLevel(ELevel.eFatal);
                break;
            case 'M':
                setLevel(ELevel.eMessage);
                break;
            default:
                expectNever();
        }
    }

    public ELevel setLevel(ELevel level) {
        final ELevel was = getLevel();
        __level = level;
        return was;
    }

    public Duration getElapsed() {
        return Duration.between(__started, Instant.now());
    }

    protected Logger() {
        this(ELevel.eInfo, true);
    }

    protected Logger(ELevel level) {
        this(level, true);
    }

    protected Logger(ELevel level, boolean startNow) {
        if (startNow) {
            __started = Instant.now();
        }
    }

    public String getMessage(ELevel svr, String msg) {
        return String.format("%c-%s: %s", svr.abbrev, Util.getLocalDateTime(), msg);
    }

    public abstract Logger print(ELevel svr, String message);

    protected Logger _debug(String message) {
        return print(ELevel.eDebug, message);
    }

    protected Logger _info(String message) {
        return print(ELevel.eInfo, message);
    }

    protected Logger _warning(String message) {
        return print(ELevel.eWarning, message);
    }

    protected Logger _error(String message) {
        return print(ELevel.eError, message);
    }

    protected Logger _fatal(String message) {
        return print(ELevel.eFatal, message);
    }

    protected Logger _message(String message) {
        return print(ELevel.eMessage, message);
    }

    public long getMessageCount(ELevel svr) {
        return __msgCnts.get(svr);
    }

    public boolean doLogMessage(ELevel svr) {
        return (0 >= __level.compareTo(svr));
    }

    protected Logger _print(ELevel svr, String msg, Print... oses) {
        __msgCnts.put(svr, 1 + getMessageCount(svr));
        if (doLogMessage(svr)) {
            final String fmsg = getMessage(svr, msg);
            for (Print os : oses) {
                if (isNonNull(os)) {
                    os.print(svr, fmsg);
                }
            }
        }
        return this;
    }

    private Instant __started;
    private ELevel __level = ELevel.eInfo;

    private static Map<ELevel, Long> __initCounts() {
        HashMap<ELevel, Long> counts = new HashMap<>();
        for (ELevel svr : ELevel.values()) {
            counts.put(svr, 0L);
        }
        return counts;
    }

    private Map<ELevel, Long> __msgCnts = __initCounts();
}
