package testPackaging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by WangYimin on 2017/11/9.
 */
public class JarRead {
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("Please provide a JAR filename and file to read");
			System.exit(-1);
		}
		JarFile jarFile = new JarFile(args[0]);
		JarEntry entry = jarFile.getJarEntry(args[1]);
		InputStream input = jarFile.getInputStream(entry);
		process(input);
		jarFile.close();
	}

	private static void process(InputStream input) throws IOException {
		InputStreamReader isr = new InputStreamReader(input);
		BufferedReader reader = new BufferedReader(isr);
		String line;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}
		reader.close();
	}
}
