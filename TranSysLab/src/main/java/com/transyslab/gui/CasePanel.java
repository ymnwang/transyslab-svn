package com.transyslab.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import com.transyslab.commons.io.FileUtils;
import com.transyslab.commons.tools.Producer;
import com.transyslab.commons.tools.Worker;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.roadnetwork.RoadNetworkPool;
import com.transyslab.simcore.AppSetup;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mesots.MesoNetworkPool;
import com.transyslab.simcore.mlp.MLPNetworkPool;

public class CasePanel extends JPanel{
	/*
	private JTextField textField;   //��������
	private JTextField textField[1]; //�����ļ�·��
	private JTextField textField[2]; //��ʼʱ��:hour
	private JTextField textField[3]; //��ʼʱ��:minute
	private JTextField textField[4]; //��ʼʱ��:second
	private JTextField textField[5]; //����ʱ��:hour
	private JTextField textField[6]; //����ʱ��:minute
	private JTextField textField[7]; //����ʱ��:second
	private JTextField textField[8]; //���沽��:second*/
	private JTextField[] textFields;
	

	public CasePanel(){
		textFields = new JTextField[9];
		
		
		JButton button = new JButton("ȡ��");
		button.setFont(new Font("����", Font.PLAIN, 13));
		button.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				//�˳�����
				SubWindow.getInstance().setVisible(false);
				//text�����ÿ�
				clearTextFields();
			}
		});
		JLabel label = new JLabel("��������");
		label.setFont(new Font("����", Font.PLAIN, 15));
		//���÷�������
		textFields[0] = new JTextField("�����뷽������");
		textFields[0].setColumns(10);
		
		JLabel label_2 = new JLabel("�����ļ�");
		label_2.setFont(new Font("����", Font.PLAIN, 15));
		//���������ļ�·��
		textFields[1] = new JTextField("��ָ�������ļ�");
		textFields[1].setColumns(10);
		
		JButton button_2 = new JButton("...");
		button_2.setFont(new Font("����", Font.PLAIN, 12));
		button_2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser("src/main/resources");
				fileChooser.setDialogTitle("ѡ��ͨ�����ļ�");
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setFileFilter(new FileNameExtensionFilter("XML/CSV�ļ�","xml","csv"));
				int state = fileChooser.showOpenDialog(null);
				if(state == JFileChooser.APPROVE_OPTION){
					File file = fileChooser.getSelectedFile();
					textFields[1].setText(file.getAbsolutePath());
				}
				
			}
		});
		
		JLabel label_4 = new JLabel("��ʼʱ��");
		label_4.setFont(new Font("����", Font.PLAIN, 15));
		
		JLabel label_5 = new JLabel("����ʱ��");
		label_5.setFont(new Font("����", Font.PLAIN, 15));
		
		JLabel label_1 = new JLabel("���沽��");
		label_1.setFont(new Font("����", Font.PLAIN, 15));
		
		JLabel label_3 = new JLabel("ʱ");
		label_3.setFont(new Font("����", Font.PLAIN, 15));
		//���ÿ�ʼʱ��:ʱ
		textFields[2] = new JTextField("8");
		textFields[2].setColumns(10);
		
		JLabel label_6 = new JLabel("��");
		label_6.setFont(new Font("����", Font.PLAIN, 15));
		
		JLabel label_7 = new JLabel("��");
		label_7.setFont(new Font("����", Font.PLAIN, 15));
		//���ÿ�ʼʱ��:��
		textFields[3] = new JTextField("0");
		textFields[3].setColumns(10);
		//���ÿ�ʼʱ��:��
		textFields[4] = new JTextField("0");
		textFields[4].setColumns(10);
		//���ý���ʱ��:ʱ
		textFields[5] = new JTextField("9");
		textFields[5].setColumns(10);
		
		JLabel label_8 = new JLabel("ʱ");
		label_8.setFont(new Font("����", Font.PLAIN, 15));
		//���ý���ʱ��:��
		textFields[6] = new JTextField("0");
		textFields[6].setColumns(10);
		
		JLabel label_9 = new JLabel("��");
		label_9.setFont(new Font("����", Font.PLAIN, 15));
		//���ý���ʱ��:��
		textFields[7] = new JTextField("0");
		textFields[7].setColumns(10);
		
		JLabel label_10 = new JLabel("��");
		label_10.setFont(new Font("����", Font.PLAIN, 15));
		//���÷��沽��:��
		textFields[8] = new JTextField("0.2");
		textFields[8].setColumns(10);
		
		JLabel label_11 = new JLabel("��");
		label_11.setFont(new Font("����", Font.PLAIN, 15));
		
		JButton btnS = new JButton("��һ��");
		btnS.setFont(new Font("����", Font.PLAIN, 13));
		btnS.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				//����Text_Field����
				SubWindow.createPanel("�½���Ŀ", ProjectPanel.class);
			}
		});
		JLabel label_12 = new JLabel("����ģ��");
		label_12.setFont(new Font("����", Font.PLAIN, 15));
		
		JComboBox comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"MesoTS", "MLP"}));
		JButton btnNewButton = new JButton("���");
		btnNewButton.setFont(new Font("����", Font.PLAIN, 13));
		btnNewButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
