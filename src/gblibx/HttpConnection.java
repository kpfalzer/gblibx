
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static gblibx.Util.*;
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
     * @param host hostname (can be null if path is complete url).
     * @param port port.
     * @param path path.json
     * @param vals key+val set.
     * @return response.
     * @throws Exception
     */
    public static Map<String, Object> postJSON(String host, int port, String path, Map<String, Object> vals) throws Exception {
        //https://stackoverflow.com/questions/3324717/sending-http-post-request-in-java
        //https://stackoverflow.com/questions/7181534/http-post-using-json-in-java
        final JSONObject json = new JSONObject(vals);
        final URL url;
        try {
            url = (isNonNull(host))
                    ? new URL("http", host, port, path)
                    : new URL(path);
            final HttpURLConnection http = downcast(url.openConnection());
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
            checkResponse(http);
            return getResponse(http);
        } catch (IOException e) {
            throw new Exception(e);
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
        Map<String, Object> kvs = new HashMap<>();
        for (int i = 0; i < keyVals.length; i += 2) {
            String key = castobj(keyVals[i]);
            Object val = keyVals[i + 1];
            kvs.put(key, val);
        }
        return postJSON(host, port, path, kvs);
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
}
