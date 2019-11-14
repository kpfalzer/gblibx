
package gblibx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    public void setCout(Consumer<String> cout) {
        expectNull(__cout);
        __cout = cout;
    }

    public void setCerr(Consumer<String> cerr) {
        expectNull(__cerr);
        __cerr = cerr;
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
            Thread cout = new Thread(new StreamGobbler(proc.getInputStream(), __cout));
            Thread cerr = new Thread(new StreamGobbler(proc.getErrorStream(), __cerr));
            cout.start();
            cerr.start();
            proc.waitFor();
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
