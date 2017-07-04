/**
 *
 */
package com.transyslab.simcore.mesots;

import com.transyslab.roadnetwork.*;

/**
 * MesoODCell
 *
 * @author YYL 2016-6-6
 */
public class MesoODCell extends ODPair{


	protected double headway; // average headway (second)
	protected double nextTime; // next departure time
	protected int type; // vehicle type
	protected double randomness; // 0=uniform 1=random

	protected int busTypeBRT; // Dan: bus type for bus rapid transit assignment
	protected int runIDBRT; // Dan: assigned bus run id for bus rapid transit


	public MesoODCell(MesoNode o, MesoNode d) {
		nextTime = 0;
		oriNode = o;
		desNode = d;
	}

	public double rate() {
		return 3600.0 / headway;
	}
	public double nextTime() {
		return nextTime;
	}
	public int type() {
		return type;
	}

	public int busTypeBRT() {
		return busTypeBRT;
	}
	public int runIDBRT() {
		return runIDBRT;
	}


	// This function is called by OD_Parser. Return 0 if sucess or -1 if fail
	public void init(int ori, int des, double rate, double var, float r) {

		// return 1;
	}
	// Calculate the inter departure time for the next vehicle
	public double randomizedHeadway(MesoRandom rand) {
		if (randomness < 1.0e-10 || (rand.brandom(randomness)) == 0) {
			// uniform distribution
			return (headway);
		}
		else { // random distribution
			return (-Math.log(rand.urandom()) * headway);
		}
	}

	// Returns a created vehicle if it is time for a vehicle to depart or
	// NULL if no vehicle needs to depart at this time. It also update the
	// time that next vehicle departs if a vehicle is created.
	/*
	public MesoVehicle emitVehicle() {
		if (nextTime <= SimulationClock.getInstance().getCurrentTime()) {
			MesoVehicle pv = newVehicle();

			if ((type() & Constants.VEHICLE_BUS_RAPID) != 0) {
				// pv.initRapidBus(type, od, runIDBRT, busTypeBRT,
				// headway);
			}
			else
			{
				pv.init(0, type, od, null);
				//wym
				pv.initPath(od.getOriNode(), od.getDesNode());
			}		



			if ((type() & Constants.VEHICLE_BUS_RAPID) != 0) {
				pv.PretripChoosePath();
			}
			else
				pv.PretripChoosePath(this);


			nextTime += randomizedHeadway();
			return pv;
		}
		return null;
	}*/
	/*
	 * // Return a route if getPath table and splits are specified public RN_Path
	 * chooseRoute(RN_Vehicle pv){ int i =
	 * theRandomizers[MesoRandom::Routing]->drandom(nPaths(), splits); return
	 * getPath(i) ; }
	 */

	// Used when parsing the od table
	// Return a route if getPath table and splits are specified

	/*Path chooseRoute(Vehicle pv) {
//		int i = (MesoRandom.getInstance().get(2)).drandom(nPaths(), splits);
		double r = RoadNetwork.getInstance().sysRand.nextDouble();
		int n = nPaths();
		int i;
		for (n = n - 1, i = 0; i < n && r > splits[i]; i++);
		return getPath(i);
	}*/
	public void emitVehicles(MesoNetwork network, double currentTime) {
		/*MesoVehicle pv;
		while ((pv = emitVehicle()) != null) {
			pv.enterPretripQueue();
		}*/
		MesoVehicle vehicle = network.createVehicle();

		if ((type() & Constants.VEHICLE_BUS_RAPID) != 0) {
			// pv.initRapidBus(type, odPair, runIDBRT_, busTypeBRT_,
			// headway_);
		}
		else
		{
			vehicle.setType(this.type);
			// yyl 使用默认最短路径
			vehicle.setPath(this.getPath(0));
			// yyl 固定路径
			vehicle.fixPath();
			vehicle.setDepartTime(currentTime);
			vehicle.setTimeEntersLink(currentTime);
			/*
			vehicle.init(0, type, odPair, null);
			//wym
			vehicle.initPath(odPair.getOriNode(), odPair.getDesNode());*/
		}


		while(nextTime <= currentTime){
			// Find the first link to travel. Variable 'oriNode', 'desNode' and
			// 'type' must have valid values before the route choice model is
			// called.
			if ((type() & Constants.VEHICLE_BUS_RAPID) != 0) {
				vehicle.PretripChoosePath(oriNode,network);
			}
			else {
				vehicle.PretripChoosePath(this,network);
			}
			vehicle.enterPretripQueue(network.getSimClock().getStepSize());
			nextTime += randomizedHeadway(network.mesoRandom[MesoRandom.Departure]);
		}
	}

}
