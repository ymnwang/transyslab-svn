package com.transyslab.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yali on 2017/8/27.
 */
public class SubWindow1 {
    public JFrame windowFrame;
    private ProjectPanel projectPanel;
    private CasePanel casePanel;
    private CardLayout cardLayout;
    //private Map<String,JPanel> panels;
    public SubWindow1(){
        initComponets();
    }
    private void initComponets(){

        windowFrame = new JFrame();
        projectPanel = new ProjectPanel();
        casePanel = new CasePanel();
        cardLayout = new CardLayout();
        //======== panel name ========
        //panels.put(panelNames[0],projectPanel);
        //panels.put(panelNames[1],casePanel);
        //======== windowFrame ========
        windowFrame.setTitle("\u65b0\u5efa\u9879\u76ee");
        windowFrame.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
        Container contentPane = windowFrame.getContentPane();
        contentPane.setLayout(cardLayout);
        contentPane.add(projectPanel,"project");
        contentPane.add(casePanel,"case");
        windowFrame.pack();
        windowFrame.setLocationRelativeTo(windowFrame.getOwner());
    }
    public class ProjectPanel extends JPanel{
        private JTextField textField1; // 项目名称
        private JTextField textField2; // 保存路径
        private JTextField textField3; // 路网文件路径
        public ProjectPanel(){
            initComponent();
        }
        private void initComponent(){
            JLabel label1 = new JLabel();
            textField1 = new JTextField();
            JLabel label2 = new JLabel();
            textField2 = new JTextField();
            JButton button1 = new JButton();
            JLabel label3 = new JLabel();
            textField3 = new JTextField();
            JButton button2 = new JButton();
            JPanel panel2 = new JPanel();
            JButton button4 = new JButton();
            JButton button5 = new JButton();
            //======== 新建项目面板 ========
            setPreferredSize(new Dimension(306, 381));
            setMinimumSize(new Dimension(306, 381));
            setLayout(new GridBagLayout());
            ((GridBagLayout)getLayout()).columnWidths = new int[] {15, 91, 63, 56, 63, 25, 0};
            ((GridBagLayout)getLayout()).rowHeights = new int[] {45, 35, 32, 35, 32, 35, 32, 35, 35, 32, 30, 0};
            ((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};
            ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

            //---- label1 ----
            label1.setText("\u9879\u76ee\u540d\u79f0\uff1a");
            label1.setHorizontalAlignment(SwingConstants.CENTER);
            label1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 16));
            add(label1, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
            add(textField1, new GridBagConstraints(2, 2, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- label2 ----
            label2.setText("\u4fdd\u5b58\u8def\u5f84\uff1a");
            label2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 16));
            label2.setHorizontalAlignment(SwingConstants.CENTER);
            add(label2, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
            add(textField2, new GridBagConstraints(2, 4, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- button1 ----
            button1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            button1.setText("\u6d4f\u89c8");
            button1.setMargin(new Insets(2, 2, 2, 2));
            add(button1, new GridBagConstraints(4, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- label3 ----
            label3.setText("\u8def\u7f51\u6587\u4ef6\uff1a");
            label3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 16));
            label3.setHorizontalAlignment(SwingConstants.CENTER);
            add(label3, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
            add(textField3, new GridBagConstraints(2, 6, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- button2 ----
            button2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            button2.setText("\u6d4f\u89c8");
            button2.setMargin(new Insets(2, 2, 2, 2));
            add(button2, new GridBagConstraints(4, 6, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //======== panel2 ========
            {
                panel2.setLayout(new GridBagLayout());
                ((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {15, 71, 31, 68, 0};
                ((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {27, 0};
                ((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
                ((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

                //---- button4 ----
                button4.setText("\u4e0b\u4e00\u6b65");
                button4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
                button4.setMargin(new Insets(2, 8, 2, 8));
                panel2.add(button4, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));
                button4.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        cardLayout.show(windowFrame.getContentPane(),"case");
                    }
                });
                //---- button5 ----
                button5.setText("\u53d6\u6d88");
                button5.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
                panel2.add(button5, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            add(panel2, new GridBagConstraints(2, 9, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
        }
    }
    public class CasePanel extends JPanel{
        private JTextField textField1;//方案名称
        private JComboBox<String> comboBox1;//仿真模型
        private JTextField textField4;//开始时间
        private JTextField textField5;//结束时间
        private JTextField textField2;//仿真步长
        private JTextField textField3;//需求文件路径
        public CasePanel() {
            initComponents();
        }

        private void initComponents() {

            JLabel label1 = new JLabel();
            textField1 = new JTextField();
            JLabel label2 = new JLabel();
            comboBox1 = new JComboBox<>();
            JLabel label3 = new JLabel();
            textField4 = new JTextField();
            JLabel label4 = new JLabel();
            textField5 = new JTextField();
            JLabel label5 = new JLabel();
            textField2 = new JTextField();
            JLabel label6 = new JLabel();
            textField3 = new JTextField();
            JButton button2 = new JButton();
            JPanel panel1 = new JPanel();
            JButton button5 = new JButton();
            JButton button3 = new JButton();
            JButton button4 = new JButton();

            //======== this ========
            setMinimumSize(new Dimension(306, 381));
            setPreferredSize(new Dimension(306, 381));

            setLayout(new GridBagLayout());
            ((GridBagLayout)getLayout()).columnWidths = new int[] {30, 0, 89, 35, 0, 29, 0};
            ((GridBagLayout)getLayout()).rowHeights = new int[] {25, 32, 15, 32, 15, 32, 15, 32, 15, 32, 15, 32, 25, 32, 0, 0};
            ((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};
            ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

            //---- label1 ----
            label1.setText("\u65b9\u6848\u540d\u79f0\uff1a");
            label1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 16));
            label1.setHorizontalAlignment(SwingConstants.CENTER);
            add(label1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
            add(textField1, new GridBagConstraints(2, 1, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- label2 ----
            label2.setText("\u4eff\u771f\u6a21\u578b\uff1a");
            label2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 16));
            label2.setHorizontalAlignment(SwingConstants.CENTER);
            add(label2, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- comboBox1 ----
            comboBox1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
            comboBox1.setModel(new DefaultComboBoxModel<>(new String[] {
                    "MLP",
                    "MesoTS"
            }));
            add(comboBox1, new GridBagConstraints(2, 3, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- label3 ----
            label3.setText("\u5f00\u59cb\u65f6\u95f4\uff1a");
            label3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 16));
            label3.setHorizontalAlignment(SwingConstants.CENTER);
            add(label3, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
            add(textField4, new GridBagConstraints(2, 5, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- label4 ----
            label4.setText("\u7ed3\u675f\u65f6\u95f4\uff1a");
            label4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 16));
            label4.setHorizontalAlignment(SwingConstants.CENTER);
            add(label4, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
            add(textField5, new GridBagConstraints(2, 7, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- label5 ----
            label5.setText("\u4eff\u771f\u6b65\u957f\uff1a");
            label5.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 16));
            label5.setHorizontalAlignment(SwingConstants.CENTER);
            add(label5, new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
            add(textField2, new GridBagConstraints(2, 9, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- label6 ----
            label6.setText("\u9700\u6c42\u6587\u4ef6\uff1a");
            label6.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 16));
            label6.setHorizontalAlignment(SwingConstants.CENTER);
            add(label6, new GridBagConstraints(1, 11, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));
            add(textField3, new GridBagConstraints(2, 11, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //---- button2 ----
            button2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            button2.setText("\u6d4f\u89c8");
            button2.setMargin(new Insets(2, 2, 2, 2));
            add(button2, new GridBagConstraints(4, 11, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

            //======== panel1 ========
            {
                panel1.setLayout(new GridBagLayout());
                ((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {15, 73, 15, 73, 15, 67, 0};
                ((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {27, 0};
                ((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
                ((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

                //---- button5 ----
                button5.setText("\u4e0a\u4e00\u6b65");
                button5.setMargin(new Insets(2, 6, 2, 6));
                button5.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
                panel1.add(button5, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- button3 ----
                button3.setText("\u5b8c\u6210");
                button3.setMargin(new Insets(2, 6, 2, 6));
                button3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
                panel1.add(button3, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- button4 ----
                button4.setText("\u53d6\u6d88");
                button4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
                panel1.add(button4, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            add(panel1, new GridBagConstraints(1, 13, 4, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 5), 0, 0));

        }


    }
}
