package com.wzp.util.lang.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

class CipherBuilderTest {

    @Test
    void testAes() throws InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
        byte[] key = "1234567812345678".getBytes();
        CipherBuilder.PaddingBlock pb = CipherBuilder.aes().key(key);
        byte[] data = pb.encrypt().doFinal(key);
        Assertions.assertArrayEquals(key, pb.decrypt().doFinal(data));

        // AES默认为ecb, pkcs5模式
        Assertions.assertArrayEquals(key, CipherBuilder.aes().key(key).ecb().pkcs5().decrypt().doFinal(data));
    }

    @Test
    void test3des() throws InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
        byte[] key = "123456788765432112345678".getBytes();
        CipherBuilder.PaddingBlock pb = CipherBuilder.desede().key(key);
        byte[] data = pb.encrypt().doFinal(key);

        System.out.println(Base64.getEncoder().encodeToString(data));

        Assertions.assertArrayEquals(key, pb.decrypt().doFinal(data));
        // 3DES默认为ecb, pkcs5模式
        Assertions.assertArrayEquals(key, CipherBuilder.desede().key(key).ecb().pkcs5().decrypt().doFinal(data));
    }

    @Test
    void test3desIv() throws InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
        byte[] key = "123456788765432112345678".getBytes();
        CipherBuilder.PaddingBlock pb = CipherBuilder.desede().key(key).cbc().iv(Arrays.copyOf(key, 8));
        byte[] data = pb.encrypt().doFinal(key);
        Assertions.assertArrayEquals(key, pb.decrypt().doFinal(data));
    }
}
