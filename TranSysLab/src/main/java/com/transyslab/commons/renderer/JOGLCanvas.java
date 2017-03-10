package com.transyslab.commons.renderer;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.transyslab.roadnetwork.Boundary;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.Lane;
import com.transyslab.roadnetwork.Point;
import com.transyslab.roadnetwork.RoadNetwork;
import com.transyslab.roadnetwork.RoadNetworkPool;
import com.transyslab.roadnetwork.Segment;
import com.transyslab.roadnetwork.Surface;
import com.transyslab.roadnetwork.VehicleData;
import com.transyslab.roadnetwork.VehicleDataPool;

import static com.jogamp.opengl.GL.*; // GL constants
import static com.jogamp.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import java.awt.Color;
import java.util.List;

public class JOGLCanvas extends GLCanvas implements GLEventListener {
	private GLU glu; // for the GL Utility
	private RoadNetwork drawableNetwork_;
	private JOGLCamera cam_;
	private boolean isFirstRender;
	//
	public boolean isPause;
	//
	public boolean isRendering;

	/** Constructor to setup the GUI for this Component */
	public JOGLCanvas() {
		this.addGLEventListener(this);
		// 区别于从线程-编号哈希表获取对象
//		drawableNetwork_ = RoadNetworkPool.getInstance().getNetwork(0);
	}
	public JOGLCanvas(int width, int height) {
		this.addGLEventListener(this);
		// 区别于从线程-编号哈希表获取对象
		//drawableNetwork_ = RoadNetworkPool.getInstance().getNetwork(0);
		// 设置画布大小
		// setPreferredSize 有布局管理器下使用；setSize 无布局管理器下使用
//		this.setPreferredSize(new Dimension(width, height));
	}
	public boolean isNetworkReady(){
		return drawableNetwork_ != null? true : false; 
	}
	public void setDrawableNetwork(RoadNetwork network){
		drawableNetwork_ = network;
	}
	public void setCamera(JOGLCamera cam) {
		cam_ = cam;
	}
	public void setFirstRender(boolean isFirstRender) {
		this.isFirstRender = isFirstRender;
	}
	// ------ Implement methods declared in GLEventListener ------

	/**
	 * Called back immediately after the OpenGL context is initialized. Can be
	 * used to perform one-time initialization. Run only once.
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2(); // get the OpenGL graphics context
		glu = new GLU(); // get GL Utilities
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
		// YYL begin
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		float widthHeightRatio = (float) getWidth() / (float) getHeight();
		glu.gluPerspective(45, widthHeightRatio, 0.1, 10000);
		// YYL end
		gl.glClearDepth(1.0f); // set clear depth value to farthest
		gl.glEnable(GL_DEPTH_TEST); // enables depth testing
		gl.glDepthFunc(GL_LEQUAL); // the type of depth test to do
		gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // best
																// perspective
																// correction
		gl.glShadeModel(GL_SMOOTH); // blends colors nicely, and smoothes out
									// lighting

		// ----- Your OpenGL initialization code here -----


	}

	/**
	 * Call-back handler for window re-size event. Also called when the drawable
	 * is first set to visible.
	 */
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL2 gl = drawable.getGL().getGL2(); // get the OpenGL 2 graphics context

		if (height == 0)
			height = 1; // prevent divide by zero
		float aspect = (float) width / height;

		// Set the view port (display area) to cover the entire window
		gl.glViewport(0, 0, width, height);

		// Setup perspective projection, with aspect ratio matches viewport
		gl.glMatrixMode(GL_PROJECTION); // choose projection matrix
		gl.glLoadIdentity(); // reset projection matrix
		glu.gluPerspective(45.0, aspect, 0.1, 10000.0); // fovy, aspect, zNear,
														// zFar

