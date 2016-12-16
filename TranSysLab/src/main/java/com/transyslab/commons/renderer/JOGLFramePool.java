package com.transyslab.commons.renderer;

import java.util.LinkedList;

public class JOGLFramePool {
	private LinkedList<JOGLAnimationFrame> frameList_;
	private int nFrames_;
	private static JOGLFramePool theFramePool_; 
	
	public static JOGLFramePool getFramePool(){
		if(theFramePool_==null)
			theFramePool_ = new JOGLFramePool();
		return theFramePool_;
	}
	private JOGLFramePool(){
		frameList_ = new LinkedList<JOGLAnimationFrame>();
		nFrames_ = 0;
	}
	public void recycleFrame(JOGLAnimationFrame frame){
		frame.clean();
		frameList_.offerLast(frame);
	}
	public JOGLAnimationFrame getFrame() /* get a vehicle from the list */
	{
		JOGLAnimationFrame frame;

		if (frameList_.isEmpty()) { // list is empty
			frame = new JOGLAnimationFrame();
			nFrames_++;
		}
		else { // get head from the list
			frame = frameList_.pollFirst();
		}
		return frame;
	}
	public int getNewFrames(){
		return nFrames_;
	}
}
