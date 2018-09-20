/**
 *
 */
package com.transyslab.commons.io;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.DbUtils;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

import java.sql.PreparedStatement;

/**
 * @author yali
 *
 */
public class JdbcUtils {

//	private static String driver_ = "org.postgresql.Driver";  //"oracle.jdbc.driver.OracleDriver";
//	private static String url_ = "jdbc:postgresql://192.168.8.23:5432/neihuandb";//"jdbc:oracle:thin:@192.168.8.138:1521:orcl";
//	private static String user_ = "postgres";
//	private static String pwd_ = "its312";
	private static DataSource dataSource;
	// log4j2 通过 log4j-jcl 实现Common logging接口
	// 可修改配置文件，设置输出优先级DEBUG以上
//	private static Log logger = LogFactory.getLog(JdbcUtils.class);; 
	public JdbcUtils() {

	}
	public static void initDataSource(){
		/*Parameters params = new Parameters();
		FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
		    new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
		    .configure(params.properties()
		        .setFileName("src/main/resources/demo_neihuan/scenario2/dbcp.properties"));*/
		Configurations configs = new Configurations();

		try
		{
//			Configuration config = builder.getConfiguration();
			Configuration config = configs.properties(new File("src/main/resources/xc_test/dbcp.properties"));
			String driver = config.getString("driverClassName");
		    String url = config.getString("url");
		    String user = config.getString("username");
		    String pwd = config.getString("password");
		    int initialSize = config.getInt("initialSize");
		    int maxActive = config.getInt("maxActive");
		    int minIdle = config.getInt("minIdle");
		    int maxIdle = config.getInt("maxIdle");
		    int maxWait = config.getInt("maxWait");
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
	        dataSource = bds;
		    
		}
		catch(ConfigurationException cex)
		{
		    // loading of the configuration file failed
		}
		
	}
	public static DataSource getDataSource(){
		if(dataSource == null)
			initDataSource();
		return dataSource;
	}
	public static Connection getConnection() throws SQLException{
		if(dataSource == null)
			initDataSource();
		return dataSource .getConnection();
	}
	public static void release(Connection con, ResultSet rs, PreparedStatement pstm) {
		DbUtils.closeQuietly(con, pstm, rs);
	}
	public static void close() {
		if (dataSource != null) {
			try {
				((BasicDataSource) dataSource).close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			dataSource = null;
		}
	}

	public static boolean isIdle() {
		return (dataSource == null);
	}
}
