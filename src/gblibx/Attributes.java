
/*
 *
 *  * The MIT License
 *  *
 *  * Copyright 2019 kpfalzer.
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

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;

public abstract class Attributes<T> {
    public static class InvalidKeyException extends Exception {
        public InvalidKeyException(String k) {
            super("Invalid key: " + k);
        }
    }

    public abstract boolean isValidKey(String k);

    public T set(String key, T val) throws InvalidKeyException {
        if (isNull(__attrs)) {
            __attrs = new HashMap<>();
        }
        __attrs.put(checkKey(key), val);
        return val;
    }

    public T get(String key) throws InvalidKeyException {
        if (isNull(__attrs)) {
            return null;
        }
        return __attrs.get(checkKey(key));
    }

    public String checkKey(String k) throws InvalidKeyException {
        if (isValidKey(k)) {
            return k;
        }
        throw new InvalidKeyException(k);
    }

    private Map<String, T> __attrs;
}
