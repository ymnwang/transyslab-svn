package com.transyslab.experiments;

import com.transyslab.simcore.mlp.*;
import org.encog.util.Stopwatch;

/**
 * Created by WANG YiMin on 2017/10/12.
 */
public class SpdField {

    public static void addLoop(MLPLink mlpLink, double distanceInterval) {
        MLPNetwork mlpNetwork = (MLPNetwork) mlpLink.getNetwork();
        double currentP = 0;
        double lnkLen = mlpLink.length();
        int n = 0;
        while (currentP <= lnkLen) {
            mlpNetwork.setLoopsOnLink(String.valueOf(n), mlpLink.getId(), currentP / lnkLen);
            currentP += distanceInterval;
            n += 1;
        }
        mlpNetwork.setLoopsOnLink("det_" + mlpLink.getId() + "_" + n,
                mlpLink.getId(),
                1.0);
    }

    public static void main(String[] args) {
        //�ƶ�·��+��ȡ�ļ�
        MLPEngine mlpEngine = new MLPEngine("src/main/resources/demo_neihuan/scenario2/�ٶȳ�����.properties");
        mlpEngine.loadFiles();

        //���в�������
        mlpEngine.seedFixed = true;//ǿ��
        mlpEngine.runningSeed = 1500613842660l;

        //����·��������Ȧ
        SpdField.addLoop(mlpEngine.getNetwork().findLink(111), 50);
        SpdField.addLoop(mlpEngine.getNetwork().findLink(112), 50);
        SpdField.addLoop(mlpEngine.getNetwork().findLink(26), 50);
        SpdField.addLoop(mlpEngine.getNetwork().findLink(113), 50);
        SpdField.addLoop(mlpEngine.getNetwork().findLink(25), 50);

        //��ʼ����ʱ��
        Stopwatch timer = new Stopwatch();
        timer.start();

        //��������
        mlpEngine.repeatRun();

        //ͳ�Ʒ���
        System.out.println("δ����������" + mlpEngine.countOnHoldVeh() + "��");
        timer.stop();
        System.out.println("time " + timer.getElapsedMilliseconds() + " ms");

        //�ر�����
        mlpEngine.close();
    }
}
