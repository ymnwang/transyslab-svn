package com.transyslab.simcore.mlp;

/**
 * Created by WangYimin on 2017/8/16.
 */
public class MacroCharacter {
	//�������Զ�Ϊ���ͳ�ƾ�ֵ����δƽ������
	protected double flow; //unit: veh/s
	protected double speed; //unit: m/s
	protected double density; //unit: veh/m
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

	public static double getHourFlow(double flow) {
		return flow * 3600;
	}

	public static double getKmSpeed(double speed) {
		return speed * 3.6;
	}

	public static double getKmDensity(double density) {
		return density * 1000;
	}
}
