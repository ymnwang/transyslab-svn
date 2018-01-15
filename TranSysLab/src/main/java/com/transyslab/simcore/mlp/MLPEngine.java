package com.transyslab.simcore.mlp;

import java.io.File;
import java.util.*;

import com.transyslab.commons.io.*;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.csv.CSVRecord;
import java.io.IOException;

import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.simcore.SimulationEngine;


public class MLPEngine extends SimulationEngine{

    public boolean seedFixed;
    public long runningSeed;
    public boolean needEmpData;
	public boolean displayOn = false;
	protected double updateTime_;
	protected double LCDTime_;
	protected double statTime_;
	protected double loadTime;
	private boolean firstEntry; // simulationLoop中第一次循环的标记
	protected boolean needRndETable; //needRndETable==true,随机生成发车表，needRndETable==false，从文件读入发车表
	protected TXTUtils loopRecWriter;
	protected boolean rawRecOn;
	protected TXTUtils trackWriter;
	protected boolean trackOn;
	protected TXTUtils infoWriter;
	protected boolean infoOn;
	protected boolean statRecordOn;
	private HashMap<String, List<MacroCharacter>> empMap;
	private HashMap<String, List<MacroCharacter>> simMap;
	private int mod;//总计运行次数，在输出结束仿真信号时自增

	//引用路网结构
	private MLPNetwork mlpNetwork;

	//引擎运行输入文件配置
	private HashMap<String,String> runProperties;
	//引擎运行时间设置信息
	private double timeStart;
	private double timeEnd;
	private double timeStep;
	//引擎运行参数
	private int repeatTimes;
	private double[] ob_paras;
	private double[] free_paras;

	String rootDir;
	String emitSource;

	private MLPEngine(){
		master_ = null;
		state_ = Constants.STATE_NOT_STARTED;
		mode_ = 0;
		breakPoints_ = null;
		nextBreakPoint_ = 0;

		firstEntry = true;
		mod = 0;

		mlpNetwork = new MLPNetwork();
		runProperties = new HashMap<>();
	}

	public MLPEngine(String masterFilePath) {
		this();
		rootDir = new File(masterFilePath).getParent() + "/";
		parseProperties(masterFilePath);
	}


	private void parseProperties(String configFilePath) {
		Configuration config = ConfigUtils.createConfig(configFilePath);

		//input files
		runProperties.put("roadNetworkPath", rootDir + config.getString("roadNetworkPath"));
		runProperties.put("sensorPath", rootDir + config.getString("sensorPath"));
		runProperties.put("empDataPath", rootDir + config.getString("empDataPath"));
		runProperties.put("outputPath", rootDir + config.getString("outputPath"));

		runProperties.put("emitSourceType", config.getString("emitSourceType"));
		if (runProperties.get("emitSourceType").equals("FILE"))
			runProperties.put("emitSource", rootDir + config.getString("emitSource"));
		else
			runProperties.put("emitSource", config.getString("emitSource"));

		//time setting
		timeStart = Double.parseDouble(config.getString("timeStart"));
		timeEnd = Double.parseDouble(config.getString("timeEnd"));
		timeStep = Double.parseDouble(config.getString("timeStep"));

		//the value will be false if config.getString() returns null
		needRndETable = Boolean.parseBoolean(config.getString("needRndETable"));
		rawRecOn = Boolean.parseBoolean(config.getString("rawRecOn"));
		trackOn = Boolean.parseBoolean(config.getString("trackOn"));
		infoOn = Boolean.parseBoolean(config.getString("infoOn"));
		statRecordOn = Boolean.parseBoolean(config.getString("statRecordOn"));
		seedFixed = Boolean.parseBoolean(config.getString("seedFixed"));
		runningSeed = seedFixed ? Long.parseLong(config.getString("runningSeed")) : 0l;
		needEmpData = Boolean.parseBoolean(config.getString("needEmpData"));

		//Statistic Output setting
		getSimParameter().statWarmUp = Double.parseDouble(config.getString("statWarmUp"));//set time to Parameter
		getSimParameter().statStepSize = Double.parseDouble(config.getString("statTimeStep"));
		//输出变量在loadfiles后再进行初始化，当前只将String读入
		runProperties.put("statLinkIds",config.getString("statLinkIds"));
		runProperties.put("statDetNames",config.getString("statDetNames"));

		//repeatRun setting
		repeatTimes = Integer.parseInt(config.getString("repeatTimes"));
		String[] parasStrArray = config.getString("obParas").split(",");
		ob_paras = new double[parasStrArray.length];
		for (int i = 0; i<parasStrArray.length; i++) {
			ob_paras[i] = Double.parseDouble(parasStrArray[i]);
		}

		//read-in free parameters setting
		String[] freeStrArray = config.getString("freeParas").split(",");
		free_paras = new double[freeStrArray.length];
		for (int i = 0; i<freeStrArray.length; i++) {
			free_paras[i] = Double.parseDouble(freeStrArray[i]);
		}
		//other parameters
		runProperties.put("lcBufferTime",config.getString("lcBufferTime"));
		runProperties.put("lcdStepSize",config.getString("lcdStepSize"));

	}

