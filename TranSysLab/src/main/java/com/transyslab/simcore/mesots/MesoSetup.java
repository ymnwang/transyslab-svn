/**
 *
 */
package com.transyslab.simcore.mesots;

import java.util.List;

import com.transyslab.commons.io.XmlParser;

/**
 * 静态方法解析各类表格
 *
 * @author yali
 *
 */
public class MesoSetup {

	public static void ParseParameters() {

		/*
		 * if(MESO_Parameter.getInstance()!=null){
		 * MESO_Parameter.getInstance().load(); }
		 */
	}
	// --------------------------------------------------------------------
	// Read the network database. This include network objects (nodes,
	// link labels, links, segments, lanes), traffic control objects
	// (traffic signals, message signs, toll booths, etc.) and
	// surveillance objects (sensors).
	// --------------------------------------------------------
	public static void ParseNetwork() {
		// HashMap<String, Integer> hm =
		// MESO_InfoArrays.getInstance().getHashMap();
		// int threadid = hm.get(Thread.currentThread().getName()).intValue();
		// String s = String.valueOf(threadid);
		// String file = "E:\\network"+s+".xml";
		XmlParser.parseNetworkXml("src/main/resources/demo/network.xml");
		/*
		 * if (MESO_Network.getInstance()!=null) { // if (ToolKit::verbose()) {
		 * // cout << "Unloading <" << theNetwork->name() // << ">" << endl; //
		 * } // delete (MESO_Network *)theNetwork; } // theNetwork = new
		 * MESO_Network; String filename =
		 * ToolKit::optionalInputFile(theNetwork->name()); RN_Parser
		 * rn_parser(theNetwork); rn_parser.parse(filename);
		 *
		 * // if (ToolKit::verbose()) { // theNetwork->printBasicInfo(); // }
		 */
	}
	public static void ParsePathTables() {
		// HashMap<String, Integer> hm =
		// MESO_InfoArrays.getInstance().getHashMap();
		// int threadid = hm.get(Thread.currentThread().getName()).intValue();
		// String s = String.valueOf(threadid);
		// String file = "E:\\pathtable"+s+".xml";
		XmlParser.parsePathTableXml("src/main/resources/demo/pathtable.xml");
		/*
		 * if (thePathTable) { delete thePathTable; thePathTable = NULL; } if
		 * (ToolKit::isValidInputFilename(RN_PathTable::name())) { thePathTable
		 * = new RN_PathTable; const char *filename =
		 * ToolKit::optionalInputFile(RN_PathTable::name()); if (!filename)
		 * theException->exit(1); RN_PathParser pt_parser(thePathTable);
		 * pt_parser.parse(filename); thePathTable->setPathPointers();
		 * theNetwork->calcPathCommonalityFactors(); } else if
		 * (ToolKit::verbose()) { cout << "No path tables. " <<
		 * "Route choice model will be used for all drivers." << endl; }
		 */
	}
	public static void ParseODTripTables(int tarid) {
		// HashMap<String, Integer> hm =
		// MESO_InfoArrays.getInstance().getHashMap();
		// int threadid = hm.get(Thread.currentThread().getName()).intValue();
		// String s = String.valueOf(threadid);
		// String file = "E:\\demand"+s+".xml";
		XmlParser.parseODXml("src/main/resources/demo/demand1-21.xml", tarid);
		/*
		 * if (theODTable) { delete (MESO_ODTable *)theODTable; theODTable =
		 * NULL; }
		 *
		 * MESO_Communicator *c = (MESO_Communicator*)theCommunicator; if
		 * (c->odFile()) { theODTable = new MESO_ODTable;
		 * theODTable->open(c->odFile()); } else if
		 * (ToolKit::isValidInputFilename(MESO_ODTable::name())) { theODTable =
		 * new MESO_ODTable; theODTable->open(); } else if (ToolKit::verbose())
		 * { cout << "No trip tables." << endl; }
		 */
	}

	public static void ParseVehicleTables(double start_time) {
		/*
		 * if (theVehicleTable) { delete (MESO_VehicleTable *)theVehicleTable;
		 * theVehicleTable = NULL; } if
		 * (ToolKit::isValidInputFilename(VehicleTable::name())) {
		 * theVehicleTable = new MESO_VehicleTable;
		 * theVehicleTable->open(start_time); } else if (ToolKit::verbose()) {
		 * cout << "No vehicle tables." << endl; }
		 */
	}
	public static void ParseFCDTable(float[][] realtime) {
		// HashMap<String, Integer> hm =
		// MESO_InfoArrays.getInstance().getHashMap();
		// int threadid = hm.get(Thread.currentThread().getName()).intValue();
		// String s = String.valueOf(threadid);
		// String file = "E:\\dettime"+s+".xml";
		XmlParser.parseDetTimeXml("E:\\MesoInput_Cases\\12.25goodluck\\dettime.xml", realtime);
	}
	public static void ParseSensorTables() {
		XmlParser.parseSensorXml("src/main/resources/demo/sensor.xml");
	}
	public static void ParseSnapshotList(List<MesoVehicle> vhclist){
		XmlParser.parseSnapshotXml("src/main/resources/demo_snapshot/snapshot.xml", vhclist);
	}
	public static void ParseVehicleTable(){
		XmlParser.parseVehicleTable("src/main/resources/demo_snapshot/vehicletable.xml");
	}
	public static void SetupMiscellaneous() {
		// Assign the pointers to control and surveillance devices in
		// MESO_Segment objects.
		/*
		 * MESO_Network.getInstance().assignCtrlListInSegments();
		 * MESO_Network.getInstance().assignSurvListInSegments();
		 */
	}
}
