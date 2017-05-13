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
			double ans = sdPara[1] + (sdPara[0]-sdPara[1]) * Math.pow(1.0-Math.pow(k/sdPara[2], sdPara[3]), sdPara[4]);
//			if (Double.isNaN(ans)){
//				System.out.println("BUG SD函数输出异常");
//			}
			return ans;
		}
		else {
			return 0.0;
		}
	}
	public double cfFun(MLPVehicle theVeh){
		double gap = theVeh.leading_.Displacement() - theVeh.leading_.getLength() - theVeh.Displacement(); 
		double vlead = (double) theVeh.leading_.currentSpeed();
		double upperGap = MLPParameter.getInstance().CF_FAR;
		if(gap < MLPParameter.getInstance().CF_NEAR) {
			return vlead;
		}
		else if (gap<upperGap) {
			double r = gap/upperGap;
			//return r * sdPara[0] + (1.0-r) * vlead;
			double ans = r * MLPParameter.getInstance().maxSpeed(gap) + (1.0-r) * vlead;
//			if (Double.isNaN(ans))
//				System.out.println("BUG CF函数输出异常");
			return ans;
		}
		else {
			return sdPara[0];
		}
	}
	public double updateHeadSpd(MLPVehicle headVeh){
		MLPLane nextLane = headVeh.lane_.connectedDnLane;
		if (headVeh.distance() < MLPParameter.SEG_NEAR && 
				nextLane != null && (!nextLane.enterAllowed || !nextLane.checkVolum(headVeh))) {
			//过于接近seg末端 且 下游seg容量已满， 需停车等待
			return 0.0;
		}
		
		if (headVeh.leading_ != null) {
			return cfFun(headVeh);
		}
		else if (((MLPSegment)headVeh.link_.getEndSegment()).endDSP - headVeh.Displacement() > 
						MLPParameter.getInstance().CELL_RSP_UPPER ||
				headVeh.segment_.isTheEnd() != 0) {
			//距离终点较远 或 处于endSegment(无限制)
			return sdPara[0];
		}
		else {
			//准备处于Link Passing的头车
			//暂时不加限制，以后的版本此处与节点状态结合，综合考虑进入同一节点的其他link的状态给出headSpeed
			return sdPara[0];
		}
	}

	public void setPartialSD(double[] para, int mask) {
		int idx = 0;
		for (int i = 0; i < 5; i++) {
			if ((1<<i & mask) != 0) {
				if (idx < para.length) {
					sdPara[i] = para[idx];
					idx += 1;
				}
				else
					System.err.println("idx越界");
			}
		}
	}
	
}
