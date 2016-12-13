/**
 *
 */
package com.transyslab.simcore.mesots;

import java.util.HashMap;

import com.transyslab.commons.tools.Random;
import com.transyslab.roadnetwork.Lane;
import com.transyslab.roadnetwork.Link;
import com.transyslab.roadnetwork.Node;
import com.transyslab.roadnetwork.RoadNetwork;
import com.transyslab.roadnetwork.Segment;

/**
 * @author its312
 *
 */
public class MesoNetwork extends RoadNetwork {
	protected int[] permuteLink;
	protected int nPermutedLinks;
	public MesoNetwork() {
	}

	public static MesoNetwork getInstance() {
		HashMap<String, Integer> hm = MesoNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		return MesoNetworkPool.getInstance().getNetwork(threadid);
	}

	@Override
	public Node newNode()// C++:RN_Node* MESO_Network::newNode()
	{
		return new MesoNode();
	}
	@Override
	public Link newLink() {
		return new MesoLink();
	}
	@Override
	public Segment newSegment() {
		return new MesoSegment();
	}
	@Override
	public Lane newLane() {
		return new MesoLane();
	}/*
		 * public RN_Sensor newSensor() { return new MESO_Sensor(); }/* public
		 * RN_Signal newSignal() { return new MESO_Signal(); } public
		 * RN_TollBooth newTollBooth() { return new MESO_TollBooth(); }
		 */

	public MesoNode mesoNode(int i) {
		return (MesoNode) getNode(i);
	}
	public MesoLink mesoLink(int i) {
		return (MesoLink) getLink(i);
	}
	public MesoSegment mesoSegment(int i) {
		return (MesoSegment) getSegment(i);
	}

	public void calcStaticInfo() {
		superCalcStaticInfo();
		organize();
	}
	public void setsdIndex() {
		MesoSegment ps;
		for (int i = 0; i < nLinks(); i++) {
			if (MesoNetwork.getInstance().getLink(i).getCode() == 64) {
				ps = (MesoSegment) getLink(i).getEndSegment();
				while (ps != null) {
					ps.setSdIndex(0);
					ps = ps.getUpSegment();
				}
			}
			else if (MesoNetwork.getInstance().getLink(i).getCode() == 60) {
				ps = (MesoSegment) getLink(i).getEndSegment();
				while (ps != null) {
					ps.setSdIndex(0);
					ps = ps.getUpSegment();
				}
			}
			else if (MesoNetwork.getInstance().getLink(i).getCode() == 116) {
				ps = (MesoSegment) getLink(i).getEndSegment();
				while (ps != null) {
					ps.setSdIndex(1);
					ps = ps.getUpSegment();
				}
			}

		}
	}

	public void organize() {
		for (int i = 0; i < nLinks(); i++) {
			((MesoLink) getLink(i)).checkConnectivity();
		}
	}
	/*
	 * public void resetSensorReadings() { for (int i = 0; i < nSensors(); i ++
	 * ) { getSensor(i).resetSensorReadings(); } }
	 */

	public void calcSegmentData() {
		MesoSegment ps = new MesoSegment();
		for (int i = 0; i < nSegments(); i++) {
			ps = mesoSegment(i);
			ps.calcDensity();
			ps.calcSpeed();
		}
	}
	public void calcSegmentInfo() {
		MesoSegment ps;
		for (int i = 0; i < nSegments(); i++) {
			ps = mesoSegment(i);
			ps.calcState();
		}
	}
	/*
	 * --------------------------------------------------------------------
	 * Enter vehicles queued at starting link into the network.
	 * --------------------------------------------------------------------
	 */
	public void enterVehiclesIntoNetwork() {
		MesoVehicle pv;

		for (int i = 0; i < nLinks(); i++) {
			/*
			 * Find the first vehicle in the queue and enter it into the network
			 * if space is available. If the link is full, there is no need for
			 * checking other vehicles in the queue, skip to the next link.
			 */

			while ((pv = ((MesoLink) getLink(i)).queueHead()) != null && (pv.enterNetwork() != 0)) {
				/* push static vehicle attributes on message buffer */
				;
			}
		}
	}

	/*
	 * ------------------------------------------------------------------- Add
	 * number of vehicles allowed to move out during this time step to the
	 * segment balance.
	 * -------------------------------------------------------------------
	 */
	public void resetSegmentEmitTime() {
		for (int i = 0; i < nSegments(); i++) {
			mesoSegment(i).resetEmitTime();
		}
	}

	public void guidedVehiclesUpdatePaths() {
		MesoTrafficCell cell;
		MesoVehicle pv;
		int i;

		// Vehicles already in the network

		for (i = 0; i < nSegments(); i++) {
			cell = mesoSegment(i).firstCell();
			while (cell != null) {
				pv = cell.firstVehicle();
				while (pv != null) {
					if (pv.isGuided() != 0) {
						pv.changeRoute();
					}
					pv = pv.trailing();
				}
				cell = cell.trailing();
			}
		}

		// Vehicles waiting for entering the network

		for (i = 0; i < nLinks(); i++) {
			pv = mesoLink(i).queueHead();
			while (pv != null) {
				if (pv.isGuided() != 0) {
					pv.changeRoute();
				}
				pv = pv.trailing();
			}
		}
	}

