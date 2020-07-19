package com.wzp.util.commons;

import org.apache.commons.io.IOUtils;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.listener.ListenerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Random;

class FtpUtilsTest {

    private int port = 20000 + new Random().nextInt() % 10000;

    private FtpServer server;

    @BeforeEach
    void setUp() throws Exception {
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory factory = new ListenerFactory();

        factory.setPort(port);

        serverFactory.addListener("default", factory.createListener());
        //todo
        serverFactory.setUserManager(new FtpUserManager(null, null));

        server = serverFactory.createServer();
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void test() throws Exception {

        String data = "hello world";
        URL url = new URL("ftp://ci:123456@127.0.0.1:" + port + "/ftp-test/t2.log");

        try (OutputStream out = FtpUtils.store(url, true)) {
            IOUtils.write(data, out);
        }

        try (InputStream in = FtpUtils.retrieve(url)) {
            String s = IOUtils.toString(in);
            Assertions.assertEquals(data, s);
        }
    }
}
