package index.alchemy.util;

import java.io.*;
import java.net.*;
import java.net.Proxy.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongConsumer;

import index.project.version.annotation.Omega;

@Omega
public class Http {
    
    public static final class Code {
        
        public static final int
                CONTINUE = 100,
                SWITCHING_PROTOCOLS = 101,
                PROCESSING = 102,
                OK = 200,
                CREATED = 201,
                ACCEPTED = 202,
                NON_AUTHORITATIVE_INFORMATION = 203,
                NO_CONTENT = 204,
                RESET_CONTENT = 205,
                PARTIAL_CONTENT = 206,
                MULTI_STATUS = 207,
                MULTIPLE_CHOICES = 300,
                MOVED_PERMANENTLY = 301,
                MOVE_TEMPORARILY = 302,
                SEE_OTHER = 303,
                NOT_MODIFIED = 304,
                USE_PROXY = 305,
                SWITCH_PROXY = 306,
                TEMPORARY_REDIRECT = 307,
                BAD_REQUEST = 400,
                UNAUTHORIZED = 401,
                PAYMENT_REQUIRED = 402,
                FORBIDDEN = 403,
                NOT_FOUND = 404,
                METHOD_NOT_ALLOWED = 405,
                NOT_ACCEPTABLE = 406,
                PROXY_AUTHENTICATION_REQUIRED = 407,
                REQUEST_TIMEOUT = 408,
                CONFLICT = 409,
                GONE = 410,
                LENGTH_REQUIRED = 411,
                PRECONDITION_FAILED = 412,
                REQUEST_ENTITY_TOO_LARGE = 413,
                REQUEST_URI_TOO_LONG = 414,
                UNSUPPORTED_MEDIA_TYPE = 415,
                REQUESTED_RANGE_NOT_SATISFIABLE = 416,
                EXPECTATION_FAILED = 417,
                HERE_ARE_TOO_MANY_CONNECTIONS_FROM_YOUR_INTERNET_ADDRESS = 422,
                UNPROCESSABLE_ENTITY = 423,
                LOCKED = 424,
                FAILED_DEPENDENCY = 425,
                UNORDERED_COLLECTION = 426,
                UPGRADE_REQUIRED = 449,
                RETRY_WITH = 500,
                INTERNAL_SERVER_ERROR = 501,
                NOT_IMPLEMENTED = 502,
                BAD_GATEWAY = 503,
                SERVICE_UNAVAILABLE = 504,
                GATEWAY_TIMEOUT = 505,
                HTTP_VERSION_NOT_SUPPORTED = 506,
                VARIANT_ALSO_NEGOTIATES = 507,
                INSUFFICIENT_STORAGE = 509,
                BANDWIDTH_LIMIT_EXCEEDED = 510,
                NOT_EXTENDED = 600;
        
    }
    
    public static final class Result {
        
        private int code;
        private Map<String, List<String>> head;
        private String text;
        private Throwable t;
        
        private Result setThrowable(Throwable t) {
            this.t = t;
            return this;
        }
        
        private Result setCode(int code) {
            this.code = code;
            return this;
        }
        
        private Result setText(String text) {
            this.text = text;
            return this;
        }
        
        private Result setHead(Map<String, List<String>> head) {
            this.head = head;
            return this;
        }
        
        public Throwable getThrowable() { return t; }
        
        public int getCode() { return code; }
        
        public String getText() { return text; }
        
        public Map<String, List<String>> getHead() { return head; }
        
        public boolean isEmpty() {
            return head.size() == 0 && text.isEmpty();
        }
        
        public static final Result getEmpty() {
            return new Result().setHead(new HashMap<String, List<String>>()).setText("");
        }
        
    }
    
    public static Proxy shadowsocks = new Proxy(Type.HTTP, new InetSocketAddress("127.0.0.1", 1080));
    
    public static final int BUFFER_SIZE = 4096;
    
    public static final String GET = "GET", POST = "POST", CODE = "utf-8", ACCEPT = "Accept", ACCEPT_ENCODING = "Accept-Encoding",
            ACCEPT_LANGUAGE = "Accept-Language", COOKIE = "Cookie", SET_COOKIE = "Set-Cookie", DNT = "DNT", HOST = "Host",
            PROXY_CONNECTION = "Proxy-Connection", UPGRADE_INSECURE_REQUESTS = "Upgrade-Insecure-Requests",
            USER_AGENT = "User-Agent", CACHE_CONTROL = "Cache-Control", CONNECTION = "Connection",
            CONTENT_ENCODING = "Content-Encoding", CONTENT_LENGTH = "Content-Length", CONTENT_TYPE = "Content-Type",
            DATE = "Date", EXPIRES = "Expires", PRAGMA = "Pragma", SERVER = "Server", VARY = "Vary", WARNING = "Warning";
    
    public static final String encoreUTF8(String str) {
        try { return URLEncoder.encode(str, CODE); } catch (Exception e) { return str; }
    }
    
    public static final String decoreUTF8(String str) {
        try { return URLDecoder.decode(str, CODE); } catch (Exception e) { return str; }
    }
    
