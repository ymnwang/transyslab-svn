package com.transyslab.commons.io;

import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.*;
import java.net.URL;

/**
 * Created by WangYimin on 2017/11/9.
 */
public class JarFileUtils {
	public static File getJarFile(String relativePath) {
		try {
			InputStream input = ClassLoader.getSystemResourceAsStream(relativePath);
			File file = File.createTempFile("tempfile", ".tmp");
			OutputStream out = new FileOutputStream(file);
			int read;
			byte[] bytes = new byte[1024];
			while ((read = input.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			file.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
