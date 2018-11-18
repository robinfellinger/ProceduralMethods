/*
 * A main program to create, init and display a ShaderPanel.
 *
 * Stefan Gustavson 2006-10-24
 *
 */

import java.awt.*;
import javax.swing.*;

public class ShaderMain {

	// Entry point for the application
	public static void main(String[] args) {
		
		// Instantiate a JFrame and set it up.
		JFrame frame = new JFrame();
		frame.setTitle("Shader demo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Add the content pane, we use only a single ShaderPanel here
		ShaderPanel panel = new ShaderPanel(500, 500);
		frame.add(panel);
		
		// Create and associate a Shader with the ShaderPanel
		panel.myShader = new DemoShader();

		// Pack and display the window
		frame.pack();
		frame.setVisible(true);

		// Create and start the animation thread
		Thread animationThread = new Thread(panel);
		animationThread.start();
	}

}
