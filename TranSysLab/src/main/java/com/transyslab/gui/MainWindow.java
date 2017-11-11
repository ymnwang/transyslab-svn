package com.transyslab.gui;

import com.jogamp.opengl.util.FPSAnimator;
import com.transyslab.commons.io.JdbcUtils;
import com.transyslab.commons.renderer.Camera;
import com.transyslab.commons.renderer.FrameQueue;
import com.transyslab.commons.renderer.JOGLCanvas;
import com.transyslab.commons.renderer.OrbitCamera;
import com.transyslab.commons.tools.DataVisualization;
import com.transyslab.commons.tools.Worker;
import com.transyslab.roadnetwork.Constants;
import com.transyslab.simcore.AppSetup;
import com.transyslab.simcore.SimulationEngine;
import com.transyslab.simcore.mesots.MesoEngine;
import com.transyslab.simcore.mlp.MLPEngine;
import com.transyslab.simcore.mlp.MLPParameter;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalTime;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Created by yali on 2017/8/26.
 */
public class MainWindow {
    private JFrame windowFrame;
    private JTextArea textArea2;// 控制台信息
    private JTextArea textArea3;// 方案信息
    private JProgressBar progressBar1;
    private JLabel label5;//窗口状态信息
    private JLabel label8;//任务状态信息
    private JLabel label9;//进度条进度值
    private final String[] layerNames = {"Node","Link","Segment","Lane","Sensor","Vehicle"};
    private String curLayerName = "Node";
    private LayerPanel layerPanel;
    private JOGLCanvas canvas;
    private FPSAnimator animator;
    private SubWindow subWindow;
    private SimulationEngine[] engines;
    private Trace2DSimple traceRT;
    public boolean needRTPlot;
    public MainWindow(){

        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        initComponents();
    }
    private void initComponents(){
        windowFrame = new JFrame();
        progressBar1 = new JProgressBar();
        label5 = new JLabel();
        label8 = new JLabel();
        label9 = new JLabel();
        canvas = new JOGLCanvas();
        canvas.setMainWindow(this);
        Camera cam = new OrbitCamera();
        canvas.setCamera(cam);
        // Create a animator that drives canvas' display() at the specified FPS.
        animator = new FPSAnimator(canvas, 120, true);
        subWindow = new SubWindow();
        subWindow.setMainWindow(this);
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
        JButton button10 = new JButton();
        JPanel panel7 = new JPanel();
        JPanel panel2 = new JPanel();
        JPanel panel8 = new JPanel();
        JComboBox<String> comboBox1 = new JComboBox<>();
        JTabbedPane tabbedPane1 = new JTabbedPane();
        JPanel panel1 = new JPanel();
        JScrollPane scrollPane2 = new JScrollPane();
        textArea2 = new JTextArea();
        JPanel panel3 = new JPanel();
        JScrollPane scrollPane3 = new JScrollPane();
        textArea3 = new JTextArea();
        layerPanel = new LayerPanel();
        //======== windowFrame ========
        windowFrame.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
        windowFrame.setTitle("TranSysLab");
        Container contentPane = windowFrame.getContentPane();
        contentPane.setLayout(new GridBagLayout());
        ((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {409, 206, 0};
        ((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 54, 131,20,0};
        ((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
        ((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0E-4};
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
            menuBar1.setBorder(new CompoundBorder(UIManager.getBorder("Menu.border"),
                    null));

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

            button1.setIcon(new ImageIcon(loadImage("icon/new/file.png")
                            .getImage().getScaledInstance(20,20,java.awt.Image.SCALE_SMOOTH)));
            //button1.setFont(new Font("\u534e\u6587\u7ec6\u9ed1", Font.PLAIN, 12));
            button1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    subWindow.showPanel("project");
                }
            });
            toolBar1.add(button1);

            //---- button2 ----
            button2.setIcon(new ImageIcon(loadImage("icon/new/openfile.png")
                    .getImage().getScaledInstance(20,20,java.awt.Image.SCALE_SMOOTH)));
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

                            String modelType = config.getString("modelType");

                            if(modelType.equals("MesoTS")) {
                                //TODO: 由于properties解释过程不统一，避免报错，将meso properties的解释放在此分支之下 wym
                                String projectName = config.getString("projectName");
                                String networkPath = config.getString("networkPath");
                                String caseName = config.getString("caseName");
                                String createTime = config.getString("createTime");
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
                                AppSetup.modelType = Constants.MODEL_TYPE_MESO;
                            }
                            else {
                                AppSetup.setupParameter.put("输入文件路径", fileChooser.getSelectedFile().getPath());//解释过程放在MLPEngine中
                                AppSetup.modelType = Constants.MODEL_TYPE_MLP;
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
            button3.setIcon(new ImageIcon(loadImage("icon/new/save.png")
                   .getImage().getScaledInstance(20,20,java.awt.Image.SCALE_SMOOTH)));
            toolBar1.add(button3);
            toolBar1.addSeparator();

            //---- 按键：开始仿真 ----
            //button4.setMargin(new Insets(0, 8, 0, ));
            button4.setIcon(new ImageIcon(loadImage("icon/new/play.png")
                    .getImage().getScaledInstance(20,20,java.awt.Image.SCALE_SMOOTH)));
            button4.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    /*
				if(!canvas_.isNetworkReady()){
					JOptionPane.showMessageDialog(null, "请先加载路网");
					return;
				}*/
                    //从暂停到播放
                    if(canvas.isPause){
                        canvas.isPause = false;
                    }
                    //第一次播放
                    else if(!canvas.isRendering){
                        new Thread(()->engines[0].run()).start();
                        canvas.isRendering = true;
                    }
                }
            });
            toolBar1.add(button4);

            //---- 按键：暂停仿真 ----
            button5.setIcon(new ImageIcon(loadImage("icon/new/pause.png")
                    .getImage().getScaledInstance(20,20, Image.SCALE_SMOOTH)));
            button5.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    canvas.isPause = true;
                }
            });
            toolBar1.add(button5);

            //---- 按键：停止仿真 ----
            button7.setIcon(new ImageIcon(loadImage("icon/new/stop.png")
                    .getImage().getScaledInstance(20,20,java.awt.Image.SCALE_SMOOTH)));
            button7.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    animator.stop();
                }
            });
            toolBar1.add(button7);
            toolBar1.addSeparator();

            //---- 按键：数据库连接 ----
            button8.setIcon(new ImageIcon(loadImage("icon/new/database.png")
                    .getImage().getScaledInstance(20,20,java.awt.Image.SCALE_SMOOTH)));
            toolBar1.add(button8);

            //---- 按键：车速统计 ----
            button9.setIcon(new ImageIcon(loadImage("icon/new/chart.png")
                    .getImage().getScaledInstance(20,20,java.awt.Image.SCALE_SMOOTH)));
            //TODO 限制多次点击
            button9.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    subWindow.showPanel("calibration");
                }
            });
            toolBar1.add(button9);

            //---- 按键：下一帧 ----
            button10.setIcon(new ImageIcon(loadImage("icon/new/next.png")
                    .getImage().getScaledInstance(20,20,java.awt.Image.SCALE_SMOOTH)));
            button10.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (canvas.isPause) {
                        canvas.needNextScene = true;
                    }
                }
            });
            toolBar1.add(button10);
        }
        contentPane.add(toolBar1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 1, 5), 0, 0));

        //======== canvas展示区 ========
        contentPane.add(canvas, new GridBagConstraints(0, 1, 1, 12, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 1, 5), 0, 0));

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
            //======== 默认显示Node属性 ========
            {
                panel7.setLayout(new CardLayout());
                for (int i = 0; i < layerNames.length; i++) {
                    panel7.add(layerPanel.getLayer(layerNames[i]), layerNames[i]);
                }
            }
            panel2.add(panel7, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));
            //---- 交通要素选择器 ----
            comboBox1.setFont(new Font("\u65b0\u5b8b\u4f53", Font.PLAIN, 14));
            comboBox1.setModel(new DefaultComboBoxModel<>(layerNames));
            comboBox1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    String selectedItem = (String)comboBox1.getSelectedItem();
                    if(curLayerName != selectedItem){
                        //清除要素面板内容
                        layerPanel.getAction(curLayerName).resetTxtComponents();
                        //清除被选对象
                        canvas.deselect();
                    }

                    curLayerName = selectedItem;
                    ((CardLayout)panel7.getLayout()).show(panel7,curLayerName);
                }
            });
            panel2.add(comboBox1, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
                    GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                    new Insets(0, 3, 0, 3), 0, 0));
        }
        contentPane.add(panel2, new GridBagConstraints(1, 1, 1, 12, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 1, 0), 0, 0));

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
        //======== panel8 ========
        {
            panel8.setLayout(new GridBagLayout());
            ((GridBagLayout)panel8.getLayout()).columnWidths = new int[] {12, 42, 0, 0, 0, 0};
            ((GridBagLayout)panel8.getLayout()).rowHeights = new int[] {0, 0};
            ((GridBagLayout)panel8.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 0.0, 0.0, 1.0E-4};
            ((GridBagLayout)panel8.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

            //---- label5 ----
            label5.setText("\u72b6\u6001");
            panel8.add(label5, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(2, 5, 2, 2), 0, 0));

            //---- label8 ----
            label8.setText("\u4efb\u52a1\u6267\u884c\u4e2d");
            panel8.add(label8, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 2), 0, 0));

            //---- label9 ----
            label9.setText("(0%)");
            panel8.add(label9, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 2), 0, 0));
            panel8.add(progressBar1, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
        }
        contentPane.add(panel8, new GridBagConstraints(0, 14, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        windowFrame.setSize(880, 660);
        windowFrame.setLocationRelativeTo(windowFrame.getOwner());
        windowFrame.setVisible(true);
        windowFrame.requestFocusInWindow();
        animator.start();

    }
    public void initSimEngines(){
        FrameQueue.getInstance().initFrameQueue();
        engines = new SimulationEngine[1];
        switch (AppSetup.modelType) {
            case Constants.MODEL_TYPE_MESO:
                engines[0] = new MesoEngine(0,"E:\\test\\");
                break;
            case Constants.MODEL_TYPE_MLP:
                engines[0] = new MLPEngine(AppSetup.setupParameter.get("输入文件路径"));
                ((MLPEngine)engines[0]).displayOn = true;
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
    public void launchEngineWithParas(double[] paras, long seed){
        switch (AppSetup.modelType) {
            case Constants.MODEL_TYPE_MESO:
                //TODO 待设计传入参数运行
                ((MesoEngine)engines[0]).run();
                break;
            case Constants.MODEL_TYPE_MLP:
                if (seed>=0) {
                    ((MLPEngine)engines[0]).seedFixed = true;
                    ((MLPEngine)engines[0]).runningSeed = seed;
                }
                else
                    ((MLPEngine)engines[0]).seedFixed = false;
                    new Thread(() -> ((MLPEngine)engines[0]).runWithPara(paras==null ? MLPParameter.DEFAULT_PARAMETERS : paras)).start();
                break;
            default:
                break;
        }
    }
    public String getCurLayerName(){
        return curLayerName;
    }
    public LayerPanel getLayerPanel(){
        return layerPanel;
    }
    private ImageIcon loadImage(String relativePath) {
        return new ImageIcon(ClassLoader.getSystemResource(relativePath));
    }

}
