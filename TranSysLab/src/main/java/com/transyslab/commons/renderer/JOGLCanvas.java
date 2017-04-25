package com.transyslab.commons.renderer;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.math.Ray;
import com.jogamp.opengl.math.VectorUtil;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.transyslab.commons.tools.GeoUtil;
import com.transyslab.roadnetwork.Boundary;
import com.transyslab.roadnetwork.CodedObject;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.Lane;
import com.transyslab.roadnetwork.GeoPoint;
import com.transyslab.roadnetwork.RoadNetwork;
import com.transyslab.roadnetwork.Segment;
import com.transyslab.roadnetwork.GeoSurface;
import com.transyslab.roadnetwork.VehicleData;
import com.transyslab.roadnetwork.VehicleDataPool;
import com.transyslab.simcore.mlp.MLPNetwork;

import oracle.net.aso.f;

import static com.jogamp.opengl.GL.*; // GL constants
import static com.jogamp.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.ode.sampling.FieldStepNormalizer;

public class JOGLCanvas extends GLCanvas implements GLEventListener, KeyListener, MouseListener, 
                                                    MouseWheelListener, MouseMotionListener {
	
	private GLU glu; // for the GL Utility
	private RoadNetwork drawableNetwork_;
	private Camera cam_;
	private java.awt.Point preWinCoods;
	private TextRenderer textRenderer;
	private boolean isMidBtnDragged, isRightBtnDragged;
	private boolean isPicking;
	private boolean isFirstRender;
	private List<CodedObject> pickedObject;
	//
	public boolean isPause;
	//
	public boolean isRendering;
		
	

	/** Constructor to setup the GUI for this Component */
	public JOGLCanvas() {
		this.addGLEventListener(this);
		preWinCoods = new java.awt.Point();
		pickedObject = new ArrayList<CodedObject>();

	}
	public JOGLCanvas(int width, int height) {
		this.addGLEventListener(this);
		preWinCoods = new java.awt.Point();
		pickedObject = new ArrayList<CodedObject>();
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

	public void setCamera(Camera cam){
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
		this.addMouseListener(this);
		this.addKeyListener(this);
		this.addMouseWheelListener(this);
		this.addMouseMotionListener(this);
		this.textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 100));

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
				GeoPoint center = drawableNetwork_.getWorldSpace().getCenter();
				cam_.initFirstLookAt(new GeoPoint(center.getLocationX(), center.getLocationY(),1000), center, new float[]{0.0f, 1.0f, 0.0f});
				isFirstRender = false;
			}
			glu.gluLookAt(cam_.getEyeLocation()[0], cam_.getEyeLocation()[1], cam_.getEyeLocation()[2],
					cam_.getTarLocation()[0], cam_.getTarLocation()[1], cam_.getTarLocation()[2], 0, 1, 0);
		
			scene(gl);
			if(isPicking){
				isPicking = false;
				Ray raycast = new Ray();
				float[] ray = calcRay(gl) ;
				raycast.orig[0] = ray[0];
				raycast.orig[1] = ray[1];
				raycast.orig[2] = ray[2];
				raycast.dir[0] = ray[3];
				raycast.dir[1] = ray[4];
				raycast.dir[2] = ray[5];
				/*
				List<GeoSurface> surfaces = drawableNetwork_.getSurfaces();
				for(GeoSurface sf: surfaces){
					if(sf.getAabBox().intersectsRay(raycast)){
						//被选中对象用黄色渲染
						sf.setSelected(true);
						pickedObject.add(sf);
					}
						
				}
				*/
				for(int i=0;i<drawableNetwork_.nLanes();i++){
					GeoSurface laneSf = drawableNetwork_.getLane(i).getLaneSurface();
					if(GeoUtil.isIntersect(raycast,laneSf)){
						//被选中对象用黄色渲染
						laneSf.setSelected(true);
						pickedObject.add(laneSf);
					}
				}
			}
			/*
			textRenderer.begin3DRendering();
				textRenderer.setColor(1.0f, 0.2f, 0.2f, 0.8f);
				textRenderer.draw3D("Text to draw", cam_.getEyePosition()[0], cam_.getEyePosition()[1],0.0f,1);
			    // ... more draw commands, color changes, etc.
			textRenderer.end3DRendering();*/
			    
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
		List<GeoSurface> surfaces = drawableNetwork_.getSurfaces();
		float camHeight =  cam_.getEyeLocation()[2];
		Segment tmpsegment;
		AnimationFrame frame;
		Lane tmplane;
		final float[] liteBlue = new float[]{0.0f,0.75f,1.0f};
		for (Boundary tmpboundary:boundarys) {
			ShapeUtil.drawSolidLine(gl, tmpboundary.getStartPnt(), tmpboundary.getEndPnt(), 2,
					Constants.COLOR_WHITE);
		}

		
		for(int i=0;i<drawableNetwork_.nSegments();i++){
			tmpsegment = drawableNetwork_.getSegment(i);
			ShapeUtil.drawSolidLine(gl, tmpsegment.getStartPnt(), tmpsegment.getEndPnt(), 2,
					Constants.COLOR_WHITE);
			if(camHeight>=200){
				double width = tmpsegment.nLanes()*Constants.LANE_WIDTH;
				double offset = Math.max(width, 0.0008*camHeight*width);
				GeoSurface tmpSurface = GeoUtil.lineToRectangle(tmpsegment.getStartPnt(), tmpsegment.getEndPnt(), offset, false);
				ShapeUtil.drawPolygon(gl, tmpSurface.getKerbList(), Constants.COLOR_GREY, false);
			}
//			ShapeUtil.drawPoint(gl, tmpsegment.getStartPnt().getLocationX(), tmpsegment.getStartPnt().getLocationY(),0.5, 10, Constants.COLOR_RED);
//			ShapeUtil.drawPoint(gl, tmpsegment.getEndPnt().getLocationX(), tmpsegment.getEndPnt().getLocationY(),1, 10, Constants.COLOR_BLUE);

		}
