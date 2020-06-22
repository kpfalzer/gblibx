
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class FileLogger implements Logger.Print {
    public FileLogger(String name) throws FileNotFoundException {
        this(new File(name));
    }

    public FileLogger(File flog) throws FileNotFoundException {
        this(flog, false);
    }

    public FileLogger(File flog, boolean append) throws FileNotFoundException {
        __file = flog;
        __os = new PrintStream(new FileOutputStream(__file, append));
    }

    /**
     * Unconditionally, print message to file.
     * @param svr value is ignored.
     * @param message message to print.
     * @return
     */
    @Override
    public synchronized Logger.Print print(Logger.ELevel svr, String message) {
        __os.println(message);
        __os.flush();
        return this;
    }

    public File getFile() {
        return __file;
    }

    public String getFileName() {
        return getFile().getPath();
    }

    private final PrintStream __os;
    private final File __file;
}
