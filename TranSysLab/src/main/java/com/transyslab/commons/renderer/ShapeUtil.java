package com.transyslab.commons.renderer;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.math.VectorUtil;
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

	public static void drawSolidLine(GL2 gl, GeoPoint spnt, GeoPoint epnt, float linewidth, float[] color) {
		gl.glColor3f(color[0], color[1], color[2]);
//		gl.glLineWidth(linewidth);
		//虚线
//		gl.glLineStipple(2, (short) 0x5555);

//		gl.glLineStipple (1, (short) 0x0F0F);  
		gl.glBegin(GL_LINES);
		gl.glVertex3d(spnt.getLocationX(), spnt.getLocationY(), spnt.getLocationZ()+1);
		gl.glVertex3d(epnt.getLocationX(), epnt.getLocationY(), epnt.getLocationZ()+1);
		gl.glEnd();
	}
	public static void drawSolidLine(GL2 gl,float[] fcoods, float[] tcoods, float linewidth,float[] color){
		gl.glBegin(GL_LINES);
		gl.glVertex3d(fcoods[0], fcoods[1], fcoods[2]);
		gl.glVertex3d(tcoods[0], tcoods[1], tcoods[2]);
		gl.glEnd();
	}
	public static void drawPoint(GL2 gl, GeoPoint pos, int radius, float[] color) {
		gl.glColor3f(color[0], color[1], color[2]);
		gl.glPointSize(radius);
		gl.glBegin(GL_POINTS);
		gl.glVertex3d(pos.getLocationX(), pos.getLocationY(), pos.getLocationZ()+1);
		gl.glEnd();
	}
	public static void drawPoint(GL2 gl, double x, double y, double z, int radius, float[] color) {
		gl.glColor3f(color[0], color[1], color[2]);
		gl.glPointSize(radius);
		gl.glBegin(GL_POINTS);
		gl.glVertex3d(x, y, 2);
		gl.glEnd();
	}
	public static void drawPolygon(GL2 gl,List<GeoPoint> points, final float[] color, final boolean isSelected){
		gl.glColor3f(color[0], color[1], color[2]);
		if(isSelected)
			gl.glColor3f(1.0f, 1.0f, 0.0f);
		gl.glBegin(GL2.GL_POLYGON);
		for(GeoPoint p:points ){
			// TODO 图层管理
			gl.glVertex3d(p.getLocationX(), p.getLocationY(),0.0);
		}
		gl.glEnd();
	}
	public static void drawPolygon(GL2 gl, List<GeoPoint>points, Color color, double height){
		gl.glColor3d(color.getRed()/255.0, color.getGreen()/255.0, color.getBlue()/255.0);
		gl.glBegin(GL2.GL_POLYGON);
		for(GeoPoint p:points ){
			gl.glVertex3d(p.getLocationX(), p.getLocationY(),height);
		}
		gl.glEnd();
	}
	public static void drawPolygon(GL2 gl,List<GeoPoint>points, final float[] color, final boolean isSelected, double height){
		gl.glColor3f(color[0], color[1], color[2]);
		if(isSelected)
			gl.glColor3f(1.0f, 1.0f, 0.0f);
		gl.glBegin(GL2.GL_POLYGON);
		for(GeoPoint p:points ){
			gl.glVertex3d(p.getLocationX(), p.getLocationY(),height);
		}
		gl.glEnd();
	}
	public static void drawPolygon(GL2 gl,List<GeoPoint> points, final float[] fcolor, final float[] tcolor){

		//gl.glColor3f(color[0], color[1], color[2]);
		gl.glColor3f(1.0f, 1.0f, 0.0f);
		gl.glBegin(GL2.GL_POLYGON);
		for(GeoPoint p:points ){
			// TODO 图层管理
			gl.glVertex3d(p.getLocationX(), p.getLocationY(),0.0);
		}
		gl.glEnd();
	}
	public static void drawArrow(GL2 gl,GeoPoint fPoint, GeoPoint tPoint, final float[] color){
		gl.glColor3f(color[0], color[1], color[2]);
		gl.glBegin(GL2.GL_LINE);
		/*for(GeoPoint p:points ){
			gl.glVertex3d(p.getLocationX(), p.getLocationY(),height);
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
}
