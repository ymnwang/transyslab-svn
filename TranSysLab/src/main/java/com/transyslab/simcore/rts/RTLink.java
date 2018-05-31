package com.transyslab.simcore.rts;

import com.transyslab.roadnetwork.Link;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

/**
 * Created by ITSA405-35 on 2018/5/28.
 */
public class RTLink extends Link {
	private SimpleDirectedWeightedGraph<RTLane,DefaultWeightedEdge> laneGraph;
	public RTLink(){
		laneGraph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
	}
	public void checkConnectivity(){

	}
	protected void addLaneGraphVertex(RTLane rtLane) {
		laneGraph.addVertex(rtLane);
	}

	protected void addLaneGraphEdge(RTLane fLane, RTLane tLane, double weight) {
		DefaultWeightedEdge edge = new DefaultWeightedEdge();
		laneGraph.addEdge(fLane, tLane, edge);
		laneGraph.setEdgeWeight(edge,weight);
	}
	/*
	public void addLnPosInfo() {
		RTSegment theSeg = (RTSegment) getEndSegment();
		int JLNUM = 1;
		while (theSeg != null){
			for (int i = 0; i<theSeg.nLanes(); i++){
				RTLane ln = theSeg.getLane(i);
				JointLane tmpJLn = findJointLane(ln);
				if (tmpJLn == null) {
					JointLane newJLn = new JointLane(JLNUM);
					JLNUM += 1;
					newJLn.lanesCompose.add(ln);
					jointLanes.add(newJLn);
				}
				else {
					tmpJLn.lanesCompose.add(ln);
				}
			}
			theSeg = (MLPSegment) theSeg.getUpSegment();
		}
	}*/
}
