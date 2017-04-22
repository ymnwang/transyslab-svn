/**
 *
 */
package com.transyslab.roadnetwork;
import java.util.HashMap;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import com.transyslab.commons.tools.SimulationClock;

/**
 * Vehicle
 *
 * @author YYL 2016-6-3
 */
public abstract class Vehicle extends CodedObject {

	protected int type_; // vehicle type and class

	protected int busType_; // if vehicle is a bus, type of bus != 0
							// if not a bus, type of bus = 0 (Dan)
	protected int routeID_; // if vehicle is a bus, route ID != 0
							// if not a bus, route ID = 0 (Dan)

	protected float length_; // vehicle length

	protected int attrs_; // driver attributes

	protected ODPair od_; // od pair

	protected Path path_; // path this vehicle will follow

	// index in path (list of links) if path is defined or index to
	// network link array if path is not defined.

	protected int pathIndex_;
	protected int info_; // previous received info (e.g. vms)

	protected Link nextLink_; // next link on its path
	protected float departTime_;
	protected float timeEntersLink_; // time enters current link
	protected float distance_; // distance from downstream end
	protected float mileage_; // total distance traveled
	protected float currentSpeed_; // current speed in meter/sec
	protected static int[] lastId_ = new int[Constants.THREAD_NUM]; // id of the
																	// last
																	// vehicle
																	// generated
	public Vehicle() {
		attrs_ = 0;
		type_ = 0;
		routeID_ = 0;
		length_ = (float) Constants.DEFAULT_VEHICLE_LENGTH;
	}

	// Driver atributes

	public void toggleAttr(int attr) {
		attrs_ ^= attr;
	}
	public int attr(int mask) {
		return (attrs_ & mask);
	}
	public void setAttr(int s) {
		attrs_ |= s;
	}
	public void unsetAttr(int s) {
		attrs_ &= ~s;
	}

	public float getLength() {
		return length_;
	}

	public void initialize() {

	} // called by init()

	//wym
	public void initPath(Node oriNode, Node desNode) {
		GraphPath<Node, Link> gpath = DijkstraShortestPath.findPathBetween(RoadNetwork.getInstance(), oriNode, desNode);
		path_ = new Path(gpath);
		//调试阶段暂时固定路径
		fixPath();
	}
	
	public int init(int id, int t, ODPair od, Path p) {
		HashMap<String, Integer> hm = RoadNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		int c;
		if (id > 0) { // id is specified
			c = (id > 0) ? -id : id;
			setCode(c);
		}
		else { // not specified, assign a serial number
			c = (++lastId_[threadid]);
			setCode(c);
		}

		type_ = t;
		od_ = od;

		info_ = Constants.INT_INF;
		path_ = p;
		pathIndex_ = -1;
		nextLink_ = null;

		departTime_ = (float) SimulationClock.getInstance().getCurrentTime();
		timeEntersLink_ = departTime_;

		oriNode().nOriCounts_++;
		desNode().nDesCounts_++;

		initialize(); // virtual function

		return 1;
	}
    public int init(int id, int t, float len, float dis,float departtime){
		HashMap<String, Integer> hm = RoadNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		int c;
		if (id > 0) { // id is specified
			c = (id > 0) ? -id : id;
			setCode(c);
		}
		else { // not specified, assign a serial number
			c = (++lastId_[threadid]);
			setCode(c);
		}
        type_ = t;
        od_ = VehicleTable.getInstance().getODPair();
        setPath(VehicleTable.getInstance().getPath());
        length_ = len;
        distance_ = dis;
        info_ = Constants.INT_INF;
        //初始化路径
        nextLink_ = path_.getFirstLink();

        departTime_	= departtime;
        timeEntersLink_ = departTime_;

        oriNode().nOriCounts_ ++;
        desNode().nDesCounts_ ++;

        initialize();				// virtual function

        return 1;
    }
    public int initBus(int bid, ODPair od, Path p) {
		HashMap<String, Integer> hm = RoadNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		int c;
		if (bid > 0) { // id is specified
			c = (bid > 0) ? -bid : bid;
			setCode(c);
		}
		else { // not specified, assign a serial number
			c = (++lastId_[threadid]);
			setCode(c);
		}
		type_ = 0x4;
		od_ = od;

		info_ = 0;
		path_ = p;
		pathIndex_ = -1;
		nextLink_ = null;

		departTime_ = (float) SimulationClock.getInstance().getCurrentTime();
		timeEntersLink_ = departTime_;

		oriNode().nOriCounts_++;
		desNode().nDesCounts_++;

		initialize(); // virtual function

		return 1;
	}
	// Dan - initialization of buses for bus rapid transit
	/*
	 * public int initRapidBus(int t, OD_Pair od, int rid, int bt, double hw){
	 * code_ = (++ lastId_); type_ = t; od_ = od;
	 *
	 * info_ = DefinedConstant.INT_INF;
	 *
	 * if (rid > 0 && theBusAssignmentTable != NULL) { if (!(path_ =
	 * theBusRunTable->findPath(rid))) { // cerr <<
	 * "Warning:: Unknown bus path <" // << rid << ">. "; return -1; } } else {
	 * path_ = null; }
	 *
	 * pathIndex_ = -1; nextLink_ = null;
	 *
	 * departTime_ = (float) SimulationClock.getInstance().getCurrentTime();
	 * timeEntersLink_ = departTime_;
	 *
	 * oriNode().nOriCounts_ ++; desNode().nDesCounts_ ++;
	 *
	 * theBusAssignmentTable.nBusesParsed_ ++;
	 * theBusAssignmentTable.addBRTAssignment(code_, bt, rid, hw);
	 *
	 * initialize(); // virtual function
	 *
	 * return 1; }
	 */

