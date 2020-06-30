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

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import static gblibx.Util.invariant;

/**
 * See: https://stackoverflow.com/questions/1205135/how-to-encrypt-string-in-java
 * NOTE comment:
 * CBC is no longer a secure mode. Padding is vulnerable to padding Oracle attacks. Also, handling the key and messages
 * in String is not safe. They'll linger in the String pool and appear in a heap dump –
 *
 */
public class Encryptor {
    /*
    import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptorDemo {



    public static void main(String[] args) {
        String key = "JavasEncryptDemo"; // 128 bit key
        String randomVector = "RandomJavaVector"; // 16 bytes IV
        decrypt(key, randomVector, encrypt(key, randomVector, "Anything you want to encrypt!"));

    }
}
     */
    public static String encrypt(String key, String randomVector, String value) {
        try {
            final Util.Pair<IvParameterSpec, SecretKeySpec> keys = getKeys(key, randomVector);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, keys.v2, keys.v1);
            byte[] encrypted = cipher.doFinal(value.getBytes());
            //System.out.println("encrypted text: "  + Base64.getEncoder().encodeToString(encrypted));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encrypt(String kiv, String value) {
        return encrypt(kiv.substring(16), kiv.substring(0,16), value);
    }

    public static String encrypt(File kiv, String value) throws IOException {
        return encrypt(Util.readFile(kiv.getAbsolutePath()).trim(), value);
    }

    public static String decrypt(File kiv, String encrypted) throws IOException {
        return decrypt(Util.readFile(kiv.getAbsolutePath()).trim(), encrypted);
    }

    public static String decrypt(String kiv, String encrypted) {
        return decrypt(kiv.substring(16), kiv.substring(0,16), encrypted);
    }

    public static String decrypt(String key, String randomVector, String encrypted) {
        try {
            final Util.Pair<IvParameterSpec, SecretKeySpec> keys = getKeys(key, randomVector);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, keys.v2, keys.v1);
            byte[] originalText = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            //System.out.println("decrypted text: "  + new String(originalText));
            return new String(originalText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Util.Pair<IvParameterSpec, SecretKeySpec> getKeys(String key, String randomVector) throws UnsupportedEncodingException {
        invariant(16 == key.length(), "key must be 16 chars");
        invariant(16 == randomVector.length(), "IV must be 16 chars");
        IvParameterSpec iv = new IvParameterSpec(randomVector.getBytes("UTF-8"));
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), KEY);
        return new Util.Pair(iv, skeySpec);
    }

    public static String KEY = System.getProperty("gblibx.Encryptor.KEY","AES");
    public static String CIPHER = System.getProperty("gblibx.Encryptor.CIPHER","AES/CBC/PKCS5PADDING");

    public static void main(String[] argv) {
        if (2 != argv.length) {
            System.err.println("Usage: key value");
            System.exit(1);
        }
        final String key = argv[0], value = argv[1];
        if (32 != key.length()) {
            System.err.println("key must be 32 chars");
            System.exit(1);
        }
        final String encrypted = encrypt(key, value);
        System.out.println("Encrypted: " + encrypted);
        final String decrypted = decrypt(key, encrypted);
        //System.out.println("Decrypted: " + decrypted);
        invariant(value.equals(decrypted));
        System.exit(0);
    }
}
