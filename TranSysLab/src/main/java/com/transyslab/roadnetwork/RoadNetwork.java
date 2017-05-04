/**
 *
 */
package com.transyslab.roadnetwork;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import com.transyslab.commons.io.JdbcUtils;
import com.transyslab.commons.tools.EngTread;
import com.transyslab.commons.tools.SimulationClock;

/**
 * roadnetwork
 *
 * @author YYL 2016-6-4
 */
public class RoadNetwork extends SimpleDirectedWeightedGraph<Node, Link>{

	// These default initial number of objects

	protected String description_; // description of the network

	// int nNodes_, nLinks_, nSegments_, nLanes_, nLabels_;
	// int nSensors_, nSignals_;

	protected int nDestNodes_;
	protected int nConnectors_, nProhibitors_;
	protected int nStdSignals_;
	protected int nTollBooths_;
	protected int nBusStops_; // margaret
	protected int nSelectedSegs_;
	protected int nSurvStation_;

	protected List<Node> nodes_ = new ArrayList<Node>();
	protected List<Link> links_ = new ArrayList<Link>();
	protected List<Segment> segments_ = new ArrayList<Segment>();
	protected List<Lane> lanes_ = new ArrayList<Lane>();
	protected List<Boundary> boundarys_ = new ArrayList<Boundary>();
	protected List<GeoSurface> surfaces_ = new ArrayList<GeoSurface>();
	protected List<Label> labels_ = new ArrayList<Label>();

	protected List<Sensor> sensors_ = new ArrayList<Sensor>();
	protected List<Signal> signals_ = new ArrayList<Signal>();
	protected List<BusStop> busstops_ = new ArrayList<BusStop>();
	protected List<SurvStation> survStations_ = new ArrayList<SurvStation>();
	public Random sysRand;
	// protected Vector < Vector <RN_Sensor > > sensorsOfType_; //IEM(May2) each
	// vector of sensors is for one interval type
	// protected void sortSensors() {}; //IEM(May2) Fills sensorsOfType.
	// Implemented in TS_Network :P

	protected List<SdFn> sdFns_ = new ArrayList<SdFn>();

	// tomer

	protected List<Intersection> IS_;

	// **

	protected WorldSpace worldSpace_ = new WorldSpace();

	protected SurvStation lastSurvStation_;
	protected CtrlStation lastCtrlStation_;

	// Some basic statistics of the network
	// 更改为public
	public double totalLinkLength_;
	public double totalLaneLength_;

	public int drivingDirection_; // tomer - should be protected but...
	protected float maxCapacity_;

	// public static String name_; // network file name
	// public static String getName() { return name_; }
	// public static char **nameptr() { return &name_; }
	public RoadNetwork() {
		super(Link.class);
		description_ = null;
		// name_ = "network.xml";
		nDestNodes_ = 0;
		nStdSignals_ = 0;
		nTollBooths_ = 0;
		nBusStops_ = 0; // margaret
		lastSurvStation_ = null;
		lastCtrlStation_ = null;
		nConnectors_ = 0;
		nProhibitors_ = 0;
		totalLinkLength_ = 0;
		totalLaneLength_ = 0;
		sysRand = new Random();
	}
	public static RoadNetwork getInstance() {
		RoadNetworkPool pool = RoadNetworkPool.getInstance();		
		if (pool != null) {
			HashMap<String, Integer> hm = pool.getHashMap();
			int threadid = hm.get(Thread.currentThread().getName());
			return RoadNetworkPool.getInstance().getNetwork(threadid);
		}
		else {
			return ((EngTread) Thread.currentThread()).network;
		}
		
	}
	/*
	 * public void setMaxCapacity(float mc){ maxCapacity_ = mc; } public float
	 * getMaxCapacity(){ return maxCapacity_ ; }
	 */
	/*
	 * public void updateCapacity(int index, float c){
	 * sdFns_.get(index).setCapacity(c); }
	 */
	public int initNetwork(String name) {

		return 1;
	}
	public WorldSpace getWorldSpace() {
		return worldSpace_;
	}

	// tomer

	public Intersection newIS() {
		return new Intersection();
	}

	// **

	public void setDrivingDirection(int k) {
		drivingDirection_ = k;
	}


	// 新增方法，供MESO_Segment调用
	public double getTotalLinkLength() {
		return totalLinkLength_;
	}
	public double getTotalLaneLength() {
		return totalLaneLength_;
	}

