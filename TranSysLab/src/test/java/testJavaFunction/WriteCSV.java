package testJavaFunction;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.distribution.NormalDistribution;

import com.transyslab.commons.io.CSVUtils;
import com.transyslab.commons.io.FileUtils;

public class WriteCSV {

	public static void main(String[] args) throws UnknownHostException, IOException {
		double[][] data = new double[50][5];
		NormalDistribution normDistr = new NormalDistribution(50, 10);
		for(int i=0;i<50;i++){
			data[i] = normDistr.sample(5);
		}
		
		try {
			CSVUtils.writeCSV("R:\\testwrite.csv", null, data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
