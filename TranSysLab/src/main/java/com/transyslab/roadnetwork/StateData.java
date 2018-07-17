package com.transyslab.roadnetwork;

import com.transyslab.commons.tools.GeoUtil;
import com.transyslab.simcore.rts.RTLane;

public class StateData implements NetworkObject{
    protected NetworkObject stateOn;// Segment or Lane
    protected GeoSurface surface;
    protected double avgSpeed;
    protected double queueLength;
    public StateData(NetworkObject stateOn){
        this.stateOn = stateOn;
        initialize();
    }
    private void initialize(){
        if(stateOn instanceof RTLane){
            RTLane curLane = (RTLane) stateOn;
            this.avgSpeed = curLane.getAvgSpeed();
            this.queueLength = curLane.getQueueLength();//
            GeoPoint queuePosition = curLane.getStartPnt().intermediate(curLane.getEndPnt(),queueLength/curLane.getGeoLength());
            this.surface = GeoUtil.lineToRectangle(curLane.getStartPnt(),queuePosition,Constants.LANE_WIDTH,true);
        }
    }
    public GeoSurface getSurface(){
        return this.surface;
    }
    public double getAvgSpeed(){
        return this.avgSpeed;
    }

    @Override
    public int getId() {
        return stateOn.getId();
    }

    @Override
    public String getObjInfo() {
        return null;
    }

    @Override
    public boolean isSelected() {
        return false;
    }

    @Override
    public void setSelected(boolean flag) {

    }
}
