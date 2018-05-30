package com.transyslab.simcore.mlp;

/**
 * Created by WangYimin on 2018/3/2.
 */
public class ExpSwitch {
	public final static boolean DOUBLE_LOOP = false;
	public final static boolean CAP_CTRL = false;
	public final static boolean MAX_ACC_CTRL = true;
	public final static boolean ACC_SMOOTH = false;//Ä¿Ç°ÓÐBUG
	public final static boolean APPROACH_CTRL = true;
	public final static boolean CF_CURVE = false;
	public final static boolean SPD_BUFFER = false;
	public final static boolean VIRTUAL_RELEASE = false;

	public final static double MAX_ACC = 4.0;
	public final static double MAX_DEC = -7.0;
	public final static double APPROACH_SPD = 60.0/3.6;
	public final static int SPD_BUFFER_VAL = 5;
	public final static double CF_VT_END = 200.0;
	public final static double CF_VT = 120.0/3.6;
}
