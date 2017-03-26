package com.transyslab.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.transyslab.simcore.AppMain;
import com.transyslab.simcore.AppSetup;

public class ProjectPanel extends JPanel{
	/*
	private JTextField textField;   //��Ŀ����
	private JTextField textField_1; //��Ŀ����·��
	private JTextField textField_2; //·���ļ�·��*/
	private JTextField[] textFields;
		
	public ProjectPanel(){
		textFields = new JTextField[3];
		JButton btnNewButton = new JButton("��һ��");
		btnNewButton.setFont(new Font("����", Font.PLAIN, 13));
		btnNewButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
		/*		if(checkTextFields()){*/
					SubWindow.createPanel("���÷���", CasePanel.class);
					AppSetup.setupParameter.put("��Ŀ����", textFields[0].getText());
					AppSetup.setupParameter.put("��Ŀ·��", textFields[1].getText());
					AppSetup.setupParameter.put("·��·��", textFields[2].getText());
	//			}
			/*	else{
					JOptionPane.showMessageDialog(null, "����дȫ����Ϣ");
					return;
				}*/
			}
		});

		JButton button = new JButton("ȡ��");
		button.setFont(new Font("����", Font.PLAIN, 13));
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				//���ش���
				SubWindow.getInstance().setVisible(false);
				//text�����ÿ�
				clearTextFields();
			}
		});
		
		JLabel label = new JLabel("\u9879\u76EE\u540D\u79F0\uFF1A");
		label.setFont(new Font("����", Font.PLAIN, 15));
		
		textFields[0] = new JTextField();
		textFields[0].setColumns(10);
		
		JLabel label_1 = new JLabel("\u4FDD\u5B58\u8DEF\u5F84\uFF1A");
		label_1.setFont(new Font("����", Font.PLAIN, 15));
		// ����������Ŀ�ļ�·��
		textFields[1] = new JTextField();
		textFields[1].setColumns(10);
		// �ļ�ѡ����
		JButton button_1 = new JButton("...");
		button_1.setFont(new Font("����", Font.PLAIN, 12));
		button_1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				//�ļ�ѡ������ѡ����Ŀ�ļ������·��
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogTitle("ѡ����Ŀ�ļ������·��");
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int state = fileChooser.showSaveDialog(null);
				if(state == JFileChooser.APPROVE_OPTION){
					File file = fileChooser.getSelectedFile();
					textFields[1].setText(file.getAbsolutePath());
					
				}

			}
		});
		
		JLabel label_2 = new JLabel("\u8DEF\u7F51\u6587\u4EF6\uFF1A");
		label_2.setFont(new Font("����", Font.PLAIN, 15));
		
		textFields[2] = new JTextField();
		textFields[2].setColumns(10);
		
		JButton button_2 = new JButton("...");
		button_2.setFont(new Font("����", Font.PLAIN, 12));
		button_2.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser("E:\\javacode\\git\\TranSysLab\\TranSysLab\\src\\main\\resources");
				fileChooser.setDialogTitle("ѡ��·���ļ�");
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setFileFilter(new FileNameExtensionFilter("XML�ļ�","xml"));
				int state = fileChooser.showOpenDialog(null);
				if(state == JFileChooser.APPROVE_OPTION){
					File file = fileChooser.getSelectedFile();
					textFields[2].setText(file.getAbsolutePath());
				}
				
			}
		});
		GroupLayout gl_contentPane = new GroupLayout(this);
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
							.addComponent(textFields[0], GroupLayout.PREFERRED_SIZE, 156, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
							.addGroup(gl_contentPane.createSequentialGroup()
								.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
								.addGap(18)
								.addComponent(textFields[1], GroupLayout.PREFERRED_SIZE, 156, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(button_1, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
							.addGroup(gl_contentPane.createSequentialGroup()
								.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
								.addGap(18)
								.addComponent(textFields[2], GroupLayout.PREFERRED_SIZE, 156, GroupLayout.PREFERRED_SIZE)
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
						.addComponent(textFields[0], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(58)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
							.addComponent(textFields[1], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(button_1, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)))
					.addGap(52)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
							.addComponent(textFields[2], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(button_2)))
					.addPreferredGap(ComponentPlacement.RELATED, 108, Short.MAX_VALUE)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnNewButton)
						.addComponent(button))
					.addContainerGap())
		);
		
		this.setLayout(gl_contentPane);
	}
	private boolean checkTextFields(){
		for(JTextField text:textFields){
			if(text.getText().equals(""))
				return false;
		}
		return true;
	}
	public void clearTextFields(){
		for(JTextField text:textFields){
			text.setText("");
		}
	}


}
