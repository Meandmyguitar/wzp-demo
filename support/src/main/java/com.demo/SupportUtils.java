package com.demo;

import com.rabbitmq.client.ConnectionFactory;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;

public class SupportUtils {

    public static ConnectionFactory createConnectionFactory(URI uri) {
        try {
            String userInfo = uri.getRawUserInfo();
            String user = null;
            String password = null;
            if (userInfo != null) {
                String[] arr = userInfo.split(":");
                if (arr.length >= 1) {
                    user = URLDecoder.decode(arr[0], "utf-8");
                }
                if (arr.length >= 2) {
                    password = URLDecoder.decode(arr[1], "utf-8");
                }
            }

            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(uri.getHost());
            connectionFactory.setPort(uri.getPort());
            connectionFactory.setUsername(user);
            connectionFactory.setPassword(password);
            connectionFactory.setVirtualHost(uri.getPath());
            connectionFactory.useNio();
            return connectionFactory;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
