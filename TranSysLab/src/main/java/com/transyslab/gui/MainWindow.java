package com.transyslab.gui;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.event.*;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.transyslab.simcore.mesots.MesoEngine;
import com.transyslab.simcore.mesots.MesoNetwork;
import com.transyslab.simcore.mlp.MLPEngine;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;

import com.jogamp.opengl.util.FPSAnimator;
import com.transyslab.commons.io.JdbcUtils;
import com.transyslab.commons.renderer.Camera;
import com.transyslab.commons.renderer.JOGLCanvas;
import com.transyslab.commons.renderer.FrameQueue;
import com.transyslab.commons.renderer.OrbitCamera;
import com.transyslab.commons.tools.DataVisualization;
import com.transyslab.commons.tools.Worker;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.simcore.AppSetup;
import com.transyslab.simcore.SimulationEngine;

import info.monitorenter.gui.chart.traces.Trace2DSimple;

public class MainWindow extends JFrame{
	// Define constants for the top-level container
	private String title_ = "TranSysLab"; // window's title
	private int fps_; // animator's target frames per second
	private JOGLCanvas canvas_;
	private FPSAnimator animator_;
	
	private JTextField textField;
	// 控制台信息
	private JTextArea txtConsole;
	private JTextArea txtCase;
	private int windowWidth = 810;
	private int windowHeight = 632;
	
	private SimulationEngine[] engines;
	private Trace2DSimple traceRT;
	public boolean needRTPlot;
	private static MainWindow theWindow = new MainWindow();
	public static MainWindow getInstance(){
		return theWindow;
	}
	/** Constructor to setup the top-level container and animator */
	private MainWindow() {
		//Complete window design
		initialize();
		// Set rendering canvas
		fps_ = 120;		
		Camera cam = new OrbitCamera();
		canvas_.setCamera(cam);
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
		// 信息显示区
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);


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
		// 带滚动条的面板，显示控制台输出信息
		JScrollPane scrollPane = new JScrollPane();
		tabbedPane.addTab("控制台", null, scrollPane, null);
		this.txtConsole = new JTextArea();
		//设置自动换行
		this.txtConsole.setLineWrap(true);
		scrollPane.setViewportView(txtConsole);


		// 带滚动条的面板，显示方案信息
		JScrollPane scrollPane2 = new JScrollPane();
		tabbedPane.addTab("方案", null, scrollPane2, null);
		this.txtCase = new JTextArea();
		//设置自动换行
		this.txtCase.setLineWrap(true);
		scrollPane2.setViewportView(txtCase);


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
		
		JButton button_1 = new JButton("打开项目");
		button_1.setFont(new Font("宋体", Font.PLAIN, 14));
		button_1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser("src/main/resources");
				fileChooser.setDialogTitle("选择项目文件");
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setFileFilter(new FileNameExtensionFilter("配置文件","properties"));
				int state = fileChooser.showOpenDialog(null);
				if(state == JFileChooser.APPROVE_OPTION){
					File file = fileChooser.getSelectedFile();
					Configurations configs = new Configurations();

					try{
						Configuration config = configs.properties(file);
						String projectName = config.getString("projectName");
					    String networkPath = config.getString("networkPath");
					    String caseName = config.getString("caseName");
					    String createTime = config.getString("createTime");
					    String simModel = config.getString("simModel");
					    String startTime =  config.getString("startTime");
					    String endTime = config.getString("endTime");
					    String demandPath = config.getString("demandPath");
					    Float simStep = config.getFloat("simStep");
					    LocalTime stTime = LocalTime.parse(startTime);
					    AppSetup.startTime = stTime.getHour()*3600+stTime.getMinute()*60+stTime.getSecond();
					    LocalTime edTime = LocalTime.parse(endTime);
					    AppSetup.endTime = edTime.getHour()*3600+edTime.getMinute()*60+edTime.getSecond();
					    AppSetup.setupParameter.put("项目名称", projectName);
					    AppSetup.setupParameter.put("路网路径",networkPath);
					    AppSetup.setupParameter.put("方案名称", caseName);
					    AppSetup.setupParameter.put("需求路径", demandPath);
					    AppSetup.timeStep = simStep;
					    if(simModel.equals("MesoTS"))	    	
					    	AppSetup.modelType = 1;
					    else {
					    	AppSetup.modelType = 2;
						}
					    initSimEngines();
					}
					catch(ConfigurationException cex)
					{
					    // loading of the configuration file failed
					}
					
				}
				
			}
		});
		JButton button_2 = new JButton("车速统计");
		button_2.setFont(new Font("宋体", Font.PLAIN, 13));
		//TODO 限制多次点击
		button_2.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				//TODO 硬写未设计
				needRTPlot = true;
				new Thread(new Runnable() {
					
					@Override
					public void run() {
			
						QueryRunner qr = new QueryRunner(JdbcUtils.getDataSource());
						String sql = "select C,round(S/C*2*5*3) as hourfolw,meanspeed, D from (select count(\"FLOW\") AS C,sum(\"FLOW\") AS S,sum(\"FLOW\"*\"SPEED\")/(sum(\"FLOW\")+0.0000001) as meanspeed, floor((extract(epoch from \"CTIME\")-extract(epoch from timestamp without time zone '2016-06-20 07:55:00'))/300)*300 AS D from nhschema.\"Loop\"  where \"CPN\" = 'LP/A24' "
								+ "   and (extract(epoch from \"CTIME\")>=extract(epoch from timestamp without time zone '2016-06-20 07:55:00')) and (extract(epoch from \"CTIME\")<=extract(epoch from timestamp without time zone '2016-06-20 09:50:00'))"
								+ " group by floor((extract(epoch from \"CTIME\")-extract(epoch from timestamp without time zone '2016-06-20 07:55:00'))/300)*300) as derivedtable order by D";
						try {
							List result = (List) qr.query(sql, new ColumnListHandler(3));
							traceRT = new Trace2DSimple("仿真车速");
							
							DataVisualization.realTimePlot(traceRT, null, result);
							//System.out.println("");
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}).start();
			}	
		});
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
					FrameQueue.getInstance().initFrameQueue();
					Worker worker = new Worker(engines[0]);
					Thread thread = new Thread(worker);
					thread.start();
				/*
					Worker[] workerList = new Worker[Constants.THREAD_NUM];
					Thread[] threadList = new Thread[Constants.THREAD_NUM];



					for (int i = 0; i < Constants.THREAD_NUM; i++) {
						workerList[i] = new Worker(engines[i]);
						threadList[i] = new Thread(workerList[i]);
					}

//					RoadNetworkPool.getInstance().organizeHM(threadList);
					for (int i = 0; i < Constants.THREAD_NUM; i++) {
						threadList[i].start();
					}*/
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
	public SimulationEngine[] getSimEngines(){
		return engines;
	}
	public Trace2DSimple getTrace2D(){
		return traceRT;
	}
	public JTextArea getTXTConsole(){
		return txtConsole;
	}
	public JTextArea getTXTCase(){
		return txtCase;
	}
	public void initSimEngines(){
		engines = new SimulationEngine[1];
		switch (AppSetup.modelType) {
			case 1:
				engines[0] = new MesoEngine(0,"E:\\test\\");
				break;
			case 2:
				engines[0] = new MLPEngine("src/main/resources/demo_neihuan/scenario2/master.properties");
				break;
				default:
					break;
		}
		engines[0].loadFiles();

		// Network is ready for simulation
		canvas_.setFirstRender(true);
		canvas_.setDrawableNetwork(engines[0].getNetwork());
		canvas_.requestFocusInWindow();
	}
}
