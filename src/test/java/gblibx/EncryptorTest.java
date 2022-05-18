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

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class EncryptorTest {

    final String key = "HY^&UJKI**(OL:P)";
    final String random = "12345678ABCDEFGH"; //must be 16 long
    final String input = "the quick brown fox JUMPS over the lazy dog ***";

    @Test
    void encrypt() throws IOException {
        {
            final String encrypted = Encryptor.encrypt(key, random, input);
            System.out.println("Encrypted="+encrypted);
            final String decrypted = Encryptor.decrypt(key, random, encrypted);
            assertEquals(input, decrypted);
        }
        {
            final File kvf = new File("/Users/kwpfalzer/.gblibx.cryptokey");
            final String encrypted = Encryptor.encrypt(kvf, input);
            System.out.println("Encrypted="+encrypted);
            final String decrypted = Encryptor.decrypt(kvf, encrypted);
            assertEquals(input, decrypted);
        }
    }
}