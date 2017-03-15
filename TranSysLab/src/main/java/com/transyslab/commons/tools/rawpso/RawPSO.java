package com.transyslab.commons.tools.rawpso;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.transyslab.commons.tools.TaskCenter;
import com.transyslab.simcore.mlp.MLPEngThread;

public class RawPSO extends Thread{
	int dim;	
	double[] lower;
	double[] upper;
	double[] gbestPos;
	double gbestVal;
	int population;
	List<Particle> particles;
	int iterationLim;
	double[] ag_para;
	Random rand;
	
	TaskCenter taskCenter;
	
	public RawPSO(String threadName, TaskCenter tc) {
		setName(threadName);
		taskCenter = tc;
		particles = new ArrayList<>();
	}
	
	public void initSetting(int P, double[] LOW, double[] UP, int iLim){
		dim = LOW.length;
		lower = LOW;
		upper = UP;
		population = P;
		gbestVal = Double.POSITIVE_INFINITY;
		for (int i = 0; i < P; i++) {
			particles.add(new Particle(dim));
		}
		iterationLim = iLim;
		ag_para = new double [] {0.5, 0.5};
		rand = new Random();
	}
	
	public void setAGPara(double[] arg) {
		ag_para = arg;
	}
	
	public double[] posTrans(double[] pos) {
		double[] ans = new double[pos.length]; 
		for (int i = 0; i < pos.length; i++) {
			ans[i] = lower[i] + (upper[i] - lower[i]) * pos[i];
		}
		return ans;
	}
	
	public String showGBestPos() {
		String s = "";
		double [] pos = posTrans(gbestPos);		
		for (int i = 0; i < pos.length; i++) {
			s += String.valueOf(pos[i]) + " ";
		}
		return s;
	}
	
	public double[] organizeTask(int idx, double[] arg){
		double[] ans = new double[dim + 1];
		ans[0] = (double) idx;
		for (int k = 0; k < dim; k++) {
			ans[k+1] = arg[k];
		}
		return ans;
	}
	
	public boolean checkConstraints(double [] arg) {
		boolean ans = true;
		for (int i = 0; i < arg.length; i++) {
			ans = ans && (arg[i]>lower[i]) && (arg[i]<upper[i]);
		}
		return ans;
	}
	
	public void updateGBest(double arg0, double[] arg1) {
		if (arg0 < gbestVal) {
			gbestVal = arg0;
			gbestPos = arg1;
		}
	}
	
	public void updateParticlePos(Particle p) {
		for (int i = 0; i < dim; i++) {
			p.pos[i] += ag_para[0]*rand.nextDouble()*(gbestPos[i] - p.pos[i]) + 
							   ag_para[1]*rand.nextDouble()*(p.bestPos[i] - p.pos[i]);
		}
	}
	
	@Override
	public void run() {
		for (int i = 0; i < iterationLim; i++) {
			taskCenter.setTaskAmount(population);
			long tb = System.currentTimeMillis();
			for (int j = 0; j < population; j++) {
				double[] testingPara = posTrans(particles.get(j).pos);
				double[] tmp = organizeTask(j, testingPara);
				try {
					taskCenter.undoneTasks.put(tmp);//dispatch task
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			for (int j = 0; j < population; j++) {
				double val = taskCenter.getResult(j);//fetch result
				System.out.println(val);
				if (checkConstraints(particles.get(j).pos)) {
					particles.get(j).updateBest(val);
					updateGBest(val,particles.get(j).pos);
				}				
			}
			
			for (int j = 0; j < gbestPos.length; j++) 
				updateParticlePos(particles.get(j));
			
			System.out.println("Gbest : " + gbestVal);
			System.out.println("Position : " + showGBestPos());
			System.out.println("Gneration " + i + " used " + ((System.currentTimeMillis() - tb)/1000) + " sec");
		}
		taskCenter.Dismiss();//stop engÏß³Ì¡£
	}
	public static void main(String[] args) {
		int pop = 50;
		double[] low = new double[] {15, 0.0, 0.120, 3.0, 1.0};
		double[] up = new double[] {20, 0.0, 0.200, 7.0, 3.0};
		int maxGeneration = 500;
		int maxTasks = 100;
		TaskCenter tc = new TaskCenter(maxTasks);
		
		RawPSO pso = new RawPSO("ThreadPSO", tc);
		pso.initSetting(pop, low, up, maxGeneration);
		pso.start();
		MLPEngThread mlp_eng_thread;
		for (int i = 0; i < 25; i++) {
			mlp_eng_thread = new MLPEngThread("Eng"+i, tc);
			mlp_eng_thread.setMode(3);
			mlp_eng_thread.start();
		}
	}
}
