package com.wzp.util.http;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 简化CloseableHttpClient的操作
 */
public class SimpleHttpClient implements Closeable {

	public static final String POST = "post";

	public static final String GET = "get";

	private CloseableHttpClient client;
	
	private Charset charset;
	
	public SimpleHttpClient(CloseableHttpClient client, Charset charset) {
		this.client = client;
		this.charset = charset;
	}

	public CloseableHttpClient getClient() {
		return client;
	}

	public CloseableHttpResponse get(URL url, Map<String, String> param) throws IOException {

		List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
		for (Entry<String, String> entry : param.entrySet()) {
			list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}

		return get(url, list);
	}
	
	public CloseableHttpResponse get(URL url, List<BasicNameValuePair> param) throws IOException {
		if (url.getQuery() != null) {
			throw new IllegalArgumentException("url不能存在query part");
		}
		return get(new URL(url.toString() + "?" + URLEncodedUtils.format(param, charset)));
	}

	public CloseableHttpResponse get(URL url) throws IOException {
		HttpGet get = new HttpGet(url.toString());
		return client.execute(get);
	}

	public CloseableHttpResponse post(URL url, Map<String, String> param) throws IOException {
		List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
		for (Entry<String, String> entry : param.entrySet()) {
			list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, charset);
		return post(url, entity);
	}
	
	public CloseableHttpResponse post(URL url, String body) throws IOException {
		return post(url, new StringEntity(body, charset));
	}

	public CloseableHttpResponse post(URL url, HttpEntity entity) throws IOException {
		HttpPost post = new HttpPost(url.toString());
		if (entity != null) {
			post.setEntity(entity);
		}
		return client.execute(post);
	}

	@Override
	public void close() throws IOException {
		client.close();
	}

	public String executeString(String method, URL url, Map<String, String> param) throws IOException {
		CloseableHttpResponse resp = null;
		try {
			resp = execute(method, url, param);
			return IOUtils.toString(resp.getEntity().getContent(), charset);
		} finally {
			IOUtils.closeQuietly(resp);
		}
	}

	public CloseableHttpResponse execute(String method, URL url, Map<String, String> param) throws IOException {
		if (POST.equalsIgnoreCase(method)) {
			return post(url, param);
		} else if (GET.equalsIgnoreCase(method)) {
			return get(url, param);
		} else {
			throw new IllegalArgumentException("不支持的method:" + method);
		}
	}
}
