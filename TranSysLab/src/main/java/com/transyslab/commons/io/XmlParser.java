/**
 *
 */
package com.transyslab.commons.io;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.transyslab.roadnetwork.Boundary;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.Path;
import com.transyslab.roadnetwork.PathTable;
import com.transyslab.roadnetwork.Point;
import com.transyslab.roadnetwork.Sensor;
import com.transyslab.roadnetwork.Surface;
import com.transyslab.roadnetwork.SurvStation;
import com.transyslab.simcore.mesots.MesoLane;
import com.transyslab.simcore.mesots.MesoLink;
import com.transyslab.simcore.mesots.MesoNetwork;
import com.transyslab.simcore.mesots.MesoNode;
import com.transyslab.simcore.mesots.MesoODCell;
import com.transyslab.simcore.mesots.MesoODTable;
import com.transyslab.simcore.mesots.MesoSegment;
import com.transyslab.simcore.mesots.MesoVehicle;
import com.transyslab.simcore.mesots.MesoVehicleList;
import com.transyslab.simcore.mesots.MesoVehicleTable;
import com.transyslab.simcore.mlp.MLPLane;
import com.transyslab.simcore.mlp.MLPLink;
import com.transyslab.simcore.mlp.MLPNetwork;
import com.transyslab.simcore.mlp.MLPNode;
import com.transyslab.simcore.mlp.MLPSegment;

/**
 * @author yali
 *
 */
public class XmlParser {
	//路网类型，1:MesoTS模型; 2:MLP模型
	public static int networkType_ = 2;
	
