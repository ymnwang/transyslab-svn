package com.transyslab.simcore;

import com.transyslab.gui.Arrow2D;
import com.transyslab.gui.MainWindow;
import com.transyslab.roadnetwork.SignalStage;

import javax.swing.SwingUtilities;
import java.util.HashMap;


public class AppMain {
		
	public static void main(String[] args) {
		SignalStage.mapDirInt = new HashMap<>();
		SignalStage.mapDirInt.put("182_185",new int[]{1,0});
		SignalStage.mapDirInt.put("182_230",new int[]{1,0});
		SignalStage.mapDirInt.put("186_181",new int[]{-1,0});
		SignalStage.mapDirInt.put("186_183",new int[]{-1,0});
		SignalStage.mapDirInt.put("184_230",new int[]{0,-1});
		SignalStage.mapDirInt.put("184_181",new int[]{0,-1});
		SignalStage.mapDirInt.put("229_183",new int[]{0,1});
		SignalStage.mapDirInt.put("229_185",new int[]{0,1});
		SignalStage.mapDirString = new HashMap<>();
		SignalStage.mapDirString.put("182_185",Arrow2D.STRAIGHTARROW);
		SignalStage.mapDirString.put("182_230",Arrow2D.LEFTARROW);
		SignalStage.mapDirString.put("186_181",Arrow2D.STRAIGHTARROW);
		SignalStage.mapDirString.put("186_183",Arrow2D.LEFTARROW);
		SignalStage.mapDirString.put("184_230",Arrow2D.STRAIGHTARROW);
		SignalStage.mapDirString.put("184_181",Arrow2D.LEFTARROW);
		SignalStage.mapDirString.put("229_183",Arrow2D.STRAIGHTARROW);
		SignalStage.mapDirString.put("229_185",Arrow2D.LEFTARROW);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new MainWindow();
			}
		});
	}
		

}


