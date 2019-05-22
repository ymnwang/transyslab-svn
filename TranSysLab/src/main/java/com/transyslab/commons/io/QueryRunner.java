package com.transyslab.commons.io;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by WangYimin on 2017/9/24.
 */
public class QueryRunner extends org.apache.commons.dbutils.QueryRunner {

	//wym 将Apache的para[][]改成List<Obj[]>的形式

	public QueryRunner(DataSource ds) {
		super(ds);
	}

	public int[] batch(String sql, List<Object[]> params) throws SQLException {
		Connection conn = this.prepareConnection();
		return this.batch(conn, true, sql, params);
	}


	public int[] batch(Connection conn, boolean closeConn, String sql, List<Object[]> params) throws SQLException {
		int count = 0;
		final int batchSize = 1000;
		if(conn == null) {
			throw new SQLException("Null connection");
		} else if(sql == null) {
			if(closeConn) {
				this.close(conn);
			}

			throw new SQLException("Null SQL statement");
		} else if(params == null) {
			if(closeConn) {
				this.close(conn);
			}

			throw new SQLException("Null parameters. If parameters aren't need, pass an empty array.");
		} else {
			PreparedStatement stmt = null;
			int[] rows = null;

			try {
				stmt = this.prepareStatement(conn, sql);

				long t_start = System.currentTimeMillis();

				for (Object[] p : params) {
					this.fillStatement(stmt, p);
					stmt.addBatch();
					if(++count % batchSize == 0) {
						stmt.executeBatch();
					}
				}

				System.out.println("batch processing time: " + (System.currentTimeMillis()-t_start));

				rows = stmt.executeBatch();
			} catch (SQLException var11) {
				this.rethrow(var11, sql, (Object[])params.toArray());
			} finally {
				this.close(stmt);
				if(closeConn) {
					this.close(conn);
				}

			}

			return rows;
		}
	}

	/*public void closeConn() throws SQLException {
		Connection conn = this.prepareConnection();
		this.close(conn);
	}*/
}
