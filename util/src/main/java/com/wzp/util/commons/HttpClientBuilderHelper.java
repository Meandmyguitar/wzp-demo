package com.wzp.util.commons;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@SuppressWarnings({"WeakerAccess", "unused"})
public class HttpClientBuilderHelper {

    private boolean trustAny;

    private boolean keepalive;

    private Integer connectTimeout;

    private Integer readTimeout;

    private Boolean connectionReuse;

    private String httpProxy;

    /**
     * 在https下，信任任何服务端证书
     */
    public HttpClientBuilderHelper trustAny(boolean value) {
        trustAny = value;
        return this;
    }

    /**
     * 设置超时时间
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

    /**
     * 是否允许keepalive
     */
    public HttpClientBuilderHelper keepAlive(boolean keepalive) {
        this.keepalive = keepalive;
        return this;
    }

    /**
     * 指定HTTP代理服务器
     */
    public HttpClientBuilderHelper proxy(String httpProxy) {
        this.httpProxy = httpProxy;
        return this;
    }

    public void config(HttpClientBuilder builder) {

        try {
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

            if (!keepalive) {
                builder.setKeepAliveStrategy((response, context) -> -1);
            }

            // 最大连接数不设上限
            builder.setMaxConnPerRoute(Integer.MAX_VALUE).setMaxConnTotal(Integer.MAX_VALUE);

            if (trustAny) {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new SecureRandom());

                builder.setSSLContext(sslContext);
                builder.setSSLHostnameVerifier(new TrustAnyHostnameVerifier());
            }

            builder.disableAutomaticRetries();
            if (connectionReuse != null) {
                if (connectionReuse) {
                    builder.setConnectionReuseStrategy(new DefaultConnectionReuseStrategy());
                } else {
                    builder.setConnectionReuseStrategy(new NoConnectionReuseStrategy());
                }
            }

            if (httpProxy != null) {
                String[] arr = httpProxy.split(":");
                builder.setProxy(new HttpHost(arr[0], Integer.parseInt(arr[1])));
            }
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException(e);
        }
    }

    public HttpClientBuilder config() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        config(builder);
        return builder;
    }

    public CloseableHttpClient build() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        config(builder);
        return builder.build();
    }

    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    private static class TrustAnyTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }
    }
}
