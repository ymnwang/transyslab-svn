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
	public Producer(SimulationEngine eg) {
		engine_ = eg;
	}
	@Override
	public MesoEngine call() {
		// =0:非snapshot启动，按OD流量随机发车；
        // =1:非snapshot启动，按过车记录定时发车;
		// =2:snapshot启动，按OD流量随机发车；
        // =3:snapshot启动，按过车记录定时发车；
		engine_ = new MesoEngine(Constants.SIM_MODE);
		// engine_.initPSO(pso_);
		// engine_.initDE(de_);
		engine_.loadFiles();
		return (MesoEngine) engine_;
	}
}
