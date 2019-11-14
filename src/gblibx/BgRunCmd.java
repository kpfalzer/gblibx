
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
