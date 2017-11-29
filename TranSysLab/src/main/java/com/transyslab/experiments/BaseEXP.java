package com.transyslab.experiments;

import com.transyslab.commons.io.CSVUtils;
import com.transyslab.commons.tools.ADFullerTest;
import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MacroCharacter;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.util.Arrays;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by WangYimin on 2017/9/18.
 */
public class BaseEXP {
	public static void main(String[] args) throws IOException {
		CSVPrinter printer = CSVUtils.getCSVWriter("R://BestResult.csv",null,false);
		MLPEngine mlpEngine = new MLPEngine("src/main/resources/demo_neihuan/scenario2/default.properties");
		mlpEngine.loadFiles();
		mlpEngine.getSimParameter().setLCBuffTime(3.5627);
		mlpEngine.getSimParameter().setLCDStepSize(2.0);
		mlpEngine.seedFixed = false;
		for(int i=0;i<10;i++){
			mlpEngine.alterEngineFreeParas(new double[]{170.19,1.4367,4.1219,3.6699});
			mlpEngine.repeatRun();
			List<MacroCharacter> result = mlpEngine.getSimMap().get("det2");
			double[] speed = result.stream().mapToDouble(e -> e.getKmSpeed()).toArray();
			double[] flow = result.stream().mapToDouble(e -> e.getHourFlow()*3/12.0).toArray();
			double rmsne = FitnessFunction.evaRNSE(speed,mlpEngine.getEmpData());
			double ksdis = FitnessFunction.evaKSDistance(ADFullerTest.seriesDiff(speed,1),
					ADFullerTest.seriesDiff(mlpEngine.getEmpData(),1));
			printer.printRecords(rmsne,ksdis,speed,flow);
			printer.flush();
//			System.out.println(Arrays.toString(flow));
//			System.out.println(Arrays.toString(speed));
			System.out.println(rmsne);
			System.out.println(ksdis);
		}
		mlpEngine.close();
	}
}
