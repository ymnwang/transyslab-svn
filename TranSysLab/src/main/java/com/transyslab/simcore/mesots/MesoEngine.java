/**
 *
 */
package com.transyslab.simcore.mesots;

import java.io.IOException;
import java.util.HashMap;

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

	protected int runTimes_; // �������д���
	protected double beginTime_; // start time for this run
	protected double endTime_; // end time for this run
	protected float frequency_; // 1/step size
	protected double batchTime_;
	protected double updateTime_;
	protected double pathTime_;
	protected double stateTime_;
	protected int tm_; // time period for state3d
	protected double batchStepSize_; // update console and queue info
	protected double stateStepSize_; // write state data for dta
	protected double updateStepSize_; // update traffic cell variables
	protected double pathStepSize_;
	protected double depRecordStepSize_;
	protected int rollingLength_; // length of the rolling horizon
	protected int rolls_; // number of rollings
	protected int iteration_; // id of the iteration
	protected int first_entry = 1; // simulationLoop�е�һ��ѭ���ı��
	protected int tmsConnected = 1;
	protected double depRecordTime;
	protected int mode_;// ����ģʽ��1Ϊ����У׼��0Ϊ�Ӷ��������ļ�����һ�η���
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
	private MesoIncident ic_;

	public MesoEngine() {
		// д��
		realFlow_ = new int[3][34];
		realTraTime_ = new float[3][34];
		tempBestFitness_ = Constants.FLT_INF;
		runTimes_ = 1;
		batchStepSize_ = 10; // update console and queue info
		stateStepSize_ = 60.0; // write state data for dta
		MesoParameter.getInstance().setUpdateStepSize(10);
		updateStepSize_ = MesoParameter.getInstance().getUpdateStepSize(); // update traffic cell variables
		pathStepSize_ = Constants.ONE_DAY;// ONE_DAY�궨��
		depRecordStepSize_ = 5;
		detStepSize_ = 300; // �����ͳ�Ƽ����Ĭ��Ϊ30s
		rollingLength_ = 86400; // length of the rolling horizon
		rolls_ = 0; // number of rollings
		iteration_ = 0; // id of the iteration
		// :SimulationEngineδ����
		// MESO_Exception theException = new MESO_Exception("MesoTS");
		parseODID_ = 0;
		if (SimulationClock.getInstance() == null) {
			// ����
		}

		// MESO_Communicator theCommunicator = new
		// MESO_Communicator(MSG_TAG_MESO);

		SimulationClock.getInstance().init(28800.0, 32400.0, 1.0);

		// Default parameter filename

		if (MesoParameter.getInstance().getName() != null)
			MesoParameter.getInstance().setName(null);
		MesoParameter.getInstance().setName("mesolib.dat");
	}

	public float frequency() {
		return frequency_;
	}

	@Override
	public final String ext() {
		return ".meso";
	}

	/*
	 * public static void loadArrays(){
	 * MESO_InfoArrays.getInstance().loadArrays(); }
	 */
	public void init() {
		// ͨ��parameter ��ֵ,����ʱ��������300s
		SimulationClock.getInstance().init(43200, 84900, 0.2);

		double now = SimulationClock.getInstance().getCurrentTime();
		batchTime_ = now;
		updateTime_ = now;
		updateDetTime_ = now + detStepSize_;
		pathTime_ = now + pathStepSize_;
		stateTime_ = now;
		tm_ = 0;
		frequency_ = (float) (1.0 / SimulationClock.getInstance().getStepSize());
		// Random.getInstance().create(3);
	}

	public void initPSO(PSO pso) {
		pso_ = pso;
		tempBest_ = new float[pso_.getDim()];
	}
	public void initDE(DE de) {
		de_ = de;
		tempBest_ = new float[de_.getDim()];
	}/*
		 * private synchronized void setGbestFitness(float fn,int pi){
		 * if(tempBestFitness_ < de_.getGbestFitness()){
		 * de_.setGbestFitness(tempBestFitness_); de_.setGbestIndex(pi); } }
		 */
	public void resetBeforeSimloop() {
		first_entry = 1;
		SimulationClock.getInstance().init(12 * 3600, 14 * 3600 + 50 * 60, 0.2);
		double now = SimulationClock.getInstance().getCurrentTime();
		batchTime_ = now;
		updateTime_ = now;
		pathTime_ = now + pathStepSize_;
		stateTime_ = now;
		tm_ = 0;
		frequency_ = (float) (1.0 / SimulationClock.getInstance().getStepSize());
		MesoNetwork.getInstance().resetLinkStatistics();
		MesoNetwork.getInstance().clean();
		parseODID_ = 1;
		MesoODTable.getInstance().setNextTime(0);
	}
	// ���߳�ģʽ�³�ʼ�����з�������������Ϣ
	@Override
	public void loadFiles() {
		if (canStart() > 0) {
			loadMasterFile();
		}

		loadSimulationFiles();
	}
	@Override
	public void run() {/*
						 * if (canStart()>0) { loadMasterFile(); }
						 * //���߳����Ӧ��ÿ���߳�ִ������ǰ��ʼ������������Ϣ loadSimulationFiles();
						 */
		/* while (state_ >= 0 && (/*receiveMessages() > 0 || */
		// isWaiting() &&
		/*
		 * receiveMessages(NO_MSG_WAITING_TIME) >= 0 || simulationLoop() >= 0))
		 * { }
		 */

		while (simulationLoop() >= 0) {

		}

	}

	// �������
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
					// ������Ӧ�ȣ�������pbest
					pso_.getParticle(i).evaMRE(simFlow_, simTraTime_, realFlow_, realTraTime_, 0);
					// ����gbest
					if (tempBestFitness_ > pso_.getParticle(i).getFitness()) {
						tempBestFitness_ = (float) pso_.getParticle(i).getFitness();
						// ���µ������Ž�
						for (int j = 0; j < pso_.getDim(); j++) {
							tempBest_[j] = pso_.getParticle(i).getPos()[j];
						}
						// Particle.updateGbest(pso_.pars_[i].getPos());
					}
				}

				pso_.posToLearn(i);

				// ���������ٶ��Լ�λ��
				pso_.getParticle(i).updateVel(pso_.getParaW(), pso_.getParaC1(), pso_.getParaPl(), pso_.getParaPu(),
						pso_.getParaVl(), pso_.getParaVu());
				resetBeforeSimloop();
				simFlow_ = null;
				simTraTime_ = null;
			}
		}
		else if (mode == 2) {
			// DE�㷨ͬ��gbest�ı��
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
				resetBeforeSimloop();
				simFlow_ = null;
				simTraTime_ = null;

			}
		} /*
			 * else if(mode==3){ HashMap<String, Integer> hm =
			 * MESO_InfoArrays.getInstance().getHashMap(); int threadid =
			 * hm.get(Thread.currentThread().getName()).intValue(); }
			 */
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
					// ���µ������Ž�
					alpha_ = a;
					Beta_ = b;
				}
				resetBeforeSimloop();
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
	// ��������������ά�������ʽ��֯������ʱ������
	// �м�¼��ͬlink���м�¼��ͬʱ��
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
		// MESO_FileManager.getInstance().closeOutputFiles();
		// MESO_Status.getInstance().report();
		// super.quit(state);
		// MESO_Status.getInstance().closeLogFile();
		// theException.done(0);
	}
	public void loadInputFiles() {
		// MESO_Setup.ParseFCDTable(realTraTime_);
		MesoSetup.ParseParameters();
		MesoSetup.ParseNetwork();
		// ����·�����ݺ���֯·����ͬҪ�صĹ�ϵ
		MesoNetwork.getInstance().calcStaticInfo();
		// ����·��Ҫ�ؼ�����Ϣ�����ڻ�ͼ
		// MesoNetwork.getInstance().calcGeometircData();
		// �Ƚ���·�����ٽ���OD��OD��Ҫ�õ�·������Ϣ
		MesoSetup.ParsePathTables();
		// RN_DynamicRoute.parseTravelTimeTables('e');
		// MESO_Setup.SetupMiscellaneous();
		// ��ʼ����¼����ʱ��Ķ�����������
		LinkTimes.getInstance().initTravelTimes();
		// ��������ʱ������ʱ������ʼ����¼����
		MesoNetwork.getInstance().initializeLinkStatistics();
		// ���²�ͬ·�ε����ܺ���
		// MESO_Network.getInstance().setsdIndex();
		MesoSetup.ParseSensorTables();
		// ���ģ����캯���ڸ�0
		// MESO_ODTable.getInstance().setNextTime(0);
		// ���·����Ϣ

		try {
			MesoNetwork.getInstance().outputSegments();
		}
		catch (IOException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
	}
	@Override
	public int loadSimulationFiles() {
		if (state_ == Constants.STATE_NOT_STARTED) {
			if (loadMasterFile() != 0)
				return 1;
		}

		// ��ʼ��SimulationClock,�˴�����ʼʱ�䣬����ʱ��
		init();
		// ��ȡ����xml�����ļ�
		loadInputFiles();

		ic_ = new MesoIncident();
		ic_.init(1, 33000, 39900, 18, -1.0f);

		/*
		 * if (SimulationClock.getInstance().startTime() <
		 * theGuidedRoute.infoStartTime() ||
		 * SimulationClock.getInstance().stopTime() >
		 * theGuidedRoute.infoStopTime()) { //δ���� cerr << endl // <<
		 * "Warning:: Simulation period " // <<
		 * SimulationClock.getInstance()->startTime() << "-" // <<
		 * SimulationClock.getInstance()->stopTime() << endl // <<
		 * "\tnot covered by time horizon of the input file" // << "\t<" <<
		 * theGuidedRoute->filename() << ">." // << endl << endl; }
		 */
		// ��ע��Ϊ�պ���
		start();
		parseODID_ = 1;
		return 0;
	}

	public int loadMasterFile() {
		if (superLoadMasterFile() != 0) {
			// δ���� msg = Str("Error:: Cannot find the master file <%s>.",
			// master());
			// δ���� cerr << msg << endl;
			quit(Constants.STATE_ERROR_QUIT);// STATE_ERROR_QUIT�궨�� quit because
												// of error
			return 1;
		}

		state_ = Constants.STATE_OK;// STATE_OK�궨��,a step is done (clock
									// advanced)
		return 0;
	}

	@Override
	public int simulationLoop() {

		// static double depRecordTime;

		final double epsilon = 1.0E-3;
		// ʵ����·������һ���̶߳�Ӧһ��ʵ��
		MesoNetwork meso_network = MesoNetwork.getInstance();

		double now = SimulationClock.getInstance().getCurrentTime();

		if (first_entry != 0) {
			first_entry = 0;

			// This block is called only once just before the simulation gets
			// started.

			if (tmsConnected != 0) {
				// �����ϵ㹦��
				// resetBreakPoint(now);

				meso_network.resetSegmentEmitTime();
				// �������¼�
				/*
				 * filename = MESO_Incident.filename(); if
				 * (ToolKit.isValidFilename(filename)) { filename =
				 * ToolKit.infile(filename); LoadIncidents(filename); }
				 *
				 * filename = theFileManager.stateDumpFile(); if
				 * (ToolKit.isValidFilename(filename)) { filename =
				 * ToolKit.infile(filename);
				 * meso_network.loadInitialState(filename,1); }
				 */

				if (super.chosenOutput(Constants.OUTPUT_VEHICLE_DEP_RECORDS) != 0) {
					depRecordTime = now;
				}
				else {
					depRecordTime = Constants.DBL_INF;// DBL_INF = MAXDOUBLE /
														// 3.0(Tools/Math.h)
				}
				// ��VehicleTable
				// MESO_Setup.ParseVehicleTables(now);

				rolls_ = 1;
				tm_ = 0;
				if (chosenOutput(Constants.OUTPUT_STATE_3D) != 0) {
					// meso_network.save_3d_state(0, 0,
					// theFileManager.state3d());
					stateTime_ = now + stateStepSize_;
				}
				else {
					stateTime_ = Constants.DBL_INF;
				}

			}
			else {

				// Update of path info is message based in simlab mode
				// һ�㲻�����������
				pathStepSize_ = pathTime_ = Constants.DBL_INF;
			}
		}
		/*
		 * ���̿�ʼ if (now >= SimulationClock.getInstance().getMasterTime()) { //
		 * return (state_ = Constants.STATE_WAITING);////
		 * STATE_WAITING�궨��,waiting for other process }
		 */

		// SAVE GENERATED VEHICLE WHICH CAN BE LOADED IN LATER RUNS
		// ����¼����ʱ��,��������
		if (now >= depRecordTime) {
			// MesoVehicle.nextBlockOfDepartureRecords();
			depRecordTime = now + depRecordStepSize_;
		}
		// ��·������
		// UPDATE SHORTEST PATH FOR GUIDED VEHICLES
		/*
		 * if (now >= pathTime_) {
		 *
		 * // Update info network, calc shortest path, and update travel // time
		 * in path table. Used only in stand alone mode.
		 *
		 * theGuidedRoute.updatePathTable(null, theParameter.pathAlpha());
		 *
		 * // Call path choice or switching model for each guided vehicles. //
		 * This is temporary and assumed that info in broadcased to // every
		 * guided vehicle at the same time. I used it for getting // evaluation
		 * result for the systems that are based on // prevailing traffic
		 * conditions.
		 *
		 * if (isSpFlag(INFO_FLAG_UPDATE_PATHS)) {
		 * meso_network.guidedVehiclesUpdatePaths(); }
		 *
		 * pathTime_ = now + pathStepSize_; }
		 */

		// UPDATE DEMAND
		// �˴�ע�ͣ���ΪtheODTable�ǵ�������ODTable��init�������������˶�cells_��ʵ����
		// if (MESO_ODTable.theODTable!=null) {

		// Update OD trip tables

		if (MesoODTable.getInstance().getNextTime() <= now) {
			// MESO_ODTable.theODTable.read();
			// ����Ӧʱ�ε�OD��Ϣ
			MesoSetup.ParseODTripTables(parseODID_);
			MesoODTable.getInstance().sortODCell();
			parseODID_++;

		}

		// Create vehicles based on trip tables

		MesoODTable.getInstance().emitVehicles();

		/*
		 * if (theVehicleTable) {
		 *
		 * // Read vehicles from a file if it exists.
		 *
		 * theVehicleTable.read(); }
		 */

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

			// Update densities and speeds

			// meso_network.calcSegmentData();

			updateTime_ = now + updateStepSize_;
		}
		// ���¼����ͳ��ʱ��
		if (now > updateDetTime_) {
			meso_network.updateSurvStationMeasureTime();
			updateDetTime_ = updateDetTime_ + detStepSize_;
			meso_network.calcSegmentInfo();
		}
		// δ���� cout << " YYY++++Now" ;
		meso_network.calcTrafficCellUpSpeed();
		meso_network.calcTrafficCellDnSpeeds();

		// ADVANCE PHASE : Update position of TCs, move vehicles from TC to
		// TC and create/destroy TCs as necessary.

		meso_network.advanceVehicles();
		MesoVehicle.increaseCounter();

		if (now >= batchTime_) {
			// theStatus.print();
			batchTime_ = now + batchStepSize_;
		}

		// Report network state

		if (now >= stateTime_) {
			tm_++;
			// meso_network.save_3d_state(rolls_-1, tm_);
			stateTime_ = now + stateStepSize_;
		}
		// ������������г�����λ����Ϣ
		/*
		 * try { MESO_Network.getInstance().outputVhcPosition(); } catch
		 * (IOException e) { // TODO �Զ����ɵ� catch �� e.printStackTrace(); }
		 */
		// Advance the clock

		SimulationClock.getInstance().advance(SimulationClock.getInstance().getStepSize());
		if (now > SimulationClock.getInstance().getStopTime() + epsilon) {
			// HashMap<String, Integer> hm =
			// MESO_InfoArrays.getInstance().getHashMap();
			// int threadid =
			// hm.get(Thread.currentThread().getName()).intValue();
			// MESO_Network.getInstance().outputLinkFlowPlusTravelTimes(threadid);
			// �����淽����������oracle
			/*
			 * MESO_Network.getInstance().outputModelSegmentDataToOracle();
			 * MESO_Network.getInstance().outputTaskSegmentDataToOracle();
			 * MESO_Network.getInstance().outputModelSensorDataToOracle();
			 * MESO_Network.getInstance().outputTaskSensorDataToOracle();
			 */
			organize2DFlow();
			organize2DTraTime();
			return (state_ = Constants.STATE_DONE);// STATE_DONE�궨�� simulation
													// is done
		}
		else
			return state_ = Constants.STATE_OK;// STATE_OK�궨��
	}

	public double updateStepSize() {
		return updateStepSize_;
	}
	public double beginTime() {
		return beginTime_;
	}
	public double endTime() {
		return endTime_;
	}

}