	// tomer - where is the capacity() function?

	public int nMaxIS() {
		return IS_.size();
	}

	// **

	// These are the numbers parsed from network databases

	public int nLabels() {
		return labels_.size();
	}
	public int nNodes() {
		return nodes_.size();
	}
	public int nLinks() {
		return links_.size();
	}
	public int nSegments() {
		return segments_.size();
	}
	public int nLanes() {
		return lanes_.size();
	}
	public int nBoundarys() {
		return boundarys_.size();
	}
	public int nSurfaces(){
		return surfaces_.size();
	}
	public int nSensors() {
		return sensors_.size();
	}
	// public int nSensors(int intervalType) { if (nSensors() == 0) {return 0;}
	// else {return sensorsOfType_.get(intervalType).size();} }
	// IEM(May2) # of sensors of a given interval type
	// IEM(Jul25) If there are no sensors at all, returns 0 no matter what (that
	// way, no segfaults)
	public int nSignals() {
		return signals_.size();
	}
	public int nSdFns() {
		return sdFns_.size();
	}
	public int nSurvStation() {
		return survStations_.size();
	}
	// tomer

	public int nIS() {
		return IS_.size();
	}

	// **

	public int nStdSignals() {
		return nStdSignals_;
	}
	public int nTollBooths() {
		return nTollBooths_;
	}
	public int nBusStops() {
		return nBusStops_;
	} // margaret
	public int nDestNodes() {
		return nDestNodes_;
	}

	// This is called when adding incidents

	public List getSignals() {
		return signals_;
	}

	// These takes an index and return pointer to a object

	public Label getLabel(int i) {
		return labels_.get(i);
	}
	public Node getNode(int i) {
		return nodes_.get(i);
	}
	public Link getLink(int i) {
		return links_.get(i);
	}
	public Segment getSegment(int i) {
		return segments_.get(i);
	}
	public Lane getLane(int i) {
		return lanes_.get(i);
	}
	public Sensor getSensor(int i) {
		return sensors_.get(i);
	}
	// public RN_Sensor getSensor(int intervalType, int i) { return
	// sensorsOfType_.get(intervalType).get(i); } //IEM(May2)
	public Signal getSignal(int i) {
		return signals_.get(i);
	}
	public SdFn getSdFn(int i) {
		return sdFns_.get(i);
	}

	// tomer

	public Intersection getIS(int i) {
		return IS_.get(i);
	}

	// **

	public Label findLabel(int c) {
		ListIterator<Label> i = labels_.listIterator();// <RN_Label>::iterator
														// i;
		while (i.hasNext()) {
			Label templabel = i.next();
			if (templabel.cmp(c) == 0) {
				// <c,return -1;>c,return 1;=c return 0;
				return templabel;
			}
		}
		return null;
	}
	public Node findNode(int c) {
		ListIterator<Node> i = nodes_.listIterator();
		while (i.hasNext()) {
			Node templabel = i.next();
			if (templabel.cmp(c) == 0) {
				// <c,return -1;>c,return 1;=c return 0;
				return templabel;
			}
		}
		return null;
	}
	public Link findLink(int c) {
		ListIterator<Link> i = links_.listIterator();
		while (i.hasNext()) {
			Link templabel = i.next();
			if (templabel.cmp(c) == 0) {
				// <c,return -1;>c,return 1;=c return 0;
				return templabel;
			}
		}
		return null;
	}
	public Segment findSegment(int c) {
		ListIterator<Segment> i = segments_.listIterator();
		while (i.hasNext()) {
			Segment templabel = i.next();
			if (templabel.cmp(c) == 0) {
				// <c,return -1;>c,return 1;=c return 0;
				return templabel;
			}
		}
		return null;
	}
	public Lane findLane(int c) {
		ListIterator<Lane> i = lanes_.listIterator();
		while (i.hasNext()) {
			Lane templabel = i.next();
			if (templabel.cmp(c) == 0) {
				// <c,return -1;>c,return 1;=c return 0;
				return templabel;
			}
		}
		return null;
	}
	/*
	public SurvStation findSurvStation(int c) {
		ListIterator<SurvStation> i = survStations_.listIterator();
		while (i.hasNext()) {
			SurvStation templabel = i.next();
			if (templabel.cmp(c) == 0) {
				// <c,return -1;>c,return 1;=c return 0;
				return templabel;
			}
		}
		return null;
	}
	/*
	 * public RN_Sensor findSensor(int c){ ListIterator<RN_Sensor> i =
	 * sensors_.listIterator(); while(i.hasNext()){ RN_Sensor templabel =
	 * i.next(); if(templabel.cmp(c)==0){ //<c,return -1;>c,return 1;=c return
	 * 0; return templabel; } } return null; }
	 */
	public Signal findSignal(int c) {
		ListIterator<Signal> i = signals_.listIterator();
		while (i.hasNext()) {
			Signal templabel = i.next();
			if (templabel.cmp(c) == 0) {
				// <c,return -1;>c,return 1;=c return 0;
				return templabel;
			}
		}
		return null;
	}
	public void updateSurvStationMeasureTime() {
		ListIterator<SurvStation> i = survStations_.listIterator();
		while (i.hasNext()) {
			SurvStation temp = i.next();
			/*if (temp.measureTime_ < SimulationClock.getInstance().getCurrentTime()) {
				temp.nextMeasureTime();*/
				temp.aggregate();
		}

	}
	/*
	 * public RN_BusStop findBusStop(int c){ ListIterator<RN_BusStop> i =
	 * busstops_.listIterator(); while(i.hasNext()){ RN_BusStop templabel =
	 * i.next(); if(templabel.cmp(c)==0){ //<c,return -1;>c,return 1;=c return
	 * 0; return templabel; } } return null; }
	 */

