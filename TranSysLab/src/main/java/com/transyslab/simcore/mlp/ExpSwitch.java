package com.transyslab.simcore.mlp;

/**
 * Created by WangYimin on 2018/3/2.
 */
public class ExpSwitch {
	public static boolean DOUBLE_LOOP = false;
	public static boolean CAP_CTRL = false;
	public static boolean MAX_ACC_CTRL = false;
	public static boolean ACC_SMOOTH = false;//Ä¿Ç°ÓÐBUG
	public static boolean APPROACH_CTRL = false;
	public static boolean CF_CURVE = false;
	public static boolean SPD_BUFFER = false;
	public static boolean VIRTUAL_RELEASE = false;

	public static double MAX_ACC = 4.0;
	public static double MAX_DEC = -7.0;
	public static double APPROACH_SPD = 60.0/3.6;
	public static int SPD_BUFFER_VAL = 5;
	public static double CF_VT_END = 200.0;
	public static double CF_VT = 120.0/3.6;
	public static double LC_SENSITIVE = 0.5;
}