	@Override
	public int simulationLoop() {
		final double epsilon = 1.0E-3;

		if (firstEntry) {
			// This block is called only once just before the simulation gets started.
			firstEntry = false;
			//确保在Simloop前执行resetBeforeSimloop
			resetBeforeSimLoop();
		}

		double now = mlpNetwork.getSimClock().getCurrentTime();
		double startTime = mlpNetwork.getSimClock().getStartTime();
//		String time = String.format("%.1f", now - startTime);
		
		if (now >= updateTime_){
			mlpNetwork.resetReleaseTime();
			updateTime_ = now + ((MLPParameter) mlpNetwork.getSimParameter()).updateStepSize_;
		}

		if (now >= statTime_){
			double stepSize = ((MLPParameter) mlpNetwork.getSimParameter()).statStepSize;
			mlpNetwork.sectionStatistics(statTime_ - stepSize, now, Constants.HARMONIC_MEAN);//车速使用调和均值
			mlpNetwork.linkStatistics(statTime_ - stepSize, now);
			statTime_ = now + stepSize;
		}

		if (now >= loadTime) {
			double loadTimeStep = 3600 * 12;
			String sourceType = runProperties.get("emitSourceType");
			if (needRndETable) {
				//TODO: 未完成从数据库随机发车
				mlpNetwork.genInflowFromFile(runProperties.get("emitSource"), loadTime + loadTimeStep);
			}
			else {
				if (sourceType.equals("SQL"))
					mlpNetwork.loadInflowFromSQL(runProperties.get("emitSource"), loadTime, loadTime + loadTimeStep);
				else if (sourceType.equals("FILE"))
					mlpNetwork.loadInflowFromFile(runProperties.get("emitSource"), loadTime + loadTimeStep);
			}
			loadTime += loadTimeStep;
//			System.out.println("day: " + now/3600/24 );
		}
		
		//读入发车表
		mlpNetwork.loadEmtTable();
		
		//路网存在车辆的情况下才进行计算
		if (mlpNetwork.veh_list.size()>0) {
			//车队识别
			mlpNetwork.platoonRecognize();
			//若进入换道决策时刻，进行换道计算与车队重新识别
			if (now >= LCDTime_) {
				for (int i = 0; i < mlpNetwork.nLinks(); i++){
					mlpNetwork.mlpLink(i).lanechange();
				}
				mlpNetwork.platoonRecognize();
				LCDTime_ = now + ((MLPParameter) mlpNetwork.getSimParameter()).LCDStepSize_;
			}
			//车辆速度计算
			for (int i = 0; i < mlpNetwork.nLinks(); i++){
				mlpNetwork.mlpLink(i).move();
			}
			//线圈检测
			for (int j = 0; j < mlpNetwork.nSensors(); j++){
				String msg = ((MLPLoop) mlpNetwork.getSensor(j)).detect(now);
				if (rawRecOn) {//按需输出记录
					loopRecWriter.write(msg);
				}
			}
			//车辆状态更新(同时更新)
			for (int k = 0; k<mlpNetwork.veh_list.size(); k++) {
				MLPVehicle theVeh = mlpNetwork.veh_list.get(k);
				if (theVeh.updateMove()==Constants.VEHICLE_RECYCLE)
					k -=1;
			}
			//车辆推进
			for (MLPVehicle vehicle : mlpNetwork.veh_list) {
				vehicle.advance();
			}
			//加载transpose车辆
			for (int i = 0; i < mlpNetwork.nNodes(); i++) {
				mlpNetwork.mlpNode(i).dispatchStatedVeh();
			}

		}
		
		//可视化渲染
		SimulationClock clock = mlpNetwork.getSimClock();
		int tmp = (int) Math.floor(clock.getCurrentTime()*clock.getStepSize());
        if (displayOn) { // && (tmp%10)==0
            mlpNetwork.recordVehicleData();
		}
		
		//输出轨迹
		if (trackOn) {
			if (!mlpNetwork.veh_list.isEmpty() ) {//&& now - 2*Math.floor(now/2) < 0.001
				int LV;
				int FV;
				for (MLPVehicle v : mlpNetwork.veh_list) {
					LV = 0; FV = 0;
					if (v.leading !=null)
						LV = v.leading.getId();
					if (v.trailing != null)
						FV = v.trailing.getId();
					trackWriter.write(
							now + "," +
								v.rvId + "," +
								v.getId() + "," +
								v.virtualType + "," +
								v.buffer + "," +
								v.lane.getLnPosNum() + "," +
								v.segment.getId() + "," +
								v.link.getId() + "," +
								v.Displacement() + "," +
								v.getCurrentSpeed() + "," +
								LV + "," +
								FV + "," +
								/*Thread.currentThread().getName() + "_" + mod + "," +
								LocalDateTime.now() +*/ "\r\n"
					);
				}
			}
		}

//		System.out.println("DEBUG Sim world day: " + (now / 3600.0 / 24 + 1)  );

		clock.advance(clock.getStepSize());
		if (now > clock.getStopTime() + epsilon) {
			if (infoOn)
				infoWriter.writeNFlush("共产生真实车与虚拟车：" + (mlpNetwork.getNewVehID()-1)+"\r\n");
			if (rawRecOn)
				loopRecWriter.closeWriter();
			if (trackOn)
				trackWriter.closeWriter();
			if (infoOn)
				infoWriter.closeWriter();
			if(statRecordOn) {
//				String statFileOut = "src/main/resources/output/loop" + Thread.currentThread().getName() + "_" + mod + ".csv";
//				mlpNetwork.writeStat(statFileOut);
				String outputFileName = runProperties.get("outputPath") + "/LoopRec_" + Thread.currentThread().getName() +"_"+ mod + ".csv";
				mlpNetwork.writeStat(outputFileName);
			}
			//完整运行次数+1，重置firstEntry为真，以便下次再执行SimLoop时可以重置参数。
			mod += 1;
			firstEntry = true;
			return (state_ = Constants.STATE_DONE);// STATE_DONE宏定义 simulation
													// is done
		}
		else {
			/*System.out.println(String.valueOf(now/3600));
			if(Math.abs(now/3600-8)<0.001)
				System.out.println("BUG");*/
			if (rawRecOn)
				loopRecWriter.flushBuffer();
			if (trackOn)
				trackWriter.flushBuffer();
			if (infoOn)
				infoWriter.flushBuffer();
			return state_ = Constants.STATE_OK;// STATE_OK宏定义
		}			
	}

