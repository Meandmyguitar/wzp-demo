package com.wzp.util.etc;

import javax.net.ssl.KeyManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;

public class SslUtils {

	public static KeyStore toKeyStore(InputStream in, String password) throws GeneralSecurityException, IOException {
		KeyStore ks;
		try {
			ks = KeyStore.getInstance("PKCS12");
			ks.load(in, password.toCharArray());
			return ks;
		} catch (KeyStoreException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public static KeyManagerFactory toKeyManagerFactory(InputStream in, String password) throws GeneralSecurityException, IOException {
		KeyStore ks = toKeyStore(in, password);
		ks.load(in, password.toCharArray());
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(ks, password.toCharArray());
		return kmf;
	}

}
