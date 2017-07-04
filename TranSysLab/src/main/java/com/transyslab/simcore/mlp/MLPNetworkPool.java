package com.transyslab.simcore.mlp;

import java.util.Vector;

import com.transyslab.simcore.mesots.MesoRandom;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.LinkTimes;
import com.transyslab.roadnetwork.PathTable;
import com.transyslab.roadnetwork.RoadNetworkPool;

public class MLPNetworkPool extends RoadNetworkPool{
	private MLPNetwork[] networkArray_;
	private MLPVehList[] vhcListArray_;
	private MLPParameter[] parameterArray_;
	private SimulationClock[] simClockArray_;
	private MLPODTable[] odTableArray_;


	private static MLPNetworkPool theInfoArrays;
	public static MLPNetworkPool getInstance() {
		// 延迟实例化
		if (theInfoArrays == null)
			theInfoArrays = new MLPNetworkPool();
		return theInfoArrays;
	}
	private MLPNetworkPool() {
	}
	// 数据数组
	@Override
	public void init(int n, RoadNetworkPool pool) {
		setInstance(pool);
		threadNum_ = n;
		threadIndex_ = new int[threadNum_];
		networkArray_ = new MLPNetwork[threadNum_];
		vhcListArray_ = new MLPVehList[threadNum_];
		parameterArray_ = new MLPParameter[threadNum_];
		simClockArray_ = new SimulationClock[threadNum_];
		odTableArray_ = new MLPODTable[threadNum_];
		pathTableArray_ = new PathTable[threadNum_];
		linkTimesArray_ = new LinkTimes[threadNum_];
		randomArray_ = new Vector<Vector<MesoRandom>>();
		// 实例化数组内的对象
		initArrays();
	}

	@Override
	public void initArrays() {
		for (int i = 0; i < threadNum_; i++) {
			threadIndex_[i] = i;
			networkArray_[i] = new MLPNetwork();
			pathTableArray_[i] = new PathTable();
			linkTimesArray_[i] = new LinkTimes();
			odTableArray_[i] = new MLPODTable();
			vhcListArray_[i] = new MLPVehList();
			parameterArray_[i] = new MLPParameter();
			simClockArray_[i] = new SimulationClock();
			randomArray_.add(new Vector<MesoRandom>());
			MesoRandom.create(3, randomArray_.get(i));
		}
	}

	@Override
	public MLPNetwork getNetwork(int i) {
		return networkArray_[i];
	}
	public MLPVehList getVhcList(int i) {
		return vhcListArray_[i];
	}
	@Override
	public MLPParameter getParameter(int i) {
		return parameterArray_[i];
	}
	@Override
	public SimulationClock getSimulationClock(int i) {
		return simClockArray_[i];
	}
	@Override
	public MLPODTable getODTable(int i) {
		return odTableArray_[i];
	}
	public int getThreadNum() {
		return threadNum_;
	}
	@Override
	public VehicleTable getVhcTable(int i) {
		// TODO Auto-generated method stub
		return null;
	}

}
