package com.wzp.util.http;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.net.ssl.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class HttpClientBuilderHelper {

	private SSLContext sslContext;
	
	private HostnameVerifier hostnameVerifier;
	
	private Charset charset = Charset.forName("utf-8");
	
	private Integer connectTimeout;
	
	private Integer readTimeout;
	
	private Boolean connectionReuse;

	/**
	 * 在https下，信任任何服务端证书
	 */
	public HttpClientBuilderHelper trustAny() {
		try {
			sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, new TrustManager[] { new TrustAnyTrustManager() }, new SecureRandom());
			hostnameVerifier = new TrustAnyHostnameVerifier();
		} catch (KeyManagementException e) {
			throw new IllegalStateException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
		return this;
	}
	
	/**
	 * 设置超时时间
	 * @param connectTimeout
	 * @param readTimeout
	 * @return
	 */
	public HttpClientBuilderHelper timeout(int connectTimeout, int readTimeout) {
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
		return this;
	}
	
	/**
	 * 是否重用连接
	 */
	public HttpClientBuilderHelper resue(boolean resue) {
		this.connectionReuse = resue;
		return this;
	}
	
	public HttpClientBuilderHelper charset(Charset charset) {
		this.charset = charset;
		return this;
	}
	
	public HttpClientBuilderHelper charset(String charset) {
		this.charset = Charset.forName(charset);
		return this;
	}
	
	public void config(HttpClientBuilder builder) {
		
		// 设置超时时间		
		RequestConfig.Builder requestConfigBuild = RequestConfig.custom();
		if (connectTimeout != null) {
			requestConfigBuild.setConnectTimeout(connectTimeout);
		} else {
			requestConfigBuild.setConnectTimeout(30000);
		}
		if (readTimeout != null) {
			requestConfigBuild.setSocketTimeout(readTimeout);
		} else {
			requestConfigBuild.setSocketTimeout(60000);
		}
		builder.setDefaultRequestConfig(requestConfigBuild.build());
		
		// 最大连接数不设上限
		builder.setMaxConnPerRoute(Integer.MAX_VALUE).setMaxConnTotal(Integer.MAX_VALUE);
		
		if (sslContext != null) {
			builder.setSslcontext(sslContext);
		}
		if (hostnameVerifier != null) {
			builder.setSSLHostnameVerifier(hostnameVerifier);
		}
		
		if (connectionReuse != null) {
			if (connectionReuse) {
				builder.setConnectionReuseStrategy(new DefaultConnectionReuseStrategy());
			} else {
				builder.setConnectionReuseStrategy(new NoConnectionReuseStrategy());
			}
		}
	}
	
	public HttpClientBuilder config() {
		HttpClientBuilder builder = HttpClientBuilder.create();
		config(builder);
		return builder;
	}
	
	public SimpleHttpClient simple() {
		return new SimpleHttpClient(config().build(), charset);
	}
	
	private static class TrustAnyHostnameVerifier implements HostnameVerifier {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}
	
	private static class TrustAnyTrustManager implements X509TrustManager {

		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[] {};
		}
	}
	
	public static void main(String[] args) throws Exception {
		SimpleHttpClient client = new HttpClientBuilderHelper().trustAny().timeout(1000, 1000).simple();
		CloseableHttpResponse resp = client.get(new URL("http://www.baidu.com"));
		String s = IOUtils.toString(resp.getEntity().getContent());
		System.out.println(s);
		resp.close();
	}
}
