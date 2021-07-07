
/*
 *
 *  * The MIT License
 *  *
 *  * Copyright 2006 - 2020 kpfalzer.
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *
 *
 */
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
    public synchronized Logger.Print print(Logger.ELevel svr, String message) {
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

    @Override
    public void flush() {
        __cerr.flush();
        __cerr.flush();
    }

    @Override
    public void close() throws Exception {
        //do nothing since cerr/out closing handled by system level?!
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

        public Logger info(String msg) {
            print(ELevel.eInfo, msg);
            return this;
        }

        public Logger info(String fmt, Object... args) {
            return info(String.format(fmt, args));
        }

        public Logger warn(String msg) {
            print(ELevel.eWarning, msg);
            return this;
        }

        public Logger warn(String fmt, Object... args) {
            return warn(String.format(fmt, args));
        }

        public Logger error(String msg) {
            print(ELevel.eError, msg);
            return this;
        }

        public Logger error(String fmt, Object... args) {
            return error(String.format(fmt, args));
        }

        public Logger fatal(String msg) {
            print(ELevel.eFatal, msg);
            return this;
        }

        public Logger fatal(String fmt, Object... args) {
            return fatal(String.format(fmt, args));
        }

        public Logger debug(String msg) {
            print(ELevel.eDebug, msg);
            return this;
        }

        public Logger debug(String fmt, Object... args) {
            return debug(String.format(fmt, args));
        }

        public Logger message(String msg) {
            print(ELevel.eMessage, msg);
            return this;
        }

        public Logger message(String fmt, Object... args) {
            return message(String.format(fmt, args));
        }

        private final Print __logger;
    }

    private final PrintStream __cout, __cerr;
}
