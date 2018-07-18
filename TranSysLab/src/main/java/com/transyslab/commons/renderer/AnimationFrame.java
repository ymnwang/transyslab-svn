package com.transyslab.commons.renderer;

import java.io.IOException;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedList;

import com.transyslab.commons.io.CSVUtils;
import com.transyslab.roadnetwork.*;
import org.apache.commons.csv.CSVPrinter;

public class AnimationFrame{
	private int frameID_;
	private LinkedList<VehicleData> vhcDataQueue_;
	private LinkedList<StateData> stateDataQueue;
	// 除车辆状态信息外，动画帧的其它信息
	private HashMap info;
	private static int counter = 0;
	//private LocalTime simTime;
    private double simTimeInSeconds;
    private HashMap<SignalArrow, float[]> signalColors;

	public AnimationFrame(){
		vhcDataQueue_ = new LinkedList<>();
		stateDataQueue = new LinkedList<>();
		info = new HashMap<String, Object>();
		counter++;
		frameID_ = counter;
		signalColors = new HashMap<>();
	}
	public LinkedList<VehicleData> getVhcDataQueue(){
		return vhcDataQueue_;
	}
	public LinkedList<StateData> getStateDataQueue(){return stateDataQueue;}
	public void setInfo(String key,Object data){
		info.put(key,data);
	}
	public Object getInfo(String key){
		return info.get(key);
	}
	public HashMap<SignalArrow, float[]> getSignalColors(){
		return this.signalColors;
	}
	public void setSimTimeInSeconds(double seconds){
		this.simTimeInSeconds = seconds;
	}
	public double getSimTimeInSeconds(){
		return this.simTimeInSeconds;
	}
	public void addSignalColor(SignalArrow sa, float[] color){
		this.signalColors.put(sa,color);
	}
	public void setFrameID(int id){
		frameID_ = id;
	}
	public int getFrameID(){
		return frameID_;
	}
	public void addVehicleData(VehicleData vd){
		//从尾部插入对象
		vhcDataQueue_.offerLast(vd);
	}
	public void addStateData(StateData sd){
		stateDataQueue.offerLast(sd);
	}
	public VehicleData getVehicleData(boolean needRetain){
		if(needRetain)//返回头部对象，不做移除
			return vhcDataQueue_.peekFirst();
		else//从头部移除对象
			return vhcDataQueue_.pollFirst();
	}
	public void toCSV(String filePath){
		String[] header = new String[]{"VhcID","VhcType","VhcLength","Flag","LaneID","Speed","Distance","Path"};
		try {
			String fileName = "/仿真快照_"+ String.valueOf(frameID_) + ".csv";
			CSVPrinter printer = CSVUtils.getCSVWriter(filePath + fileName,header,false);
			for(VehicleData vd:vhcDataQueue_){
				String[] row2Write = new String[]{String.valueOf(vd.getId()),String.valueOf(vd.getVehicleType()),String.valueOf(vd.getVhcLength()),
				                                  String.valueOf(vd.getSpecialFlag()),String.valueOf(vd.getCurLaneID()),String.valueOf(vd.getCurSpeed()),
				                                  String.valueOf(vd.getDistance()),vd.getPathInfo()};
				printer.printRecord(row2Write);
			}
			printer.flush();
			printer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void resetCounter(){
		counter = 0;
	}
	public void clean(){
		frameID_ = 0;
		while(!vhcDataQueue_.isEmpty()){
			VehicleDataPool.getInstance().recycle(vhcDataQueue_.pollFirst());
		}
		stateDataQueue.clear();
		signalColors.clear();
	}
}