	/**
	 * 读入仿真输入文件并进行引擎初始化
	 */
	@Override
	public void loadFiles() {
		//读入仿真文件
		loadSimulationFiles();
		//读入实测数据用于计算fitness
		if(needEmpData) {
			readEmpData(runProperties.get("empDataPath"));
		}
		//引擎初始化
		initEngine();
	}

	private int loadSimulationFiles(){
		
		//load xml
		//parse xml into parameter & network

		// 读取路网xml
		XmlParser.parseNetwork(mlpNetwork, runProperties.get("roadNetworkPath"));
		// 读入路网数据后组织路网不同要素的关系
		mlpNetwork.calcStaticInfo();
		// 读入检测器数据
		if (!runProperties.get("sensorPath").equals(""))
			XmlParser.parseSensors(mlpNetwork, runProperties.get("sensorPath"));
		// 解释路网输出变量
		mlpNetwork.initLinkStatMap(runProperties.get("statLinkIds"));
		mlpNetwork.initSectionStatMap(runProperties.get("statDetNames"));
		return 0;
	}

	private void readEmpData(String fileName) {
		empMap = new HashMap<>();
		String[] headers = {"NAME","FLOW_RATE","SPEED","DENSITY","TRAVEL_TIME"};
		List<CSVRecord> results = null;
		try {
			 results = CSVUtils.readCSV(fileName, headers);
		} catch (IOException e) {
			e.printStackTrace();
		}

		for(int i=1;i<results.size();i++){
			String detName = results.get(i).get(0);
			double flow = Double.parseDouble(results.get(i).get(1));
			double speed = Double.parseDouble(results.get(i).get(2));
			double density = Double.parseDouble(results.get(i).get(3));
			double travelTime = Double.parseDouble(results.get(i).get(4));
			List<MacroCharacter> records = empMap.get(detName);
			if (records == null) {
				records = new ArrayList<>();
				empMap.put(detName, records);
			}
			records.add(new MacroCharacter(flow,speed,density,travelTime));
		}
		//readFromLoop(MLPSetup.getLoopData_fileName());
	}

