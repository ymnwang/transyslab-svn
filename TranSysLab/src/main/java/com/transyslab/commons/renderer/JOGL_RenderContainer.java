package com.transyslab.commons.renderer;
import java.awt.event.*;
import javax.swing.*;
import com.jogamp.opengl.util.FPSAnimator;
public class JOGL_RenderContainer extends JFrame {
	// Define constants for the top-level container
	private String title_ = "JOGL 2.0 Setup (GLCanvas)"; // window's title
	private int fps_; // animator's target frames per second
	private JOGL_Canvas canvas_;
	private FPSAnimator animator_;

	/** Constructor to setup the top-level container and animator */
	public JOGL_RenderContainer() {
		// Create the OpenGL rendering canvas
		canvas_ = new JOGL_Canvas();
		fps_ = 60;
	}
	public JOGL_RenderContainer(int width, int height, int fps) {
		// Create the OpenGL rendering canvas
		canvas_ = new JOGL_Canvas(width, height);
		fps_ = fps;
	}
	@Override
	public void setTitle(String s) {
		title_ = s;
	}
	public void init() {

		// canvas_ = new JOGL_Canvas();
		// canvas_.setPreferredSize(new Dimension(canvasWidth_, canvasHeight_));
		JOGL_Camera cam = new JOGL_Camera();
		canvas_.setCamera(cam);
		canvas_.addKeyListener(cam);
		canvas_.addMouseListener(cam);
		canvas_.addMouseWheelListener(cam);
		// Create a animator that drives canvas' display() at the specified FPS.
		animator_ = new FPSAnimator(canvas_, fps_, true);

		// Create the top-level container frame
		this.getContentPane().add(canvas_);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// Use a dedicate thread to run the stop() to ensure that the
				// animator stops before program exits.
				new Thread() {
					@Override
					public void run() {
						if (animator_.isStarted())
							animator_.stop();
						System.exit(0);
					}
				}.start();
			}
		});
		this.setTitle(title_);
		this.pack();
		this.setVisible(true);

	}
	public void render() {
		animator_.start(); // start the animation loop
	}
}
