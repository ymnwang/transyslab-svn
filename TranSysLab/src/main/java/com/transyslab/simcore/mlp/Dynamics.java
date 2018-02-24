package com.transyslab.simcore.mlp;

public class Dynamics {
	protected MLPLink link;
	protected MLPParameter mlpParameter;
	public double [] linkCharacteristics;//[0]vf_SD; [1]VMin; [2]KJam; [3]Alpha; [4]Beta; [5]vf_CF
	//public double [] cfPara;//[0]Critical Gap; [1]dUpper;
	public Dynamics(MLPLink theLink){
		link = theLink;
		mlpParameter = (MLPParameter) theLink.getNetwork().getSimParameter();
		linkCharacteristics = ((MLPParameter) theLink.getNetwork().getSimParameter()).getSDPara();
	}
	public Dynamics(MLPLink theLink, double [] SDParas) {
		link = theLink;
		mlpParameter = (MLPParameter) theLink.getNetwork().getSimParameter();
		linkCharacteristics = SDParas;
	}
	public double sdFun(double k) {
		if (k <= linkCharacteristics[2]){
			 //vMin + (vMax - vMin)*(1-(K/kJam).^a).^b;
			double ans = linkCharacteristics[1] + (linkCharacteristics[0]- linkCharacteristics[1]) * Math.pow(1.0-Math.pow(k/ linkCharacteristics[2], linkCharacteristics[3]), linkCharacteristics[4]);
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
		double gap = theVeh.leading.Displacement() - theVeh.leading.getLength() - theVeh.Displacement();
		double vlead = (double) theVeh.leading.getCurrentSpeed();
		double upperGap = mlpParameter.CF_FAR;
		if(gap < mlpParameter.CF_NEAR) {
			return vlead;
		}
		else if (gap<upperGap) {
			double r = gap/upperGap;
			//return r * linkCharacteristics[0] + (1.0-r) * vlead;
			double ans = r * mlpParameter.maxSpeed(gap) + (1.0-r) * vlead;
//			if (Double.isNaN(ans))
//				System.out.println("BUG CF函数输出异常");
			return ans;
		}
		else {
			return linkCharacteristics[5];
		}
	}
	public double updateHeadSpd(MLPVehicle headVeh){
		MLPLane nextLane = headVeh.lane.connectedDnLane;
		if (headVeh.getDistance() < MLPParameter.SEG_NEAR &&
				nextLane != null && (!nextLane.enterAllowed || !nextLane.checkVolum(headVeh))) {
			//过于接近seg末端 且 下游seg容量已满， 需停车等待
			return 0.0;
		}
		
		if (headVeh.leading != null) {
			return cfFun(headVeh);
		}
		else if (((MLPSegment)headVeh.link.getEndSegment()).endDSP - headVeh.Displacement() >
						mlpParameter.CELL_RSP_UPPER ||
				headVeh.segment.isTheEnd() != 0) {
			//距离终点较远 或 处于endSegment(无限制)
			return linkCharacteristics[0];
		}
		else {
			//准备处于Link Passing的头车
			//TODO: 暂时不加限制，以后的版本此处与节点状态结合，综合考虑进入同一节点的其他link的状态给出headSpeed
			return linkCharacteristics[0];
		}
	}

	public void setPartialCharacteristics(double[] para, int mask) {
		int idx = 0;
		for (int i = 0; i < 5; i++) {
			if ((1<<i & mask) != 0) {
				if (idx < para.length) {
					linkCharacteristics[i] = para[idx];
					idx += 1;
				}
				else
					System.err.println("idx越界");
			}
		}
	}
	
}
