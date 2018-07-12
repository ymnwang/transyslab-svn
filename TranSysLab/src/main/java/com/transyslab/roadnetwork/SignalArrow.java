package com.transyslab.roadnetwork;


import com.transyslab.commons.tools.GeoUtil;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;


public class SignalArrow implements Comparable<SignalArrow>{
    public final static int LEFTARROW = 1;
    public final static int STRAIGHTARROW = 2;
    public final static int RIGHTARROW = 3;
    public final static double LINELENGTH = 2.5;
    public final static double TIPLENGTH =1.5;
    public final static double TURNLENGTH = 1;
    public final static double TURNANGLE= 45.0/180.0*Math.PI;

    protected int id;
    protected int type;
    protected Lane entrance;
    protected GeoPoint[] polyline;
    protected GeoPoint[] arrowTip;// 三角形
    protected float[] color = Constants.COLOR_GREEN;

    public SignalArrow(int id, int type ,Lane entrance){
        this.id = id;
        this.entrance = entrance;
        this.type = type;
        this.arrowTip = new GeoPoint[3];
        this.polyline = new GeoPoint[3];
        initialize();
    }
    public GeoPoint[] getPolyline(){
        return this.polyline;
    }
    public GeoPoint[] getArrowTip(){
        return  this.arrowTip;
    }
    public void setColor(float[] color){
        this.color = color;
    }
    public float[] getColor(){
        return this.color;
    }
    private void initialize(){
        double r = (entrance.getGeoLength()+1)/entrance.getGeoLength();// 起点偏移1m
        GeoPoint rectgFP = entrance.endPnt.intermediate(entrance.startPnt,r);
        r = r + 6.0/entrance.getGeoLength();
        GeoPoint rectgEP = entrance.endPnt.intermediate(entrance.startPnt,r);
        // polyline fpoint
        polyline[0] = rectgEP.intermediate(rectgFP,1.0/6.0);// 箭头起点偏移1m
        polyline[1] = rectgEP.intermediate(polyline[0],(LINELENGTH - TURNLENGTH)/6.0);
        polyline[2] = rectgEP.intermediate(polyline[1],TURNLENGTH/6.0);
        arrowTip[0] = rectgEP.intermediate(polyline[2], TIPLENGTH/6.0);
        GeoSurface rectangle2 = GeoUtil.lineToRectangle(polyline[2],arrowTip[0],1.0 / Math.tan(Math.PI/3.0),true);
        arrowTip[1] = rectangle2.getKerbList().get(0);
        arrowTip[2] = rectangle2.getKerbList().get(1);
        if(type!=STRAIGHTARROW){

            // 计算旋转轴
            Vector3D dir1 = new Vector3D(polyline[1].getLocationX() - polyline[0].getLocationX(),
                                polyline[1].getLocationY() - polyline[0].getLocationY(),
                                polyline[1].getLocationZ() - polyline[0].getLocationZ());
            Vector3D dir2 = new Vector3D(rectangle2.kerbPoints.get(1).getLocationX() - polyline[2].getLocationX(),
                                 rectangle2.kerbPoints.get(1).getLocationY() - polyline[2].getLocationY(),
                                 rectangle2.kerbPoints.get(1).getLocationZ() - polyline[2].getLocationZ());
            Vector3D rotateDir = dir2.crossProduct(dir1).normalize();
            double theta;
            if(type == LEFTARROW)
                theta = TURNANGLE;
            else
                theta = -1*TURNANGLE;

            polyline[2] = GeoUtil.calcRotation(polyline[1],polyline[2],theta,rotateDir);
            arrowTip[0] = GeoUtil.calcRotation(polyline[1],arrowTip[0],theta,rotateDir);
            arrowTip[1] = GeoUtil.calcRotation(polyline[1],arrowTip[1],theta,rotateDir);
            arrowTip[2] = GeoUtil.calcRotation(polyline[1],arrowTip[2],theta,rotateDir);
        }

    }

    @Override
    public int compareTo(SignalArrow o) {
        if(this.type>o.type)
            return 1;
        else if(this.type<o.type)
            return -1;
        else
            return 0;
    }
}
