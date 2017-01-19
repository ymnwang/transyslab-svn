package com.transyslab.commons.renderer;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.transyslab.roadnetwork.Point;

public class JOGLCamera implements KeyListener, MouseListener, MouseWheelListener, MouseMotionListener {

	private double mouseSensitive_;
	private double keySensitive_;
	private double keyStep_;
	private double mouseStep_;
	private int[] eyePosition_ = {353, 145, 200};
	private int[] targetPosition_ = {353, 145, -100};
	private boolean isMidButtonPressed_;
	private int[] prePosition_;
	public boolean canStart_ = false;
	public JOGLCamera() {
		keyStep_ = 1.0;
		mouseStep_ = 1.0;
		mouseSensitive_ = 5.0;
		keySensitive_ = 5.0;
		prePosition_ = new int[2];

	}
	public int[] getEyePosition() {
		return eyePosition_;
	}
	public int[] getTargetPosition() {
		return targetPosition_;
	}
	public void initCamera(Point p, int zHeight) {
		targetPosition_[0] = eyePosition_[0] = (int) p.getLocationX();
		targetPosition_[1] = eyePosition_[1] = (int) p.getLocationY();
		targetPosition_[2] = eyePosition_[2] = zHeight;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO 自动生成的方法存根

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO 自动生成的方法存根

		if(e.getButton()==MouseEvent.BUTTON2 ){
		
			prePosition_[0] = e.getX();
			prePosition_[1] = e.getY();
			isMidButtonPressed_ = true;
//			System.out.println(prePosition_[0]);
//			System.out.println(prePosition_[1]);
		}

		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO 自动生成的方法存根
		isMidButtonPressed_ =false;

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
			case KeyEvent.VK_SPACE:
				canStart_ = true;
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
//		System.out.println(eyePosition_[2]);
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		if(isMidButtonPressed_){
			eyePosition_[0] += 0.1*(e.getX()-prePosition_[0]);
			targetPosition_[0] += 0.1*(e.getX()-prePosition_[0]);
			eyePosition_[1] -= 0.1*(e.getY()-prePosition_[1]);
			targetPosition_[1] -= 0.1*(e.getY()-prePosition_[1]);
			
		}
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
