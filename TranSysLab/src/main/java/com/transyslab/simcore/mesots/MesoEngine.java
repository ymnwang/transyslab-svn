/**
 *
 */
package com.transyslab.simcore.mesots;

import java.io.IOException;
import java.util.HashMap;

import com.transyslab.commons.renderer.JOGLAnimationFrame;
import com.transyslab.commons.tools.DE;
import com.transyslab.commons.tools.PSO;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.LinkTimes;
import com.transyslab.simcore.SimulationEngine;

/**
 * @author its312
 *
 */
public class MesoEngine extends SimulationEngine {

	protected int runTimes_; // 仿真运行次数
	protected float frequency_; // 1/step size
	protected double batchTime_;
	protected double updateTime_;
	protected double pathTime_;
	protected double stateTime_;
	protected double batchStepSize_; // update console and queue info
	protected double stateStepSize_; // write state data for dta
	protected double updateStepSize_; // update traffic cell variables
	protected double pathStepSize_;
	protected double depRecordStepSize_;
	protected int iteration_; // id of the iteration
	protected int firstEntry = 1; // simulationLoop中第一次循环的标记
	protected double depRecordTime_;
	private int parseODID_;
	private int[][] simFlow_;
	private float[][] simTraTime_;
	private int[][] realFlow_;
	private float[][] realTraTime_;
	private double updateDetTime_;
	private double detStepSize_;
	private PSO pso_;
	private DE de_;
	private float tempBestFitness_;
	private float[] tempBest_;
	public float alpha_;
	public float Beta_;

	public MesoEngine() {
		// 浮动车旅行时间
		realFlow_ = new int[3][34];
		realTraTime_ = new float[3][34];
		tempBestFitness_ = Constants.FLT_INF;
		runTimes_ = 1;
		batchStepSize_ = 10; // update console and queue info
		stateStepSize_ = 60.0; // write state data for dta
		MesoParameter.getInstance().setUpdateStepSize(10);
		updateStepSize_ = MesoParameter.getInstance().getUpdateStepSize(); // update traffic cell variables
		pathStepSize_ = Constants.ONE_DAY;// ONE_DAY宏定义
		depRecordStepSize_ = 5;
		detStepSize_ = 300; // 检测器统计间隔，默认为30s
//		rollingLength_ = 86400; // length of the rolling horizon
//		rolls_ = 0; // number of rollings
		iteration_ = 0; // id of the iteration
		parseODID_ = 0;
		//默认仿真时间和步长
		SimulationClock.getInstance().init(28800.0, 32400.0, 1.0);

		// Default parameter filename
	}
	public double beginTime() {
		return beginTime_;
	}
	public double endTime() {
		return endTime_;
	}
	public float getFrequency() {
		return frequency_;
	}

	public void init() {
		// 通过parameter 赋值,结束时间往后推300s
		SimulationClock.getInstance().init(43200, 84900, 0.2);

		double now = SimulationClock.getInstance().getCurrentTime();
		batchTime_ = now;
		updateTime_ = now;
		updateDetTime_ = now + detStepSize_;
		pathTime_ = now + pathStepSize_;
		stateTime_ = now;
		frequency_ = (float) (1.0 / SimulationClock.getInstance().getStepSize());
	}

	public void initPSO(PSO pso) {
		pso_ = pso;
		tempBest_ = new float[pso_.getDim()];
	}
	public void initDE(DE de) {
		de_ = de;
		tempBest_ = new float[de_.getDim()];
	}
	public void resetBeforeSimLoop() {
		firstEntry = 1;
		SimulationClock.getInstance().init(12 * 3600, 14 * 3600 + 50 * 60, 0.2);
		double now = SimulationClock.getInstance().getCurrentTime();
		batchTime_ = now;
		updateTime_ = now;
		pathTime_ = now + pathStepSize_;
		stateTime_ = now;
		frequency_ = (float) (1.0 / SimulationClock.getInstance().getStepSize());
		MesoNetwork.getInstance().resetLinkStatistics();
		MesoNetwork.getInstance().clean();
		parseODID_ = 1;
		MesoODTable.getInstance().setNextTime(0);
	}
	// 读入所有输入文件，包含MasterFile（仿真配置文件）和SimulationFile（仿真参数文件）
	@Override
	public void loadFiles() {
		if (canStart() > 0) {
			loadMasterFile();
		}
		loadSimulationFiles();
	}
	@Override
	public void run() {

		while (simulationLoop() >= 0) {

		}

	}

