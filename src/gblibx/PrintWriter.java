
package gblibx;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A java.io.PrintWriter with chain-able methods.
 */
public class PrintWriter implements Closeable {
    public PrintWriter(File fname) throws FileNotFoundException {
        this(fname.getPath());
    }

    public PrintWriter(String fname) throws FileNotFoundException {
        _writer = new java.io.PrintWriter(fname);
    }

    public PrintWriter println(String s) {
        _writer.println(s);
        return this;
    }

    public PrintWriter printf(String format, Object... args) {
        String s = String.format(format, args);
        _writer.print(s);
        return this;
    }

    public PrintWriter print(String s) {
        _writer.print(s);
        return this;
    }

    public void flush() {
        _writer.flush();
    }

    private final java.io.PrintWriter _writer;

    @Override
    public void close() throws IOException {
        _writer.close();
    }
}
