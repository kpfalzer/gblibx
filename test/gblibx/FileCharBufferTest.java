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

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static gblibx.Util.isNonNull;
import static org.junit.jupiter.api.Assertions.*;

class FileCharBufferTest {

    @Test
    void create() {
        final File f = new File("src/gblibx/FileCharBuffer.java");
        try {
            final CharBuffer cb = new FileCharBuffer(f.getPath());
            assertEquals(f.length(), cb.rem());
            while ('*' != cb.accept(1)) ;
            assertEquals('*', cb.la());
            assertEquals(1, cb.col());
            final String m = "public final String filename";
            CharBuffer.Mark match = null;
            while (m.length() < cb.rem()) {
                for (int i = 0; i < m.length(); ++i) {
                    if (m.charAt(i) == cb.accept()) {
                        if (0 == i) match = cb.mark();
                    } else {
                        match = null;
                        break; //for
                    }
                }
                if (isNonNull(match)) {
                    assertEquals(66, match.lineno);
                    break;//while
                }
            }
            assertNotNull(match);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}