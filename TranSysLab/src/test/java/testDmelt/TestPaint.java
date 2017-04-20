package testDmelt;


import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import jhplot.*;
import jhplot.math.Random;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.transyslab.commons.io.JdbcUtils;

import de.erichseifert.gral.examples.ExamplePanel;
import info.monitorenter.gui.chart.traces.Trace2DSimple;

public class TestPaint extends ExamplePanel {

	public void P2Test() {

			XYSeries xySeries=new XYSeries("flow");
			try {
				Connection con = JdbcUtils.getConnection();
				String sql = "select C,ROUND(S/C*12*5/4) as hourfolw,meanspeed, D from (select count(\"FLOW\") AS C,sum(\"FLOW\") AS S,sum(\"FLOW\"*\"SPEED\")/(sum(\"FLOW\")+0.0000001) as meanspeed, floor((extract(epoch from \"CTIME\")-extract(epoch from timestamp without time zone '2016-01-21 00:00:00'))/300)*300 AS D from nhschema.\"Loop\"  where \"CPN\" = 'LP/B9' and (to_char(\"CTIME\",'mm-dd') = '01-21') group by floor((extract(epoch from \"CTIME\")-extract(epoch from timestamp without time zone '2016-01-21 00:00:00'))/300)*300) as derivedtable order by D";
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet result = ps.executeQuery();
				
				while(result.next())
				{
					xySeries.add(result.getInt(4), result.getInt(2));
				}
				XYSeriesCollection xSeriesCollection = new XYSeriesCollection(xySeries);
				
				ChartFrame frame = new ChartFrame("pic", ChartFactory.createXYLineChart("PIC", "time", "flow veh/15min", xSeriesCollection));
				frame.setVisible(true);
				frame.setSize(600,600);
				
			} catch (SQLException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
		

		
		
		public void P3Test(){
			

			try {
				long begin = System.currentTimeMillis();
//				Connection con = JdbcUtils.getConnection();
				QueryRunner qr = new QueryRunner(JdbcUtils.getDataSource());
				String sql = "select C,ROUND(S/C*12*5/4) as hourfolw,meanspeed, D from (select count(\"FLOW\") AS C,sum(\"FLOW\") AS S,sum(\"FLOW\"*\"SPEED\")/(sum(\"FLOW\")+0.0000001) as meanspeed, floor((extract(epoch from \"CTIME\")-extract(epoch from timestamp without time zone '2016-01-21 00:00:00'))/300)*300 AS D from nhschema.\"Loop\"  where \"CPN\" = 'LP/B9' and (to_char(\"CTIME\",'mm-dd') = '01-21') group by floor((extract(epoch from \"CTIME\")-extract(epoch from timestamp without time zone '2016-01-21 00:00:00'))/300)*300) as derivedtable order by D";
				List<Object[]> result = (List) qr.query(sql, new ArrayListHandler());
//				PreparedStatement ps = con.prepareStatement(sql);
//				ResultSet result = ps.executeQuery();
				
				P2D d3 = new P2D();
				d3.setSymbolSize(2);	
				

				for(Object[] row:result)
				{
					double x = ((Double)row[1]).doubleValue();
					double y = ((Double)row[2]).doubleValue();
					double z = x/y;
					d3.add(x, y, z);					
				}/*
				while(result.next()){
					d3.add(result.getDouble(2), result.getDouble(3), result.getDouble(2)/result.getDouble(3));
				}
				JdbcUtils.release(con, result, ps);*/
				HPlot3D h3d = new HPlot3D();
				h3d.setRange(0, 200, 0, 100, 0,5);
				h3d.setNameX("flow");
				h3d.setNameY("speed");
				h3d.setNameZ("density");
				h3d.visible();
				h3d.draw(d3);
				long end = System.currentTimeMillis();
				System.out.println((end - begin)/1000.0);
				
			} catch (SQLException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}

		public static void main(String[] args) {
			//new Test();
			
			TestPaint test = new TestPaint();
			test.P3Test();
//			test.rtDraw();
//			test.func2DDraw();
			System.out.println("Done");
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public String getTitle() {
			return "PLOT";
		}
		public void rtDraw(){
			HPlotRT c1 = new HPlotRT();
			Trace2DSimple trace1 = new Trace2DSimple();
			trace1.setColor(Color.RED);
			c1.add(trace1);
			Random rand = new Random();
			for(int i=0;i<100;i++){
				try {
					Thread.currentThread().sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				trace1.addPoint(i, rand.normal(10, 5));
			}
		}
		public void func2DDraw(){
			HPlot3D h3d = new HPlot3D();
			h3d.visible();
			F2D function = new F2D("x*x+y*y",-2,2,-2,2);
	//		h3d.setContour(true);
			h3d.draw(function);
			h3d.setRange(-2, 2, -2, 2, 0,5);
			h3d.update();
		}

}
