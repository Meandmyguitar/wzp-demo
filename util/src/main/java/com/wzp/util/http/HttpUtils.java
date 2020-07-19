package com.wzp.util.http;

import com.wzp.util.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

public class HttpUtils {

	public static String post(URL url, Charset charset, Map<String, String> param) throws IOException {
		
		SimpleHttpClient client = null;
		CloseableHttpResponse resp = null;
		try {
			client = new HttpClientBuilderHelper().trustAny().charset(charset).simple();
			resp = client.post(url, param);
			return IOUtils.toString(resp.getEntity().getContent(), charset);
		} finally {
			IOUtils.closeQuietly(resp);
			IOUtils.closeQuietly(client);
		}
	}
}
