package com.transyslab.commons.renderer;

import com.jogamp.opengl.GL2;
import com.transyslab.roadnetwork.Point;
import static com.jogamp.opengl.GL.*; // GL constants

public class JOGLDrawShapes {

	public static void drawSolidLine(GL2 gl, Point spnt, Point epnt, float linewidth, float[] color) {
		gl.glColor3f(color[0], color[1], color[2]);
		// gl.glLineWidth(linewidth);
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
}
