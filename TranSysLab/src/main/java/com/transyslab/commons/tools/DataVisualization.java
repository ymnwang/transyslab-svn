package com.transyslab.commons.tools;

import java.awt.Color;
import java.util.List;

import info.monitorenter.gui.chart.traces.Trace2DSimple;
import jhplot.HPlotRT;

public class DataVisualization {
	
	public static int realTimePlot(Trace2DSimple traceSim, List<Double> simData,List<Object> realData){
		HPlotRT plotRT = new HPlotRT();
		Trace2DSimple traceReal = new Trace2DSimple();
		traceSim.setColor(Color.RED);
		traceReal.setColor(Color.BLUE);
		plotRT.add(traceSim);
		plotRT.add(traceReal);
		int i = 0;
		for(i=0;i<realData.size();i++){
			traceReal.addPoint(i, (double) realData.get(i));
		}
		if(simData != null){
			for(i=0;i<simData.size();i++){
				traceSim.addPoint(i, simData.get(i));
			}
		}
		
		return i;
	}
}
