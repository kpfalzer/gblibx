
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

import java.util.HashMap;
import java.util.Map;

import static gblibx.Util.expectNonNull;
import static gblibx.Util.expectNull;

public abstract class MessageManager {
    protected MessageManager() {
        this(new MultiLogger());
    }

    protected MessageManager(MultiLogger logger) {
        __logger = logger;
        expectNull(__theOne);
        __theOne = this;
    }

    public MultiLogger logger() {
        return __logger;
    }

    /**
     * Check if we would log the debug (severity) messages.
     * Useful precondition to qualify expensive debug-related calculations,
     * so we don't waste time doing if they won't be logged.
     *
     * @return true if we log debug level.
     */
    public static boolean doLogDebug() {
        return __theOne.logger().doLogMessage(Logger.ELevel.eDebug);
    }

    public static void debug(String key, Object... args) {
        MultiLogger.debug(__getMessage(key, args));
    }

    public static void info(String key, Object... args) {
        MultiLogger.info(__getMessage(key, args));
    }

    public static void warning(String key, Object... args) {
        MultiLogger.warning(__getMessage(key, args));
    }

    public static void error(String key, Object... args) {
        MultiLogger.error(__getMessage(key, args));
    }

    public static void fatal(String key, Object... args) {
        MultiLogger.fatal(__getMessage(key, args));
    }

    public static void message(String key, Object... args) {
        MultiLogger.message(__getMessage(key, args));
    }

    private static String __getMessage(String key, Object... args) {
        String format = expectNonNull(__MESSAGES.get(key));
        return String.format("%s  (%s)", String.format(format, args), key);
    }


    private final MultiLogger __logger;
    private static MessageManager __theOne = null;

    private static Map<String, String> __MESSAGES = new HashMap<>();

    protected static Map<String, String> _add(String key, String format) {
        __MESSAGES.put(key, format);
        return __MESSAGES;
    }
}