	// 多次运行
	public void run(int mode) {

		if (mode == 1) {
			HashMap<String, Integer> hm = MesoNetworkPool.getInstance().getHashMap();
			int threadid = hm.get(Thread.currentThread().getName()).intValue();

			for (int i = threadid * pso_.getPcount() / Constants.THREAD_NUM; i < threadid * pso_.getPcount()
					/ Constants.THREAD_NUM + pso_.getPcount() / Constants.THREAD_NUM; i++) {
				if (pso_.getParticle(i).isFeasible()) {
					MesoNetwork.getInstance().updateSdFns(pso_.getParticle(i).getAlpha(),
							pso_.getParticle(i).getBeta());
					// MESO_Parameter.getInstance().setRspLower(pso_.getParticle(i).getDLower());
					// MESO_Parameter.getInstance().setRspUpper(pso_.getParticle(i).getDUpper());
					// MESO_Parameter.getInstance().updateCSG();
					while (simulationLoop() >= 0) {
					}
					// 计算适应度，并更新pbest
					pso_.getParticle(i).evaMRE(simFlow_, simTraTime_, realFlow_, realTraTime_, 0);
					// 更新gbest
					if (tempBestFitness_ > pso_.getParticle(i).getFitness()) {
						tempBestFitness_ = (float) pso_.getParticle(i).getFitness();
						// 更新当代最优解
						for (int j = 0; j < pso_.getDim(); j++) {
							tempBest_[j] = pso_.getParticle(i).getPos()[j];
						}
						// Particle.updateGbest(pso_.pars_[i].getPos());
					}
				}

				pso_.posToLearn(i);

				// 更新粒子速度以及位置
				pso_.getParticle(i).updateVel(pso_.getParaW(), pso_.getParaC1(), pso_.getParaPl(), pso_.getParaPu(),
						pso_.getParaVl(), pso_.getParaVu());
				resetBeforeSimLoop();
				simFlow_ = null;
				simTraTime_ = null;
			}
		}
		else if (mode == 2) {
			// DE算法同步gbest的标记
			HashMap<String, Integer> hm = MesoNetworkPool.getInstance().getHashMap();
			int threadid = hm.get(Thread.currentThread().getName()).intValue();
			int si = threadid * de_.getPopulation() / Constants.THREAD_NUM;
			int ei = threadid * de_.getPopulation() / Constants.THREAD_NUM + de_.getPopulation() / Constants.THREAD_NUM;
			for (int i = si; i < ei; i++) {
				MesoNetwork.getInstance().updateSdFns(de_.getNewPosition(i)[2], de_.getNewPosition(i)[3]);
				MesoParameter.getInstance().setRspLower(de_.getNewPosition(i)[0]);
				MesoParameter.getInstance().setRspUpper(de_.getNewPosition(i)[1]);
				MesoParameter.getInstance().updateCSG();
				while (simulationLoop() >= 0) {
				}
				de_.getNewIdvds()[i].evaMRE(simFlow_, simTraTime_, realFlow_, realTraTime_, 0);
				de_.selection(i);
				if (tempBestFitness_ > de_.getFitness(i)) {
					tempBestFitness_ = de_.getFitness(i);
					// tempIndex_ = i;
					for (int j = 0; j < tempBest_.length; j++) {
						tempBest_[j] = de_.getPosition(i)[j];
					}

				}
				de_.changePos(i);
				resetBeforeSimLoop();
				simFlow_ = null;
				simTraTime_ = null;

			}
		} 
	}
	public void exhaustionRun(float mp, float step) {
		HashMap<String, Integer> hm = MesoNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		int si = threadid * (int) (mp) / Constants.THREAD_NUM;
		int ei = si + (int) (mp) / Constants.THREAD_NUM;
		float tmpfn;
		float a = si + 0.01f, b = 0.01f;
		while (b < 5) {
			while (a < ei + 0.01) {
				MesoNetwork.getInstance().updateSdFns(a, b);
				while (simulationLoop() >= 0) {
				}
				tmpfn = evaMRE(0);
				if (tempBestFitness_ > tmpfn) {
					tempBestFitness_ = tmpfn;
					// 更新当代最优解
					alpha_ = a;
					Beta_ = b;
				}
				resetBeforeSimLoop();
				simFlow_ = null;
				simTraTime_ = null;
				a = a + step;
			}
			a = si + 0.01f;
			b = b + step;
		}

	}
	public float evaMRE(float w) {
		int col = simFlow_[0].length;
		int row = simFlow_.length;
		double sumOfLinkFlowError;
		double sumOfLinkTimeError;
		double sumError = 0;
		for (int j = 0; j < col; j++) {
			sumOfLinkFlowError = 0;
			sumOfLinkTimeError = 0;
			for (int i = 0; i < row; i++) {
				if (realFlow_[i][j] == 0)
					realFlow_[i][j] = 1;
				sumOfLinkFlowError = sumOfLinkFlowError
						+ (Math.abs(realFlow_[i][j] - simFlow_[i][j])) / realFlow_[i][j];
				sumOfLinkTimeError = sumOfLinkTimeError
						+ (Math.abs(realTraTime_[i][j] - simTraTime_[i][j])) / realTraTime_[i][j];
			}
			sumError = sumError + w * (sumOfLinkFlowError) + (1 - w) * (sumOfLinkTimeError);
		}
		return (float) (sumError / col);
	}
	// 新增方法，按二维数组的形式组织流量与时间的输出
	// 行记录不同link，列记录不同时段
	public void organize2DFlow() {
		int linknum = MesoNetwork.getInstance().nLinks();
		int timenum = LinkTimes.getInstance().infoPeriods();
		simFlow_ = new int[3][timenum];
		for (int i = 0; i < linknum; i++) {
			if (MesoNetwork.getInstance().getLink(i).getCode() == 64)
				simFlow_[0] = MesoNetwork.getInstance().getLink(i).getSumOfFlow();
			else if (MesoNetwork.getInstance().getLink(i).getCode() == 60)
				simFlow_[1] = MesoNetwork.getInstance().getLink(i).getSumOfFlow();
			else if (MesoNetwork.getInstance().getLink(i).getCode() == 116)
				simFlow_[2] = MesoNetwork.getInstance().getLink(i).getSumOfFlow();
		}
	}
	public void organize2DTraTime() {
		int linknum = MesoNetwork.getInstance().nLinks();
		int timenum = LinkTimes.getInstance().infoPeriods();
		simTraTime_ = new float[3][timenum];
		for (int i = 0; i < linknum; i++) {
			if (MesoNetwork.getInstance().getLink(i).getCode() == 64)
				simTraTime_[0] = MesoNetwork.getInstance().getLink(i).getAvgTravelTime();
			else if (MesoNetwork.getInstance().getLink(i).getCode() == 60)
				simTraTime_[1] = MesoNetwork.getInstance().getLink(i).getAvgTravelTime();
			else if (MesoNetwork.getInstance().getLink(i).getCode() == 116)
				simTraTime_[2] = MesoNetwork.getInstance().getLink(i).getAvgTravelTime();
		}
	}
	public int[][] getSimFlow() {
		return simFlow_;
	}
	public float[][] getSimTraTime() {
		return simTraTime_;
	}
	public float getTempBestFitness() {
		return tempBestFitness_;
	}
	public float[] getTempBest() {
		return tempBest_;
	}
	public void quit(int state) {

	}
	public int loadSimulationFiles() {

		// 初始化SimulationClock,此处赋开始时间，结束时间
		init();
		// 读取所有xml输入文件
		// MESO_Setup.ParseFCDTable(realTraTime_);
		MesoSetup.ParseParameters();
		MesoSetup.ParseNetwork();
		// 读入路网数据后组织路网不同要素的关系
		MesoNetwork.getInstance().calcStaticInfo();
		// 计算路网要素几何信息，用于绘图
		// MesoNetwork.getInstance().calcGeometircData();
		// 先解析路径表再解析OD表，OD表要用到路径表信息
		MesoSetup.ParsePathTables();
		// RN_DynamicRoute.parseTravelTimeTables('e');
		// MESO_Setup.SetupMiscellaneous();
		// 初始化记录旅行时间的对象，新增代码
		LinkTimes.getInstance().initTravelTimes();
		// 根据旅行时间的输出时间间隔初始化记录数组
		MesoNetwork.getInstance().initializeLinkStatistics();
		// 更新不同路段的速密函数
		// MESO_Network.getInstance().setsdIndex();
		MesoSetup.ParseSensorTables();

		// 输出路网信息
		/*try {
			MesoNetwork.getInstance().outputSegments();
		}
		catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}*/

		// 空函数
		start();
		parseODID_ = 1;
		return 0;
	}

