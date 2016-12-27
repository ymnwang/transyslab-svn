package com.transyslab.simcore.mlp;

import java.util.List;

import com.transyslab.simcore.mlp.MLPParser;


public class MLPSetup {
	
	public static void ParseNetwork() {
		MLPParser.parseNetworkXml("src/main/resources/demo_snapshot/network.xml");
	}
	
	public static void ParsePathTables() {		
		MLPParser.parsePathTableXml("src/main/resources/demo_snapshot/pathtable.xml");		
	}
	
	public static void ParseODTripTables(int tarid) {		
		MLPParser.parseODXml("src/main/resources/demo/demand1-21.xml", tarid);
	}
	
	public static void ParseFCDTable(float[][] realtime) {
		MLPParser.parseDetTimeXml("src/main/resources/demo/dettime.xml", realtime);
	}
	
	public static void ParseSensorTables() {
		MLPParser.parseSensorXml("src/main/resources/demo_snapshot/sensor.xml");
	}
	
	public static void ParseSnapshotList(List<MLPVehicle> vhclist){
		MLPParser.parseMLPSnapshotXml("src/main/resources/demo_snapshot/snapshot.xml", vhclist);
	}
	
	public static void ParseVehicleTable(){
		MLPParser.parseVehicleTable("src/main/resources/demo_snapshot/vehicletable.xml");
	}
}
