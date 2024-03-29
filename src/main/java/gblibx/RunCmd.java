
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static gblibx.Util.expectNull;

public class RunCmd implements Runnable {
    public static int runCommand(String command) {
        RunCmd e = new RunCmd(command);
        e.run();
        return e.getExitValue();
    }

    public static String runCommandStdout(String command) {
        final StringBuilder stdout = new StringBuilder();
        RunCmd e = new RunCmd(new Consumer<String>() {
            @Override
            public void accept(String s) {
                stdout.append((0 < stdout.length()) ? "\n" : "").append(s);
            }
        }, splitCmd(command));
        e.run();
        return stdout.toString();
    }

    public static String[] splitCmd(String cmd) {
        return cmd.split("\\s+");
    }

    public RunCmd(String cmd) {
        this(splitCmd(cmd));
    }

    public RunCmd(String... cmd) {
        this(cmd, System.out::println, System.err::println);
    }

    public RunCmd(Consumer<String> cout, String... cmd) {
        this(cout, cout, cmd);
    }

    public RunCmd(Consumer<String> cout, Consumer<String> cerr, String... cmd) {
        this(cmd, cout, cerr);
    }

    public RunCmd(String[] cmd, Consumer<String> cout, Consumer<String> cerr) {
        __cmd = cmd;
        __cout = cout;
        __cerr = cerr;
        _builder = new ProcessBuilder(__cmd);
    }

    public RunCmd(List<String> cmd, Consumer<String> cout, Consumer<String> cerr) {
        this(cmd.toArray(new String[0]), cout, cerr);
    }

    public RunCmd(String cmd, Consumer<String> cout, Consumer<String> cerr) {
        this(splitCmd(cmd), cout, cerr);
    }

    public RunCmd setCout(Consumer<String> cout) {
        expectNull(__cout);
        __cout = cout;
        return this;
    }

    public RunCmd setCerr(Consumer<String> cerr) {
        expectNull(__cerr);
        __cerr = cerr;
        return this;
    }

    public static enum ExitType {
        eUnknown, eNormal, eException
    }

    public String getCmd() {
        return Util.join(__cmd);
    }

    private final String[] __cmd;
    private int __exitValue = -666;
    private ExitType __exitType = ExitType.eUnknown;
    private Exception __exception = null;
    private Consumer<String> __cout, __cerr;
    protected final ProcessBuilder _builder;
    protected Process _process = null;

    @Override
    public void run() {
        try {
            _process = _builder.start();
            List<Thread> threads = Arrays.asList(
                    new Thread(new StreamGobbler(_process.getInputStream(), __cout)),
                    new Thread(new StreamGobbler(_process.getErrorStream(), __cerr))
            );
            threads.forEach(Thread::start);
            _process.waitFor();
            for (Thread thread : threads) thread.join();
            __exitValue = _process.exitValue();
            __exitType = ExitType.eNormal;
        } catch (IOException | InterruptedException e) {
            __setException(e);
        }
    }

    public int getExitValue() {
        return __exitValue;
    }

    public ExitType getExitType() {
        return __exitType;
    }

    public boolean isNormalExit() {
        return  ExitType.eNormal == getExitType();
    }

    public Exception getException() {
        return __exception;
    }

    private void __setException(Exception ex) {
        __exception = ex;
        __exitType = ExitType.eException;
    }

    public static class StreamGobbler implements Runnable {
        public StreamGobbler(InputStream from, Consumer<String> to) {
            _reader = new BufferedReader(new InputStreamReader(from));
            _writer = to;
        }

        protected final BufferedReader _reader;
        protected final Consumer<String> _writer;

        @Override
        public void run() {
            _reader.lines().forEach(_writer);
        }
    }

}
