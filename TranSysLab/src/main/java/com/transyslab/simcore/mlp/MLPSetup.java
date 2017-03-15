package com.transyslab.simcore.mlp;

import java.util.List;

import com.transyslab.commons.io.XmlParser;


public class MLPSetup {
	
	public synchronized static void ParseNetwork() {
		XmlParser.parseNetworkXml("src/main/resources/demo_neihuan/scenario2/network.xml");
	}
	
	public static void ParsePathTables() {		
		XmlParser.parsePathTableXml("src/main/resources/demo_neihuan/scenario2/pathtable.xml");		
	}
	
	public static void ParseODTripTables(int tarid) {		
		XmlParser.parseODXml("src/main/resources/demo/demand1-21.xml", tarid);
	}
	
	public static void ParseFCDTable(float[][] realtime) {
		XmlParser.parseDetTimeXml("src/main/resources/demo/dettime.xml", realtime);
	}
	
	public static void ParseSensorTables() {
		XmlParser.parseSensorXml("src/main/resources/demo_neihuan/scenario2/sensor.xml");
	}
	
	/*public static void ParseSnapshotList(List<MLPVehicle> vhclist){
		XmlParser.parseMLPSnapshotXml("src/main/resources/demo_snapshot/snapshot.xml", vhclist);
	}*/
	
	public static void ParseVehicleTable(){
		XmlParser.parseVehicleTable("src/main/resources/demo_snapshot/vehicletable.xml");
	}
	
	public synchronized static String getEmitFormDir() {
		return EmitFormDir;
	}

	public synchronized static void setEmitFormDir(String emitFormDir) {
		EmitFormDir = emitFormDir;
	}

	public synchronized static String getLoopDir() {
		return LoopDir;
	}

	public synchronized static void setLoopDir(String loopDir) {
		LoopDir = loopDir;
	}

	public synchronized static String getODFormDir() {
		return ODFormDir;
	}

	public synchronized static void setODFormDir(String oDFormDir) {
		ODFormDir = oDFormDir;
	}

	private static String ODFormDir = "src/main/resources/demo_neihuan/scenario2/od_form.csv";
	private static String EmitFormDir = "src/main/resources/demo_neihuan/scenario2/emit_form_20170228.csv";
	private static String FCDFormDir = "src/main/resources/demo_neihuan/scenario2/FCD_20160620_ARCID4855inRL6.7.csv";
	private static String LoopDir = "src/main/resources/demo_neihuan/scenario2/LOOP_A24_20160620_800-1000.csv";
}