	// Calculate capacity of the nodes

	public void updateNodeCapacities() {
		for (int i = 0; i < nNodes(); i++) {
			mesoNode(i).calcCapacities();
		}
	}

	// Update phase
	/*
	 * -------------------------------------------------------------------- For
	 * each traffic cells in the network, calculate its density and upSpeed.
	 * These two variables depend only on the state of a particular traffic cell
	 * itself.
	 * --------------------------------------------------------------------
	 */
	public void calcTrafficCellUpSpeed() {
		MesoSegment ps;
		for (int i = 0; i < nLinks(); i++) {
			ps = (MesoSegment) getLink(i).getEndSegment();
			while (ps != null) {
				ps.calcTrafficCellUpSpeed();
				ps = ps.getUpSegment();
			}
		}
	}
	/*
	 * -------------------------------------------------------------------- For
	 * each traffic cells in the network, calculate its dnSpeed. This variable
	 * variable depends on its own state and the states of the traffic cells
	 * ahead. This function is called after calcIndependentTrafficCellStates()
	 * is called.
	 * --------------------------------------------------------------------
	 */
	public void calcTrafficCellDnSpeeds() {
		MesoSegment ps;
		for (int i = 0; i < nLinks(); i++) {
			ps = (MesoSegment) getLink(i).getEndSegment();
			while (ps != null) { // downstream first
				ps.calcTrafficCellDnSpeeds();
				ps = ps.getUpSegment();
			}
		}
	}

	// Advance phase

	public void advanceVehicles() {
		permuteLink = null;
		nPermutedLinks = 0;
		int i;

		if (nPermutedLinks != nLinks()) { // this piece is executed only once
			if (permuteLink != null) {
				// δ���� delete [] permuteLink;
			}
			nPermutedLinks = nLinks();
			permuteLink = new int[nPermutedLinks];
			for (i = 0; i < nPermutedLinks; i++) {
				permuteLink[i] = i;
			}
		}

		// Randomize the link order

		Random.getInstance().get(Random.Misc).permute(nLinks(), permuteLink);
		for (i = 0; i < nLinks(); i++) {
			MesoLink p = mesoLink(permuteLink[i]);
			p.advanceVehicles();
		}
	}

	// Remove, merge, and split cells

	public void formatTrafficCells() {
		for (int i = 0; i < nSegments(); i++) {
			mesoSegment(i).formatTrafficCells();
		}
	}

	// Remove all vehicles in the network, including those in
	// pretrip queues

	public void clean() {
		int i;

		// Release current cells

		for (i = 0; i < nLinks(); i++) {
			mesoLink(i).clean();
		}

		// Restore capacities

		for (i = 0; i < nSegments(); i++) {
			MesoSegment ps = mesoSegment(i);
			ps.setCapacity(ps.defaultCapacity());
		}
	}