	// tomer
	/*
	 * public RN_IS findIS(int c){ ListIterator<RN_IS> i = IS_.listIterator();
	 * while(i.hasNext()){ RN_IS templabel = i.next(); if(templabel.cmp(c)==0){
	 * //<c,return -1;>c,return 1;=c return 0; return templabel; } } return
	 * null; }
	 */

	// **

	// These return the last object in each class. Used mostly by RN_Parser.

	public Label lastLabel() {
		return labels_.get((labels_.size() - 1));
	}
	public Node lastNode() {
		return nodes_.get((nodes_.size() - 1));
	}
	public Link lastLink() {
		return links_.get((links_.size() - 1));
	}
	public Segment lastSegment() {
		return segments_.get((segments_.size() - 1));
	}
	public Lane lastLane() {
		return lanes_.get((lanes_.size() - 1));
	}
	public Sensor lastSensor() {
		return sensors_.get((sensors_.size() - 1));
	}
	public Signal lastSignal() {
		return signals_.get((signals_.size() - 1));
	}
	public SurvStation lastSurvStation() {
		return survStations_.get((survStations_.size() - 1));
	}

	// tomer - again where is the back(0 function defined?
/*
	public Intersection lastIS() {
		return IS_.lastElement();
	}*/

	// **

	public List<SurvStation> getSurvStations() {
		return survStations_;
	}
	public List<Boundary> getBoundarys() {
		return boundarys_;
	}
	public List<GeoSurface> getSurfaces(){
		return surfaces_;
	}
	public SurvStation getLastSurvStation() {
		return lastSurvStation_;
	}
	public CtrlStation getLastCtrlStation() {
		return lastCtrlStation_;
	}
	public void setLastCtrlStation(CtrlStation ctrl) {
		lastCtrlStation_ = ctrl;
	}
	public void setLastSurvStation(SurvStation surv) {
		lastSurvStation_ = surv;
	}

	public void addNode(Node i) {
		nodes_.add(i);
	}
	public void addLink(Link i) {
		links_.add(i);
	}
	public void addSegment(Segment i) {
		segments_.add(i);
	}
	public void addLane(Lane i) {
		lanes_.add(i);
	}
	public void addBoundary(Boundary i) {
		boundarys_.add(i);
	}
	public void addSurface(GeoSurface i){
		surfaces_.add(i);
	}
	public void addLabel(Label i) {
		labels_.add(i);
	}
	public void addSensor(Sensor i) {
		sensors_.add(i);
	}
	public void addSignal(Signal i) {
		signals_.add(i);
	}
	public void addBusStop(BusStop i) {
		busstops_.add(i);
	}
	public void addSurvStation(SurvStation i) {
		survStations_.add(i);
	}

	// tomer - again where is push_back()?

	public void addIS(Intersection i) {
		IS_.add(i);
	}

	// **

