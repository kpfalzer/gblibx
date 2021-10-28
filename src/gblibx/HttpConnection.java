
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

import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static gblibx.Util.*;
import static java.lang.Thread.sleep;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Objects.isNull;

public class HttpConnection {
    public static class Exception extends java.lang.Exception {
        public Exception(int code, String message) {
            super(String.format("%d: %s", code, message));
        }

        public Exception(java.io.IOException ex) {
            super(ex);
        }
    }

    public static String encode(String in) throws UnsupportedEncodingException {
        return URLEncoder.encode(in, __CHARSET);
    }

    /**
     * GET request.
     *
     * @param host   hostname (null if path is complete url).
     * @param port   port.
     * @param path   path.json.
     * @param params key=val...
     * @return response.
     * @throws Exception
     */
    public static Map<String, Object> get(String host, int port, String path, String... params) throws Exception {
        try {
            StringBuffer npath = new StringBuffer(path);
            {
                char sep = '?';
                for (String kv : params) {
                    int eqix = kv.indexOf('=');
                    String k = kv.substring(0, eqix);
                    String v = encode(kv.substring(eqix + 1));
                    npath.append(sep).append(k).append('=').append(v);
                    sep = '&';
                }
            }
            final URL url = (isNonNull(host))
                    ? new URL("http", host, port, npath.toString())
                    : new URL(path);
            final HttpURLConnection http = downcast(url.openConnection());
            http.setRequestMethod("GET");
            http.setRequestProperty("User-Agent", __USER_AGENT);
            final int responseCode = http.getResponseCode();
            final String responseMessage = http.getResponseMessage();
            switch (responseCode) {
                case HTTP_OK:   //fall through
                case HTTP_CREATED: //fall through
                    break;
                default:
                    throw new Exception(responseCode, responseMessage);
            }
            return getResponse(http);
        } catch (IOException ex) {
            throw new Exception(ex);
        }
    }

    /**
     * POST request.
     *
     * @param path url.json
     * @param vals key+val set.
     * @return response.
     * @throws Exception
     */
    public static Map<String, Object> postJSON(String path, Map<String, Object> vals)
            throws Exception {
        return postJSON(null, -1, path, vals);
    }

        /**
         * POST request.
         *
         * @param host hostname (can be null if path is complete url).
         * @param port port.
         * @param path path.json
         * @param vals key+val set.
         * @return response.
         * @throws Exception
         */
    public static Map<String, Object> postJSON(String host, int port, String path, Map<String, Object> vals)
            throws Exception {
        class Sideband {
            Map<String, Object> rval = null;
            Exception ex = null;
        }
        final Sideband sb = new Sideband();
        postJSON(host, port, path, vals, (http) -> {
            try {
                checkResponse(http);
                sb.rval = getResponse(http);
            } catch (IOException e) {
                sb.ex = new Exception(e);
            }
        });
        if (isNonNull(sb.ex)) throw sb.ex;
        return sb.rval;
    }

    public static void
    postJSON(String host, int port, String path, Map<String, Object> vals,
             Consumer<HttpURLConnection> responseHandler)
            throws Exception {
        postJSON(host, port, path, vals, responseHandler, POST_RETRY_LOOP_SEC, __POST_RETRY_NLOOP);
    }

    public static void
    postJSON(String host, int port, String path, Map<String, Object> vals,
             Consumer<HttpURLConnection> responseHandler, int retrySleepSec, int retryNTimes)
            throws Exception {
        //https://stackoverflow.com/questions/3324717/sending-http-post-request-in-java
        //https://stackoverflow.com/questions/7181534/http-post-using-json-in-java
        final JSONObject json = new JSONObject(vals);
        final URL url;
        try {
            url = (isNonNull(host))
                    ? new URL("http", host, port, path)
                    : new URL(path);
        } catch (MalformedURLException e) {
            throw new Exception(e);
        }
        //Well loop here with retry
        for (int nloop = retryNTimes; nloop > 0; --nloop) {
            HttpURLConnection http = null;
            try {
                http = downcast(url.openConnection());
                http.setRequestMethod("POST");
                http.setRequestProperty("User-Agent", __USER_AGENT);
                http.setRequestProperty("Accept-Charset", __CHARSET);
                final byte[] data = json.toString().getBytes(StandardCharsets.UTF_8);
                http.setFixedLengthStreamingMode(data.length);
                http.setDoOutput(true);
                http.setRequestProperty("Content-Type", "application/json;charset=" + __CHARSET);
                try (OutputStream os = http.getOutputStream()) {
                    os.write(data);
                    os.flush();
                }
                responseHandler.accept(http);
                return;
            } catch (IOException e) {
                if (isNonNull(http)) {
                    http.disconnect();
                }
                if (0 >= nloop)
                    throw new Exception(e);
            }
            try {
                sleep(1000 * retrySleepSec);
            } catch (InterruptedException e) {
                ;
            }
        }
    }


    /**
     * POST request.
     *
     * @param host    hostname.
     * @param port    port.
     * @param path    path.json
     * @param keyVals empty or even list: key val ...
     * @return response.
     * @throws Exception
     */
    public static Map<String, Object> postJSON(String host, int port, String path, Object... keyVals) throws Exception {
        invariant(isEven(keyVals.length));
        return postJSON(host, port, path, toMap(keyVals));
    }

    private static void checkResponse(HttpURLConnection http) throws IOException {
        if (200 == http.getResponseCode()) return;
        System.err.printf("checkResponse fail details: BEGIN{{\n");
        System.err.printf("%s getContentType=%s\n", http.getURL(),
                http.getContentType());
        int rcode = http.getResponseCode();
        System.err.printf("%d: %s\n", rcode, http.getResponseMessage());
        if (200 != rcode) {
            String resp = new BufferedReader(new InputStreamReader(http.getErrorStream()))
                    .lines().collect(Collectors.joining("\n"));
            System.err.printf("details: %s\n", resp);
        }
        for (int i = 0; i < 99; ++i) {
            String k = http.getHeaderFieldKey(i);
            if (isNull(k)) continue;
            String v = http.getHeaderField(i);
            System.err.printf("%s=%s\n", k, v);
        }
        System.err.println("}}END");
    }

    public static Map<String, Object> getResponse(HttpURLConnection http) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(http.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        String str = response.toString();
        if (!str.isEmpty() && ('{' != str.charAt(0))) {
            //TODO: should return [] or {}.
            //for now, everything is object/map.
            str = "{\"data\":" + str + "}";
        }
        final JSONObject json = new JSONObject(str);
        return toMap(json);
    }

    private static final String __USER_AGENT = "Mozilla/5.0";
    private static final String __CHARSET = "UTF-8";

    public static final int POST_RETRY_LOOP_SEC =
            Integer.parseInt(System.getProperty("gblibx.httpconnection.postRetryLoopSec", "5"));
    public static final int POST_RETRY_TOTAL_WAIT_SEC =
            Integer.parseInt(System.getProperty("gblibx.httpconnection.postRetryTotalWaitSec", "300"));
    private static final int __POST_RETRY_NLOOP =
            POST_RETRY_TOTAL_WAIT_SEC / POST_RETRY_LOOP_SEC;
}