	// Interface to MITSIM
	/*
	 * public long loadInitialState(final String name, int check_time) { if
	 * (!ToolKit.isValidFilename(name)) { //δ���� cout << "No initial state. " <<
	 * endl //δ���� << "Simulation start from empty network." << endl; return
	 * (int) SimulationClock.getInstance().currentTime(); }
	 *
	 * // Reader is(name);
	 *
	 * MESO_Vehicle pv = new MESO_Vehicle(); int tnum = 0, inum = 0, qnum = 0;
	 *
	 * is.eatwhite();
	 *
	 * // Start time
	 *
	 * double start; //δ���� is >> start;
	 *
	 * // Vehicles in the network
	 *
	 * if (!is.findToken(OPEN_TOKEN)) theException.exit(); for (is.eatwhite();
	 * is.peek() != CLOSE_TOKEN; is.eatwhite()) { pv = theVehicleList.recycle();
	 * // create a new vehicle if (pv.load(is, 0) != 0) {
	 * theVehicleList.recycle(pv); // put it back } else { inum ++; } tnum ++; }
	 * if (!is.findToken(CLOSE_TOKEN)) theException.exit();
	 *
	 * // Vehicles in pretrip queue
	 *
	 * if (!is.findToken(OPEN_TOKEN)) theException.exit(); for (is.eatwhite();
	 * is.peek() != CLOSE_TOKEN; is.eatwhite()) { pv = theVehicleList.recycle();
	 * // create a new vehicle if (pv.load(is, 1) != 0) {
	 * theVehicleList.recycle(pv); // put it back } else { qnum ++; } tnum ++; }
	 * if (!is.findToken(CLOSE_TOKEN)) theException.exit();
	 *
	 * is.close();
	 *
	 * if (ToolKit.verbose()) { //δ���� cout << tnum << " vehicles parsed, " //δ����
	 * << inum << " loaded, and " //δ���� << qnum << " queued." << endl; }
	 *
	 * // In the simlab, we do not check time becuase the currentTime will // be
	 * set to the time it read here.
	 *
	 * if (check_time && !AproxEqual(start, theSimulationClock.currentTime())) {
	 * //δ���� cerr << "Warning:: State dumped at " //δ���� <<
	 * theSimulationClock.convertTime(start) //δ���� << " loaded at " //δ���� <<
	 * theSimulationClock.currentStringTime() << endl; }
	 *
	 * return (long) (start + 0.5); }
	 */
	/*
	 * public void saveLinkTravelTimes() { int offset = ( int)
	 * theEngine.beginTime(); int len = ( int) theEngine.endTime() - offset; int
	 * step = theGuidedRoute.infoPeriodLength(); int col = offset / step; int
	 * ncols = len / step; int num = col + ncols;
	 *
	 * final String fn; fn = Str("l%x.tmp", theCommunicator.simlab().tid()); δ����
	 * ofstream os(fn); if (!os.good()) { cerr <<
	 * "Error:: Cannot open output file <" << fn << ">." << endl;
	 * theException.exit(); }
	 *
	 * os << "% SIMULATED LINKS TRAVEL TIMES" << endl << col <<
	 * "\t% Start period" << endl << ncols << "\t% Number of periods" << endl <<
	 * step << "\t% Length per period" << endl;
	 *
	 * os << OPEN_TOKEN << endl; for (int i = 0; i < nLinks(); i ++) { MESO_Link
	 * *pl = (MESO_Link*) link(i); os << indent << pl.code() << endc <<
	 * pl.type() << endc << pl.length(); for (int j = col; j < num; j ++) { os
	 * << endc << pl.averageTravelTimeEnteringAt(j); } os << endl; } os <<
	 * CLOSE_TOKEN << endl;
	 *
	 * os.close(); }
	 */
	public void recordLinkTravelTimeOfActiveVehicle() {
		MesoTrafficCell cell;
		MesoVehicle pv;
		int i;

		// Record travel time for vehicle still in the network

		for (i = 0; i < nSegments(); i++) {
			cell = mesoSegment(i).firstCell();
			while (cell != null) {
				pv = cell.firstVehicle();
				while (pv != null) {
					pv.link().recordExpectedTravelTime(pv);
					pv = pv.trailing();
				}
				cell = cell.trailing();
			}
		}

		// Record travel time for vehicle in ths spill back queues

		for (i = 0; i < nLinks(); i++) {
			pv = mesoLink(i).queueHead();
			while (pv != null) {
				pv.nextLink().recordExpectedTravelTime(pv);
				pv = pv.trailing();
			}
		}
	}
	// Save state, return error code (0 = no error)
	/*
	 * public int dumpNetworkState( final String filename) { int i;
	 * MESO_TrafficCell[] cell ; MESO_Vehicle[] pv ;
	 *
	 * //δ���� ofstream os(filename);
	 *
	 * if (!os.good()) { //δ���� cerr <<
	 * "Error:: Failed doing a state dump to file <" //δ���� << filename << ">."
	 * << endl; return 1; }
	 *
	 * //δ���� os << "" << endl << " * A snap shot of the network state in MesoTS"
	 * << endl << " * Generated by " << UserName() << endl << " * at " <<
	 * TimeTag() << endl << " *" << endl << " * {" << endl <<
	 * " *   Code Type OriIndex DesIndex DepTime Mileage (cont)" << endl <<
	 * " *   Path PathIndex TimeEntersLink SegIndex SegDist" << endl << " * }"
	 * << endl << "
	 */// " << endl << endl;*/
	/*
	 * os << theSimulationClock.currentTime() << "\t# " <<
	 * theSimulationClock.currentStringTime() << endl;
	 *
	 * os << OPEN_TOKEN << "\t# Vehicles in the network" << endl;
	 *
	 * int ni = 0; for (i = 0; i < nSegments(); i ++) { cell =
	 * mesoSegment(i).firstCell(); while (cell) { pv = cell.firstVehicle();
	 * while (pv) { //δ���� pv.dumpState(os); ni ++; pv = pv.trailing(); } cell =
	 * cell.trailing(); } } //δ���� os << CLOSE_TOKEN << "\t#" << ni << endl;
	 *
	 * //δ���� os << OPEN_TOKEN //δ���� << "\t# Vehicles in the pretrip queues" <<
	 * endl; int nq = 0; for (i = 0; i < nLinks(); i ++) { pv =
	 * mesoLink(i).queueHead(); while (pv) { //δ���� pv.dumpState(os); nq ++; pv =
	 * pv.trailing(); } } // δ���� os << CLOSE_TOKEN << "\t#" << nq << endl;
	 *
	 * os << endl << "# " << (ni + nq) << " vehciles dumped" << endl;
	 *
	 * os.close();
	 *
	 * return 0; }
	 */

	// Debugging

	/*
	 * δ���� public int watchQueueLength(ostream& os = cout) { int n = 0; for (
	 * int i = 0; i < nLinks(); i ++) { n += mesoLink(i).reportQueueLength(os);
	 * } return n; }
	 */
	// C++ �������������

}
