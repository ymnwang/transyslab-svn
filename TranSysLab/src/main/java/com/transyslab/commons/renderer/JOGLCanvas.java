package com.transyslab.commons.renderer;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.math.Ray;
import com.jogamp.opengl.math.VectorUtil;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.transyslab.commons.tools.GeoUtil;
import com.transyslab.gui.MainWindow;
import com.transyslab.roadnetwork.*;
import com.transyslab.simcore.mlp.MLPLoop;
import jhplot.math.LinearAlgebra;


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
import java.util.Arrays;
import java.util.List;

public class JOGLCanvas extends GLCanvas implements GLEventListener, KeyListener, MouseListener,
		MouseWheelListener, MouseMotionListener {
	//status
	public static final int ANIMATOR_STOP = 0;
	public static final int ANIMATOR_PLAYING = 1;
	public static final int ANIMATOR_PAUSE = 2;
	//mode
	public static final int ANIMATOR_FRAME_ADVANCE = 1;
	private MainWindow mainWindow;
	private GLU glu; // for the GL Utility
	private RoadNetwork drawableNetwork;
	private Camera cam;
	private java.awt.Point preWinCoods;
	private TextRenderer textRenderer;
	private boolean isMidBtnDragged, isRightBtnDragged;
	private boolean isPicking;
	private boolean isFirstRender;
	private List<NetworkObject> pickedObjects;
	// 临时记录已选择的对象
	private NetworkObject preObject;
	private AnimationFrame curFrame;
	private int status;
	private int mode;

	/** Constructor to setup the GUI for this Component */
	public JOGLCanvas() {
		this.addGLEventListener(this);
		status = 0;
		mode = 0;
		preWinCoods = new java.awt.Point();
		pickedObjects = new ArrayList<NetworkObject>();

	}
	public JOGLCanvas(int width, int height) {
		this.addGLEventListener(this);
		preWinCoods = new java.awt.Point();
		pickedObjects = new ArrayList<NetworkObject>();
		// 设置画布大小
		// setPreferredSize 有布局管理器下使用；setSize 无布局管理器下使用
//		this.setPreferredSize(new Dimension(width, height));
	}
	public void setMainWindow(MainWindow window){
		this.mainWindow = window;
	}
	public boolean isNetworkReady(){
		return drawableNetwork != null? true : false;
	}
	public void setDrawableNetwork(RoadNetwork network){
		drawableNetwork = network;
	}

	public void setCamera(Camera cam){
		this.cam = cam;
	}
	public void setFirstRender(boolean isFirstRender) {
		this.isFirstRender = isFirstRender;
	}
	public void setStatus(int status){
		this.status = status;
	}
	public int getStatus(){
		return this.status;
	}
	public void setMode(int mode){
		this.mode = mode;
	}
	public AnimationFrame getCurFrame(){
		return this.curFrame;
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

		if(drawableNetwork !=null){
			if(isFirstRender){
				GeoPoint center = drawableNetwork.getWorldSpace().getCenter();
				cam.initFirstLookAt(new GeoPoint(center.getLocationX(), center.getLocationY(),1000), center, new float[]{0.0f, 1.0f, 0.0f});
				isFirstRender = false;
			}
			glu.gluLookAt(cam.getEyeLocation()[0], cam.getEyeLocation()[1], cam.getEyeLocation()[2],
					cam.getTarLocation()[0], cam.getTarLocation()[1], cam.getTarLocation()[2], 0, 1, 0);

			scene(gl);
			if(isPicking) {
				isPicking = false;
				selectObject(gl, mainWindow.getCurLayerName());
			}

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
		float camHeight =  cam.getEyeLocation()[2];
		Segment tmpsegment;

		for (int i = 0; i< drawableNetwork.nBoundaries(); i++) {
			Boundary tmpboundary = drawableNetwork.getBoundary(i);
			ShapeUtil.drawSolidLine(gl, tmpboundary.getStartPnt(), tmpboundary.getEndPnt(), 2,
					Constants.COLOR_WHITE);
		}
		for(int i = 0; i< drawableNetwork.nSegments(); i++){
			tmpsegment = drawableNetwork.getSegment(i);
			ShapeUtil.drawSolidLine(gl, tmpsegment.getStartPnt(), tmpsegment.getEndPnt(), 2,
					Constants.COLOR_WHITE);
			if(camHeight>=200){
				/*
				double width = tmpsegment.nLanes()*Constants.LANE_WIDTH;
				double offset = Math.max(width, 0.0008*camHeight*width);
				GeoSurface tmpSurface = GeoUtil.lineToRectangle(tmpsegment.getStartPnt(), tmpsegment.getEndPnt(), offset, false);
				ShapeUtil.drawPolygon(gl, tmpSurface.getKerbList(), Constants.COLOR_GREY, false);*/
			}
		}

		// 显示车道面
		if(camHeight<200){
			for(int i = 0; i< drawableNetwork.nLanes(); i++){
				Lane tmpLane = drawableNetwork.getLane(i);
				ShapeUtil.drawPolygon(gl, tmpLane.getSurface().getKerbList(),Constants.COLOR_GREY, tmpLane.isSelected());
			}
		}
		//显示路段面
		else if(camHeight < 1000){
			for(int i = 0; i< drawableNetwork.nSegments(); i++){
				Segment tmpSegment = drawableNetwork.getSegment(i);
				ShapeUtil.drawPolygon(gl, tmpSegment.getSurface().getKerbList(),Constants.COLOR_GREY, tmpSegment.isSelected());
			}
		}
		//
		Sensor tmpSensor = null;
		for(int i = 0; i< drawableNetwork.nSensors(); i++){
			tmpSensor = drawableNetwork.getSensor(i);
			ShapeUtil.drawPolygon(gl, tmpSensor.getSurface().getKerbList(),Constants.COLOR_GREEN, tmpSensor.isSelected());
		}
		// 车道连接器
		for(int i=0; i< drawableNetwork.nConnectors();i++){
			Connector tmpConnector = drawableNetwork.getConnector(i);
			ShapeUtil.drawSolidLine(gl, tmpConnector.getStartPoint(),tmpConnector.getEndPoint(),2, new float[]{0.98f, 0.72f, 0.35f});
		}
		boolean isPause = (status == ANIMATOR_PAUSE);
		//暂停时不更新帧索引
		curFrame =FrameQueue.getInstance().poll(isPause);
		//暂停状态下读下一帧
		if (isPause && mode == ANIMATOR_FRAME_ADVANCE){
			curFrame = FrameQueue.getInstance().poll(false);
			mode = 0;
		}
		if(curFrame !=null){
			for(VehicleData vd:curFrame.getVhcDataQueue()){
				if ((vd.getSpecialFlag()&Constants.FOLLOWING) == 0){
					ShapeUtil.drawPolygon(gl, vd.getVhcShape().getKerbList(), Constants.COLOR_RED, vd.isSelected(),0.2);
				}
				else {
					if((vd.getSpecialFlag()&Constants.VIRTUAL_VEHICLE) != 0)//虚拟车
						ShapeUtil.drawPolygon(gl, vd.getVhcShape().getKerbList(), Constants.COLOR_LITEBLUE, vd.isSelected(),0.2);
					else //非虚拟车
						ShapeUtil.drawPolygon(gl, vd.getVhcShape().getKerbList(), Constants.COLOR_BLUE, vd.isSelected(),0.2);
				}

			}
			//回收vehicledata
			if(!isPause)
				curFrame.clean();
		}

	}
	//计算拾取射线与x/y/z = intersectPlane 平面的交点
	// offset=0,1,2:x,y,z
	public float[] calcRay(final GL2 gl, final float intersectPlane, final int offset, final int winx, final int winy){
		int[] viewport = new int[4];
		float[] projmatrix = {1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1};
		float[] mvmatrix = {1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1};
		float[] posNear = new float[3];
		float[] posFar = new float[3];
		gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
		gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, mvmatrix, 0);
		gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projmatrix, 0);
		float winX = winx;
		float winY = viewport[3] - winy;
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
	public Ray calcRay(final GL2 gl){
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
		Ray ray = new Ray();
		for(int i=0;i<posFar.length;i++){
			ray.orig[i] = posNear[i];
			ray.dir[i] = posFar[i];
		}
		/*
		float[] ray = new float[6];
		System.arraycopy(posNear, 0, ray, 0, 3);
		System.arraycopy(posFar, 0, ray, 3, 3);*/
		return ray;
	}
	private void selectObject(GL2 gl, String layerName){

		Ray pickRay = calcRay(gl) ;
		switch (layerName){
			case "Node":
				break;
			case "Link":
				break;
			case "Segment":
				//路段选择
				for(int i=0;i<drawableNetwork.nSegments();i++){
					Segment tmpSegment = drawableNetwork.getSegment(i);
					if(GeoUtil.isIntersect(pickRay,tmpSegment.getSurface())){
						//被选中对象用黄色渲染
						tmpSegment.setSelected(true);
						// TODO 临时添加
						ShapeUtil.drawPolygon(gl, tmpSegment.getSurface().getKerbList(),Constants.COLOR_GREY, tmpSegment.isSelected());
						pickedObjects.add(tmpSegment);
					}
				}
				break;
			case "Lane":
				//车道选择
				for(int i=0;i<drawableNetwork.nLanes();i++){
					Lane tmpLane = drawableNetwork.getLane(i);
					if(GeoUtil.isIntersect(pickRay,tmpLane.getSurface())){
						//被选中对象用黄色渲染
						tmpLane.setSelected(true);
						pickedObjects.add(tmpLane);
					}
				}
				break;
			case "Vehicle":
				//选择车辆
				if(status==ANIMATOR_PAUSE && curFrame!=null) {
					for (VehicleData vd : curFrame.getVhcDataQueue()) {
						if (GeoUtil.isIntersect(pickRay, vd.getVhcShape())) {
							//被选中对象用黄色渲染
							vd.setSelected(true);
							pickedObjects.add(vd);
						}
					}
				}
				break;
			case "Sensor":
				break;
			default:
				System.out.println("The name of layer might be wrong!");
				break;
		}
		//TODO 写死读取第一个对象
		if(!pickedObjects.isEmpty()){
			mainWindow.getLayerPanel().getAction(layerName).writeTxtComponents(pickedObjects.get(0));
		}
	}

	public void deselect(){
		for(NetworkObject no:pickedObjects){
			no.setSelected(false);
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
				//清空前一帧已选对象
				if(!pickedObjects.isEmpty()){
					deselect();
					pickedObjects.clear();
					mainWindow.getLayerPanel().getAction(mainWindow.getCurLayerName())
							.resetTxtComponents();
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
			cam.calcMouseMotion(e.getX() - preWinCoods.x, e.getY() - preWinCoods.y, MouseEvent.BUTTON2);
			preWinCoods.setLocation(e.getX(), e.getY());
		}
		else if (isRightBtnDragged){
			cam.calcMouseMotion(e.getX() - preWinCoods.x, e.getY() - preWinCoods.y, MouseEvent.BUTTON3);
			preWinCoods.setLocation(e.getX(), e.getY());
		}
	}
	@Override
	public void mouseMoved(MouseEvent e) {

		preWinCoods.setLocation(e.getX(), e.getY());
	}
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		cam.calcMouseWheelMotion(e.getWheelRotation());
	}
	@Override
	public void keyTyped(KeyEvent e) {

	}
	@Override
	public void keyPressed(KeyEvent e) {
		cam.calcKeyMotion(e.getKeyCode());
	}
	@Override
	public void keyReleased(KeyEvent e) {

	}

	//wym
	private List<GeoPoint> platoonBoundGen(VehicleData vd) {
		List<GeoPoint> pts = vd.getVhcShape().getKerbList();
		double[] v1 = LinearAlgebra.minus(pts.get(1).getLocCoods(),pts.get(0).getLocCoods());
		double[] v2 = LinearAlgebra.minus(pts.get(2).getLocCoods(),pts.get(1).getLocCoods());
		double d1 = Math.sqrt(Arrays.stream(v1).map(e -> e*e).sum());
		double d2 = Math.sqrt(Arrays.stream(v2).map(e -> e*e).sum());
		double[] el, et;
		if(d1<d2) {
			el = LinearAlgebra.times(v2, 1/d2);
			et = LinearAlgebra.times(v1, 1/d1);
		}
		else {
			el = LinearAlgebra.times(v1, 1/d1);
			et = LinearAlgebra.times(v2, 1/d2);
		}
		double[] nt = LinearAlgebra.times(et,Constants.LANE_WIDTH);
		double[] nl = LinearAlgebra.times(et,0.5);
		List<GeoPoint> ans = new ArrayList<>();
		double[] newP = LinearAlgebra.minus(vd.getHeadPosition().getLocCoods(),LinearAlgebra.times(nt,0.5));
		ans.add(new GeoPoint(newP));
		newP = LinearAlgebra.plus(newP,nl);
		ans.add(new GeoPoint(newP));
		newP = LinearAlgebra.plus(newP,nt);
		ans.add(new GeoPoint(newP));
		newP = LinearAlgebra.minus(newP,nl);
		ans.add(new GeoPoint(newP));
		return ans;
	}
}
