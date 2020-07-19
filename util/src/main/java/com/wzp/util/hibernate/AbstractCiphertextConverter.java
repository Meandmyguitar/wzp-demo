package com.wzp.util.hibernate;

import javax.crypto.Cipher;
import javax.persistence.AttributeConverter;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Base64;

/**
 * 加密字符转换
 */
public abstract class AbstractCiphertextConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        try {
            String algorithm = getAlgorithm();
            Cipher cipher = Cipher.getInstance(algorithm);// 创建密码器
            cipher.init(Cipher.ENCRYPT_MODE, getKey());
            byte[] c = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(c);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            String algorithm = getAlgorithm();
            Cipher cipher = Cipher.getInstance(algorithm);// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, getKey());
            byte[] c = cipher.doFinal(Base64.getDecoder().decode(dbData));
            return new String(c, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    abstract protected String getAlgorithm();

    abstract protected Key getKey();
}
