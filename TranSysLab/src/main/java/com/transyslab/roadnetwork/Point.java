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

	public Point() {
		locationX = 0;
		locationY = 0;
	}
	public Point(double x, double y) {
		locationX = x;
		locationY = y;
	}
	public Point(Point spt, Point ept, double r) {
		this.locationX = r * spt.locationX + (1.0 - r) * ept.locationX;
		this.locationY = r * spt.locationY + (1.0 - r) * ept.locationY;
	}
	public int init(double x, double y) {
		locationX = x;
		locationY = y;
		return 0;
	}

	/*
	 * Interpolate a point between this and p with ratio r. Notice that r
	 * corresponses to the distance from p.
	 */

	public Point intermediate(Point p, double r) {
		return new Point(r * locationX + (1.0 - r) * p.locationX, r * locationY + (1.0 - r) * p.locationY);
	}
	public double getLocationX() {
		return locationX;
	}
	public double getLocationY() {
		return locationY;
	}
	public double getEast() {
		return locationX;
	}
	public double getNorth() {
		return locationY;
	}
	public void setCoodinate(double x, double y) {
		locationX = x;
		locationY = y;
	}
	public double distance_squared(Point p) {
		double n_diff = p.locationY - locationY;
		double e_diff = p.locationX - locationX;
		return (n_diff * n_diff + e_diff * e_diff);
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

}
