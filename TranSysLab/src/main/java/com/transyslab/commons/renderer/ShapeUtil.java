package com.transyslab.commons.renderer;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.math.VectorUtil;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.transyslab.roadnetwork.GeoPoint;

import jhplot.math.DoubleArray;
import jhplot.math.LinearAlgebra;

import static com.jogamp.opengl.GL.*;// GL constants

import java.awt.*;
import java.util.List;
import java.util.Vector;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ode.sampling.FieldStepNormalizer;
import org.apache.commons.math3.util.MathUtils; 

public class ShapeUtil {

	public static void drawSolidLine(GL2 gl, GeoPoint spnt, GeoPoint epnt, float linewidth, float[] color, double shiftZ) {
		gl.glColor3f(color[0], color[1], color[2]);
		gl.glLineWidth(linewidth);
		gl.glBegin(GL_LINES);
		gl.glVertex3d(spnt.getLocationX(), spnt.getLocationY(), spnt.getLocationZ()+shiftZ);
		gl.glVertex3d(epnt.getLocationX(), epnt.getLocationY(), epnt.getLocationZ()+shiftZ);
		gl.glEnd();
	}

	public static void drawPolyline(GL2 gl, GeoPoint[] points, float linewidth, float[] color, double shiftZ){
		gl.glColor3f(color[0], color[1], color[2]);
		gl.glLineWidth(linewidth);
		gl.glBegin(GL_LINE_STRIP);
		for(int i=0;i<points.length;i++){
			gl.glVertex3d(points[i].getLocationX(),points[i].getLocationY(),points[i].getLocationZ()+shiftZ);
		}
		gl.glEnd();
	}
	public static void drawPoint(GL2 gl, GeoPoint pos, int radius, float[] color, double shiftZ) {
		gl.glColor3f(color[0], color[1], color[2]);
		gl.glPointSize(radius);
		gl.glBegin(GL_POINTS);
		gl.glVertex3d(pos.getLocationX(), pos.getLocationY(), pos.getLocationZ()+shiftZ);
		gl.glEnd();
	}
	public static void drawPoint(GL2 gl, double x, double y, double z, int radius, float[] color,  double shiftZ) {
		gl.glColor3f(color[0], color[1], color[2]);
		gl.glPointSize(radius);
		gl.glBegin(GL_POINTS);
		gl.glVertex3d(x, y, z + shiftZ);
		gl.glEnd();
	}

	public static void drawPolygon(GL2 gl,List<GeoPoint>points, final float[] color, final boolean isSelected, double shiftZ){
		gl.glColor3f(color[0], color[1], color[2]);
		if(isSelected)
			gl.glColor3f(1.0f, 1.0f, 0.0f);
		gl.glBegin(GL2.GL_POLYGON);
		for(GeoPoint p:points ){
			gl.glVertex3d(p.getLocationX(), p.getLocationY(),p.getLocationZ()+shiftZ);
		}
		gl.glEnd();
	}
	public static void drawPolygon(GL2 gl,GeoPoint[] points, final float[] color, final boolean isSelected, double shiftZ){
		gl.glColor3f(color[0], color[1], color[2]);
		if(isSelected)
			gl.glColor3f(1.0f, 1.0f, 0.0f);
		gl.glBegin(GL2.GL_POLYGON);
		for(GeoPoint p:points ){
			gl.glVertex3d(p.getLocationX(), p.getLocationY(),p.getLocationZ()+ shiftZ);
		}
		gl.glEnd();
	}

	public static void drawArrow(GL2 gl,GeoPoint fPoint, GeoPoint tPoint, final float[] color, double shiftZ){
		gl.glColor3f(color[0], color[1], color[2]);
		gl.glBegin(GL2.GL_LINE);
		/*for(GeoPoint p:points ){
			gl.glVertex3d(p.getLocationX(), p.getLocationY(),p.getLocationZ()+shiftZ);
		}*/
		gl.glEnd();
	}
	public static void drawCylinder(GL2 gl, GLU glu, final float[] spnt, final float[] epnt, final float[] color){
		float[] dir = new float[3];
		float[] up = new float[]{0.0f,1.0f,0.0f};
		float[] side = new float[3];
		VectorUtil.subVec3(dir, epnt, spnt);
		float length = VectorUtil.normSquareVec3(dir);
		GLUquadric quad = glu.gluNewQuadric();
		glu.gluQuadricDrawStyle(quad, glu.GLU_LINE);
		glu.gluQuadricNormals(quad, glu.GLU_SMOOTH);
		gl.glPushMatrix();
		//平移到起始点
		gl.glTranslated(spnt[0], spnt[1], spnt[2]);
		VectorUtil.normalizeVec3(dir);
		VectorUtil.crossVec3(side, up, dir);
		VectorUtil.normalizeVec3(side);
		VectorUtil.crossVec3(up, side, dir);
		VectorUtil.normalizeVec3(up);
		float[] matrix = new float[]{side[0], side[1], side[2], 0.0f,
									 up[0], up[1],up[2],0.0f,
									 dir[0], dir[1], dir[2], 0.0f,
									 0.0f, 0.0f, 0.0f, 1.0f};
		gl.glMultMatrixf(matrix, 0);
		glu.gluCylinder(quad, 2, 2, length, 8, 3);
		gl.glPopMatrix();
	}
	public static void drawText(TextRenderer tr, String[] texts,float scaleFactor,List<GeoPoint> locations,float[] color){
		if(texts.length != locations.size()) {
			System.out.println("can not draw the text");
			return;
		}
		tr.begin3DRendering();
		tr.setColor(0.85f, 0.588f, 0.580f, 0.8f);
		int i = 0;
		for(GeoPoint point:locations){
			tr.draw3D(texts[i], (float)point.getLocationX(),(float)point.getLocationY(),(float)point.getLocationZ(), scaleFactor);
			i++;
		}
		tr.end3DRendering();
	}
}
