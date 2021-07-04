package gblibx.logging;

import gblibx.Util;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import static gblibx.Util.*;

/**
 * Another logging facility based on java.util.logging package classes.
 */
public class GbLogger {

    /**
     * Create console logger.
     * Logger name is auto-generated.
     */
    public GbLogger() {
        this(generateName());
    }

    /**
     * Create console logger.
     *
     * @param name logger name ({@link Logger#getLogger(String)}).
     */
    public GbLogger(String name) {
        try {
            initialize(name, null, true);
        } catch (FileException e) {
            expectNever();
        }
    }

    /**
     * Create file logger and (optional) console logger.
     * Logger name is auto-generated.
     *
     * @param log        logfile name (created here).
     * @param useConsole log to console (if true).
     * @throws {@link Util.FileException} if log cannot be created.
     */
    public GbLogger(File log, boolean useConsole) throws Util.FileException {
        this(generateName(), log, useConsole);
    }

    /**
     * Create file logger and (optional) console logger.
     *
     * @param name       logger name ({@link Logger#getLogger(String)}).
     * @param log        logfile name (created here).
     * @param useConsole log to console (if true).
     * @throws {@link Util.FileException} if log cannot be created.
     */
    public GbLogger(String name, File log, boolean useConsole) throws Util.FileException {
        initialize(name, log, useConsole);
    }

    /**
     * Initialize this object.
     * @param name logger name ({@link Logger#getLogger(String)}).
     * @param log logfile name (created here).
     * @param useConsole log to console (if true).
     * @return this instance.
     * @throws {@link Util.FileException} if log cannot be created.
     */
    private GbLogger initialize(String name, File log, boolean useConsole) throws Util.FileException {
        __logger = Logger.getLogger(name);
        if (isNonNull(log)) {
            try {
                __logger.addHandler(new FileHandler(createFile(log).getAbsolutePath()));
            } catch (IOException e) {
                throw new Util.CreateFileFailed(log, e);
            } catch (Util.FileException e) {
                throw e;
            }
        }
        if (useConsole) {
            __logger.addHandler(new GbConsoleLogger());
        }
        return this;
    }

    private synchronized static String generateName() {
        return String.format("gblogger%d", __incr);
    }

    /**
     * Suffix for auto-generated name.
     */
    private static int __incr = 0;
    /**
     * Logger instance.
     */
    private Logger __logger;
    /**
     * Helper logger instance.
     */
    private final XLogger __xlogger = new XLogger();

    private class XLogger extends gblibx.Logger {
        @Override
        public gblibx.Logger print(ELevel svr, String message) {
            return null;
        }
    }
}
