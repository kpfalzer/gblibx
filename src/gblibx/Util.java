
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.setPosixFilePermissions;
import static java.util.Objects.isNull;

public class Util {
    public static String repeat(String s, int rep) {
        //note: there is String.repeat() in java11
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < rep; ++i) {
            buf.append(s);
        }
        return buf.toString();
    }

    public static String readFile(String fname) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fname)));
    }

    public static Path toPath(File file) {
        return FileSystems.getDefault().getPath(file.getPath());
    }

    public static String getAbsoluteFileName(File f) {
        return toPath(f).toAbsolutePath().toString();
    }

    public static String getAbsoluteFileName(String fname) {
        return getAbsoluteFileName(new File(fname));
    }

    /**
     * Determine (shortest) relative path of one filename to another.
     *
     * @param to    reference path.
     * @param other other path.
     * @return shortest relative path of 'other' to 'to'.
     */
    public static Path getRelativePath(File to, File other) {
        return toPath(to).relativize(toPath(other));
    }


    public static boolean isEven(int n) {
        int rem = n % 2;
        return 0 == rem;
    }

    public static <T> Stream<T> stream(T... eles) {
        return Arrays.stream(eles);
    }

    public static <T, R> R applyIfNotNull(T obj, Function<T, R> func) {
        if (obj != null) {
            return func.apply(obj);
        }
        return null;
    }

    public static <T> void acceptIfNotNull(T obj, Consumer<T> func) {
        if (obj != null) {
            func.accept(obj);
        }
    }

    public static <T> void expectNonNullThenAccept(T obj, Consumer<T> func) {
        expectNonNull(obj);
        func.accept(obj);
    }

    public static JSONArray readJSONArray(String fname) throws IOException {
        String jsonTxt = new String(Files.readAllBytes(Paths.get(fname)));
        JSONArray json = new JSONArray(jsonTxt);
        return json;
    }

    public static JSONObject readJSONObject(String fname) throws IOException {
        String jsonTxt = new String(Files.readAllBytes(Paths.get(fname)));
        JSONObject json = new JSONObject(jsonTxt);
        return json;
    }

    public static Map<String, Object> toMap(JSONObject jsobj) {
        HashMap<String, Object> map = new HashMap<>();
        jsobj.keySet().forEach(k -> {
            Object pojo = toPOJO(jsobj.get(k));
            map.put(k, pojo);
        });
        return map;
    }

    public static Object toPOJO(Object jsobj) {
        final String clsname = jsobj.getClass().getSimpleName();
        switch (clsname) {
            case "JSONObject":
                return toMap((JSONObject) jsobj);
            case "JSONArray":
                return toArray((JSONArray) jsobj);
            default:
                return jsobj;
        }
    }

    public static <T> T[] toArrayOfT(Object[] objs) {
        invariant(0 <= objs.length);
        T inst = castobj(objs[0]);
        return Arrays
                .stream(objs)
                .map(o -> castobj(o))
                .toArray(size -> castobj(Array.newInstance(inst.getClass(), size)));
    }

    public static Object[] toArray(JSONArray jsary) {
        return Util.<Object, Object>toArray(
                jsary,
                obj -> toPOJO(obj),
                listof -> listof.toArray(new Object[0])
        );
    }

    public static <R, T> R[] toArray(JSONArray jsary,
                                     Function<T, R> creator,
                                     Function<List<R>, R[]> toArray) {
        List<R> eles = new LinkedList<>();
        for (Iterator<Object> iter = jsary.iterator(); iter.hasNext(); ) {
            eles.add(creator.apply(downcast(iter.next())));
        }
        return toArray.apply(eles);
    }

    public static void writeJSON(String fname, Map<String, Object> map) throws IOException {
        final JSONObject jsobj = new JSONObject(map);
        try (FileWriter os = new FileWriter(fname)) {
            jsobj.write(os, 2, 0);
        }
    }

    public static <D, B> D downcast(B base) {
        return (D) base;
    }

    public static <D> D castobj(Object base) {
        return (D) base;
    }

    public static <T> T expectNull(T x) {
        if (x != null) {
            throw new RuntimeException("Expect null");
        }
        return x;
    }

    public static <T> T expectNonNull(T x) {
        if (x == null) {
            throw new RuntimeException("Expect non-null");
        }
        return x;
    }

    public static boolean isNonNull(Object obj) {
        return (!isNull(obj));
    }

    public static void expectNever(String msg) {
        invariant(false, msg);
    }

    public static void expectNever() {
        expectNever("never expect to get here");
    }

    public static void invariant(boolean test, String msg) {
        if (!test) {
            throw new RuntimeException(msg);
        }
    }

    public static void invariant(boolean test) {
        invariant(test, "invariant failed");
    }

    public static <T, R> R invariantThen(T obj, Function<T, Boolean> test, Function<T, R> func) {
        invariant(test.apply(obj));
        return func.apply(obj);
    }

    public static <T> Stream<T> findMatches(T[] lookHere, Collection<T> from) {
        return from.stream().filter(ele -> 0 <= Arrays.binarySearch(lookHere, ele));
    }

    public static <T> int find(T[] items, T item) {
        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(item)) {
                return i;
            }
        }
        return -1;
    }

    public static <T> boolean contains(T[] set, T item) {
        return Arrays.asList(set).contains(item);
    }

    public static <T> boolean containsAny(T[] lookHere, Collection<T> from) {
        return from.stream().map(ele -> {
            return 0 <= Arrays.binarySearch(lookHere, ele);
        }).reduce(false, (acc, v) -> acc || v);
    }

    public static String toHHMMSS(Duration duration) {
        return toHHMMSS(duration, false);
    }

    public static String toHHMMSS(Duration durationx, boolean useDays) {
        long dd = -1, hh, mm, ss;
        Duration duration = durationx;
        if (useDays) {
            dd = duration.toDays();
            duration = duration.minusDays(dd);
        }
        hh = duration.toHours();
        duration = duration.minusHours(hh);
        mm = duration.toMinutes();
        duration = duration.minusMinutes(mm);
        ss = TimeUnit.MILLISECONDS.toSeconds(duration.toMillis());
        String res;
        if (0 < dd) {
            res = String.format("%dd:%02d:%02d:%02d", dd, hh, mm, ss);
        } else {
            res = String.format("%d:%02d:%02d", hh, mm, ss);
        }
        return res;
    }

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("ddMMMyy-HH:mm:ss");

    public static String getLocalDateTime() {
        return getLocalDateTime(DATE_TIME_FORMATTER);
    }

    public static String getLocalDateTime(DateTimeFormatter formatter) {
        final LocalDateTime now = LocalDateTime.now();
        return now.format(formatter);
    }

    public static class Pair<T1, T2> {
        public Pair(T1 v1, T2 v2) {
            this.v1 = v1;
            this.v2 = v2;
        }

        public Pair() {
            this(null, null);
        }

        public T1 v1;
        public T2 v2;
    }

    /**
     * Look for fileName in paths.
     *
     * @param paths    ordered paths to search.
     * @param fileName file to find.
     * @return path to first found or null (if not found).
     */
    public static File findFile(String[] paths, String fileName) {
        for (String path : paths) {
            File fpath = new File(path, fileName);
            if (fpath.isFile())
                return fpath;
        }
        return null;
    }

    public static String join(Iterable<String> eles, String sep) {
        StringBuilder sbuf = new StringBuilder();
        for (String ele : eles) {
            if (0 < sbuf.length()) {
                sbuf.append(sep);
            }
            sbuf.append(ele);
        }
        return sbuf.toString();
    }

    public static String join(String[] eles) {
        return join(eles, " ");
    }

    public static String join(String[] eles, String sep) {
        return join(Arrays.asList(eles), sep);
    }

    public static String getString(Map<String, Object> opts, String key) {
        Object val = opts.get(key);
        if (isNull(val)) return null;
        if (val instanceof Iterable) {
            Iterable<Object> coll = downcast(val);
            StringBuffer buf = new StringBuffer();
            for (Object ele : coll) {
                if (0 < buf.length()) {
                    buf.append(' ');
                }
                buf.append(ele.toString());
            }
            return buf.toString();
        } else {
            return val.toString();
        }
    }

    //Thanx to: https://softwarecave.org/2018/03/24/delete-directory-with-contents-in-java/
    public static void rmRfDirectory(File dir) throws IOException {
        final Path path = toPath(dir);
        FileVisitor visitor = new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) {
                    throw exc;
                }
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(path, visitor);
    }

    public static String unique(String s, String splitRex, String join) {
        List<String> opts = new ArrayList<>(Arrays.asList(s.split(splitRex)));
        String uniq = opts
                .stream()
                .distinct()
                .collect(Collectors.joining(join));
        return uniq;
    }

    public static String upcase(String s) {
        return applyIfNotNull(s, t -> t.toUpperCase());
    }

    public static String downcase(String s) {
        return applyIfNotNull(s, t -> t.toLowerCase());
    }

    /**
     * Set file permissions.
     * @param f file.
     * @param perms (e.g. rw-r--r--)
     */
    public static Path setFilePermissions(File f, String perms) throws IOException {
        Set<PosixFilePermission> permissions = PosixFilePermissions.fromString(perms);
        return setPosixFilePermissions(toPath(f), permissions);
    }

    public static boolean fileIsReadable(String fileName) {
        return Files.isReadable(toPath(new File(fileName)));
    }

    public static <T> T[] arrayFill(T[] ar, T val) {
        Arrays.fill(ar, val);
        return ar;
    }
}
