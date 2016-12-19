package com.transyslab.roadnetwork;

import java.util.concurrent.LinkedBlockingQueue;

//VehicleData����أ����ڻ��պͲ���VehicleData
public class VehicleDataPool {
	
	private static VehicleDataPool vhcDataPool_ = new VehicleDataPool();;
	private int nRows_;
	//�̰߳�ȫ��˫�˶��У���˵ʹ�÷ǻ�����ʵ�֣�����Ч�����߳�������
	private LinkedBlockingQueue<VehicleData> recycleList_;
	
	private VehicleDataPool(){
		recycleList_ = new LinkedBlockingQueue<VehicleData>();
		nRows_ = 0;
	}
	
	public static VehicleDataPool getVehicleDataPool(){
		return vhcDataPool_;
	}
	public void recycleVehicleData(VehicleData vd){
		vd.clean();
		recycleList_.offer(vd);
	}
	public VehicleData getVehicleData() /* get a vehicle from the list */
	{
		VehicleData vd;

		if (recycleList_.isEmpty()) { // list is empty
			vd = new VehicleData();
			nRows_++;
		}
		else { // get head from the list
			vd = recycleList_.poll();
		}
		return vd;
	}
	public int nRows(){
		return nRows_;
	}
}
