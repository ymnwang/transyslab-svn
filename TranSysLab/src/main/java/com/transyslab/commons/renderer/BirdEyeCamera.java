package com.transyslab.commons.renderer;
import java.awt.event.KeyEvent;

import com.jogamp.newt.event.MouseEvent;

public class BirdEyeCamera extends Camera{
	protected float keySensitive,keyStep;
	protected float zoomSpeed,scale;
	public BirdEyeCamera() {
		keyStep = 1.0f;
		keySensitive = 5.0f;
		zoomSpeed = 1.0f;
		scale = 1.0f;
	}
	public float[] getEyePosition() {
		return eyeLocation;
	}
	public float[] getTargetPosition() {
		return tarLocation;
	}

	public void calcMouseMotion(final int deltaWinX, final int deltaWinY, final int mouseButton){
		if(mouseButton == MouseEvent.BUTTON2){
			this.tarMotion[0] = this.eyeMotion[0] = -(deltaWinX) * eyeLocation[2] / 500.0f;
			this.tarMotion[1] = this.eyeMotion[1] = (deltaWinY) * eyeLocation[2] / 500.0f;
			this.tarMotion[2] = this.eyeMotion[2] = 0.0f;
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
			this.eyeMotion[2] = (this.eyeLocation[2] - this.tarLocation[2]) * this.scale;
			update();
			this.scale = 1.0f;
		
	}
	public void calcKeyMotion(final int keyCode){

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
		}
		update();
	}
	
}
