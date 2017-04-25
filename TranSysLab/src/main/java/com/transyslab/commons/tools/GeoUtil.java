package com.transyslab.commons.tools;

import java.util.List;

import com.jogamp.opengl.math.Ray;
import com.jogamp.opengl.math.VectorUtil;
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
	public static GeoSurface lineToRectangle(final GeoPoint fPoint, final GeoPoint tPoint, final double width, final boolean bothSide){
		GeoSurface sf = new GeoSurface();
		double[] vecDir = LinearAlgebra.minus(tPoint.getLocCoods(), fPoint.getLocCoods());
		double distance;
		if(bothSide)// 从中心线扩展成矩形面
			distance = width/2.0f;
		else // 从中心线向右拓宽
			distance = width;
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
		if(bothSide){
			sf.addKerbPoint(new GeoPoint(LinearAlgebra.plus(fPoint.getLocCoods(),translation)));
			sf.addKerbPoint(new GeoPoint(LinearAlgebra.minus(fPoint.getLocCoods(),translation)));
			sf.addKerbPoint(new GeoPoint(LinearAlgebra.minus(tPoint.getLocCoods(),translation)));
			sf.addKerbPoint(new GeoPoint(LinearAlgebra.plus(tPoint.getLocCoods(),translation)));
		}
		else{
			sf.addKerbPoint(new GeoPoint(fPoint));
			sf.addKerbPoint(new GeoPoint(LinearAlgebra.minus(fPoint.getLocCoods(),translation)));
			sf.addKerbPoint(new GeoPoint(LinearAlgebra.minus(tPoint.getLocCoods(),translation)));
			sf.addKerbPoint(new GeoPoint(tPoint));
		}
		return sf;
	}

	public static boolean isIntersect(Ray ray, GeoSurface surf) {
		boolean Intersect = true;

		List<GeoPoint> Gp = surf.getKerbList();
		float[] x = new float[Gp.size() + 1];
		float[] y = new float[Gp.size() + 1];
		float[] z = new float[Gp.size() + 1];

		for (int i = 0; i < Gp.size(); i++) {

			x[i] = Gp.get(i).getLocCoodsf()[0];
			y[i] = Gp.get(i).getLocCoodsf()[1];
			z[i] = Gp.get(i).getLocCoodsf()[2];

		}
		float[] v1 = { x[0] - x[1], y[0] - y[1], z[0] - z[1] };

		// 不共线的三点组成一个平面，根据两向量叉乘判断，a x b=0->a与b共线
		int k = 0;
		float[] normal_v = new float[3];
		for (int i = 2; i < Gp.size(); i++) {
			float[] v2 = { x[0] - x[i], y[0] - y[i], z[0] - z[i] };

			float[] result = { 0, 0, 0 };
			VectorUtil.crossVec3(result, v1, v2);
			if (!(result[0] == 0 && result[1] == 0 && result[2] == 0)) {
				// normal vector
				normal_v = result;
				k = 1;
				break;
			}
		}

		// 求平面的单位法向量
		VectorUtil.normalizeVec3(normal_v,normal_v);


		if (VectorUtil.dotVec3(normal_v, ray.dir) == 0) // when ray is orthogonal with normal vector, there
														// is no intersection
		{
			Intersect = false;
		}

		else // if there is a intersection, find out whether it is within the lane
		{
			// find out the scale to intersect
			float scale = -(normal_v[0] * (ray.orig[0] - v1[0]) + normal_v[1] * (ray.orig[1] - v1[1])
					+ normal_v[2] * (ray.orig[2] - v1[2]))
					/ (normal_v[0] * ray.dir[0] + normal_v[1] * ray.dir[0] + normal_v[2] * ray.dir[2]);
			float[] intersection = { ray.orig[0] + scale * ray.dir[0], ray.orig[1] + scale * ray.dir[1],
					ray.orig[2] + scale * ray.dir[2] };

			int flag = 0;
			double temp = 1;

			float[] c1 = { 0, 0, 0 };
			float[] c2 = { 0, 0, 0 };

			// 判断点是否在多边形内部，设多边形由点n1、n2、n3...组成，交点为no，依次求ni->ni+1与ni->no到叉乘，判断叉乘结果是否都在一个方向，若是则在平面内，反之在平面外
			for (int i = 0; i < Gp.size() - 2; i++) {

				float[] va = { x[i + 1] - x[i], y[i + 1] - y[i], z[i + 1] - z[i] };
				float[] vai = { intersection[0] - x[i], intersection[1] - y[i], intersection[2] - z[i] };
				float[] vb = { x[i + 2] - x[i + 1], y[i + 2] - y[i + 1], z[i + 2] - z[i + 1] };
				float[] vbi = { intersection[0] - x[i + 1], intersection[1] - y[i + 1], intersection[2] - z[i + 1] };
				VectorUtil.crossVec3(c1, va, vai);
				VectorUtil.crossVec3(c2, vb, vbi);

				temp = temp * VectorUtil.dotVec3(c1, c2);

				if (temp < 0) {
					flag = 1;
					break;
				}
			}
			float[] va = { x[0] - x[Gp.size() - 2], y[0] - y[Gp.size() - 2], z[0] - z[Gp.size() - 2] };
			float[] vai = { intersection[0] - x[Gp.size() - 2], intersection[1] - y[Gp.size() - 2],
					intersection[2] - z[Gp.size() - 2] };
			VectorUtil.crossVec3(c1, va, vai);
			temp = temp * VectorUtil.dotVec3(c1, c2);
			if (temp < 0) {
				flag = 1;
			}
			if (flag == 0) {
				Intersect = true;
			} else {
				Intersect = false;
			}
		}
		return Intersect;
	}
	
}
