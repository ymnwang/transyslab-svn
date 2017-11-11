package testPackaging;

import com.transyslab.commons.io.ConfigUtils;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.*;
import java.net.URL;

/**
 * 通过IDE获取的资源文件路径与通过Jar获取的路径并不一致。
 * IDE获取的路径中，url会以"file:"开头；String path 会以路径开头。
 * Jar获取的路径中，url会以"Jar: file:"开头；String path 会以"file:"开头。
 * Created by WangYimin on 2017/11/9.
 */

public class PackageTest {
	public static void main(String[] args) {

		//方式1：通过ClassLoader获取路径时，使用相对路径，即不以“/”开头
		URL url = ClassLoader.getSystemResource("demo_neihuan/scenario2/default.properties");
		//注意url的地址开头
		System.out.println(url);
		//注意path的地址开头
		System.out.println(url.getPath());

		//方式2：通过class获取路径时，使用绝对路径，即以“/”开头，代表根目录
		URL url2 = PackageTest.class.getResource("/demo_neihuan/scenario2/default.properties");
		System.out.println(url2);
		System.out.println(url2.getPath());

		//通过不同方式启动时获得的url地址会有不同。
		File file = null;
		String resource = "demo_neihuan/scenario2/default.properties";
		URL res = ClassLoader.getSystemResource(resource);

		if (res.toString().startsWith("jar:")) {
			try {
				InputStream input = ClassLoader.getSystemResourceAsStream(resource);
				file = File.createTempFile("tempfile", ".tmp");
				OutputStream out = new FileOutputStream(file);
				int read;
				byte[] bytes = new byte[1024];

				while ((read = input.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}
				System.out.println(PackageTest.createConfig(file).getString("tmp"));
				PackageTest.writeout(file);
				file.deleteOnExit();
				System.out.println("When you launch with Jar, you probably reach here that url starts with \"jar:\".");
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} else {
			//this will probably work in your IDE, but not from a JAR
			file = new File(res.getFile());
			System.out.println(PackageTest.createConfig(file).getString("tmp"));
			System.out.println("When you launch with IDE, you probably reach here that url starts without \"jar:\".");
			PackageTest.writeout(file);
		}
		if (file != null && !file.exists()) {
			throw new RuntimeException("Error: File " + file + " not found!");
		}
	}

	static void writeout(File file) {
		String line = null;
		try {
			FileReader fileReader = new FileReader(file);
			// Always wrap FileReader in BufferedReader.
			BufferedReader bufferedReader =
					new BufferedReader(fileReader);

			while((line = bufferedReader.readLine()) != null) {
				System.out.println(line);
			}

			// Always close files.
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static Configuration createConfig(File inputFile) {
		try {
			return new Configurations().properties(inputFile);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}

}
