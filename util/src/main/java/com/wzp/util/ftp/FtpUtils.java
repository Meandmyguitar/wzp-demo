package com.wzp.util.ftp;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FtpUtils {

	/**
	 * 使用类似 ftp://user:password@127.0.0.1:21/file/path 的路径创建FTPClient
	 * <p>
	 * 如果用户名密码中含有特殊字符 ':' , '@' 可以使用url转义 '%3A' '%40'
	 * </p>
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static FTPClient buildFtpClient(URL url) throws IOException {
		if (!url.getProtocol().equals("ftp")) {
			throw new IllegalArgumentException("不是ftp协议");
		}

		int port = url.getPort();
		if (port == -1) {
			port = url.getDefaultPort();
		}

		String username = "anonymous";
		String password = "";
		if (url.getUserInfo() != null) {
			String[] t = url.getUserInfo().split(":");
			username = URLDecoder.decode(t[0], "utf-8");
			password = URLDecoder.decode(t[1], "utf-8");
		}

		FTPClient client = new FTPClient();
		try {
			client.connect(url.getHost(), port);
			if (!client.login(username, password)) {
				throw new FtpException(client.getReplyCode(), client.getReplyString());
			}

		} catch (IOException e) {
			client.disconnect();
			throw e;
		}

		return client;
	}

	public static File tempFile() throws IOException {
		return tempFile(null);
	}

	/**
	 * 返回一个临时文件
	 * 
	 * @return
	 * @throws IOException 
	 */
	public static File tempFile(String suffix) throws IOException {
		String s = new SimpleDateFormat("yyyyMMdd").format(new Date());
		return File.createTempFile(s, suffix);
	}

	public static File downloadGZIP(URL url) throws IOException {
		File temp = downloadFile(url);
		try {
			return ungzip(temp);
		} finally {
			temp.delete();
		}
	}

	/**
	 * 从ftp服务器上下载一个文件到临时目录
	 * 
	 * @param name
	 * @return
	 * @throws IOException 
	 */
	public static File downloadFile(URL url) throws IOException {
		FileOutputStream output = null;
		InputStream input = null;
		try {
			input = retrieve(url);
			File temp = tempFile();
			output = new FileOutputStream(temp);
			IOUtils.copy(input, output);
			return temp;
		} finally {
			IOUtils.closeQuietly(output);
			IOUtils.closeQuietly(input);
		}
	}

	public static void storeGZIP(URL url, File file, boolean overwrite) throws IOException {

		FileInputStream input = null;
		try {
			input = new FileInputStream(file);
			storeGZIP(url, input, overwrite);
		} finally {
			IOUtils.closeQuietly(input);
		}
	}

	public static void storeGZIP(URL url, InputStream input, boolean overwrite) throws IOException {
		FtpOutputStream out = null;
		try {
			out = store(url, overwrite);
			GZIPOutputStream gzip = new GZIPOutputStream(out);
			IOUtils.copy(input, gzip);
			gzip.finish();
			out.finish();
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	public static void storeFile(URL url, File file, boolean overwrite) throws IOException {

		FileInputStream input = null;
		try {
			input = new FileInputStream(file);
			storeFile(url, input, overwrite);
		} finally {
			IOUtils.closeQuietly(input);
		}
	}

	public static void storeFile(URL url, InputStream input, boolean overwrite) throws IOException {

		FtpOutputStream output = null;
		try {
			output = store(url, overwrite);
			IOUtils.copy(input, output);
			output.finish();
		} finally {
			IOUtils.closeQuietly(output);
		}
	}

	public static FtpOutputStream store(URL url, boolean overwrite) throws IOException {

		checkUrl(url);

		FTPClient ftp = null;
		OutputStream out = null;
		File file = null;
		try {
			ftp = FtpUtils.buildFtpClient(url);
			ftp.enterLocalPassiveMode();
			ftp.setFileType(FTP.BINARY_FILE_TYPE);

			file = new File(url.getPath().substring(1));

			String[] names = null;
			if (file.getParent() == null) {
				names = ftp.listNames();
			} else {
				names = ftp.listNames(file.getParent());
			}
			ftp.listNames(url.getPath());
			if (!overwrite && Arrays.asList(names).contains(file.toString())) {
				throw new IllegalStateException("文件已存在:" + file);
			}

			// 创建目录
			if (file.getParentFile() != null) {
				mkdirs(ftp, file.getParent());
			}

			out = ftp.storeFileStream(file.toString());
			if (out == null) {
				throw new FtpException(ftp.getReplyCode(), ftp.getReplyString());
			}
		} finally {
			if (out == null) {
				try {
					ftp.disconnect();
				} catch (Exception e) {
				}
			}
		}

		return new FtpOutputStream(ftp, out);
	}

	public static InputStream retrieveGZIP(URL url) throws IOException {
		InputStream in = retrieve(url);
		try {
			return new GZIPInputStream(in);
		} catch (IOException e) {
			IOUtils.closeQuietly(in);
			throw e;
		}
	}

	public static InputStream retrieve(URL url) throws IOException {

		checkUrl(url);

		FTPClient ftp = null;
		InputStream in = null;
		String filepath = null;
		try {
			ftp = FtpUtils.buildFtpClient(url);
			ftp.enterLocalPassiveMode();
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			filepath = url.getPath().substring(1);
			in = ftp.retrieveFileStream(filepath);
			if (in == null) {
				throw new FtpException(ftp.getReplyCode(), ftp.getReplyString());
			}
		} finally {
			if (in == null) {
				try {
					ftp.disconnect();
				} catch (Exception e) {
				}
			}
		}

		return new FtpInputStream(ftp, in);
	}

	private static void mkdirs(FTPClient client, String path) throws IOException {
		ArrayList<String> list = new ArrayList<String>();
		for (String s : path.split("/")) {
			if (!StringUtils.isEmpty(s)) {
				list.add(s);
				String t = StringUtils.join(list, '/');
				if (!client.makeDirectory(t)) {
					// 可能是目录已存在
				}
			}
		}
	}

	private static File ungzip(File file) throws IOException {
		FileInputStream input = null;
		try {
			input = new FileInputStream(file);
			return ungzip(input);
		} finally {
			IOUtils.closeQuietly(input);
		}
	}

	private static File ungzip(InputStream input) throws IOException {
		File file = tempFile();
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(file);
			GZIPInputStream gzip = new GZIPInputStream(input);
			IOUtils.copy(gzip, output);
			output.flush();
			return file;
		} finally {
			IOUtils.closeQuietly(output);
		}
	}

	private static void checkUrl(URL url) {
		if (url == null) {
			throw new IllegalArgumentException("url为空");
		}
		if (StringUtils.isEmpty(url.getPath())) {
			throw new IllegalArgumentException("url path 为空");
		}
		if (url.getPath().endsWith("/")) {
			throw new IllegalArgumentException("url不能以/结尾");
		}
	}
	
	public static void main(String[] args) throws IOException {
		
		URL url = new URL("ftp://ci:123456@172.19.60.13/t2.log");
		
		FtpOutputStream out = store(url, true);
		IOUtils.write("hello world", out);
		out.finish();
		out.close();
		
		InputStream in = retrieve(url);
		String s = IOUtils.toString(in);
		System.out.println(s);
		in.close();
	}
	
}
