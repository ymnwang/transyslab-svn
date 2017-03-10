/**
 *
 */
package com.transyslab.roadnetwork;

/**
 * @author YYL 2016-6-1
 */
public class Point {
	public static final double POINT_EPSILON = 0.1;
	private double locationX;
	private double locationY;
	private double locationZ;

	public Point() {
		locationX = 0;
		locationY = 0;
		locationZ = 0;
	}
	//二维坐标
	public Point(double x, double y) {
		locationX = x;
		locationY = y;
		locationZ = 0;
	}
	//三维坐标
	public Point(double x, double y, double z) {
		locationX = x;
		locationY = y;
		locationZ = z;
	}
	public Point(Point spt, Point ept, double r) {
		this.locationX = r * spt.locationX + (1.0 - r) * ept.locationX;
		this.locationY = r * spt.locationY + (1.0 - r) * ept.locationY;
		this.locationZ = r * spt.locationZ + (1.0 - r) * ept.locationZ;
	}

	/*
	 * Interpolate a point between this and p with ratio r. Notice that r
	 * corresponses to the distance from p.
	 */

	public Point intermediate(Point p, double r) {
		return new Point(r * locationX + (1.0 - r) * p.locationX, r * locationY + (1.0 - r) * p.locationY, 
				         r * locationZ + (1.0 - r) * p.locationZ);
	}
	public double getLocationX() {
		return locationX;
	}
	public double getLocationY() {
		return locationY;
	}
	public double getLocationZ(){
		return locationZ;
	}
	public double getEast() {
		return locationX;
	}
	public double getNorth() {
		return locationY;
	}
	public double getHeight(){
		return locationZ;
	}

	public double distance_squared(Point p) {
		double n_diff = p.locationY - locationY;
		double e_diff = p.locationX - locationX;
		double h_diff = p.locationZ - locationZ;
		return (n_diff * n_diff + e_diff * e_diff + h_diff * h_diff);
	}
	// Distance to another point
	public double distance(Point p) {
		return Math.sqrt(distance_squared(p));
	}
	public boolean equal(Point p, double epsilon) {
		return ((Math.abs(locationY - p.locationY) < epsilon) && (Math.abs(locationX - p.locationX) < epsilon));
	}
	/*
	 * Compute the angle (in radian) of a line from this point to point 'pnt'.
	 */
	public double angle(Point pnt) {
		double dx, dy, dis, alpha;
		dx = pnt.locationX - locationX;
		dy = pnt.locationY - locationY;
		dis = dx * dx + dy * dy;

		if (dis > 1.0E-10) {
			alpha = Math.acos(dx / Math.sqrt(dis));
			if (dy < 0.0)
				alpha = 2 * Math.PI - alpha;
		}
		else {
			alpha = 0.0;
		}

		return alpha;
	}
	// Calculate a point with a distance of 'offset' and an angle of
	// 'alpha' wrt point 'p'. The angle must be in radian.
	public Point bearing(double offset, double alpha) {
		return new Point(locationX + offset * Math.cos(alpha), locationY + offset * Math.sin(alpha));
	}
	public void setLocation(double x, double y, double z) {
		locationX = x;
		locationY = y;
		locationZ = z;	
	}

}
