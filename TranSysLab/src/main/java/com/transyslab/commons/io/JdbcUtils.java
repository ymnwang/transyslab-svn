/**
 *
 */
package com.transyslab.commons.io;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

/**
 * @author yali
 *
 */
public class JdbcUtils {

	private static String driver_ = "oracle.jdbc.driver.OracleDriver";
	private static String url_ = "jdbc:oracle:thin:@192.168.8.138:1521:orcl";
	private static String user_ = "sa";
	private static String pwd_ = "sa";

	public JdbcUtils() {
	}

	public static Connection getConnection(String driver, String url, String user, String pwd)
			throws ClassNotFoundException, SQLException {
		Class.forName(driver);
		Connection con = DriverManager.getConnection(url, user, pwd);
		return con;
	}
	public static Connection getConnection() throws ClassNotFoundException, SQLException {
		Class.forName(driver_);
		Connection con = DriverManager.getConnection(url_, user_, pwd_);
		return con;
	}
	public static void release(Connection con, ResultSet rs, PreparedStatement pstm) {
		if (rs != null) {
			try {
				rs.close();
			}
			catch (SQLException e1) {
				// TODO 自动生成的 catch 块
				e1.printStackTrace();
			}
		}
		if (pstm != null) {
			try {
				pstm.close();

			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		try {
			if (con != null && (!con.isClosed())) {
				try {
					con.close();
				}
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
