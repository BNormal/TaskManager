package TaskManager.scripts.misc;

import java.awt.EventQueue;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JButton;

public class LogOutInGUI {
	private transient JFrame frame;
	private String title;
	private transient JTextField txtNickname;
	private transient JRadioButton rdbtnLogout;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LogOutInGUI window = new LogOutInGUI("TEST");
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public LogOutInGUI(String title) {
		this.title = title;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle(title);
		frame.setBounds(100, 100, 250, 145);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblNickname = new JLabel("Nickname:");
		lblNickname.setEnabled(false);
		lblNickname.setBounds(10, 14, 57, 14);
		frame.getContentPane().add(lblNickname);
		
		txtNickname = new JTextField();
		txtNickname.setEnabled(false);
		txtNickname.setBounds(77, 11, 149, 20);
		frame.getContentPane().add(txtNickname);
		txtNickname.setColumns(10);
		
		rdbtnLogout = new JRadioButton("Logout");
		rdbtnLogout.setFocusable(false);
		rdbtnLogout.setMargin(new Insets(0, 0, 0, 0));
		rdbtnLogout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lblNickname.setEnabled(false);
				txtNickname.setEnabled(false);
			}
		});
		rdbtnLogout.setSelected(true);
		rdbtnLogout.setBounds(57, 39, 86, 23);
		frame.getContentPane().add(rdbtnLogout);
		
		JRadioButton rdbtnLogin = new JRadioButton("Login");
		rdbtnLogin.setFocusable(false);
		rdbtnLogin.setMargin(new Insets(0, 0, 0, 0));
		rdbtnLogin.setBounds(149, 40, 77, 23);
		frame.getContentPane().add(rdbtnLogin);
		rdbtnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lblNickname.setEnabled(true);
				txtNickname.setEnabled(true);
			}
		});
		
		ButtonGroup btnGroup = new ButtonGroup();
		btnGroup.add(rdbtnLogout);
		btnGroup.add(rdbtnLogin);
		
		JButton btnFinished = new JButton("Finished");
		btnFinished.setFocusable(false);
		btnFinished.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		btnFinished.setBounds(10, 71, 216, 30);
		frame.getContentPane().add(btnFinished);
	}

	public boolean isLoggingOut() {
		return rdbtnLogout.isSelected();
	}

	public void setLoggingOut(boolean isLoggingOut) {
		rdbtnLogout.setSelected(isLoggingOut);
	}
	
	public String getNickname() {
		return txtNickname.getText();
	}
	
	public void open() {
		frame.setVisible(true);
	}
	
	public void close() {
		frame.setVisible(false);
	}

	public void exit() {
		frame.setVisible(false);
		frame.dispose();
	}
}
