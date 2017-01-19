package testwinform;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.SpringLayout;
import javax.swing.JToolBar;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Canvas;
import javax.swing.JButton;
import java.awt.CardLayout;

public class WinForm extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WinForm frame = new WinForm();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public WinForm() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1130, 747);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);
		
		JPanel panel = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, panel, 0, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, panel, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, panel, 37, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, panel, 953, SpringLayout.WEST, contentPane);
		contentPane.add(panel);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		JMenuBar menuBar = new JMenuBar();
		sl_panel.putConstraint(SpringLayout.NORTH, menuBar, 0, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, menuBar, 0, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.SOUTH, menuBar, 37, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, menuBar, 953, SpringLayout.WEST, panel);
		panel.add(menuBar);
		
		JMenu mnNewMenu = new JMenu("\u6587\u4EF6");
		mnNewMenu.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 13));
		menuBar.add(mnNewMenu);
		
		JMenu mnNewMenu_1 = new JMenu("\u7F16\u8F91");
		menuBar.add(mnNewMenu_1);
		
		JMenu menu = new JMenu("\u8FD0\u884C");
		menuBar.add(menu);
		
		JMenu menu_1 = new JMenu("\u65B9\u6848");
		menuBar.add(menu_1);
		
		JMenu menu_2 = new JMenu("\u7A97\u53E3");
		menuBar.add(menu_2);
		
		JMenu menu_3 = new JMenu("\u67E5\u770B");
		menuBar.add(menu_3);
		
		JMenu menu_4 = new JMenu("\u5E2E\u52A9");
		menuBar.add(menu_4);
		
		Canvas canvas = new Canvas();
		sl_contentPane.putConstraint(SpringLayout.NORTH, canvas, 37, SpringLayout.SOUTH, panel);
		sl_contentPane.putConstraint(SpringLayout.WEST, canvas, 0, SpringLayout.WEST, panel);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, canvas, 574, SpringLayout.SOUTH, panel);
		sl_contentPane.putConstraint(SpringLayout.EAST, canvas, 866, SpringLayout.WEST, contentPane);
		contentPane.add(canvas);
		
		JToolBar toolBar = new JToolBar();
		sl_contentPane.putConstraint(SpringLayout.NORTH, toolBar, 6, SpringLayout.SOUTH, panel);
		sl_contentPane.putConstraint(SpringLayout.WEST, toolBar, 0, SpringLayout.WEST, panel);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, toolBar, 0, SpringLayout.NORTH, canvas);
		sl_contentPane.putConstraint(SpringLayout.EAST, toolBar, -5, SpringLayout.EAST, panel);
		contentPane.add(toolBar);
		
		JButton btnNewButton = new JButton("");
		toolBar.add(btnNewButton);
	}
}
