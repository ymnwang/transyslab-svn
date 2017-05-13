package com.transyslab.commons.tools;

import com.jogamp.newt.event.DoubleTapScrollGesture;

public class TimeMeasureUtil {
	public double timeuse = 0.0;
	private double timeBegin;
	
	public void tic() {
		timeBegin = System.currentTimeMillis();
	}
	
	public double toc() {
		double DelT = System.currentTimeMillis() - timeBegin;
		timeuse += DelT;
		return DelT;
	}
}