	// Connects lane 'up' with lane 'dn'. Return -1 if error, 1 if these
	// two lanes are already connected, or 0 if success.
	public int addLaneConnector(int up, int dn) {

		Lane ulane, dlane;
		if ((ulane = findLane(up)) == null) {
			// cerr << "Error:: unknown upstream lane <" << up << ">. ";
			return -1;
		}
		else if ((dlane = findLane(dn)) == null) {
			// cerr << "Error:: unknown upstream lane <" << dn << ">. ";
			return -1;
		}

		// Check if this connector make sense

		if (isNeighbor(ulane.getSegment(), dlane.getSegment()) == 0) {
			// cerr << "Error:: lanes <" << up << "> and <" << dn << "> "
			// << "are not neighbors. ";
			return -1;
		}

		if (ulane.findInDnLane(dn) != null || dlane.findInUpLane(up) != null) {
			// cerr << "Warning:: lanes <" << up << "> and <" << dn << "> "
			// << "are already connected. ";
			return 1;
		}
		ulane.dnLanes_.add(dlane);
		dlane.upLanes_.add(ulane);

		nConnectors_++;

		return 0;
	}
	// Exclude turn movements from link 'up' to link "dn". Returns 0 if
	// it success, -1 if error, or 1 if the turn is already excluded.
	public int addTurnProhibitor(int up, int dn) {
		/*
		 * if (ToolKit::debug()) { cout << indent << "<" << up << endc << dn <<
		 * ">" << endl; }
		 */

		Link ulink, dlink;
		if ((ulink = findLink(up)) == null) {
			// cerr << "Error:: unknown upstream link <" << up << ">. ";
			return -1;
		}
		else if ((dlink = findLink(dn)) == null) {
			// cerr << "Error:: unknown upstream link <" << dn << ">. ";
			return -1;
		}

		// Check if this prohibitor make sensor

		if (isNeighbor(ulink, dlink) <= 0) {
			// cerr << "Error:: links <" << up << "> and <" << dn << "> "
			// << "are not neighbors. ";
			return -1;
		}

		ulink.excludeTurn(dlink);

		nProhibitors_++;

		return 0;
	}

	public int isNeighbor(Link s1, Link s2) {
		if (s1.getDnNode().whichDnLink(s2) < 0)
			return 0;
		return 1;

	}
	public int isNeighbor(Segment s1, Segment s2) {
		if (s1.getLink() == s2.getLink()) {
			if ((s1.getIndex() + 1) == s2.getIndex())
				return 1;
			else
				return 0;
		}
		else if (s1.getDnSegment() != null || s2.getUpSegment() != null) {
			return 0;
		}
		else if (isNeighbor(s1.getLink(), s2.getLink()) == 0) {
			return 0;
		}
		return 1;
	}
	// 更新速密函数
	public void updateSdFns(float alpha, float beta) {
		if (nSdFns() > 0) {
			sdFns_ = null;
			sdFns_ = new Vector<SdFn>();
			sdFns_.add(new SdFnNonLinear(alpha, beta));
		}
		else {
			System.out.println("warning: no speed-density function to update");
			sdFns_ = null;
			sdFns_.add(new SdFnNonLinear());
		}
	}

