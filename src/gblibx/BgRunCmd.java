
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

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gblibx.Util.invariant;

public class BgRunCmd extends RunCmd {
    public static BgRunCmd create(String cmd) {
        return create(cmd, System.out::println);
    }

    public static BgRunCmd create(String cmd, Consumer<String> cout) {
        BgRunCmd bgcmd = new BgRunCmd(cmd, System.err::println);
        bgcmd.setCout(bgcmd.new Gobbler(cout));
        return bgcmd;
    }

    private BgRunCmd(String cmd, Consumer<String> cerr) {
        super(new String[]{"/bin/csh", "-f", "-c", cmd + " &"}, null, cerr);
    }

    public int getPid() {
        return __pid;
    }

    private int __pid = -99;

    private static final Pattern PID = Pattern.compile("\\s*\\[(\\d+)\\]\\s+(\\d+)");

    private class Gobbler implements Consumer<String> {
        public Gobbler(Consumer<String> os) {
            __os = os;
        }

        private final Consumer<String> __os;

        @Override
        public void accept(String s) {
            if (-99 == __pid) {
                //[n] pid
                final Matcher m = PID.matcher(s);
                if (m.matches()) {
                    __pid = Integer.parseInt(m.group(2));
                    invariant(0 < __pid);
                } else {
                    __pid = -1;
                }
            }
            __os.accept(s);
        }
    }
}
