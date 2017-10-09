package com.transyslab.simcore.mlp;

import com.transyslab.roadnetwork.Parameter;
import com.transyslab.simcore.mlp.Functions.FunsCombination1;
import com.transyslab.simcore.mlp.Functions.FunsCombination2;
import com.transyslab.simcore.mlp.Functions.TSFun;
import com.transyslab.commons.tools.optimizer.DE;

public class MLPParameter extends Parameter {
	public static final double[] DEFAULT_PARAMETERS = new double[] {0.5122,20.37,0.1928,45.50056,0.92191446,7.792739,1.6195029,0.6170239};
	private double SegLenBuff_;//ǿ�ƻ����߽磨segmentʵ�߳��ȣ�meter
	private double LCBuffTime_;//Lane ChangingӰ��ʱ�� / �������������ȴʱ��
	private int LCBuff_;//LCBuffTmieת��Ϊ֡��Frame or fin
	protected double updateStepSize_;//update�׶�ʱ��
	protected double LCDStepSize_;//Lane changing decision ��������ʱ����
	protected double capacity;// veh/s/lane
	protected float CELL_RSP_LOWER; // ��λ�ף�about 200 feet 30.48f
	protected float CELL_RSP_UPPER; // ��λ�ף�about 500 feet 91.44f
	protected float CF_FAR;
	protected float CF_NEAR;
	protected double PHYSICAL_SPD_LIM;
	private double [] SDPara_;//[0]VMax m/s; [1]VMin m/s; [2]KJam veh/m; [3]Alpha a.u.; [4]Beta a.u.;
	private double [] LCPara_;//[0]gamma1 a.u.; [1]gamma2 a.u.;	
	protected float[] limitingParam_; // [0] stopping gap (m); [1] moving time gap (t); [2] ?
//	protected float[] queueParam; // max speed for queue releasing
	final static float VEHICLE_LENGTH = 6.0960f; // ��λ�ף� 20 feet	
//	final static double CF_NEAR = 0.1;//meter
	final static double SEG_NEAR = 1.0;//meter
	final static double LC_Lambda1 = 18.4204;//����logitģ�ͳ�����
	final static double LC_Lambda2 = -9.2102;//����logitģ�ͳ�����
//	final static double PHYSICAL_SPD_LIM = 120/3.6; // meter/s
	/*public KSDM_Eq ksdm_Eq;
	public QSDFun_Eq qsdFun_Eq;
	public QSDM_Eq qsdm_Eq;*/
	public FunsCombination1 funsCombination1;
	public FunsCombination2 funsCombination2;
	public FunsCombination1 funsCombination3;
	public TSFun tsFun;
	private DE de;
	private double simStepSize;

	//���ʱ������
	protected double statWarmUp;
	protected double statStepSize;//stat(ͳ��)�׶�ʱ������λ����

	public MLPParameter() {
		SegLenBuff_ = 10.0;
		LCBuffTime_ = 2.0;
		updateStepSize_ = 10.0;
		LCDStepSize_ = 0.0;//2.0
		statStepSize = 300.0;//Ĭ��5���ӽ���ͳ�ƣ������ʼ��ʱ��ȡmaster�ļ��Ḳ�����ֵ��
		statWarmUp = 300.0;//Ĭ��5���ӽ���Ԥ�ȣ������ʼ��ʱ��ȡmaster�ļ��Ḳ�����ֵ��
		capacity = 0.5;//default 0.5
		CELL_RSP_LOWER = 30.87f;
		CELL_RSP_UPPER = 91.58f;
		CF_FAR = 91.58f;
		CF_NEAR = (float) (5.0 * lengthFactor);
		PHYSICAL_SPD_LIM = 120/3.6; // meter/s
		SDPara_ = new double [] {16.67, 0.0, 0.180, 1.8, 5.0};//ԭ{16.67, 0.0, 0.180, 5.0, 1.8}{19.76, 0.0, 0.15875, 2.04, 5.35}
		LCPara_ = new double [] {20.0, 20.0};
		limitingParam_ = new float[3];
//		queueParam = new float[3];
		//��mesolib�ļ������Ĭ�ϲ���ֵ
		limitingParam_[0] = (float) (5.0 * lengthFactor);// turn to meter
		limitingParam_[1] = 1.36f;
		limitingParam_[2] = (float) (5.0 * speedFactor);// turn to km/hour
//		SEG_NEAR = SDPara_[0]*SimulationClock.getInstance().getStepSize();
//		queueParam[0] = -0.001f;
//		queueParam[1] = (float) (25.0 * speedFactor);
//		queueParam[2] = 100.0f;// seconds
		funsCombination1 = new FunsCombination1();
		funsCombination2 = new FunsCombination2();
		funsCombination3 = new FunsCombination1();
		tsFun = new TSFun();
		de = new DE();
		simStepSize = 0.0;
	}
	public double getUpdateStepSize() {
		return updateStepSize_;
	}
	public void setUpdateStepSize(double uss) {
		updateStepSize_ = uss;
	}
	public double getLCDStepSize() {
		return LCDStepSize_;
	}
	public void setLCDStepSize(double lcd_ss){
		LCDStepSize_ = lcd_ss;
	}