	// This function is called by vehicle table parser. It sould
	// returns 0 if the initialization is successful, -1 if error
	// (it causes program to quit) and 1 if warning error. A none
	// zero return value also indicate the caller to delete this
	// vehicle. The last two arguments are optional
	/*
	 * public int superInit(int id,int ori, int des,int type_id, int path_id){
	 * RN_Node o = MESO_Network.getInstance().findNode(ori); RN_Node d =
	 * MESO_Network.getInstance().findNode(des);
	 *
	 * if (o == null) { // cerr << "Error:: Unknown origin node <" << ori <<
	 * ">. "; return (-1); } else if ( d == null) { // cerr <<
	 * "Error:: Unknown destination node <" << des << ">. "; return (-1); }
	 *
	 * OD_Pair odpair(o, d); PtrOD_Pair odptr(odpair); OdPairSetType::iterator i
	 * = theOdPairs.find(odptr); if (i == theOdPairs.end()) { i =
	 * theOdPairs.insert(i, new OD_Pair(odpair)); } od_ = (*i).p();
	 *
	 * od_->oriNode()->type_ |= NODE_TYPE_ORI; od_->desNode()->type_ |=
	 * NODE_TYPE_DES;
	 *
	 * if (path_id > 0 && thePathTable != NULL) { if (!(path_ =
	 * thePathTable->findPath(path_id))) { // cerr << "Warning:: Unknown path <"
	 * // << path_id << ">. "; return -1; } } else { path_ = NULL; }
	 *
	 * theVehicleTable.nVehiclesParsed_ ++;
	 *
	 * // tomer - to allow vehicle table trips to be assigned with a path
	 *
	 * int error = init(id, type_id, od_, path_);
	 *
	 * PretripChoosePath();
	 *
	 * return (error); }
	 */
	// Dan - initialization of buses
	/*
	 * public int initBus(int bid,int ori_node_id, int des_node_id,int path_id){
	 * RN_Node o = theNetwork->findNode(ori); RN_Node d =
	 * theNetwork->findNode(des);
	 *
	 * if (o==null) { // cerr << "Error:: Unknown origin node <" << ori << ">. "
	 * ; return (-1); } else if (d==null) { // cerr <<
	 * "Error:: Unknown destination node <" << des << ">. "; return (-1); }
	 *
	 * OD_Pair odpair(o, d); PtrOD_Pair odptr(odpair); OdPairSetType::iterator i
	 * = theOdPairs.find(odptr); if (i == theOdPairs.end()) { i =
	 * theOdPairs.insert(i, new OD_Pair(odpair)); } od_ = (*i).p();
	 *
	 * od_->oriNode()->type_ |= DefinedConstant.NODE_TYPE_ORI;
	 * od_->desNode()->type_ |= DefinedConstant.NODE_TYPE_DES;
	 *
	 * if (path_id > 0 && theBusAssignmentTable != NULL) { if (!(path_ =
	 * theBusRunTable->findPath(path_id))) { // cerr <<
	 * "Warning:: Unknown bus path <" // << path_id << ">. "; return -1; } }
	 * else { path_ = NULL; }
	 *
	 * theBusAssignmentTable->nBusesParsed_ ++;
	 *
	 * // tomer - to allow vehicle table trips to be assigned with a path
	 *
	 * int error = initBus(bid, od_, path_);
	 *
	 * PretripChoosePath();
	 *
	 * return (error); }
	 */

