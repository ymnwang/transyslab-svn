package com.transyslab.commons.renderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import com.jogamp.opengl.util.FPSAnimator;

public class WinForm extends JFrame {
	// Define constants for the top-level container
	private String title_ = "TranSysLab"; // window's title
	private int fps_; // animator's target frames per second
	private JOGLCanvas canvas_;
	private FPSAnimator animator_;
	
	private JPanel contentPane;
	private JTextField txtLane;
	
	/** Constructor to setup the top-level container and animator */
	public WinForm() {
		// Create the OpenGL rendering canvas
		canvas_ = new JOGLCanvas();
		fps_ = 25;
		

		//
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1054, 717);
		contentPane = new JPanel();
		contentPane.setBackground(UIManager.getColor("Button.light"));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		

		canvas_.setBounds(0, 77, 763, 460);

		
		JLabel label = new JLabel("\u63A7\u5236\u53F0\u4FE1\u606F");
		label.setFont(new Font("宋体", Font.PLAIN, 15));
		label.setBackground(Color.LIGHT_GRAY);
		label.setBounds(0, 535, 101, 28);
		contentPane.add(label);
		
		JLabel label_3 = new JLabel("\u6587\u4EF6");
		label_3.setFont(new Font("宋体", Font.PLAIN, 18));
		label_3.setBounds(778, 84, 87, 28);
		contentPane.add(label_3);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBorderPainted(false);
		menuBar.setBounds(0, 0, 874, 40);
		contentPane.add(menuBar);
		
