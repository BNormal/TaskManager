package Mule;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.awt.event.ActionEvent;
import javax.swing.UIManager;

import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.wrappers.items.Item;

import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import javax.swing.JSeparator;

public class GUI {

	private JFrame frmMule;

	private Mule mule;
	private JTextField txtMasterUsername;
	private Properties prop;
	private String propFileName = "mule.properties";
	private String username;
	private JComboBox<String> cbBanks;
	private JTextField txtX;
	private JTextField txtY;
	private JLabel lblLocation;
	private JLabel lblTrade;
	private ItemListGUI itemGUI;
	private JCheckBox chkNoting;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					@SuppressWarnings("unused")
					GUI window = new GUI(null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI(Mule mule) {
		this.mule = mule;
		initialize();
	}
	
	public void loadProperties() {
		prop = new Properties();
		try {
			InputStream inputStream = new FileInputStream(propFileName);
			prop.load(inputStream);
			username = prop.getProperty("username");
			String[] tile = prop.getProperty("tile").split(":");
			int itemId = Integer.parseInt(prop.getProperty("tradeItem"));
			boolean isNoting = Boolean.parseBoolean(prop.getProperty("noting"));
			int bankLoc = Integer.parseInt(prop.getProperty("bank_location"));
			int x = Integer.parseInt(tile[0]);
			int y = Integer.parseInt(tile[1]);
			int z = Integer.parseInt(tile[2]);
			mule.setDestTile(new Tile(x, y, z));
			txtMasterUsername.setText(username);
			lblLocation.setText("X: " + x + ", Y: " + y + ", Z: " + z);
			setItemId(itemId);
			chkNoting.setSelected(isNoting);
			mule.setNoting(isNoting);
			cbBanks.setSelectedIndex(bankLoc);
		} catch (Exception e) {
		}
	}
	
	public void saveProperties() {
		OutputStream outputStream;
		try {
			boolean hasChanges = true;
			/*if (txtMasterUsername != null && (username == null || !username.equals(txtMasterUsername.getText()))) {
				hasChanges = true;
			}*/
			if (hasChanges) {
				outputStream = new FileOutputStream(propFileName);
				prop.setProperty("username", txtMasterUsername.getText());
				Tile tile = mule.getDestTile();
				prop.setProperty("tile", tile.getX() + ":" + tile.getY() + ":" + tile.getZ());
				prop.setProperty("tradeItem", mule.getItemId() + "");
				prop.setProperty("noting", chkNoting.isSelected() + "");
				prop.setProperty("bank_location", cbBanks.getSelectedIndex() + "");
				prop.store(outputStream, "");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void start() {
		String username = txtMasterUsername.getText();
		if (username != null && ! username.equals("")) {
			saveProperties();
			frmMule.setVisible(false);
			if (mule != null) {
				mule.setUsername(username);
				mule.setRunning(true);
			}
		} else {
			JOptionPane.showMessageDialog(null, "Please enter a username!", "Missing master username", JOptionPane.WARNING_MESSAGE);
		}
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
		if (mule != null)
			mule.setDestTile(new Tile(3200, 3200, 0));
		frmMule = new JFrame();
		frmMule.setTitle("Wine Mule Trader");
		frmMule.setBounds(100, 100, 300, 250);
		frmMule.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmMule.getContentPane().setLayout(null);
		frmMule.setLocationRelativeTo(null);
		frmMule.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	mule.stop();
		    	windowEvent.getWindow().dispose();
		    }
		});
		JLabel lblMasterUsername = new JLabel("Master Username:");
		lblMasterUsername.setBounds(10, 11, 88, 14);
		frmMule.getContentPane().add(lblMasterUsername);
		
		txtMasterUsername = new JTextField();
		txtMasterUsername.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getID() == 1001) {
					start();
				}
			}
		});
		txtMasterUsername.setBounds(108, 8, 165, 20);
		frmMule.getContentPane().add(txtMasterUsername);
		txtMasterUsername.setColumns(10);
		
		JButton btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				start();
			}
		});
		btnStart.setBounds(10, 176, 263, 22);
		frmMule.getContentPane().add(btnStart);
		
		cbBanks = new JComboBox<String>();
		cbBanks.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mule.setBankName(cbBanks.getSelectedItem().toString());
			}
		});
		cbBanks.setModel(new DefaultComboBoxModel<String>(new String[] {"Falador West", "Falador East", "Edgeville", "Lumbridge", "Varrock West", "Varrock East", "Draynor", "Grand Exchange", "Al-Karid"}));
		cbBanks.setBounds(108, 78, 165, 23);
		frmMule.getContentPane().add(cbBanks);
		
		JLabel lblBank = new JLabel("Bank Items at:");
		lblBank.setBounds(10, 78, 88, 23);
		frmMule.getContentPane().add(lblBank);
		
		JLabel lblWaitingLocation = new JLabel("Waiting Location:");
		lblWaitingLocation.setBounds(10, 112, 102, 20);
		frmMule.getContentPane().add(lblWaitingLocation);
		
		JLabel lblX = new JLabel("X:");
		lblX.setBounds(152, 146, 16, 14);
		frmMule.getContentPane().add(lblX);
		
		lblLocation = new JLabel("");
		lblLocation.setHorizontalAlignment(SwingConstants.TRAILING);
		lblLocation.setBounds(108, 112, 165, 20);
		frmMule.getContentPane().add(lblLocation);
		
		txtX = new JTextField();
		txtX.setBounds(165, 143, 42, 20);
		frmMule.getContentPane().add(txtX);
		txtX.setColumns(10);
		txtX.addKeyListener(new KeyAdapter() {
	        @Override
	        public void keyTyped(KeyEvent e) {

	        	char c = e.getKeyChar();
	            if (!((c >= '0') && (c <= '9') ||
	               (c == KeyEvent.VK_BACK_SPACE) ||
	               (c == KeyEvent.VK_DELETE))) {
	              e.consume();
	            }
	        }
	    });
		
		txtY = new JTextField();
		txtY.setColumns(10);
		txtY.setBounds(231, 143, 42, 20);
		txtY.addKeyListener(new KeyAdapter() {
	        @Override
	        public void keyTyped(KeyEvent e) {

	        	char c = e.getKeyChar();
	            if (!((c >= '0') && (c <= '9') ||
	               (c == KeyEvent.VK_BACK_SPACE) ||
	               (c == KeyEvent.VK_DELETE))) {
	              e.consume();
	            }
	        }
	    });
		frmMule.getContentPane().add(txtY);
		
		JLabel lblY = new JLabel("Y:");
		lblY.setBounds(217, 146, 16, 14);
		frmMule.getContentPane().add(lblY);
		
		JButton btnAssign = new JButton("Change Trade Spot");
		btnAssign.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int x = 0;
				int y = 0;
				int z = 0;
				if (txtX != null && !txtX.getText().equals("") && txtY != null && !txtY.getText().equals("")) {
					x = Integer.parseInt(txtX.getText());
					y = Integer.parseInt(txtY.getText());
					mule.setDestTile(new Tile(x, y, z));
				} else {
					x = mule.getLocalPlayer().getTile().getX();
					y = mule.getLocalPlayer().getTile().getY();
					z = mule.getLocalPlayer().getTile().getZ();
					mule.setDestTile(new Tile(x, y, z));
				}
				lblLocation.setText("X: " + x + ", Y: " + y + ", Z: " + z);
			}
		});
		btnAssign.setBounds(10, 143, 130, 22);
		frmMule.getContentPane().add(btnAssign);
		
		chkNoting = new JCheckBox("Noting");
		chkNoting.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mule.setNoting(chkNoting.isSelected());
			}
		});
		chkNoting.setBounds(217, 38, 57, 22);
		frmMule.getContentPane().add(chkNoting);
		
		lblTrade = new JLabel("Trading: " + mule.getItemName());//mule.getItemName()
		lblTrade.setBounds(85, 39, 130, 20);
		frmMule.getContentPane().add(lblTrade);
		
		JButton btnItem = new JButton("Change");
		btnItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				itemGUI.setVisible(true);
			}
		});
		btnItem.setBounds(10, 39, 69, 20);
		frmMule.getContentPane().add(btnItem);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(10, 69, 263, 2);
		frmMule.getContentPane().add(separator);
		loadProperties();
		itemGUI = new ItemListGUI(mule, this);
		frmMule.setVisible(true);
	}
	
	public void setItemId(int itemId) {
		Item item = new Item(itemId, 0, mule.getClient().getInstance());
		lblTrade.setText("Trading: " + item.getName());
		mule.setItemId(itemId);
	}
	
	public JFrame getFrmMule() {
		return frmMule;
	}

	public void setFrmMule(JFrame frmMule) {
		this.frmMule = frmMule;
	}
}
