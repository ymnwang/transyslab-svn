package com.transyslab.simcore.mlp;

import java.util.List;

public class MicroCharacter {
    //属性挑选
    public static final int SELECT_DETTIME = 1;
    public static final int SELECT_SPEED = 2;
    public static final int SELECT_HEADWAY = 4;
    public static final int SELECT_LANEID = 8;
    protected double detTime; //unit: s
    protected double speed; //unit: m/s
    protected double headway; //unit: s
    protected int laneId; //unit: veh/m/lane

    public MicroCharacter(int laneId,double detTime, double speed,double headway){
        this.laneId =laneId;
        this.detTime = detTime;
        this.speed = speed;
        this.headway = headway;
    }
    public static double[] select(List<MicroCharacter> mcList, int mask) {
        switch (mask) {
            case SELECT_DETTIME:
                return mcList.stream().mapToDouble(e -> e.detTime).toArray();
            case SELECT_SPEED:
                return mcList.stream().mapToDouble(e -> e.speed).toArray();
            case SELECT_HEADWAY:
                return mcList.stream().mapToDouble(e -> e.headway).toArray();
            //TODO laneid 为整型
            case SELECT_LANEID:
                return mcList.stream().mapToDouble(e -> e.laneId).toArray();
            default:
                return null;
        }
    }
    public double getDetTime(){
        return detTime;
    }
    public double getSpeed(){
        return speed;
    }
    public double getHeadway(){
        return headway;
    }
}
