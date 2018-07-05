package com.transyslab.simcore.rts;

import com.transyslab.commons.io.CSVUtils;
import com.transyslab.commons.io.ConfigUtils;
import com.transyslab.commons.io.XmlParser;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.RoadNetwork;
import com.transyslab.roadnetwork.VehicleData;
import com.transyslab.roadnetwork.VehicleDataPool;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mlp.MacroCharacter;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ITSA405-35 on 2018/5/28.
 */
public class RTEngine extends SimulationEngine{
	//����·���ṹ
	private RTNetwork rtNetwork;

	//�������������ļ�����
	private HashMap<String,String> runProperties;
	//��������ʱ��������Ϣ
	private double timeStart;
	private double timeEnd;
	private double timeStep;
	private String rootDir;
	private Configuration config;
	private CSVParser frameParser;
	private int curFrameId;
	private boolean firstEntry;
	private boolean isStop;
	public static boolean isState;

	public RTEngine(){
		curFrameId = -1;
		rtNetwork = new RTNetwork();
		runProperties = new HashMap<>();
		firstEntry = true;
		isStop = false;
		isState = true;
	}
	public RTEngine(String masterFilePath) {
		this();
		rootDir = new File(masterFilePath).getParent() + "/";
		parseProperties(masterFilePath);
	}
	private void parseProperties(String configFilePath) {
		config = ConfigUtils.createConfig(configFilePath);

		//input files
		runProperties.put("roadNetworkPath", rootDir + config.getString("roadNetworkPath"));
		runProperties.put("sensorPath", rootDir + config.getString("sensorPath"));
		String tmp = config.getString("extVhcPath");
		runProperties.put("extVhcPath", tmp==null || tmp.equals("") ? null : rootDir + tmp);

		//time setting
		timeStart = Double.parseDouble(config.getString("timeStart"));
		timeEnd = Double.parseDouble(config.getString("timeEnd"));
		timeStep = Double.parseDouble(config.getString("timeStep"));

	}
	@Override
	public int simulationLoop() {
		return 0;
	}
	public void run(){
		// frameid, vhcid, distance, laneid
		// �����Ѱ�֡������
		CSVRecord curRecord;
		List<VehicleData> vds = new ArrayList<>();
		int frameConter = 1;
		while((curRecord = frameParser.iterator().next())!=null){
			int frameid = Integer.parseInt(curRecord.get(0));
			if(firstEntry) {
				curFrameId = frameid;
				firstEntry = false;
			}
			int vhcid = Integer.parseInt(curRecord.get(1));
			double distance = Double.parseDouble(curRecord.get(2));
			int laneid = Integer.parseInt(curRecord.get(3));
			int flag = Integer.parseInt(curRecord.get(4));
			boolean queueFlag = false;
			if(flag == 1)
				queueFlag = true;
			int tarLaneid = Integer.parseInt(curRecord.get(6));
			double speed = Double.parseDouble(curRecord.get(5));
			VehicleData vd = VehicleDataPool.getVehicleDataPool().getVehicleData();
			vd.init(vhcid,rtNetwork.findLane(laneid), Constants.DEFAULT_VEHICLE_LENGTH,distance,speed,tarLaneid,queueFlag,true);
			if(curFrameId != frameid){//�µ�һ֡
				if(!vds.isEmpty()){
					if (isStop) {
						forceReset();
						isStop = false;
						// ����ѭ��
						break;
					}
					if(!isState) {
						rtNetwork.renderVehicle(vds);
					}
					else{
						if(frameConter%30 == 0)
							rtNetwork.renderState(vds);
					}

					vds.clear();
				}
				frameConter ++;
				curFrameId = frameid;
			}
			vds.add(vd);
		}

	}
	@Override
	public void loadFiles() {
		loadSimulationFiles();
		// ��ȡ�ⲿ�����켣����
		try {
			frameParser = CSVUtils.getCSVParser(runProperties.get("extVhcPath"),true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void loadSimulationFiles(){
		// ��ȡ·��xml
		XmlParser.parseNetwork(rtNetwork, runProperties.get("roadNetworkPath"));
		// ����·�����ݺ���֯·����ͬҪ�صĹ�ϵ
		rtNetwork.calcStaticInfo();
		// TODO �����
	}
	@Override
	public RoadNetwork getNetwork() {
		return rtNetwork;
	}

	@Override
	public int repeatRun() {
		return 0;
	}

	@Override
	public void close() {
		if(frameParser!=null) {
			try {
				frameParser.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stop() {
		isStop = true;
	}
	public void forceReset(){
		this.firstEntry = true;
		this.curFrameId = -1;
		try {
			this.frameParser.close();
			this.frameParser = CSVUtils.getCSVParser(runProperties.get("extVhcPath"),true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public HashMap<String, List<MacroCharacter>> getEmpMap() {
		return null;
	}

	@Override
	public HashMap<String, List<MacroCharacter>> getSimMap() {
		return null;
	}

	@Override
	public int countRunTimes() {
		return 0;
	}
}
