/**
 *
 */
package com.transyslab.roadnetwork;
import java.util.*;

/**
 * //-------------------------------------------------------------------- //
 * CLASS NAME: RN_CtrlStation -- a surveillance station consists of // one or
 * more signals. Ctrlerllace station is stored in a sorted // list in each link
 * according to their distance from the end of the // link. The sorting is in
 * descending order (Upstream = // LongerDistance = Top) //------
 *
 * @author YYL 2016-6-5
 */
public class CtrlStation extends CodedObject {
	protected static float maxVisibility_;
	protected int type_; // signal type
	protected Segment segment_; // pointer to segment
	protected float distance_; // distance from the link end
	protected float visibility_; // length of detection zone
	protected float position_; // position in % from segment end
	protected Vector<Signal> signals_; // array of pointers to signals

	public CtrlStation() {

	}/*
		 * public int type() { return (CTRL_SIGNAL_TYPE & type_); } public int
		 * isLinkWide() { return (CTRL_LINKWIDE & type_); }
		 *
		 * public RN_Segment segment() { return segment_; } public RN_Link
		 * getLink(){ return segment_.getLink(); }
		 *
		 * public int nLanes(){ return segment_.nLanes(); }
		 *
		 * // Returns pointer to the signal in ith lane. It may be NULL if //
		 * there is no signal in the ith lane.
		 *
		 * public RN_Signal signal(int i){ //Î´´¦Àí if (isLinkWide()>0) return
		 * signals_.get(0); else return signals_.get(i); }
		 *
		 * // Connect ith point to the signal
		 *
		 * public void signal(int i, RN_Signal s){ if (isLinkWide()>0) i = 0;
		 * signals_.add(i,s); }
		 *
		 * public float distance() { return distance_; } public float
		 * visibility() { return visibility_; } public void visibility(float d){
		 * visibility_ = d; if (visibility_ > maxVisibility_) { maxVisibility_ =
		 * visibility_; } } public float position() { return position_; }
		 *
		 * public int initWithoutInsert(int ty, float vis, int seg, float pos){
		 * switch (ty) { case CTRL_PS: case CTRL_TS: case CTRL_VSLS: case
		 * CTRL_VMS: { type_ = (ty | CTRL_LINKWIDE); break; } default: { type_ =
		 * ty; break; } }
		 *
		 * if (!(segment_ = theNetwork.findSegment(seg))) { // cerr <<
		 * "Error:: Unknown segment <" << seg << ">. "; return -1; }
		 *
		 * vis *= theBaseParameter.lengthFactor(); vis *=
		 * theBaseParameter.visibilityScaler(); visibility(vis);
		 *
		 * position_ = (float) (1.0 - pos); // position in % from segment end
		 *
		 * distance_ = segment_.getDistance() + position_ *
		 * segment_.getLength();
		 *
		 * if (isLinkWide()>0) { // signals_.reserve(1); signals_.add(0,null); }
		 * else { int n = segment_.nLanes(); // signals_.reserve(n); while (n >
		 * 0) { n --; signals_.add(n,null); } }
		 *
		 * return 0; }
		 */
	/*
	 * public int init(int ty, float vis, int seg, float pos){ // This function
	 * is called by RN_Parser to set information of a // ctrleillance station
	 * from network database. It returns zero if no // error, -1 if fatal error
	 * or a positive number if warning error. /* if (ToolKit::debug()) { cout <<
	 * indent << "<" << ty << endc << vis << endc << seg << endc << pos << ">"
	 * << endl; }
	 *
	 * int err = initWithoutInsert(ty, vis, seg, pos); if (err < 0) return err;
	 *
	 * addIntoNetwork();
	 *
	 * return err; }
	 */
	/*
	 * public void addIntoNetwork(){ /*static int serial_no = 0;
	 * getLink().getCtrlStationList().add(this);
	 * theNetwork.lastCtrlStation(this); code_ = ++serial_no; }
	 */

	@Override
	public void print() {

	}

	@Override
	public int cmp(CodedObject other) {
		CtrlStation ctrl = (CtrlStation) other;
		if (distance_ < ctrl.distance_)
			return 1;
		else if (distance_ > ctrl.distance_)
			return -1;
		else
			return 0;
	}
	@Override
	public int cmp(int c) {
		return this.cmp(c);
	}
}
