package com.transyslab.commons.tools;

import com.transyslab.roadnetwork.Boundary;
import com.transyslab.roadnetwork.GeoPoint;
import com.transyslab.roadnetwork.GeoSurface;

import jhplot.math.LinearAlgebra;
import math.geom3d.Vector3D;

public class GeoUtil {
	public static GeoPoint intersect(Boundary upBound, Boundary dnBound) {
		double[][] coef = new double[][] { upBound.getDelta(), LinearAlgebra.times(dnBound.getDelta(), -1)};
		coef = LinearAlgebra.transpose(coef);
		double[][] b = new double[][] {LinearAlgebra.minus(dnBound.getStartPnt().getLocCoods(), 
																						upBound.getStartPnt().getLocCoods())};
		b = LinearAlgebra.transpose(b);
		int r = LinearAlgebra.rank(coef);
		switch (r) {
		case 0:
			return null;
		case 1:
			return dnBound.getStartPnt();
		case 2:
			double[][] ans = LinearAlgebra.solve(coef, b);
			double[] p = LinearAlgebra.plus(LinearAlgebra.times(upBound.getDelta(), ans[0][0]), upBound.getStartPnt().getLocCoods());
			return new GeoPoint(p);
		case 3:
			return null;
		default:
			return null;
		}
	}
	public static GeoSurface lineToRectangle(final GeoPoint fPoint, final GeoPoint tPoint, final double width){
		GeoSurface sf = new GeoSurface();
		double[] vecDir = LinearAlgebra.minus(tPoint.getLocCoods(), fPoint.getLocCoods());
		// 从中心线扩展成矩形面
		double distance = width/2.0f;
		double[] translation = new double[3];
		if(vecDir[0] == 0.0){
			// coods.Y
			translation[1] = distance;
		}
		else{
			// coods.Y
			translation[1] = distance/Math.sqrt(1.0 + Math.pow(vecDir[1]/vecDir[0],2));
			// coods.X
			translation[0] = (-vecDir[1]/vecDir[0]) * translation[1];
		}
		Vector3D normVec = new Vector3D(translation[0],translation[1],translation[2]);
		Vector3D dirVec = new Vector3D(vecDir[0],vecDir[1],vecDir[2]);
		normVec = Vector3D.crossProduct(normVec, dirVec);
		if(normVec.getZ()<0)
			// z>=0的面朝上，适应opengl右手坐标系
			LinearAlgebra.divide(translation, -1);
		// 矩形四个顶点，按逆时针顺序存储
		sf.addKerbPoint(new GeoPoint(LinearAlgebra.plus(fPoint.getLocCoods(),translation)));
		sf.addKerbPoint(new GeoPoint(LinearAlgebra.minus(fPoint.getLocCoods(),translation)));
		sf.addKerbPoint(new GeoPoint(LinearAlgebra.minus(tPoint.getLocCoods(),translation)));
		sf.addKerbPoint(new GeoPoint(LinearAlgebra.plus(tPoint.getLocCoods(),translation)));
		return sf;
	}
}
