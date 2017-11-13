package testPackaging;

import com.transyslab.commons.io.ConfigUtils;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.*;
import java.net.URL;

/**
 * ͨ��IDE��ȡ����Դ�ļ�·����ͨ��Jar��ȡ��·������һ�¡�
 * IDE��ȡ��·���У�url����"file:"��ͷ��String path ����·����ͷ��
 * Jar��ȡ��·���У�url����"Jar: file:"��ͷ��String path ����"file:"��ͷ��
 * Created by WangYimin on 2017/11/9.
 */

public class PackageTest {
	public static void main(String[] args) {

		//��ʽ1��ͨ��ClassLoader��ȡ·��ʱ��ʹ�����·���������ԡ�/����ͷ
		URL url = ClassLoader.getSystemResource("demo_neihuan/scenario2/default.properties");
		//ע��url�ĵ�ַ��ͷ
		System.out.println(url);
		//ע��path�ĵ�ַ��ͷ
		System.out.println(url.getPath());

		//��ʽ2��ͨ��class��ȡ·��ʱ��ʹ�þ���·�������ԡ�/����ͷ�������Ŀ¼
		URL url2 = PackageTest.class.getResource("/demo_neihuan/scenario2/default.properties");
		System.out.println(url2);
		System.out.println(url2.getPath());

		//ͨ����ͬ��ʽ����ʱ��õ�url��ַ���в�ͬ��
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
