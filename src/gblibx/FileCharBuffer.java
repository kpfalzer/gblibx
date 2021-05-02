/*
 *
 *  * The MIT License
 *  *
 *  * Copyright 2021 kpfalzer.
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

import java.io.*;
import java.util.Arrays;

import static gblibx.Util.invariant;

/**
 * Read complete text file buffer.
 */
public class FileCharBuffer extends CharBuffer {
    public FileCharBuffer(String filename) throws IOException {
        super(getContents(filename));
        this.filename = filename;
    }

    public static FileCharBuffer create(String filename) throws IOException {
        return new FileCharBuffer(filename);
    }

    private static char[] getContents(String filename) throws IOException {
        final File f = new File(filename);
        if (!f.canRead()) throw new FileNotFoundException(filename);
        long sz = f.length();
        if (0 == sz) return new char[0];
        invariant(sz <= Integer.MAX_VALUE);
        char[] buf = new char[(int) sz];
        int i = 0, ch;
        char c;
        try (InputStream ins = new FileInputStream(filename)) {
            while (0 < (ch = ins.read())) {
                c = (char) ch;
                if ('\r' != c) buf[i++] = c;
            }
        }
        return (i == sz) ? buf : Arrays.copyOf(buf, i);
    }

    public String getLocation(int lineno, int col) {
        return String.format("%s:%d:%d", lineno, col);
    }

    public final String filename;
}
