
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

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A java.io.PrintWriter with chain-able methods.
 */
public class PrintWriter implements Closeable {
    public PrintWriter(File fname) throws FileNotFoundException {
        this(fname.getPath());
    }

    public PrintWriter(String fname) throws FileNotFoundException {
        _writer = new java.io.PrintWriter(fname);
    }

    public PrintWriter println(String s) {
        _writer.println(s);
        return this;
    }

    public PrintWriter printf(String format, Object... args) {
        String s = String.format(format, args);
        _writer.print(s);
        return this;
    }

    public PrintWriter print(String s) {
        _writer.print(s);
        return this;
    }

    public void flush() {
        _writer.flush();
    }

    private final java.io.PrintWriter _writer;

    @Override
    public void close() throws IOException {
        _writer.close();
    }
}