	private void initEngine(){
		getSimParameter().setSimStepSize(timeStep);
		SimulationClock clock = mlpNetwork.getSimClock();
		clock.init(timeStart, timeEnd, timeStep);

		//applying
		((MLPParameter) mlpNetwork.getSimParameter()).setLCDStepSize(Double.parseDouble(runProperties.get("lcdStepSize")));
		((MLPParameter) mlpNetwork.getSimParameter()).setLCBuffTime(Double.parseDouble(runProperties.get("lcBufferTime")));

		//establish writers
		String threadName = Thread.currentThread().getName();
		if (rawRecOn) {
			loopRecWriter = new TXTUtils(runProperties.get("outputPath") + "/" + "loop" + threadName + "_" + mod + ".csv");
			loopRecWriter.write("DETNAME,TIME,VID,VIRTYPE,SPD,POS,LINK,LOCATION\r\n");
		}
		if (trackOn) {
			trackWriter = /*new DBWriter("insert into simtrack(time, rvid, vid, virtualIdx, buff, lanePos, segment, link, displacement, speed, lead, trail, tag, create_time) " +
					"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)")*/
							new TXTUtils(runProperties.get("outputPath") + "/" + "track" + threadName + "_" + mod + ".csv");
			trackWriter.write("TIME,RVID,VID,VIRTUALIDX,BUFF,LANEPOS,SEGMENT,LINK,DISPLACEMENT,SPEED,LEAD\r\n");
		}
		if (infoOn)
			infoWriter = new TXTUtils(runProperties.get("outputPath") + "/" + "info" + threadName + "_" + mod + ".txt");
		//信息统计：发车数
		if (infoOn) {
			int total = 0;
			for (int i = 0; i < mlpNetwork.nLinks(); i++) {
				List<Inflow> IFList = mlpNetwork.mlpLink(i).getInflow();
				int tmp = IFList.size();
				total += tmp;
				for (int j = 0; j < tmp; j++)
					infoWriter.write(IFList.get(j).time + ",1\r\n");
			}
			infoWriter.writeNFlush("随机发出真实车： " + total + "\r\n");
		}
	}

	/**
	 * 重置引擎时钟 时间相关的参数 与路网状态
	 * 注意：若要设置种子、发车等于路网状态重设过程相关的参数，需要在执行此函数前进行。
	 */
	private void resetBeforeSimLoop() {
		SimulationClock clock = mlpNetwork.getSimClock();
		clock.resetTimer();
		double now = clock.getCurrentTime();
		updateTime_ = now;
		LCDTime_ = now;
		statTime_ = now + getSimParameter().statWarmUp + getSimParameter().statStepSize; //第一次统计时刻为：现在时间+warmUp+统计间隔
		loadTime = now;

		//Network状态重设并准备发车表
		if (!seedFixed)
			runningSeed = System.currentTimeMillis();
		mlpNetwork.resetNetwork(runningSeed);
	}

