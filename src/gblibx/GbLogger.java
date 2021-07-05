package gblibx;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static gblibx.Util.createFile;
import static gblibx.Util.downcast;
import static gblibx.Util.expectNever;
import static gblibx.Util.invariant;
import static gblibx.Util.isNonNull;

/**
 * Another logging facility (better than gblibx.Logger?).
 */
public class GbLogger extends Logger {

    /**
     * Create console logger and use stdout + stderr.
     */
    public GbLogger() {
        this(true);
    }

    /**
     * Create console logger.
     *
     * @param useStderr specify to use stderr (when needed) and stdout.
     */
    public GbLogger(boolean useStderr) {
        try {
            initialize(null, true, useStderr);
        } catch (Util.FileException e) {
            expectNever();
        }
    }

    /**
     * Create file logger and (optional) console logger.
     *
     * @param log        logfile name (created here) or null.
     * @param useConsole log to console (if true).
     * @param useStderr  use stderr (when needed) for console.
     * @throws {@link Util.FileException} if log cannot be created.
     */
    public GbLogger(File log, boolean useConsole, boolean useStderr) throws Util.FileException {
        initialize(log, useConsole, useStderr);
    }

    public GbLogger debug(String message) {
        return downcast(_debug(message));
    }

    public GbLogger info(String message) {
        return downcast(_info(message));
    }

    public GbLogger warning(String message) {
        return downcast(_warning(message));
    }

    public GbLogger error(String message) {
        return downcast(_error(message));
    }

    public GbLogger fatal(String message) {
        _fatal(message);
        if (0 != __exitCodeOnFatal) {
            System.exit(__exitCodeOnFatal);
        }
        return this;
    }

    public GbLogger message(String message) {
        return downcast(_message(message));
    }

    /**
     * Set exit code on fatal message.
     *
     * @param code 0 for no exit, otherwise exit code.
     * @return this object.
     */
    public GbLogger setExitOnFatal(int code) {
        __exitCodeOnFatal = code;
        return this;
    }

    @Override
    public Logger print(ELevel svr, String message) {
        _print(svr, message, __handlers);
        return this;
    }

    /**
     * Initialize this object.
     *
     * @param log        logfile name (created here) or null.
     * @param useConsole log to console (if true).
     * @param useStderr  use stderr (when needed) for console.
     * @return this instance.
     * @throws {@link Util.FileException} if log cannot be created.
     */
    private GbLogger initialize(File log, boolean useConsole, boolean useStderr) throws Util.FileException {
        invariant(isNonNull(log) || useConsole, "Specify log or console");
        if (useStderr) invariant(useConsole);
        if (isNonNull(log)) {
            try {
                __handlers.add(new FileLogger(createFile(log).getAbsolutePath()));
            } catch (IOException e) {
                throw new Util.CreateFileFailed(log, e);
            } catch (Util.FileException e) {
                throw e;
            }
        }
        if (useConsole) {
            __handlers.add(new ConsoleLogger(useStderr));
        }
        return this;
    }

    private int __exitCodeOnFatal = 0;
    private final List<Logger.Print> __handlers = new LinkedList<>();
}
