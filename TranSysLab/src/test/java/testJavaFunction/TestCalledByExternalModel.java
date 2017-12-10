package testJavaFunction;

import com.transyslab.experiments.ToExternalModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by ITSA405-35 on 2017/12/9.
 */
public class TestCalledByExternalModel {

	public static void main(String[] args){
		Scanner sc = new Scanner(System.in);
		String[] params = sc.nextLine().split(",");
		sc.close();
		double[] input = new double[params.length];
		for(int i=0;i<input.length;i++){
			input[i] = Double.parseDouble(params[i]);
		}

		ToExternalModel simEngins = new ToExternalModel("E:/³ÌÐò´úÂë/Matlab³ÌÐò/ExternalModel/optmks.properties");
		simEngins.startSimEngines();
		simEngins.dispatchTask(input);
		System.out.println(Arrays.toString(simEngins.getTaskResult(0,"SimSpeed")));
		simEngins.closeSimEngines();
	}

}
