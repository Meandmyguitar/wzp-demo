package com.wzp.util.commons;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SuppressWarnings({"WeakerAccess", "unused"})
public class HttpUtils {

    public static String basicAuth(String user, String password) {
        try {
            String a = URLEncoder.encode(user, "utf-8");
            String b = URLEncoder.encode(password, "utf-8");
            String t = a + ":" + b;
            return "Basic " +  Base64.getEncoder().encodeToString(t.getBytes(StandardCharsets.UTF_8));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String post(URI uri) throws IOException {
        return post(uri, Collections.emptyMap());
    }

    public static String post(URI uri, Map<String, String> param) throws IOException {
        return post(uri, param, Collections.emptyMap());
    }

    public static String post(URI uri, Map<String, String> param, Map<String, String> header) throws IOException {
        return post(uri, param, header, StandardCharsets.UTF_8);
    }

    public static String post(URI uri, Map<String, String> param, Map<String, String> header, Charset charset) throws IOException {

        HttpPost post = new HttpPost(uri);
        post.setEntity(createEntity(charset, param));
        return executeString(post, header, charset);
    }

    public static String get(URI uri) throws IOException {
        return get(uri, Collections.emptyMap());
    }

    public static String get(URI uri, Map<String, String> param) throws IOException {
        return get(uri, param, Collections.emptyMap());
    }

    public static String get(URI uri, Map<String, String> param, Map<String, String> header) throws IOException {
        return get(uri, param, header, StandardCharsets.UTF_8);
    }

    public static String get(URI uri, Map<String, String> param, Map<String, String> header, Charset charset) throws IOException {

        if (uri.getQuery() != null) {
            throw new IllegalArgumentException("url不能存在query part");
        }

        List<BasicNameValuePair> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : param.entrySet()) {
            list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        String url = uri.toString() + "?" + URLEncodedUtils.format(list, charset);

        HttpGet get = new HttpGet(url);
        return executeString(get, header, charset);
    }


    private static UrlEncodedFormEntity createEntity(Charset charset, Map<String, String> param) {
        List<BasicNameValuePair> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : param.entrySet()) {
            list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        return new UrlEncodedFormEntity(list, charset);
    }

    private static CloseableHttpClient createClient() {
        return new HttpClientBuilderHelper().trustAny(true).build();
    }

    public static String executeString(HttpUriRequest request, Map<String, String> header, Charset charset) throws IOException {
        return execute(request, header, resp -> IOUtils.toString(resp.getEntity().getContent(), charset.name()));
    }

    public static byte[] executeBytes(HttpUriRequest request, Map<String, String> header) throws IOException {
        return execute(request, header, resp -> IOUtils.toByteArray(resp.getEntity().getContent()));
    }

    public static <T> T execute(HttpUriRequest request, Map<String, String> header, HttpFunction<T> callback) throws IOException {
        try (CloseableHttpClient client = createClient()) {
            return execute(client, request, header, callback);
        }
    }

    private static <T> T execute(CloseableHttpClient client, HttpUriRequest request, Map<String, String> header, HttpFunction<T> callback) throws IOException {
        if (header != null) {
            header.forEach(request::setHeader);
        }
        try (CloseableHttpResponse resp = client.execute(request)) {
            return callback.apply(resp);
        }
    }

    @FunctionalInterface
    public interface HttpFunction<T> {
        T apply(CloseableHttpResponse response) throws IOException;
    }

    public static void main(String[] args) throws URISyntaxException, IOException {

        String s = get(new URI("https://www.baidu.com"));
        System.out.println(s);
    }
}
