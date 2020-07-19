package com.wzp.util.hibernate;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

public abstract class AESCiphertextConverter extends AbstractCiphertextConverter {

    @Override
    final protected String getAlgorithm() {
        return "AES/ECB/PKCS5Padding";
    }

    @Override
    final protected Key getKey() {
        return new SecretKeySpec(Base64.getDecoder().decode(getBase64Key()), "AES");
    }

    abstract protected String getBase64Key();
}
