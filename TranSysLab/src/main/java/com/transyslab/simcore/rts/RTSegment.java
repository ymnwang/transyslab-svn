package com.transyslab.simcore.rts;

import com.transyslab.roadnetwork.Segment;

/**
 * Created by ITSA405-35 on 2018/5/28.
 */
public class RTSegment extends Segment {
	private double startDSP;//在当前link中的起点里程
	private double endDSP;//当前link中的segment终点里程
	public void setStartDSP(double startDSP){
		this.startDSP = startDSP;
	}
	public void setEndDSP(double endDSP){
		this.endDSP = endDSP;
	}
	public double getStartDSP(){
		return this.startDSP;
	}
	public double getEndDSP(){
		return this.endDSP;
	}
	@Override
	public RTSegment getUpSegment() {
		return (RTSegment) super.getUpSegment();
	}

	@Override
	public RTSegment getDnSegment(){
		return (RTSegment) super.getDnSegment();
	}
	@Override
	public RTLane getLane(int index) {
		return (RTLane) super.getLane(index);
	}
	public boolean isEndSeg() {
		return link.getEndSegment().equals(this);
	}

	public boolean isStartSeg() {
		return link.getStartSegment().equals(this);
	}
	public void setSucessiveLanes() {
		RTSegment dnSeg = getDnSegment();
		if (dnSeg != null) {
			dealSuccessive(dnSeg);
			return;
		}
		int ndnLinks = link.nDnLinks();
		if (ndnLinks > 0) {
			for (int i = 0; i < ndnLinks; i++) {
				RTSegment startSeg = (RTSegment) link.dnLink(i).getStartSegment();
				dealSuccessive(startSeg);
			}
		}
	}
	private boolean checkConnected(RTSegment dnSeg) {
		boolean ans = false;
		for (int i = 0; i < nLanes() && (!ans); i++) {
			for (int j = 0; j < dnSeg.nLanes() && (!ans); j++) {
				ans |= getLane(i).successiveDnLanes.contains(dnSeg.getLane(j));
			}
		}
		return ans;
	}

	private void dealSuccessive(RTSegment dnSeg) {
		//若xml中有此信息，则初始化过程已添加，不需要进行推断。要求将该seg所有lane关于dnSeg的successiveDnLane全部指定好。
		if (checkConnected(dnSeg))
			return;
		int nLanes = nLanes();
		if (nLanes == dnSeg.nLanes()) {
			for (int i = 0; i < nLanes; i++) {
				getLane(i).successiveDnLanes.add(dnSeg.getLane(i));
				dnSeg.getLane(i).successiveUpLanes.add(getLane(i));
			}
		}
		else {
			int m = Math.min(nLanes, dnSeg.nLanes());
			double sumLF = 0.0, sumLFSquared = 0.0, sumRT = 0.0, sumRTSquared = 0.0;
			for (int i = 0; i < m; i++) {
				double tmpLF = getLane(i).getEndPnt().distanceSquared(dnSeg.getLane(i).getStartPnt());
				double tmpRT = getLane(nLanes -1-i).getEndPnt().distanceSquared(dnSeg.getLane(dnSeg.nLanes()-1-i).getStartPnt());
				sumLFSquared += tmpLF;
				sumLF += Math.sqrt(tmpLF);
				sumRTSquared += tmpRT;
				sumRT += Math.sqrt(tmpRT);
			}
			double coefVarLF = Math.sqrt(sumLFSquared/m - Math.pow(sumLF/m, 2))/(sumLF/m);
			double coefVarRT = Math.sqrt(sumRTSquared/m - Math.pow(sumRT/m, 2))/(sumRT/m);
			if (coefVarLF <= coefVarRT) {
				for (int i = 0; i < m; i++) {
					getLane(i).successiveDnLanes.add(dnSeg.getLane(i));
					dnSeg.getLane(i).successiveUpLanes.add(getLane(i));
				}
			}
			else {
				for (int i = 0; i < m; i++) {
					getLane(nLanes -1-i).successiveDnLanes.add(dnSeg.getLane(dnSeg.nLanes()-1-i));
					dnSeg.getLane(dnSeg.nLanes()-1-i).successiveUpLanes.add(getLane(nLanes -1-i));
				}
			}
		}
	}

}
