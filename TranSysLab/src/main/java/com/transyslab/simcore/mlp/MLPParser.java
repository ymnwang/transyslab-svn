package com.transyslab.simcore.mlp;

import java.io.File;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.transyslab.commons.io.XmlParser;
import com.transyslab.simcore.mesots.MesoVehicle;
import com.transyslab.simcore.mlp.MLPVehicle;

public class MLPParser extends XmlParser{
	
/*	public static void parseMLPSnapshot(String filename, List<MLPVehicle> vehlist){
		File inputXml=new File(filename); 
		SAXReader saxReader = new SAXReader(); 
		try { 
			Document document = saxReader.read(inputXml); 
			Element node = document.getRootElement();
			listMLPSnapshotNodes(node,vehlist);
		} catch (DocumentException e) { 
			System.out.println(e.getMessage()); 
		} 
	}*/
	
	public static void parseMLPSnapshotXml(String filename, List<MLPVehicle> vhclist){
		File inputXml=new File(filename); 
		SAXReader saxReader = new SAXReader(); 
		try { 
		Document document = saxReader.read(inputXml); 
		Element node = document.getRootElement();
		listMLPSnapshotNodes(node,vhclist);
		} catch (DocumentException e) { 
		System.out.println(e.getMessage()); 
		} 
	}
	
	public static void listMLPSnapshotNodes(Element node,List<MLPVehicle> vehlist){
		int vhcid=-1,type=-1;
		float distance=0,length=0,departtime=0;
		List<Element> rootchild = node.elements();
		for(Element vehicleobj : rootchild){
			List<Attribute> vehiclearr = vehicleobj.attributes();
			for(Attribute vehicle: vehiclearr){
				if(vehicle.getName()=="vhcID")
					vhcid = Integer.parseInt(vehicle.getValue());
				if(vehicle.getName()=="length")
					length = Float.parseFloat(vehicle.getValue());
				if(vehicle.getName()=="distance")
					distance = Float.parseFloat(vehicle.getValue());
				if(vehicle.getName()=="type")
					type = Integer.parseInt(vehicle.getValue());
				if(vehicle.getName()=="departtime")
					departtime = Float.parseFloat(vehicle.getValue());
			}
			MLPVehicle tmp = MLPVehList.getInstance().recycle();
			tmp.init(vhcid, type, length,distance,departtime);
			vehlist.add(tmp);
		}
	}
}