	//解析仿真路网
	public static void parseNetworkXml(String fileName) {
		File inputXml = new File(fileName);
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(inputXml);
			Element node = document.getRootElement();
			listNetworkNodes(node);
		}
		catch (DocumentException e) {
			System.out.println(e.getMessage());
		}
	}
	public static void listNetworkNodes(Element node) {
		
		int tempid = -100;
		int temptype = -100;
		String tempname = "null";
		int tempupnode = -100;
		int tempdnnode = -100;
		int tempspeedlimit = -100;
		double tempfreespeed = -100.0;
		int gradient = -100;
		double beginx = -100.0;
		double beginy = -100.0;
		double endx = -100.0;
		double endy = -100.0;
		int tempdnl = -100;
		int tempupl = -100;
		int tempboundary = -100;
		int tmpsurface = -100;
		int arcid = -100;
		double kerbx = 0;
		double kerby = 0;

		// 获取当前节点的所有属性节点
		List<Attribute> list = node.attributes();
		// 解析Node
		if (node.getName() == "N") {

			for (Attribute attr : list) {
				if (attr.getName() == "id")
					tempid = Integer.parseInt(attr.getValue());
				if (attr.getName() == "type")
					temptype = Integer.parseInt(attr.getValue());
				if (attr.getName() == "name")
					tempname = attr.getValue();

			}
			//实例化不同仿真模型的路网要素
			if(networkType_ == 1){
				MesoNode tempnode = new MesoNode();
				tempnode.init(tempid, temptype, tempname);
			}	
			else if(networkType_ == 2){
				MLPNode tempnode = new MLPNode();
				tempnode.init(tempid, temptype, tempname);
			}
			
		}
		// 解析Link
		if (node.getName() == "L") {
			
			for (Attribute attr : list) {
				if (attr.getName() == "id")
					tempid = Integer.parseInt(attr.getValue());
				if (attr.getName() == "type")
					temptype = Integer.parseInt(attr.getValue());
				if (attr.getName() == "UpNode")
					tempupnode = Integer.parseInt(attr.getValue());
				if (attr.getName() == "DnNode")
					tempdnnode = Integer.parseInt(attr.getValue());

			}
			//实例化不同仿真模型的路网要素
			if(networkType_ == 1){
				MesoLink templink = new MesoLink();
				templink.init(tempid, temptype, tempupnode, tempdnnode);
			}	
			else if(networkType_ == 2){
				MLPLink templink = new MLPLink();
				templink.init(tempid, temptype, tempupnode, tempdnnode);
			}
		}
		//解析Segment
		if (node.getName() == "S") {
			
			for (Attribute attr : list) {
				if (attr.getName() == "id")
					tempid = Integer.parseInt(attr.getValue());
				if (attr.getName() == "speedLimit")
					tempspeedlimit = Integer.parseInt(attr.getValue());
				if (attr.getName() == "freeSpeed")
					tempfreespeed = Double.parseDouble(attr.getValue());
				if (attr.getName() == "gradient")
					gradient = Integer.parseInt(attr.getValue());
				if (attr.getName() == "beginX")
					beginx = Double.parseDouble(attr.getValue());
				if (attr.getName() == "beginY")
					beginy = Double.parseDouble(attr.getValue());
				if (attr.getName() == "endX")
					endx = Double.parseDouble(attr.getValue());
				if (attr.getName() == "endY")
					endy = Double.parseDouble(attr.getValue());

			}
			if(networkType_ == 1){
				MesoSegment tempsegment = new MesoSegment();
				tempsegment.init(tempid, tempspeedlimit, (float) tempfreespeed, gradient);
				tempsegment.initArc(beginx, beginy, 0, endx, endy);
			}	
			else if(networkType_ == 2){
				MLPSegment tempsegment = new MLPSegment();
				tempsegment.init(tempid, tempspeedlimit, (float) tempfreespeed, gradient);
				tempsegment.initArc(beginx, beginy, 0, endx, endy);
			}

		}
		//解析Lane
		if (node.getName() == "LA") {
			
			for (Attribute attr : list) {
				// System.out.println(attr.getText() + "-----" + attr.getName()
				// + "---" + attr.getValue());
				if (attr.getName() == "LaneID")
					tempid = Integer.parseInt(attr.getValue());
				if (attr.getName() == "rule")
					temptype = Integer.parseInt(attr.getValue());
				if (attr.getName() == "beginX")
					beginx = Double.parseDouble(attr.getValue());
				if (attr.getName() == "beginY")
					beginy = Double.parseDouble(attr.getValue());
				if (attr.getName() == "endX")
					endx = Double.parseDouble(attr.getValue());
				if (attr.getName() == "endY")
					endy = Double.parseDouble(attr.getValue());

			}
			//创建不同仿真模型的Lane对象
			if(networkType_ == 1){
				MesoLane templane = new MesoLane();
				templane.init(tempid, temptype,beginx,beginy,endx,endy);
			}	
			else if(networkType_ == 2){
				MLPLane templane = new MLPLane();
				templane.init(tempid, temptype,beginx,beginy,endx,endy);
			}
			
		}
		//解析LaneConnector
		if (node.getName() == "LC") {
			for (Attribute attr : list) {
				
				if (attr.getName() == "UpLane")
					tempupl = Integer.parseInt(attr.getValue());
				if (attr.getName() == "DnLane")
					tempdnl = Integer.parseInt(attr.getValue());

			}
			//组织车道纵向拓扑关系
			if(networkType_ == 1){
				MesoNetwork.getInstance().addLaneConnector(tempupl, tempdnl);
			}	
			else if(networkType_ == 2){
				MLPNetwork.getInstance().addLaneConnector(tempupl, tempdnl);
			}
			
		}
		//解析Boundary
		if (node.getName() == "Boundary") {
			for (Attribute attr : list) {
				
				if (attr.getName() == "BoundaryID")
					tempboundary = Integer.parseInt(attr.getValue());
				if (attr.getName() == "BeginX")
					beginx = Double.parseDouble(attr.getValue());					
				if (attr.getName() == "BeginY")
					beginy = Double.parseDouble(attr.getValue());
				if (attr.getName() == "EndX")
					endx = Double.parseDouble(attr.getValue());
				if (attr.getName() == "EndY")
					endy = Double.parseDouble(attr.getValue());

			}
			//创建Boundary对象，用于绘制车道分隔线，与仿真模型无关
			Boundary tempbdy = new Boundary();
			tempbdy.init(tempboundary, beginx, beginy, endx, endy);
		}
		//解析Surface
		if(node.getName() == "Surface"){
			for (Attribute attr : list) {

				if (attr.getName() == "id")
					tmpsurface = Integer.parseInt(attr.getValue());
				if (attr.getName() == "arcid")
					arcid = Integer.parseInt(attr.getValue());

			}
			//创建Surface对象，用于绘制路面，与仿真模型无关
			Surface surface = new Surface();
			surface.init(tmpsurface, arcid);
			List<Element> kerblist = node.elements();
			for(Element p : kerblist){
				List<Attribute> pattr = p.attributes();
				for(Attribute attr : pattr){
					if(attr.getName()=="KerbX"){
						if(attr.getValue().isEmpty())
							System.out.print(tmpsurface);
						kerbx = Double.parseDouble(attr.getValue());
					}
						
					if(attr.getName()=="KerbY")
						kerby = Double.parseDouble(attr.getValue());
				}
				surface.addKerbPoint(new Point(kerbx,kerby));
			}
			
		}

		// 当前节点下面子节点迭代器
		Iterator<Element> it = node.elementIterator();
		// 遍历
		while (it.hasNext()) {
			// 获取某个子节点对象
			Element e = it.next();
			// 对子节点进行遍历
			listNetworkNodes(e);
		}
	}
	public static void parseODXml(String filename, int tarid) {
		File inputXml = new File(filename);
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(inputXml);
			Element node = document.getRootElement();
			listODNodes(node, tarid);
		}
		catch (DocumentException e) {
			System.out.println(e.getMessage());
		}
	}
	public static void listODNodes(Element node, int tarid) {
		// System.out.println("当前节点的名称：：" + node.getName());
		Element tare = null;
		int ODtime = -100;
		int s = -100;
		int u = -100;
		int oid = -100;
		int did = -100;
		int flow = -100;
		int c1 = -100;
		int c2 = -100;
		// rootchild为Time
		List<Element> rootchild = node.elements();
		// 找到对应id的Time元素
		for (Element tempe : rootchild) {
			List<Attribute> list = tempe.attributes();
			for (Attribute attr1 : list) {
				if (attr1.getName() == "timeid" && tarid == Integer.parseInt(attr1.getValue()))
					tare = tempe;
			}
		}
		if (tare != null) {
			// System.out.println("当前节点的名称：：" + tare.getName());
			// 遍历Time节点的所有属性
			List<Attribute> arroftare = tare.attributes();
			for (Attribute attr2 : arroftare) {
				// System.out.println(attr2.getText() + "-----" +
				// attr2.getName()
				// + "---" + attr2.getValue());
				if (attr2.getName() == "sttime") {
					String strDate = attr2.getValue();
					SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
					Date date = null;
					try {
						date = sdf.parse(strDate);
					}
					catch (ParseException e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
					int h = date.getHours();
					int m = date.getMinutes();
					int sec = date.getSeconds();
					// 往后读5分钟，故+300秒
					ODtime = h * 3600 + m * 60 + sec + 300;
					// 最后一个时间间隔
					if (ODtime == 68700)
						ODtime = Constants.INT_INF;
					// System.out.println(ODtime+"-----");
				}
				if (attr2.getName() == "s") {
					s = Integer.parseInt(attr2.getValue());
					// System.out.println(s+"-----");
				}
				if (attr2.getName() == "u") {
					u = Integer.parseInt(attr2.getValue());
					// System.out.println(u+"-----");
				}
			}
			MesoODTable.getInstance().init(ODtime, s, u);
			// Time的子节点Item
			List<Element> childoftare = tare.elements();
			for (Element temptar : childoftare) {
				MesoODCell tempcell = new MesoODCell();
				arroftare = temptar.attributes();
				for (Attribute attr2 : arroftare) {
					// System.out.println(attr2.getText() + "-----" +
					// attr2.getName()
					// + "---" + attr2.getValue());
					if (attr2.getName() == "o") {
						oid = Integer.parseInt(attr2.getValue());
					}
					if (attr2.getName() == "d") {
						did = Integer.parseInt(attr2.getValue());
					}
					if (attr2.getName() == "flow") {
						flow = Integer.parseInt(attr2.getValue());
					}
					if (attr2.getName() == "c1") {
						c1 = Integer.parseInt(attr2.getValue());
					}
					if (attr2.getName() == "c2") {
						c2 = Integer.parseInt(attr2.getValue());
					}

				}
				tempcell.init(oid, did, flow, c1, c2);
				// 更新odtable时会重新生成odcell，则每个odcell都需要建立新的path
				if (PathTable.getInstance().getPaths() != null) {
					Vector<Path> celltopaths = PathTable.getInstance().getPaths();
					for (int pindex = 0; pindex < celltopaths.size(); pindex++) {
						if (celltopaths.get(pindex).getOriCode() == oid && celltopaths.get(pindex).getDesCode() == did)
							tempcell.addPath(celltopaths.get(pindex).getCode());
					}
				}
			}
		}
	}
	// 解析路径表
	public static void parsePathTableXml(String filename) {
		File inputXml = new File(filename);
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(inputXml);
			Element node = document.getRootElement();
			listPathTableNodes(node);
		}
		catch (DocumentException e) {
			System.out.println(e.getMessage());
		}
	}
	public static void listPathTableNodes(Element node) {
		// System.out.println("当前节点的名称：：" + node.getName());
		int pid = -100;
		int lkid = -100;
		int oid = -100;
		int did = -100;
		List<Attribute> list = node.attributes();
		// 遍历属性节点
		if (node.getName() == "P") {
			for (Attribute attr : list) {
				// System.out.println(attr.getText() + "-----" + attr.getName()
				// + "---" + attr.getValue());
				if (attr.getName() == "id")
					pid = Integer.parseInt(attr.getValue());
				if (attr.getName() == "o")
					oid = Integer.parseInt(attr.getValue());
				if (attr.getName() == "d")
					did = Integer.parseInt(attr.getValue());

			}
			PathTable.getInstance().addPath(pid, oid, did);
			List<Element> childofp = node.elements();
			for (Element lofp : childofp) {
				// P元素下的子元素L
				List<Attribute> listofl = lofp.attributes();
				for (Attribute attrofl : listofl) {
					// System.out.println(attrofl.getText() + "-----" +
					// attrofl.getName()
					// + "---" + attrofl.getValue());
					if (attrofl.getName() == "id") {
						lkid = Integer.parseInt(attrofl.getValue());
						PathTable.getInstance().findPath(pid).addLink(lkid);
					}
					// 完成path初始化
				}

			}
		}

		// 当前节点下面子节点迭代器
		Iterator<Element> it = node.elementIterator();
		// 遍历
		while (it.hasNext()) {
			// 获取某个子节点对象
			Element e = it.next();
			// 对子节点进行遍历
			listPathTableNodes(e);
		}
	}
	//解析外部检测数据，建议使用csv格式
	public static void parseDetTimeXml(String filename, float[][] realtime) {
		File inputXml = new File(filename);
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(inputXml);
			Element node = document.getRootElement();
			listDetTimeNodes(node, realtime);
		}
		catch (DocumentException e) {
			System.out.println(e.getMessage());
		}
	}
	public static void listDetTimeNodes(Element node, float[][] realtime) {
		
		int colindex;
		List<Element> rootchild = node.elements();
		for (Element linkobj : rootchild) {
			colindex = 0;
			List<Attribute> linklist = linkobj.attributes();
			for (Attribute link : linklist) {
				List<Element> timelist = linkobj.elements();
				for (Element time : timelist) {
					List<Attribute> timearr = time.attributes();
					for (Attribute traveltime : timearr) {
						if (traveltime.getName() == "traveltime") {
							if (Integer.parseInt(link.getValue()) == 64) {
								realtime[0][colindex] = Float.parseFloat(traveltime.getValue());
								colindex++;
							}
							else if (Integer.parseInt(link.getValue()) == 60) {
								realtime[1][colindex] = Float.parseFloat(traveltime.getValue());
								colindex++;
							}
							else if (Integer.parseInt(link.getValue()) == 116) {
								realtime[2][colindex] = Float.parseFloat(traveltime.getValue());
								colindex++;
							}

						}
					}

				}

			}

		}
	}
	//解析交通检测器
	public static void parseSensorXml(String filename) {
		File inputXml = new File(filename);
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(inputXml);
			Element node = document.getRootElement();
			listSensorNodes(node);
		}
		catch (DocumentException e) {
			System.out.println(e.getMessage());
		}
	}
	public static void listSensorNodes(Element node) {
		int type = -100;
		float interval = -100;
		int segid = -100;
		float zone = -100;
		int id = -100;
		float pos = -100;
		int sid = -100;
		int lid = -100;

		List<Attribute> list = node.attributes();
		// 遍历属性节点
		if (node.getName() == "station") {
			for (Attribute attr : list) {
				// System.out.println(attr.getText() + "-----" + attr.getName()
				// + "---" + attr.getValue());
				if (attr.getName() == "type")
					type = Integer.parseInt(attr.getValue());
				if (attr.getName() == "interval")
					interval = Float.parseFloat(attr.getValue());
				if (attr.getName() == "zone")
					zone = Float.parseFloat(attr.getValue());
				if (attr.getName() == "segid")
					segid = Integer.parseInt(attr.getValue());
				if (attr.getName() == "id")
					id = Integer.parseInt(attr.getValue());
				if (attr.getName() == "pos")
					pos = Float.parseFloat(attr.getValue());

			}
			SurvStation tmp = new SurvStation();
			tmp.init(type, interval, zone, segid, id, pos);
			List<Element> childofp = node.elements();
			for (Element lofp : childofp) {
				// station元素下的子元素sensor
				List<Attribute> listofl = lofp.attributes();
				for (Attribute attrofl : listofl) {
					if (attrofl.getName() == "id") 
						sid = Integer.parseInt(attrofl.getValue());
					if (attrofl.getName() == "laneid") 
						lid = Integer.parseInt(attrofl.getValue());
				}
				Sensor tsensor = new Sensor();
				tsensor.init(sid, lid);
			}
		}

		// 当前节点下面子节点迭代器
		Iterator<Element> it = node.elementIterator();
		// 遍历
		while (it.hasNext()) {
			// 获取某个子节点对象
			Element e = it.next();
			// 对子节点进行遍历
			listSensorNodes(e);
		}
	}
	
	//解析路网快照
	//注意：应先解析VehicleTable，得到od和path
	public static void parseSnapshotXml(String filename, List<MesoVehicle> vhclist){
		File inputXml=new File(filename); 
		SAXReader saxReader = new SAXReader(); 
		try { 
		Document document = saxReader.read(inputXml); 
		Element node = document.getRootElement();
		listSnapshotNodes(node,vhclist);
		} catch (DocumentException e) { 
		System.out.println(e.getMessage()); 
		} 
	}
	public static void listSnapshotNodes(Element node, List<MesoVehicle> vhclist){
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
			MesoVehicle tmp = MesoVehicleList.getInstance().recycle();
			tmp.init(vhcid, type, length,distance,departtime);
			vhclist.add(tmp);
		}
	}
	
	//解析外部发车表
	public static void parseVehicleTable(String filename){
		File inputXml=new File(filename); 
		SAXReader saxReader = new SAXReader(); 
		try { 
		Document document = saxReader.read(inputXml); 
		Element node = document.getRootElement();
		listVhcTableNodes(node);
		} catch (DocumentException e) { 
		System.out.println(e.getMessage()); 
		} 
	}
	public static void listVhcTableNodes(Element node){
		int vhcid=-1,o=-1,d=-1,type=-1,pid=-1;
		float length = 0, departtime=0;
		List<Element> rootchild = node.elements();
		for(Element tableobj:rootchild){
			List<Attribute> vhctables = tableobj.attributes();
			for(Attribute tableatt:vhctables){
				if(tableatt.getName()=="o")
					o = Integer.parseInt(tableatt.getValue());
				if(tableatt.getName()=="d")
					d = Integer.parseInt(tableatt.getValue());
				if(tableatt.getName()=="pid")
					pid = Integer.parseInt(tableatt.getValue());
			}
			MesoVehicleTable.getInstance().init(o, d, pid);
			List<Element> vehiclelist = tableobj.elements();
			for(Element vehicleobj : vehiclelist){
				List<Attribute> vehiclearr = vehicleobj.attributes();
				for(Attribute vehicle: vehiclearr){
					if(vehicle.getName()=="vhcID")
						vhcid = Integer.parseInt(vehicle.getValue());
					if(vehicle.getName()=="length")
						length = Float.parseFloat(vehicle.getValue());
					if(vehicle.getName()=="type")
						type = Integer.parseInt(vehicle.getValue());
					if(vehicle.getName()=="departtime")
						departtime = Float.parseFloat(vehicle.getValue());
				}
				MesoVehicle tmp = new MesoVehicle();
				tmp.init(vhcid, type,length,0,departtime);
				((MesoVehicleTable) MesoVehicleTable.getInstance()).getVhcList().add(tmp);
			}
		}
		
		
	}
}