    public static final File downloadFromUrl(String urlStr, String fileName, String savePath, int timeout, Proxy proxy,
                                             Map<String, String> key_val, LongConsumer callback) {
        HttpURLConnection conn;
        File dir = new File(savePath);
        if (!dir.exists())
            dir.mkdirs();
        File result = new File(dir, fileName);
        try {
            if (proxy != null)
                conn = (HttpURLConnection) new URL(urlStr).openConnection(proxy);
            else
                conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setConnectTimeout(timeout);
            if (key_val != null)
                key_val.forEach((k, v) -> conn.setRequestProperty(k, v));
            try (InputStream is = conn.getInputStream();
                 FileOutputStream fos = new FileOutputStream(new File(dir, fileName))) {
                byte[] buf = new byte[BUFFER_SIZE];
                int len = 0;
                while ((len = is.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                    if (callback != null) callback.accept(len);
                }
            }
            return result;
        } catch (Exception e) {
            result.delete();
            return null;
        }
    }
    
    public static final File downloadFromUrl(String urlStr, String fileName, String savePath, int timeout, Proxy proxy) {
        return downloadFromUrl(urlStr, fileName, savePath, timeout, proxy, null, null);
    }
    
    public static final File downloadFromUrl(String urlStr, String fileName, String savePath, int timeout) {
        return downloadFromUrl(urlStr, fileName, savePath, timeout, null, null, null);
    }
    
    public static final File downloadFromUrl(String urlStr, String fileName, String savePath) {
        return downloadFromUrl(urlStr, fileName, savePath, 3000, null, null, null);
    }
    
    public static final File downloadFromUrl(String urlStr, String fileName, String savePath, int timeout, Proxy proxy, Map<String, String> key_val) {
        return downloadFromUrl(urlStr, fileName, savePath, timeout, proxy, key_val, null);
    }
    
    public static final File downloadFromUrl(String urlStr, String fileName, String savePath, int timeout, Map<String, String> key_val) {
        return downloadFromUrl(urlStr, fileName, savePath, timeout, null, key_val, null);
    }
    
    public static final File downloadFromUrl(String urlStr, String fileName, String savePath, Map<String, String> key_val) {
        return downloadFromUrl(urlStr, fileName, savePath, 3000, null, key_val, null);
    }
    
    public static final Result get(String urlStr, Proxy proxy, int timeout, Map<String, String> key_val) {
        try {
            HttpURLConnection conn;
            if (proxy != null)
                conn = (HttpURLConnection) new URL(urlStr).openConnection(proxy);
            else
                conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setConnectTimeout(timeout);
            if (key_val != null)
                key_val.forEach((k, v) -> conn.setRequestProperty(k, v));
            return new Result().setCode(conn.getResponseCode()).setHead(conn.getHeaderFields())
                    .setText(inputStreamToString(conn.getInputStream()));
        } catch (Throwable t) {
            return Result.getEmpty().setThrowable(t);
        }
    }
    
    public static final Result get(String urlStr, Proxy proxy, Map<String, String> key_val) {
        return get(urlStr, proxy, 3000, key_val);
    }
    
    public static final Result get(String urlStr, int timeout, Map<String, String> key_val) {
        return get(urlStr, null, timeout, key_val);
    }
    
    public static final Result get(String urlStr, Proxy proxy, int timeout) {
        return get(urlStr, proxy, timeout, null);
    }
    
    public static final Result get(String urlStr, Proxy proxy) {
        return get(urlStr, proxy, 3000, null);
    }
    
    public static final Result get(String urlStr, int timeout) {
        return get(urlStr, null, timeout, null);
    }
    
    public static final Result get(String urlStr) {
        return get(urlStr, null, 3000, null);
    }
    
    public static final Result post(String urlStr, String postArgs, Proxy proxy, int timeout, Map<String, String> key_val) {
        try {
            HttpURLConnection conn;
            if (proxy != null)
                conn = (HttpURLConnection) new URL(urlStr).openConnection(proxy);
            else
                conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setConnectTimeout(timeout);
            conn.setDoOutput(true);
            conn.setRequestMethod(POST);
            if (key_val != null) key_val.forEach((k, v) -> conn.setRequestProperty(k, v));
            try (OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), CODE)) { out.write(postArgs); }
            return new Result().setCode(conn.getResponseCode()).setHead(conn.getHeaderFields())
                    .setText(inputStreamToString(conn.getInputStream()));
        } catch (Throwable t) {
            return Result.getEmpty().setThrowable(t);
        }
    }
    
    public static final Result post(String urlStr, String postArgs, Proxy proxy, Map<String, String> key_val) {
        return post(urlStr, postArgs, proxy, 3000, key_val);
    }
    
    public static final Result post(String urlStr, String postArgs, int timeout, Map<String, String> key_val) {
        return post(urlStr, postArgs, null, timeout, key_val);
    }
    
    public static final Result post(String urlStr, String postArgs, Map<String, String> key_val) {
        return post(urlStr, postArgs, null, 3000, key_val);
    }
    
    public static final String inputStreamToString(InputStream in) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[BUFFER_SIZE];
        int count = -1;
        while ((count = in.read(data, 0, BUFFER_SIZE)) != -1)
            outStream.write(data, 0, count);
        return new String(outStream.toByteArray(), CODE);
    }
    
    public static final String key_valToString(Map<String, String> lss) {
        StringBuffer sb = new StringBuffer("?");
        lss.forEach((k, v) -> sb.append(encoreUTF8(k) + "=" + encoreUTF8(v) + "&"));
        return sb.toString();
    }
    
}
