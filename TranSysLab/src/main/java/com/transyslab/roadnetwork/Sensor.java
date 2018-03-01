package com.transyslab.roadnetwork;

public interface Sensor extends NetworkObject{
	
	void measure(double speed);
	
	void aggregate(double curTime);
	
	void createSurface();
	
	double getPosition();

	void clean();

	GeoSurface getSurface();

	String getName();

}
