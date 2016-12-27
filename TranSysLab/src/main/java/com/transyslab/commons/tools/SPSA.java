package com.transyslab.commons.tools;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.stat.descriptive.SynchronizedMultivariateSummaryStatistics;

//ͬ���Ŷ�����ƽ��㷨
public class SPSA {
	private int dims_;
	//��Ҫ�궨�Ĳ�����������һ������
	private float[] parameters_;
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
	//����ֲ�
	private BinomialDistribution bd_;
	
	public SPSA(){
		//��Ŭ���ֲ�
		bd_ = new BinomialDistribution(1,0.5);
	}
	public SPSA(int dims){
		dims_ = dims;
		parameters_ = new float[dims];
		bd_ = new BinomialDistribution(1,0.5);
		gradient_ = new double[dims];
		perturbationValue_ = new int[dims];
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
			//��һ��
			parameters_[i] = (parameters_[i] - pLower_[i]) / (pUpper_[i]- pLower_[i]);
		}
	}
	//����һ��
	public void inverseNomalization(float[] parameters){
		for(int i=0;i<parameters.length;i++){
			parameters[i] = parameters[i]*(pUpper_[i]- pLower_[i]) + pLower_[i];
		}
	}
	public void getInverseNomal(float[] parameters){
		for(int i=0;i<parameters.length;i++){
			parameters[i] = parameters_[i]*(pUpper_[i]- pLower_[i]) + pLower_[i];
		}
//		return parameters;
	}
	//ͬʱ�Ŷ�����
	public void perturbation(int k, float[] parameters1, float[] parameters2){
		perturbationStep_ = c_ / (Math.pow((k+1),gamma_));
		//����ʱ������
//		bd_.reseedRandomGenerator(System.currentTimeMillis());
		perturbationValue_ = bd_.sample(dims_);
		for(int i=0;i<dims_;i++){
			if(perturbationValue_[i] == 0)
				perturbationValue_[i] = -1;
			parameters1[i] = (float) (parameters_[i] + perturbationStep_ * perturbationValue_[i]); 
			parameters2[i] = (float) (parameters_[i] - perturbationStep_ * perturbationValue_[i]); 
		}
		//����һ�������ڷ������
		inverseNomalization(parameters1);
		inverseNomalization(parameters2);
//		System.out.println("");
	}
	//���Ƶ�k�ε������ݶȷ���
	public void estimateGradient(double psim, double nsim){
		for(int i=0;i<dims_;i++){
			gradient_[i] = (psim-nsim)/(2 * perturbationStep_ * perturbationValue_[i]);
		}		
	}
	//�ݶ��½�
	public void updateParameters(int k, float[] parameters){
		//���µ�k�ε������ݶ��½�����
		gradientStep_ = a_/(Math.pow((A_+k+1), alpha_));
		for(int i=0;i<dims_;i++){
			parameters_[i] = (float) (parameters_[i] - gradientStep_ * gradient_[i]);
			parameters[i] = parameters_[i];
		}
		inverseNomalization(parameters);
	}
}
