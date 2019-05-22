package com.transyslab.commons.io;

import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by WangYimin on 2017/10/11.
 */
public class DBWriter extends JdbcUtils{
	protected QueryRunner qr;
	protected String sqlStr;
	protected List<Object[]> rows;

	public DBWriter(String sqlStr) {
		initDataSource();
		this.qr = new QueryRunner(getDataSource());
		this.sqlStr = sqlStr;
		this.rows = new ArrayList<>();
	}

	public void write(Object[] row) {
		rows.add(row);
	}

	private void batchUpload() {
		System.out.println("uploading Db");
		long t_start = System.currentTimeMillis();
		int batchNum = 100;
		int headIdx = 0;
		while (headIdx + batchNum < rows.size()) {
			upload(rows.subList(headIdx, headIdx+batchNum-1));
			headIdx += batchNum;
		}
		upload(rows.subList(headIdx, rows.size()-1));
		rows.clear();
		System.out.println("finished uploading in " + (System.currentTimeMillis()-t_start) + "ms");
	}

	private void upload(List<Object[]> uploadingRows) {
		try {
			qr.batch(sqlStr, uploadingRows);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	private void upload() {
		System.out.println("uploading Db");
		System.out.println("Data size:"+rows.size());
		long t_start = System.currentTimeMillis();
		try {
			qr.batch(sqlStr, rows);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		rows.clear();
		System.out.println("finished uploading in " + (System.currentTimeMillis()-t_start) + "ms");
	}

	public synchronized void flush() {
		if (ObjectSizeCalculator.getObjectSize(rows)>=1e7) {
			upload();
		}
	}

	public void beforeClose() {
		batchUpload();
	}
}
