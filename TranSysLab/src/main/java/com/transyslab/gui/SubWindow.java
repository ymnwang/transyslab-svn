package com.transyslab.gui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class SubWindow extends JFrame{
	
	private int windowWidth = 350;
	private int windowHeight = 440;
	private static SubWindow theWindow = new SubWindow();
	private static Map<String, JPanel> panels;

	/**
	 * Create the frame.
	 */
	private SubWindow() {
		//设置窗口关闭
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		int screenWidth = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		int screenHeight = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		setBounds(screenWidth/2 - windowWidth/2, screenHeight/2-windowHeight/2, windowWidth, windowHeight);
		panels = new HashMap<String, JPanel>();

	}
	public static SubWindow getInstance(){
		return theWindow;
	}
	
	public static void createPanel(String panelName, Class whichPanel){
		
		JPanel thePanel = panels.get(panelName);
		try {
			if (thePanel == null ) {
				Constructor constructor = whichPanel.getConstructors()[0];
				thePanel = (JPanel) constructor.newInstance();
				panels.put(panelName, thePanel);
			}
			
			if(!theWindow.isVisible()&& panelName == "新建项目"){//首次打开窗体
				Method method = whichPanel.getDeclaredMethod("clearTextFields");
				method.invoke(thePanel);
			}
			theWindow.setContentPane(thePanel);
			theWindow.setTitle(panelName);
			theWindow.setVisible(true);
			theWindow.validate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
