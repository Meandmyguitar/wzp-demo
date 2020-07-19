package com.wzp.util.ftp;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.io.OutputStream;

public class FtpOutputStream extends OutputStream {
	
	private FTPClient ftp;
	
	private OutputStream out;
	
	private boolean finished;

	public FtpOutputStream(FTPClient ftp, OutputStream out) {
		super();
		this.ftp = ftp;
		this.out = out;
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		out.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
	}
	
	public void finish() throws IOException {
		if (finished) {
			return;
		}
		try {
			out.close();
			if (!ftp.completePendingCommand()) {
				throw new FtpException(ftp.getReplyCode(), ftp.getReplyString());
			}
		} finally {
			finished = true;
		}
	}

	@Override
	public void close() throws IOException {
		try {
			if (!finished) {
				finish();
			}
		} finally {
			ftp.disconnect();
		}
	}

}
