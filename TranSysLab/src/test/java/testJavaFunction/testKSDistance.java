package testJavaFunction;

import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVRecord;

import com.transyslab.commons.io.CSVUtils;
import com.transyslab.commons.tools.FitnessFunction;

public class testKSDistance {

	public static void main(String[] args) throws IOException {
		List<CSVRecord> detData = CSVUtils.readCSV("R://DetSpeed.csv", null);
		List<CSVRecord> simData = CSVUtils.readCSV("R://test2.csv", null);
		double[] simArray = new double[simData.size()];
		double[] detArray = new double[detData.size()];
		for(int j =0;j<simData.get(0).size();j++){
			for(int i=0;i<simArray.length;i++){
				if(i<detArray.length)
					detArray[i] = Double.parseDouble(detData.get(i).get(0));
				simArray[i] =  Double.parseDouble(simData.get(i).get(j));
			}
			double ksDistance = FitnessFunction.evaKSDistance(simArray, detArray);
			System.out.println(ksDistance);
		}
		
	}

}