	public void forceResetEngine() {
		firstEntry = true;
		resetBeforeSimLoop();
	}

	/**
	 * 设置观测参数
	 * @param ob_paras [0]Qm, [1]Vfree, [2]Kjam, [3]VPhyLim
	 */
	private void setObservedParas (double [] ob_paras){
		if (ob_paras.length != 4) {
			System.err.println("ob_paras' length does not match");
			return;
		}

		//解释
		double Qm = ob_paras[0];
		double Vfree = ob_paras[1];
		double Kjam = ob_paras[2];
		double VPhyLim = ob_paras[3];

		//检测运行参数设置
		if (getSimClock().getStepSize() >= MLPParameter.deltaTUpper(Vfree, Kjam, Qm)) {
			System.err.println("step size of simClock is too big.");
			return;
		}
		if (!MLPParameter.isVpFastEnough(Vfree, VPhyLim)) {
			System.err.println("VPhyLim should be bigger than VFree");
			return;
		}

		MLPParameter allParas = (MLPParameter) mlpNetwork.getSimParameter();

		//设置通行能力，包括parameter中的capacity
		allParas.capacity = Qm;
		mlpNetwork.setOverallCapacity(Qm);//路段单车道每秒通行能力

		//路段速密函数
		int mask = 0;
		mask |= 1<<(1-1);//Vmax
		mask |= 1<<(3-1);//Kjam
		mlpNetwork.setOverallSDParas(new double[] {Vfree, Kjam}, mask);

		//根据Kjam 保持 leff 与 CF_near 的一致性
		allParas.limitingParam_[0] = (float) (1.0/Kjam - MLPParameter.VEHICLE_LENGTH);
		allParas.CF_NEAR = allParas.limitingParam_[0];//锁定与kjam吻合

		//设置物理限速
		allParas.setPhyLim(VPhyLim);
	}

	/**
	 * 设置非观测参数，内部调用
	 * 参数间存在依赖，不是完全独立的。详见@setParas
	 * @param optparas [0]ts, [1]xc, [2]alpha, [3]beta, [4]gamma1, [5]gamma2.
	 */
	private void setOptParas(double [] optparas) {
		if (optparas.length != 6) {
			System.err.println("length does not match");
			return;
		}
		MLPParameter allParas = (MLPParameter) mlpNetwork.getSimParameter();
		allParas.limitingParam_[1] = (float) optparas[0];//ts
		allParas.CF_FAR = (float) optparas[1];//xc
		int mask = 0;
		mask |= 1<<(4-1);
		mask |= 1<<(5-1);
		mlpNetwork.setOverallSDParas(new double[] {optparas[2], optparas[3]}, mask);//alpha, beta
		allParas.setLCPara(new double[] {optparas[4], optparas[5]});//gamma1 gamma2
	}

	/**
	 * 设置全体运动参数
	 * @param ob_paras [0]Qm, [1]Vfree, [2]Kjam, [3]VPhyLim
	 * @param varying_paras 自由参数 [0]Xc, [1]r(i.e. alpha*beta), [2]gamma1, [3]gamma2.
	 */
	private void setParas(double[] ob_paras, double[] varying_paras) {
		//长度检查
		if (ob_paras.length != 4 || varying_paras.length != 4) {
			System.err.println("parameters' length does not match");
			return;
		}

		//解释观测参数
		double Qm = ob_paras[0];
		double Vfree = ob_paras[1];
		double Kjam = ob_paras[2];
		double VPhyLim = ob_paras[3];
		//解释自由参数
		double Xc = varying_paras[0];
		double r = varying_paras[1];
		double gamma1 = varying_paras[2];
		double gamma2 = varying_paras[3];

		setObservedParas(ob_paras);

		//检测Xc取值
		if (Xc <= MLPParameter.xcLower(Kjam, Qm, getSimClock().getStepSize())) {
			System.err.println("Xc out of lower boundary");
			return;
		}

		//读取deltaT
		double deltaT = getSimClock().getStepSize();

		//计算安全车头时距
		double ts = MLPParameter.calcTs(Xc, Vfree, Kjam, Qm, VPhyLim, deltaT);

		/*弃用*/
		/*//检查r取值
		if (r >= MLPParameter.rUpper(10, Vfree, Kjam, Qm)) {
			System.err.println("check r constraints");
			return;
		}*/

		//计算alpha, beta
		double alpha = MLPParameter.calcAlpha(r, Vfree, Kjam, Qm);
		double beta = r / alpha;

		//组织opt_paras
		//[0]ts, [1]xc, [2]alpha, [3]beta, [4]gamma1, [5]gamma2.
		double[] opt_paras = new double[] {ts, Xc, alpha, beta, gamma1, gamma2};

		setOptParas(opt_paras);
	}

