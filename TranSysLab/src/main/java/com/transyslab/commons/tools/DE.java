/**
 *
 */
package com.transyslab.commons.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.transyslab.commons.tools.rawpso.Particle;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.simcore.mlp.MLPEngThread;
import com.transyslab.simcore.mlp.MLPEngine;

/**
 * @author yali
 *
 */
public class DE extends Thread{

	private float F_;
	private Individual[] newidvds_;
	private Individual[] idvds_;
	private int dims_;
	private int population_;
	private float[] plower_;
	private float[] pupper_;
	private float Cr_;
	private float gbestFitness_;
	private float[] gbest_;
	
	private TaskCenter taskCenter;
	private int iterationLim;

	public DE() {

	}
	public DE(String threadName, TaskCenter tc) {
		setName(threadName);
		taskCenter = tc;
	}
	public int getDim() {
		return dims_;
	}
	public int getPopulation() {
		return population_;
	}
	public float[] getPosition(int i) {
		return idvds_[i].pos_;
	}
	public float[] getNewPosition(int i) {
		return newidvds_[i].pos_;
	}
	public Individual[] getIdvds() {
		return idvds_;
	}
	public Individual[] getNewIdvds() {
		return newidvds_;
	}
	public void initDE(int p, int dim, float f, float cr, float[] pl, float[] pu) {
		dims_ = dim;
		population_ = p;
		F_ = f;
		Cr_ = cr;
		gbestFitness_ = Constants.FLT_INF;
		gbest_ = new float[dims_];
		plower_ = pl;
		pupper_ = pu;
		idvds_ = new Individual[population_];
		newidvds_ = new Individual[population_];
		/// gbest_ = new float[mg];
		for (int i = 0; i < p; i++) {
			idvds_[i] = new Individual(dim);
			newidvds_[i] = new Individual(dim);
			//生成初始解
			idvds_[i].init(pl, pu);
			for (int j = 0; j < dims_; j++) {
				newidvds_[i].pos_[j] = idvds_[i].pos_[j];
			}
		}

	}
	public void selection(int pi) {
		if (newidvds_[pi].fitness_ < idvds_[pi].fitness_) {
			for (int j = 0; j < dims_; j++) {
				idvds_[pi].pos_[j] = newidvds_[pi].pos_[j];
			}
			idvds_[pi].fitness_ = newidvds_[pi].fitness_;
		}

	}
	public float[] getGbest() {
		return gbest_;
	}
	public float getGbestFitness() {
		return gbestFitness_;
	}
	public void setGbestFitness(float gbf) {
		gbestFitness_ = gbf;
	}
	public void setGbest(float[] gbest) {
		for (int i = 0; i < gbest.length; i++) {
			gbest_[i] = gbest[i];
		}
	}
	public float getFitness(int pi) {
		return idvds_[pi].fitness_;
	}
	public void changePos(int pi) {
		int pi1, pi2, pi3;
		pi1 = Individual.rnd_.nextInt(population_);
		do {
			pi2 = Individual.rnd_.nextInt(population_);
		} while (pi1 == pi2);
		do {
			pi3 = Individual.rnd_.nextInt(population_);
		} while (pi1 == pi3 || pi2 == pi3);
		// int counter=0;
		for (int j = 0; j < dims_; j++) {
			newidvds_[pi].pos_[j] = idvds_[pi1].pos_[j] + F_ * (idvds_[pi2].pos_[j] - idvds_[pi3].pos_[j]);
			if (newidvds_[pi].pos_[j] > pupper_[j] || newidvds_[pi].pos_[j] < plower_[j])
				newidvds_[pi].pos_[j] = idvds_[pi].pos_[j];
			if (Individual.rnd_.nextFloat() > Cr_)
				newidvds_[pi].pos_[j] = idvds_[pi].pos_[j];
		}
	}
	public void showresult() {
		System.out.println("程序求得的最优解是" + gbestFitness_);
		System.out.println("每一维的值是");
		for (int i = 0; i < dims_; i++) {
			System.out.println(gbest_[i]);
		}
	}
	public void setMaxGeneration(int arg) {
		iterationLim = arg;
	}
	public double[] organizeTask(int idx, float[] arg){
		double[] ans = new double[dims_ + 1];
		ans[0] = (double) idx;
		for (int k = 0; k < dims_; k++) {
			ans[k+1] = arg[k];
		}
		return ans;
	}
	public String showGBestPos() {
		String s = "";		
		for (int i = 0; i < gbest_.length; i++) {
			s += String.valueOf(gbest_[i]) + " ";
		}
		return s;
	}
	@Override
	public void run() {
		for (int i = 0; i < iterationLim; i++) {
			taskCenter.setTaskAmount(population_);
			long tb = System.currentTimeMillis();
			for (int j = 0; j < population_; j++) {
				float[] testingPara = newidvds_[j].pos_;
				double[] tmp = organizeTask(j, testingPara);
				try {
					taskCenter.undoneTasks.put(tmp);//dispatch task
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			for (int j = 0; j < population_; j++) {
				float fval = (float)taskCenter.getResult(j);//fetch result
				newidvds_[j].setFitness(fval);				
				if (fval<gbestFitness_) {
					setGbest(newidvds_[j].pos_);
					setGbestFitness(fval);
				}
				changePos(j);
			}
			
			System.out.println("Gbest : " + gbestFitness_);
			System.out.println("Position : " + showGBestPos());
			System.out.println("Gneration " + i + " used " + ((System.currentTimeMillis() - tb)/1000) + " sec");
		}
		taskCenter.Dismiss();//stop eng线程。
	}
	public static void main(String[] args) {
		Individual.rnd_.setSeed(System.currentTimeMillis());//固定算法随机数
		int maxGeneration = 200;
		int maxTasks = 100;
		TaskCenter tc = new TaskCenter(maxTasks);
		int pop = 30;
		float[] plower = new float[]{12.0f,0.15f,1.0f,5.0f,25,85};
		float[] pupper = new float[]{23.0f,0.17f,4.0f,8.0f,35,95};//,180.0f,25,40,100};
		//Gbest : 0.10734763
		//Position : 15.475985 0.15889278 1.546905 6.5494165 29.030441 91.544785
		//Gbest : 0.10625457
		//Position : 15.993167 0.15445936 1.5821557 6.34795 33.02263 93.043655 
		DE de = new DE("DE", tc);
		de.initDE(pop, plower.length, 0.5f, 0.5f, plower, pupper);
		de.setMaxGeneration(maxGeneration);		
		de.start();
		MLPEngThread mlp_eng_thread;
		for (int i = 0; i < 30; i++) {
			mlp_eng_thread = new MLPEngThread("Eng"+i, tc);
			mlp_eng_thread.setMode(3);
//			((MLPEngine) mlp_eng_thread.engine).seedFixed = true;
			mlp_eng_thread.start();
		}
	}
}
