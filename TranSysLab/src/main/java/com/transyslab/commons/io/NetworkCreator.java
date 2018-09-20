package com.transyslab.commons.io;

import com.transyslab.roadnetwork.*;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.postgis.LineString;
import org.postgis.MultiLineString;
import org.postgis.PGgeometry;
import org.postgis.Point;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class NetworkCreator {
    public static void readDataFromDB(RoadNetwork roadNetwork,String nodeIdList) throws SQLException {
        DataSource ds = JdbcUtils.getDataSource();
        org.apache.commons.dbutils.QueryRunner qr = new QueryRunner(ds);
        String sql;
        List<Object[]> result = null;
        // 读取节点数据, roadid为空的记录为交叉口节点
        sql = "select nodeid, st_transform(geom,2362) from topo_node ";
        // 按节点集筛选
        if(nodeIdList!=null)
            sql += "where nodeid in (" + nodeIdList + ")";
        else
            sql += "where roadid isnull";
        // 节点数据
        result = qr.query(sql, new ArrayListHandler());
        // 遍历，node -> node
        for(Object[] row:result){
            long nodeid = (long)row[0];
            Point pos = ((Point)((PGgeometry)row[1]).getGeometry());
            roadNetwork.createNode(nodeid,1,"N" + String.valueOf(nodeid),
                    new GeoPoint(pos.getX(),pos.getY(),pos.getZ()));
        }
        // 读取中心线数据
        sql = "select gid, name, fnode, tnode from topo_centerroad ";
        if(nodeIdList!=null)
            sql += "where fnode in (" + nodeIdList + ") and tnode in ("+ nodeIdList+")";
        // 中心线数据
        result = qr.query(sql, new ArrayListHandler());
        // 遍历中心线数据，topo_centerroad -> link
        for(Object[] row:result){
            long linkid = (int)row[0];
            String linkName = (String)row[1];
            long upNodeId = (long)row[2];
            long dnNodeId = (long)row[3];
            Link newLink =  roadNetwork.createLink(linkid,1,linkName,upNodeId,dnNodeId);
            // 与当前中心线同向的子路段数据，topo_link -> segment
            sql = "select id,st_transform(geom,2362) from topo_link " +
                    "where roadid = " + String.valueOf(linkid) + " and flowdir = 1";
            List<Segment> sgmt2check = readSegments(qr,sql,roadNetwork);
            // 按上下游顺序存储Segment
            List<Segment> sortedSgmts = sortSegments(sgmt2check);
            newLink.setSegments(sortedSgmts);

            // 反向路段
            Link newLinkRvs = roadNetwork.createLink(-linkid,1,linkName,dnNodeId,upNodeId);
            // 与反向中心线同向的子路段数据，topo_link -> segment
            sql = "select id,st_transform(geom,2362) from topo_link " +
                    "where roadid = " + String.valueOf(linkid) + " and flowdir = -1";
            sgmt2check = readSegments(qr,sql,roadNetwork);
            sortedSgmts = sortSegments(sgmt2check);
            newLinkRvs.setSegments(sortedSgmts);
        }
        // 目标区域的所有车道编号集，筛选出相关的车道连接器
        String laneIds = Arrays.toString(roadNetwork.getLanes().stream().mapToLong(e -> e.getId()).toArray());
        laneIds = laneIds.substring(1,laneIds.length()-1);
        // 读取车道连接器数据
        sql = "select connectorid, fromlaneid, tolaneid, st_transform(geom,2362) from topo_laneconnector "+
                "where fromlaneid in (" + laneIds +") and tolaneid in (" + laneIds + ")";
        // 车道连接器数据 LaneConnector -> Connector
        readConnectors(qr,sql,roadNetwork);
        JdbcUtils.close();
    }
    public static List<Segment> readSegments(QueryRunner qr, String sql, RoadNetwork roadNetwork) throws SQLException {
        List<Object[]> segmentRslt = qr.query(sql, new ArrayListHandler());
        List<Segment> sgmt2check = new ArrayList<>();
        for (Object[] sgmtRow : segmentRslt) {
            PGgeometry geom = (PGgeometry) sgmtRow[1];
            LineString[] lines = ((MultiLineString) geom.getGeometry()).getLines();
            List<GeoPoint> ctrlPoint = new ArrayList<>();
            for (LineString line : lines) {
                for (Point p : line.getPoints()) {
                    ctrlPoint.add(new GeoPoint(p.getX(), p.getY(), p.getZ()));
                }
            }
            Segment newSgmt = roadNetwork.createSegment((long) sgmtRow[0], 60, 60, 0, ctrlPoint);
            String sql2GetLanes = "select laneid, laneindex, width, direction, st_transform(geom,2362) from topo_lane " +
                    "where segmentid = " + String.valueOf(newSgmt.getId());
            // 读取属于当前Segment的Lane
            List<Lane> lanesInSgmt = readLanes(qr,sql2GetLanes,roadNetwork);
            // 将车道按流向从左到右排列
            Collections.sort(lanesInSgmt);
            newSgmt.setLanes(lanesInSgmt);
            sgmt2check.add(newSgmt);
        }
        return sgmt2check;
    }
    public static List<Lane> readLanes(QueryRunner qr, String sql, RoadNetwork roadNetwork) throws SQLException {
        // 读取Segment的Lane

        List<Object[]> laneRslt = qr.query(sql, new ArrayListHandler());
        List<Lane> lanesInSgmt = new ArrayList<>();
        // 遍历Lane数据
        for (Object[] laneRow : laneRslt) {
            long laneid = (long) laneRow[0];
            int orderNum = (int) laneRow[1];
            double width = ((BigDecimal) laneRow[2]).doubleValue();
            String direction = (String) laneRow[3];
            // lane的几何属性
            PGgeometry geomLane = (PGgeometry) laneRow[4];
            LineString[] linesLane = ((MultiLineString) geomLane.getGeometry()).getLines();
            List<GeoPoint> ctrlPoints = new ArrayList<>();
            for (LineString line : linesLane) {
                for (Point p : line.getPoints()) {
                    ctrlPoints.add(new GeoPoint(p.getX(), p.getY(), p.getZ()));
                }
            }
            Lane newLane = roadNetwork.createLane(laneid, 3, orderNum, width, direction, ctrlPoints);
            lanesInSgmt.add(newLane);
        }
        return lanesInSgmt;
    }
    public static List<Connector> readConnectors(QueryRunner qr, String sql, RoadNetwork roadNetwork) throws SQLException {
        List<Connector> connectors = new ArrayList<>();
        List<Object[]> result = qr.query(sql, new ArrayListHandler());
        // 遍历车道连接器 LaneConnector -> Connector
        for(Object[] connRow:result){
            long connId = (long)connRow[0];
            long fLaneId = (long)connRow[1];
            long tLaneId = (long)connRow[2];
            // connector的几何属性
            PGgeometry geom = (PGgeometry)connRow[3];
            LineString[] lines = ((MultiLineString)geom.getGeometry()).getLines();
            List<GeoPoint> ctrlPoints = new ArrayList<>();
            for(LineString line:lines){
                for(Point p:line.getPoints()){
                    ctrlPoints.add(new GeoPoint(p.getX(),p.getY(),p.getZ()));
                }
            }
            connectors.add(roadNetwork.createConnector(connId,fLaneId,tLaneId,ctrlPoints));
        }
        return connectors;
    }
    public static List<Segment> sortSegments(List<Segment> sgmt2Check){
        // 按流向对segment进行排序
        // 统计sgmt的公用顶点数，找出起始segment
        Segment startSgmt = null;
        for(Segment sgmt: sgmt2Check){
            GeoPoint spnt = sgmt.getCtrlPoints().get(0);
            int appeartimes = 0;
            for (Segment compareSgmt: sgmt2Check) {
                if (spnt.equal(compareSgmt.getCtrlPoints().get(0))) {
                    appeartimes ++;
                }
            }
            if (appeartimes <= 1) {// 某条表示该segment是起始segment的某个坐标只出现一次，表示该segment是起始segment
                startSgmt = sgmt;
                break;
            }
        }
        Segment nextSgmt = null;
        List<Segment> sortedSgmts = null;
        if(startSgmt == null){
            System.out.println("Error:Could not find the first segment of link"+
                    String.valueOf(startSgmt.getLink().getId()));
            return null;
        }
        else{
            nextSgmt = startSgmt;
            sortedSgmts = new ArrayList<>();
        }

        while(nextSgmt!=null){
            sortedSgmts.add(nextSgmt);
            // 获取Segment终点
            final GeoPoint endPnt = nextSgmt.getCtrlPoints().get(nextSgmt.getCtrlPoints().size()-1);
            // 下游segment，起点与上游终点重合
            nextSgmt = sgmt2Check.stream().filter(e->e.getCtrlPoints().get(0).equal(endPnt)).findFirst().orElse(null);
        }
        return sortedSgmts;
    }

}
