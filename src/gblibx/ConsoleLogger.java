
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
