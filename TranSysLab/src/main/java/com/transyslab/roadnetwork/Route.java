/**
 *
 */
package com.transyslab.roadnetwork;

/**
 * Â·¾¶
 *
 * @author YYL
 *
 */
public class Route extends LinkTimes {

	public Route() {
		// super(filename);
	}
	public Route(Route sp) {
		super(sp);
	}
	// public RN_Route& operator=(const RN_Route &r);

	public void initialize(char tag) {

	}

	public int pathPeriods() {
		return 1;
	}
	public int matrixSize() {
		return matrixSize_;
	}/*
		 *
		 * // The last two parameters are used only in the derived class //
		 * where the time dependent path table is calculated
		 *
		 * // Read time dependent link travel times and recalculate the shortest
		 * // path if necessary. If the path table is specified, it also updates
		 * // the travel time from each link to the destination of individual //
		 * paths. public void updatePathTable(String fname, float alpha,double
		 * start, double stop){ // Update info network
		 *
		 * if (fname!=null) { // from a file read(fname, alpha); } else if
		 * (isSpFlag(DefinedConstant.INFO_FLAG_UPDATE)) { // from prevailing
		 * condition updateLinkTravelTimes(alpha); } else { // info have not
		 * changed return; }
		 *
		 * // Recalculate the shortest path trees.
		 *
		 * if (isSpFlag(DefinedConstant.INFO_FLAG_UPDATE_TREES)) {
		 * findShortestPathTrees(start, stop); } } inline int isSpFlag(int flag)
		 * { return (flag & theSpFlag); } // public char *altname = NULL, float
		 * alpha = 1.0, // public double start = 0, double stop = 0);
		 *
		 * // Travel time from upstream end of olink to dnode
		 *
		 * // Return travel time on the shorted path from upstream end of link
		 * // "olink" to destination node "dnode". public float
		 * upRouteTime(RN_Link olink, RN_Node dnode, double timesec ){ int d =
		 * dnode.getDestIndex(); if( d >= 0 && d < nDestNodes()) { return
		 * upRouteTime(olink.getIndex(), d, timesec); } else { // cerr <<
		 * "Node " << dnode->code() // << " is not defined as a destination." <<
		 * endl; return FLT_INF; } } public float upRouteTime(int olink, int
		 * dnode, double timesec ){ // note: dnode is a destination node index,
		 * not a node index. return routeTimes_.read(olink * nDestNodes() +
		 * dnode); } // Travel time from dnstream end of olink to dnode //
		 * Return travel time on the shorted path from dnstream end of link //
		 * "olink" to node "dnode". public float dnRouteTime(RN_Link olink,
		 * RN_Node dnode, double timesec ){ int d = dnode.getDestIndex(); if( d
		 * >= 0 && d < nDestNodes()) { return dnRouteTime(olink.getIndex(), d,
		 * timesec); }else { // cerr << "Node " << dnode->code() // <<
		 * " is not defined as a destination." << endl; return FLT_INF; } public
		 * float dnRouteTime(int olink, int dnode, double timesec){ float total
		 * = upRouteTime(olink, dnode,0.0); float dt = avgLinkTime(olink);
		 * return total - dt; }
		 *
		 * // public void printShortestRouteTimes(ostream &os = cout, int p =
		 * 0);
		 *
		 */

	protected int state_; // 0=new 1=old

	protected int matrixSize_;
	/*
	 * protected DiskData<float> routeTimes_; // times from each link to des
	 * nodes protected static RN_LinkGraph *linkGraph_; protected static
	 * Graph<float, RN_LinkTimes> *graph(); //
	 * ------------------------------------------------------------------ //
	 * Compute shortest paths from each link to every node for each time //
	 * period //
	 * ------------------------------------------------------------------
	 * protected void findShortestPathTrees(double start, double stop){ register
	 * int i, n = nLinks();
	 *
	 * if (!linkGraph_) { linkGraph_ = new RN_LinkGraph(theNetwork, this); }
	 * else { linkGraph_->updateLinkCosts(this); }
	 *
	 * const char *s; s = Str("Building %d shortest path trees ...", n);
	 * theNetwork->updateProgress(s);
	 *
	 * float done = 0; float step = (n > 1) ? (1.0 / (n - 1)) : 1.0;
	 *
	 * for (i = 0; i < n; i ++) { linkGraph_->calcShortestPathTree(i);
	 * recordShortestPathTree(i); done = i * step * 100.0;
	 * theNetwork->updateProgress(done); } } //
	 * ------------------------------------------------------------------- //
	 * Record travel time on shortest path from "root" to every nodes // into
	 * the DiskData "routeTime_". When more than one link arrives at // a node,
	 * the incoming link with the shortes travel time from // upstream node of
	 * "root" is selected. //
	 * -------------------------------------------------------------------
	 * protected void recordShortestPathTree(int root, int p ){ int i, n =
	 * nNodes(); long pos = p * matrixSize() + root * nDestNodes(); float cost;
	 *
	 * // Travel times from root to every destination node
	 *
	 * for (i = 0; i < n; i ++) { cost = graph()->node(i)->cost(); if(
	 * theNetwork->node(i)->destIndex() != -1 ) { routeTimes_.write(pos +
	 * theNetwork->node(i)->destIndex(), cost); } }
	 *
	 * }
	 */
}
