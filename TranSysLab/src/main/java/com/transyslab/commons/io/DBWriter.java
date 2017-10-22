package com.transyslab.commons.io;

import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;
import org.encog.util.Stopwatch;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by WangYimin on 2017/10/11.
 */
public class DBWriter {
	protected QueryRunner qr;
	protected String sqlStr;
	protected List<Object[]> rows;

	public DBWriter(String sqlStr) {
		this.qr = new QueryRunner(JdbcUtils.getDataSource());
		this.sqlStr = sqlStr;
		this.rows = new ArrayList<>();
	}

	public void write(Object[] row) {
		rows.add(row);
	}

	private void batchUpload() {
		System.out.println("uploading Db");
		Stopwatch timer = new Stopwatch();
		timer.start();
		int batchNum = 100;
		int headIdx = 0;
		while (headIdx + batchNum < rows.size()) {
			upload(rows.subList(headIdx, headIdx+batchNum-1));
			headIdx += batchNum;
		}
		upload(rows.subList(headIdx, rows.size()-1));
		rows.clear();
		timer.stop();
		System.out.println("finished uploading in " + timer.getElapsedMilliseconds() + "ms");
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
		Stopwatch timer = new Stopwatch();
		System.out.println("Data size:"+rows.size());
		timer.start();
		try {
			qr.batch(sqlStr, rows);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		rows.clear();
		timer.stop();
		System.out.println("finished uploading in " + timer.getElapsedMilliseconds() + "ms");
	}

	public synchronized void flush() {
		if (ObjectSizeCalculator.getObjectSize(rows)>=1e7) {
			upload();
		}
	}

	public void close() {
		batchUpload();
	}
}
