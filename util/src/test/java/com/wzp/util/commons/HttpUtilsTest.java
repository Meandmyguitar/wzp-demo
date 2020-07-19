package com.wzp.util.commons;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HttpUtilsTest {

    @Test
    void testBasicAuth() {

        String t = HttpUtils.basicAuth("hello", "world");
        Assertions.assertEquals("Basic aGVsbG86d29ybGQ=", t);
    }
}