		// Enable the model-view transform
		// gl.glMatrixMode(GL_MODELVIEW);
		// gl.glLoadIdentity(); // reset
	}

	/**
	 * Called back by the animator to perform rendering.
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2(); // get the OpenGL 2 graphics context
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear color
																// and depth  buffers
													
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity(); // reset the model-view matrix

		// ----- Your OpenGL rendering code here (render a white triangle for
		// testing) -----
		if(drawableNetwork_!=null){
			if(isFirstRender){
				Point center = drawableNetwork_.getWorldSpace().getCenter();
				cam_.initCamLookAt(new Point(center.getLocationX(), center.getLocationY(),1000), center);
				isFirstRender = false;
			}
			glu.gluLookAt(cam_.getEyePosition()[0], cam_.getEyePosition()[1], cam_.getEyePosition()[2],
					cam_.getTargetPosition()[0], cam_.getTargetPosition()[1], cam_.getTargetPosition()[2], 0, 1, 0);
			scene(gl);
		}
	}

	/**
	 * Called back before the OpenGL context is destroyed. Release resource such
	 * as buffers.
	 */
	@Override
	public void dispose(GLAutoDrawable drawable) {
	}
	public void scene(GL2 gl) {
		List<Boundary> boundarys = drawableNetwork_.getBoundarys();
		List<Surface> surfaces = drawableNetwork_.getSurfaces();
		
		Segment tmpsegment;
		JOGLAnimationFrame frame;
		Lane tmplane;
		final float[] liteBlue = new float[]{0.0f,0.75f,1.0f};
		for (Boundary tmpboundary:boundarys) {
			JOGLDrawShapes.drawSolidLine(gl, tmpboundary.getStartPnt(), tmpboundary.getEndPnt(), 2,
					Constants.COLOR_WHITE);
		}

		
		for(int i=0;i<drawableNetwork_.nSegments();i++){
			tmpsegment = drawableNetwork_.getSegment(i);
			JOGLDrawShapes.drawSolidLine(gl, tmpsegment.getStartPnt(), tmpsegment.getEndPnt(), 2,
					Constants.COLOR_WHITE);

		}
		/*
		for(int i=0;i<drawableNetwork_.nLanes();i++){
			tmplane = drawableNetwork_.getLane(i);
			JOGLDrawShapes.drawPoint(gl, tmplane.getStartPnt().getLocationX(), tmplane.getStartPnt().getLocationY(), 10, Constants.COLOR_RED);
			JOGLDrawShapes.drawPoint(gl, tmplane.getEndPnt().getLocationX(), tmplane.getEndPnt().getLocationY(), 5, Constants.COLOR_GREEN);
			
		}*/
		for(Surface sf: surfaces){
			JOGLDrawShapes.drawPolygon(gl, sf.getKerbList(),Constants.COLOR_GREY);
		}
		//暂停时不更新数组索引
		frame =JOGLFrameQueue.getInstance().poll(isPause);
		//暂停时保留VehicleData
		if(isPause){
			for(VehicleData vd:frame.getVhcDataQueue()){
				if(vd.getSpecialFlag()==1)
					JOGLDrawShapes.drawPoint(gl, vd.getVhcLocationX(), vd.getVhcLocationY(),0.1, 10, liteBlue);
				else if(vd.getSpecialFlag() == 0)
					JOGLDrawShapes.drawPoint(gl, vd.getVhcLocationX(), vd.getVhcLocationY(),0.1, 10, Constants.COLOR_BLUE);
			}
		}
		else{
			
			if(frame!=null){
				while(!frame.getVhcDataQueue().isEmpty()){
					VehicleData vd = frame.getVehicleData();
					//根据摄像机高度调整绘制的车辆大小，2017年1月2日ppt材料
//						JOGLDrawShapes.drawPoint(gl, vd.getVhcLocationX(), vd.getVhcLocationY(),15*(1000-cam_.getEyePosition()[2])/1000, Constants.COLOR_BLUE);
					if(vd.getSpecialFlag()==1)
						JOGLDrawShapes.drawPoint(gl, vd.getVhcLocationX(), vd.getVhcLocationY(),0.1, 10, liteBlue);
					else if(vd.getSpecialFlag() == 0)
						JOGLDrawShapes.drawPoint(gl, vd.getVhcLocationX(), vd.getVhcLocationY(),0.1, 10, Constants.COLOR_BLUE);
					
					//回收vehicledata
					VehicleDataPool.getVehicleDataPool().recycleVehicleData(vd);
				}
				//清空frame
				frame.clean();
			}
		}
				
		
		
	}
}
