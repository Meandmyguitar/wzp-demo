package com.wzp.util.security;

import javax.crypto.Mac;
import java.io.IOException;
import java.io.InputStream;
import java.security.SignatureException;

public class MacInputStream extends InputStream {

	private Mac mac;
	
	private byte[] macResult;

	private InputStream in;

	public MacInputStream(Mac mac, InputStream in) {
		super();
		this.mac = mac;
		this.in = in;
	}

	public byte[] getMacResult() throws SignatureException {
		if (macResult == null) {
			macResult = mac.doFinal();
		}
		return macResult;
	}

	@Override
	public int read() throws IOException {
		int b = in.read();
		if (b != -1) {
			mac.update((byte) b);
		}
		return b;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int c = in.read(b, off, len);
		if (c == -1) {
			return -1;
		}
		mac.update(b, off, len);
		return c;
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
