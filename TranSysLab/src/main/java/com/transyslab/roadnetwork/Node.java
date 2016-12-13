/**
 *
 */
package com.transyslab.roadnetwork;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author YYL 2016-5-24
 */

public class Node extends CodedObject {

	// 数组中的索引
	protected int index_;
	// node的类型
	protected int type_;
	// 存储终点数组的的索引（当type是NODE_TYPE_DES才有效）
	protected int destIndex_;
	// inbound link
	protected List<Link> upLinks_;
	// outbound link
	protected List<Link> dnLinks_;

	// RN_IS 实体
	protected Intersection InterS_;

	protected int state_;

	// 车辆计数
	protected int nOriCounts_;
	protected int nDesCounts_;

	public Node() {
		InterS_ = null;
		state_ = 0;
		nOriCounts_ = 0;
		nDesCounts_ = 0;
		upLinks_ = new ArrayList<Link>();
		dnLinks_ = new ArrayList<Link>();
	}

	public int type(int flag) {
		return type_ & flag;
	}
	public void setType(int t) {
		type_ = t;
	}
	public int getType() {
		return type_;
	}
	public int odType() {
		return type_ & Constants.NODE_TYPE_OD;
	}
	public int geometryType() {
		return type_ & Constants.NODE_TYPE_GEOMETRY;
	}
	public void setState(int s) {
		state_ |= s;
	}
	public void unsetState(int s) {
		state_ &= ~s;
	}
	public void setInterS(Intersection intersection) {
		InterS_ = intersection;
	}
	public Intersection getInterS() {
		return InterS_;
	}
	public int nUpLinks() {
		return upLinks_.size();
	}
	public Link getUpLink(int i) {
		return upLinks_.get(i);
	}
	public int nDnLinks() {
		return dnLinks_.size();
	}

	public Link getDnLink(int i) {
		return dnLinks_.get(i);
	}

	public void addUpLink(Link link) {
		upLinks_.add(link);
	}
	public void addDnLink(Link link) {
		dnLinks_.add(link);
	}
	// Return local index of an inbound link or -1 if 'link' is not a
	// upstream link of this node
	public int whichUpLink(Link link) {
		int i;
		for (i = nUpLinks() - 1; i >= 0; i--) {
			if (upLinks_.get(i) == link)
				break;
		}
		return i;
	}
	// Return local index of an outbound link or -1 if 'link' is not a
	// downstream link of this node
	public int whichDnLink(Link link) {
		int i;
		for (i = nDnLinks() - 1; i >= 0; i--) {
			if (dnLinks_.get(i) == link)
				break;
		}
		return i;
	}
	// Sort outbound links based on their directions. Direction is
	// represebted by the angle counter-clockwise, with 3 O'clock
	// being 0.
	public void sortUpLinks() {
		int i, j;
		Link lastarc;
		Link toparc;
		for (i = nUpLinks() - 1; i > 0; i--) {
			lastarc = upLinks_.get(i);
			for (j = i - 1; j >= 0; j--) {
				toparc = upLinks_.get(j);
				if (toparc.inboundAngle() > lastarc.inboundAngle()) {
					upLinks_.set(i, toparc);
					upLinks_.set(j, lastarc);
					lastarc = toparc;
				}
			}
		}
	}
	// Sort outbound links based on their directions. Direction is
	// represebted by the angle counter-clockwise, with 3 O'clock
	// being 0.
	public void sortDnLinks() {
		int i, j;
		Link lastarc;
		Link toparc;
		for (i = nDnLinks() - 1; i > 0; i--) {
			lastarc = dnLinks_.get(i);
			for (j = i - 1; j >= 0; j--) {
				toparc = dnLinks_.get(j);
				if (toparc.outboundAngle() > lastarc.outboundAngle()) {
					dnLinks_.set(i, toparc);
					dnLinks_.set(j, lastarc);
					lastarc = toparc;
				}
			}
		}
	}

	public int getIndex() {
		return index_;
	}
	public int getDestIndex() {
		return destIndex_;
	}

	public int init(int c, int t, String n) {
		type_ = t;
		super.setCode(c);
		super.setName(n);
		index_ = RoadNetwork.getInstance().nNodes();
		RoadNetwork.getInstance().addNode(this);
		return 0;
	}/*
		 * public void print(){
		 *
		 * }
		 */
	public void superCalcStaticInfo() {

	}

