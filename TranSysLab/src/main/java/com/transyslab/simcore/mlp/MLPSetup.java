package com.transyslab.simcore.mlp;

import java.util.List;

import com.transyslab.commons.io.XmlParser;


public class MLPSetup {
	
	public static void ParseNetwork() {
		XmlParser.parseNetworkXml("src/main/resources/demo_snapshot/network.xml");
	}
	
	public static void ParsePathTables() {		
		XmlParser.parsePathTableXml("src/main/resources/demo_snapshot/pathtable.xml");		
	}
	
	public static void ParseODTripTables(int tarid) {		
		XmlParser.parseODXml("src/main/resources/demo/demand1-21.xml", tarid);
	}
	
	public static void ParseFCDTable(float[][] realtime) {
		XmlParser.parseDetTimeXml("src/main/resources/demo/dettime.xml", realtime);
	}
	
	public static void ParseSensorTables() {
		XmlParser.parseSensorXml("src/main/resources/demo_snapshot/sensor.xml");
	}
	
	/*public static void ParseSnapshotList(List<MLPVehicle> vhclist){
		XmlParser.parseMLPSnapshotXml("src/main/resources/demo_snapshot/snapshot.xml", vhclist);
	}*/
	
	public static void ParseVehicleTable(){
		XmlParser.parseVehicleTable("src/main/resources/demo_snapshot/vehicletable.xml");
	}
}
