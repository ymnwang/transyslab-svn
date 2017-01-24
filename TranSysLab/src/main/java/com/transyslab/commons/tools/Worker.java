/**
 *
 */
package com.transyslab.commons.tools;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import com.transyslab.simcore.SimulationEngine;

/**
 * @author yali
 *
 */
public class Worker implements Runnable {
	private int id_;
	private String name_;
	private SimulationEngine engine_;
	private CyclicBarrier barrier_;
	public Worker() {

	}
	public Worker(int id, String n) {
		this.id_ = id;
		this.name_ = n;
	}
	public Worker(SimulationEngine eg, CyclicBarrier b) {
		engine_ = eg;
		barrier_ = b;
	}
	@Override
	public void run() {
		// engine_.run(2);
		// engine_.exhaustionRun(6f, 0.01f);
		engine_.run(3);
		try {
			barrier_.await();
		}
		catch (InterruptedException | BrokenBarrierException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}

}
