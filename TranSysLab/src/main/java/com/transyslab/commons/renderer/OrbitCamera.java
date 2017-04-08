package com.transyslab.commons.renderer;

import java.awt.event.KeyEvent;

import com.jogamp.newt.event.MouseEvent;
import com.jogamp.opengl.math.VectorUtil;
import com.transyslab.roadnetwork.GeoPoint;

import jhplot.fit.BreitWigner;

public class OrbitCamera extends Camera{
	private float zoomSpeed, rotateSpeed, panSpeed;
	private float minDistance;
	private float maxDistance;
	private float minPolarAngle;
	private float maxPolarAngle;
	private final static float EPS = 0.00001f;
	private final static float PIXELS_PER_ROUND = 1800;
	//Çò×ø±êÏµ
	private double theta, thetaDelta;
	private double phi, phiDelta;
	private double radius,scale;
	public OrbitCamera(){
		scale = 1.0;
		zoomSpeed = 1.0f;
		rotateSpeed = 1.0f;
		panSpeed = 1.0f;
		minDistance = Float.NEGATIVE_INFINITY;
		maxDistance = Float.MAX_VALUE;
		
		maxPolarAngle = (float) Math.PI;
		minPolarAngle = 0;
	}
	public void initFirstLookAt(final GeoPoint eyePoint, final GeoPoint tarPoint, final float[] camUp){
		super.initFirstLookAt(eyePoint, tarPoint, camUp);
		float[] offset = new float[3];
		VectorUtil.subVec3(offset, eyeLocation, tarLocation);
		this.theta =  Math.atan2(offset[0], offset[2]);
		this.phi = Math.atan2(Math.sqrt(offset[0] * offset[0] + offset[2] * offset[2]), offset[1]);
		this.radius =  VectorUtil.normVec3(offset) *this.scale;
	}
	public void calcMouseMotion(final int deltaWinX, final int deltaWinY, final int mouseButton){

		if( mouseButton == MouseEvent.BUTTON2){
			this.thetaDelta -= (2.0*Math.PI*(deltaWinX)/PIXELS_PER_ROUND * rotateSpeed);
			this.phiDelta -= (2.0*Math.PI*(deltaWinY)/PIXELS_PER_ROUND * rotateSpeed);
			float[] offset = new float[3];
	/*		VectorUtil.subVec3(offset, eyeLocation, tarLocation);
			this.theta =  Math.atan2(offset[0], offset[2]);
			this.phi = Math.atan2(Math.sqrt(offset[0] * offset[0] + offset[2] * offset[2]), offset[1]);
			*/
			this.theta += this.thetaDelta;
			this.phi += this.phiDelta;
			
			this.phi = Math.max(this.minPolarAngle, Math.min(this.maxPolarAngle, phi));
			this.phi = Math.max( EPS, Math.min( Math.PI - EPS, phi ) );
			
			offset[0] = (float) (radius * Math.sin(phi) * Math.sin(theta));
			offset[1] = (float) (radius * Math.cos(phi));
			offset[2] = (float) (radius * Math.sin(phi) * Math.cos(theta));
			
			VectorUtil.addVec3(eyeLocation, tarLocation, offset);
			this.thetaDelta = 0.0;
			this.phiDelta = 0.0;
		}
		else if(mouseButton == MouseEvent.BUTTON3){
			float[] dir = new float[3];
			VectorUtil.subVec3(dir, eyeLocation, tarLocation);
			float[] right = new float[3];
			VectorUtil.crossVec3(right, dir, this.camUp);
			float[] up = new float[3];
			VectorUtil.crossVec3(up, right, dir);
			VectorUtil.normalizeVec3(up);
			VectorUtil.scaleVec3(up, up, deltaWinY);
			VectorUtil.normalizeVec3(right);
			VectorUtil.scaleVec3(right, right, deltaWinX);
			VectorUtil.addVec3(eyeMotion, right, up);
			VectorUtil.addVec3(tarMotion, right, up);
			update();
		}
		
	}

	public void calcMouseWheelMotion(final int wheelRotation){
		if(wheelRotation>0){
			this.scale /= Math.pow(0.95, zoomSpeed);
		}
		else{
			this.scale *= Math.pow(0.95, zoomSpeed);
		}
		float[] offset = new float[3];	
		this.radius *= this.scale;
		offset[0] = (float) (radius * Math.sin(phi) * Math.sin(theta));
		offset[1] = (float) (radius * Math.cos(phi));
		offset[2] = (float) (radius * Math.sin(phi) * Math.cos(theta));
		
		VectorUtil.addVec3(eyeLocation, tarLocation, offset);
		scale = 1.0;
		
	}
	public void calcKeyMotion(final int keyCode){
		/*
		switch (keyCode) {
			case KeyEvent.VK_PAGE_UP :
				keyStep += keySensitive;
				break;
			case KeyEvent.VK_PAGE_DOWN :
				keyStep -= keySensitive;
				break;
			case KeyEvent.VK_UP :
				eyeMotion[1] += keyStep;
				tarMotion[1] += keyStep;
				break;
			case KeyEvent.VK_DOWN :
				eyeMotion[1] -= keyStep;
				tarMotion[1] -= keyStep;
				break;
			case KeyEvent.VK_RIGHT :
				eyeMotion[0] += keyStep;
				tarMotion[0] += keyStep;
				break;
			case KeyEvent.VK_LEFT :
				eyeMotion[0] -= keyStep;
				tarMotion[0] -= keyStep;
				break;
		}*/
	}
	
}
