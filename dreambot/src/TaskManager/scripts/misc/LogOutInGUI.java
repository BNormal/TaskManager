package TaskManager.scripts.misc;

import java.awt.EventQueue;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;


import java.awt.event.ActionListener;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.JButton;

public class LogOutInGUI {
	private JFrame frame;
	private String title;
	private JTextField txtNickname;
	private JRadioButton rdbtnLogout;
	private JRadioButton rdbtnLogin;

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
		frame.setLocationRelativeTo(null);
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
		
		rdbtnLogin = new JRadioButton("Login");
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
	
	public String getSaveDate() {
		Gson gson = new GsonBuilder().create();
		List<String> settings = new ArrayList<String>();
		settings.add(txtNickname.getText());
		settings.add(rdbtnLogout.isSelected() + "");
		return gson.toJson(settings);
	}

	public void loadSaveDate(String json) {
		Gson gson = new Gson();
		List<String> settings = new ArrayList<String>();
		Type type = new TypeToken<List<String>>() {}.getType();
		settings = gson.fromJson(json, type);
		txtNickname.setText(settings.get(0));
		if (settings.get(1).equalsIgnoreCase("true"))
			rdbtnLogout.setSelected(true);
		else
			rdbtnLogin.setSelected(true);
	}
}
