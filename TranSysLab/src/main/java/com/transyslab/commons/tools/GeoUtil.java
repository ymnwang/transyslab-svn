package com.transyslab.commons.tools;

import com.transyslab.roadnetwork.Boundary;
import com.transyslab.roadnetwork.Point;

import jhplot.math.LinearAlgebra;

public class GeoUtil {
	public static Point intersect(Boundary upBound, Boundary dnBound) {
		double[][] coef = new double[][] { upBound.getDelta(), LinearAlgebra.times(dnBound.getDelta(), -1)};
		coef = LinearAlgebra.transpose(coef);
		double[][] b = new double[][] {LinearAlgebra.minus(dnBound.getStartPnt().getLocations(), 
																						upBound.getStartPnt().getLocations())};
		b = LinearAlgebra.transpose(b);
		int r = LinearAlgebra.rank(coef);
		switch (r) {
		case 0:
			return null;
		case 1:
			return dnBound.getStartPnt();
		case 2:
			double[][] ans = LinearAlgebra.solve(coef, b);
			double[] p = LinearAlgebra.plus(LinearAlgebra.times(upBound.getDelta(), ans[0][0]), upBound.getStartPnt().getLocations());
			return new Point(p[0], p[1], p[2]);
		case 3:
			return null;
		default:
			return null;
		}
	}
}
