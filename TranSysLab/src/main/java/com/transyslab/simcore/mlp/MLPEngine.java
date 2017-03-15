package com.transyslab.simcore.mlp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.DoubleToLongFunction;
import java.util.regex.Matcher;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.util.MathUtils;

import com.transyslab.commons.io.CSVUtils;
import com.transyslab.commons.io.TXTUtils;
import com.transyslab.commons.tools.DE;
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
	protected DE de_;
	protected double tempBestFitness_;
	protected double[] tempBest_;
	//public double fitnessVal;
	
	public MLPEngine() {
		super();
		firstEntry = true;
		needRndETable = false;
		loopRecOn = false;
		trackOn = false;
		infoOn = false;
		msg = "";
//		fitnessVal = Double.POSITIVE_INFINITY;
	}
	public void initDE(DE de) {
		de_ = de;
		tempBest_ = new double[de_.getDim()];
	}
	@Override
	public void run(int mode) {
		if(mode == 0) {
			//Engine参数与状态的初始化
			init();
			//优化参数设置
			setOptParas(null);
			super.run(mode);
		}
		else if(mode == 1) {
			// DE算法同步gbest的标记
						HashMap<String, Integer> hm = MLPNetworkPool.getInstance().getHashMap();
						int threadid = hm.get(Thread.currentThread().getName()).intValue();
						int si = threadid * de_.getPopulation() / Constants.THREAD_NUM;
						int ei = threadid * de_.getPopulation() / Constants.THREAD_NUM + de_.getPopulation() / Constants.THREAD_NUM;
						for (int i = si; i < ei; i++) {



							de_.getNewIdvds()[i].setFitness((float) calFitness(null));
							de_.selection(i);
							if (tempBestFitness_ > de_.getFitness(i)) {
								tempBestFitness_ = de_.getFitness(i);
								// tempIndex_ = i;
								for (int j = 0; j < tempBest_.length; j++) {
									tempBest_[j] = de_.getPosition(i)[j];
								}

							}
							de_.changePos(i);

						}
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
			firstEntry = false;

			// This block is called only once just before the simulation gets
			// started.
			
			//establish writers
			String threadName = Thread.currentThread().getName();
			if (loopRecOn) {
				loopRecWriter = new TXTUtils("src/main/resources/output/loop" + threadName + ".csv");
				loopRecWriter.write("TIME,VID,VIRTYPE,SPD,POS,LINK,LOCATION\r\n");
			}				
			if (trackOn) {
				trackWriter = new TXTUtils("src/main/resources/output/track" + threadName + ".csv");
				trackWriter.write("TIME,VID,VIRTYPE,BUFF,POS,SEG,LINK,DSP,SPD,LEAD,TRAIL\r\n");
			}				
			if (infoOn)
				infoWriter = new TXTUtils("src/main/resources/output/info" + threadName + ".txt");
			
			
			//set overall parameters
			//mlp_network.setOverallCapacity(0.5);
//			mlp_network.setOverallSDParas(new double [] {16.67,0.0,0.180,5.0,1.8});
//			mlp_network.setLoopSection(mlp_network.getLink(0).getCode(), 0.75);
			
			//reset update time
			mlp_network.resetReleaseTime();
			
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
			if (loopRecOn) {
				for (int j = 0; j < mlp_network.loops.size(); j++){
					msg = mlp_network.loops.get(j).detect();
					if (msg != "") {
						loopRecWriter.write(time + "," + msg);
					}
				}
			}			
			//车辆推进
			for (MLPVehicle vehicle : mlp_network.veh_list) {
				vehicle.advance();
			}						
		}
		
		//可视化渲染
//		mlp_network.recordVehicleData();
		
		//输出轨迹
		if (trackOn) {
			if (!mlp_network.veh_list.isEmpty()) {
				int LV;
				int FV;
				for (MLPVehicle v : mlp_network.veh_list) {
					LV = 0; FV = 0;
					if (v.leading_!=null)
						LV = v.leading_.getCode();
					if (v.trailing_ != null)
						FV = v.trailing_.getCode();
					trackWriter.write(time + "," + 
							 				  v.getCode() + "," +
							 				  v.VirtualType_ + "," +
							 				  v.buffer_ + "," +
							 				  v.lane_.getLnPosNum() + "," +
							 				  v.segment_.getCode() + "," +
							 				  v.link_.getCode() + "," +
							 				  v.Displacement() + "," +
							 				  v.currentSpeed() + "," + 
							 				  LV + "," + 
							 				  FV + "\r\n");
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
			return (state_ = Constants.STATE_DONE);// STATE_DONE宏定义 simulation
													// is done
		}
		else {
//			System.out.println(time);
			return state_ = Constants.STATE_OK;// STATE_OK宏定义
		}			
	}

	@Override
	public void loadFiles() {
		//读入仿真文件
		loadSimulationFiles();
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
		return 0;
	}
	
	public void init() {//Engine中需要初始化的属性
		//固定参数 时间设置
		firstEntry = true;
		SimulationClock.getInstance().init(0, 6900, 0.2);		
		double now = SimulationClock.getInstance().getCurrentTime();
		updateTime_ = now;
		LCDTime_ = now;
		
		//Network状态重设并准备发车表
		MLPNetwork.getInstance().resetNInit(needRndETable, System.currentTimeMillis());
	}
	
	public void initSnapshotData(){
		
	}
	
	public double calFitness(double [] paras) {
		//初始化引擎的固定参数（时间）
		init();
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

				double avg_trTime = 0.0;				
				if (trTlist.size()>0) {
					for (Double trt : trTlist) {
						avg_trTime += trt;
					}
					avg_trTime = avg_trTime / trTlist.size();
					simLinkFlow[idx] = trTlist.size();	
				}
				simTrT[idx] = avg_trTime;

				trTlist.clear();

				double avg_ExpSpeed = 0.0;
				if (!mlp_network.mlpLink(0).hasNoVeh()) {
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
				simSpeed[idx] = avg_ExpSpeed;

				caltime += calStep;
				idx += 1;
			}
		}
		double [][] realLoopDetect = readFromLoop(MLPSetup.getLoopDir());
		double [] realSpeed = new double [realLoopDetect.length];
		for (int k = 0; k < realLoopDetect.length; k++) {
			realSpeed[k] = realLoopDetect[k][0];
		}
		double fitnessVal = MAPE(simSpeed, realSpeed);
		//		System.out.println(fitnessVal);
		return fitnessVal;
	}
	
	public double [][] readFromLoop(String filePath) {
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
		return ans;
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
	
	public void setOptParas(double [] optparas) {
		if (optparas != null) {
			MLPNetwork mlp_network = MLPNetwork.getInstance();
			mlp_network.setOverallSDParas(new double [] {optparas[0],optparas[1],optparas[2],optparas[3],optparas[4]});
		}
		
	}

}
