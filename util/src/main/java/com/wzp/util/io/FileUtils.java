package com.wzp.util.io;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class FileUtils extends org.apache.commons.io.FileUtils {

	public static Iterable<String> lineIterable(final File file) throws IOException {
        return new Iterable<String>() {
			
			@Override
			public Iterator<String> iterator() {
				try {
					return lineIterator(file);
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
			}
		};
    }
}
