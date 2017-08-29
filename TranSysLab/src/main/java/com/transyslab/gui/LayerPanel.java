package com.transyslab.gui;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Created by yali on 2017/8/26.
 */
public class LayerPanel {

    private Map<String, JPanel> layers;

    public LayerPanel(){
        layers = new HashMap<>();
        layers.put("Node",new NodePanel());
        layers.put("Link",new LinkPanel());
        layers.put("Segment",new SegmentPanel());
        layers.put("Lane",new LanePanel());
        layers.put("Vehicle",new VehiclePanle());
        layers.put("Sensor",new SensorPanel());
    }

    public JPanel getLayer(String layName){
        return layers.get(layName);
    }

    public class VehiclePanle extends JPanel {

        private JTextField textField1;//编号
        private JTextField textField4;//类型
        private JTextField textField2;//长度
        private JTextField textField5;//当前车速
        private JTextField textField3;//所在车道
        private JTextField textField6;//起点
        private JTextField textField7;//终点
        private JTextField textField8;//路径
        private JTextArea textArea4;//其它

        public VehiclePanle() {
            initComponents();
        }

        private void initComponents() {

            JLabel label1 = new JLabel();
            textField1 = new JTextField();
            JLabel label5 = new JLabel();
            textField4 = new JTextField();
            JLabel label2 = new JLabel();
            textField2 = new JTextField();
            JLabel label8 = new JLabel();
            textField5 = new JTextField();
            JLabel label3 = new JLabel();
            textField3 = new JTextField();
            JPanel panel5 = new JPanel();
            JLabel label6 = new JLabel();
            textField6 = new JTextField();
            JLabel label7 = new JLabel();
            textField7 = new JTextField();
            JLabel label9 = new JLabel();
            textField8 = new JTextField();
            JLabel label4 = new JLabel();
            JScrollPane scrollPane4 = new JScrollPane();
            textArea4 = new JTextArea();

            //======== this ========
            setLayout(new GridBagLayout());
            ((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0};
            ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0};
            ((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
            ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

            //---- label1 ----
            label1.setText("\u7f16\u53f7\uff1a");
            label1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 0), 0, 0));
            add(textField1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label5 ----
            label5.setText("\u7c7b\u578b\uff1a");
            label5.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label5, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 0), 0, 0));
            add(textField4, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label2 ----
            label2.setText("\u957f\u5ea6\uff1a");
            label2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 0), 0, 0));
            add(textField2, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label8 ----
            label8.setText("\u5f53\u524d\u8f66\u901f\uff1a");
            label8.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label8, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 0), 0, 0));
            add(textField5, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label3 ----
            label3.setText("\u6240\u5728\u8f66\u9053\uff1a");
            label3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label3, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 0), 0, 0));
            add(textField3, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //======== panel5 ========
            {
                panel5.setBorder(new CompoundBorder(
                        new TitledBorder(null, "\u8def\u5f84\u4fe1\u606f", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
                                new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 13)),
                        BorderFactory.createEmptyBorder()));
                panel5.setLayout(new GridBagLayout());
                ((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {0, 0, 0};
                ((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
                ((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
                ((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

                //---- label6 ----
                label6.setText("\u8d77\u70b9\uff1a");
                label6.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 5, 5, 5), 0, 0));
                panel5.add(textField6, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 14, 5, 0), 0, 0));

                //---- label7 ----
                label7.setText("\u7ec8\u70b9\uff1a");
                label7.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label7, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 5, 5, 5), 0, 0));
                panel5.add(textField7, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 14, 5, 0), 0, 0));

                //---- label9 ----
                label9.setText("\u8def\u5f84\uff1a");
                label9.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label9, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 5, 0, 5), 0, 0));
                panel5.add(textField8, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 14, 0, 0), 0, 0));
            }
            add(panel5, new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            //---- label4 ----
            label4.setText("\u5176\u5b83\uff1a");
            label4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label4, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 0), 0, 0));

            //======== scrollPane4 ========
            {
                scrollPane4.setViewportView(textArea4);
            }
            add(scrollPane4, new GridBagConstraints(1, 6, 1, 2, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 0, 3), 0, 0));
        }
    }

    public class SensorPanel extends JPanel {

        private JTextField textField1;//编号
        private JTextField textField2;//类型
        private JTextField textField3;//隶属于
        private JTextField textField5;//开始时间
        private JTextField textField4;//统计间隔
        private JButton button1;//查看数据
        private JTextArea textArea4;//其它

        public SensorPanel() {
            initComponents();
        }

        private void initComponents() {
            // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner Evaluation license - Yunlin Yang
            JLabel label1 = new JLabel();
            textField1 = new JTextField();
            JLabel label2 = new JLabel();
            textField2 = new JTextField();
            JLabel label3 = new JLabel();
            textField3 = new JTextField();
            JPanel panel5 = new JPanel();
            JLabel label5 = new JLabel();
            textField5 = new JTextField();
            JLabel label6 = new JLabel();
            textField4 = new JTextField();
            button1 = new JButton();
            JLabel label4 = new JLabel();
            JScrollPane scrollPane4 = new JScrollPane();
            textArea4 = new JTextArea();

            //======== this ========

            setLayout(new GridBagLayout());
            ((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0};
            ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
            ((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
            ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

            //---- label1 ----
            label1.setText("\u7f16\u53f7\uff1a");
            label1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            add(textField1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label2 ----
            label2.setText("\u7c7b\u578b\uff1a");
            label2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            add(textField2, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label3 ----
            label3.setText("\u96b6\u5c5e\u4e8e\uff1a");
            label3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            add(textField3, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //======== panel5 ========
            {
                panel5.setBorder(new CompoundBorder(
                        new TitledBorder(null, "\u68c0\u6d4b\u4fe1\u606f", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
                                new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 13)),
                        BorderFactory.createEmptyBorder()));
                panel5.setLayout(new GridBagLayout());
                ((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {38, 0, 0};
                ((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
                ((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
                ((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

                //---- label5 ----
                label5.setText("\u5f00\u59cb\u65f6\u95f4\uff1a");
                label5.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label5, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 3, 5, 0), 0, 0));
                panel5.add(textField5, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 0), 0, 0));

                //---- label6 ----
                label6.setText("\u7edf\u8ba1\u95f4\u9694\uff1a");
                label6.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label6, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 3, 5, 0), 0, 0));
                panel5.add(textField4, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 0), 0, 0));

                //---- button1 ----
                button1.setText("\u67e5\u770b\u6570\u636e");
                button1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(button1, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            add(panel5, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            //---- label4 ----
            label4.setText("\u5176\u5b83\uff1a");
            label4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label4, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));

            //======== scrollPane4 ========
            {
                scrollPane4.setViewportView(textArea4);
            }
            add(scrollPane4, new GridBagConstraints(1, 4, 1, 2, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 0, 3), 0, 0));

        }

    }

    public class LanePanel extends JPanel {

        private JTextField textField1;//编号
        private JTextField textField2;//长度
        private JTextField textField3;//隶属于
        private JComboBox<String> comboBox2;//横向规则
        private JComboBox<String> comboBox3;//纵向规则
        private JTextArea textArea4;//其它

        public LanePanel() {
            initComponents();
        }
        private void initComponents() {

            JLabel label1 = new JLabel();
            textField1 = new JTextField();
            JLabel label2 = new JLabel();
            textField2 = new JTextField();
            JLabel label3 = new JLabel();
            textField3 = new JTextField();
            JPanel panel5 = new JPanel();
            JLabel label6 = new JLabel();
            comboBox2 = new JComboBox<>();
            JLabel label7 = new JLabel();
            comboBox3 = new JComboBox<>();
            JLabel label4 = new JLabel();
            JScrollPane scrollPane4 = new JScrollPane();
            textArea4 = new JTextArea();

            //======== this ========

            setLayout(new GridBagLayout());
            ((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0};
            ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
            ((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
            ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

            //---- label1 ----
            label1.setText("\u7f16\u53f7\uff1a");
            label1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            add(textField1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label2 ----
            label2.setText("\u957f\u5ea6\uff1a");
            label2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            add(textField2, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label3 ----
            label3.setText("\u96b6\u5c5e\u4e8e\uff1a");
            label3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            add(textField3, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //======== panel5 ========
            {
                panel5.setBorder(new CompoundBorder(
                        new TitledBorder(null, "\u4ea4\u901a\u89c4\u5219", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
                                new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 13)),
                        new EmptyBorder(2, 2, 2, 2)));
                panel5.setLayout(new GridBagLayout());
                ((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {0, 0, 0};
                ((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0, 0};
                ((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
                ((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

                //---- label6 ----
                label6.setText("\u6a2a\u5411\uff1a");
                label6.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 4, 5, 5), 0, 0));

                //---- comboBox2 ----
                comboBox2.setModel(new DefaultComboBoxModel<>(new String[] {
                        "\u5141\u8bb8\u5de6\u53f3\u6362\u9053",
                        "\u4ec5\u5141\u8bb8\u5de6\u6362\u9053",
                        "\u4ec5\u5141\u8bb8\u53f3\u6362\u9053",
                        "\u7981\u6b62\u6362\u9053"
                }));
                comboBox2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(comboBox2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 6, 5, 0), 0, 0));

                //---- label7 ----
                label7.setText("\u7eb5\u5411\uff1a");
                label7.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label7, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 4, 0, 5), 0, 0));

                //---- comboBox3 ----
                comboBox3.setModel(new DefaultComboBoxModel<>(new String[] {
                        "\u76f4\u884c",
                        "\u4e13\u5de6",
                        "\u4e13\u53f3",
                        "\u76f4\u5de6",
                        "\u76f4\u53f3",
                        "\u76f4\u5de6\u53f3"
                }));
                comboBox3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(comboBox3, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 6, 0, 0), 0, 0));
            }
            add(panel5, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            //---- label4 ----
            label4.setText("\u5176\u5b83\uff1a");
            label4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label4, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));

            //======== scrollPane4 ========
            {

                //---- textArea4 ----
                textArea4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 13));
                scrollPane4.setViewportView(textArea4);
            }
            add(scrollPane4, new GridBagConstraints(1, 4, 1, 2, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 0, 3), 0, 0));
        }

    }
    public class SegmentPanel extends JPanel {

        private JTextField textField1;//编号
        private JTextField textField2;//长度
        private JTextField textField3;//隶属于
        private JTextField textField4;//限速
        private JTextField textField5;//控制
        private JTextArea textArea4;//其它

        public SegmentPanel() {
            initComponents();
        }

        private void initComponents() {

            JLabel label1 = new JLabel();
            textField1 = new JTextField();
            JLabel label2 = new JLabel();
            textField2 = new JTextField();
            JLabel label3 = new JLabel();
            textField3 = new JTextField();
            JPanel panel5 = new JPanel();
            JLabel label6 = new JLabel();
            textField4 = new JTextField();
            JLabel label7 = new JLabel();
            textField5 = new JTextField();
            JLabel label4 = new JLabel();
            JScrollPane scrollPane4 = new JScrollPane();
            textArea4 = new JTextArea();

            //======== this ========

            setLayout(new GridBagLayout());
            ((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0};
            ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
            ((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
            ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

            //---- label1 ----
            label1.setText("\u7f16\u53f7\uff1a");
            label1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            add(textField1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label2 ----
            label2.setText("\u957f\u5ea6\uff1a");
            label2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            add(textField2, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label3 ----
            label3.setText("\u96b6\u5c5e\u4e8e\uff1a");
            label3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            add(textField3, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //======== panel5 ========
            {
                panel5.setBorder(new CompoundBorder(
                        new TitledBorder(null, "\u4ea4\u901a\u89c4\u5219", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
                                new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 13)),
                        BorderFactory.createEmptyBorder()));
                panel5.setLayout(new GridBagLayout());
                ((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {0, 0, 0};
                ((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0, 0};
                ((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
                ((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

                //---- label6 ----
                label6.setText("\u9650\u901f\uff1a");
                label6.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 5, 5, 5), 0, 0));
                panel5.add(textField4, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 7, 5, 0), 0, 0));

                //---- label7 ----
                label7.setText("\u63a7\u5236\uff1a");
                label7.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label7, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 5, 0, 5), 0, 0));
                panel5.add(textField5, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 7, 0, 0), 0, 0));
            }
            add(panel5, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            //---- label4 ----
            label4.setText("\u5176\u5b83\uff1a");
            label4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label4, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));

            //======== scrollPane4 ========
            {

                //---- textArea4 ----
                textArea4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 13));
                scrollPane4.setViewportView(textArea4);
            }
            add(scrollPane4, new GridBagConstraints(1, 4, 1, 2, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 0, 3), 0, 0));
        }


    }

    public class LinkPanel extends JPanel {

        private JTextField textField1;//编号
        private JTextField textField3;//类型
        private JTextField textField2;//长度
        private JTextField textField4;//上游节点
        private JTextField textField5;//下游节点
        private JTextArea textArea4;//其它

        public LinkPanel() {
            initComponents();
        }

        private void initComponents() {

            JLabel label1 = new JLabel();
            textField1 = new JTextField();
            JLabel label3 = new JLabel();
            textField3 = new JTextField();
            JLabel label2 = new JLabel();
            textField2 = new JTextField();
            JPanel panel5 = new JPanel();
            JLabel label6 = new JLabel();
            textField4 = new JTextField();
            JLabel label7 = new JLabel();
            textField5 = new JTextField();
            JLabel label4 = new JLabel();
            JScrollPane scrollPane4 = new JScrollPane();
            textArea4 = new JTextArea();

            //======== this ========
            setLayout(new GridBagLayout());
            ((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0};
            ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
            ((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
            ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

            //---- label1 ----
            label1.setText("\u7f16\u53f7\uff1a");
            label1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            add(textField1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label3 ----
            label3.setText("\u7c7b\u578b\uff1a");
            label3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label3, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            add(textField3, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label2 ----
            label2.setText("\u957f\u5ea6\uff1a");
            label2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            add(textField2, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //======== panel5 ========
            {
                panel5.setBorder(new CompoundBorder(
                        new TitledBorder(null, "\u8fde\u63a5\u8282\u70b9", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
                                new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 13)),
                        BorderFactory.createEmptyBorder()));
                panel5.setLayout(new GridBagLayout());
                ((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {28, 0, 0};
                ((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0, 0};
                ((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
                ((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

                //---- label6 ----
                label6.setText("\u4e0a\u6e38\uff1a");
                label6.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 3, 5, 0), 0, 0));
                panel5.add(textField4, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 2, 5, 0), 0, 0));

                //---- label7 ----
                label7.setText("\u4e0b\u6e38\uff1a");
                label7.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label7, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 3, 0, 0), 0, 0));
                panel5.add(textField5, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 2, 0, 0), 0, 0));
            }
            add(panel5, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            //---- label4 ----
            label4.setText("\u5176\u5b83\uff1a");
            label4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label4, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));

            //======== scrollPane4 ========
            {

                //---- textArea4 ----
                textArea4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 13));
                scrollPane4.setViewportView(textArea4);
            }
            add(scrollPane4, new GridBagConstraints(1, 4, 1, 2, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 0, 3), 0, 0));
        }


    }
    public class NodePanel extends JPanel{

        private JTextField textField1; //编号
        private JTextField textField2; //类型
        private JTextField textField3; //x
        private JTextField textField4; //y
        private JTextField textField5; //z
        private JTextArea textArea4; //其它信息
        public NodePanel() {
            initComponents();
        }
        private void initComponents() {

            JLabel label1 = new JLabel();
            textField1 = new JTextField();
            JLabel label2 = new JLabel();
            textField2 = new JTextField();
            JPanel panel5 = new JPanel();  //坐标信息
            JLabel label6 = new JLabel();
            textField3 = new JTextField();
            JLabel label7 = new JLabel();
            textField4 = new JTextField();
            JLabel label3 = new JLabel();
            textField5 = new JTextField();
            JLabel label4 = new JLabel();
            JScrollPane scrollPane4 = new JScrollPane();//其它
            textArea4 = new JTextArea();

            //======== this ========

            setLayout(new GridBagLayout());
            ((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0};
            ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
            ((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
            ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};

            //---- label1 ----
            label1.setText("\u7f16\u53f7\uff1a");
            label1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            add(textField1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //---- label2 ----
            label2.setText("\u7c7b\u578b\uff1a");
            label2.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));
            add(textField2, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 3), 0, 0));

            //======== panel5 ========
            {
                panel5.setBorder(new CompoundBorder(
                        new TitledBorder(null, "\u5750\u6807\u4fe1\u606f", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
                                new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 13)),
                        BorderFactory.createEmptyBorder()));
                panel5.setLayout(new GridBagLayout());
                ((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {0, 0, 0};
                ((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
                ((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
                ((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

                //---- label6 ----
                label6.setText("x\uff1a");
                label6.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 8, 5, 5), 0, 0));
                panel5.add(textField3, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 10, 5, 0), 0, 0));

                //---- label7 ----
                label7.setText("y\uff1a");
                label7.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label7, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 8, 5, 5), 0, 0));
                panel5.add(textField4, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 10, 5, 0), 0, 0));

                //---- label3 ----
                label3.setText("z\uff1a");
                label3.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
                panel5.add(label3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 8, 0, 5), 0, 0));
                panel5.add(textField5, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 10, 0, 0), 0, 0));
            }
            add(panel5, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            //---- label4 ----
            label4.setText("\u5176\u5b83\uff1a");
            label4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 12));
            add(label4, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 5, 5), 0, 0));

            //======== scrollPane4 ========
            {

                //---- textArea4 ----
                textArea4.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 13));
                scrollPane4.setViewportView(textArea4);
            }
            add(scrollPane4, new GridBagConstraints(1, 3, 1, 2, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 3, 0, 3), 0, 0));
        }

    }
}
