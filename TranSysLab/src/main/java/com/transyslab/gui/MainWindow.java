package com.transyslab.gui;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EtchedBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.jogamp.opengl.util.FPSAnimator;
import com.transyslab.commons.renderer.BirdEyeCamera;
import com.transyslab.commons.renderer.Camera;
import com.transyslab.commons.renderer.JOGLCanvas;
import com.transyslab.commons.renderer.FrameQueue;
import com.transyslab.commons.renderer.OrbitCamera;
import com.transyslab.commons.tools.Producer;
import com.transyslab.commons.tools.Worker;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.RoadNetwork;
import com.transyslab.roadnetwork.RoadNetworkPool;
import com.transyslab.simcore.AppSetup;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mesots.MesoNetworkPool;
import com.transyslab.simcore.mlp.MLPNetworkPool;

import jogamp.common.util.locks.RecursiveThreadGroupLockImpl01Unfairish;

public class MainWindow extends JFrame{
	// Define constants for the top-level container
	private String title_ = "TranSysLab"; // window's title
	private int fps_; // animator's target frames per second
	private JOGLCanvas canvas_;
	private FPSAnimator animator_;
	
//	private JFrame frame;
	private JTextField textField;
	private int windowWidth = 810;
	private int windowHeight = 632;
	private static MainWindow theWindow = new MainWindow();
	public static MainWindow getInstance(){
		return theWindow;
	}
	/** Constructor to setup the top-level container and animator */
	private MainWindow() {
		//Complete window design
		initialize();
		// Set rendering canvas
		fps_ = 25;		
		Camera cam = new OrbitCamera();
		canvas_.setCamera(cam);
		canvas_.addKeyListener(canvas_);
		canvas_.addMouseListener(canvas_);
		canvas_.addMouseWheelListener(canvas_);
		canvas_.addMouseMotionListener(canvas_);
		// Create a animator that drives canvas' display() at the specified FPS.
		animator_ = new FPSAnimator(canvas_, fps_, true);

		// Create the top-level container frame
		getContentPane().add(canvas_);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// Use a dedicate thread to run the stop() to ensure that the
				// animator stops before program exits.
				new Thread() {
					@Override
					public void run() {
						if (animator_.isStarted())
							animator_.stop();
						System.exit(0);
					}
				}.start();
			}
		});		
	}
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		int screenWidth = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		int screenHeight = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		canvas_ = new JOGLCanvas();
		
		setBounds(screenWidth/2 - windowWidth/2, screenHeight/2-windowHeight/2, windowWidth, windowHeight);

		setTitle(title_);
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(SystemColor.controlHighlight);
		panel_1.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent e) {
				windowHeight = getHeight();
				windowWidth = getWidth();
				int referHeight = panel_1.getHeight();
				int referWidth = panel_1.getWidth();
				java.awt.Point p = panel_1.getLocation();
				canvas_.setBounds(0, (int) p.getY(), windowWidth-5-referWidth, referHeight);
				}
			}
		);

		
		JPanel panel_2 = new JPanel();
		
		JPanel panel_3 = new JPanel();
		
		JPanel panel_5 = new JPanel();
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		
		JPanel panel_4 = new JPanel();
		
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 586, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 70, Short.MAX_VALUE)
					.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 138, GroupLayout.PREFERRED_SIZE))
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(panel_5, GroupLayout.PREFERRED_SIZE, 235, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(559, Short.MAX_VALUE))
				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
					.addComponent(canvas_, GroupLayout.DEFAULT_SIZE, 552, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 794, Short.MAX_VALUE)
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(2)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(panel, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
							.addGap(3)
							.addComponent(panel_5, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE))
						.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE))
					.addGap(1)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(canvas_, GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
						.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 136, GroupLayout.PREFERRED_SIZE))
		);

		
		JTabbedPane tabbedPane_1 = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("方案", null, tabbedPane_1, null);
		
		JTabbedPane tabbedPane_2 = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("控制台", null, tabbedPane_2, null);
		
		JPanel panel_6 = new JPanel();
		panel_6.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		
		JPanel panel_7 = new JPanel();
		panel_7.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_7.setToolTipText("");
		
		JLabel label = new JLabel("\u6587\u4EF6");
		label.setBackground(Color.LIGHT_GRAY);
		label.setLabelFor(panel_6);
		label.setFont(new Font("宋体", Font.PLAIN, 15));
		
		JLabel label_1 = new JLabel("\u9053\u8DEF\u89C4\u5219");
		label_1.setBackground(Color.LIGHT_GRAY);
		label_1.setLabelFor(panel_7);
		label_1.setFont(new Font("宋体", Font.PLAIN, 15));
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_panel_1.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_1.createParallelGroup(Alignment.TRAILING)
						.addComponent(panel_6, GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
						.addComponent(panel_7, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
						.addComponent(label, Alignment.LEADING)
						.addComponent(label_1, Alignment.LEADING))
					.addContainerGap())
		);
		gl_panel_1.setVerticalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addContainerGap()
					.addComponent(label)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_6, GroupLayout.PREFERRED_SIZE, 146, GroupLayout.PREFERRED_SIZE)
					.addGap(5)
					.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
					.addGap(3)
					.addComponent(panel_7, GroupLayout.PREFERRED_SIZE, 146, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(25, Short.MAX_VALUE))
		);
		
		JLabel label_2 = new JLabel("\u5F53\u524D\u5BF9\u8C61");
		label_2.setFont(new Font("宋体", Font.PLAIN, 14));
		
		textField = new JTextField();
		textField.setText("Lane31101");
		textField.setFont(new Font("宋体", Font.PLAIN, 15));
		textField.setEditable(false);
		textField.setColumns(10);
		
		JLabel label_3 = new JLabel("\u6A2A\u5411\u89C4\u5219");
		label_3.setFont(new Font("宋体", Font.PLAIN, 14));
		
		JLabel label_4 = new JLabel("\u7EB5\u5411\u89C4\u5219");
		label_4.setFont(new Font("宋体", Font.PLAIN, 14));
		
		JComboBox comboBox = new JComboBox();
		comboBox.setFont(new Font("宋体", Font.PLAIN, 14));
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"\u5141\u8BB8\u5DE6\u53F3\u6362\u9053", "\u5141\u8BB8\u5DE6\u6362\u9053", "\u5141\u8BB8\u53F3\u6362\u9053", "\u7981\u6B62\u6362\u9053"}));
		
		JComboBox comboBox_1 = new JComboBox();
		comboBox_1.setModel(new DefaultComboBoxModel(new String[] {"\u76F4\u5DE6", "\u76F4\u53F3", "\u76F4\u5DE6\u53F3", "\u76F4\u884C", "\u4E13\u5DE6", "\u4E13\u53F3"}));
		comboBox_1.setFont(new Font("宋体", Font.PLAIN, 14));
		GroupLayout gl_panel_7 = new GroupLayout(panel_7);
		gl_panel_7.setHorizontalGroup(
			gl_panel_7.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_7.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_7.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_7.createSequentialGroup()
							.addComponent(label_2)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(textField, GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))
						.addGroup(gl_panel_7.createSequentialGroup()
							.addComponent(label_3, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(comboBox, 0, 122, Short.MAX_VALUE))
						.addGroup(gl_panel_7.createSequentialGroup()
							.addComponent(label_4, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
							.addGap(10)
							.addComponent(comboBox_1, GroupLayout.PREFERRED_SIZE, 122, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap())
		);
		gl_panel_7.setVerticalGroup(
			gl_panel_7.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_7.createSequentialGroup()
					.addGroup(gl_panel_7.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_7.createSequentialGroup()
							.addGap(18)
							.addComponent(label_2))
						.addGroup(gl_panel_7.createSequentialGroup()
							.addContainerGap()
							.addComponent(textField, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)))
					.addGap(22)
					.addGroup(gl_panel_7.createParallelGroup(Alignment.BASELINE)
						.addComponent(label_3, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
						.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
					.addGroup(gl_panel_7.createParallelGroup(Alignment.BASELINE)
						.addComponent(comboBox_1, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
						.addComponent(label_4, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE))
					.addGap(18))
		);
		panel_7.setLayout(gl_panel_7);
		
		JButton button = new JButton("\u65B0\u5EFA\u9879\u76EE");
		button.setFont(new Font("宋体", Font.PLAIN, 14));
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				SubWindow.createPanel("新建项目", ProjectPanel.class);
				SubWindow.getInstance().setVisible(true);
				
			}
		});
		
		JButton button_1 = new JButton("\u52A0\u8F7D\u8DEF\u7F51");
		button_1.setFont(new Font("宋体", Font.PLAIN, 14));
		
		JButton button_2 = new JButton("\u53E6\u5B58\u4E3A");
		button_2.setFont(new Font("宋体", Font.PLAIN, 14));
		
		JButton button_3 = new JButton("\u9000\u51FA");
		button_3.setFont(new Font("宋体", Font.PLAIN, 14));
		GroupLayout gl_panel_6 = new GroupLayout(panel_6);
		gl_panel_6.setHorizontalGroup(
			gl_panel_6.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_6.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_6.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_6.createSequentialGroup()
							.addComponent(button)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(button_1, GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE))
						.addGroup(gl_panel_6.createSequentialGroup()
							.addComponent(button_2, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(button_3, GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)))
					.addContainerGap())
		);
		gl_panel_6.setVerticalGroup(
			gl_panel_6.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_6.createSequentialGroup()
					.addGap(31)
					.addGroup(gl_panel_6.createParallelGroup(Alignment.BASELINE)
						.addComponent(button_1)
						.addComponent(button))
					.addGap(33)
					.addGroup(gl_panel_6.createParallelGroup(Alignment.BASELINE)
						.addComponent(button_2)
						.addComponent(button_3))
					.addContainerGap(32, Short.MAX_VALUE))
		);
		panel_6.setLayout(gl_panel_6);
		panel_1.setLayout(gl_panel_1);
		panel_2.setLayout(new GridLayout(1, 0, 0, 0));
		
		JFormattedTextField formattedTextField = new JFormattedTextField();
		formattedTextField.setText("\u641C\u7D22");
		panel_2.add(formattedTextField);
		panel_5.setLayout(new GridLayout(0, 1, 0, 0));
		
		JToolBar toolBar = new JToolBar();
		toolBar.setRollover(true);
		panel_5.add(toolBar);
		
		JButton btnNewButton = new JButton("");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnNewButton.setIcon(new ImageIcon("src/main/resources/icon/play.png"));
		toolBar.add(btnNewButton);
		btnNewButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				/*
				if(!canvas_.isNetworkReady()){
					JOptionPane.showMessageDialog(null, "请先加载路网");
					return;
				}*/
				//从暂停到播放
				if(canvas_.isPause){
					canvas_.isPause = false;
				}
				//第一次播放
				else if(!canvas_.isRendering){
					SimulationEngine[] engineList = ((CasePanel)SubWindow.getInstance().getContentPane()).getSimEngines();
					Worker[] workerList = new Worker[Constants.THREAD_NUM];
					Thread[] threadList = new Thread[Constants.THREAD_NUM];

					FrameQueue.getInstance().initFrameQueue();

					for (int i = 0; i < Constants.THREAD_NUM; i++) {
						workerList[i] = new Worker(engineList[i]);
						threadList[i] = new Thread(workerList[i]);
					}

					RoadNetworkPool.getInstance().organizeHM(threadList);
					for (int i = 0; i < Constants.THREAD_NUM; i++) {
						threadList[i].start();
					}
					canvas_.isRendering = true;
				}
				
			
			}
		});
		
		JButton btnNewButton_1 = new JButton("");
		btnNewButton_1.setIcon(new ImageIcon("src/main/resources/icon/pause.png"));
		toolBar.add(btnNewButton_1);
		btnNewButton_1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {

				canvas_.isPause = true;	
			}
		});
		
		JButton btnNewButton_2 = new JButton("");
		btnNewButton_2.setIcon(new ImageIcon("src/main/resources/icon/stop.png"));
		toolBar.add(btnNewButton_2);
		
		JSlider slider = new JSlider();
		slider.setFont(new Font("宋体", Font.PLAIN, 13));
		toolBar.add(slider);
		panel.setLayout(new GridLayout(1, 1, 0, 0));
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBorderPainted(false);
		menuBar.setForeground(Color.WHITE);
		panel.add(menuBar);
		
		JMenu menu = new JMenu("\u6587\u4EF6");
		menu.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
		menuBar.add(menu);
		
		JMenu mnNewMenu = new JMenu("\u7F16\u8F91");
		mnNewMenu.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
		menuBar.add(mnNewMenu);
		
		JMenu menu_1 = new JMenu("\u8FD0\u884C");
		menu_1.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
		menuBar.add(menu_1);
		
		JMenu menu_2 = new JMenu("\u7A97\u53E3");
		menu_2.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
		menuBar.add(menu_2);
		
		JMenu menu_3 = new JMenu("\u67E5\u770B");
		menu_3.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
		menuBar.add(menu_3);
		
		JMenu menu_4 = new JMenu("\u65B9\u6848");
		menu_4.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
		menuBar.add(menu_4);
		
		JMenu menu_5 = new JMenu("\u5DE5\u5177");
		menu_5.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
		menuBar.add(menu_5);
		
		JMenu menu_6 = new JMenu("\u5E2E\u52A9");
		menu_6.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
		menuBar.add(menu_6);
		getContentPane().setLayout(groupLayout);
	}
	public void render() {
		animator_.start(); // start the animation loop
	}
	public void setNetworkReady(){
		canvas_.setFirstRender(true);
		canvas_.setDrawableNetwork(RoadNetworkPool.getInstance().getNetwork(0));
		canvas_.requestFocusInWindow();
	}

}
