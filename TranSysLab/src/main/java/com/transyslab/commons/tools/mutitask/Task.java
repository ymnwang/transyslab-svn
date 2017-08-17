package com.transyslab.commons.tools.mutitask;

/**
 * Created by WangYimin on 2017/7/18.
 */
public class Task {
	private double[] inputs;
	private double[] outputs;
	private boolean canRetrieve;
	protected String workerName;

	public Task(double[] arg_inputs, String workerName) {
		inputs = arg_inputs;
		canRetrieve = false;
		this.workerName = workerName;
	}

	public double[] getInputs() {
		return inputs;
	}

	public synchronized void setOutputs(double[] ans) {
		outputs = ans;
		canRetrieve = true;
		notify();
	}

	public synchronized double[] getOutputs() {
		while (!canRetrieve) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return outputs;
	}

	public void resetResult() {
		for (int i = 0; i < outputs.length; i++) {
			outputs[i] = 0.0;
		}
		canRetrieve = false;
	}

}
