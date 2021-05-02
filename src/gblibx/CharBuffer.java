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

public class CharBuffer {
    public CharBuffer(char[] buf) {
        __buf = buf;
        __mark = new Mark();
    }

    public CharBuffer(String buf) {
        this(buf.toCharArray());
    }

    public char la(int i) {
        int ix = ix() + i;
        return (isEOF(ix)) ? EOF : __buf[ix];
    }

    public char la() {
        return la(0);
    }

    /**
     * Accept la(0) and return it.
     *
     * @return la(0)
     */
    public char accept() {
        char la0 = la();
        accept(1);
        return la0;
    }

    /**
     * Accept n characters.
     * NOTE: accept() and accept(1) return different values.
     *
     * @param n
     * @return la(n) (i.e., la(0) after skip n).
     */
    public char accept(int n) {
        while (0 <= --n) {
            if (isEOF()) break;
            char c = la();
            __mark.ix++;
            if ('\n' != c) {
                __mark.col++;
            } else {
                __mark = new Mark(lineno() + 1, 0, ix());
            }
        }
        return la();
    }

    public void reset(Mark mark) {
        __mark = mark;
    }

    public Mark mark() {
        return __mark.clone();
    }

    public boolean isEOF() {
        return isEOF(ix());
    }

    public boolean isEOF(int ix) {
        return (__buf.length <= ix);
    }

    private int ix() {
        return __mark.ix;
    }

    /**
     * Get number of chars remaining.
     *
     * @return number of chars remaining.
     */
    public int rem() {
        return __buf.length - ix();
    }

    public int lineno() {
        return __mark.lineno;
    }

    public int col() {
        return __mark.col;
    }

    public static class Mark {
        private Mark(int lineno, int col, int ix) {
            this.lineno = lineno;
            this.col = col;
            this.ix = ix;
        }

        public Mark clone() {
            return new Mark(lineno, col, ix);
        }

        private Mark() {
            this(1, 0, 0);
        }

        public int lineno, col, ix;
    }

    public String getLocation() {
        return getLocation(lineno(), col());
    }

    public String getLocation(int lineno, int col) {
        return String.format("%d:%d", lineno, col);
    }

    public String getLocation(Mark mark) {
        return getLocation(mark.lineno, mark.col);
    }

    private final char[] __buf;
    private Mark __mark;

    public static final char EOF = 0xFF;
}
