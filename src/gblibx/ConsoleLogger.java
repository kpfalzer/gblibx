
package gblibx;

import java.io.PrintStream;

import static gblibx.Util.expectNever;
import static gblibx.Util.isNonNull;

/**
 * Implementation of Logger with colored outputs.
 * Use ConsoleLogger within MultiLogger.
 * Use ConsoleLogger.Impl as the only logger, otherwise.
 */
public class ConsoleLogger implements Logger.Print {
    public ConsoleLogger() {
        this(true);
    }

    public ConsoleLogger(boolean useStderr) {
        __cout = System.out;
        __cerr = (useStderr) ? System.err : System.out;
    }

    @Override
    public Logger.Print print(Logger.ELevel svr, String message) {
        String color = null;
        PrintStream os = __cout;
        switch (svr) {
            case eDebug:
                color = ConsoleColors.BLUE;
                break;
            case eInfo: //fall through
            case eMessage:
                break;
            case eWarning:
                color = ConsoleColors.YELLOW;
                break;
            case eError:
                color = ConsoleColors.RED;
                os = __cerr;
                break;
            case eFatal:
                color = ConsoleColors.YELLOW_BACKGROUND_BRIGHT + ConsoleColors.RED;
                os = __cerr;
                break;
            default:
                expectNever();
        }
        if (isNonNull(color)) {
            ConsoleColors.print(os, color, message + System.lineSeparator());
        } else {
            os.println(message);
        }
        os.flush(); //need to flush to keep stdout/err in order.
        return this;
    }

    /**
     * A toplevel Logger implementation.
     */
    public static class Impl extends Logger {
        public Impl() {
            this(true);
        }

        public Impl(boolean useStderr) {
            __logger = new ConsoleLogger(useStderr);
        }

        public Logger print(ELevel svr, String message) {
            _print(svr, message, __logger);
            return this;
        }

        private final Print __logger;
    }

    private final PrintStream __cout, __cerr;
}
