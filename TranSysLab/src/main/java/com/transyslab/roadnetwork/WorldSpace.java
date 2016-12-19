/**
 *
 */
package com.transyslab.roadnetwork;

/**
 * 世界坐标
 *
 * @author YYL extern RN_WorldSpace * theWorldSpace; = this RN_WorldSpace *
 *         theWorldSpace = NULL;
 *
 *         // We initialize the north east point to - LARGE_NUMBER and the south
 *         // west point to + LARGE_NUMBER to ensure that the first network
 *         point // compared in recordExtremePoints will replace the initial
 *         values.
 *
 */
public class WorldSpace {

	// These two coordinates define the boundaries of world space.
	// They will be set dynamically as a network is loaded, and at
	// the end of the load they will coorespond to the maximum end
	// points of the network

	private Point northEastPnt;
	private Point southWestPnt;

	// These are point in world space

	private Point lowLeftPnt; // for sw_pnt_, should be (0, 0)
	private Point topRightPnt; // for ne_pnt_

	// These varables are set by function createWorldSpace(), which
	// is called after the network has been loaded. Their values
	// will not change afterwards.

	private double width; /* west-east width */
	private double height; /* south-north height */
	private Point center;
	private double angle; /*
							 * angle from center to farthest east point
							 */
	public WorldSpace() {
		southWestPnt = new Point(Double.MAX_VALUE, Double.MAX_VALUE);
		northEastPnt = new Point(Double.MIN_VALUE, Double.MIN_VALUE);
	}
	// 记录ne东北、sw西南两个极点
	public void recordExtremePoints(Point point) {
		southWestPnt = new Point(Math.min(southWestPnt.getLocationX(), point.getLocationX()),
				Math.min(southWestPnt.getLocationY(), point.getLocationY()));

		northEastPnt = new Point(Math.max(northEastPnt.getLocationX(), point.getLocationX()),
				Math.max(northEastPnt.getLocationY(), point.getLocationY()));
	}
	public void createWorldSpace() {
		width = (northEastPnt.getLocationX() - southWestPnt.getLocationX()); //* Parameter.lengthFactor();
		height = (northEastPnt.getLocationY() - southWestPnt.getLocationY()); //* Parameter.lengthFactor();
		// 坐标平移，将sw极点平移到坐标系原点，即ll_pnt坐标为(0,0)
		lowLeftPnt = worldSpacePoint(southWestPnt);
		topRightPnt = worldSpacePoint(northEastPnt);

		if (width < Point.POINT_EPSILON && height < Point.POINT_EPSILON) {
			// cerr << "Error: World space is empty. Check geometric data!"
			// << endl;
			// exit(1);
		}
		else if (width < Point.POINT_EPSILON) { // N-S linear network
			width = 0.1 * height;
		}
		else if (height < Point.POINT_EPSILON) { // W-E linear network
			height = 0.1 * width;
		}
		// Point初始化
		if (center == null)
			center = new Point();
		center.setCoodinate(0.5 * width, 0.5 * height);
		angle = 0.0;
	}

	// Convert a point from original network database cooridinates
	// to work space coordinates.

	public Point worldSpacePoint(Point p) {
		return worldSpacePoint(p.getLocationX(), p.getLocationY());
	}
	public Point worldSpacePoint(double x, double y) {
		return new Point((x - southWestPnt.getLocationX()) /* * Parameter.lengthFactor()*/,
				(y - southWestPnt.getLocationY())  /* * Parameter.lengthFactor()*/);

	}

	// Convert a point from work space coordinates to original
	// network database cooridinates

	public Point databasePoint(Point p) {
		return new Point(p.getLocationX() /* / Parameter.lengthFactor() */ + southWestPnt.getLocationX(),
				p.getLocationY() /* / Parameter.lengthFactor() */ + southWestPnt.getLocationY());
	}
	public Point databasePoint(double x, double y) {
		return new Point(x /* / Parameter.lengthFactor() */ + southWestPnt.getLocationX(),
				y /* / Parameter.lengthFactor() */ + southWestPnt.getLocationY());
	}

	public Point getSouthWestPoint() {
		return southWestPnt;
	}
	public Point getNorthEastPoint() {
		return northEastPnt;
	}

	public Point getLowLeftPoint() {
		return lowLeftPnt;
	}
	public Point getTopRightPoint() {
		return topRightPnt;
	}

	public Point getCenter() {
		return center;
	}
	public double getWidth() {
		return width;
	}
	public double getHeight() {
		return height;
	}
	public double getAngle() {
		return angle;
	}

}
