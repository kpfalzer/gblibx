
package gblibx;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static gblibx.Util.*;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

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
     * @param host   hostname.
     * @param port   port.
     * @param path   path.json.
     * @param params key=val...
     * @return response.
     * @throws Exception
     */
    public static Map<String,Object> get(String host, int port, String path, String... params) throws Exception {
        invariant(path.endsWith(".json"));
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
            final URL url = new URL("http", host, port, npath.toString());
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
     * @param host hostname.
     * @param port port.
     * @param path path.json
     * @param vals key+val set.
     * @return response.
     * @throws Exception
     */
    public static Map<String,Object> postJSON(String host, int port, String path, Map<String, Object> vals) throws Exception {
        //https://stackoverflow.com/questions/3324717/sending-http-post-request-in-java
        //https://stackoverflow.com/questions/7181534/http-post-using-json-in-java
        invariant(path.endsWith(".json"));
        final JSONObject json = new JSONObject(vals);
        final URL url;
        try {
            url = new URL("http", host, port, path);
            final HttpURLConnection http = downcast(url.openConnection());
            http.setRequestMethod("POST");
            http.setRequestProperty("User-Agent", __USER_AGENT);
            http.setRequestProperty("Accept-Charset", __CHARSET);
            final byte[] data = json.toString().getBytes(StandardCharsets.UTF_8);
            http.setFixedLengthStreamingMode(data.length);
            http.setRequestProperty("Content-Type", "application/json;charset=" + __CHARSET);
            http.setDoOutput(true);  //todo: need this?
            try (OutputStream os = http.getOutputStream()) {
                os.write(data);
                os.flush();
            }
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
    public static Map<String,Object> postJSON(String host, int port, String path, Object... keyVals) throws Exception {
        invariant(isEven(keyVals.length));
        Map<String, Object> kvs = new HashMap<>();
        for (int i = 0; i < keyVals.length; i += 2) {
            String key = castobj(keyVals[i]);
            Object val = keyVals[i + 1];
            kvs.put(key, val);
        }
        return postJSON(host, port, path, kvs);
    }

    public static Map<String,Object> getResponse(HttpURLConnection http) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(http.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        final String str = response.toString();
        final JSONObject json = new JSONObject(str);
        return toMap(json);
    }

    private static final String __USER_AGENT = "Mozilla/5.0";
    private static final String __CHARSET = "UTF-8";
}
