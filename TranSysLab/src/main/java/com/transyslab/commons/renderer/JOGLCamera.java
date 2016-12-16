package com.transyslab.commons.renderer;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;

public class JOGLCamera implements KeyListener, MouseListener, MouseWheelListener {

	private double sensitive_;
	private double keyStep_;
	private double mouseStep_;
	private double[] eyePosition_ = {271, 614, 20};
	private double[] targetPosition_ = {271, 614, 0};
	public JOGLCamera() {
		// 271, 614, 20, 271, 614, 0
		// eyePosition_ = new double[3];
		// targetPosition_ = new double[3];
		keyStep_ = 1.0;
		mouseStep_ = 1.0;
		sensitive_ = 20.0;

	}
	public double[] getEyePosition() {
		return eyePosition_;
	}
	public double[] getTargetPosition() {
		return targetPosition_;
	}
	public void setCamPosition() {

	}
	public void setCamera(GL2 gl, GLU glu) {
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
				keyStep_ += sensitive_;
				mouseStep_ += sensitive_;
				break;
			case KeyEvent.VK_PAGE_DOWN :
				keyStep_ -= sensitive_;
				mouseStep_ -= sensitive_;
				break;
			case KeyEvent.VK_SPACE :
				break;
			case KeyEvent.VK_UP :
				eyePosition_[1] += sensitive_;
				targetPosition_[1] += sensitive_;
				System.out.println(eyePosition_[1]);
				break;
			case KeyEvent.VK_DOWN :
				eyePosition_[1] -= sensitive_;
				targetPosition_[1] -= sensitive_;
				break;
			case KeyEvent.VK_RIGHT :
				eyePosition_[0] += sensitive_;
				targetPosition_[0] += sensitive_;
				break;
			case KeyEvent.VK_LEFT :
				eyePosition_[0] -= sensitive_;
				targetPosition_[0] -= sensitive_;
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
	}

}
