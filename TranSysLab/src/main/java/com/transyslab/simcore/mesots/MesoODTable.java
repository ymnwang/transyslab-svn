/**
 *
 */
package com.transyslab.simcore.mesots;

import java.util.HashMap;

import com.transyslab.roadnetwork.ODCell;
import com.transyslab.roadnetwork.ODTable;

/**
 * @author its312
 *
 */
public class MesoODTable extends ODTable {

	public MesoODTable() {

	}

	@Override
	public ODCell newOD_Cell() {
		return new MesoODCell();
	}
	public static MesoODTable getInstance() {
		HashMap<String, Integer> hm = MesoNetworkPool.getInstance().getHashMap();
		int threadid = hm.get(Thread.currentThread().getName()).intValue();
		return MesoNetworkPool.getInstance().getODTable(threadid);
	}

}
