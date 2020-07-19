package com.wzp.util.lang.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;

class SecurityUtilsTest {

    @Test
    void test() throws Exception {

        String plain = "hello";
        String algorithm = "SHA1withRSA";

        KeyPair pair = SecurityUtils.generateRsaKeyPair(1024);
        byte[] sign = SecurityUtils.sign(algorithm, pair.getPrivate(), plain.getBytes());
        Assertions.assertTrue(SecurityUtils.verify(algorithm, pair.getPublic(), plain.getBytes(), sign));
    }
}
