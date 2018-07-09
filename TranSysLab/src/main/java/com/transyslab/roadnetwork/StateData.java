package com.transyslab.roadnetwork;

public class StateData implements NetworkObject{
    protected NetworkObject stateOn;// Segment or Lane
    protected GeoSurface surface;
    protected double avgSpeed;
    protected GeoPoint queuePostion;
    public StateData(NetworkObject stateOn, GeoSurface surface,GeoPoint queuePostion ,double avgSpeed){
        this.stateOn = stateOn;
        this.surface = surface;
        this.queuePostion = queuePostion;
        this.avgSpeed = avgSpeed;
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
