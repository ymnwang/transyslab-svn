package com.transyslab.simcore.rts;

import com.transyslab.roadnetwork.Parameter;

/**
 * Created by ITSA405-35 on 2018/5/28.
 */
public class RTParameter extends Parameter{
    private int normHeadway = 6; //��׼С�����ı��ͳ�ͷ�����Ϊ6m
    private int gapSpeed = 30; //�����Ŷ��볩�е��ٶ���ֵ��km/h
    private int dischargeSpeed = 20; //��ɢ���٣�km/h
    private int dischargeSecond = 1; //����ʱ�࣬sec
    private int arriveSecond = 1; //����ʱ�࣬sec
    private int saturateSecond = 2; //���͹�����ͷʱ�࣬sec
    private int expSpeed = 35; //���������ٶȣ�km/h
    private int maxSpeed = 60; //һ����ٶ����ޣ�km/h

}
