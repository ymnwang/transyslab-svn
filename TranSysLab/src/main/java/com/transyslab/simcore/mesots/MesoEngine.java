/**
 *
 */
package com.transyslab.simcore.mesots;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.csv.CSVRecord;

import com.transyslab.commons.io.CSVUtils;
import com.transyslab.commons.renderer.JOGLFrameQueue;
import com.transyslab.commons.tools.DE;
import com.transyslab.commons.tools.PSO;
import com.transyslab.commons.tools.Random;
import com.transyslab.commons.tools.SPSA;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.LinkTimes;
import com.transyslab.roadnetwork.SurvStation;
import com.transyslab.roadnetwork.VehicleData;
import com.transyslab.roadnetwork.VehicleDataPool;
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
	private SPSA spsa_;
	private float tempBestFitness_;
	private float[] tempBest_;
	public float alpha_;
	public float Beta_;
	private List<MesoVehicle> snapshotList_;//初始帧的在网车辆列表
	private int vhcTableIndex_;
	private int mode_;// =0:非snapshot启动，按OD流量随机发车；
	                  // =1:非snapshot启动，按过车记录定时发车;
					  // =2:snapshot启动，按OD流量随机发车；
	                  // =3:snapshot启动，按过车记录定时发车；
	private List<Float> LPB9_ =  new ArrayList<>();
	private List<Float> VDB7_ = new ArrayList<>();
	private List<Float> LPB11_ = new ArrayList<>();
	private List<Float> LPA24_ = new ArrayList<>();
	private float objFunction_;
	private float[] parameter_;
	private List<CSVRecord> vhcData_;
	private int vhcDataID_;
		
	public MesoEngine(int mode) {
		//定义仿真运行模式
		mode_ = mode;
		vhcTableIndex_ = 0;
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
//		SimulationClock.getInstance().init(28800.0, 32400.0, 1.0);

	}
	public float getObjFunction(){
		return objFunction_;
	}
	public float[] getParameters(){
		return parameter_;
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
		SimulationClock.getInstance().init(0*3600,2*3600, 0.2);

		double now = SimulationClock.getInstance().getCurrentTime();
		batchTime_ = now;
		updateTime_ = now;
		//待改
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
	public void initSPSA(SPSA spsa){
		spsa_ = spsa;
		parameter_ = new float[spsa_.getDimention()];
		//参数赋初值
		spsa_ .getInverseNomal(parameter_);
	}
	public void resetBeforeSimLoop() {
		firstEntry = 1;
		SimulationClock.getInstance().init(0*3600,2*3600, 0.2);
		double now = SimulationClock.getInstance().getCurrentTime();
		batchTime_ = now;
		updateTime_ = now;
		//重置检测器
		updateDetTime_ = now + detStepSize_;
		
		pathTime_ = now + pathStepSize_;
		stateTime_ = now;
		frequency_ = (float) (1.0 / SimulationClock.getInstance().getStepSize());
		MesoNetwork.getInstance().resetLinkStatistics();
		MesoNetwork.getInstance().clean();
		parseODID_ = 1;
		MesoODTable.getInstance().setNextTime(0);
		//重置种子
		Random.getInstance().get(Random.Misc).resetSeed();
		Random.getInstance().get(Random.Departure).resetSeed();
		Random.getInstance().get(Random.Routing).resetSeed();
		if(mode_ == 1){
			vhcTableIndex_ =0;
			MesoVehicleTable.getInstance().getVhcList().clear();
			MesoSetup.ParseVehicleTable();
		}
			
	}
	// 读入所有输入文件，包含MasterFile（仿真配置文件）和SimulationFile（仿真参数文件）
	@Override
	public void loadFiles() {
		if (canStart() > 0) {
			loadMasterFile();
		}
		
		loadSimulationFiles();


		List<CSVRecord> records;
		int i=0;
		try {
			// E:\\MesoInput_Cases\\12.25goodluck\\input1.csv
			// E:\\MesoInput_Cases\\12.28bestluck\\input1.csv
			records = CSVUtils.readCSV("E:\\MesoInput_Cases\\12.28bestluck\\input1.csv", null);
			
			for(CSVRecord record : records){
				for(int j=0;j<record.size();j++){
					if(i==0)
						LPA24_.add(Float.parseFloat(record.get(j)));
//					LPB9_.add(Float.parseFloat(record.get(j)));
					else if (i==1)
						VDB7_.add(Float.parseFloat(record.get(j)));
						else
							LPB11_.add(Float.parseFloat(record.get(j)));
				}
				i++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			vhcData_ = CSVUtils.readCSV("src/main/resources/demo_pre/newdata.csv", null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	// 多次运行
	public void run(int mode) {
		if(mode==0){
//			MesoNetwork.getInstance().updateParaSdfns(0.15f,0.0f, 21.95f, 156.25f,1.61f,6.31f);
			while (simulationLoop() >= 0) {

			}
		}
		else if (mode == 1) {
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
				MesoNetwork.getInstance().updateParaSdfns(0.5f, 0.0f, 16.67f, 180.0f, de_.getNewPosition(i)[0], de_.getNewPosition(i)[1]);
//				MesoNetwork.getInstance().updateSdFns(de_.getNewPosition(i)[2], de_.getNewPosition(i)[3]);
//				MesoParameter.getInstance().setRspLower(de_.getNewPosition(i)[0]);
//				MesoParameter.getInstance().setRspUpper(de_.getNewPosition(i)[1]);
//				MesoParameter.getInstance().updateCSG();
				while (simulationLoop() >= 0) {
				}
//				de_.getNewIdvds()[i].evaMRE(simFlow_, simTraTime_, realFlow_, realTraTime_, 0);
				evaRMSN();
				de_.getNewIdvds()[i].setFitness(objFunction_);
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
		else if(mode ==3){

			MesoNetwork mesonetwork = MesoNetwork.getInstance();
			//0.45f,0.0f, 21.95f, 156.25f,1.61f,6.31f
			//2.2822566,5.56166,154.72292,19.469088,32.80778,91.904686
			//扰动六个参数
//			MesoNetwork.getInstance().updateParaSdfns(0.45f,0.0f, parameter_[3], parameter_[2],parameter_[0],parameter_[1]);
			
//			MesoNetwork.getInstance().updateParaSdfns(0.45f,0.0f, 17.69f,183.95f,2.91f,0.65f);
			//扰动四个参数
//			MesoNetwork.getInstance().updateParaSdfns(0.45f,0.0f, 21.95f,156.25f,1.61f,8.11f);// parameter_[0],parameter_[1]);
			MesoParameter.getInstance().setRspLower(30.57f);//parameter_[4]);
			MesoParameter.getInstance().setRspUpper(91.79f);//parameter_[5]);
			MesoParameter.getInstance().updateCSG();
			//人工合成数据
//			mesonetwork.updateParaSdfns(0.5f,0.0f, 16.67f, 180.0f,parameter_[0],parameter_[1]);
			mesonetwork.updateParaSdfns(0.45f,0.0f, 19.76f, 156.21f,2.0f,5.35f);
			mesonetwork.updateSegFreeSpeed();
			while (simulationLoop() >= 0) {
			}
//			evaMRE();
			evaRMSN();
			resetBeforeSimLoop();
		}
		else if(mode == 4){
			int tmpframeid =-1;
			for(CSVRecord record: vhcData_){
				vhcDataID_ = Integer.parseInt(record.get(0));
/*				if(tmpframeid!=vhcDataID_){
					tmpframeid = vhcDataID_;
				}*/				
				//从对象池获取vehicledata对象
				VehicleData vd = VehicleDataPool.getVehicleDataPool().getVehicleData();
				//记录车辆信息
				vd.init(Integer.parseInt(record.get(1)),Float.parseFloat(record.get(2)));
				//将vehicledata插入frame
				try {
					JOGLFrameQueue.getInstance().offer(vd, Integer.parseInt(record.get(3)));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	/*
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

	}*/
	public void evaMRE(/*float w*/) {
/*		int col = simFlow_[0].length;
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
		return (float) (sumError / col);	*/
		MesoNetwork meso_network = MesoNetwork.getInstance();
		List<SurvStation> survstations = meso_network.getSurvStations();
		List<Float> vList;
		float head=0,tail=0;
		float head1=0,tail1=0;
		for(int i=0;i<survstations.size();i++){
			if(survstations.get(i).getCode()==7){
				vList = LPA24_;
				for(int j=0;j<vList.size();j++){
					head1 += Math.abs(vList.get(j)-survstations.get(i).getSpeedList().get(j+3))/vList.get(j);
				}
			}
				
			
		}
		objFunction_ = head1/LPA24_.size();		
		
	}
	public void evaRMSN(){
		MesoNetwork meso_network = MesoNetwork.getInstance();
		List<SurvStation> survstations = meso_network.getSurvStations();
		List<Float> vList;
		float head=0,tail=0;
		float head1=0,tail1=0;
		/*人工数据
		for(int i=0;i<survstations.size();i++){
			if(survstations.get(i).getCode()==-9)
				vList = LPB9_;
				else if(survstations.get(i).getCode()==7)
					vList = VDB7_;
					else
						vList = LPB11_;
			head1 = 0;
			tail1 = 0;
			for(int j=0;j<survstations.get(i).getSpeedList().size();j++){
				head1 += (float) Math.pow(vList.get(j)-survstations.get(i).getSpeedList().get(j),2);
				tail1 += vList.get(j);
			}
			head += head1;
			tail += tail1;
		}*/
		for(int i=0;i<survstations.size();i++){
			if(survstations.get(i).getCode()==7){
				vList = LPA24_;
				for(int j=0;j<vList.size();j++){
					head1 += (float) Math.pow(vList.get(j)-survstations.get(i).getSpeedList().get(j+3),2);
					tail1 += vList.get(j);
				}
				head += head1;
				tail += tail1;
			}
		}
		objFunction_ = (float) (Math.sqrt(head*20))/tail;
			
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
	  public void initSnapshotData(){
		  int vhcnum = snapshotList_.size();
		  //在网车辆数统计
		  MesoVehicle.setVehicleCounter(vhcnum);;
		  MesoSegment seg = (MesoSegment) MesoNetwork.getInstance().getSegment(0);
		  seg.append(MesoCellList.getInstance().recycle());
		  seg.getLastCell().initialize();
		  for(int i=0;i<vhcnum-1;i++){
			  if(snapshotList_.get(i+1).distance()-snapshotList_.get(i).distance()<MesoParameter.getInstance().cellSplitGap()){
				  seg.getLastCell().appendSnapshot(snapshotList_.get(i));
				  if(i==vhcnum-2){
					  //最后一辆车
					  seg.getLastCell().appendSnapshot(snapshotList_.get(i+1));
				  }
			  }
			  else{
				  seg.append(MesoCellList.getInstance().recycle());
				  seg.getLastCell().initialize();
				  seg.getLastCell().appendSnapshot(snapshotList_.get(i+1));
			  }
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
		
		if(mode_ == 2||mode_ ==3){//从SnapShot启动仿真
 
			//解析已在路网上的车辆列表
			  snapshotList_ = new ArrayList<MesoVehicle>();
			  MesoSetup.ParseSnapshotList(snapshotList_);
		}
		if(mode_ == 1||mode_ ==3){
			 //解析车辆表
			  MesoSetup.ParseVehicleTable(); 
		}
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
			if(mode_==2||mode_==3){
				//初始化路网已有车辆的所有信息
				initSnapshotData();
			}

		}

		// SAVE GENERATED VEHICLE WHICH CAN BE LOADED IN LATER RUNS
		// 记录发车时间
		if (now >= depRecordTime_) {
			// MesoVehicle.nextBlockOfDepartureRecords();
			depRecordTime_ = now + depRecordStepSize_;
		}
		//按OD流量随机发车
		if(mode_ ==0||mode_==2){
			
			// Update OD trip tables
			MesoODTable odtable = MesoODTable.getInstance();
			if (MesoODTable.getInstance().getNextTime() <= now) {
				// MESO_ODTable.theODTable.read();
				// 读对应时段的OD信息
				MesoSetup.ParseODTripTables(parseODID_);
				MesoODTable.getInstance().sortODCell();
				parseODID_++;

			}

			// Create vehicles based on trip tables

			MesoODTable.getInstance().emitVehicles();
		}
		else if(mode_ == 1|| mode_==3){
			//按过车记录定时发车
			MesoVehicleTable tmptable = MesoVehicleTable.getInstance();
			while(vhcTableIndex_<MesoVehicleTable.getInstance().getVhcList().size() 
					&& MesoVehicleTable.getInstance().getVhcList().get(vhcTableIndex_).departTime()<=now){
				  MesoVehicleTable.getInstance().getVhcList().get(vhcTableIndex_).enterPretripQueue();
				  vhcTableIndex_++;
			 }
		}
		else{
			//error, 请定义发车模式
		}

		// ENTER VEHICLES INTO THE NETWORK

		// Move vehicles from vitual queue into the network if they could
		// enter the network at present time.
//		MesoVehicleTable tmptable = MesoVehicleTable.getInstance();
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
			//输出路段统计量
//			meso_network.calcSegmentInfo();
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

		//当前帧在网车辆的位置信息存储到framequeue
		
		if(MesoVehicle.nVehicles()!=0 && mode_==1)
			meso_network.recordVehicleData();
		
		
		// 输出步长内所有车辆的位置信息
		/*
		  try { MesoNetwork.getInstance().outputVhcPosition(); 
		  } catch(IOException e) { 
			  // TODO 自动生成的 catch 块 
			  e.printStackTrace(); }*/
		 
		// Advance the clock
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
			//输出检测器检测记录
			/*
			  try {
				MESO_Network.getInstance().outputSectionRecord();
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			organize2DFlow();
			organize2DTraTime();*/

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
