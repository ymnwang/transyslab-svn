package com.transyslab.commons.io;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by WangYimin on 2017/12/13.
 */
public class SQLConnection {
	private static SQLConnection theSQLConn;
	Connection conn;
	boolean isIdle;

	private SQLConnection() {
		try {
			conn = JdbcUtils.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		isIdle = true;
	}

	public synchronized static SQLConnection getInstance() {
		if(theSQLConn==null) {
			theSQLConn = new SQLConnection();
			System.out.println("new instance built");
		}
		return theSQLConn;
	}

	public synchronized Connection getConn() {
		while (!isIdle)
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		isIdle = false;
		return conn;
	}

	public synchronized void release() {
		try {
			conn.close();
			conn = JdbcUtils.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		isIdle = true;
		notify();
	}
}
