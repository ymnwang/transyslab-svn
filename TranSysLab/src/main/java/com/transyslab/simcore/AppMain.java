package com.transyslab.simcore;

import javax.swing.SwingUtilities;

import com.transyslab.gui.MainWindow;



public class AppMain {
		
	public static void main(String[] args) {
		//����������
		MainWindow appGUI = MainWindow.getInstance(); 
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				appGUI.setVisible(true);
				appGUI.render();
			}
		});
	}
		

}