	// Before we use the parsed network, this function must called to
	// calculate some static information, sort objects, etc.
	// --------------------------------------------------------------------
	// Requires: Called after network database has been sucessfully parsed
	// Modifies: variables in link and node
	// Effects : Sorts links and calculates topology variables.
	// --------------------------------------------------------------------
	public void superCalcStaticInfo() {
		int i;

		// Create a default speed density function if no one is defined
		// 输入
		if (nSdFns() <= 0) {
			sdFns_.add(new SdFnNonLinear());
		}
		// sdFns_.add(new
		// RN_SdFnNonLinear(0.5f,0.01116f,155.4626f,0.4458f,1.0252f));
		// sdFns_.add(new
		// RN_SdFnNonLinear(0.5f,0.0199f,118.5746f,0.6815f,0.9952f));
		// sdFns_.add(new
		// RN_SdFnNonLinear(0.5f,2.83f,149.5334f,1.3969f,6.2228f));
		// sdFns_.add(new RN_SdFnNonLinear());

		// Calculate the arc information in worldSpace

		for (i = 0; i < nSegments(); i++) {

			// Make end point of each pair of connected segments snapped at
			// the same point

			segments_.get(i).snapCoordinates();
		}

		// Create the world space

		worldSpace_.createWorldSpace();

		for (i = 0; i < nSegments(); i++) {

			// Generate arc info such as angles and length from the two
			// endpoints and bulge. This function also convert the
			// coordinates from database format to world space format

			segments_.get(i).calcArcInfo(worldSpace_);
		}
		// Boundary 位置平移
		for (i = 0; i < nBoundarys(); i++) {
			boundarys_.get(i).translateInWorldSpace(worldSpace_);
		}
		// kerbpoint 位置平移,创建包围盒
		for(i=0;i < nSurfaces(); i++){
//			surfaces_.get(i).translateInWorldSpace(worldSpace_);
//			surfaces_.get(i).createAabBox();
		}
		// Sort outgoing and incoming arcs at each node.
		// Make sure RN_Link::comp() is based on angle.

		for (i = 0; i < nNodes(); i++) {
			nodes_.get(i).sortUpLinks();
			nodes_.get(i).sortDnLinks();
		}

		// Set destination index of all destination nodes

		for (i = 0; i < nNodes(); i++) {
			nodes_.get(i).destIndex_ = -1;
		}
		for (i = nDestNodes_ = 0; i < nNodes(); i++) {
			if ((nodes_.get(i).getType() & Constants.NODE_TYPE_DES) != 0)
				nodes_.get(i).destIndex_ = nDestNodes_++;
		}
		if (nDestNodes_ == 0) {
			// cerr << "Warning:: Destination nodes not defined." << endl
			// << "All nodes are treated as destinations for "
			// << "shortest path calculations." << endl;
			for (i = 0; i < nNodes(); i++) {
				nodes_.get(i).destIndex_ = nDestNodes_++;
			}
		}

		// Set upLink and dnLink indices

		for (i = 0; i < nLinks(); i++) {
			links_.get(i).calcIndicesAtNodes();
		}

		// Set variables in links

		for (i = 0; i < nLinks(); i++) {
			links_.get(i).calcStaticInfo();
		}

		// Set variables in segments

		for (i = 0; i < nSegments(); i++) {
			segments_.get(i).calcStaticInfo();
		}

		// Set variables in lanes
		// 增加坐标平移操作
		for (i = 0; i < nLanes(); i++) {
			lanes_.get(i).calcStaticInfo(worldSpace_);
		}
	}
	// public int calcnSelectedSegs();

	public int drivingDirection() {
		return drivingDirection_;
	}

	// Control and surveillance devices are stored in each link. But
	// each segment also has a pointer to the first device. The
	// following two function prepares the device pointers in segments.
	/*
	 * public void assignSurvListInSegments(){ for (int i = 0; i < nLinks(); i
	 * ++) { getLink(i).assignSurvListInSegments(); } } public void
	 * assignCtrlListInSegments(){ for (int i = 0; i < nLinks(); i ++) {
	 * getLink(i).assignCtrlListInSegments(); } }
	 */

	// Calculate the commonality factors for route choice model
	/*
	 * public void calcPathCommonalityFactors(){ // if (thePathTable == null)
	 * return;
	 *
	 * // updateProgress("Calculating path commonality factors ...");
	 *
	 * for (int i = 0; i < nLinks(); i ++) {
	 * getLink(i).calcPathCommonalityFactors(); } //
	 * printPathCommonalityFactors(); }
	 */
	public void printPathCommonalityFactors() {

	}

	// Save the network database. The result can be reloaded by the
	// network parser.
	/*
	 * public int save(){
	 *
	 * }
	 */

	// This prints the network objects for debugging.

	public void printBasicInfo() {

	}

	public void print() {

	}
	public void printLabels() {

	}
	public void printNodes() {

	}
	public void printLinks() {

	}
	public void printConnectors() {

	}
	public void printProhibitors() {

	}
	public void printSensors() {

	}
	public void printSignals() {

	}
	public void printTollPlazas() {

	}
	public void printBusStops() {// margaret
	}
	public void printSdFns() {

	}

	// tomer - I commented this out now because I don't use it and it gives
	// problems

	// virtual void printIS(ostream &os = cout);