		JMenu menu = new JMenu("\u6587\u4EF6");
		menu.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 15));
		menuBar.add(menu);
		
		JMenuItem menuItem = new JMenuItem("\u65B0\u5EFA");
		menu.add(menuItem);
		
		JMenuItem menuItem_1 = new JMenuItem("\u6253\u5F00");
		menu.add(menuItem_1);
		
		JMenuItem menuItem_2 = new JMenuItem("\u5173\u95ED");
		menu.add(menuItem_2);
		
		JMenu menu_1 = new JMenu("\u7F16\u8F91");
		menu_1.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 15));
		menuBar.add(menu_1);
		
		JMenu menu_2 = new JMenu("\u8FD0\u884C");
		menu_2.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 15));
		menuBar.add(menu_2);
		
		JMenu menu_3 = new JMenu("\u7A97\u53E3");
		menu_3.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 15));
		menuBar.add(menu_3);
		
		JMenu menu_4 = new JMenu("\u67E5\u770B");
		menu_4.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 15));
		menuBar.add(menu_4);
		
		JMenu menu_5 = new JMenu("\u65B9\u6848");
		menu_5.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 15));
		menuBar.add(menu_5);
		
		JMenu menu_6 = new JMenu("\u5DE5\u5177");
		menu_6.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 15));
		menuBar.add(menu_6);
		
		JMenu menu_7 = new JMenu("\u5E2E\u52A9");
		menu_7.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 15));
		menuBar.add(menu_7);
		
		JToolBar toolBar = new JToolBar();
		toolBar.setBounds(0, 38, 274, 33);
		contentPane.add(toolBar);
		
		JButton btnNewButton = new JButton("");
		btnNewButton.setIcon(new ImageIcon("src/main/resources/icon/play.png"));
		toolBar.add(btnNewButton);
		
		JButton button = new JButton("");
		button.setIcon(new ImageIcon("src/main/resources/icon/pause.png"));
		toolBar.add(button);
		
		JButton btnNewButton_1 = new JButton("");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnNewButton_1.setIcon(new ImageIcon("src/main/resources/icon/stop.png"));
		toolBar.add(btnNewButton_1);
		
		JSlider slider = new JSlider();
		slider.setPaintTicks(true);
		slider.setValue(30);
		toolBar.add(slider);
		
		JTextArea textArea = new JTextArea();
		textArea.setForeground(new Color(0, 0, 0));
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
		textArea.setText("\u4EFF\u771F\u65B9\u6848\uFF1A\u5185\u73AF\u8DEF1\u6708\u4EFD\u73B0\u72B6\u4EFF\u771F   \r\n\u4EFF\u771F\u65F6\u95F4\uFF1A1\u670821\u65E58:15-20:30   \r\n\u8FD0\u884C\u72B6\u6001\uFF1A\u6B63\u5728\u8FD0\u884C...");
		textArea.setBounds(0, 557, 1038, 111);
		contentPane.add(textArea);
		
		JFormattedTextField formattedTextField = new JFormattedTextField();
		formattedTextField.setFont(new Font("宋体", Font.PLAIN, 15));
		formattedTextField.setText("\u641C\u7D22");
		formattedTextField.setToolTipText("\u641C\u7D22");
		formattedTextField.setBounds(907, 0, 131, 40);
		contentPane.add(formattedTextField);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.setBounds(778, 111, 260, 139);
		contentPane.add(panel);
		panel.setLayout(null);
		
		JButton button_1 = new JButton("\u65B0\u5EFA\u9879\u76EE");
		button_1.setFont(new Font("宋体", Font.PLAIN, 15));
		button_1.setBounds(8, 22, 116, 33);
		panel.add(button_1);
		
		JButton button_3 = new JButton("\u53E6\u5B58\u4E3A");
		button_3.setFont(new Font("宋体", Font.PLAIN, 15));
		button_3.setBounds(8, 76, 116, 33);
		panel.add(button_3);
		
		JButton button_2 = new JButton("\u52A0\u8F7D\u8DEF\u7F51");
		button_2.setFont(new Font("宋体", Font.PLAIN, 15));
		button_2.setBounds(134, 22, 116, 33);
		panel.add(button_2);
		
		JButton button_4 = new JButton("\u9000\u51FA");
		button_4.setFont(new Font("宋体", Font.PLAIN, 15));
		button_4.setBounds(134, 76, 116, 33);
		panel.add(button_4);
		
		JPanel panel_1 = new JPanel();
		panel_1.setLayout(null);
		panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel_1.setBounds(778, 299, 260, 224);
		contentPane.add(panel_1);
		
		JComboBox comboBox = new JComboBox();
		comboBox.setFont(new Font("宋体", Font.PLAIN, 15));
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"\u5141\u8BB8\u5DE6\u53F3\u6362\u9053", "\u5141\u8BB8\u5DE6\u6362\u9053", "\u5141\u8BB8\u53F3\u6362\u9053", "\u4E0D\u5141\u8BB8\u6362\u9053"}));
		comboBox.setBounds(99, 95, 134, 30);
		panel_1.add(comboBox);
		
		JLabel label_1 = new JLabel("\u6A2A\u5411\u89C4\u5219");
		label_1.setFont(new Font("宋体", Font.PLAIN, 15));
		label_1.setBounds(10, 95, 79, 30);
		panel_1.add(label_1);
		
		JComboBox comboBox_1 = new JComboBox();
		comboBox_1.setFont(new Font("宋体", Font.PLAIN, 15));
		comboBox_1.setModel(new DefaultComboBoxModel(new String[] {"\u76F4\u5DE6", "\u76F4\u53F3", "\u76F4\u5DE6\u53F3", "\u4E13\u5DE6", "\u4E13\u53F3"}));
		comboBox_1.setBounds(99, 160, 134, 30);
		panel_1.add(comboBox_1);
		
		JLabel label_2 = new JLabel("\u7EB5\u5411\u89C4\u5219");
		label_2.setFont(new Font("宋体", Font.PLAIN, 15));
		label_2.setBounds(10, 156, 106, 38);
		panel_1.add(label_2);
		
		JLabel label_5 = new JLabel("\u5F53\u524D\u5BF9\u8C61");
		label_5.setFont(new Font("宋体", Font.PLAIN, 15));
		label_5.setBounds(10, 28, 62, 30);
		panel_1.add(label_5);
		
		txtLane = new JTextField();
		txtLane.setFont(new Font("宋体", Font.PLAIN, 15));
		txtLane.setEditable(false);
		txtLane.setText("Lane31101");
		txtLane.setBounds(99, 29, 134, 30);
		panel_1.add(txtLane);
		txtLane.setColumns(10);
		
		JLabel label_4 = new JLabel("\u9053\u8DEF\u89C4\u5219\u7F16\u8F91");
		label_4.setFont(new Font("宋体", Font.PLAIN, 18));
		label_4.setBounds(778, 271, 260, 28);
		contentPane.add(label_4);
	}
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
	public WinForm(int width, int height, int fps) {
		// Create the OpenGL rendering canvas
		canvas_ = new JOGLCanvas(width, height);
		fps_ = fps;
	}
	@Override
	public void setTitle(String s) {
		title_ = s;
	}
	public void init() {

		// canvas_ = new JOGL_Canvas();
		// canvas_.setPreferredSize(new Dimension(canvasWidth_, canvasHeight_));
		JOGLCamera cam = new JOGLCamera();
		canvas_.setCamera(cam);
		canvas_.addKeyListener(cam);
		canvas_.addMouseListener(cam);
		canvas_.addMouseWheelListener(cam);
		canvas_.addMouseMotionListener(cam);
		// Create a animator that drives canvas' display() at the specified FPS.
		animator_ = new FPSAnimator(canvas_, fps_, true);

		// Create the top-level container frame
		//
		this.getContentPane().add(canvas_);
		this.addWindowListener(new WindowAdapter() {
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
		this.setTitle(title_);
		this.pack();
		this.setVisible(true);

	}
	public void render() {
		animator_.start(); // start the animation loop
	}
}