	// Dynamically generate vehicle path
	/*
	 * public void routeGenerationModel(RN_Vehicle pv){ // the link by which
	 * vehicle pv came
	 *
	 * RN_Link slink = pv.link();
	 *
	 * if (slink!=null && slink.getDnNode() != this) {
	 *
	 * // Error in calling this function
	 *
	 * pv.donePathIndex(); return; }
	 *
	 * // destination node of this vehicle
	 *
	 * RN_Node dnode = pv.desNode();
	 *
	 * if (nDnLinks() < 1 || this == dnode) {
	 *
	 * // reached destination
	 *
	 * pv.donePathIndex(); return; }
	 *
	 * double[] util = new double [DefinedConstant.MAX_NUM_OF_OUT_LINKS]; double
	 * NOT_CONNECTED = DefinedConstant.FLT_INF - 1.0;
	 *
	 * RN_Route info = pv.routingInfo();
	 *
	 * double sum = 0.0; // sum of utilities double cost; // travel time int i;
	 * int itype = pv.infoType(); float beta = Parameter.routingBeta(itype);
	 * double entry = SimulationClock.getInstance().getCurrentTime();
	 *
	 * // expected travel time from this node to destination node
	 *
	 * double cost0; RN_Link plink;
	 *
	 * if (slink != null) {
	 *
	 * cost0 = info.dnRouteTime(slink, dnode, entry); entry +=
	 * info.linkTime(slink.getIndex(), entry);
	 *
	 * } else { cost0 = DefinedConstant.FLT_INF; for (i = 0; i < nDnLinks(); i
	 * ++) { plink = getDnLink(i); cost = info.upRouteTime(plink, dnode, entry);
	 * if (cost < cost0) { cost0 = cost; } } }
	 *
	 * plink = null;
	 *
	 * for (i = 0; i < nDnLinks(); i ++) {
	 *
	 * plink = getDnLink(i);
	 *
	 * if (slink!=null && (slink.isMovementAllowed(plink))==0) {
	 *
	 * util[i] = 0.0; // Turn is not allowed
	 *
	 * } else if ((plink.isCorrect(pv))==0) {
	 *
	 * util[i] = 0.0; // Can not use the link
	 *
	 * } else if ((cost = info.upRouteTime( plink, dnode, entry)) >=
	 * NOT_CONNECTED) {
	 *
	 * util[i] = 0.0; // Not connected to destination
	 *
	 * } else if (cost < cost0 * MESO_Parameter.getInstance().validPathFactor())
	 * {
	 *
	 * // Cost1 is the travel time on next link.
	 *
	 * double cost1 = info.linkTime(plink, entry);
	 *
	 * // Cost2 is the travel time on the shorted path from the // downstream
	 * end of plink to pv's destination.
	 *
	 * double cost2 = cost - cost1;
	 *
	 * // Add the diversion penalty if change from freeway to // ramp/urban
	 *
	 *
	 * // tomer - adding restriction such that the driver doesn't choose // a
	 * link that takes it further away from the destination.
	 *
	 * if (MESO_Parameter.getInstance().rationalLinkFactor() * cost2 <= cost0) {
	 * double cost3;
	 *
	 * if (slink != null && slink.linkType() ==
	 * DefinedConstant.LINK_TYPE_FREEWAY && plink.linkType() !=
	 * DefinedConstant.LINK_TYPE_FREEWAY) { cost3 =
	 * MESO_Parameter.getInstance().diversionPenalty(); } else { cost3 = 0; }
	 *
	 * cost = cost1 + cost2 + cost3;
	 *
	 * util[i] = Math.exp(beta * cost / cost0); sum += util[i]; } } else {
	 *
	 * util[i] = 0.0; // this path is too long or contains a cycle } }
	 *
	 * // Select one of the outgoing links based on the probabilities //
	 * calculated using a logit model
	 *
	 * if (sum > DefinedConstant.DBL_EPSILON) { // At least one link is valid
	 *
	 * // a uniform (0,1] random number
	 *
	 * double rnd = ((Random)
	 * Random.getInstance().get(Random.Routing)).urandom(); double cdf; for (i =
	 * nDnLinks() - 1, cdf = util[i] / sum; i > 0 && rnd > cdf; i --) { cdf +=
	 * util[i-1] / sum; } pv.setPathIndex(getDnLink(i).getIndex());
	 *
	 * } else { // No link is valid
	 *
	 * if (slink==null || // not enter the network yet
	 * type(DefinedConstant.NODE_TYPE_EXTERNAL)!=0) {
	 *
	 * // will be removed (ori or external node)
	 *
	 * pv.donePathIndex();
	 *
	 * } else if ((i = slink.getRightDnIndex()) != 0xFF) {
	 *
	 * // choose the right most link, hopefully it is a off-ramp if // freeway
	 *
	 * pv.setPathIndex(getDnLink(i).getIndex());
	 *
	 * } else { // no where to go
	 *
	 * pv.donePathIndex(); } } }
	 *
	 * // For vehicles that already has a path, they use this function // to
	 * check whether they should keep their current paths or // enroute
	 *
	 * public void routeSwitchingModel(RN_Vehicle pv, OD_Cell od){ // the link
	 * from which the vehicle pv came /* RN_Link slink = pv.link();
	 *
	 * if (slink!=null && slink.getDnNode() != this) {
	 *
	 * // Error in calling this function
	 *
	 * pv.donePathIndex(); return; }
	 *
	 * // destination node of this vehicle
	 *
	 * RN_Node dnode = pv.desNode();
	 *
	 * // Information used to routing the vehicle
	 *
	 * RN_Route info = pv.routingInfo();
	 *
	 * RN_PathPointer pp; int flag; int i, j, n; float cost;
	 *
	 * Vector<Pointer<RN_PathPointer> ALLOCATOR> choices; Vector<float
	 * ALLOCATOR> costs;
	 *
	 *
	 * // Prepare the choice sets and find the shortest route
	 *
	 * float smallest = FLT_INF; if (slink) { // enroute for (i = 0; i <
	 * slink.nPathPointers(); i ++) { pp = slink->pathPointer(i); if
	 * (pp.desNode() == dnode) { // goes to my desination // add to my choice
	 * set cost = pp.cost(info); costs.push_back(cost); choices.push_back(pp);
	 * if (cost < smallest) { smallest = cost; } } } flag = 1; } else { // at
	 * the origin for (i = 0; i < nDnLinks(); i ++) { // check each out going
	 * link slink = dnLink(i); for (j = 0; j < slink.nPathPointers(); j ++) { //
	 * each path pp = slink.pathPointer(j);
	 *
	 * // Dan: bus run paths should not be considered
	 *
	 * if (!theBusAssignmentTable || (theBusAssignmentTable &&
	 * !theBusRunTable.findPath(pp.path().code()))) { if (pp->desNode() == dnode
	 * && // goes to my desination (!od || pp.IsUsedBy(od))) { // add to my
	 * choice set cost = pp.cost(info); costs.add(cost); choices.add(pp); if
	 * (cost < smallest) { smallest = cost; } } } } } flag = 0; }
	 *
	 * n = choices.size();
	 *
	 * if (n > 1) {
	 *
	 * // Find the utility of choosing each route
	 *
	 * double util[] = new double[n]; int itype = pv.infoType(); float beta =
	 * theBaseParameter.routingBeta(itype); float alpha =
	 * theBaseParameter.commonalityFactor(); double sum = 0.0; // sum of
	 * utilities
	 *
	 * for (i = 0; i < n; i ++) { pp = choices[i]; // diversion penalty if (flag
	 * && pp.path() != pv.path()) { cost =
	 * theBaseParameter->pathDiversionPenalty(); } else { cost = 0; } cost +=
	 * costs[i]; util[i] = exp(beta * cost / smallest + alpha * pp.cf()); sum +=
	 * util[i]; }
	 *
	 * // Select path based on the probabilities calculated using a // logit
	 * model
	 *
	 * if (sum > DBL_EPSILON) {
	 *
	 * // a uniform (0,1] random number
	 *
	 * double rnd = theRandomizers[Random::Routing].urandom();
	 *
	 * double cdf; for (i = n - 1, cdf = util[i] / sum; i > 0 && rnd > cdf; i
	 * --) { cdf += util[i-1] / sum; } } else { i =
	 * theRandomizers[Random::Routing].urandom(n); }
	 *
	 * pp = choices[i]; pv.setPath(pp->path(), pp.position() + flag);
	 *
	 * delete [] util;
	 *
	 * } else if (n) { // n == 1
	 *
	 * pp = choices[0]; pv.setPath(pp->path(), pp.position() + flag);
	 *
	 * } else {
	 *
	 * // No available path at this node. Switch to route generation // model
	 *
	 * pv.setPath(null); routeGenerationModel(pv); } }
	 */

}
