package testJavaFunction;

/**
 * The EngineGUIDemo uses a Swing UI to take an input number and calculate the factorial of a the given number.
 * Copyright 2016-2017 The MathWorks, Inc.
 */

import com.mathworks.engine.MatlabEngine;
import com.transyslab.commons.io.CSVUtils;
import org.apache.commons.csv.CSVRecord;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by yali on 2017/11/19.
 */
public class TestConnMatlab {

	// Declare an Engine future object and an Engine object. Future object engFuture is used to return the result in case of an asynchronous call
	private static Future<MatlabEngine> engFuture;
	private static MatlabEngine eng;
	private JTextField inputText;
	private JFrame mainFrame;
	private JLabel statusLabel;
	private JLabel startLabel;
	private JLabel inputLabel;
	private JLabel resLabel;
	private JPanel controlPanel;
	private JPanel headPanel;
	private JPanel resPanel;
	public double[][] testSeries;
	public TestConnMatlab() {
		prepareGUI();
		try {
			java.util.List<CSVRecord> results= CSVUtils.readCSV("R://SttSpeed2017-07WithoutHead.csv",null);
			int rows = results.size();
			int cols = results.get(0).size();
			testSeries = new double[rows][cols];
			for(int i=0;i<rows;i++){
				for(int j=0;j<cols;j++){
					testSeries[i][j] = Double.parseDouble(results.get(i).get(j));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		TestConnMatlab EngineGUIDemo = new TestConnMatlab();
		EngineGUIDemo.showEvent();
	}

	// Set the UI components for the window
	private void prepareGUI() {
		mainFrame = new JFrame("ADFullerTest calculator");
		mainFrame.setSize(450, 300);
		mainFrame.setLayout(new GridLayout(4, 1));

		statusLabel = new JLabel("Calculate Factorial of a given number using MATLAB", JLabel.CENTER);
		resLabel = new JLabel("");

		statusLabel.setSize(350, 100);
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				if (eng != null) {
					try {
						eng.disconnect();
					} catch (Exception ex) {}
				}
				System.exit(0);
			}
		});
		controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		headPanel = new JPanel();
		headPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		resPanel = new JPanel();
		resPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		mainFrame.add(statusLabel);
		mainFrame.add(headPanel);
		mainFrame.add(controlPanel);
		mainFrame.add(resPanel);
		mainFrame.setVisible(true);
		mainFrame.setLocationRelativeTo(null);
	}

	private void showEvent() {
		JButton StartButton = new JButton("Start MATLAB");
		JButton submitButton = new JButton("Calculate ADFullerTest");

		startLabel = new JLabel("");
		inputText = new JTextField("");
		inputText.setColumns(10);
		inputLabel = new JLabel("Input:");

		StartButton.setActionCommand("Start");
		submitButton.setActionCommand("Submit");

		StartButton.addActionListener(new ButtonClickListener());
		submitButton.addActionListener(new ButtonClickListener());

		headPanel.add(StartButton);
		headPanel.add(startLabel);
		controlPanel.add(inputLabel);
		controlPanel.add(inputText);

		controlPanel.add(submitButton);
		resPanel.add(resLabel);
		mainFrame.setVisible(true);
	}

	static class EngSwingWorker extends SwingWorker<Double, Object> {
		Future<Double> future;
		JLabel field;

		public EngSwingWorker(Future<Double> future_, JLabel field_) {
			future = future_;
			field = field_;
		}

		@Override
		public Double doInBackground() {
			double result = 0;
			try {
				result = future.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void done() {
			try {
				field.setText("TestResult: " + String.valueOf(this.get()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Declare action listeners for buttons.
	 * 1. Start MATLAB: Start MATLAB asynchronously, and return future result
	 * 2. Submit: Get the MATLAB Engine object from the future result, and use it to pass input to the workspace. Then call the 'factorial' function in MATLAB asynchronously, and get the result using a SwingWorker
	 * 3. Cancel: Cancel the asynchronous launch of MATLAB
	 */
	private class ButtonClickListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("Start")) {
				startLabel.setText("");
				resLabel.setText("");
				inputText.setText("");
				if (engFuture == null || engFuture.isCancelled()) {

					// Start a MATLAB session if the future is null or is cancelled
					engFuture = MatlabEngine.startMatlabAsync();
					startLabel.setText("MATLAB started asynchronously");
				} else {
					startLabel.setText("Active MATLAB session present");
				}
			} else if (command.equals("Submit")) {
				try {
					double input;
					if (inputText.getText().trim().isEmpty()) {
						resLabel.setForeground(Color.red);
						resLabel.setText("Error: No input provided");
					} else if (engFuture == null) {
						resLabel.setForeground(Color.red);
						resLabel.setText("Error: No active MATLAB session present");
					} else {
						resLabel.setForeground(Color.black);

						// Get the MATLAB engine object from the future. Use "fevalAsync" to call the "factorial" function in MATLAB asynchronously.
						eng = engFuture.get();
						input = Double.valueOf(inputText.getText());
						Future<Double> future = eng.fevalAsync("factorial", input);
						EngSwingWorker task = new EngSwingWorker(future, resLabel);
						task.execute();
					}
				} catch (NumberFormatException ex) {
					resLabel.setForeground(Color.red);
					resLabel.setText("Error: Invalid Input");
				} catch (CancellationException ex) {
					resLabel.setForeground(Color.red);
					resLabel.setText("Error: MATLAB session canceled");
				}
				catch(ExecutionException | InterruptedException ex){
				}
			}
		}
	}

}
