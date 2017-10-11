package com.transyslab.commons.io;

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

	public void flush() {
		try {
			qr.batch(sqlStr, rows);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		rows.clear();
	}
}
