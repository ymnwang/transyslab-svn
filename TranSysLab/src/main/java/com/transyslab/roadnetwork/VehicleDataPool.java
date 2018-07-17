package com.transyslab.roadnetwork;

import java.util.concurrent.ConcurrentLinkedQueue;

//VehicleData����أ����ڻ��պͲ���VehicleData
public class VehicleDataPool {
	
	private static VehicleDataPool vhcDataPool_ = new VehicleDataPool();;
	private int counter; // ConcurrentLinkedQueue.size()��Ҫ�������ϣ�Ч�ʽϵ�
	//�̰߳�ȫ����
	private ConcurrentLinkedQueue<VehicleData> recycleList_;
	
	private VehicleDataPool(){
		recycleList_ = new ConcurrentLinkedQueue<>();
		counter = 0;
	}
	
	public static VehicleDataPool getInstance(){
		return vhcDataPool_;
	}
	public void recycle(VehicleData vd){
		vd.clean();
		recycleList_.offer(vd);
		counter ++;
	}
	public VehicleData newData() /* get a vehicle from the list */
	{
		VehicleData vd;
		if (recycleList_.isEmpty()) { // list is empty
			vd = new VehicleData();
		}
		else { // get head from the list
			vd = recycleList_.poll();
			counter --;
		}
		return vd;
	}
	public int nRows(){
		return counter;
	}
}
