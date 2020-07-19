package com.wzp.util.security;

import javax.crypto.Mac;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SignatureException;

public class MacOutputStream extends OutputStream {

	private Mac mac;
	
	private byte[] macResult;

	private OutputStream out;

	public MacOutputStream(Mac signature, OutputStream out) {
		super();
		this.mac = signature;
		this.out = out;
	}

	public byte[] sign() throws SignatureException {
		if (macResult == null) {
			macResult = mac.doFinal();
		}
		return macResult;
	}

	@Override
	public void write(int b) throws IOException {
			mac.update((byte) b);
			out.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
			mac.update(b, off, len);
			out.write(b, off, len);
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
