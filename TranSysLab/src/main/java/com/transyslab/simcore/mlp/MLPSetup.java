package com.transyslab.simcore.mlp;

import java.util.List;

import com.transyslab.commons.io.XmlParser;


public class MLPSetup {
	
	public static void ParseNetwork() {
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
	
	public static String ODFormDir = "src/main/resources/demo_neihuan/scenario2/od_form.csv";
	public static String EmitFormDir = "src/main/resources/demo_neihuan/scenario2/emit_form.csv";
}
