package testJavaFunction;

import java.io.FileOutputStream;

import com.transyslab.commons.io.TXTUtils;

public class LogitTest {

	public static void main(String[] args) {
		try {
			TXTUtils fout = new TXTUtils("src/main/resources/output/test2.csv");
			double[] gamma = new double[] {7.16, 7.16};
			for (int i = 0; i < 10000; i++) {
				double x = 0.0002*i-1.0;
				double u = gamma[0]*(x);
				double pr = Math.exp(u)/(1+Math.exp(u));
				fout.write(x + "," + u + "," + pr + "\r\n");
			}
			fout.closeWriter();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
