package com.transyslab.simcore.mlp;

import java.util.HashMap;
import com.transyslab.roadnetwork.Parameter;
import com.transyslab.commons.tools.SimulationClock;

public class MLPParameter extends Parameter {
	private float startTime_ = 28800;
	private float endTime_ = 32400;
	private double simStep_ = 0.1;
	

	final static float ETC_RATE = 0.3f;
	final static float HOV_RATE = 0.25f;

	final static float VEHICLE_LENGTH = 6.0960f; // 单位米， 20 feet

	final static float CELL_RSP_LOWER = 30.48f; // 单位米，about 200 feet
	final static float CELL_RSP_UPPER = 91.44f; // 单位米，about 500 feet
	final static float CHANNELIZE_DISTANCE = 60.96f; // 单位米，about 400 feet

	protected int nVehicleClasses_;
	protected float[] vehicleClassCDF_;
	protected String[] vehicleName_;
	protected double updateStepSize_;// 从MESO_Engine转移过来
	protected float hovRate_;
	protected float etcRate_;
	protected float[] vehicleLength_; // meter

	protected float[] limitingParam_; // min headway, headway/speed slope,
	// max acc, min speed, etc.

	protected float[] queueParam_; // max speed for queue releasing

	protected float cellSplitGap_; // gap threshold for split a cell
	protected float rspLower_;
	protected float rspUpper_;
	protected float channelizeDistance_; // from dnNode (meter)

	public static MLPParameter getInstance() {
		HashMap<String, Integer> hm = MLPNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		return MLPNetworkPool.getInstance().getParameter(threadid);
	}

	public MLPParameter() {
		nVehicleClasses_ = 1;
		vehicleClassCDF_ = new float[1];
		vehicleLength_ = new float[1];
		vehicleName_ = new String[1];
		etcRate_ = ETC_RATE;
		hovRate_ = HOV_RATE;
		rspLower_ = CELL_RSP_LOWER;
		rspUpper_ = CELL_RSP_UPPER;
		cellSplitGap_ = 0.5f * (CELL_RSP_LOWER + CELL_RSP_UPPER);
		channelizeDistance_ = CHANNELIZE_DISTANCE;
		// limitingParam_ = null;
		// queueParam_ = null;
		limitingParam_ = new float[3];
		queueParam_ = new float[3];

		vehicleClassCDF_[0] = 1.0f;
		vehicleLength_[0] = VEHICLE_LENGTH;
		vehicleName_[0] = new String("Cars");
		// 从mesolib文件读入的默认参数值
		limitingParam_[0] = (float) (5.0 * lengthFactor_);
		limitingParam_[1] = 1.36f;
		limitingParam_[2] = (float) (5.0 * speedFactor_);// km/hour
		queueParam_[0] = -0.001f;
		queueParam_[1] = (float) (25.0 * speedFactor_);
		queueParam_[2] = 100.0f;// seconds
	}
	public double getUpdateStepSize() {
		return updateStepSize_;
	}
	public void setUpdateStepSize(double uss) {
		updateStepSize_ = uss;
	}
	public int nVehicleClasses() {
		return nVehicleClasses_;
	}
	public float[] vehicleClassCDF() {
		return vehicleClassCDF_;
	}
	public float vehicleClassCDF(int i) {
		return vehicleClassCDF_[i];
	}
	public float hovRate() {
		return hovRate_;
	}
	public float etcRate() {
		return etcRate_;
	}
	public float vehicleLength(int i) {
		if (i >= 0 && i < nVehicleClasses_)
			return vehicleLength_[i];
		else
			return vehicleLength_[0];
	}
	public String vehicleName(int i) {
		return vehicleName_[i];
	}

	// This returns the minimum distance gap for a given speed
	public float minGap(float speed) {
		return minHeadwayGap() + headwaySpeedSlope() * speed;
	}

	// This returns a maximum speed for a give gap, in a n-lane segment
	float maxSpeed(float gap, int n) {
		float dt = (float) (SimulationClock.getInstance().getStepSize() + headwaySpeedSlope() / n);
		float dx = gap - minHeadwayGap() / n;
		float v = dx / dt;
		if (v > 40)
			return 40;
		return (v > 0.0) ? v : 0.0f;
	}

	public float cellSplitGap() {
		return cellSplitGap_;
	}
	public float rspLower() {
		return rspLower_;
	}
	public float rspUpper() {
		return rspUpper_;
	}
	public float rspRange() {
		return rspUpper_ - rspLower_;
	}
	public void setCellSplitGap(float dmax) {
		cellSplitGap_ = dmax;
	}
	public void updateCSG() {
		cellSplitGap_ = 0.5f * (rspLower_ + rspUpper_);
	}
	public void setRspLower(float dmin) {
		rspLower_ = dmin;
	}
	public void setRspUpper(float dupper) {
		rspUpper_ = dupper;
	}
	public float channelizeDistance() {
		return channelizeDistance_;
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

	public float queueReleasingSpeed(float t, float v_f) {
		if (t > queueParam_[2])
			return v_f;

		float r = 1.0f - (float) Math.exp(queueParam_[0] * t * t);
		return queueParam_[1] + (v_f - queueParam_[1]) * r;
	}


}
