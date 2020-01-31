/*
 *
 *  * The MIT License
 *  *
 *  * Copyright 2020 kpfalzer.
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

import java.util.ArrayList;

/**
 * A base-x incrementor.
 */
public class BasexIncr {
    public BasexIncr() {
        this(36);
    }

    public BasexIncr(int base) {
        __base = base;
        __count.add(0);
    }

    public BasexIncr incr() {
        final int sz = __count.size();
        for (int i = 0; i < sz; ++i) {
            int val = (__count.get(i) + 1) % __base;
            __count.set(i, val);
            if (0 == val) {
                if (i == (sz - 1)) {
                    __count.add(1);
                    break;
                }
            } else {
                break;
            }
        }
        return this;
    }

    public String toString() {
        final int sz = __count.size();
        char val[] = new char[sz];
        for (int i = 0; i < val.length; ++i) {
            val[sz - 1 - i] = __S.charAt(__count.get(i));
        }
        return new String(val);
    }

    public long toLong() {
        long tl = 0, mult = 1;
        for (Integer e : __count) {
            tl += (e * mult);
            mult *= __base;
        }
        return tl;
    }

    private static final String __S =
            "0123456789"
                    + "abcdefghijklmnopqrstuvwxyz"
                    + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final int __base;
    private final ArrayList<Integer> __count = new ArrayList<>();
}
