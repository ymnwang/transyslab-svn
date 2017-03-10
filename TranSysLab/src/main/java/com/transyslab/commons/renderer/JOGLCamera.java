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
	private int[] eyeLocation_ = new int[3];
	private int[] targetLocation_ = new int[3];
	private boolean isMidButtonPressed_;
	private int[] preWinCoordinate_;
	public boolean canStart_ = false;
	public JOGLCamera() {
		keyStep_ = 1.0;
		mouseStep_ = 1.0;
		mouseSensitive_ = 5.0;
		keySensitive_ = 5.0;
		preWinCoordinate_ = new int[2];

	}
	public int[] getEyePosition() {
		return eyeLocation_;
	}
	public int[] getTargetPosition() {
		return targetLocation_;
	}
	public void initCamLookAt(Point curp, Point tarp) {
		targetLocation_[0] = (int) tarp.getLocationX();  
		targetLocation_[1] = (int) tarp.getLocationY();
		targetLocation_[2] = (int) tarp.getLocationZ();
		eyeLocation_[0] = (int) curp.getLocationX();
		eyeLocation_[1] = (int) curp.getLocationY();
		eyeLocation_[2] = (int) curp.getLocationZ();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO 自动生成的方法存根

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO 自动生成的方法存根

		if(e.getButton()==MouseEvent.BUTTON2 ){
		
			preWinCoordinate_[0] = e.getX();
			preWinCoordinate_[1] = e.getY();
			isMidButtonPressed_ = true;
		}

		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO 自动生成的方法存根
		if(e.getButton() == MouseEvent.BUTTON2)
			isMidButtonPressed_ = false;

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
				eyeLocation_[1] += keyStep_;
				targetLocation_[1] += keyStep_;
				break;
			case KeyEvent.VK_DOWN :
				eyeLocation_[1] -= keyStep_;
				targetLocation_[1] -= keyStep_;
				break;
			case KeyEvent.VK_RIGHT :
				eyeLocation_[0] += keyStep_;
				targetLocation_[0] += keyStep_;
				break;
			case KeyEvent.VK_LEFT :
				eyeLocation_[0] -= keyStep_;
				targetLocation_[0] -= keyStep_;
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
			eyeLocation_[2] += mouseStep_;
		if (count < 0)
			eyeLocation_[2] -= mouseStep_;
		if(eyeLocation_[2]<0)
			eyeLocation_[2] = 0;
//		System.out.println(eyeLocation_[2]);

	}
	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		if(isMidButtonPressed_){
			eyeLocation_[0] += 0.1*(e.getX()-preWinCoordinate_[0]);
			targetLocation_[0] += 0.1*(e.getX()-preWinCoordinate_[0]);
			eyeLocation_[1] -= 0.1*(e.getY()-preWinCoordinate_[1]);
			targetLocation_[1] -= 0.1*(e.getY()-preWinCoordinate_[1]);
			
		}
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		
	}

}
