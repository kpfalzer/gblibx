
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

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static gblibx.Util.*;

// messages of form: W-17sep19-10:02:35:
public abstract class Logger {
    public enum ELevel {
        eDebug('D'),
        eInfo('I'),
        eWarning('W'),
        eError('E'),
        eFatal('F'),
        eMessage('M')   //always
        ;

        ELevel(char abbrev) {
            this.abbrev = abbrev;
        }

        final char abbrev;
    }

    public interface Print extends AutoCloseable {
        Print print(ELevel svr, String message);
        void flush();
    }

    public ELevel getLevel() {
        return __level;
    }

    public void setLevel(char level) {
        for (ELevel e : ELevel.values()) {
            if (Character.toUpperCase(level) == e.abbrev) {
                setLevel(e);
                return;
            }
        }
        expectNever();
    }

    public ELevel setLevel(ELevel level) {
        final ELevel was = getLevel();
        __level = level;
        return was;
    }

    public Duration getElapsed() {
        return Duration.between(__started, Instant.now());
    }

    protected Logger() {
        this(ELevel.eInfo, true);
    }

    protected Logger(ELevel level) {
        this(level, true);
    }

    protected Logger(ELevel level, boolean startNow) {
        if (startNow) {
            __started = Instant.now();
        }
    }

    public static String getMessage(ELevel svr, String msg) {
        return String.format("%c-%s: %s", svr.abbrev, Util.getLocalDateTime(), msg);
    }

    public abstract Logger print(ELevel svr, String message);

    protected Logger _debug(String message) {
        return print(ELevel.eDebug, message);
    }

    protected Logger _info(String message) {
        return print(ELevel.eInfo, message);
    }

    protected Logger _warning(String message) {
        return print(ELevel.eWarning, message);
    }

    protected Logger _error(String message) {
        return print(ELevel.eError, message);
    }

    protected Logger _fatal(String message) {
        return print(ELevel.eFatal, message);
    }

    protected Logger _message(String message) {
        return print(ELevel.eMessage, message);
    }

    public long getMessageCount(ELevel svr) {
        return __msgCnts.get(svr);
    }

    public boolean doLogMessage(ELevel svr) {
        return (0 >= __level.compareTo(svr));
    }

    protected synchronized Logger _print(ELevel svr, String msg, Iterable<Print> oses) {
        __msgCnts.put(svr, 1 + getMessageCount(svr));
        if (doLogMessage(svr)) {
            final String fmsg = getMessage(svr, msg);
            for (Print os : oses) {
                if (isNonNull(os)) {
                    os.print(svr, fmsg);
                }
            }
        }
        return this;
    }

    protected synchronized Logger _print(ELevel svr, String msg, Print... oses) {
        return _print(svr, msg, toList(oses));
    }

    private Instant __started;
    private ELevel __level = ELevel.eInfo;

    private static Map<ELevel, Long> __initCounts() {
        HashMap<ELevel, Long> counts = new HashMap<>();
        for (ELevel svr : ELevel.values()) {
            counts.put(svr, 0L);
        }
        return counts;
    }

    private Map<ELevel, Long> __msgCnts = __initCounts();
}
