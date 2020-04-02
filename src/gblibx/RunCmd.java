
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
    public RunCmd(String s) {
        this(s.split("\\s+"));
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

    @Override
    public void run() {
        ProcessBuilder pb = new ProcessBuilder(__cmd);
        Process proc = null;
        try {
            proc = pb.start();
            List<Thread> threads = Arrays.asList(
                    new Thread(new StreamGobbler(proc.getInputStream(), __cout)),
                    new Thread(new StreamGobbler(proc.getErrorStream(), __cerr))
            );
            threads.forEach(Thread::start);
            proc.waitFor();
            for (Thread thread : threads) thread.join();
            __exitValue = proc.exitValue();
            __exitType = ExitType.eNormal;
        } catch (IOException | InterruptedException e) {
            __setException(e);
        }
    }

    public int getExitValue() {
        return __exitValue;
    }

    public boolean isNormalExit() {
        return __exitType == ExitType.eNormal;
    }

    private void __setException(Exception ex) {
        __exception = ex;
        __exitType = ExitType.eException;
    }

    public static class StreamGobbler implements Runnable {
        public StreamGobbler(InputStream from, Consumer<String> to) {
            __reader = new BufferedReader(new InputStreamReader(from));
            __writer = to;
        }

        private final BufferedReader __reader;
        private final Consumer<String> __writer;

        @Override
        public void run() {
            __reader.lines().forEach(__writer);
        }
    }

}
