package unit_test;

import com.google.common.hash.HashCode;
import com.transyslab.commons.io.CSVUtils;
import com.transyslab.commons.io.JdbcUtils;
import com.transyslab.simcore.mlp.MLPParameter;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ContainTest {

	public static void main(String[] args) {

		System.out.println(String.valueOf(Double.parseDouble("NaN")));

		List<CSVRecord> results = null;
		try {
			results = CSVUtils.readCSV("testHashCode.csv",null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		double[] data = new double[results.size()];
		for (int i = 0; i < results.size(); i++) {
			data[i] = results.get(i).get(0).hashCode();
		}
		try {
			CSVUtils.writeCSV("POIHashCode.csv",null, data);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(String.format("%.2f",12.5623));

		QueryRunner qr = new QueryRunner(JdbcUtils.getDataSource());
		String sql = "insert into simloop(travel_time) values(?)";
		Object[][] paras = {{Double.NaN},{Double.NaN}};
		try {
			qr.update(sql, paras);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println(LocalDateTime.now().toString());

		System.out.println("root is " + MLPParameter.rUpper(1.0,21.7950, 0.1765, 0.4633));

		//测试null
		List<Integer> lista = Arrays.asList(1, 10, 3, 7, 5);
		List<Integer> listb = new ArrayList<>();
		lista.parallelStream().filter(a -> a>2).count();


		//测试无限长时间仿真的边界判断
		System.out.println(Double.POSITIVE_INFINITY+1 > Double.POSITIVE_INFINITY);

		//测试流计算的边界
		List<Integer> list = Arrays.asList(1, 10, 3, 7, 5);
		Integer a = list.stream()
				.peek(num -> System.out.println("will filter " + num))
				.filter(x -> x > 100)
				.findFirst()
				.orElse(null);
		System.out.println(a);


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
