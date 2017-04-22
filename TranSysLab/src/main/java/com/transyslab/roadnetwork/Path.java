/**
 *
 */
package com.transyslab.roadnetwork;

import java.util.*;

import org.jgrapht.GraphPath;

import com.transyslab.commons.tools.SimulationClock;

/**
 * @author YYL 2016-6-4
 */

public class Path extends CodedObject {
	protected static int sorted_; // 1=sorted 0=arbitrary order

	protected int index_; // index in the array;
	// สตภýปฏ
	protected Vector<Link> links_; // list of links of the
														// path
	protected Node oriNode_; // origin node
	protected Node desNode_; // destination node

	// Total travel time from origin to destination. This value is
	// read from path table and not changed during the simulation.

	protected float pathTravelTime_;
	protected static int last = -1;
	protected static int idx = 0;

	private float cf_;				// commonality factor
	
	public Path() {
		links_ = new Vector<Link>();
	}

	public static int sorted() {
		return sorted_;
	}

	public Node getOriNode() {
		return oriNode_;
	}
	public Node getDesNode() {
		return desNode_;
	}

	public int getOriCode() {
		return oriNode_.getCode();
	}
	public int getDesCode() {
		return desNode_.getCode();
	}

	public int nLinks() {
		return links_.size();
	}
	public Link getLink(int i) {
		if (i < 0)
			return null;
		else if (i >= nLinks())
			return null;
		else
			return links_.get(i);
	}
	public Link getNextLink(int i) {
		if (i < nLinks() - 1)
			return links_.get(i + 1);
		else
			return null;
	}
	public Link getFirstLink() {
		if (nLinks() > 0)
			return links_.get(0);
		else
			return null;
	}
	public Link getLastLink() {
		if (nLinks() > 0)
			return links_.get(nLinks() - 1);
		else
			return null;
	}

	public int index() {
		return index_;
	}

	public float getPathTravelTime() {
		return pathTravelTime_;
	}
	public void setPathTravelTime(float s) {
		pathTravelTime_ = s;
	}

	// Travel time from the kth link to the destination if entering
	// at the calling time (currentTiem of the SimulationClock)

	// Calculate travel times from kth link on the path to the destination
	// based on the given information, assuming enter the kth link at the
	// current time.

	public float travelTime(LinkTimes info, int kth) {
		float t = (float) SimulationClock.getInstance().getCurrentTime();
		float x = 0.0f;
		float y = 0.0f;
		int i = kth, n = nLinks();
		while (i < n) {
			int k = links_.get(i).getIndex(); // index of the link
			x += info.linkTime(k, t); // add the travel time on link k
			y = info.linkTime(k, t);
			// cout << " Manish link cost " << x << " time = " << t << endl;
			// t += x; // entry time for next link
			t += y;
			i++;
		}
		return x;
	}
	// Calculate travel times from kth link on the path to the destination
	// based on the given information, assuming enter the kth link at time
	// t.
	public float travelTime(LinkTimes info, int kth, float t) {
		float x = 0.0f;
		int i = kth, n = nLinks();
		while (i < n) {
			int k = links_.get(i).getIndex(); // index of the link
			x += info.linkTime(k, t); // add the travel time on link k
			t += x; // entry time for next link
			i++;
		}
		return x;
	}

	// These two are called by RN_PathParser
	// Called by RN_PathParser to initialize a path
	public int init(int c, int ori, int des) {
		// /*static */int idx = 0;
		// /*static */int last = -1;
		setCode(c);
		if (sorted_ > 0 && c <= last)
			sorted_ = 0;
		else
			last = c;

		if ((oriNode_ = RoadNetwork.getInstance().findNode(ori)) == null) {
			// cerr << "Error:: Unknown origin node <" << ori << ">. ";
			return (-1);
		}
		else if ((desNode_ = RoadNetwork.getInstance().findNode(des)) == null) {
			// cerr << "Error:: Unknown destination node <" << des << ">. ";
			return (-1);
		}
		index_ = idx++;

		return 0;
	}
	// Called by RN_PathParser to add a link to a path
	public int addLink(int linkcode) {
		Link linkptr = RoadNetwork.getInstance().findLink(linkcode);
		if (linkptr == null) {
			// cerr << "Error:: Unkonwn link code <" << linkcode << ">. ";
			return -1;
		}
		Link prevlink = getLastLink();
		if (prevlink != null && prevlink.getDnNode() != linkptr.getUpNode()) {
			// cerr << "Error:: Link <" << linkcode << "> is not connected to "
			// << "link <" << prevlink->code() << ">. ";
			return -1;
		}
		links_.add(linkptr);
		return 0;
	}

	@Override
	public void print() {

	}
	
	public float cost(LinkTimes info)
	{
	   return travelTime(info, 0);
	}
	
	public boolean IsUsedBy(ODCell od)
	{
		if (od==null) return false ;
		for (int i = 0; i < od.nPaths(); i ++) {
			if (this == od.path(i)) return true ;
		}
		return false ;
	}
	
	public float cf() {
		return cf_;
	}
	
	public void cf(float arg) {
		cf_ = arg;
	}
	
	//wym
	public Path(GraphPath<Node, Link> GPath) {
		links_ = new Vector<>();
		List<Link> gp_links = GPath.getEdgeList();
		for (Link LN : gp_links) {
			links_.add(LN);
		}
		oriNode_ = GPath.getStartVertex();
		desNode_ = GPath.getEndVertex();
	}

}
