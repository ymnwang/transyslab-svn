package com.transyslab.roadnetwork;

public interface Sensor {
	
	public void measure(float speed);
	
	public void aggregate();
	
	public void record();
	
	public void createSurface();
	
	public float getPosition();
	 
}