	@Override
	public void print() {

	}

	public ODPair od() {
		return od_;
	}
	public Node desNode() {
		return od_.getDesNode();
	}
	public Node oriNode() {
		return od_.getOriNode();
	}
	public Path path() {
		return path_;
	}

	public int isType(int flag) {
		return type_ & flag;
	}
	public int types() {
		return type_;
	}
	public int getType() {
		return type_ & Constants.VEHICLE_CLASS;
	}
	public int group() {
		return type_ & Constants.VEHICLE_GROUP;
	}
	public int isGuided() {
		return (type_ & Constants.VEHICLE_GUIDED) != 0 ? 1 : 0;
	}
	public int infoType() {
		return isGuided();
	}
	
	//wym
	public void fixPath() {
		type_ |= Constants.VEHICLE_FIXEDPATH;
	}
	
	public float departTime() {
		return departTime_;
	}

	public float currentSpeed() {
		return currentSpeed_;
	}
	public float distance() {
		return distance_;
	}
	public float mileage() {
		return mileage_;
	}
	/*
	 * -------------------------------------------------------------------
	 * Returns the distance from downstream node of current link.
	 * -------------------------------------------------------------------
	 */
	public float distanceFromDownNode() {
		return (float) (getSegment().getDistance() + distance_);
	}

	// Current link the vehicle stays

	public abstract Link getLink();
	public Segment getSegment() {
		return null;
	}
	public Lane getLane() {
		return null;
	}

	// Path
	public Link nextLink() {
		return nextLink_;
	}
	// CAUTION: i has double meaning, depending on whether path is
	// defined.
	public void setPathIndex(int i) {
		pathIndex_ = i;
		if (path_ != null) { // has a path
			// i is index in path
			if (i >= 0 && i < path_.nLinks())
				nextLink_ = path_.getLink(i);
			else
				nextLink_ = null;
		}
		else { // no path
				// i is link index
			if (i >= 0 && i < RoadNetwork.getInstance().nLinks())
				nextLink_ = RoadNetwork.getInstance().getLink(i);
			else
				nextLink_ = null;
		}
	}
	public void donePathIndex() {
		pathIndex_ = -1;
		nextLink_ = null;
	}
	public int enRoute() {
		return 0;
	} // check if enroute
		// Return 1 if the link is in path, 0 if not, and -1 if unknown.

	public int isLinkInPath(Link plink, int depth /* = 0xFFFF */) {
		if (path_ != null) {
			int n = path_.nLinks();
			if (pathIndex_ + depth < n)
				n = pathIndex_ + depth;

			for (int i = pathIndex_; i < n; i++) {
				if (path_.getLink(i) == plink) {
					return 1;
				}
			}
			return 0;
		}
		else {
			if (nextLink_ == plink)
				return 1;
			else
				return -1;
		}
	}

	// These functions are called only if the path is defined.

	public void setPath(Path p) {
		path_ = p;
		pathIndex_ = 0;
	}
	public void setPath(Path p, int i) {
		path_ = p;
		setPathIndex(i);
	}
	public void advancePathIndex() {
		int i = pathIndex_ + 1;
		Link pl = path_.getLink(i);

		if (getLink() == null || (RoadNetwork.getInstance().isNeighbor(getLink(), pl)) != 0) {
			setPathIndex(i);
		}
		else if (pl != nextLink_) {
			donePathIndex();
		}
	}
	public void retrievePathIndex() {
		int i = pathIndex_ - 1;
		Link pl = path_.getLink(i);
		if (getLink() == null || RoadNetwork.getInstance().isNeighbor(pl, getLink()) != 0) {
			setPathIndex(i);
		}
	}

	public Link prevLinkOnPath() {
		int i = pathIndex_ - 2;
		return (path_.getLink(i));
	}

