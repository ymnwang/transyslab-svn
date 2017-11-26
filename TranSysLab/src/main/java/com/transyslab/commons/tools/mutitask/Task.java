package com.transyslab.commons.tools.mutitask;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by WangYimin on 2017/7/18.
 */
public class Task {
	protected double[] inputVariables;
	protected double[] objectiveValues;
	protected Map<Object, Object> attributes;
	private boolean canRetrieve;
	protected String workerName;

	public Task(double[] arg_inputs, String workerName) {
		inputVariables = arg_inputs;
		canRetrieve = false;
		this.workerName = workerName;
	}

	public double[] getInputVariables() {
		return inputVariables;
	}

	protected synchronized void setResults(double[] objectiveVals, Map<Object, Object> attributes) {
		this.objectiveValues = objectiveVals;
		this.attributes = attributes;
		canRetrieve = true;
//		System.out.println("DEBUG: SimTask finished at " + LocalDateTime.now() + " by " + Thread.currentThread().getName());
		notify();
	}

	public synchronized double[] getObjectiveValues() {
		while (!canRetrieve) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return objectiveValues;
	}

	public synchronized Object getAttributes(Object id) {
		while (!canRetrieve) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return this.attributes.get(id);
	}

	public void resetResult() {
		objectiveValues = new double[objectiveValues.length];//ÖØÖÃÎª0
		canRetrieve = false;
	}

	public boolean isFinished(){
		return canRetrieve;
	}

	public double getInputVariableValue(int index) {
		if (index>=0 && inputVariables.length>index)
			return inputVariables[index];
		else
			return Double.NaN;
	}

	public void setInputVariableValue(int index, double newValue) {
		if (index>=0 && inputVariables.length>index)
			inputVariables[index] = newValue;
		else
			System.err.println("wrong index");
	}

	public Map getAttributes() {
		if (attributes==null)
			attributes = new HashMap<>();
		return attributes;
	}
}
