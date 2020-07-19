package TaskManager.scripts.woodcutting;

import java.awt.EventQueue;

import javax.swing.JFrame;

public class WoodcutterGUI {

	private JFrame frameWoodcutter;
	private String title;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WoodcutterGUI window = new WoodcutterGUI("TEST");
					window.frameWoodcutter.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public WoodcutterGUI(String title) {
		this.title = title;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frameWoodcutter = new JFrame();
		frameWoodcutter.setTitle(title);
		frameWoodcutter.setBounds(100, 100, 450, 300);
		frameWoodcutter.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	public void open() {
		frameWoodcutter.setVisible(true);
	}
	
	public void close() {
		frameWoodcutter.setVisible(false);
	}

	public void exit() {
		frameWoodcutter.setVisible(false);
		frameWoodcutter.dispose();
	}

}
