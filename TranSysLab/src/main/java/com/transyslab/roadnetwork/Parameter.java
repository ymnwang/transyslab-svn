/**
 *
 */
package com.transyslab.roadnetwork;

import java.util.HashMap;

/**
 * @author YYL
 *
 */

public class Parameter {

	protected static String name_; // file name

	// Constants for transfering between I/O and internal
	// units. Internal units are in metric system

	protected static float lengthFactor_ = 0.3048f; // length and coordinates
													// (1=meter)
	protected static float speedFactor_ = 0.4470f; // speed (1=km/hr)
	protected static float densityFactor_ = 0.6214f; // density (1=vehicles/km)
	protected static float flowFactor_ = 1.0000f; // flow & capacity
													// (1=vehicles/hr)
	protected static float timeFactor_ = 60.0000f; // travel time (1=minutes)
	protected static float odFactor_ = 1.0000f;

	protected static String densityLabel_ = "Density(vpm)";
	protected static String speedLabel_ = "Speed(mph)";
	protected static String flowLabel_ = "Flow(vph)";
	protected static String occupancyLabel_ = "Occupancy(%)";

	protected float visibilityScaler_;
	protected float visibility_;

	protected static int resolution_[]; // for view accuracy

	protected float pathAlpha_ = 0.5f; // parameter for updating travel time

	// For route choice model

	protected static float[][] routingParams_ = new float[2][2]; // in logic
																	// route
																	// choice
																	// model
	protected float commonalityFactor_ = 0.0f; // in path choice model
	protected float[] diversionPenalty_ = {300}; // cost added in util func
	protected float validPathFactor_ = 1.5f; // compared to shorted path
	protected float rationalLinkFactor_ = 0.0f; // reduces irrational link choices
	protected float freewayBias_ = 1.0f; // travel time
	protected float busToStopVisibility_; // distance from bus stop at which bus
											// begins to change lanes
	protected float busStopSqueezeFactor_; // reduction in speed in lane next to
											// bus at a stop

	// Check if the two tokens are the same

	// protected int isEqual(const char *s1, const char *s2);

	public Parameter() {
		routingParams_[0][0] = 0.7f;
		routingParams_[0][1] = -5.0f;
		routingParams_[1][0] = 0.3f;
		routingParams_[1][1] = -5.0f;
	}
	public static Parameter getInstance() {
		HashMap<String, Integer> hm = RoadNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		return RoadNetworkPool.getInstance().getParameter(threadid);
	}

	public String getName() {
		return name_;
	}
	// public char ** nameptr() { return &name_; }
	public void setName(String s) {
		name_ = s;
	}

	// public static int error(const char *);

	// Unit transfer

	public static float lengthFactor() {
		return lengthFactor_;
	}
	public static float speedFactor() {
		return speedFactor_;
	}
	public static float densityFactor() {
		return densityFactor_;
	}
	public static float flowFactor() {
		return flowFactor_;
	}
	public static float timeFactor() {
		return timeFactor_;
	}

	public static String densityLabel() {
		return densityLabel_;
	}
	public static String speedLabel() {
		return speedLabel_;
	}
	public static String flowLabel() {
		return flowLabel_;
	}
	public static String occupancyLabel() {
		return occupancyLabel_;
	}

	public static int resolution(int i) {
		return resolution_[i];
	}
	// public static int loadResolution(GenericVariable &);

	public float pathAlpha() {
		return pathAlpha_;
	}

	// Route choice

	public float guidedRate() {
		return routingParams_[1][0];
	}
	public static float routingBeta(int type) {
		return routingParams_[type][1];
	}
	public float commonalityFactor() {
		return commonalityFactor_;
	}
	public float diversionPenalty() {
		return diversionPenalty_[0];
	}
	public float rationalLinkFactor() {
		return rationalLinkFactor_;
	}
	public float busToStopVisibility() {
		return busToStopVisibility_;
	}
	public float busStopSqueezeFactor() {
		return busStopSqueezeFactor_;
	}
	public float pathDiversionPenalty() {
		return diversionPenalty_[1];
	}

	public float validPathFactor() {
		return validPathFactor_;
	}
	public float freewayBias() {
		return freewayBias_;
	}

	// TS_Parameters for responding traffic ocntrols

	public float visibilityScaler() { // virtual
		return visibilityScaler_;
	}
	public float visibility() {
		return visibility_;
	}
	// public void checkVisibility();

	// public int parseVariable(GenericVariable &gv);

	// private int loadRouteChoiceParas(GenericVariable &);
	// private int loadDiversionPenalty(GenericVariable &);
}
