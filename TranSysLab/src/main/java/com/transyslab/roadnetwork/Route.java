/**
 *
 */
package com.transyslab.roadnetwork;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.alg.shortestpath.AllDirectedPaths;

/**
 * 路径
 *
 * @author YYL
 *
 */
public class Route extends LinkTimes {
	
	protected int state_; // 0=new 1=old
	protected int matrixSize_;
	protected List<Float> routeTimes_;// times from each link to des nodes
	protected AllDirectedPaths<Node, Link> linkGraph_;

	public Route() {
		// super(filename);
		routeTimes_ = new ArrayList<>();
	}
	
	public Route(Route sp) {
		super(sp);
		routeTimes_ = new ArrayList<>();
	}
	// public RN_Route& operator=(const RN_Route &r);

	public void initialize(RoadNetwork theNetwork) {
		linkGraph_ = new AllDirectedPaths<>(theNetwork);
		matrixSize_ = theNetwork.nLinks() * theNetwork.nDestNodes();
	}

	public int pathPeriods() {
		return 1;
	}
	
	public int matrixSize() {
		return matrixSize_;
	}
	
	// Return travel time on the shorted path from upstream end of link
	// "olink" to destination node "dnode".
	public float upRouteTime(Link olink, Node dnode, double timesec) {
		int d = dnode.destIndex_;
		//只是判断dnode是否为终点 没有判断olink是否可以到达dnode
		if (d >= 0 && d < nDestNodes()) {
			return upRouteTime(olink.getIndex(), d, timesec);
		} else {
			return Float.POSITIVE_INFINITY;
		}
	}

	public float upRouteTime(int olink, int dnode, double timesec) {
		// note: dnode is a destination node index, not a node index.
		return routeTimes_.get(olink * nDestNodes() + dnode);
	}

	// Travel time from dnstream end of olink to dnode
	// Return travel time on the shorted path from dnstream end of link
	// "olink" to node "dnode".
	public float dnRouteTime(Link olink, Node dnode, double timesec ){
		int d = dnode.getDestIndex();
		if( d >= 0 && d < nDestNodes()) {	    	
			return dnRouteTime(olink.getIndex(), d, timesec);
		}else {
			return Float.POSITIVE_INFINITY;
		}
	}

	public float dnRouteTime(int olink, int dnode, double timesec) {
		float total = upRouteTime(olink, dnode, 0.0);
		float dt = avgLinkTime(olink);
		return total - dt;
	}
	
	public AllDirectedPaths<Node, Link> getLinkGraph() {
		return linkGraph_;
	}
	
	public void findShortestPathTrees(double ftime, double ttime) {
		
	}
	
	public void calcShortestPathTree(int i) {
		
	}
}
