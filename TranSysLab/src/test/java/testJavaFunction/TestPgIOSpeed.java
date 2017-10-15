package testJavaFunction;


import com.transyslab.commons.io.JdbcUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.encog.util.Stopwatch;

import java.sql.Date;
import java.sql.SQLException;


/**
 * Created by yali on 2017/10/13.
 */
public class TestPgIOSpeed {
	public static void main(String[] args) throws SQLException {
		final int dataSize = 100000;
		QueryRunner qr = new QueryRunner(JdbcUtils.getDataSource());
		Stopwatch sw = new Stopwatch();
		sw.start();
		String sql = "insert into iotest(name,password,email,birthday) values(?,?,?,?)";

		for (int i = 0; i < 1000; i++) {
			Object params[][] = new Object[100][];
			for(int j=0;j<100;j++){
				params[j] = new Object[] { "aa" + j, "123", "aa@sina.com",
						new Date(System.currentTimeMillis()) };
			}
			qr.batch(sql, params);
		}
		sw.stop();
		System.out.println("Time cost:" + sw.getElapsedMilliseconds());
	}

}
