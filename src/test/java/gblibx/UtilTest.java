package gblibx;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilTest {

    @Test
    void createFile() {
        {
            try {
                Util.createFile("/tmp");
            } catch (Util.FileException e) {
                assertTrue(e instanceof Util.DirectoryAlreadyExists);
            }
        }
        {
            try {
                final String fn = "/bogus.kwp";
                File x = Util.createFile(fn);
            } catch (Util.FileException e) {
                assertTrue(e instanceof Util.DeleteFileFailed);
            }
        }
        {
            try {
                Util.createFile("/kwp/aaa");
            } catch (Util.FileException e) {
                assertTrue(e instanceof Util.MkdirFailed);
            }
        }
    }
}