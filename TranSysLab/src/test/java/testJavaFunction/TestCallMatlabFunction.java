package testJavaFunction;

import com.mathworks.toolbox.javabuilder.*;
import com.transyslab.commons.io.CSVUtils;
import matlabfunctions.MatlabFunctions;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;

/**
 * Created by yali on 2017/11/26.
 */
public class TestCallMatlabFunction {
	public static void main(String[] args) throws MWException {
		MatlabFunctions mlFunctions = new MatlabFunctions();
		Double[] testSeries;
		try {
			java.util.List<CSVRecord> results= CSVUtils.readCSV("R://SttSpeed2017-07WithoutHead.csv",null);
			int rows = 1;//results.size();
			int cols = results.get(0).size();
			testSeries = new Double[cols];//[rows][cols];
			//for(int i=0;i<rows;i++){
				for(int j=0;j<cols;j++){
					testSeries[j] = Double.valueOf(results.get(0).get(j));
				}
			//}
			MWNumericArray mlInput = new MWNumericArray(testSeries);
			// 结果大小为1
			Object[] result = mlFunctions.adfullerTest(1,mlInput,1);
			double[] resultArray = ((double[][])((MWNumericArray)result[0]).toDoubleArray())[0];
			System.out.println(result[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
