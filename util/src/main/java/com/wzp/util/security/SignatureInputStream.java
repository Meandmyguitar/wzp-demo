package com.wzp.util.security;

import java.io.IOException;
import java.io.InputStream;
import java.security.Signature;
import java.security.SignatureException;

public class SignatureInputStream extends InputStream {

	private Signature signature;

	private InputStream in;

	public SignatureInputStream(Signature signature, InputStream in) {
		super();
		this.signature = signature;
		this.in = in;
	}
	
	public boolean verify(byte[] sign) throws SignatureException {
		return signature.verify(sign);
	}

	@Override
	public int read() throws IOException {
		try {
			int b = in.read();
			if (b != -1) {
				signature.update((byte) b);
			}
			return b;
		} catch (SignatureException e) {
			throw new IOException(e);
		}
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		try {
			int c = in.read(b, off, len);
			if (c == -1) {
				return -1;
			}
			signature.update(b, off, len);
			return c;
		} catch (SignatureException e) {
			throw new IOException(e);
		}
	}

	@Override
	public int available() throws IOException {
		return in.available();
	}

	@Override
	public void close() throws IOException {
		in.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
	}

	@Override
	public synchronized void reset() throws IOException {
		throw new IOException("mark/reset not supported");
	}

	@Override
	public boolean markSupported() {
		return false;
	}

}
