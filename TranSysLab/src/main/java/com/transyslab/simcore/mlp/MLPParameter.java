package com.transyslab.simcore.mlp;

import com.transyslab.roadnetwork.Parameter;
import com.transyslab.simcore.mlp.Functions.FunsCombination1;
import com.transyslab.simcore.mlp.Functions.FunsCombination2;
import com.transyslab.simcore.mlp.Functions.TSFun;
import com.transyslab.commons.tools.optimizer.DE;

public class MLPParameter extends Parameter {		
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
	private double [] SDPara_;//[0]VMax m/s; [1]VMin m/s; [2]KJam veh/m; [3]Alpha a.u.; [4]Beta a.u.;
	private double [] LCPara_;//[0]gamma1 a.u.; [1]gamma2 a.u.;	
	protected float[] limitingParam_; // [0] stopping gap (m); [1] moving time gap (t); [2] ?
//	protected float[] queueParam; // max speed for queue releasing
	final static float VEHICLE_LENGTH = 6.0960f; // ��λ�ף� 20 feet	
//	final static double CF_NEAR = 0.1;//meter
	final static double SEG_NEAR = 1.0;//meter
	final static double LC_Lambda1 = 18.4204;//����logitģ�ͳ�����
	final static double LC_Lambda2 = -9.2102;//����logitģ�ͳ�����
	final static double PHYSICAL_SPD_LIM = 120/3.6; // meter/s
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

	public double[] genSolution(double[] obsParas, double xc, double kstar){//QM,VF,Kj
		double[] XC = new double[]{xc};
		//deltaT,VP
		double deltaT = simStepSize;
		tsFun.setParas(obsParas,deltaT ,PHYSICAL_SPD_LIM);
		double tsValue = tsFun.cal(XC);
		// ��k = k1
		double k1 = obsParas[0]/PHYSICAL_SPD_LIM;
		funsCombination1.setParas(k1, obsParas[0], obsParas[1], obsParas[2], kstar);
		funsCombination2.setParas(obsParas[1], obsParas[2], obsParas[0], kstar);
		double k2 = (1-obsParas[0]*(tsValue+deltaT))*obsParas[2];
		// ��k = k2
		funsCombination3.setParas(k2, obsParas[0], obsParas[1], obsParas[2], kstar);
		double[] results ;
		double[] initValue = new double[]{0.01,0.01};
		if(kstar<k1)
			results = de.solve(funsCombination1, new float[]{0.01f,0.01f}, new float[]{10.0f,10.0f});//BroydenMethod.solve(funsCombination1, initValue);
		else if(k1<=kstar&&kstar<k2)
			results = de.solve(funsCombination2, new float[]{0.01f,0.01f}, new float[]{10.0f,10.0f});
		else if(kstar>k2)
			results = de.solve(funsCombination3, new float[]{0.01f,0.01f}, new float[]{10.0f,10.0f});
		else{
			System.out.println("check kstar!");
			results = new double[]{-1,-1};
		}
		double[] finalRes = new double[3];
		finalRes[0] = tsValue;
		System.arraycopy(results, 0, finalRes, 1, results.length);
		return finalRes;
	}
	public double genSolution2(double[] obsParas, double xc){//QM,VF,Kj
		double[] XC = new double[]{xc};
		//deltaT,VP
		double deltaT = simStepSize;
		tsFun.setParas(obsParas,deltaT ,PHYSICAL_SPD_LIM);
		double tsValue = tsFun.cal(XC);
		return tsValue;
	}
	public boolean constraints(double[] paras) {
		double Qm = paras[0];
		double Vf = paras[1];
		double Kj = paras[2];
		
		double ts = paras[3];
		double xc = paras[4];
		double alpha =paras[5];
		double beta = paras[6];
		double gamma1 = paras[7];
		double gamma2 = paras[8];
		
		double delta_t = simStepSize;
		double leff = 1/Kj;
		double Vp = PHYSICAL_SPD_LIM;
		boolean check1 = false;
		check1 |= (ts==1/Qm - leff/Vf - delta_t && xc<=Vf/Qm) || //condition 1
						 (xc==leff/(1-Qm*(delta_t+ts)) && ts < 1/Qm-delta_t
								 										  && ts < (Vf-Qm*leff)/Qm/Vf-delta_t
								 										  && ts <= (Vp-Qm*leff)/Qm/Vp
								 										  && Vp-Qm*leff>0) || //condition 2
						 (ts==1/Qm-leff/Vp-delta_t && xc>Vp*(delta_t+ts)+leff); //condition 3
		
		boolean check2;
		double xb = Vp*(delta_t+ts)+leff, Qb = Vp/xb;
		double Km_star = Kj/Math.pow(1+alpha*beta, 1/alpha); 
		double Qm_star = Km_star * Vf * Math.pow(1-Math.pow(Km_star/Kj, alpha), beta);
		//double root = solve((1/x-leff)/(delta_t+ts)==Vf*(1-(x/Kj)^alpha)^beta);//�����Է��̣�Ҫ�����Ӳ��ܽ⣻��ʱ�������������
		check2 =  Qb/(Kj-1/xb)>Qm_star/(Kj-Km_star) ? Qm==Qm_star : true; //Qm==root*(1/root-leff)/(delta_t+ts);
		return check1 && check2;
	}

	public void setSimStepSize(double arg) {
		simStepSize = arg;
	}
}
