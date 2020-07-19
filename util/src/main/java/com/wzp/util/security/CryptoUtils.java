package com.wzp.util.security;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.Key;

/**
 * 对称加密工具类
 *
 */
public class CryptoUtils {

	private static final Charset charset = Charset.forName("utf-8");

	public static final String ALGORITHM_AES = "AES";
	
	public static Cipher getRsaEncryptCipher(String name, String publicKey) {
		return getRsaEncryptCipher(name, Base64.decodeBase64(publicKey));
	}
	
	public static Cipher getRsaEncryptCipher(String name, byte[] publicKey) {
		try {
			Key secretKey = SignatureUtils.getRsaX509PublicKey(publicKey);
			return createEncryptCipher(name, secretKey);
		} catch (GeneralSecurityException e) {
			throw new CryptoException("创建加密算法失败:" + name, e);
		}
	}
	
	public static Cipher getRsaDecryptCipher(String name, String privateKey) {
		return getRsaDecryptCipher(name, Base64.decodeBase64(privateKey));
	}
	
	public static Cipher getRsaDecryptCipher(String name, byte[] privateKey) {
		try {
			Key secretKey = SignatureUtils.getRsaPkcs8PrivateKey(privateKey);
			return createDecryptCipher(name, secretKey);
		} catch (GeneralSecurityException e) {
			throw new CryptoException("创建加密算法失败:" + name, e);
		}
	}

	/**
	 * 使用特定的算法和算法密钥创建加密Cipher对象
	 * @param algorithm 加密算法
	 * @param key 算法密钥
	 * @return
	 */
	public static Cipher createEncryptCipher(String algorithm, byte[] key) {
		return createEncryptCipher(algorithm, new SecretKeySpec(key, algorithm));
	}
	
	/**
	 * 使用特定的算法和算法密钥创建加密Cipher对象
	 * @param algorithm 加密算法
	 * @param key 算法密钥
	 * @return
	 */
	public static Cipher createDecryptCipher(String algorithm, byte[] key) {
		return createDecryptCipher(algorithm, new SecretKeySpec(key, algorithm));
	}

	private static Cipher createEncryptCipher(String algorithm, Key key) {
		try {
			Cipher cipher = Cipher.getInstance(algorithm);// 创建密码器
			cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
			return cipher;
		} catch (GeneralSecurityException e) {
			throw new CryptoException("createCipher失败", e);
		}
	}

	private static Cipher createDecryptCipher(String algorithm, Key key) {
		try {
			Cipher cipher = Cipher.getInstance(algorithm);// 创建密码器
			cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
			return cipher;
		} catch (GeneralSecurityException e) {
			throw new CryptoException("createCipher失败", e);
		}
	}

	/**
	 * 使用特定的算法和算法密钥加密明文
	 * @param algorithm 加密算法
	 * @param key 密钥
	 * @param data 明文
	 * @return 密文
	 */
	public static byte[] encrypt(String algorithm, byte[] key,
			byte[] data) {

		try {
			Cipher cipher = createEncryptCipher(algorithm, key);
			byte[] result = cipher.doFinal(data);
			return result; // 加密
		} catch (GeneralSecurityException e) {
			throw new CryptoException("加密失败", e);
		}
	}
	
	public static byte[] doFinal(Cipher cipher,
			byte[] data) {
		try {
			return cipher.doFinal(data);
		} catch (GeneralSecurityException e) {
			throw new CryptoException("加密失败", e);
		}
	}
	
	/**
	 * 使用utf8加密文本信息,返回base64密文
	 * @param algorithm 加密算法
	 * @param password 密钥
	 * @param data 明文
	 * @return base64密文
	 */
	public static String encryptBase64(String algorithm, String password, String data) {
		byte[] key = password.getBytes(charset);
		return Base64.encodeBase64String(encrypt(algorithm, key, data.getBytes(charset)));
	}
	
	public static String doFinalBase64(Cipher cipher, String data) {
		return Base64.encodeBase64String(doFinal(cipher, Base64.decodeBase64(data)));
	}
	
	/**
	 * 使用特定的算法和算法密钥解密明文
	 * @param algorithm 加密算法
	 * @param key 密钥
	 * @param data 密文
	 * @return 明文
	 */
	public static byte[] decrypt(String algorithm, byte[] key,
			byte[] data) {
		try {
			Cipher cipher = createDecryptCipher(algorithm, key);
			byte[] result = cipher.doFinal(data);
			return result; // 解密
		} catch (GeneralSecurityException e) {
			throw new CryptoException("解密失败", e);
		}
	}
	
	/**
	 * 解密base64密文，返回文本的明文信息
	 * @param algorithm 加密算法
	 * @param password 密钥
	 * @param base64 base64密文
	 * @return 明文
	 */
	public static String decryptBase64(String algorithm, String password, String base64) {
		byte[] key = password.getBytes(charset);
		return new String(decrypt(algorithm, key, Base64.decodeBase64(base64)), charset);
	}
}
