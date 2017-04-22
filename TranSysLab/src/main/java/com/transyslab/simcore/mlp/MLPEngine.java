package com.transyslab.simcore.mlp;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.DoubleToLongFunction;
import java.util.regex.Matcher;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.genetics.Fitness;
import org.apache.commons.math3.util.MathUtils;

import com.transyslab.commons.io.CSVUtils;
import com.transyslab.commons.io.TXTUtils;
import com.transyslab.commons.tools.DE;
import com.transyslab.commons.tools.FitnessFunction;
import com.transyslab.commons.tools.SimulationClock;
import com.transyslab.commons.tools.TimeMeasureUtil;
import com.transyslab.commons.tools.emitTable;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.simcore.AppSetup;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mesots.MesoNetwork;
import com.transyslab.simcore.mesots.MesoNetworkPool;


public class MLPEngine extends SimulationEngine{
	
	//protected int runTimes_; // 仿真运行次数
	protected double updateTime_;
	protected double LCDTime_;
	protected boolean firstEntry; // simulationLoop中第一次循环的标记
	protected boolean needRndETable; //needRndETable==true,随机生成发车表，needRndETable==false，从文件读入发车表
	protected TXTUtils loopRecWriter;
	protected boolean loopRecOn;
	protected TXTUtils trackWriter;
	protected boolean trackOn;
	protected TXTUtils infoWriter;
	protected boolean infoOn;
	protected String msg;
	public boolean seedFixed;
	public long runningseed;
	public boolean outputSignal;
	private Object empData;
	public boolean needEmpData;
	private int mod;
	private boolean displayOn;
	//public double fitnessVal;
	
	public MLPEngine() {
		super();
		firstEntry = true;
		needRndETable = false;
		loopRecOn = false;
		trackOn = false;
		infoOn = false;
		msg = "";
		seedFixed = false;
		needEmpData = false;
		mod = 0;
		displayOn = false;
//		fitnessVal = Double.POSITIVE_INFINITY;
	}

	@Override
	public void run(int mode) {//0: no display 1: with display 2: run with logs
		switch (mode) {		
		case 0:
			//Engine参数与状态的初始化
			resetEngine(0, 6900, 0.2);
			//优化参数设置
			setOptParas(null);
			displayOn = true;
			while (simulationLoop()>=0);
			break;
		case 1:
			//Engine参数与状态的初始化
			resetEngine(0, 6900, 0.2);
			//优化参数设置
			setOptParas(null);
			while (simulationLoop()>=0);
			break;
		case 2:
			trackOn = true;
			needRndETable = true;
			seedFixed = true;
			runningseed = 1490183749797l;
			resetEngine(0, 6900, 0.2);
			setOptParas(null);			
			while (simulationLoop()>=0);
			break;
		default:
			break;
		}
	}