	public int loadMasterFile() {

		state_ = Constants.STATE_OK;// STATE_OK宏定义,a step is done (clock
									// advanced)
		return 0;
	}

	@Override
	public int simulationLoop() {

		final double epsilon = 1.0E-3;
		// 实例化路网对象，一个线程对应一个实例
		MesoNetwork meso_network = MesoNetwork.getInstance();

		double now = SimulationClock.getInstance().getCurrentTime();

		if (firstEntry != 0) {
			firstEntry = 0;

			// This block is called only once just before the simulation gets
			// started.
			meso_network.resetSegmentEmitTime();
			// 加载事件
			MesoIncident ic = new MesoIncident();
			ic.init(1, 33000, 39900, 18, -1.0f);
		}

		// SAVE GENERATED VEHICLE WHICH CAN BE LOADED IN LATER RUNS
		// 记录发车时间
		if (now >= depRecordTime_) {
			// MesoVehicle.nextBlockOfDepartureRecords();
			depRecordTime_ = now + depRecordStepSize_;
		}

		// Update OD trip tables

		if (MesoODTable.getInstance().getNextTime() <= now) {
			// MESO_ODTable.theODTable.read();
			// 读对应时段的OD信息
			MesoSetup.ParseODTripTables(parseODID_);
			MesoODTable.getInstance().sortODCell();
			parseODID_++;

		}

		// Create vehicles based on trip tables

		MesoODTable.getInstance().emitVehicles();

		// ENTER VEHICLES INTO THE NETWORK

		// Move vehicles from vitual queue into the network if they could
		// enter the network at present time.

		meso_network.enterVehiclesIntoNetwork();

		// UPDATE PHASE : Calculate the density of speeds of all TCs in the
		// network based on their current state.

		// Every update step we reset segment capacity balance to 0 to
		// prevent consumption of accumulated capacities.

		if (now >= updateTime_) {
			// UpdateCapacities(); // update incident related capacity
			/*
			 * if(now>=ic_.getStartTime()&&ic_.getNeedChange()){
			 * ic_.updateCapacity(); ic_.setNeedChange(false); }
			 * if(now>=ic_.getEndTime()&&ic_.getNeedResume()){
			 * ic_.resumeCapacity(); ic_.setNeedResume(false); }
			 */
			meso_network.resetSegmentEmitTime();

			updateTime_ = now + updateStepSize_;
		}
		// 更新检测器统计时间
		if (now > updateDetTime_) {
			meso_network.updateSurvStationMeasureTime();
			updateDetTime_ = updateDetTime_ + detStepSize_;
			meso_network.calcSegmentInfo();
		}
		meso_network.calcTrafficCellUpSpeed();
		meso_network.calcTrafficCellDnSpeeds();

		// ADVANCE PHASE : Update position of TCs, move vehicles from TC to
		// TC and create/destroy TCs as necessary.

		meso_network.advanceVehicles();
		MesoVehicle.increaseCounter();

		if (now >= batchTime_) {
			batchTime_ = now + batchStepSize_;
		}

		// Report network state

		if (now >= stateTime_) {
			// meso_network.save_3d_state(rolls_-1, tm_);
			stateTime_ = now + stateStepSize_;
		}
		// 输出步长内所有车辆的位置信息
		/*
		 * try { MESO_Network.getInstance().outputVhcPosition(); } catch
		 * (IOException e) { // TODO 自动生成的 catch 块 e.printStackTrace(); }
		 */
		//当前帧在网车辆的位置信息存储到framequeue
		if(MesoVehicle.nVehicles()!=0)
			meso_network.recordVehicleData();
		// Advance the clock
		
//		System.out.println(MesoVehicle.nVehicles());
		SimulationClock.getInstance().advance(SimulationClock.getInstance().getStepSize());
		if (now > SimulationClock.getInstance().getStopTime() + epsilon) {
			// HashMap<String, Integer> hm =
			// MESO_InfoArrays.getInstance().getHashMap();
			// int threadid =
			// hm.get(Thread.currentThread().getName()).intValue();
			// MESO_Network.getInstance().outputLinkFlowPlusTravelTimes(threadid);
			// 将仿真方案结果输出到oracle
			/*
			 * MESO_Network.getInstance().outputModelSegmentDataToOracle();
			 * MESO_Network.getInstance().outputTaskSegmentDataToOracle();
			 * MESO_Network.getInstance().outputModelSensorDataToOracle();
			 * MESO_Network.getInstance().outputTaskSensorDataToOracle();
			 */
			organize2DFlow();
			organize2DTraTime();
			return (state_ = Constants.STATE_DONE);// STATE_DONE宏定义 simulation
													// is done
		}
		else
			return state_ = Constants.STATE_OK;// STATE_OK宏定义
	}
	@Override
	public void start() {
		// TODO 自动生成的方法存根
		
	}

}
