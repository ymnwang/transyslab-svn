package com.transyslab.simcore.mlp;

import com.transyslab.commons.io.XmlParser;


public class MLPSetup {
	
	public synchronized static void ParseNetwork() {
		XmlParser.parseNetworkXml(rootDir + network_fileName);
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
	
	public synchronized static String getEmitForm_fileName() {
		return rootDir + emitForm_fileName;
	}

	public synchronized static void setEmitForm_fileName(String emitForm_fileName) {
		MLPSetup.emitForm_fileName = emitForm_fileName;
	}

	public synchronized static String getLoopData_fileName() {
		return rootDir + loopData_fileName;
	}

	public synchronized static void setLoopData_fileName(String loopData_fileName) {
		MLPSetup.loopData_fileName = loopData_fileName;
	}

	public synchronized static String getOdFormDir() {
		return rootDir + odForm_fileName;
	}

	public synchronized static void setOdForm_fileName(String oDForm) {
		odForm_fileName = oDForm;
	}

	public synchronized static void setRootDir(String arg){
		rootDir = arg;
	}

	private static String rootDir = "src/main/resources/DemoNeihuan/";//DemoNeihuan demo_neihuan/scenario2
	private static String network_fileName = "network.xml";
	private static String odForm_fileName = "od_form.csv";
	private static String emitForm_fileName = "emit_form.csv";
	private static String FCDForm_fileName = "FCD.csv";
	private static String loopData_fileName = "LOOP_A24_20160620_800-1000.csv";
}
