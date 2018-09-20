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
        // ��ȡ�ڵ�����, roadidΪ�յļ�¼Ϊ����ڽڵ�
        sql = "select nodeid, st_transform(geom,2362) from topo_node ";
        // ���ڵ㼯ɸѡ
        if(nodeIdList!=null)
            sql += "where nodeid in (" + nodeIdList + ")";
        else
            sql += "where roadid isnull";
        // �ڵ�����
        result = qr.query(sql, new ArrayListHandler());
        // ������node -> node
        for(Object[] row:result){
            long nodeid = (long)row[0];
            Point pos = ((Point)((PGgeometry)row[1]).getGeometry());
            roadNetwork.createNode(nodeid,1,"N" + String.valueOf(nodeid),
                    new GeoPoint(pos.getX(),pos.getY(),pos.getZ()));
        }
        // ��ȡ����������
        sql = "select gid, name, fnode, tnode from topo_centerroad ";
        if(nodeIdList!=null)
            sql += "where fnode in (" + nodeIdList + ") and tnode in ("+ nodeIdList+")";
        // ����������
        result = qr.query(sql, new ArrayListHandler());
        // �������������ݣ�topo_centerroad -> link
        for(Object[] row:result){
            long linkid = (int)row[0];
            String linkName = (String)row[1];
            long upNodeId = (long)row[2];
            long dnNodeId = (long)row[3];
            Link newLink =  roadNetwork.createLink(linkid,1,linkName,upNodeId,dnNodeId);
            // �뵱ǰ������ͬ�����·�����ݣ�topo_link -> segment
            sql = "select id,st_transform(geom,2362) from topo_link " +
                    "where roadid = " + String.valueOf(linkid) + " and flowdir = 1";
            List<Segment> sgmt2check = readSegments(qr,sql,roadNetwork);
            // ��������˳��洢Segment
            List<Segment> sortedSgmts = sortSegments(sgmt2check);
            newLink.setSegments(sortedSgmts);

            // ����·��
            Link newLinkRvs = roadNetwork.createLink(-linkid,1,linkName,dnNodeId,upNodeId);
            // �뷴��������ͬ�����·�����ݣ�topo_link -> segment
            sql = "select id,st_transform(geom,2362) from topo_link " +
                    "where roadid = " + String.valueOf(linkid) + " and flowdir = -1";
            sgmt2check = readSegments(qr,sql,roadNetwork);
            sortedSgmts = sortSegments(sgmt2check);
            newLinkRvs.setSegments(sortedSgmts);
        }
        // Ŀ����������г�����ż���ɸѡ����صĳ���������
        String laneIds = Arrays.toString(roadNetwork.getLanes().stream().mapToLong(e -> e.getId()).toArray());
        laneIds = laneIds.substring(1,laneIds.length()-1);
        // ��ȡ��������������
        sql = "select connectorid, fromlaneid, tolaneid, st_transform(geom,2362) from topo_laneconnector "+
                "where fromlaneid in (" + laneIds +") and tolaneid in (" + laneIds + ")";
        // �������������� LaneConnector -> Connector
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
            // ��ȡ���ڵ�ǰSegment��Lane
            List<Lane> lanesInSgmt = readLanes(qr,sql2GetLanes,roadNetwork);
            // ���������������������
            Collections.sort(lanesInSgmt);
            newSgmt.setLanes(lanesInSgmt);
            sgmt2check.add(newSgmt);
        }
        return sgmt2check;
    }
    public static List<Lane> readLanes(QueryRunner qr, String sql, RoadNetwork roadNetwork) throws SQLException {
        // ��ȡSegment��Lane

        List<Object[]> laneRslt = qr.query(sql, new ArrayListHandler());
        List<Lane> lanesInSgmt = new ArrayList<>();
        // ����Lane����
        for (Object[] laneRow : laneRslt) {
            long laneid = (long) laneRow[0];
            int orderNum = (int) laneRow[1];
            double width = ((BigDecimal) laneRow[2]).doubleValue();
            String direction = (String) laneRow[3];
            // lane�ļ�������
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
        // �������������� LaneConnector -> Connector
        for(Object[] connRow:result){
            long connId = (long)connRow[0];
            long fLaneId = (long)connRow[1];
            long tLaneId = (long)connRow[2];
            // connector�ļ�������
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
        // �������segment��������
        // ͳ��sgmt�Ĺ��ö��������ҳ���ʼsegment
        Segment startSgmt = null;
        for(Segment sgmt: sgmt2Check){
            GeoPoint spnt = sgmt.getCtrlPoints().get(0);
            int appeartimes = 0;
            for (Segment compareSgmt: sgmt2Check) {
                if (spnt.equal(compareSgmt.getCtrlPoints().get(0))) {
                    appeartimes ++;
                }
            }
            if (appeartimes <= 1) {// ĳ����ʾ��segment����ʼsegment��ĳ������ֻ����һ�Σ���ʾ��segment����ʼsegment
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
            // ��ȡSegment�յ�
            final GeoPoint endPnt = nextSgmt.getCtrlPoints().get(nextSgmt.getCtrlPoints().size()-1);
            // ����segment������������յ��غ�
            nextSgmt = sgmt2Check.stream().filter(e->e.getCtrlPoints().get(0).equal(endPnt)).findFirst().orElse(null);
        }
        return sortedSgmts;
    }

}
