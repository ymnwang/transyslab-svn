package com.transyslab.commons.renderer;

import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

public class SubWindow extends JFrame{
	
	private JPanel contentPane;
	private int windowWidth = 300;
	private int windowHeight = 400;
	private String windowTitle;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;

	/**
	 * Create the frame.
	 */
	public SubWindow() {
		//设置窗口关闭
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		int screenWidth = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		int screenHeight = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		setBounds(screenWidth/2 - windowWidth/2, screenHeight/2-windowHeight/2, windowWidth, windowHeight);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
	}

	//type=1:新建项目;type=2:方案设置;type=3:参数设置
	public void init(int type){
		if(type == 1){
			windowTitle = "新建项目";
			JButton btnNewButton = new JButton("\u4E0B\u4E00\u6B65");
			btnNewButton.setFont(new Font("宋体", Font.PLAIN, 13));
			
			JButton button = new JButton("\u53D6\u6D88");
			button.setFont(new Font("宋体", Font.PLAIN, 13));
			
			JLabel label = new JLabel("\u9879\u76EE\u540D\u79F0\uFF1A");
			label.setFont(new Font("宋体", Font.PLAIN, 15));
			
			textField = new JTextField();
			textField.setColumns(10);
			
			JLabel label_1 = new JLabel("\u4FDD\u5B58\u8DEF\u5F84\uFF1A");
			label_1.setFont(new Font("宋体", Font.PLAIN, 15));
			
			textField_1 = new JTextField();
			textField_1.setColumns(10);
			
			JButton button_1 = new JButton("...");
			button_1.setFont(new Font("宋体", Font.PLAIN, 12));
			
			JLabel label_2 = new JLabel("\u8DEF\u7F51\u6587\u4EF6\uFF1A");
			label_2.setFont(new Font("宋体", Font.PLAIN, 15));
			
			textField_2 = new JTextField();
			textField_2.setColumns(10);
			
			JButton button_2 = new JButton("...");
			button_2.setFont(new Font("宋体", Font.PLAIN, 12));
			GroupLayout gl_contentPane = new GroupLayout(contentPane);
			gl_contentPane.setHorizontalGroup(
				gl_contentPane.createParallelGroup(Alignment.LEADING)
					.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
						.addContainerGap(156, Short.MAX_VALUE)
						.addComponent(btnNewButton)
						.addGap(18)
						.addComponent(button, GroupLayout.PREFERRED_SIZE, 69, GroupLayout.PREFERRED_SIZE)
						.addGap(24))
					.addGroup(gl_contentPane.createSequentialGroup()
						.addGap(25)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
							.addGroup(gl_contentPane.createSequentialGroup()
								.addComponent(label)
								.addGap(18)
								.addComponent(textField, GroupLayout.PREFERRED_SIZE, 156, GroupLayout.PREFERRED_SIZE))
							.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
								.addGroup(gl_contentPane.createSequentialGroup()
									.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
									.addGap(18)
									.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, 156, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(button_1, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_contentPane.createSequentialGroup()
									.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
									.addGap(18)
									.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, 156, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(button_2, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))))
						.addContainerGap(36, Short.MAX_VALUE))
			);
			gl_contentPane.setVerticalGroup(
				gl_contentPane.createParallelGroup(Alignment.LEADING)
					.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
						.addGap(54)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
							.addComponent(label)
							.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGap(58)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
							.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
								.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(button_1, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)))
						.addGap(52)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
							.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
								.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(button_2)))
						.addPreferredGap(ComponentPlacement.RELATED, 108, Short.MAX_VALUE)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
							.addComponent(btnNewButton)
							.addComponent(button))
						.addContainerGap())
			);
			contentPane.setLayout(gl_contentPane);
			
		}
		
	}
}
