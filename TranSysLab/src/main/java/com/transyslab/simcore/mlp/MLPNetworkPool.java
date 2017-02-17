package com.transyslab.simcore.mlp;

import java.util.HashMap;
import java.util.Vector;

import com.transyslab.commons.tools.Random;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.LinkTimes;
import com.transyslab.roadnetwork.PathTable;
import com.transyslab.roadnetwork.RoadNetworkPool;
import com.transyslab.roadnetwork.VehicleTable;
import com.transyslab.simcore.mlp.MLPNetwork;
import com.transyslab.simcore.mlp.MLPNetworkPool;
import com.transyslab.simcore.mlp.MLPODTable;
import com.transyslab.simcore.mlp.MLPParameter;
import com.transyslab.simcore.mlp.MLPVehList;

public class MLPNetworkPool extends RoadNetworkPool{
	private MLPNetwork[] networkArray_;
	private MLPVehList[] vhcListArray_;
	private MLPParameter[] parameterArray_;
	private SimulationClock[] simClockArray_;
	private MLPODTable[] odTableArray_;
	private int[] threadIndex_;
	private int threadNum_;
	private HashMap<String, Integer> threadMap_;

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
		randomArray_ = new Vector<Vector<Random>>();
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
			randomArray_.add(new Vector<Random>());
			Random.create(3, randomArray_.get(i));
		}
	}

	public void organizeHM(Thread[] tl) {
		if (threadMap_ != null)
			threadMap_ = null;
		threadMap_ = new HashMap<String, Integer>();
		for (int i = 0; i < threadNum_; i++) {
			threadMap_.put(tl[i].getName(), threadIndex_[i]);
		}
	}
	@Override
	public HashMap<String, Integer> getHashMap() {
		return threadMap_;
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
