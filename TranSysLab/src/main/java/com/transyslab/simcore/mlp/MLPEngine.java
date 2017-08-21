package com.transyslab.simcore.mlp;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.transyslab.commons.io.XmlParser;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.csv.CSVRecord;
import java.io.IOException;

import com.transyslab.commons.io.CSVUtils;
import com.transyslab.commons.io.TXTUtils;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.simcore.SimulationEngine;


public class MLPEngine extends SimulationEngine{

    public boolean seedFixed;
    public long runningSeed;
    public boolean needEmpData;
	protected double updateTime_;
	protected double LCDTime_;
	protected double statTime_;
	protected boolean firstEntry; // simulationLoop�е�һ��ѭ���ı��
	protected boolean needRndETable; //needRndETable==true,������ɷ�����needRndETable==false�����ļ����뷢����
	protected TXTUtils loopRecWriter;
	protected boolean loopRecOn;
	protected TXTUtils trackWriter;
	protected boolean trackOn;
	protected TXTUtils infoWriter;
	protected boolean infoOn;
	protected boolean statRecordOn;
	protected String msg;
	private Object empData;
	private int mod;//�ܼ����д�������������������ź�ʱ����
	private boolean displayOn;

	//����·���ṹ
	private MLPNetwork mlpNetwork;

	//�������������ļ�����
	private HashMap<String,String> runProperties;
	//��������ʱ��������Ϣ
	double timeStart;
	double timeEnd;
	double timeStep;

	
	public MLPEngine() {
		super();
		firstEntry = true;
		needRndETable = false;
		loopRecOn = false;
		trackOn = false;
		infoOn = false;
		statRecordOn = false;
		msg = "";
		seedFixed = false;
		runningSeed = 0l;
		needEmpData = false;
		mod = 0;
		displayOn = false;
//		fitnessVal = Double.POSITIVE_INFINITY;

		mlpNetwork = new MLPNetwork();

		runProperties = new HashMap<>();

		timeStart = 0.0;
		timeEnd = 0.0;
		timeStep = 0.0;
	}

	public MLPEngine(String masterFileDir) {
		//super()
		master_ = null;
		state_ = Constants.STATE_NOT_STARTED;
		mode_ = 0;
		breakPoints_ = null;
		nextBreakPoint_ = 0;

		//MLPEngine() blocked fields are initialized in parseProperties
		firstEntry = true;
//		needRndETable = false;
//		loopRecOn = false;
//		trackOn = false;
//		infoOn = false;
//		statRecordOn = false;
		msg = "";
//		seedFixed = false;
//		runningSeed = 0l;
//		needEmpData = false;
		mod = 0;
//		displayOn = false;
		mlpNetwork = new MLPNetwork();
		runProperties = new HashMap<>();

		parseProperties(masterFileDir);

//		timeStart = 0.0;
//		timeEnd = 0.0;
//		timeStep = 0.0;
	}

