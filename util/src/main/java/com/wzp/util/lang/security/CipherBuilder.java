package com.wzp.util.lang.security;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

@SuppressWarnings("ALL")
public class CipherBuilder {

    public static CipherBasic rsa() {
        return new Builder("RSA", Cipher.ENCRYPT_MODE, (key, encrypt) -> {
            KeyFactory keyFactory = null;
            try {
                keyFactory = KeyFactory.getInstance("RSA");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
            if (encrypt) {
                return keyFactory.generatePublic(new X509EncodedKeySpec(key));
            } else {
                return keyFactory.generatePublic(new PKCS8EncodedKeySpec(key));
            }
        });
    }

    public static PaddingBlockIV desede() {
        return new Builder("DESede", Cipher.ENCRYPT_MODE,
                (key, encrypt) -> new SecretKeySpec(key, "DESede"));
    }

    public static PaddingBlock aes() {
        return new Builder("AES", Cipher.ENCRYPT_MODE,
                (key, encrypt) -> new SecretKeySpec(key, "AES"));
    }

    static class Builder implements PaddingBlockIV {

        private String algorithm;

        private String blockMode;

        private String padding;

        private byte[] key;

        private byte[] iv;

        private KeyConvertor keyFunction;

        public Builder() {
        }

        Builder(String algorithm, int mode, KeyConvertor keyFunction) {
            this.algorithm = algorithm;
            this.keyFunction = keyFunction;
        }

        @Override
        public Builder pkcs5() {
            padding = "PKCS5Padding";
            return this;
        }

        @Override
        public Builder pkcs7() {
            padding = "PKCS7Padding";
            return this;
        }

        @Override
        public Builder cbc() {
            blockMode = "CBC";
            return this;
        }

        @Override
        public Builder ecb() {
            blockMode = "ECB";
            return this;
        }

        @Override
        public Builder iv(byte[] iv) {
            this.iv = Arrays.copyOf(iv, iv.length);
            return this;
        }

        @Override
        public Builder key(byte[] key) {
            this.key = Arrays.copyOf(key, key.length);
            return this;
        }

        @Override
        public Cipher encrypt() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidKeySpecException {
            Cipher cipher = getCipher();
            Key k = keyFunction.convert(key, true);
            if (iv != null) {
                cipher.init(Cipher.ENCRYPT_MODE, k, new IvParameterSpec(iv));
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, k);
            }
            return cipher;
        }

        @Override
        public Cipher decrypt() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidKeySpecException {
            Cipher cipher = getCipher();
            Key k = keyFunction.convert(key, false);
            if (iv != null) {
                cipher.init(Cipher.DECRYPT_MODE, k, new IvParameterSpec(iv));
            } else {
                cipher.init(Cipher.DECRYPT_MODE, k);
            }
            return cipher;
        }

        private Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
            String s = algorithm;
            if (blockMode != null || padding != null) {
                if (blockMode != null) {
                    s += "/" + blockMode;
                } else {
                    s += "/" + "ECB";
                }
                if (padding != null) {
                    s += "/" + padding;
                } else {
                    s += "/" + "PKCS5Padding";
                }
            }
            return Cipher.getInstance(s);// 创建密码器
        }
    }

    public interface CipherBasic {

        CipherBasic key(byte[] key) throws InvalidKeySpecException;

        Cipher encrypt() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidKeySpecException;

        Cipher decrypt() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidKeySpecException;
    }

    public interface IV {

        IV iv(byte[] iv);
    }

    public interface Padding {

        Padding pkcs5();

        Padding pkcs7();
    }

    public interface Block {

        Block cbc();

        Block ecb();
    }

    public interface PaddingBlock extends Block, Padding, CipherBasic {

        @Override
        PaddingBlock key(byte[] key) throws InvalidKeySpecException;

        @Override
        PaddingBlock pkcs5();

        @Override
        PaddingBlock pkcs7();

        @Override
        PaddingBlock cbc();

        @Override
        PaddingBlock ecb();
    }


    public interface PaddingBlockIV extends PaddingBlock, IV {

        @Override
        PaddingBlockIV key(byte[] key) throws InvalidKeySpecException;

        @Override
        PaddingBlockIV pkcs5();

        @Override
        PaddingBlockIV pkcs7();

        @Override
        PaddingBlockIV cbc();

        @Override
        PaddingBlockIV ecb();

        @Override
        PaddingBlockIV iv(byte[] iv);
    }

    interface KeyConvertor {

        Key convert(byte[] key, boolean encrypt) throws InvalidKeySpecException;
    }
}
