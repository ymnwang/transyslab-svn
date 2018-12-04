package com.transyslab.simcore.mlp;

import com.transyslab.commons.tools.GeoUtil;
import com.transyslab.roadnetwork.Connector;
import com.transyslab.roadnetwork.GeoPoint;
import com.transyslab.roadnetwork.Lane;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MLPConnector extends Connector {
    protected LinkedList<MLPVehicle> vehsOnConn;
    private List<MLPConnector> conflictConns;
    private MLPNode node;
    private double length;
    public MLPLane upLane;
    public MLPLane dnLane;

    public MLPConnector(long id, List<GeoPoint> shapePoints, MLPLane upLane, MLPLane dnLane) {
        super(id, shapePoints, upLane.getId(), dnLane.getId());
        vehsOnConn = new LinkedList<>();
        conflictConns = new ArrayList<>();
        length = -1.0;
        node = null;
        this.upLane = upLane;
        this.dnLane = dnLane;
    }

    public void setNode(MLPNode node) {
        this.node = node;
    }

    protected void clearVehsOnConn(){
        vehsOnConn.clear();
    }

    public int vehNumOnConn() {
        return vehsOnConn.size();
    }

    public double getLength() {
        if (length<0) {
            length = 0.0;
            for (int i = 0; i < shapePoints.size() - 1; i++)
                length += shapePoints.get(i).distance(shapePoints.get(i+1));
        }
        return length;
    }

    public double spaceOccupied() {
        return vehsOnConn.getLast().getDistance() / this.getLength();
    }

    public int queueNum() {
        return vehsOnConn.size();
    }

    public boolean checkVolume(MLPVehicle mlpv) {
        MLPVehicle tail_ = getTail();
        if (tail_ != null &&
                getLength() - tail_.getDistance() <
                        (mlpv.getLength() + ((MLPParameter)mlpv.getMLPNetwork().getSimParameter()).minGap(mlpv.getCurrentSpeed()))) {
            return false;
        }
        else
            return true;
    }

    public MLPVehicle getTail(){
        if (vehsOnConn.isEmpty())
            return null;
        else
            return vehsOnConn.getLast();
    }

    protected void addConflictConn(MLPConnector connector) {
        if (checkConflict(connector))
            conflictConns.add(connector);
    }

    public boolean checkConflict(MLPConnector connector) {
        return GeoUtil.isCross(this.getShapePoints(),connector.getShapePoints());
    }

    public double conflictCoef(){
        double c = 1.0;
        for (int i = 0; i < conflictConns.size(); i++) {
            c *= conflictConns.get(i).spaceOccupied();
        }
        return c;
    }

    protected List<MLPVehicle> updateVehs(){
        List<MLPVehicle> outputs = new ArrayList<>();
        for (MLPVehicle veh:vehsOnConn) {
            //todo update newdis newSpd here
            if (veh.newDis <=0 )
                outputs.add(veh);
        }
        return outputs;
    }

    public double calSpd(){
        MLPLink link = (MLPLink) upLane.getLink();
        double spd_normal = link.dynaFun.sdFun(((double)queueNum())/getLength());
        double rate = node.getPassSpd() / link.dynaFun.getFreeFlow();
        return spd_normal*rate*conflictCoef();
    }

//    public GeoPoint getStartPoint(){
//        return upLane.getEndPnt();
//    }
//
//    public GeoPoint getEndPoint(){
//        return dnLane.getStartPnt();
//    }

    public long upLinkID() {
        return upLane.getLink().getId();
    }

    public long dnLinkID() {
        return dnLane.getLink().getId();
    }
}
