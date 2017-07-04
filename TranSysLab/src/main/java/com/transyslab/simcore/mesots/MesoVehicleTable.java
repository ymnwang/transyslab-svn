/**
 *
 */
package com.transyslab.simcore.mesots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.transyslab.roadnetwork.*;

/**
 * @author its312
 *
 */
public class MesoVehicleTable {
	protected List<MesoVehicle> vhcList;
	protected ODPair odPair;
	protected Path path;
	
	public MesoVehicleTable() {
	}
	public ODPair getODPair(){
		return odPair;
	}
	public Path getPath(){
		return path;
	}

	//��дinit���������ݳ���od��·��id�����ɳ���
	public void init(int o, int d, int pid){
		// TODO ��ʵ��, ���ǳ����Ĳ�ͬOD
		vhcList = new ArrayList<MesoVehicle>();
		/*
	    Node ori = MesoNetwork.getInstance().findNode(o);
	    Node des = MesoNetwork.getInstance().findNode(d);
	    odPair = new ODPair(ori,des);
	    path = PathTable.getInstance().findPath(pid);*/
	}
	public List<MesoVehicle> getVhcList(){
		return vhcList;
	}

}