	// This returns the minimum distance gap for a given speed
	public double minGap(double speed) {
		return minHeadwayGap() + headwaySpeedSlope() * speed;
	}
	
	//This returns a maximum speed for a give gap, in specific MLPlane, provide for MLP model
	public double maxSpeed(double gap) {
		double dt = simStepSize + headwaySpeedSlope();
		double dx = gap - minHeadwayGap();
		return Math.max(0.0, Math.min(PHYSICAL_SPD_LIM, dx/dt));
	}
	
	public float minHeadwayGap() {
		return limitingParam_[0];
	}
	public float headwaySpeedSlope() {
		return limitingParam_[1];
	}
	public float minSpeed() {
		return limitingParam_[2];
	}

/*	public float queueReleasingSpeed(float t, float v_f) {
		if (t > queueParam[2])
			return v_f;
		float r = 1.0f - (float) Math.exp(queueParam[0] * t * t);
		return queueParam[1] + (v_f - queueParam[1]) * r;
	}*/
	
	public void setSegLenBuff(double val){
		SegLenBuff_ = val;
	}
	public double getSegLenBuff() {
		return SegLenBuff_;
	}
	
	public void setLCBuffTime(double val){
		LCBuffTime_ = val;
		LCBuff_ = (int) Math.floor(LCBuffTime_ / simStepSize);
	}
	public double getLCBuffTime() {
		return LCBuffTime_;
	}
	public int getLCBuff() {
		if (LCBuff_ == 0) 
			LCBuff_ = (int) Math.floor(LCBuffTime_ / simStepSize);
		return LCBuff_;
	}
	
	public void setSDPara(double [] val){
		if (val.length != 5) {
			System.out.println("fail setting SDPara: Length error");
			return;
		}
		SDPara_ = val;
	}	
	public double [] getSDPara(){
		return (SDPara_);
	}
	
	public void setLCPara(double [] val){
		if (val.length != 2) {
			System.out.println("fail setting LCPara: Length error");
			return;
		}
		LCPara_ = val;
	}
	public double [] getLCPara(){
		return LCPara_;
	}
	public void setDUpper(float arg) {
		CELL_RSP_UPPER = arg;
	}
	public void setDLower(float arg) {
		CELL_RSP_LOWER = arg;
	}

	public void setSimStepSize(double arg) {
		simStepSize = arg;
	}

	public void setPhyLim(double arg) {
		PHYSICAL_SPD_LIM = arg;
	}

	public static double xcLower(double kj, double qm, double deltat) {
		//xcȡֵ��Ҫ���ڴ�ֵ(������
		return 1.0/kj/(1-qm*deltat);
	}

	public static double deltaTUpper(double vf, double kj, double qm) {
		//deltaTȡֵ��ҪС�ڴ�ֵ��������
		return 1.0/qm - 1.0/vf/kj;
	}

	public static boolean isVpFastEnough(double vf, double vp) {
		return vp>vf;
	}

	public static double rUpper(double start, double vf, double kj, double qm) {//����startȡ10
		double epsilon = 1e-12;
		double x = start;
		double f = funcG(x,vf,kj,qm);
		int iterTimes = 0;
		while (Math.abs(f)>epsilon) {
			if (iterTimes>1e6) {
				System.err.println("���̲�����");
				return Double.NaN;
			}
			x = x - f*epsilon/(funcG(x+epsilon,vf,kj,qm)-f);//����x(k+1)
			f = funcG(x,vf,kj,qm);
			iterTimes += 1;
		}
		if (x<0 || Double.isNaN(f)) {
			return rUpper(start*10, vf, kj, qm);
		}
		return x;
	}

	public static double funcG(double r, double vf, double kj, double qm) { // ��������
		return (r*r-r)*Math.pow(Math.log(r),2) + 4*Math.log(qm/kj/vf)*Math.log(1+r);
	}

	public static double calcTs(double Xc, double vf, double kj, double qm, double vp, double deltaT) {
		double x1 = vf / qm;
		double x2 = vp / qm;

		if (Xc<x1) {
			return 1/qm - 1/vf/kj - deltaT;
		}
		else if (Xc<x2) {
			return 1/qm - 1/qm/kj/Xc - deltaT;
		}
		else
			return 1/qm - 1/vp/kj - deltaT;
	}

	public static double calcAlpha(double r, double vf, double kj, double qm) {
		double delta = r*Math.pow(Math.log(r),2) - 4*Math.log(qm/kj/vf)*Math.log(1+r);
		return (r*Math.log(r)-Math.sqrt(delta)) / 2 / Math.log(qm/kj/vf);
	}
}
