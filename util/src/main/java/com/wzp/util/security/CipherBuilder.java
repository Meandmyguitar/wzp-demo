
package com.wzp.util.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CipherBuilder {
    public CipherBuilder() {
    }

    public static CipherBuilder.CipherBasic rsa() {
        return new CipherBuilder.Builder("RSA", 1, (key, encrypt) -> {
            KeyFactory keyFactory = null;

            try {
                keyFactory = KeyFactory.getInstance("RSA");
            } catch (NoSuchAlgorithmException var4) {
                throw new IllegalStateException(var4);
            }

            return encrypt ? keyFactory.generatePublic(new X509EncodedKeySpec(key)) : keyFactory.generatePublic(new PKCS8EncodedKeySpec(key));
        });
    }

    public static CipherBuilder.PaddingBlockIV desede() {
        return new CipherBuilder.Builder("DESede", 1, (key, encrypt) -> {
            return new SecretKeySpec(key, "DESede");
        });
    }

    public static CipherBuilder.PaddingBlock aes() {
        return new CipherBuilder.Builder("AES", 1, (key, encrypt) -> {
            return new SecretKeySpec(key, "AES");
        });
    }

    interface KeyConvertor {
        Key convert(byte[] var1, boolean var2) throws InvalidKeySpecException;
    }

    public interface PaddingBlockIV extends CipherBuilder.PaddingBlock, CipherBuilder.IV {
        CipherBuilder.PaddingBlockIV key(byte[] var1) throws InvalidKeySpecException;

        CipherBuilder.PaddingBlockIV pkcs5();

        CipherBuilder.PaddingBlockIV pkcs7();

        CipherBuilder.PaddingBlockIV cbc();

        CipherBuilder.PaddingBlockIV ecb();

        CipherBuilder.PaddingBlockIV iv(byte[] var1);
    }

    public interface PaddingBlock extends CipherBuilder.Block, CipherBuilder.Padding, CipherBuilder.CipherBasic {
        CipherBuilder.PaddingBlock key(byte[] var1) throws InvalidKeySpecException;

        CipherBuilder.PaddingBlock pkcs5();

        CipherBuilder.PaddingBlock pkcs7();

        CipherBuilder.PaddingBlock cbc();

        CipherBuilder.PaddingBlock ecb();
    }

    public interface Block {
        CipherBuilder.Block cbc();

        CipherBuilder.Block ecb();
    }

    public interface Padding {
        CipherBuilder.Padding pkcs5();

        CipherBuilder.Padding pkcs7();
    }

    public interface IV {
        CipherBuilder.IV iv(byte[] var1);
    }

    public interface CipherBasic {
        CipherBuilder.CipherBasic key(byte[] var1) throws InvalidKeySpecException;

        Cipher encrypt() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidKeySpecException;

        Cipher decrypt() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidKeySpecException;
    }

    static class Builder implements CipherBuilder.PaddingBlockIV {
        private String algorithm;
        private String blockMode;
        private String padding;
        private byte[] key;
        private byte[] iv;
        private CipherBuilder.KeyConvertor keyFunction;

        public Builder() {
        }

        Builder(String algorithm, int mode, CipherBuilder.KeyConvertor keyFunction) {
            this.algorithm = algorithm;
            this.keyFunction = keyFunction;
        }

        public CipherBuilder.Builder pkcs5() {
            this.padding = "PKCS5Padding";
            return this;
        }

        public CipherBuilder.Builder pkcs7() {
            this.padding = "PKCS7Padding";
            return this;
        }

        public CipherBuilder.Builder cbc() {
            this.blockMode = "CBC";
            return this;
        }

        public CipherBuilder.Builder ecb() {
            this.blockMode = "ECB";
            return this;
        }

        public CipherBuilder.Builder iv(byte[] iv) {
            this.iv = Arrays.copyOf(iv, iv.length);
            return this;
        }

        public CipherBuilder.Builder key(byte[] key) {
            this.key = Arrays.copyOf(key, key.length);
            return this;
        }

        public Cipher encrypt() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidKeySpecException {
            Cipher cipher = this.getCipher();
            Key k = this.keyFunction.convert(this.key, true);
            if (this.iv != null) {
                cipher.init(1, k, new IvParameterSpec(this.iv));
            } else {
                cipher.init(1, k);
            }

            return cipher;
        }

        public Cipher decrypt() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, InvalidKeySpecException {
            Cipher cipher = this.getCipher();
            Key k = this.keyFunction.convert(this.key, false);
            if (this.iv != null) {
                cipher.init(2, k, new IvParameterSpec(this.iv));
            } else {
                cipher.init(2, k);
            }

            return cipher;
        }

        private Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
            String s = this.algorithm;
            if (this.blockMode != null || this.padding != null) {
                if (this.blockMode != null) {
                    s = s + "/" + this.blockMode;
                } else {
                    s = s + "/ECB";
                }

                if (this.padding != null) {
                    s = s + "/" + this.padding;
                } else {
                    s = s + "/PKCS5Padding";
                }
            }

            return Cipher.getInstance(s);
        }
    }
}
