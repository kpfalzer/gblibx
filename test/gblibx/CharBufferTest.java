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

import static org.junit.jupiter.api.Assertions.*;

class CharBufferTest {

    @Test
    void la() {
        final CharBuffer cb1 = new CharBuffer("0123");
        assertEquals('0', cb1.la());
    }

    @Test
    void accept() {
        final CharBuffer cb1 = new CharBuffer("0123");
        assertEquals('0', cb1.accept());
    }

    @Test
    void reset() {
        final CharBuffer cb1 = new CharBuffer("012345");
        CharBuffer.Mark m1 = cb1.mark();
        assertEquals('3', cb1.accept(3));
        assertEquals(3, cb1.col());
        cb1.reset(m1);
        assertEquals(0, cb1.col());
        assertEquals('0', cb1.la());
    }

    @Test
    void isEOF() {
        final CharBuffer cb1 = new CharBuffer("123\n456\n789");
        assertEquals(0, cb1.col());
        assertEquals('\n', cb1.accept(3));
        assertEquals(1, cb1.lineno());
        assertEquals(3, cb1.col());
        assertEquals('4', cb1.accept(1));
        assertEquals(2, cb1.lineno());
        assertEquals(0, cb1.col());
        cb1.accept(99);
        assertTrue(cb1.isEOF());
        assertEquals(CharBuffer.EOF, cb1.la());
    }
}