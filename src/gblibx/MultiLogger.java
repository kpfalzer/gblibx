
package gblibx;

import java.io.File;
import java.io.IOException;

import static gblibx.Util.expectNull;

public class MultiLogger extends Logger {
    public MultiLogger() {
        __clogger = new ConsoleLogger();
        expectNull(__theOne);
        __theOne = this;
    }

    public void setFileLogger(String fname) throws IOException {
        setFileLogger(fname, false);
    }

    public void setFileLogger(String fname, boolean append) throws IOException {
        final File file = new File(fname);
        if (!append) {
            file.delete();
            file.createNewFile();
        }
        __flogger = new FileLogger(file, append);
    }

    @Override
    public Logger print(ELevel svr, String message) {
        return super._print(svr, message, __clogger, __flogger);
    }

    public static Logger debug(String message) {
        return __theOne._debug(message);
    }

    public static Logger info(String message) {
        return __theOne._info(message);
    }

    public static Logger warning(String message) {
        return __theOne._warning(message);
    }

    public static Logger error(String message) {
        return __theOne._error(message);
    }

    public static Logger fatal(String message) {
        return __theOne._fatal(message);
    }

    private final ConsoleLogger __clogger;
    private FileLogger __flogger;
    private static MultiLogger __theOne = null;
}
