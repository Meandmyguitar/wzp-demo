package com.wzp.util.ftp;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.io.InputStream;

public class FtpInputStream extends InputStream {
	
	private byte[] buf;
	
	private FTPClient ftp;
	
	private InputStream in;

	public FtpInputStream(FTPClient ftp, InputStream in) {
		super();
		this.ftp = ftp;
		this.in = in;
		buf = new byte[1];
	}

	@Override
	public int read() throws IOException {
		int r = read(buf);
		if (r == -1) {
			return r;
		}
		return buf[0] & 0xff;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int r =  in.read(b, off, len);
		if (r == -1) {
			in.close();
			if (!ftp.completePendingCommand()) {
				throw new FtpException(ftp.getReplyCode(), ftp.getReplyString());
			}
		}
		return r;
	}

	@Override
	public void close() throws IOException {
		ftp.disconnect();
	}

}
