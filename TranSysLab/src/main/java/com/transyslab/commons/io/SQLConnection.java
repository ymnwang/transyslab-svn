package com.transyslab.commons.io;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.File;
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
		try
		{
//			Configuration config = builder.getConfiguration();
			String driver = "org.postgresql.Driver";
			String url = "jdbc:postgresql://192.168.8.23:5432/neihuandb";
			String user = "postgres";
			String pwd = "its312";
			int initialSize = 5;
			int maxActive = 20;
			int minIdle = 1;
			int maxIdle = 20;
			int maxWait = 1000;
			BasicDataSource bds = new BasicDataSource();
			bds.setDriverClassName(driver);
			bds.setUrl(url);
			bds.setUsername(user);
			bds.setPassword(pwd);
			bds.setInitialSize(initialSize);
			bds.setMaxTotal(maxActive);
			bds.setMinIdle(minIdle);
			bds.setMaxIdle(maxIdle);
			bds.setMaxWaitMillis(maxWait);
			conn = bds.getConnection();
		}
		catch (SQLException e) {
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
