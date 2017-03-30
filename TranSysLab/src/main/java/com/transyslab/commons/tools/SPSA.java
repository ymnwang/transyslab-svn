package com.transyslab.commons.tools;

import org.apache.commons.math3.distribution.BinomialDistribution;

import com.transyslab.simcore.mesots.MesoEngine;
import com.transyslab.simcore.mlp.MLPEngThread;

//同步扰动随机逼近算法
public class SPSA extends Thread{
	private int dims_;
	//需要标定的参数，已做归一化操作
	private float[] parameters_;
	//两组不同方向扰动的参数,不做归一化
	private float[][] newPara;
	private float[] pLower_;
	private float[] pUpper_;
	private double a_;
	private double A_;
	private double alpha_;
	private double gradientStep_;
	private double[] gradient_;
	private double c_;
	private double gamma_;
	private double perturbationStep_;
	private int[] perturbationValue_;
	private TaskCenter taskCenter;
	private int iterationLim;
	private double fitness;
	//二项分布
	private BinomialDistribution bd_;
	
	public SPSA(){
		//伯努利分布
		bd_ = new BinomialDistribution(1,0.5);
	}
	public SPSA(int dims){
		dims_ = dims;
		parameters_ = new float[dims];
		newPara = new float[3][dims];
		bd_ = new BinomialDistribution(1,0.5);
		gradient_ = new double[dims];
		perturbationValue_ = new int[dims];
	}
	public SPSA(int dims,String threadName, TaskCenter tc ){
		dims_ = dims;
		parameters_ = new float[dims];
		newPara = new float[3][dims];
		bd_ = new BinomialDistribution(1,0.5);
		gradient_ = new double[dims];
		perturbationValue_ = new int[dims];
		setName(threadName);
		taskCenter = tc;
		fitness = Double.MAX_VALUE;
	}
	public int getDimention(){
		return dims_;
	}
	public float[] getParameters(){
		return parameters_;
	}
	public void setAlgParameters(double a, double A, double alpha,double c,double gamma){
		a_ = a;
		A_ = A;
		alpha_ = alpha;
		c_ = c;
		gamma_ = gamma;
	}
	public void setBounderies(float[] plower, float[] pupper){
		pLower_ = plower;
		pUpper_ = pupper;
	}
	public void setParameters(float[] parameters){
		for(int i=0;i<parameters.length;i++){
			parameters_[i] = parameters[i];
			//归一化
			parameters_[i] = (parameters_[i] - pLower_[i]) / (pUpper_[i]- pLower_[i]);
		}
	}
	//反归一化
	public void inverseNomalization(float[] parameters){
		for(int i=0;i<parameters.length;i++){
			parameters[i] = parameters[i]*(pUpper_[i]- pLower_[i]) + pLower_[i];
		}
	}
	public float[] inverseNomal(float[] parameters){
		float[] tmp = new float[parameters.length];
		for(int i=0;i<parameters.length;i++){
			tmp[i] = parameters[i]*(pUpper_[i]- pLower_[i]) + pLower_[i];
		}
		return tmp;
	}
	public void getInverseNomal(float[] parameters){
		for(int i=0;i<parameters.length;i++){
			parameters[i] = parameters_[i]*(pUpper_[i]- pLower_[i]) + pLower_[i];
		}
//		return parameters;
	}
	//同时扰动参数
	public void perturbation(int k, float[] parameters1, float[] parameters2){
		perturbationStep_ = c_ / (Math.pow((k+1),gamma_));
		//重设时间种子
//		bd_.reseedRandomGenerator(System.currentTimeMillis());
		perturbationValue_ = bd_.sample(dims_);
		for(int i=0;i<dims_;i++){
			if(perturbationValue_[i] == 0)
				perturbationValue_[i] = -1;
			parameters1[i] = (float) (parameters_[i] + perturbationStep_ * perturbationValue_[i]); 
			parameters2[i] = (float) (parameters_[i] - perturbationStep_ * perturbationValue_[i]); 
		}
		//反归一化，用于仿真计算
		inverseNomalization(parameters1);
		inverseNomalization(parameters2);
//		System.out.println("");
	}
	//估计第k次迭代的梯度方向
	public void estimateGradient(double psim, double nsim){
		for(int i=0;i<dims_;i++){
			gradient_[i] = (psim-nsim)/(2 * perturbationStep_ * perturbationValue_[i]);
		}		
	}
	//梯度下降
	public void updateParameters(int k, float[] parameters){
		//更新第k次迭代的梯度下降步长
		gradientStep_ = a_/(Math.pow((A_+k+1), alpha_));
		for(int i=0;i<dims_;i++){
			parameters_[i] = (float) (parameters_[i] - gradientStep_ * gradient_[i]);
			parameters[i] = parameters_[i];
		}
		inverseNomalization(parameters);
	}
	public void updateParameters(int k){
		//更新第k次迭代的梯度下降步长
		gradientStep_ = a_/(Math.pow((A_+k+1), alpha_));
		for(int i=0;i<dims_;i++){
			parameters_[i] = (float) (parameters_[i] - gradientStep_ * gradient_[i]);
		}
	}
	public String showGBestPos() {
		String s = "";		
		for (int i = 0; i < newPara[2].length; i++) {
			s += String.valueOf( newPara[2][i]) + " ";
		}
		return s;
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
	@Override
	public void run(){
		for (int i = 0; i < iterationLim; i++) {
			taskCenter.setTaskAmount(3);

			//扰动参数，更新第一、二个engine的参数
			perturbation(i, newPara[0], newPara[1]);
			newPara[2] = inverseNomal(parameters_);
			long tb = System.currentTimeMillis();
			for (int j = 0; j < 3; j++) {
				double[] tmp = organizeTask(j, newPara[j]);
				try {
					taskCenter.undoneTasks.put(tmp);//dispatch task
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(taskCenter.getResult(2)<fitness)
				fitness = taskCenter.getResult(2);
			
			System.out.println("Gbest : " + fitness);
			System.out.println("Position : " + showGBestPos());
			System.out.println("Gneration " + i + " used " + ((System.currentTimeMillis() - tb)/1000) + " sec");
			if(i!=iterationLim-1){
				//梯度逼近
				estimateGradient(taskCenter.getResult(0), taskCenter.getResult(1));
				//更新spsa里面的parameter（属于[0,1]区间），同时更新第三个engine的参数
				updateParameters(i);
			}
		}
		taskCenter.Dismiss();//stop eng线程。
	}
	public static void main(String[] args) {
		int maxGeneration = 2000;
		int maxTasks = 100;
		TaskCenter tc = new TaskCenter(maxTasks);
		float[] plower = new float[]{18.0f,0.15f,1.0f,5.0f,25,85};
		float[] pupper = new float[]{23.0f,0.17f,4.0f,8.0f,35,95};//,180.0f,25,40,100};
		float[] pinit = new float[]{21.95f,0.156f,1.61f,6.31f,30.48f,91.44f};
        SPSA spsa = new SPSA(6,"SPSA",tc);
        //Spall建议20，100，0.602，1.9，0.101
        spsa.setAlgParameters(0.5, 50, 0.602, 0.1, 0.101);
        spsa.setBounderies(plower, pupper);
        spsa.setParameters(pinit);
		spsa.setMaxGeneration(maxGeneration);		
		spsa.start();
		MLPEngThread mlp_eng_thread;
		for (int i = 0; i < 3; i++) {
			mlp_eng_thread = new MLPEngThread("Eng"+i, tc);
			mlp_eng_thread.setMode(3);
//			((MLPEngine) mlp_eng_thread.engine).seedFixed = true;
			mlp_eng_thread.start();
		}
	}
}