//				if(checkTextFields()){
					//���ش���
					SubWindow.getInstance().setVisible(false);
					//����������õĲ���
					Configurations configs = new Configurations();
					

					AppSetup.setupParameter.put("��������", textFields[0].getText());
					AppSetup.setupParameter.put("����·��", textFields[1].getText());
					AppSetup.startTime = Double.parseDouble(textFields[2].getText())*3600
							+ Double.parseDouble(textFields[3].getText())*60
							+ Double.parseDouble(textFields[4].getText());
					AppSetup.endTime = Double.parseDouble(textFields[5].getText())*3600
							+ Double.parseDouble(textFields[6].getText())*60
							+ Double.parseDouble(textFields[7].getText());
					AppSetup.timeStep = Double.parseDouble(textFields[8].getText());
					String modelName = (String) comboBox.getSelectedItem();
					if(modelName.equals("MesoTS"))
						AppSetup.modelType = 1;
					else
						AppSetup.modelType = 2;
					StringBuilder projPath = new StringBuilder(AppSetup.setupParameter.get("��Ŀ·��"));
					projPath.append("\\").append(AppSetup.setupParameter.get("��Ŀ����")).append(".properties");
					//�½��ļ�
					if(FileUtils.createFile(projPath.toString())){
						//д��.properties�ļ�
						try{
							FileBasedConfigurationBuilder<PropertiesConfiguration> builder = 
									configs.propertiesBuilder(projPath.toString());
							PropertiesConfiguration config = builder.getConfiguration();
							config.addProperty("projectName", AppSetup.setupParameter.get("��Ŀ����"));
							config.addProperty("networkPath", AppSetup.setupParameter.get("·��·��"));
							config.addProperty("caseName", textFields[0].getText());
							LocalDateTime createDateTime = LocalDateTime.now();
							config.addProperty("createTime", createDateTime.toString());
							config.addProperty("demandPath", AppSetup.setupParameter.get("����·��"));
							config.addProperty("simModel", modelName);
							//��ʼʱ�䣬ʱ���֣���
							LocalTime stTime = LocalTime.of(Integer.parseInt(textFields[2].getText()), 
									                      Integer.parseInt(textFields[3].getText()), 
									                      Integer.parseInt(textFields[4].getText()));
							
							config.addProperty("startTime", stTime.toString());
							//����ʱ�䣬ʱ���֣���
							LocalTime edTime = LocalTime.of(Integer.parseInt(textFields[5].getText()), 
				                      Integer.parseInt(textFields[6].getText()), 
				                      Integer.parseInt(textFields[7].getText()));
							config.addProperty("endTime", edTime.toString());
							config.addProperty("simStep", textFields[8].getText());
							builder.save();
						}catch(ConfigurationException cex){
						    cex.printStackTrace();
						}
					}
					
					//text�����ÿ�
					clearTextFields();
					//TODO �������Ŀ���½���Ŀ��ť�ĳ�ͻ
					MainWindow.getInstance().initSimEngines();
					
	/*			}
				else{
					JOptionPane.showMessageDialog(null, "����дȫ����Ϣ");
					return;
				}*/
			}
		});
		
		GroupLayout gl_contentPane = new GroupLayout(this);
		gl_contentPane.setHorizontalGroup(
				gl_contentPane.createParallelGroup(Alignment.TRAILING)
					.addGroup(gl_contentPane.createSequentialGroup()
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
							.addGroup(gl_contentPane.createSequentialGroup()
								.addGap(44)
								.addComponent(btnS)
								.addGap(18)
								.addComponent(btnNewButton, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE)
								.addGap(18)
								.addComponent(button, GroupLayout.PREFERRED_SIZE, 69, GroupLayout.PREFERRED_SIZE))
							.addGroup(gl_contentPane.createSequentialGroup()
								.addGap(28)
								.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
									.addGroup(Alignment.LEADING, gl_contentPane.createSequentialGroup()
										.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(textFields[1], GroupLayout.PREFERRED_SIZE, 156, GroupLayout.PREFERRED_SIZE)
										.addGap(2)
										.addComponent(button_2, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
									.addGroup(Alignment.LEADING, gl_contentPane.createParallelGroup(Alignment.LEADING)
										.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
											.addComponent(label_4, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(textFields[2], GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(label_3, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(textFields[3], GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(label_6, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(textFields[4], GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(label_7)
											.addGap(57))
										.addGroup(gl_contentPane.createSequentialGroup()
											.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
												.addComponent(label)
												.addComponent(label_12, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE))
											.addGap(18)
											.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
												.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addComponent(textFields[0], 171, 171, 171)))
										.addGroup(gl_contentPane.createSequentialGroup()
											.addComponent(label_5, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(textFields[5], GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(label_8, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(textFields[6], GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(label_9, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(textFields[7], GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(label_10, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE))
										.addGroup(gl_contentPane.createSequentialGroup()
											.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(textFields[8], GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(label_11, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE))))))
						.addGap(41))
			);
			gl_contentPane.setVerticalGroup(
				gl_contentPane.createParallelGroup(Alignment.TRAILING)
					.addGroup(gl_contentPane.createSequentialGroup()
						.addGap(22)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
							.addComponent(label)
							.addComponent(textFields[0], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGap(29)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
							.addComponent(label_12, GroupLayout.DEFAULT_SIZE, 18, Short.MAX_VALUE)
							.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGap(26)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
							.addComponent(label_4, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
							.addComponent(textFields[2], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(label_3, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
							.addComponent(textFields[3], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(label_6, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
							.addComponent(textFields[4], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(label_7, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
						.addGap(30)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
							.addComponent(label_5, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
							.addComponent(label_10, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
							.addComponent(textFields[7], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(label_9, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
							.addComponent(textFields[6], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(label_8, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
							.addComponent(textFields[5], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGap(33)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
							.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
							.addComponent(label_11, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
							.addComponent(textFields[8], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGap(31)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
							.addComponent(button_2)
							.addComponent(textFields[1], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
						.addGap(35)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
							.addComponent(btnS, GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
							.addComponent(button)
							.addComponent(btnNewButton))
						.addGap(15))
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