	// **
	/*
	 *
	 * public int outputLinkTravelTimes(){
	 *
	 * } public int outputReloadableLinkTravelTimes(){
	 *
	 * }
	 */
	public void outputLinkFlowPlusTravelTimes(int runtimes) {
		for (int i = 0; i < nLinks(); i++) {
			try {
				getLink(i).printFlowPlusTravelTimes(runtimes);
			}
			catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
	}
	public void outputLinkDataToOracle() {
		String sql = "insert into MESO_OUTPUT_TEST (LinkID, CTime, Flow, TravelTime) values (?, ?, ?, ?)";
		Connection con;
		try {
			con = JdbcUtils.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			for (int i = 0; i < nLinks(); i++) {
				getLink(i).outputToOracle(ps);
			}
			JdbcUtils.release(con, null, ps);
		}
		catch (ClassNotFoundException | SQLException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
	public void outputTaskSegmentDataToOracle() {
		String sql = "insert into sa.Sim_Task_ArcState(SimTaskID, ArcID, CTime, LaneCount,Flow, Velocity,Density) values (?, ?, ?, ?, ?, ?, ?)";
		Connection con;
		try {
			con = JdbcUtils.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			for (int i = 0; i < nSegments(); i++) {
				getSegment(i).outputToOracle(ps);
			}
			JdbcUtils.release(con, null, ps);
		}
		catch (ClassNotFoundException | SQLException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
	public void outputModelSegmentDataToOracle() {
		String sql = "insert into sa.Sim_Model_ArcState(SimModelID, ArcID, CTime, LaneCount,Flow, Velocity,Density) values (?, ?, ?, ?, ?, ?, ?)";
		Connection con;
		try {
			con = JdbcUtils.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			for (int i = 0; i < nSegments(); i++) {
				getSegment(i).outputToOracle(ps);
			}
			JdbcUtils.release(con, null, ps);
		}
		catch (ClassNotFoundException | SQLException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
	/*
	public void outputTaskSensorDataToOracle() {
		String sql = "insert into sa.Sim_Task_DetState(SimTaskID,DetID, DetBelong, FTime, TTime,LaneCount,Flow, Velocity) values (?, ?, ?, ?, ?, ?, ?, ?)";
		Connection con;
		try {
			con = JdbcUtils.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			for (int i = 0; i < nSensors(); i++) {
				getSensor(i).outputToOracle(ps);
			}
			JdbcUtils.release(con, null, ps);
		}
		catch (ClassNotFoundException | SQLException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
	public void outputModelSensorDataToOracle() {
		String sql = "insert into sa.Sim_Model_DetState(SimModelID,DetID, DetBelong, FTime, TTime,LaneCount,Flow, Velocity) values (?, ?, ?, ?, ?, ?, ?, ?)";
		Connection con;
		try {
			con = JdbcUtils.getConnection();
			PreparedStatement ps = con.prepareStatement(sql);
			for (int i = 0; i < nSensors(); i++) {
				getSensor(i).outputToOracle(ps);
			}
			JdbcUtils.release(con, null, ps);
		}
		catch (ClassNotFoundException | SQLException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}*/
	public void outputVhcPosition() throws IOException {
		Segment seg;
		for (int i = 0; i < nSegments(); i++) {
			seg = segments_.get(i);
			seg.outputVhcPosition();
		}
	}
	public void outputSegments() throws IOException {
		Segment seg;
		for (int i = 0; i < nSegments(); i++) {
			seg = segments_.get(i);
			seg.outputSegment();;
		}
	}
	public void updateProgress() {

	}
	public void updateProgress(float pct) {

	}

	public void initializeLinkStatistics() {
		for (int i = 0; i < nLinks(); i++) {
			getLink(i).initializeStatistics();
		}
	}
	public void resetLinkStatistics(int col, int ncols) {
		for (int i = 0; i < nLinks(); i++) {
			getLink(i).resetStatistics(col, ncols);
		}
	}
	// 新增代码，复位统计流量和旅行时间的数组，用于参数校准模块，仿真重启
	public void resetLinkStatistics() {
		for (int i = 0; i < nLinks(); i++) {
			getLink(i).resetStatistics();
		}
	}
	//保存每一帧在网车辆的位置信息
	public void recordVehicleData(){
		
	}

	// public void save_3d_state(int roll, int tm, const char *prefix = 0);
	// public void append_3d_state(const char *prefix1, const char *prefix2 =
	// 0);

	// Export data to transcad
	/*
	 * public int Export(int type) {
	 *
	 * }
	 *
	 * private boolean ExportSegmentAsMapInfo(String filename){
	 *
	 * } private boolean ExportSegmentAsDccText(String filename){
	 *
	 * }
	 */
}
