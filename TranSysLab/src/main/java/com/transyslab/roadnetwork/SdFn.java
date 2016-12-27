/**
 *
 */
package com.transyslab.roadnetwork;

/**
 *
 * @author YYL 2016-6-6
 */
public class SdFn {
	
	//路段末交通流最短车头时距
	protected float capacity_;
	protected float jamSpeed_;
	protected float jamDensity_;
	//覆盖segment的freeSpeed
	protected float freeSpeed_;

	public float getJamDensity() {
		return jamDensity_;
	}
	public float getCapacity() {
		return capacity_;
	}
	public float getFreeSpeed(){
		return freeSpeed_;
	}
	public void setFreeSpeed(float speed){
		freeSpeed_ = speed;
	}
	public void setCapacity(float cap){
		capacity_ = cap;
	}
	public void setJamSpeed(float speed){
		jamSpeed_ = speed;
	}
	/*
	public void updateParameters(float cap, float minspeed, float maxspeed, float maxdensity){
		capacity_ = cap;
		jamSpeed_ = minspeed;
		freeSpeed_  = maxspeed;
		jamDensity_ = maxdensity;
	}*/
	//注释传参freeSpeed
	public float densityToSpeed(/*float freeSpeed, */float density, int nlanes) {
		return freeSpeed_;
	}

	protected SdFn() {
		capacity_ = 0.5f; // 1800 vph in vps
		jamSpeed_ = 0; // 5 mph in mps
		jamDensity_ = 180.0f; // in vehicles/km
		freeSpeed_ = 16.67f;// 60km/h = 16.67m/s
	}
	protected SdFn(float cap, float spd, float kjam) {
		// capacity_ = (float) (cap / 3600.0);
		capacity_ = cap;
		jamSpeed_ = spd;// * Parameter.speedFactor();
		jamDensity_ = kjam;// * Parameter.densityFactor();
	}

}
