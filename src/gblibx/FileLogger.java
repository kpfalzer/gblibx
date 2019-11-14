
package gblibx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class FileLogger implements Logger.Print {
    public FileLogger(File flog) throws FileNotFoundException {
        this(flog, false);
    }

    public FileLogger(File flog, boolean append) throws FileNotFoundException {
        __file = flog;
        __os = new PrintStream(new FileOutputStream(__file, append));
    }

    @Override
    public Logger.Print print(Logger.ELevel svr, String message) {
        __os.println(message);
        __os.flush();
        return this;
    }

    public File getFile() {
        return __file;
    }

    public String getFileName() {
        return getFile().getPath();
    }

    private final PrintStream __os;
    private final File __file;
}
