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

public class RunCshCmd {
    public static boolean run(String cmd) {
        return run(cmd, System.out::println, System.err::println);
    }

    public static boolean run(String cmd, Consumer<String> cout, Consumer<String> cerr) {
        RunCmd rcmd = runx(cmd, cout, cerr);
        return rcmd.isNormalExit() && (0 == rcmd.getExitValue());
    }

    public static RunCmd runx(String cmd) {
        return runx(cmd, System.out::println, System.err::println);
    }

    public static RunCmd runx(String cmd, Consumer<String> cout, Consumer<String> cerr) {
        String[] acmd = new String[]{
                CSH,
                CSH_OPTS,
                cmd
        };
        RunCmd rcmd = new RunCmd(acmd, cout, cerr);
        rcmd.run();
        return rcmd;
    }

    private static final String CSH_OPTS = System.getProperty("csh.opts", "-fc");
    private static final String CSH = System.getProperty("csh.path", "/bin/csh");

    public static void main(String[] argv) {
        final String cmd = String.join(" ", argv);
        run(cmd);
    }
}
