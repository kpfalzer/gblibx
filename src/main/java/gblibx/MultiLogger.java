
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

import java.io.File;
import java.io.IOException;

import static gblibx.Util.expectNull;

public class MultiLogger extends Logger {
    public MultiLogger() {
        this(true);
    }

    public MultiLogger(boolean useConsole) {
        __clogger = (useConsole) ? new ConsoleLogger() : null;
        expectNull(__theOne);
        __theOne = this;
    }

    public MultiLogger(File fname, boolean useConsole) throws IOException {
        this(useConsole);
        setFileLogger(fname.getPath());
    }

    public MultiLogger(String fname) throws IOException {
        this();
        setFileLogger(fname);
    }

    public void setFileLogger(String fname) throws IOException {
        setFileLogger(fname, false);
    }

    public void setFileLogger(String fname, boolean append) throws IOException {
        final File file = new File(fname);
        if (!append) {
            file.delete();
            file.createNewFile();
        }
        __flogger = new FileLogger(file, append);
    }

    @Override
    public Logger print(ELevel svr, String message) {
        return super._print(svr, message, __clogger, __flogger);
    }

    public static Logger debug(String message) {
        return __theOne._debug(message);
    }

    public static Logger info(String message) {
        return __theOne._info(message);
    }

    public static Logger warning(String message) {
        return __theOne._warning(message);
    }

    public static Logger error(String message) {
        return __theOne._error(message);
    }

    public static Logger fatal(String message) {
        return __theOne._fatal(message);
    }

    public static Logger message(String message) {
        return __theOne._message(message);
    }

    private final ConsoleLogger __clogger;
    private FileLogger __flogger;
    private static MultiLogger __theOne = null;
}
