package com.wzp.util.security;

import java.io.IOException;
import java.io.OutputStream;
import java.security.*;

public class SignatureOutputStream extends OutputStream {

	private Signature signature;

	private OutputStream out;

	public SignatureOutputStream(String algorithm, PrivateKey privateKey, OutputStream out)
			throws NoSuchAlgorithmException, InvalidKeyException {
		super();
		signature = Signature.getInstance(algorithm);
		signature.initSign(privateKey);
		this.out = out;
	}
	
	public SignatureOutputStream(String algorithm, PrivateKey privateKey, SecureRandom random, OutputStream out)
			throws NoSuchAlgorithmException, InvalidKeyException {
		super();
		signature = Signature.getInstance(algorithm);
		signature.initSign(privateKey, random);
		this.out = out;
	}

	public SignatureOutputStream(Signature signature, OutputStream out) {
		super();
		this.signature = signature;
		this.out = out;
	}

	public byte[] sign() throws SignatureException {
		return signature.sign();
	}

	public Signature getSignature() {
		return signature;
	}

	@Override
	public void write(int b) throws IOException {
		try {
			signature.update((byte) b);
			out.write(b);
		} catch (SignatureException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		try {
			signature.update(b, off, len);
			out.write(b, off, len);
		} catch (SignatureException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}

	@Override
	public void close() throws IOException {
		out.close();
	}

}
