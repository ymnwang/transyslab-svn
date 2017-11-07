package com.transyslab.simcore.mlp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WangYimin on 2017/8/16.
 */
public class MacroCharacter {
	//�������Զ�Ϊ���ͳ�ƾ�ֵ����δƽ������
	protected double flow; //unit: veh/s/lane
	protected double speed; //unit: m/s
	protected double density; //unit: veh/m/lane
	protected double travelTime; //unit: s

	public MacroCharacter(double flow, double speed, double density, double travelTime) {
		this.flow = flow;
		this.speed = speed;
		this.density = density;
		this.travelTime = travelTime;
	}

	public double getHourFlow() {
		return flow * 3600; //unit: veh/h
	}

	public double getKmSpeed() {
		return speed * 3.6;
	}

	public double getKmDensity() {
		return density * 1000;
	}

	public double getHourTravelTime() {
		return travelTime / 3600.0;
	}

	public static double getHourFlow(double flow) {
		return flow * 3600;
	}

	public static double getKmSpeed(double speed) {
		return speed * 3.6;
	}

	public static double getKmDensity(double density) {
		return density * 1000;
	}

	/**
	 * ����۲�����ת��Ϊdouble����
	 * @param mcList
	 * @return double[]����
	 */
	public static List<double[]> transfer(List<MacroCharacter> mcList) {
		List<double[]> results = new ArrayList<>();

		double[] flow = mcList.stream().mapToDouble(e -> e.flow).toArray();
		double[] speed = mcList.stream().mapToDouble(e -> e.speed).toArray();
		double[] density = mcList.stream().mapToDouble(e -> e.density).toArray();
		double[] travelTime = mcList.stream().mapToDouble(e -> e.travelTime).toArray();

		results.add(flow);
		results.add(speed);
		results.add(density);
		results.add(travelTime);

		return results;
	}

	public static List<double[]> transferToKmH(List<MacroCharacter> mcList) {
		List<double[]> results = new ArrayList<>();

		double[] flow = mcList.stream().mapToDouble(MacroCharacter::getHourFlow).toArray();
		double[] speed = mcList.stream().mapToDouble(MacroCharacter::getKmSpeed).toArray();
		double[] density = mcList.stream().mapToDouble(MacroCharacter::getKmDensity).toArray();
		double[] travelTime = mcList.stream().mapToDouble(MacroCharacter::getHourTravelTime).toArray();

		results.add(flow);
		results.add(speed);
		results.add(density);
		results.add(travelTime);

		return results;
	}
}
