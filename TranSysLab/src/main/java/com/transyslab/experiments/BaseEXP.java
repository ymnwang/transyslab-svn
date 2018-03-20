package com.transyslab.experiments;

import com.transyslab.commons.io.CSVUtils;
import com.transyslab.commons.tools.ADFullerTest;
import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MacroCharacter;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.time.StopWatch;

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
//		MLPEngine mlpEngine = new MLPEngine("src/main/resources/demo_neihuan/scenario2/shortTerm.properties");
//		mlpEngine.loadFiles();
//		//mlpEngine.seedFixed = false;
//		for(int i=0;i<2;i++){
////			mlpEngine.alterEngineFreeParas(new double[] {168.6467545456, 5.21389125582427, 2.60032585710801, 2.17110747371225});
////			mlpEngine.getSimParameter().setLCBuffTime(9.42296995168615);
//			StopWatch watch = new StopWatch();
//			watch.start();
//			mlpEngine.repeatRun();
//			watch.stop();
//			System.out.println("time used: " + watch.getTime()/1000.0 + " s.");
//			/*List<MacroCharacter> result = mlpEngine.getSimMap().get("det2");
//			double[] flow = MacroCharacter.select(result,MacroCharacter.SELECT_FLOW);
//			System.out.println(Arrays.toString(flow));
//			System.out.println(Arrays.toString(Arrays.stream(flow).map(f -> f*3.0*300).toArray()));
//			System.out.println(Arrays.toString(result.stream().mapToDouble(r->r.getHourFlow()*3.0/12.0).toArray()));//
//			double[] speed = MacroCharacter.select(result,MacroCharacter.SELECT_SPEED);
//			System.out.println(Arrays.toString(speed));*/
//
//		}
//		mlpEngine.close();

		MLPEngine mlpEngine = new MLPEngine("src/main/resources/demo_neihuan/scenario2/check.properties");
		mlpEngine.loadFiles();
		mlpEngine.run();
		mlpEngine.close();
	}
}
