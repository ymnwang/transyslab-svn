/**
 *
 */
package com.transyslab.simcore.mesots;

import java.util.HashMap;
import java.util.Vector;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.LinkTimes;
import com.transyslab.roadnetwork.PathTable;
import com.transyslab.roadnetwork.RoadNetworkPool;

/**
 * @author yali
 *
 */
public class MesoNetworkPool extends RoadNetworkPool {

	private MesoNetwork[] networkArray_;
	private MesoVehiclePool[] vhcListArray_;
	private MesoCellList[] cellListArray_;
	private MesoParameter[] parameterArray_;
	private SimulationClock[] simClockArray_;
	private MesoODTable[] odTableArray_;


	//外部车辆数据
	private MesoVehicleTable[] vhcTableArray_;
	
	private HashMap<String, Integer> threadMap_;

	private static MesoNetworkPool theInfoArrays;
	public static MesoNetworkPool getInstance() {
		// 延迟实例化
		if (theInfoArrays == null)
			theInfoArrays = new MesoNetworkPool();
		return theInfoArrays;
	}
	private MesoNetworkPool() {
	}
	// 数据数组
	@Override
	public void init(int n, RoadNetworkPool pool) {
		setInstance(pool);
		threadNum_ = n;
		threadIndex_ = new int[threadNum_];
		networkArray_ = new MesoNetwork[threadNum_];
		vhcListArray_ = new MesoVehiclePool[threadNum_];
		cellListArray_ = new MesoCellList[threadNum_];
		parameterArray_ = new MesoParameter[threadNum_];
		simClockArray_ = new SimulationClock[threadNum_];
		odTableArray_ = new MesoODTable[threadNum_];
		pathTableArray_ = new PathTable[threadNum_];
		linkTimesArray_ = new LinkTimes[threadNum_];
		randomArray_ = new Vector<Vector<MesoRandom>>();
		vhcTableArray_ = new MesoVehicleTable[threadNum_];
		// 实例化数组内的对象
		initArrays();
	}

	@Override
	public void initArrays() {
		for (int i = 0; i < threadNum_; i++) {
			threadIndex_[i] = i;
			networkArray_[i] = new MesoNetwork();
			pathTableArray_[i] = new PathTable();
			linkTimesArray_[i] = new LinkTimes();
			odTableArray_[i] = new MesoODTable();
			vhcListArray_[i] = new MesoVehiclePool();
			cellListArray_[i] = new MesoCellList();
			parameterArray_[i] = new MesoParameter();
			simClockArray_[i] = new SimulationClock();
			vhcTableArray_[i] = new MesoVehicleTable();
			randomArray_.add(new Vector<MesoRandom>());
			MesoRandom.create(3, randomArray_.get(i));
		}
	}

	@Override
	public MesoNetwork getNetwork(int i) {
		return networkArray_[i];
	}
	public MesoVehiclePool getVhcList(int i) {
		return vhcListArray_[i];
	}
	public MesoCellList getCellList(int i) {
		return cellListArray_[i];
	}
	@Override
	public MesoParameter getParameter(int i) {
		return parameterArray_[i];
	}
	@Override
	public SimulationClock getSimulationClock(int i) {
		return simClockArray_[i];
	}
	@Override
	public MesoODTable getODTable(int i) {
		return odTableArray_[i];
	}
	public int getThreadNum() {
		return threadNum_;
	}
	@Override
	public MesoVehicleTable getVhcTable(int i) {
		return vhcTableArray_[i];
	}

}
