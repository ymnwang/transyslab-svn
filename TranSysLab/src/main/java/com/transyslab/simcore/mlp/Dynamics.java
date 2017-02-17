package com.transyslab.simcore.mlp;

public class Dynamics {
	public double [] sdPara;//[0]VMax; [1]VMin; [2]KJam; [3]Alpha; [4]Beta;
	//public double [] cfPara;//[0]Critical Gap; [1]dUpper;
	public Dynamics(){
		sdPara = MLPParameter.getInstance().getSDPara();
	}
	public Dynamics(double [] SDParas) {
		sdPara = SDParas;
	}
	public double sdFun(double k) {
		if (k <= sdPara[2]){
			 //vMin + (vMax - vMin)*(1-(K/kJam).^a).^b;
			return (sdPara[1] + (sdPara[0]-sdPara[1]) * Math.pow(1.0-Math.pow(k/sdPara[2], sdPara[3]), sdPara[4]));
		}
		else {
			return 0.0;
		}
	}
	public double cfFun(MLPVehicle theVeh){
		double gap = theVeh.leading_.Displacement() - theVeh.leading_.getLength() - theVeh.Displacement(); 
		double vlead = (double) theVeh.leading_.currentSpeed();
		double upperGap = MLPParameter.CELL_RSP_UPPER;
		if(gap < MLPParameter.CF_NEAR) {
			return vlead;
		}
		else if (gap<upperGap) {
			double r = gap/upperGap;
			return r * MLPParameter.getInstance().maxSpeed(gap) + (1.0-r) * vlead; 
		}
		else {
			return sdPara[0];
		}
	}
	public double updateHeadSpd(MLPVehicle headVeh){
		MLPLane nextLane = headVeh.lane_.connectedDnLane;
		if (headVeh.distance() < MLPParameter.SEG_NEAR && 
				nextLane != null && (nextLane.enterAllowed || nextLane.checkVolum(headVeh))) {
			//���ڽӽ�segĩ�� �� ����seg���������� ��ͣ���ȴ�
			return 0.0;
		}
		
		if (headVeh.leading_ != null) {
			return cfFun(headVeh);
		}
		else if (((MLPSegment)headVeh.link_.getEndSegment()).endDSP - headVeh.Displacement() > 
						MLPParameter.CELL_RSP_UPPER ||
				headVeh.segment_.isTheEnd() != 0) {
			//�����յ��Զ �� ����endSegment(������)
			return sdPara[0];
		}
		else {
			//׼������Link Passing��ͷ��
			//��ʱ�������ƣ��Ժ�İ汾�˴���ڵ�״̬��ϣ��ۺϿ��ǽ���ͬһ�ڵ������link��״̬����headSpeed
			return sdPara[0];
		}
	}	
	
}
