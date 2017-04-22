package JgraphTTest;

import java.util.List;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AStarShortestPath;

import com.transyslab.roadnetwork.Link;
import com.transyslab.roadnetwork.LinkTimes;
import com.transyslab.roadnetwork.Node;
import com.transyslab.roadnetwork.PathTable;
import com.transyslab.roadnetwork.RoadNetwork;
import com.transyslab.roadnetwork.Route;

public class NetworkPaths{

	public static void main(String[] args) {
		RoadNetwork rn = new RoadNetwork();
		for (int i = 0; i < 4; i++) {
			Node node = new Node();
			node.setCode(i+1);
			rn.addNode(node);
			rn.addVertex(rn.getNode(i));
			Link link = new Link();
			link.setCode(i+1);
			rn.addLink(link);
		}
		rn.addEdge(rn.getNode(0), rn.getNode(1), rn.getLink(0));
		rn.getLink(0).includeTurn(rn.getLink(2));
		rn.addEdge(rn.getNode(0), rn.getNode(2), rn.getLink(1));
		rn.addEdge(rn.getNode(1), rn.getNode(3), rn.getLink(2));
		rn.addEdge(rn.getNode(2), rn.getNode(3), rn.getLink(3));
		
		Route theRoute = new Route();
		theRoute.initialize(rn);
		
		LinkTimes linkTimes = new LinkTimes();
		linkTimes.initTravelTimes();
		
		List<GraphPath<Node, Link>> paths = theRoute.getLinkGraph().getAllPaths(rn.getNode(0), rn.getNode(3), true, null);
		for (int i = 0; i < paths.size(); i++) {
			System.out.println(paths.get(i).getWeight());
		}
		System.out.println("finished");
	}

}
