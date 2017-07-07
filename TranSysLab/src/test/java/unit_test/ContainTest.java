package unit_test;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ContainTest {

	public static void main(String[] args) {
		File testFile = new File("./test.txt");
		Configurations configs = new Configurations();
		Configuration config = null;
		try {
			config = configs.properties(testFile);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		String testing = config.getString("HAHAHA");
		boolean a1 = Boolean.parseBoolean(testing);
		boolean a2 = Boolean.parseBoolean("false");
		HashMap<Integer, String> b = new HashMap<>();
		b.put(1,testing);

		/*try{
			FileOutputStream fout = new FileOutputStream("output.txt");
			PrintStream out = new PrintStream(fout);
			System.setOut(out);
		}
		catch (Exception e){

		}*/

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd-HH:mm:ss");
		LocalDateTime testLDT = LocalDateTime.of(2017,7,13,15,50,0);
		System.out.println(testLDT.format(formatter));
		System.out.println(testLDT.isEqual(LocalDateTime.parse("2017/07/13-15:50:00",formatter)));


		System.out.println(ZoneId.systemDefault());
		double [][] A = new double[5][3];
		for (int j = 0; j < 5; j++) {
			for (int k = 0; k < 3; k++) {
				A[j][k] = Math.pow(j+1, k+1);
			}
		}
		System.out.println(A.length);

		List<Integer> L1 = new ArrayList<>();
		List<Integer> L2 = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			L1.add(i+1);
			L2.add((i+1)*2);
		}
		System.out.println(L2);
		L2.forEach(item -> item = 0);
		System.out.println(L2);
		List<Integer> L3 = new ArrayList<>(L1);
		Collections.copy(L3, L1);
		L3.retainAll(L2);
		System.out.println(L1);
		System.out.println(L3);
	}

}
