package unit_test;

import jhplot.HPlot;
import jhplot.P0I;
import jhplot.math.LinearAlgebra;
import jhplot.math.Random;

public class testHistogram {

	public static void main(String[] args) {
		int[] tdata = new int[10];
		double[] arg0 = new double[10];
		double[] arg1 = new double[10];
		LinearAlgebra.minus(arg0, arg1);
		for(int i=0; i<9;i ++){
			tdata[i] = 0;
		}
		P0I test = new P0I(tdata);
		HPlot data = new HPlot();
		data.visible();
	}

}
