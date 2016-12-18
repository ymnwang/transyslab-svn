package com.transyslab.commons.renderer;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.transyslab.roadnetwork.Point;

public class JOGLCamera implements KeyListener, MouseListener, MouseWheelListener {

	private double mouseSensitive_;
	private double keySensitive_;
	private double keyStep_;
	private double mouseStep_;
	private double[] eyePosition_ = {271, 614, 1000};
	private double[] targetPosition_ = {271, 614, 0};
	public JOGLCamera() {
		keyStep_ = 1.0;
		mouseStep_ = 1.0;
		mouseSensitive_ = 5.0;
		keySensitive_ = 5.0;

	}
	public double[] getEyePosition() {
		return eyePosition_;
	}
	public double[] getTargetPosition() {
		return targetPosition_;
	}
	public void initCamera(Point p, double zHeight) {
		targetPosition_[0] = eyePosition_[0] = p.getLocationX();
		targetPosition_[1] = eyePosition_[1] = p.getLocationY();
		targetPosition_[2] = eyePosition_[2] = zHeight;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO 自动生成的方法存根

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO 自动生成的方法存根

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO 自动生成的方法存根

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO 自动生成的方法存根

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO 自动生成的方法存根

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO 自动生成的方法存根

	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO 自动生成的方法存根
		switch (e.getKeyCode()) {
			case KeyEvent.VK_PAGE_UP :
				keyStep_ += keySensitive_;
				mouseStep_ += mouseSensitive_;
				break;
			case KeyEvent.VK_PAGE_DOWN :
				keyStep_ -= keySensitive_;
				mouseStep_ -= mouseSensitive_;
				break;
			case KeyEvent.VK_SPACE :
				break;
			case KeyEvent.VK_UP :
				eyePosition_[1] += keyStep_;
				targetPosition_[1] += keyStep_;
				break;
			case KeyEvent.VK_DOWN :
				eyePosition_[1] -= keyStep_;
				targetPosition_[1] -= keyStep_;
				break;
			case KeyEvent.VK_RIGHT :
				eyePosition_[0] += keyStep_;
				targetPosition_[0] += keyStep_;
				break;
			case KeyEvent.VK_LEFT :
				eyePosition_[0] -= keyStep_;
				targetPosition_[0] -= keyStep_;
				break;
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO 自动生成的方法存根

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// TODO 自动生成的方法存根
		int count = e.getWheelRotation();
		if (count > 0)
			eyePosition_[2] += mouseStep_;
		if (count < 0)
			eyePosition_[2] -= mouseStep_;
		if(eyePosition_[2]<0)
			eyePosition_[2] = 0;
	}

}
