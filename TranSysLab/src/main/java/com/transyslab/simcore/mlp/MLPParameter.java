package com.transyslab.simcore.mlp;

import java.util.HashMap;
import com.transyslab.roadnetwork.Parameter;
import com.transyslab.commons.tools.SimulationClock;

public class MLPParameter extends Parameter {		
	private double SegLenBuff_;//强制换道边界（segment实线长度）meter
	private double LCBuffTime_;//Lane Changing影响时长 / 单车换道后的冷却时间
	private int LCBuff_;//LCBuffTmie转换为帧数Frame or fin
	protected double updateStepSize_;//update阶段时长
	protected double LCDStepSize_;//Lane changing decision 换道决策时间间隔
	protected double capacity;// veh/s/lane
	protected float CELL_RSP_LOWER; // 单位米，about 200 feet 30.48f
	protected float CELL_RSP_UPPER; // 单位米，about 500 feet 91.44f
	private double [] SDPara_;//[0]VMax m/s; [1]VMin m/s; [2]KJam veh/m; [3]Alpha a.u.; [4]Beta a.u.;
	private double [] LCPara_;//[0]gamma1 a.u.; [1]gamma2 a.u.;	
	protected float[] limitingParam_; // [0] stopping gap (m); [1] moving time gap (t); [2] ?
//	protected float[] queueParam_; // max speed for queue releasing
	final static float VEHICLE_LENGTH = 6.0960f; // 单位米， 20 feet	
	final static double CF_NEAR = 0.1;//meter
	final static double SEG_NEAR = 1.0;//meter
	final static double PHYSICAL_SPD_LIM = 120/3.6; // meter/s

	public static MLPParameter getInstance() {
		HashMap<String, Integer> hm = MLPNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		return MLPNetworkPool.getInstance().getParameter(threadid);
	}

	public MLPParameter() {
		SegLenBuff_ = 10.0;
		LCBuffTime_ = 2.0;
		updateStepSize_ = 10.0;
		LCDStepSize_ = 2.0;
		capacity = 0.5;//default 0.5
		CELL_RSP_LOWER = 30.87f;
		CELL_RSP_UPPER = 91.58f;
		SDPara_ = new double [] {16.67, 0.0, 0.180, 5.0, 1.8};//原{16.67, 0.0, 0.180, 5.0, 1.8}{19.76, 0.0, 0.15875, 2.04, 5.35}
		LCPara_ = new double [] {10.0, 10.0};
		limitingParam_ = new float[3];
//		queueParam_ = new float[3];
		//从mesolib文件读入的默认参数值
		limitingParam_[0] = (float) (5.0 * lengthFactor_);// turn to meter
		limitingParam_[1] = 1.36f;
		limitingParam_[2] = (float) (5.0 * speedFactor_);// turn to km/hour
//		SEG_NEAR = SDPara_[0]*SimulationClock.getInstance().getStepSize();
//		queueParam_[0] = -0.001f;
//		queueParam_[1] = (float) (25.0 * speedFactor_);
//		queueParam_[2] = 100.0f;// seconds
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
		double dt = SimulationClock.getInstance().getStepSize() + headwaySpeedSlope();
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
		if (t > queueParam_[2])
			return v_f;
		float r = 1.0f - (float) Math.exp(queueParam_[0] * t * t);
		return queueParam_[1] + (v_f - queueParam_[1]) * r;
	}*/
	
	public void setSegLenBuff(double val){
		SegLenBuff_ = val;
	}
	public double getSegLenBuff() {
		return SegLenBuff_;
	}
	
	public void setLCBuffTime(double val){
		LCBuffTime_ = val;
		LCBuff_ = (int) Math.floor(LCBuffTime_ / SimulationClock.getInstance().getStepSize());
	}
	public double getLCBuffTime() {
		return LCBuffTime_;
	}
	public int getLCBuff() {
		if (LCBuff_ == 0) 
			LCBuff_ = (int) Math.floor(LCBuffTime_ / SimulationClock.getInstance().getStepSize());
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

}