	/**
	 * 设置全体运动参数
	 * @param fullParas [0]Qm, [1]VFree, [2]KJam, [3]VPhyLim, [4]Xc, [5]r(i.e. alpha*beta), [6]gamma1, [7]gamma2.
	 */
	private void setParas(double[] fullParas) {
		if (fullParas.length != 8) {
			System.err.println("length does not match");
			return;
		}
		double[] ob = new double[4];
		double[] varying = new double[4];
		System.arraycopy(fullParas,0,ob,0,4);
		System.arraycopy(fullParas,4,varying,0,4);
		setParas(ob,varying);
	}

	/**
	 * 检查约束
	 * @param fullParas [0]Qm, [1]VFree, [2]KJam, [3]VPhyLim, [4]Xc, [5]r(i.e. alpha*beta), [6]gamma1, [7]gamma2.
	 * @return 是否违反约束
	 */
	protected boolean violateConstraints(double[] fullParas) {
		return violateConstraints(Arrays.copyOfRange(fullParas,0,4),
				                  Arrays.copyOfRange(fullParas,4,8));
	}

	/**
	 * 检查约束
	 * @param obsParas [0]Qm, [1]Vfree, [2]Kjam, [3]VPhyLim
	 * @param varyingParas 自由参数 [0]Xc, [1]r(i.e. alpha*beta), [2]gamma1, [3]gamma2.
	 * @return 是否违反约束
	 */
	protected boolean violateConstraints(double[] obsParas, double[]varyingParas) {
		//解释观测参数
		double Qm = obsParas[0];
		double Vfree = obsParas[1];
		double Kjam = obsParas[2];
		double VPhyLim = obsParas[3];
		//解释自由参数
		double Xc = varyingParas[0];
		double r = varyingParas[1];
		double gamma1 = varyingParas[2];
		double gamma2 = varyingParas[3];
		//读取deltaT
		double deltaT = getSimClock().getStepSize();
		//计算ts
		double ts = MLPParameter.calcTs(Xc, Vfree, Kjam, Qm, VPhyLim, deltaT);
		return !MLPParameter.isVpFastEnough(Vfree, VPhyLim) ||
				Xc <= MLPParameter.xcLower(Kjam, Qm, deltaT) ||
				deltaT >= MLPParameter.deltaTUpper(Vfree, Kjam, Qm);
	}

	public MLPParameter getSimParameter() {
		return (MLPParameter) mlpNetwork.getSimParameter();
	}

	public SimulationClock getSimClock() {
		return mlpNetwork.getSimClock();
	}

	@Override
	public MLPNetwork getNetwork() {
		return mlpNetwork;
	}

	/**
	 * 返回det2的车速序列
	 * @return
	 */
	//TODO: 待删除 目前为了将就旧代码保留
	public double[] getEmpData() {
		return MacroCharacter.getKmSpeed(
				MacroCharacter.select(empMap.get("det2"),MacroCharacter.SELECT_SPEED));
	}

	public HashMap<String, List<MacroCharacter>> getEmpMap(){
		return empMap;
	}

	@Override
	public HashMap<String, List<MacroCharacter>> getSimMap() {
		return simMap;
	}

