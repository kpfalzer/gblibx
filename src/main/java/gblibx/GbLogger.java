package gblibx;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static gblibx.Util.createFile;
import static gblibx.Util.downcast;
import static gblibx.Util.expectNever;
import static gblibx.Util.invariant;
import static gblibx.Util.isNonNull;

/**
 * Another logging facility (better than gblibx.Logger?).
 */
public class GbLogger extends Logger implements AutoCloseable {

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
     * @param useConsole log to console and stderr (if true).
     * @throws {@link Util.FileException} if log cannot be created.
     */
    public GbLogger(File log, boolean useConsole) throws Util.FileException {
        this(log,useConsole,useConsole);
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
     * Add handler for named logger ({@link java.util.logging.Logger}).
     *
     * @param name name of logger (usu. package name).
     * @return this object.
     */
    public GbLogger addNamedLogger(String name, Function<LogRecord, String> logRecordToString) {
        java.util.logging.Logger xlogger = java.util.logging.Logger.getLogger(name);
        xlogger.setLevel(Level.ALL);    //we filter here, so accept all messages.
        xlogger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                GbLogger.this.publish(record, logRecordToString);
            }

            @Override
            public void flush() {
                GbLogger.this.flush();
            }

            @Override
            public void close() throws SecurityException {
                //do nothing
            }
        });
        return this;
    }

    private void publish(LogRecord record, Function<LogRecord, String> logRecordToString) {
        final int level = record.getLevel().intValue();
        switch (getLevel()) {
            case eDebug:
                if (level >= Level.FINEST.intValue()) {
                    debug(logRecordToString.apply(record));
                }
                break;
            case eInfo:
                if (level >= Level.INFO.intValue()) {
                    info(logRecordToString.apply(record));
                }
                break;
            case eWarning:
                if (level >= Level.WARNING.intValue()) {
                    warning(logRecordToString.apply(record));
                }
                break;
            case eError:
            case eFatal:
                if (level >= Level.SEVERE.intValue()) {
                    error(logRecordToString.apply(record));
                }
                break;
            case eMessage:
                if (level >= Level.ALL.intValue()) {
                    message(logRecordToString.apply(record));
                }
                break;
        }
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

    private void flush() {
        __handlers.stream().forEach(h -> h.flush());
    }

    @Override
    public void close() throws Exception {
        for (AutoCloseable c : __handlers) {
            c.close();
        }
    }
}
