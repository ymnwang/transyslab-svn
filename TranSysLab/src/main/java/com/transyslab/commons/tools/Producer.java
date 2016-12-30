/**
 *
 */
package com.transyslab.commons.tools;

import java.util.concurrent.Callable;

import com.transyslab.roadnetwork.Constants;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mesots.MesoEngine;

/**
 * @author yali
 *
 */
public class Producer implements Callable<SimulationEngine> {
	private int id_;
	private String name_;
	private SimulationEngine engine_;
	private PSO pso_;
	private DE de_;
	private SPSA spsa_;

	public Producer() {

	}
	public Producer(int id, String n) {
		this.id_ = id;
		this.name_ = n;
	}
	public Producer(SimulationEngine eg, PSO pso) {
		engine_ = eg;
		pso_ = pso;
		// barrier_ = bar;
	}
	public Producer(SimulationEngine eg, DE de) {
		engine_ = eg;
		de_ = de;
	}
	public Producer(SimulationEngine eg, SPSA spsa){
		engine_ = eg;
		spsa_ = spsa;
	}
	public Producer(SimulationEngine eg) {
		engine_ = eg;
	}
	@Override
	public MesoEngine call() {
		// =0:��snapshot��������OD�������������
        // =1:��snapshot��������������¼��ʱ����;
		// =2:snapshot��������OD�������������
        // =3:snapshot��������������¼��ʱ������
		engine_ = new MesoEngine(Constants.SIM_MODE);

		MesoEngine engine = (MesoEngine)engine_;
		engine.initSPSA(spsa_);
//		 engine_.initPSO(pso_);
//		engine.initDE(de_);
		engine_.loadFiles();
		return engine;
	}
}
