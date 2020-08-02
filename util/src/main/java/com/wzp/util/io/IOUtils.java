package com.wzp.util.io;

import java.io.IOException;
import java.util.HashMap;
import java.util.zip.ZipFile;

public class IOUtils extends org.apache.commons.io.IOUtils {

	public static void closeQuietly(ZipFile zip) {
		new HashMap<>()
		try {
			if (zip != null) {
				zip.close();
			}
		} catch (IOException ioe) {
		}
	}
}
