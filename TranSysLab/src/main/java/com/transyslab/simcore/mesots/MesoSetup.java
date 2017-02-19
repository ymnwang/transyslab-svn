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
		// E:\\MesoInput_Cases\\12.28bestluck\\
		//src/main/resources/demo/
		XmlParser.parseNetworkXml("src/main/resources/demo_pre/network.xml");

	}
	public static void ParsePathTables() {
		// 
		XmlParser.parsePathTableXml("E:\\MesoInput_Cases\\12.28bestluck\\pathtable.xml");

	}
	public static void ParseODTripTables(int tarid) {
		// 
		XmlParser.parseODXml("E:\\MesoInput_Cases\\12.25goodluck\\demand(synthesis).xml", tarid);
	}

	public static void ParseVehicleTables(double start_time) {

	}
	public static void ParseFCDTable(float[][] realtime) {
		// HashMap<String, Integer> hm =
		// MESO_InfoArrays.getInstance().getHashMap();
		// int threadid = hm.get(Thread.currentThread().getName()).intValue();
		// String s = String.valueOf(threadid);
		// String file = "E:\\dettime"+s+".xml";
		XmlParser.parseDetTimeXml("src/main/resources/demo/dettime.xml", realtime);
	}
	public static void ParseSensorTables() {
		XmlParser.parseSensorXml("E:\\MesoInput_Cases\\12.28bestluck\\sensor.xml");
	}
	public static void ParseSnapshotList(List<MesoVehicle> vhclist){
		XmlParser.parseSnapshotXml("src/main/resources/demo_snapshot/snapshot.xml", vhclist);
	}
	public static void ParseVehicleTable(){
		XmlParser.parseVehicleTable("E:\\MesoInput_Cases\\12.28bestluck\\emitList(12.28).xml");
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