	@Override
	public int simulationLoop() {
		final double epsilon = 1.0E-3;
		// 实例化路网对象，一个线程对应一个实例
		MLPNetwork mlp_network = MLPNetwork.getInstance();

		double now = SimulationClock.getInstance().getCurrentTime();
		double startTime = SimulationClock.getInstance().getStartTime();
		String time = String.format("%.1f", now - startTime);

		if (firstEntry) {
			// This block is called only once just before the simulation gets
			// started.
			firstEntry = false;
			//Network状态重设并准备发车表
			if (!seedFixed) 
				runningseed = System.currentTimeMillis();
			MLPNetwork.getInstance().resetNetwork(needRndETable, runningseed);
			
			//reset update time
			mlp_network.resetReleaseTime();
			
			//establish writers
			String threadName = Thread.currentThread().getName();
			if (loopRecOn) {
				loopRecWriter = new TXTUtils("src/main/resources/output/loop" + threadName + "_" + mod + ".csv");
				loopRecWriter.write("TIME,VID,VIRTYPE,SPD,POS,LINK,LOCATION\r\n");
			}				
			if (trackOn) {
				trackWriter = new TXTUtils("src/main/resources/output/track" + threadName + "_" + mod + ".csv");
				trackWriter.write("TIME,RVID,VID,VIRTYPE,BUFF,POS,SEG,LINK,DSP,SPD,LEAD,TRAIL\r\n");
			}				
			if (infoOn)
				infoWriter = new TXTUtils("src/main/resources/output/info" + threadName + "_" + mod + ".txt");
			//信息统计：发车数
			if (infoOn) {
				int total = 0;
				for (int i = 0; i < mlp_network.nLinks(); i++) {
					total += mlp_network.mlpLink(i).emtTable.getInflow().size();				
				}
				infoWriter.writeNFlush("随机发出真实车： " + total + "\r\n");	
			}		
		}
		
		if (now>= updateTime_){
			mlp_network.resetReleaseTime();
			if (loopRecOn) 
				loopRecWriter.flushBuffer();
			if (trackOn)
				trackWriter.flushBuffer();
			if (infoOn)
				infoWriter.flushBuffer();
			updateTime_ = now + MLPParameter.getInstance().updateStepSize_;
		}
		
		//读入发车表
		mlp_network.loadEmtTable();
		
		//路网存在车辆的情况下才进行计算
		if (mlp_network.veh_list.size()>0) {
			//车队识别
			mlp_network.platoonRecognize();			
			//若进入换道决策时刻，进行换道计算与车队重新识别
			if (now >= LCDTime_) {
				for (int i = 0; i < mlp_network.nLinks(); i++){
					mlp_network.mlpLink(i).lanechange();
				}
				mlp_network.platoonRecognize();
				LCDTime_ = now + MLPParameter.getInstance().LCDStepSize_;
			}
			//运动计算、节点服务(暂缺)、写入发车表（暂缺）
			//车辆速度计算
			for (int i = 0; i < mlp_network.nLinks(); i++){
				mlp_network.mlpLink(i).move();
			}
			//车辆状态更新(同时更新)
			for (int k = 0; k<mlp_network.veh_list.size(); k++) {
				MLPVehicle theVeh = mlp_network.veh_list.get(k);
				if (theVeh.updateMove()==1) 
					k -=1;
			}
			//线圈检测
			for (int j = 0; j < mlp_network.loops.size(); j++){
				msg = mlp_network.loops.get(j).detect(time);				
				if (loopRecOn) {//按需输出记录
					loopRecWriter.write(msg);
				}
			}
			for (int i = 0; i < mlp_network.nNodes(); i++) {
				mlp_network.getNode(i).dispatchStatedVeh();
			}
			//车辆推进
			for (MLPVehicle vehicle : mlp_network.veh_list) {
				vehicle.advance();
			}						
		}
		
		//可视化渲染
		if (displayOn) {
			mlp_network.recordVehicleData();
		}
		
		//输出轨迹
		if (trackOn) {
			if (!mlp_network.veh_list.isEmpty() ) {//&& now - 2*Math.floor(now/2) < 0.001
				int LV;
				int FV;
				for (MLPVehicle v : mlp_network.veh_list) {
					LV = 0; FV = 0;
					if (v.leading_!=null)
						LV = v.leading_.getCode();
					if (v.trailing_ != null)
						FV = v.trailing_.getCode();
					String str = time + "," +
							          v.RVID + "," + 
							          v.getCode() + "," +
							          v.VirtualType_ + "," +
							          v.buffer_ + "," +
							          v.lane_.getLnPosNum() + "," +
							          v.segment_.getCode() + "," +
							          v.link_.getCode() + "," +
							          v.Displacement() + "," +
							          v.currentSpeed() + "," + 
							          LV + "," + 
							          FV + "\r\n";
					if (true) {//v.VirtualType_==0
						trackWriter.writeNFlush(str);
					}
				}
			}
		}
		
		SimulationClock.getInstance().advance(SimulationClock.getInstance().getStepSize());
		if (now > SimulationClock.getInstance().getStopTime() + epsilon) {			
			if (infoOn)
				infoWriter.writeNFlush("共产生真实车与虚拟车：" + (mlp_network.getNewVehID()-1)+"\r\n");			
			if (loopRecOn) 
				loopRecWriter.closeWriter();
			if (trackOn)
				trackWriter.closeWriter();
			if (infoOn)
				infoWriter.closeWriter();
			mod += 1;
			return (state_ = Constants.STATE_DONE);// STATE_DONE宏定义 simulation
													// is done
		}
		else {
			System.out.println(time);			
			return state_ = Constants.STATE_OK;// STATE_OK宏定义
		}			
	}

	@Override
	public void loadFiles() {
		//读入仿真文件
		loadSimulationFiles();
		//读入实测数据用于计算fitness
		if(needEmpData) {
			readFromLoop(MLPSetup.getLoopDir());
		}
		//其他初始化过程。目前为空
		start();
	}

	@Override
	public void start() {
		// TODO 自动生成的方法存根
		
	}
	
	public int loadSimulationFiles(){
		
		//load xml
		//parse xml into parameter & network

		// 读取路网xml
		MLPSetup.ParseNetwork();
		// 读入路网数据后组织路网不同要素的关系
		MLPNetwork.getInstance().calcStaticInfo();
//		// 读取检测器
//		MLPSetup.ParseSensorTables();
//		MLPNetwork.getInstance().createLoopSurface();
		return 0;
	}
	
	public void resetEngine(double timeStart, double timeEnd, double timeStep) {//时间相关的参数
		firstEntry = true;
		SimulationClock.getInstance().init(timeStart, timeEnd, timeStep);		
		double now = SimulationClock.getInstance().getCurrentTime();
		updateTime_ = now;
		LCDTime_ = now;
	}
	
