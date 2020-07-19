package com.wzp.util.commons;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@SuppressWarnings({"WeakerAccess", "unused"})
public class FtpUtils {

    private static Logger logger = LoggerFactory.getLogger(FtpUtils.class);

    /**
     * 使用类似 ftp://user:password@127.0.0.1:21/file/path 的路径创建FTPClient
     * <p>
     * 如果用户名密码中含有特殊字符 ':' , '@' 可以使用url转义 '%3A' '%40'
     * </p>
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
            if (!temp.delete()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("删除临时文件失败: {}", temp);
                }
            }
        }
    }

    /**
     * 从ftp服务器上下载一个文件到临时目录
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
        try (FileInputStream input = new FileInputStream(file)) {
            storeGZIP(url, input, overwrite);
        }
    }

    public static void storeGZIP(URL url, InputStream input, boolean overwrite) throws IOException {
        try (OutputStream out = store(url, overwrite)) {
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            IOUtils.copy(input, gzip);
            gzip.finish();
        }
    }

    public static void storeFile(URL url, File file, boolean overwrite) throws IOException {
        try (FileInputStream input = new FileInputStream(file)) {
            storeFile(url, input, overwrite);
        }
    }

    public static void storeFile(URL url, InputStream input, boolean overwrite) throws IOException {
        try (OutputStream output = store(url, overwrite)) {
            IOUtils.copy(input, output);
        }
    }

    public static OutputStream store(URL url, boolean overwrite) throws IOException {

        checkUrl(url);

        FTPClient ftp = null;
        OutputStream out = null;
        File file;
        try {
            ftp = FtpUtils.buildFtpClient(url);
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            file = new File(url.getPath().substring(1));

            String[] names;
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
                closeSafe(ftp);
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
        String filePath;
        try {
            ftp = FtpUtils.buildFtpClient(url);
            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            filePath = url.getPath().substring(1);
            in = ftp.retrieveFileStream(filePath);
            if (in == null) {
                throw new FtpException(ftp.getReplyCode(), ftp.getReplyString());
            }
        } finally {
            if (in == null) {
                closeSafe(ftp);
            }
        }

        return new FtpInputStream(ftp, in);
    }

    private static void closeSafe(FTPClient ftp) {
        try {
            ftp.disconnect();
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.info("关闭连接异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 递归创建目录
     */
    private static void mkdirs(FTPClient client, String path) throws IOException {
        ArrayList<String> list = new ArrayList<>();
        for (String s : path.split("/")) {
            if (!StringUtils.isEmpty(s)) {
                list.add(s);
                String t = StringUtils.join(list, '/');
                if (!client.makeDirectory(t)) {
                    // 可能是目录已存在
                    if (logger.isDebugEnabled()) {
                        logger.debug("创建目录失败: {}", t);
                    }
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

        OutputStream out = store(url, true);
        IOUtils.write("hello world", out);
        out.close();

        InputStream in = retrieve(url);
        String s = IOUtils.toString(in);
        System.out.println(s);
        in.close();
    }

    static class FtpInputStream extends InputStream {

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
            int r = in.read(b, off, len);
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

    static class FtpOutputStream extends OutputStream {

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

}
