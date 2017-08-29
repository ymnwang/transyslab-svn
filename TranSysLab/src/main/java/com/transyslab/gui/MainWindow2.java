package com.transyslab.gui;

import com.jogamp.opengl.util.FPSAnimator;
import com.transyslab.commons.io.JdbcUtils;
import com.transyslab.commons.renderer.Camera;
import com.transyslab.commons.renderer.JOGLCanvas;
import com.transyslab.commons.renderer.OrbitCamera;
import com.transyslab.commons.tools.DataVisualization;
import com.transyslab.simcore.AppSetup;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mesots.MesoEngine;
import com.transyslab.simcore.mlp.MLPEngine;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;

import java.awt.*;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Created by yali on 2017/8/26.
 */
public class MainWindow2{
    private JFrame windowFrame;
    private JTextArea textArea2;// 控制台信息
    private JTextArea textArea3;// 方案信息
    private final String[] layerNames = {"Node","Link","Segment","Lane","Sensor","Vehicle"};
    private JOGLCanvas canvas;
    private FPSAnimator animator;
    private SubWindow1 subWindow;
    private SimulationEngine[] engines;
    private Trace2DSimple traceRT;
    public boolean needRTPlot;
    public MainWindow2(){
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        initComponents();
        windowFrame.setVisible(true);
    }
    private void initComponents(){
        windowFrame = new JFrame();
        canvas = new JOGLCanvas();
        Camera cam = new OrbitCamera();
        canvas.setCamera(cam);
        // Create a animator that drives canvas' display() at the specified FPS.
        animator = new FPSAnimator(canvas, 120, true);
        subWindow = new SubWindow1();
        JMenuBar menuBar1 = new JMenuBar();
        JMenu menu1 = new JMenu();
        JMenu menu2 = new JMenu();
        JMenu menu3 = new JMenu();
        JMenu menu4 = new JMenu();
        JMenu menu5 = new JMenu();
        JMenu menu6 = new JMenu();
        JMenu menu7 = new JMenu();
        JMenu menu8 = new JMenu();
        JToolBar toolBar1 = new JToolBar();
        JButton button1 = new JButton();
        JButton button2 = new JButton();
        JButton button3 = new JButton();
        JButton button4 = new JButton();
        JButton button5 = new JButton();
        JButton button7 = new JButton();
        JButton button8 = new JButton();
        JButton button9 = new JButton();
        JPanel panel6 = new JPanel();
        JPanel panel7 = new JPanel();
        JPanel panel2 = new JPanel();
        JComboBox<String> comboBox1 = new JComboBox<>();
        JTabbedPane tabbedPane1 = new JTabbedPane();
        JPanel panel1 = new JPanel();
        JScrollPane scrollPane2 = new JScrollPane();
        textArea2 = new JTextArea();
        JPanel panel3 = new JPanel();
        JScrollPane scrollPane3 = new JScrollPane();
        textArea3 = new JTextArea();
        LayerPanel layerPanel = new LayerPanel();
        //======== windowFrame ========
        windowFrame.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
        windowFrame.setTitle("TranSysLab");
        Container contentPane = windowFrame.getContentPane();
        contentPane.setLayout(new GridBagLayout());
        ((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {409, 206, 0};
        ((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 54, 131, 0};
        ((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
        ((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0E-4};
        windowFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Use a dedicate thread to run the stop() to ensure that the
                // animator stops before program exits.
                new Thread() {
                    @Override
                    public void run() {
                        if (animator.isStarted())
                            animator.stop();
                        System.exit(0);
                    }
                }.start();
            }
        });

        //======== 菜单栏 ========
        {
            menuBar1.setBorder(UIManager.getBorder("MenuBar.border"));

            //======== menu1 ========
            {
                menu1.setText("\u6587\u4ef6");
                menu1.setFont(new Font("Dialog", Font.PLAIN, 12));
                menu1.setBorder(new EmptyBorder(0, 3, 0, 3));
            }
            menuBar1.add(menu1);

            //======== menu2 ========
            {
                menu2.setText("\u7f16\u8f91");
                menu2.setFont(new Font("Dialog", Font.PLAIN, 12));
                menu2.setBorder(new EmptyBorder(0, 3, 0, 3));
            }
            menuBar1.add(menu2);

            //======== menu3 ========
            {
                menu3.setText("\u8fd0\u884c");
                menu3.setFont(new Font("Dialog", Font.PLAIN, 12));
                menu3.setBorder(new EmptyBorder(0, 3, 0, 3));
            }
            menuBar1.add(menu3);

            //======== menu4 ========
            {
                menu4.setText("\u7a97\u53e3");
                menu4.setFont(new Font("Dialog", Font.PLAIN, 12));
                menu4.setBorder(new EmptyBorder(0, 3, 0, 3));
            }
            menuBar1.add(menu4);

            //======== menu5 ========
            {
                menu5.setText("\u67e5\u770b");
                menu5.setFont(new Font("Dialog", Font.PLAIN, 12));
                menu5.setBorder(new EmptyBorder(0, 3, 0, 3));
            }
            menuBar1.add(menu5);

            //======== menu6 ========
            {
                menu6.setText("\u65b9\u6848");
                menu6.setFont(new Font("Dialog", Font.PLAIN, 12));
                menu6.setBorder(new EmptyBorder(0, 3, 0, 3));
            }
            menuBar1.add(menu6);

            //======== menu7 ========
            {
                menu7.setText("\u5de5\u5177");
                menu7.setFont(new Font("Dialog", Font.PLAIN, 12));
                menu7.setBorder(new EmptyBorder(0, 3, 0, 3));
            }
            menuBar1.add(menu7);

            //======== menu8 ========
            {
                menu8.setText("\u5e2e\u52a9");
                menu8.setFont(new Font("Dialog", Font.PLAIN, 12));
                menu8.setBorder(new EmptyBorder(0, 3, 0, 3));
            }
            menuBar1.add(menu8);
        }
        windowFrame.setJMenuBar(menuBar1);

        //======== 工具栏 ========
        {
            toolBar1.setBorder(UIManager.getBorder("ToolBar.border"));

            //---- button1 ----
            button1.setText("\u65b0\u5efa\u9879\u76ee");
            button1.setFont(new Font("\u534e\u6587\u7ec6\u9ed1", Font.PLAIN, 12));
            toolBar1.add(button1);
            button1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    subWindow.windowFrame.setVisible(true);
                }
            });
            //---- button2 ----
            button2.setText("\u6253\u5f00\u9879\u76ee");
            button2.setFont(new Font("\u534e\u6587\u7ec6\u9ed1", Font.PLAIN, 12));
            button2.addActionListener(new ActionListener() {

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
            toolBar1.add(button2);
            toolBar1.addSeparator();

            //---- button3 ----
            button3.setText("\u4fdd\u5b58");
            button3.setFont(new Font("\u534e\u6587\u7ec6\u9ed1", Font.PLAIN, 12));
            toolBar1.add(button3);
            toolBar1.addSeparator();

            //---- button4 ----
            button4.setText("\u5f00\u59cb");
            button4.setFont(new Font("\u534e\u6587\u7ec6\u9ed1", Font.PLAIN, 12));
            toolBar1.add(button4);

            //---- button5 ----
            button5.setText("\u6682\u505c");
            button5.setFont(new Font("\u534e\u6587\u7ec6\u9ed1", Font.PLAIN, 12));
            toolBar1.add(button5);

            //---- button7 ----
            button7.setText("\u7ed3\u675f");
            button7.setFont(new Font("\u534e\u6587\u7ec6\u9ed1", Font.PLAIN, 12));
            toolBar1.add(button7);
            toolBar1.addSeparator();

            //---- button8 ----
            button8.setText("\u6d41\u91cf\u7edf\u8ba1");
            button8.setFont(new Font("\u534e\u6587\u7ec6\u9ed1", Font.PLAIN, 12));
            toolBar1.add(button8);

            //---- button9 ----
            button9.setText("\u8f66\u901f\u7edf\u8ba1");
            button9.setFont(new Font("\u534e\u6587\u7ec6\u9ed1", Font.PLAIN, 12));
            //TODO 限制多次点击
            button9.addActionListener(new ActionListener() {
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
                                java.util.List result = (java.util.List) qr.query(sql, new ColumnListHandler(3));
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
            toolBar1.add(button9);
        }
        contentPane.add(toolBar1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 7, 7), 0, 0));

        //======== canvas展示区 ========
        {
            panel6.setLayout(new GridBagLayout());
            ((GridBagLayout)panel6.getLayout()).columnWidths = new int[] {0, 0};
            ((GridBagLayout)panel6.getLayout()).rowHeights = new int[] {0, 0};
            ((GridBagLayout)panel6.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
            ((GridBagLayout)panel6.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};
            panel6.add(canvas, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
        }
        contentPane.add(panel6, new GridBagConstraints(0, 1, 1, 12, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 7, 7), 0, 0));

        //======== 交通要素属性面板 ========
        {
            panel2.setBorder(new CompoundBorder(
                    new TitledBorder(null, "\u5c5e\u6027", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
                            new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 13)),
                    new EmptyBorder(5, 5, 5, 5)));


            panel2.setLayout(new GridBagLayout());
            ((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0};
            ((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0};
            ((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
            ((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {1.0, 0.0, 1.0E-4};
            //======== panel7 ========
            {
                panel7.setLayout(new CardLayout());
                for (int i = 0; i < layerNames.length; i++) {
                    panel7.add(layerPanel.getLayer(layerNames[i]), layerNames[i]);
                }
            }
            /*
            //======== 默认显示Node属性 ========
            for(int i=0;i<layerNames.length;i++){
                JPanel layer = layerPanel.getLayer(layerNames[i]);
                panel2.add(layer, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 5, 0), 0, 0));
                layer.setVisible(false);
            }
            curLayer = layerPanel.getLayer("Node");
            curLayer.setVisible(true);*/
            panel2.add(panel7, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));
            //---- 交通要素选择器 ----
            comboBox1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
            comboBox1.setModel(new DefaultComboBoxModel<>(layerNames));
            panel2.add(comboBox1, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
                    GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                    new Insets(0, 3, 0, 3), 0, 0));
        }
        contentPane.add(panel2, new GridBagConstraints(1, 1, 1, 12, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 7, 0), 0, 0));

        //======== 底部信息框 ========
        {
            tabbedPane1.setFont(new Font("\u534e\u6587\u7ec6\u9ed1", Font.PLAIN, 13));

            //======== 控制台 ========
            {
                panel1.setLayout(new GridBagLayout());
                ((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0};
                ((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
                ((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
                ((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

                //======== scrollPane2 ========
                {
                    scrollPane2.setViewportView(textArea2);
                    textArea2.setLineWrap(true);
                }
                panel1.add(scrollPane2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            tabbedPane1.addTab("\u63a7\u5236\u53f0", panel1);

            //======== 方案 ========
            {
                panel3.setLayout(new GridBagLayout());
                ((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0};
                ((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0};
                ((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
                ((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

                //======== scrollPane3 ========
                {
                    scrollPane3.setViewportView(textArea3);
                    textArea3.setLineWrap(true);
                }
                panel3.add(scrollPane3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            tabbedPane1.addTab("\u65b9\u6848", panel3);
        }
        contentPane.add(tabbedPane1, new GridBagConstraints(0, 13, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        windowFrame.setSize(820, 580);
        windowFrame.setLocationRelativeTo(windowFrame.getOwner());
            // TODO 所有comboBox都可用
            comboBox1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JComboBox cb = (JComboBox)e.getSource();
                    String itemName = (String)cb.getSelectedItem();
                    ((CardLayout)panel7.getLayout()).show(panel7,itemName);
                }
            });
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
        canvas.setFirstRender(true);
        canvas.setDrawableNetwork(engines[0].getNetwork());
        canvas.requestFocusInWindow();
    }
    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainWindow2 appGUI = new MainWindow2();
                //appGUI.render();
            }
        });
    }
}
