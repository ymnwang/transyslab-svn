package com.transyslab.commons.io;

import com.transyslab.commons.tools.TimeMeasureUtil;
import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;

import java.sql.SQLException;
import java.time.LocalDateTime;
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

	private void upload() {
		System.out.println("uploading Db");
		TimeMeasureUtil tm = new TimeMeasureUtil();
		tm.tic();
		try {
			qr.batch(sqlStr, rows);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		rows.clear();
		System.out.println("finished uploading in " + tm.toc() + "ms");
	}

	public synchronized void flush() {
		if (rows.size()>100) {////ObjectSizeCalculator.getObjectSize(rows)>=1e8
			upload();
		}
	}

	public void close() {
		upload();
	}
}
