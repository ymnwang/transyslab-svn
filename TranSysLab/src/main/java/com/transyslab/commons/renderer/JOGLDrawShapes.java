package com.transyslab.commons.renderer;

import com.jogamp.opengl.GL2;
import com.transyslab.roadnetwork.Point;
import static com.jogamp.opengl.GL.*;// GL constants

import java.util.List; 

public class JOGLDrawShapes {

	public static void drawSolidLine(GL2 gl, Point spnt, Point epnt, float linewidth, float[] color) {
		gl.glColor3f(color[0], color[1], color[2]);
		gl.glLineWidth(linewidth);
		//ÐéÏß
//		gl.glLineStipple(2, (short) 0x5555);

//		gl.glLineStipple (1, (short) 0x0F0F);  
		gl.glBegin(GL_LINES);
		gl.glVertex2d(spnt.getLocationX(), spnt.getLocationY());
		gl.glVertex2d(epnt.getLocationX(), epnt.getLocationY());
		gl.glEnd();
	}
	public static void drawPoint(GL2 gl, Point pos, int radius, float[] color) {
		gl.glColor3f(color[0], color[1], color[2]);
		gl.glPointSize(radius);
		gl.glBegin(GL_POINTS);
		gl.glVertex2d(pos.getLocationX(), pos.getLocationY());
		gl.glEnd();
	}
	public static void drawPoint(GL2 gl, double x, double y, int radius, float[] color) {
		gl.glColor3f(color[0], color[1], color[2]);
		gl.glPointSize(radius);
		gl.glBegin(GL_POINTS);
		gl.glVertex2d(x, y);
		gl.glEnd();
	}
	public static void drawPolygon(GL2 gl,List<Point> points, float[] color){
		gl.glColor3f(color[0], color[1], color[2]);
		gl.glBegin(GL_TRIANGLE_FAN);
		for(Point p:points ){
			gl.glVertex3d(p.getLocationX(), p.getLocationY(),-1);
		}
		gl.glEnd();
		
	}
}
