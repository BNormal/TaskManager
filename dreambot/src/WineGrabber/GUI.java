package WineGrabber;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextField;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.awt.event.ActionEvent;
import javax.swing.UIManager;

public class GUI {

	private JFrame frmWineGrabber;
	private JTextField txtSlaveName;
	private Properties prop;
	private String propFileName = "winegrab.properties";
	private String username;
	private WineGrabber wine;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI(null);
					window.frmWineGrabber.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI(WineGrabber wine) {
		this.wine = wine;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		frmWineGrabber = new JFrame();
		frmWineGrabber.setTitle("Wine Grabber");
		frmWineGrabber.setBounds(100, 100, 229, 118);
		frmWineGrabber.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmWineGrabber.getContentPane().setLayout(null);
		frmWineGrabber.setLocationRelativeTo(null);
		
		txtSlaveName = new JTextField();
		txtSlaveName.setBounds(81, 11, 125, 20);
		frmWineGrabber.getContentPane().add(txtSlaveName);
		txtSlaveName.setColumns(10);
		
		JLabel lblSlaveName = new JLabel("Slave Name:");
		lblSlaveName.setBounds(10, 14, 65, 14);
		frmWineGrabber.getContentPane().add(lblSlaveName);
		
		JButton btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				start();
			}
		});
		btnStart.setBounds(10, 42, 193, 29);
		frmWineGrabber.getContentPane().add(btnStart);
		loadProperties();
		frmWineGrabber.setVisible(true);
	}
	
	public void start() {
		String username = txtSlaveName.getText();
		if (username != null && ! username.equals("")) {
			saveProperties();
			frmWineGrabber.setVisible(false);
			if (wine != null) {
				wine.setUsername(username);
				wine.setRunning(true);
			}
		} else {
			JOptionPane.showMessageDialog(null, "Please enter a username!", "Missing slave username", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	public void loadProperties() {
		prop = new Properties();
		try {
			InputStream inputStream = new FileInputStream(propFileName);
			prop.load(inputStream);
			username = prop.getProperty("username");
			txtSlaveName.setText(username);
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
	
	public void saveProperties() {
		OutputStream outputStream;
		try {
			boolean hasChanges = true;
			if (hasChanges) {
				outputStream = new FileOutputStream(propFileName);
				prop.setProperty("username", txtSlaveName.getText());
				prop.store(outputStream, "");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