	public void parseProperties(String fileDir) {
		File testFile = new File(fileDir);
		Configurations configs = new Configurations();
		Configuration config = null;
		try {
			config = configs.properties(testFile);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

		//input files
		runProperties.put("roadNetworkDir", config.getString("roadNetworkDir"));
		runProperties.put("emitFileDir", config.getString("emitFileDir"));
		runProperties.put("odFileDir", config.getString("odFileDir"));
		runProperties.put("sensorDir", config.getString("sensorDir"));
		runProperties.put("empDataDir", config.getString("empDataDir"));

		//time setting
		timeStart = Double.parseDouble(config.getString("timeStart"));
		timeEnd = Double.parseDouble(config.getString("timeEnd"));
		timeStep = Double.parseDouble(config.getString("timeStep"));

		//the value will be false if config.getString() returns null
		needRndETable = Boolean.parseBoolean(config.getString("needRndETable"));
		loopRecOn = Boolean.parseBoolean(config.getString("loopRecOn"));
		trackOn = Boolean.parseBoolean(config.getString("trackOn"));
		infoOn = Boolean.parseBoolean(config.getString("infoOn"));
		statRecordOn = Boolean.parseBoolean(config.getString("statRecordOn"));
		seedFixed = Boolean.parseBoolean(config.getString("seedFixed"));
		runningSeed = seedFixed ? Long.parseLong(config.getString("runningSeed")) : 0l;
		needEmpData = Boolean.parseBoolean(config.getString("needEmpData"));
		displayOn = Boolean.parseBoolean(config.getString("displayOn"));

		//Statistic Output setting
		getSimParameter().statWarmUp = Double.parseDouble(config.getString("statWarmUp"));//set time to Parameter
		getSimParameter().statStepSize = Double.parseDouble(config.getString("statTimeStep"));
		//���������loadfiles���ٽ��г�ʼ������ǰֻ��String����
		runProperties.put("statLinkIds",config.getString("statLinkIds"));
		runProperties.put("statDetNames",config.getString("statDetNames"));
	}

	@Override
	public int simulationLoop() {
		final double epsilon = 1.0E-3;

		double now = mlpNetwork.getSimClock().getCurrentTime();
		double startTime = mlpNetwork.getSimClock().getStartTime();
		String time = String.format("%.1f", now - startTime);

		if (firstEntry) {
			// This block is called only once just before the simulation gets
			// started.
			firstEntry = false;
			//Network״̬���貢׼��������
			if (!seedFixed)
				runningSeed = System.currentTimeMillis();
			mlpNetwork.resetNetwork(needRndETable, runProperties.get("odFileDir"), runProperties.get("emitFileDir"), runningSeed);
			
			//reset update time
			mlpNetwork.resetReleaseTime();
			
			//establish writers
			String threadName = Thread.currentThread().getName();
			if (loopRecOn) {
				loopRecWriter = new TXTUtils("src/main/resources/output/loop" + threadName + "_" + mod + ".csv");
				loopRecWriter.write("TIME,VID,VIRTYPE,SPD,POS,LINK,LOCATION\r\n");
			}				
			if (trackOn) {
				trackWriter = new TXTUtils("src/main/resources/output/track" + threadName + "_" + mod + ".csv");
				trackWriter.write("TIME,rvId,VID,VIRTYPE,BUFF,POS,SEG,LINK,DSP,SPD,LEAD,TRAIL\r\n");
			}				
			if (infoOn)
				infoWriter = new TXTUtils("src/main/resources/output/info" + threadName + "_" + mod + ".txt");
			//��Ϣͳ�ƣ�������
			if (infoOn) {
				int total = 0;
				for (int i = 0; i < mlpNetwork.nLinks(); i++) {
					List<Inflow> IFList = mlpNetwork.mlpLink(i).getInflow();
					int tmp = IFList.size();
					total += tmp;
					for (int j = 0; j < tmp; j++)
						infoWriter.write(IFList.get(j).time + ",1\r\n");
				}
				infoWriter.writeNFlush("���������ʵ���� " + total + "\r\n");	
			}		
		}
		
		if (now >= updateTime_){
			mlpNetwork.resetReleaseTime();
			if (loopRecOn) 
				loopRecWriter.flushBuffer();
			if (trackOn)
				trackWriter.flushBuffer();
			if (infoOn)
				infoWriter.flushBuffer();
			updateTime_ = now + ((MLPParameter) mlpNetwork.getSimParameter()).updateStepSize_;
		}

		if (now >= statTime_){
			double stepSize = ((MLPParameter) mlpNetwork.getSimParameter()).statStepSize;
			mlpNetwork.sectionStatistics(statTime_ - stepSize, now, Constants.ARITHMETIC_MEAN);//TODO: change
			mlpNetwork.linkStatistics(statTime_ - stepSize, now);
			statTime_ = now + stepSize;
		}
		
		//���뷢����
		mlpNetwork.loadEmtTable();
		
		//·�����ڳ���������²Ž��м���
		if (mlpNetwork.veh_list.size()>0) {
			//����ʶ��
			mlpNetwork.platoonRecognize();
			//�����뻻������ʱ�̣����л��������복������ʶ��
			if (now >= LCDTime_) {
				for (int i = 0; i < mlpNetwork.nLinks(); i++){
					mlpNetwork.mlpLink(i).lanechange();
				}
				mlpNetwork.platoonRecognize();
				LCDTime_ = now + ((MLPParameter) mlpNetwork.getSimParameter()).LCDStepSize_;
			}
			//�˶����㡢�ڵ����(��ȱ)��д�뷢������ȱ��
			//�����ٶȼ���
			for (int i = 0; i < mlpNetwork.nLinks(); i++){
				mlpNetwork.mlpLink(i).move();
			}
			//����״̬����(ͬʱ����)
			for (int k = 0; k<mlpNetwork.veh_list.size(); k++) {
				MLPVehicle theVeh = mlpNetwork.veh_list.get(k);
				if (theVeh.updateMove()==1) 
					k -=1;
			}
			//��Ȧ���
			for (int j = 0; j < mlpNetwork.nSensors(); j++){
				msg = ((MLPLoop) mlpNetwork.getSensor(j)).detect(now);
				if (loopRecOn) {//���������¼
					loopRecWriter.write(msg);
				}
			}
			//�����ƽ�
			for (MLPVehicle vehicle : mlpNetwork.veh_list) {
				vehicle.advance();
			}
			//����transpose����
			for (int i = 0; i < mlpNetwork.nNodes(); i++) {
				mlpNetwork.mlpNode(i).dispatchStatedVeh();
			}

		}
		
		//���ӻ���Ⱦ
		SimulationClock clock = mlpNetwork.getSimClock();
		int tmp = (int) Math.floor(clock.getCurrentTime()*clock.getStepSize());
        if (displayOn && (tmp%10)==0) { // && (tmp%10)==0
            mlpNetwork.recordVehicleData();
		}
		
		//����켣
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
					String str = time + "," +
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
							          FV + "\r\n";
					trackWriter.writeNFlush(str);
				}
			}
		}

		clock.advance(clock.getStepSize());
		if (now > clock.getStopTime() + epsilon) {
			if (infoOn)
				infoWriter.writeNFlush("��������ʵ�������⳵��" + (mlpNetwork.getNewVehID()-1)+"\r\n");
			if (loopRecOn) 
				loopRecWriter.closeWriter();
			if (trackOn)
				trackWriter.closeWriter();
			if (infoOn)
				infoWriter.closeWriter();
			mod += 1;
			return (state_ = Constants.STATE_DONE);// STATE_DONE�궨�� simulation
													// is done
		}
		else {
			/*System.out.println(String.valueOf(now/3600));
			if(Math.abs(now/3600-8)<0.001)
				System.out.println("BUG");*/
			return state_ = Constants.STATE_OK;// STATE_OK�궨��
		}			
	}

	@Override
	public void loadFiles() {
		//��������ļ�
		loadSimulationFiles();
		//����ʵ���������ڼ���fitness
		if(needEmpData) {
			//TODO DE�Ż���ʱ�޸�
			//TODO empData��ʽ��Ҫ�������
			try {
				// ���б��
				List<CSVRecord> results = CSVUtils.readCSV(runProperties.get("empDataDir"), null);//Dir fix to "R:\\DetSpeed2.csv"
				double[] tmpEmpData = new double[results.size()]; 
				for(int i=0;i<tmpEmpData.length;i++){
					tmpEmpData[i] = Double.parseDouble(results.get(i).get(0));
				}
				empData = tmpEmpData;
			} catch (IOException e) {
				e.printStackTrace();
			}
			//readFromLoop(MLPSetup.getLoopData_fileName());
		}
	}

	public int loadSimulationFiles(){
		
		//load xml
		//parse xml into parameter & network

		// ��ȡ·��xml
		XmlParser.parseNetwork(mlpNetwork, runProperties.get("roadNetworkDir"));
		// ����·�����ݺ���֯·����ͬҪ�صĹ�ϵ
		mlpNetwork.calcStaticInfo();
		// ������������
		XmlParser.parseSensors(mlpNetwork, runProperties.get("sensorDir"));
		// �����������
		mlpNetwork.initLinkStatMap(runProperties.get("statLinkIds"));
		mlpNetwork.initSectionStatMap(runProperties.get("statDetNames"));
		return 0;
	}

	@Override
	public void resetBeforeSimLoop() {//��������ʱ�� ʱ����صĲ���
		SimulationClock clock = mlpNetwork.getSimClock();
		firstEntry = true;
		clock.init(timeStart, timeEnd, timeStep);
		getSimParameter().setSimStepSize(timeStep);
		double now = clock.getCurrentTime();
		updateTime_ = now;
		LCDTime_ = now;
		statTime_ = now + getSimParameter().statWarmUp + getSimParameter().statStepSize; //��һ��ͳ��ʱ��Ϊ������ʱ��+warmUp+ͳ�Ƽ��
	}

	public void setObservedParas (double [] ob_paras){//[Qm, Vfree, Kjam]
		if (ob_paras.length != 3) {
			System.err.println("length does not match");
			return;
		}
		mlpNetwork.setOverallCapacity(ob_paras[0]);//·�ε�����ÿ��ͨ������
		int mask = 0;
		mask |= 1<<(1-1);//Vmax
		mask |= 1<<(3-1);//Kjam
		mlpNetwork.setOverallSDParas(new double[] {ob_paras[1], ob_paras[2]}, mask);
		//����Kjam ���� leff �� CF_near ��һ����
		MLPParameter allParas = (MLPParameter) mlpNetwork.getSimParameter();
		allParas.limitingParam_[0] = (float) (1.0/ob_paras[2] - allParas.VEHICLE_LENGTH);
		allParas.CF_NEAR = allParas.limitingParam_[0];//������kjam�Ǻ�
	}

	public void setOptParas(double [] optparas) {//[0]ts, [1]xc, [2]alpha, [3]beta, [4]gamma1, [5]gamma2.
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

	public void setParas(double[] ob_paras, double[] varying_paras) {//varying_paras [0]xc, [1]alpha, [2]beta, [3]gamma1, [4]gamma2.
		if (ob_paras.length != 3 || varying_paras.length != 5) {
			System.err.println("length does not match");
			return;
		}
		setObservedParas(ob_paras);
		double ts = getSimParameter().genSolution2(ob_paras, varying_paras[0]);
		double[] opt_paras = new double[6];
		opt_paras[0] = ts;
		System.arraycopy(varying_paras,0,opt_paras,1,5);
		setOptParas(opt_paras);
	}

	public void setParas(double[] fullParas) {
		if (fullParas.length != 8) {
			System.err.println("length does not match");
			return;
		}
		double[] ob = new double[3];
		double[] varying = new double[5];
		System.arraycopy(fullParas,0,ob,0,3);
		System.arraycopy(fullParas,3,varying,0,5);
		setParas(ob,varying);
	}

	public MLPParameter getSimParameter() {
		return (MLPParameter) mlpNetwork.getSimParameter();
	}

	public SimulationClock getSimClock() {
		return mlpNetwork.getSimClock();
	}

	//TODO: ��ɾ�� ��ͳ�ƹ��ܼ��ɵ�Network�£�EngThread����Ҫ����mlpNetwork����
	public MLPNetwork getMlpNetwork() {
		return mlpNetwork;
	}

	//TODO: ��ɾ�� ��ͳ�ƹ��ܼ��ɵ�Network��
	public double[] getEmpData() {
		return (double[]) empData;
	}

	public void runWithPara(double[] fullParas) {
		resetBeforeSimLoop();
		setParas(fullParas);
		run(1);//process loop only
		if(statRecordOn) {
			String statFileOut = "src/main/resources/output/loop" + Thread.currentThread().getName() + "_" + mod + ".csv";
			mlpNetwork.writeStat(statFileOut);
		}

	}

	@Override
	public void run(int mode) {
		switch (mode) {
			case 0:
				resetBeforeSimLoop();
				setParas(MLPParameter.DEFAULT_PARAMETERS);
				while (simulationLoop() >= 0);
				break;
			case 1:
				while (simulationLoop() >= 0);
				break;
				default:
					break;
		}
	}

	@Override
	public MLPNetwork getNetwork() {
		return mlpNetwork;
	}

	public static void main(String[] args) {
		MLPEngine mlpEngine = new MLPEngine("src/main/resources/demo_neihuan/scenario2/master.properties");
		mlpEngine.loadFiles();
		for (int i = 0; i < 3; i++) {
			double[] fullParas = new double[]{0.5122,20.37,0.1928,45.50056,0.92191446,7.792739,1.6195029,0.6170239};
			mlpEngine.runWithPara(fullParas);
			List<MacroCharacter> records = mlpEngine.mlpNetwork.getSecStatRecords("det2");
			double[] kmSpd = records.stream().mapToDouble(MacroCharacter::getKmSpeed).toArray();
			System.out.println(Arrays.toString(kmSpd));
		}
	}

}