	public int runWithPara(double[] fullParas) {
		if (violateConstraints(fullParas))
			return Constants.STATE_ERROR_QUIT;
		setParas(fullParas);
		int state;
		do {
			state = simulationLoop();
		}
		while (state >= 0);
		return state;
	}

	public int runWithPara(double[] obParas, double[]varParas) {
		if (violateConstraints(obParas,varParas))
			return Constants.STATE_ERROR_QUIT;
		setParas(obParas,varParas);
		int state;
		do {
			state = simulationLoop();
		}
		while (state >= 0);
		return state;
	}

	@Override
	public int repeatRun() {

		//simMap置空，重置状态。避免重复执行repeatRun()时出错。
		simMap = null;

		if (violateConstraints(ob_paras,free_paras))
			return Constants.STATE_ERROR_QUIT;

		setParas(ob_paras,free_paras);

		//若有重复仿真任务，则多次运行进行平均
		for(int i = 0; i < Math.max(repeatTimes,1); i++){
			int state;
			do {
				state = simulationLoop();
			}
			while (state>=0);
			if (state == Constants.STATE_DONE) {
				sumStat(mlpNetwork.exportStat());
			}
			else
				return state;

		}

		//统计数据平均
		if (repeatTimes > 1)
			averageStat(simMap);

		return Constants.STATE_DONE;
	}

	/**
	 * 应该只在MainWindow中调用，仅用于可视化debug.
	 */
	@Override
	public void run() {
		setParas(ob_paras,free_paras);
		super.run();
	}

	@Override
	public void close() {
		//关闭数据库连接
		JdbcUtils.close();
	}

	public int countOnHoldVeh() {
		int vehHoldCount = 0;
		for (int k = 0; k<mlpNetwork.nLinks(); k++) {
			vehHoldCount += ((MLPLink) mlpNetwork.getLink(k)).countHoldingInflow();
		}
		return vehHoldCount;
	}

	private void sumStat(Map<String, List<MacroCharacter>> addingMap) {
		if (simMap == null) {
			simMap = new HashMap<>();
			addingMap.forEach((k,v) -> {
				List<MacroCharacter> newList = new ArrayList<>();
				v.stream().forEach(l -> newList.add(l.copy()));
				simMap.put(k,newList);
			});
		}
		Map<String, List<MacroCharacter>> srcMap = this.simMap;
		srcMap.forEach((k,v) -> {
			List<MacroCharacter> addingRecords = addingMap.get(k);
			for (int i = 0; i < v.size(); i++) {
				MacroCharacter srcRecord = v.get(i);
				MacroCharacter addingRecord = addingRecords.get(i);
				if (srcRecord.flow>0.0) {
					//平均车速
					srcRecord.speed = (srcRecord.flow*srcRecord.speed + addingRecord.flow*addingRecord.speed) / (srcRecord.flow + addingRecord.flow);
					//行程时间平均
					srcRecord.travelTime = (srcRecord.flow*srcRecord.travelTime + addingRecord.flow*addingRecord.travelTime) / (srcRecord.flow + addingRecord.flow);
					//流量累加
					srcRecord.flow += addingRecord.flow;
					//密度累加
					srcRecord.density = srcRecord.flow / srcRecord.speed;
				}
			}
		});
	}

	private void averageStat(Map<String, List<MacroCharacter>> srcMap) {
		srcMap.forEach((k,v) -> v.forEach(e -> {
			e.flow = e.flow / ((double) repeatTimes);
			e.density = e.density / ((double) repeatTimes);
		}));
	}

	public int countRunTimes(){
		return mod;
	}

	public MLPEngine alterEngineObParas(double[] args) {
		ob_paras = args;
		return this;
	}

	public MLPEngine alterEngineFreeParas(double[] args) {
		free_paras = args;
		return this;
	}

	public void modifyEmitSource(String sourceName) {
		String sourceType = runProperties.get("emitSourceType");
		if (sourceType.equals("FILE"))
			this.emitSource = this.rootDir + sourceName;
		else if (sourceType.equals("SQL"))
			this.emitSource = sourceName;
	}

}
