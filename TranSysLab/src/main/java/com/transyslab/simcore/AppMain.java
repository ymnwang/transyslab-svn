package com.transyslab.simcore;

import com.transyslab.gui.MainWindow;

import javax.swing.SwingUtilities;


public class AppMain {
		
	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new MainWindow();
			}
		});
	}
		

}


