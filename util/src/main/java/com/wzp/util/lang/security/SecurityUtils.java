package com.wzp.util.lang.security;

import javax.net.ssl.KeyManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@SuppressWarnings({"unused", "WeakerAccess"})
public class SecurityUtils {

    private static final String ALGORITHM_RSA = "RSA";

    public static KeyPair generateRsaKeyPair(int keySize) {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(ALGORITHM_RSA);
            keyPairGen.initialize(keySize, new SecureRandom());
            return keyPairGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }


    public static PublicKey rsaX509PublicKey(byte[] encodedKey)
            throws InvalidKeySpecException {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
            return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }


    public static PrivateKey rsaPkcs8PrivateKey(byte[] encodedKey)
            throws InvalidKeySpecException {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA);
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }


    public static KeyStore keystorePkck12(InputStream in, String password) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(in, password.toCharArray());
        return ks;
    }

    public static KeyManagerFactory keyManagerFactoryPkcs12(InputStream in, String password) throws GeneralSecurityException, IOException {
        KeyStore ks = keystorePkck12(in, password);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, password.toCharArray());
        return kmf;
    }

    public static byte[] sign(String algorithm, PrivateKey privateKey,
                              byte[] data) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        return sign(algorithm, privateKey, input);
    }

    public static byte[] sign(String algorithm, PrivateKey privateKey,
                              InputStream data) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance(algorithm);
        signature.initSign(privateKey);
        doUpdate(signature, data);
        return signature.sign();
    }

    public static boolean verify(String algorithm, PublicKey publicKey,
                                 InputStream input, byte[] sign) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance(algorithm);
        signature.initVerify(publicKey);
        doUpdate(signature, input);
        return signature.verify(sign);
    }

    public static boolean verify(String algorithm, PublicKey publicKey, byte[] data,
                                 byte[] sign) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        return verify(algorithm, publicKey, input, sign);
    }

    private static void doUpdate(Signature signature, InputStream input)
            throws IOException, SignatureException {
        byte[] buf = new byte[4096];
        int c;
        do {
            c = input.read(buf);
            if (c > 0) {
                signature.update(buf, 0, c);
            }
        } while (c != -1);
    }
}
