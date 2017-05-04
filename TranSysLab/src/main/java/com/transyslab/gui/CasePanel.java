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
	private JTextField textField;   //方案名称
	private JTextField textField[1]; //需求文件路径
	private JTextField textField[2]; //开始时间:hour
	private JTextField textField[3]; //开始时间:minute
	private JTextField textField[4]; //开始时间:second
	private JTextField textField[5]; //结束时间:hour
	private JTextField textField[6]; //结束时间:minute
	private JTextField textField[7]; //结束时间:second
	private JTextField textField[8]; //仿真步长:second*/
	private JTextField[] textFields;
	

	public CasePanel(){
		textFields = new JTextField[9];
		
		
		JButton button = new JButton("取消");
		button.setFont(new Font("宋体", Font.PLAIN, 13));
		button.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				//退出窗口
				SubWindow.getInstance().setVisible(false);
				//text内容置空
				clearTextFields();
			}
		});
		JLabel label = new JLabel("方案名称");
		label.setFont(new Font("宋体", Font.PLAIN, 15));
		//设置方案名称
		textFields[0] = new JTextField("请输入方案名称");
		textFields[0].setColumns(10);
		
		JLabel label_2 = new JLabel("需求文件");
		label_2.setFont(new Font("宋体", Font.PLAIN, 15));
		//设置需求文件路径
		textFields[1] = new JTextField("请指定需求文件");
		textFields[1].setColumns(10);
		
		JButton button_2 = new JButton("...");
		button_2.setFont(new Font("宋体", Font.PLAIN, 12));
		button_2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser("src/main/resources");
				fileChooser.setDialogTitle("选择交通需求文件");
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setFileFilter(new FileNameExtensionFilter("XML/CSV文件","xml","csv"));
				int state = fileChooser.showOpenDialog(null);
				if(state == JFileChooser.APPROVE_OPTION){
					File file = fileChooser.getSelectedFile();
					textFields[1].setText(file.getAbsolutePath());
				}
				
			}
		});
		
		JLabel label_4 = new JLabel("开始时间");
		label_4.setFont(new Font("宋体", Font.PLAIN, 15));
		
		JLabel label_5 = new JLabel("结束时间");
		label_5.setFont(new Font("宋体", Font.PLAIN, 15));
		
		JLabel label_1 = new JLabel("仿真步长");
		label_1.setFont(new Font("宋体", Font.PLAIN, 15));
		
		JLabel label_3 = new JLabel("时");
		label_3.setFont(new Font("宋体", Font.PLAIN, 15));
		//设置开始时间:时
		textFields[2] = new JTextField("8");
		textFields[2].setColumns(10);
		
		JLabel label_6 = new JLabel("分");
		label_6.setFont(new Font("宋体", Font.PLAIN, 15));
		
		JLabel label_7 = new JLabel("秒");
		label_7.setFont(new Font("宋体", Font.PLAIN, 15));
		//设置开始时间:分
		textFields[3] = new JTextField("0");
		textFields[3].setColumns(10);
		//设置开始时间:秒
		textFields[4] = new JTextField("0");
		textFields[4].setColumns(10);
		//设置结束时间:时
		textFields[5] = new JTextField("9");
		textFields[5].setColumns(10);
		
		JLabel label_8 = new JLabel("时");
		label_8.setFont(new Font("宋体", Font.PLAIN, 15));
		//设置结束时间:分
		textFields[6] = new JTextField("0");
		textFields[6].setColumns(10);
		
		JLabel label_9 = new JLabel("分");
		label_9.setFont(new Font("宋体", Font.PLAIN, 15));
		//设置结束时间:秒
		textFields[7] = new JTextField("0");
		textFields[7].setColumns(10);
		
		JLabel label_10 = new JLabel("秒");
		label_10.setFont(new Font("宋体", Font.PLAIN, 15));
		//设置仿真步长:秒
		textFields[8] = new JTextField("0.2");
		textFields[8].setColumns(10);
		
		JLabel label_11 = new JLabel("秒");
		label_11.setFont(new Font("宋体", Font.PLAIN, 15));
		
		JButton btnS = new JButton("上一步");
		btnS.setFont(new Font("宋体", Font.PLAIN, 13));
		btnS.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				//保留Text_Field内容
				SubWindow.createPanel("新建项目", ProjectPanel.class);
			}
		});
		JLabel label_12 = new JLabel("仿真模型");
		label_12.setFont(new Font("宋体", Font.PLAIN, 15));
		
		JComboBox comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"MesoTS", "MLP"}));
		JButton btnNewButton = new JButton("完成");
		btnNewButton.setFont(new Font("宋体", Font.PLAIN, 13));
		btnNewButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
//				if(checkTextFields()){
					//隐藏窗口
					SubWindow.getInstance().setVisible(false);
					//读入面板设置的参数
					Configurations configs = new Configurations();
					

					AppSetup.setupParameter.put("方案名称", textFields[0].getText());
					AppSetup.setupParameter.put("需求路径", textFields[1].getText());
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
					StringBuilder projPath = new StringBuilder(AppSetup.setupParameter.get("项目路径"));
					projPath.append("\\").append(AppSetup.setupParameter.get("项目名称")).append(".properties");
					//新建文件
					if(FileUtils.createFile(projPath.toString())){
						//写入.properties文件
						try{
							FileBasedConfigurationBuilder<PropertiesConfiguration> builder = 
									configs.propertiesBuilder(projPath.toString());
							PropertiesConfiguration config = builder.getConfiguration();
							config.addProperty("projectName", AppSetup.setupParameter.get("项目名称"));
							config.addProperty("networkPath", AppSetup.setupParameter.get("路网路径"));
							config.addProperty("caseName", textFields[0].getText());
							LocalDateTime createDateTime = LocalDateTime.now();
							config.addProperty("createTime", createDateTime.toString());
							config.addProperty("demandPath", AppSetup.setupParameter.get("需求路径"));
							config.addProperty("simModel", modelName);
							//开始时间，时：分：秒
							LocalTime stTime = LocalTime.of(Integer.parseInt(textFields[2].getText()), 
									                      Integer.parseInt(textFields[3].getText()), 
									                      Integer.parseInt(textFields[4].getText()));
							
							config.addProperty("startTime", stTime.toString());
							//结束时间，时：分：秒
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
					
					//text内容置空
					clearTextFields();
					//TODO 避免打开项目与新建项目按钮的冲突
					MainWindow.getInstance().initSimEngines();
					
	/*			}
				else{
					JOptionPane.showMessageDialog(null, "请填写全部信息");
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
