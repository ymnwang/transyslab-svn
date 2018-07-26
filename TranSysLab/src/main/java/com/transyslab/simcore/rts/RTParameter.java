package com.transyslab.simcore.rts;

import com.transyslab.roadnetwork.Parameter;

/**
 * Created by ITSA405-35 on 2018/5/28.
 */
public class RTParameter extends Parameter{
    private int normHeadway = 6; //标准小汽车的饱和车头间距设为6m
    private int gapSpeed = 30; //划分排队与畅行的速度阈值，km/h
    private int dischargeSpeed = 20; //消散车速，km/h
    private int dischargeSecond = 1; //启动时距，sec
    private int arriveSecond = 1; //到达时距，sec
    private int saturateSecond = 2; //饱和过车车头时距，sec
    private int expSpeed = 35; //畅行期望速度，km/h
    private int maxSpeed = 60; //一般的速度上限，km/h

}