	// Route choice model
	/*
	 * public RN_Route routingInfo(){ if (isGuided()!=0) {
	 *
	 * if (theGuidedRoute.preTripGuidance()) {
	 *
	 * // preTripGuidance == 1 means that the Time Table stored in //
	 * theGuidedRoute is *always* used by guided vehicle throughout // the
	 * simulation
	 *
	 * return theGuidedRoute;
	 *
	 * } else {
	 *
	 * // preTripGuidance != 1 means that the Time Table stored in //
	 * theGuidedRoute is used by guided vehicles only after they // have
	 * accessed some information
	 *
	 * return attr(ATTR_ACCESSED_INFO) ? theGuidedRoute : theUnGuidedRoute; } }
	 * else { return theUnGuidedRoute; } }
	 */
	// Find the nextLink to travel
	public void OnRouteChoosePath(Node node) {
		if (path_ != null) { // path assigned
			if (isType(Constants.VEHICLE_FIXEDPATH) != 0 || // path is fixed
					enRoute() == 0) { // no enroute
				advancePathIndex();
			}
			else { // decide whether to switch
					 node.routeSwitchingModel(this,null);
			}
		}
		else { // no path assigned
				// generate route dynamically
				 node.routeGenerationModel(this);
		}
	}
	public void PretripChoosePath(ODCell od) {
		if (od.splits() != null) {
			// At origin and path and splits are specified
			// 通过splits_定义的cdf计算选择odcell路径集某一路径的概率
			Path p = od.chooseRoute(this);
			setPath(p, 0);
		}
		else if (od.nPaths() != 0) { // At origin and path specified
			// 出行过程动态更新车辆路径
			// 由于无最短路径计算，这里不更新路径，由pretrip路径决定直到车辆到达目的地
			// pathtable决定的路径只有一条
			// 改变了源程序的运行逻辑
			 od.getOriNode().routeSwitchingModel(this, od);
		}
		else { // At origin and no path specified for the OD pair
				 od.getOriNode().routeGenerationModel(this);
		}
	}

	// tomer - for the vehicles read from vehicle table file

	public void PretripChoosePath() {
		Path p = path_;
		if (p != null) { // vehicle has a path specified in the vehicle table
							// file
			// Dan: or a path specified in the bus assignment file
			setPath(p, 0);
		}
		else { // vehicle has no path specified
				// od_.getOriNode().routeGenerationModel(this);
		}
	}
	
	public Route routingInfo() {
		//暂不区分guidance
		return (Route) Route.getInstance();
	}

	// MOE

	public float timeSinceDeparture() {
		return (float) (SimulationClock.getInstance().getCurrentTime() - departTime_);
	}
	public float timeEntersLink() {
		return timeEntersLink_;
	}
	public void timeEntersLink(float arg) {
		timeEntersLink_ = arg;
	}

	// Returns the time spent in this link
	public float timeInLink() {
		return (float) (SimulationClock.getInstance().getCurrentTime() - timeEntersLink_);
	}
	public float speedInLink() {
		double WORSE_THAN_WALKING = 1.38889;
		float t = timeInLink();
		float v;
		if (t < 0.1) {
			v = currentSpeed_;
		}
		else {
			v = (float) ((getLink().length() - distanceFromDownNode()) / t);
		}
		// cout<<v<<endl;
		return (float) Math.max(v, WORSE_THAN_WALKING);
	}
	public void writePathRecord() {

	}
	// Dump current data
	public void dumpState() {

	}

	public static void resetLastId() {
		HashMap<String, Integer> hm = RoadNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		lastId_[threadid] = 0;
	}

	public static void openDepartureRecords(String filename) {

	}/*
		 * public static void nextBlockOfDepartureRecords(){ if
		 * (SimulationEngine.chosenOutput(Constants.OUTPUT_RECT_TEXT)!=0)
		 * return;
		 *
		 * if (nVehicles_!=0) { // close previous block // osDepRecords_ << '}'
		 * << endl; }
		 *
		 * // open a new block and write time tag
		 *
		 * double t = SimulationClock.getInstance().getCurrentTime();
		 * osDepRecords_ << endl << Fix(t, 0.1) << endc << '{' << endl; }
		 */
	public void saveDepartureRecord() {

	}
	public static void closeDepartureRecords() {

	}

	private static int nVehicles_ = 0;
	// private static ofstream osDepRecords_;

	// private void setNextLink();
}
