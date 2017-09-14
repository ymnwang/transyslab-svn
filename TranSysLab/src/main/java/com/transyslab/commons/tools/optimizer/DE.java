/**
 *
 */
package com.transyslab.commons.tools.optimizer;

import com.transyslab.commons.tools.Function;
import com.transyslab.commons.tools.mutitask.Task;
import com.transyslab.commons.tools.mutitask.TaskCenter;
import com.transyslab.commons.tools.mutitask.TaskWorker;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.simcore.mlp.Functions.QSDFun;
import com.transyslab.simcore.mlp.Functions.QSDMax;
import com.transyslab.simcore.mlp.Functions.TSFun;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yali
 *
 */
public class DE extends SchedulerThread{

	private double F_;
	private Individual[] newidvds_;
	private Individual[] idvds_;
	private int dims_;
	private int population_;
	private double[] plower_;
	private double[] pupper_;
	private double Cr_;
	private double gbestFitness_;
	private double[] gbest_;
	private TSFun tsFun;
	private QSDFun qsdFun;
	private QSDMax qsdMax;
	private int feasibleCount = 0;
	private int gBestIndex = 0;
	
	private int iterationLim;

	public DE() {//旧代码中使用DE，但不作为SchedulerThread使用
		super("Unknown", null);
	}
	public DE(String threadName, TaskCenter tc) {
		super(threadName, tc);
	}
	public int getDim() {
		return dims_;
	}
	public int getPopulation() {
		return population_;
	}
	public double[] getPosition(int i) {
		return idvds_[i].pos_;
	}
	public double[] getNewPosition(int i) {
		return newidvds_[i].pos_;
	}
	public Individual[] getIdvds() {
		return idvds_;
	}
	public Individual[] getNewIdvds() {
		return newidvds_;
	}
	public void initDE(int p, int dim, double f, double cr, double[] pl, double[] pu) {
		dims_ = dim;
		population_ = p;
		F_ = f;
		Cr_ = cr;
		gbestFitness_ = Constants.FLT_INF;
		gbest_ = new double[dims_];
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
		tsFun = new TSFun();
		double[] obsParas = new double[]{0.5122,20.37,0.1928};
		tsFun.setParas(obsParas, 0.2, 120.0/3.6);
		qsdFun = new QSDFun();
		qsdMax = new QSDMax();
		qsdMax.setParas(20.37, 0.1928);
	}
	public void selection(int pi) {
		//5.15选择策略文献
		if(true){//constrains(pi)){ // 可行解
			if (newidvds_[pi].fitness_ < idvds_[pi].fitness_) {
				for (int j = 0; j < dims_; j++) {
					idvds_[pi].pos_[j] = newidvds_[pi].pos_[j];
				}
				idvds_[pi].results[0] = newidvds_[pi].results[0];
				idvds_[pi].results[1] = newidvds_[pi].results[1];
				idvds_[pi].fitness_ = newidvds_[pi].fitness_;
				if (idvds_[pi].fitness_<gbestFitness_) {
					setGbest(idvds_[pi].pos_);
					setGbestFitness(idvds_[pi].fitness_);
					gBestIndex = pi;
				}
				feasibleCount++;
			}
		}
		

	}
	public double[] getGbest() {
		return gbest_;
	}
	public double getGbestFitness() {
		return gbestFitness_;
	}
	public void setGbestFitness(double gbf) {
		gbestFitness_ = gbf;
	}
	public void setGbest(double[] gbest) {
		for (int i = 0; i < gbest.length; i++) {
			gbest_[i] = gbest[i];
		}
	}
	public double getFitness(int pi) {
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
		// 5.15
//		int jrand = Individual.rnd_.nextInt(dims_);
		for (int j = 0; j < dims_; j++) {
			newidvds_[pi].pos_[j] = idvds_[pi1].pos_[j] + F_ * (idvds_[pi2].pos_[j] - idvds_[pi3].pos_[j]);
			// 5.15约束添加
			if (newidvds_[pi].pos_[j] > pupper_[j] || newidvds_[pi].pos_[j] < plower_[j])
				newidvds_[pi].pos_[j] = idvds_[pi].pos_[j];
			// 5.15添加&&
			if (Individual.rnd_.nextFloat() > Cr_)// && j!=jrand)
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
	public boolean constrains(int pi){
		double tol = 0.05;
		double vf = 20.37;
		double qm = 0.5122;
		double kj = 0.1928;
		double k1 = qm/(120.0/3.6);
		double ts = tsFun.cal(new double[]{newidvds_[pi].pos_[0]});
		double k2 = (1-qm*(ts+0.2))*kj;
		
		if(newidvds_[pi].pos_[1]<k1){
			qsdFun.setParas(vf, kj, newidvds_[pi].results[0], newidvds_[pi].results[1]);
			if(Math.abs(qsdFun.cal(new double[]{k1})-qm)>tol)
				return false;
		}
		else if(k1<=newidvds_[pi].pos_[1]&&newidvds_[pi].pos_[1]<k2){
			double tmp = qsdMax.cal(new double[]{newidvds_[pi].results[0], newidvds_[pi].results[1]});
			if(Math.abs(tmp-qm)>tol)
				return false;
		}
		else if(newidvds_[pi].pos_[1]>k2){
			qsdFun.setParas(vf, kj, newidvds_[pi].results[0], newidvds_[pi].results[1]);
			if(Math.abs(qsdFun.cal(new double[]{k2})-qm)>tol)
				return false;
		}
		else{
			System.out.println("warning");
		}
		return true;	
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
		List<Task> taskList = new ArrayList<>();
		for (int i = 0; i < iterationLim; i++) {
			taskList.clear();
			long tb = System.currentTimeMillis();
			for (int j = 0; j < population_; j++) {
				taskList.add(dispatch(newidvds_[j].pos_, TaskWorker.ANY_WORKER));//dispatch task
			}
			
			for (int j = 0; j < population_; j++) {
				double[] tmpResults = taskList.get(j).getOutputs();
				float fval = (float) tmpResults[0];//fetch result
				newidvds_[j].setFitness(fval);
				newidvds_[j].results[0] = tmpResults[1];
				newidvds_[j].results[1] = tmpResults[2];
				selection(j);
				changePos(j);
			}
			
			System.out.println("Gbest : " + gbestFitness_);
			System.out.println("Position : " + showGBestPos()+","+idvds_[gBestIndex].results[0]+","+idvds_[gBestIndex].results[1]);
			System.out.println("Gneration " + i + " used " + ((System.currentTimeMillis() - tb)/1000) + " sec");
		}
		dismissAllWorkingThreads();//stop eng线程。
	}
	public double[] solve(Function fitFun, double[] plower, double[] pupper) {
		int maxGeneration = 200;
		int pop = 30;
		initDE(pop, plower.length, 0.5f, 0.5f, plower, pupper);
		setMaxGeneration(maxGeneration);
		
		for (int i = 0; i < iterationLim; i++) {
			for (int j = 0; j < population_; j++) {
				double fval = fitFun.cal(newidvds_[j].pos_);
				newidvds_[j].setFitness(fval);
				selection(j);
				if (fval<gbestFitness_) {
					setGbest(idvds_[j].pos_);
					setGbestFitness(fval);
				}
				changePos(j);
			}
		}
		
		return gbest_;
	}

//	public static void main(String[] args) {
//		Individual.rnd_.setSeed(System.currentTimeMillis());//固定算法随机数
//		int maxGeneration = 200;
//		int maxTasks = 20;
//		TaskCenter tc = new TaskCenter(maxTasks);
//		int pop = 20;
//
//		//*********************旧实验*******************************
//		/*float[] plower = new float[]{12.0f,0.15f,1.0f,5.0f,25,85};
//		float[] pupper = new float[]{23.0f,0.17f,4.0f,8.0f,35,95};//,180.0f,25,40,100};
//		//Gbest : 0.10734763
//		//Position : 15.475985 0.15889278 1.546905 6.5494165 29.030441 91.544785
//		//Gbest : 0.10625457
//		//Position : 15.993167 0.15445936 1.5821557 6.34795 33.02263 93.043655
//		DE de = new DE("DE", tc);
//		de.initDE(pop, plower.length, 0.5f, 0.5f, plower, pupper);
//		de.setMaxGeneration(maxGeneration);
//		de.start();
//		MLPEngThread mlp_eng_thread;
//		for (int i = 0; i < 30; i++) {
//			mlp_eng_thread = new MLPEngThread("Eng"+i, tc);
//			mlp_eng_thread.setMode(3);
////			((MLPEngine) mlp_eng_thread.engine).seedFixed = true;
//			mlp_eng_thread.start();
//		}*/
//		//*********************旧实验*******************************
//
//
//		//*********************旧实验2*******************************
//		/*
//		float[] plower = new float[]{12.0f,0.12f,0.01f,0.01f, 1.0f, 30.0f};
//		float[] pupper = new float[]{23.0f,0.17f,4.0f,8.0f, 2.0f, 40.0f};//,180.0f,25,40,100};
//		DE de = new DE("DE", tc);
//		de.initDE(pop, plower.length, 0.5f, 0.5f, plower, pupper);
//		de.setMaxGeneration(maxGeneration);
//		de.start();
//		MLPEngThread mlp_eng_thread;
//		for (int i = 0; i < 30; i++) {
//			mlp_eng_thread = new MLPEngThread("Eng"+i, tc);
//			mlp_eng_thread.setMode(6);
////			((MLPEngine) mlp_eng_thread.engine).seedFixed = true;
//			mlp_eng_thread.start();
//		}*/
//		//*********************旧实验2*******************************
//		float[] plower = new float[]{5.7787f,0.0002f,0.00f,0.00f};
//		float[] pupper = new float[]{65.0787f,1.798f,10.00f,10.00f};
//		DE de = new DE("DE", tc);
//		de.initDE(pop, plower.length, 0.5f, 0.5f, plower, pupper);
//		de.setMaxGeneration(maxGeneration);
//		de.start();
//		EngThread mlp_eng_thread;
//		for (int i = 0; i < 20; i++) {
//			mlp_eng_thread = new EXP_KS("Eng" + i, tc, "./master") {
//			};
//			mlp_eng_thread.start();
//		}
//	}
}