//		gl.glReadPixels(x, y, width, height, format, type, pixels_buffer_offset);
		//射线测试
		/*
		float[] rayDir = calcRay(gl, 0.0f,2);
		//绘制球显示射线与xoy平面相交的位置
		gl.glPushMatrix();
			gl.glTranslatef(rayDir[0], rayDir[1], rayDir[2]);
			GLUquadric quad = glu.gluNewQuadric();
			glu.gluSphere(quad, 5, 10, 10);
			glu.gluDeleteQuadric(quad);
		gl.glPopMatrix();
		
		/*
		for(int i=0;i<drawableNetwork_.nLanes();i++){
			tmplane = drawableNetwork_.getLane(i);
			JOGLDrawShapes.drawPoint(gl, tmplane.getStartPnt().getLocationX(), tmplane.getStartPnt().getLocationY(), 10, Constants.COLOR_RED);
			JOGLDrawShapes.drawPoint(gl, tmplane.getEndPnt().getLocationX(), tmplane.getEndPnt().getLocationY(), 5, Constants.COLOR_GREEN);
			
		}*/
		if(camHeight<200){
			for(int i=0;i<drawableNetwork_.nLanes();i++){
				GeoSurface lanesf = drawableNetwork_.getLane(i).getLaneSurface();
//				ShapeUtil.drawPoint(gl, drawableNetwork_.getLane(i).getStartPnt().getLocationX(), drawableNetwork_.getLane(i).getStartPnt().getLocationY(),0.5, 10, Constants.COLOR_RED);
//				ShapeUtil.drawPoint(gl, drawableNetwork_.getLane(i).getEndPnt().getLocationX(), drawableNetwork_.getLane(i).getEndPnt().getLocationY(),0.5, 10, Constants.COLOR_BLUE);
				
				ShapeUtil.drawPolygon(gl, lanesf.getKerbList(),Constants.COLOR_GREY, lanesf.isSelected());
			}
		}
		
		for(int i=0;i<((MLPNetwork)drawableNetwork_).loops.size();i++){
			GeoSurface loopsf = ((MLPNetwork)drawableNetwork_).loops.get(i).getSurface();
			ShapeUtil.drawPolygon(gl, loopsf.getKerbList(),Constants.COLOR_GREEN, loopsf.isSelected());
		}
		//暂停时不更新数组索引
		frame =FrameQueue.getInstance().poll(isPause);
		//暂停时保留VehicleData
		if(isPause){
			for(VehicleData vd:frame.getVhcDataQueue()){
				if(vd.getSpecialFlag()==1)
					ShapeUtil.drawPoint(gl, vd.getVhcLocationX(), vd.getVhcLocationY(),0.1, 10, liteBlue);
				else if(vd.getSpecialFlag() == 0)
					ShapeUtil.drawPoint(gl, vd.getVhcLocationX(), vd.getVhcLocationY(),0.1, 10, Constants.COLOR_BLUE);
			}
		}
		else{
			
			if(frame!=null){
				while(!frame.getVhcDataQueue().isEmpty()){
					VehicleData vd = frame.getVehicleData();
					//根据摄像机高度调整绘制的车辆大小，2017年1月2日ppt材料
//						JOGLDrawShapes.drawPoint(gl, vd.getVhcLocationX(), vd.getVhcLocationY(),15*(1000-cam_.getEyePosition()[2])/1000, Constants.COLOR_BLUE);
					if(vd.getSpecialFlag()==1)
						ShapeUtil.drawPoint(gl, vd.getVhcLocationX(), vd.getVhcLocationY(),0.1, 10, liteBlue);
					else if(vd.getSpecialFlag() == 0)
						ShapeUtil.drawPoint(gl, vd.getVhcLocationX(), vd.getVhcLocationY(),0.1, 10, Constants.COLOR_BLUE);
					
					//回收vehicledata
					VehicleDataPool.getVehicleDataPool().recycleVehicleData(vd);
				}
				//清空frame
				frame.clean();
			}
		}
				
		
		
	}
	//计算拾取射线与x/y/z = intersectPlane 平面的交点
	// offset=0,1,2:x,y,z
	public float[] calcRay(final GL2 gl, final float intersectPlane, final int offset){
		int[] viewport = new int[4];
		float[] projmatrix = {1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1};
		float[] mvmatrix = {1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1};
		float[] posNear = new float[3];
		float[] posFar = new float[3];
		gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
		gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, mvmatrix, 0);
		gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projmatrix, 0);
		float winX = preWinCoods.x;
		float winY = viewport[3] - preWinCoods.y;
		if(!glu.gluUnProject(winX, winY, 0.0f, mvmatrix, 0, projmatrix, 0, viewport, 0, posNear, 0) ||
		   !glu.gluUnProject(winX, winY, 1.0f, mvmatrix, 0, projmatrix, 0, viewport, 0, posFar, 0))
			System.out.println("The matrix can not be inverted");
		VectorUtil.subVec3(posFar, posFar, posNear);
		VectorUtil.normalizeVec3(posFar);
		float scale = (intersectPlane - posNear[offset])/posFar[offset];
		VectorUtil.scaleVec3(posFar, posFar, scale);
		float[] intersection = new float[3];
		VectorUtil.addVec3(intersection, posNear, posFar);
		return intersection;	
	}
	public float[] calcRay(final GL2 gl){
		int[] viewport = new int[4];
		float[] projmatrix = {1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1};
		float[] mvmatrix = {1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1};
		float[] posNear = new float[3];
		float[] posFar = new float[3];
		gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
		gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, mvmatrix, 0);
		gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projmatrix, 0);
		float winX = preWinCoods.x;
		float winY = viewport[3] - preWinCoods.y;
		if(!glu.gluUnProject(winX, winY, 0.0f, mvmatrix, 0, projmatrix, 0, viewport, 0, posNear, 0) ||
		   !glu.gluUnProject(winX, winY, 1.0f, mvmatrix, 0, projmatrix, 0, viewport, 0, posFar, 0))
			System.out.println("The matrix can not be inverted");
		VectorUtil.subVec3(posFar, posFar, posNear);
		VectorUtil.normalizeVec3(posFar);
		float[] ray = new float[6];
		System.arraycopy(posNear, 0, ray, 0, 3);
		System.arraycopy(posFar, 0, ray, 3, 3);
		return ray;
	}
	
	private void deselect(){
		for(CodedObject co:pickedObject){
			co.setSelected(false);
		}
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		
	}
	@Override
	public void mousePressed(MouseEvent e) {
		switch(e.getButton()){
			case MouseEvent.BUTTON1:
				isPicking = true;
				if(!pickedObject.isEmpty()){
					deselect();
					pickedObject.clear();
				}
			break;
			case MouseEvent.BUTTON2:
				isMidBtnDragged = true;
			break;
			case MouseEvent.BUTTON3:
				isRightBtnDragged = true;
			break;
		}

		
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		if(e.getButton()==MouseEvent.BUTTON2){
			isMidBtnDragged = false;
		}
		else if(e.getButton() == MouseEvent.BUTTON3){
			isRightBtnDragged = false;
		}
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		
	}
	@Override
	public void mouseExited(MouseEvent e) {
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if(isMidBtnDragged){
			cam_.calcMouseMotion(e.getX() - preWinCoods.x, e.getY() - preWinCoods.y, MouseEvent.BUTTON2);
			preWinCoods.setLocation(e.getX(), e.getY());
		}
		else if (isRightBtnDragged){
			cam_.calcMouseMotion(e.getX() - preWinCoods.x, e.getY() - preWinCoods.y, MouseEvent.BUTTON3);
			preWinCoods.setLocation(e.getX(), e.getY());
		}
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		
		preWinCoods.setLocation(e.getX(), e.getY());
	}
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		cam_.calcMouseWheelMotion(e.getWheelRotation());
	}
	@Override
	public void keyTyped(KeyEvent e) {
		
	}
	@Override
	public void keyPressed(KeyEvent e) {
		cam_.calcKeyMotion(e.getKeyCode());
	}
	@Override
	public void keyReleased(KeyEvent e) {
		
	}



}