	public void setOptParas(double [] optparas) {
		if (optparas != null) {
			MLPNetwork mlp_network = MLPNetwork.getInstance();			
			mlp_network.setOverallSDParas(new double [] {optparas[0],0.0,optparas[1],optparas[2],optparas[3]});
			MLPParameter.getInstance().setDUpper((float) optparas[5]);
			MLPParameter.getInstance().setDLower((float) optparas[4]);
			mlp_network.setOverallCapacity(0.45);
//			MLPParameter.getInstance().setLCPara(new double[] {optparas[5], optparas[5]});
//			MLPParameter.getInstance().setLCBuffTime(optparas[6]);
		}		
	}
	
	public double calFitness(double [] paras) {
		//初始化引擎的固定参数（时间）
		resetEngine(0, 6900, 0.2);
		//设置优化参数
		setOptParas(paras);
		//设置fitness fun的变量
		MLPNetwork mlp_network = MLPNetwork.getInstance();
		double calStep = 300;
		double caltime = calStep;
		int sampleSize = (int) (SimulationClock.getInstance().getDuration() / calStep);
		double []  simTrT = new double [sampleSize];
		double []  simSpeed = new double [sampleSize];
		double []  simLinkFlow = new double [sampleSize];
		int idx = 0;
		//运行仿真，定时进行输出统计
		while(simulationLoop()>=0) {
			double now = SimulationClock.getInstance().getCurrentTime();
			if (now>=caltime) {
				List<Double> trTlist = mlp_network.mlpLink(0).tripTime;				
				//SimTrT计算
				/*double avg_trTime = 0.0;				
				if (trTlist.size()>0) {
					for (Double trt : trTlist) {
						avg_trTime += trt;
					}
					avg_trTime = avg_trTime / trTlist.size();
					simLinkFlow[idx] = trTlist.size();	
				}
				simTrT[idx] = avg_trTime;*/
				trTlist.clear();

				//瞬时平均运行速度
				/*double avg_ExpSpeed = 0.0;
				if (!mlp_network.mlpLink(0).hasNoVeh(false)) {
					int count = 0;
					double sum = 0.0;
					for (JointLane JL : mlp_network.mlpLink(0).jointLanes) {
						for (MLPLane LN : JL.lanesCompose) {
							for (MLPVehicle Veh : LN.vehsOnLn) {
								if (Veh.VirtualType_ == 0) {
									sum += (Veh.Displacement() - Veh.DSPEntrance)  /  (now - Veh.TimeEntrance);
									count += 1;
								}
							}
						}
					}
					avg_ExpSpeed = sum / count;
				}
				simSpeed[idx] = avg_ExpSpeed;*/
				
				//线圈检测地点速度
				simSpeed[idx] = mlp_network.loopStatistic("det3");
						
				caltime += calStep;
				idx += 1;
			}
		}
		double[][] realLoopDetect =(double[][]) empData;
		double [] realSpeed = new double [realLoopDetect.length];
		for (int k = 0; k < realLoopDetect.length; k++) {
			realSpeed[k] = realLoopDetect[k][0];
		}
		double[] tmpSim = new double[simSpeed.length-4];
		double[] tmpReal = new double[simSpeed.length-4];
		System.arraycopy(simSpeed, 4, tmpSim, 0, simSpeed.length-4);
		System.arraycopy(realSpeed, 4, tmpReal, 0, simSpeed.length-4);
		double fitnessVal = FitnessFunction.evaRNSE(tmpSim, tmpReal);
		return fitnessVal;
	}
	
	public void readFromLoop(String filePath) {
		double [][] ans = null;
		String [] loopheader = {"FromTime","ToTime","ArcID","Speed","Flow","Density"};
		try {
			List<CSVRecord> Rows = CSVUtils.readCSV(filePath, loopheader);
			ans = new double [Rows.size() - 1] [2];
			for (int i = 1; i < Rows.size(); i++) {
				CSVRecord r = Rows.get(i);
				ans[i-1][0] = Double.parseDouble(r.get(3));
				ans[i-1][1] = Double.parseDouble(r.get(4));
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		empData = ans;
	}
	
	public double MAPE(double[] sim, double[] real) {
		if (sim.length<=0 || sim.length != real.length) {
			return Double.POSITIVE_INFINITY;
		}		
		double sum = 0.0;
		int count = 0;
		for (int i = 1; i < sim.length; i++) {//1 for warmup
			double del = Math.abs(sim[i] - real[i]);
			if (Math.abs(real[i])>0.0001) {
				sum += del / real[i];
				count += 1;
			}
		}		
		if (count > 0) 
			return (sum / count);
		else 
			return 0.0;
	}
	
}
