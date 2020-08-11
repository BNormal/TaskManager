package TaskManager.scripts.misc;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultFormatter;
import javax.swing.JList;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import TaskManager.utilities.Utilities;

public class GETraderGUI {

	private JFrame frame;
	private List<Integer> priceIncrements = new ArrayList<Integer>();
	private Map<Integer, DisplayItem> itemList = new HashMap<Integer, DisplayItem>();
	private JTextField txtItemName;
	private JList<DisplayItem> listItems;
	private DefaultListModel<DisplayItem> modelItems;
	private JList<OfferItem> listOffers;
	private DefaultListModel<OfferItem> modelOffers;
	private int itemId = -1;
	private JPanel itemImage;
	private JLabel lblLoading;
	private JLabel lblItemName;
	private JTextArea lblItemInfo;
	private JCheckBox chckbxF2P;
	private ImageIcon f2pIcon;
	private ImageIcon membersIcon;
	private Thread spriteThread;
	private long typeDelay = 0;
	private JSpinner spinnerCurrentPrice;
	private JSpinner spinnerQuantity;
	private JButton btnIncreasePrice;
	private JButton btnDecreasePrice;
	private JButton btnResetPrice;
	private JButton btnSell;
	private JButton btnBuy;
	private JButton btnRemove;
	private JButton btnMoveDown;
	private JButton btnMoveUp;
	private JButton btnStart;
	private JCheckBox chckbxWaitForInstant;
	private JCheckBox chckbxWUC;
	private JSpinner spinnerMaxIncrements;
	private boolean isFinished;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GETraderGUI window = new GETraderGUI("TEST");
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
	public GETraderGUI(String title) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		initialize(title);
	}
	
	/**
	 * Initialize the contents of the2frame.
	 */
	@SuppressWarnings("serial")
	private void initialize(String title) {
		isFinished = false;
		frame = new JFrame();
		frame.setResizable(false);
		frame.setTitle(title);
		frame.setBounds(100, 100, 500, 415);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		txtItemName = new JTextField();
		txtItemName.setEnabled(false);
		txtItemName.setColumns(10);
		txtItemName.setBounds(86, 12, 156, 20);
		txtItemName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				String itemName = txtItemName.getText();
				if (itemName != null && !itemName.equals("") && itemName.length() > 2) {
					typeDelay = System.currentTimeMillis();
					updateList(itemName);
				} else {
					modelItems.clear();
					itemImage.repaint();
				}
			}
		});
		frame.getContentPane().add(txtItemName);
		
		lblItemName = new JLabel("");
		lblItemName.setOpaque(false);
		lblItemName.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblItemName.setBounds(306, 18, 172, 20);
		frame.getContentPane().add(lblItemName);
		
		JLabel lblName = new JLabel("Item Name:");
		lblName.setBounds(14, 15, 62, 14);
		frame.getContentPane().add(lblName);
		
		JScrollPane scrollItems = new JScrollPane();
		
		modelItems = new DefaultListModel<DisplayItem>();
		
		listItems = new JList<DisplayItem>(modelItems);
		listItems.setCellRenderer(new ItemImageRenderer());
		listItems.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				handleSelectedItem();
			}
		});
		
		scrollItems.setViewportView(listItems);
		scrollItems.setBounds(10, 66, 232, 185);
		frame.getContentPane().add(scrollItems);
		
		itemImage = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				ImageIcon sprite = null;
				if (itemId > -1) {
					sprite = itemList.get(itemId).getSprite();
					if (sprite == null) {
						try {
							sprite = new ImageIcon(new URL("https://www.osrsbox.com/osrsbox-db/items-icons/" + itemId + ".png"));
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}
					}
				}
				if (sprite != null) {
					g.drawImage(sprite.getImage(), (itemImage.getWidth() - sprite.getIconWidth()) / 2 + 1, (itemImage.getHeight() - sprite.getIconHeight()) / 2, null);
				}
			}
		};
		itemImage.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		itemImage.setBounds(256, 11, 40, 36);
		frame.getContentPane().add(itemImage);
		
		chckbxF2P = new JCheckBox("F2P only");
		chckbxF2P.setToolTipText("Only display free to play items in the listing");
		chckbxF2P.setEnabled(false);
		chckbxF2P.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String itemName = txtItemName.getText();
				itemId = -1;
				itemImage.repaint();
				if (itemName != null && !itemName.equals("") && itemName.length() > 2) {
					updateList(itemName);
				}
			}
		});
		chckbxF2P.setFocusable(false);
		chckbxF2P.setBounds(10, 39, 72, 20);
		frame.getContentPane().add(chckbxF2P);
		
		lblLoading = new JLabel("Loading data...");
		lblLoading.setFont(new Font("Tahoma", Font.ITALIC, 11));
		lblLoading.setBounds(86, 39, 156, 20);
		frame.getContentPane().add(lblLoading);
		
		lblItemInfo = new JTextArea("");
		lblItemInfo.setLineWrap(true);
		lblItemInfo.setWrapStyleWord(true);
		lblItemInfo.setOpaque(false);
		lblItemInfo.setEditable(false);
		lblItemInfo.setBounds(256, 70, 222, 74);
		lblItemInfo.setFont(new Font("Tahoma", Font.PLAIN, 11));
		frame.getContentPane().add(lblItemInfo);
		
		JLabel lblBar = new JLabel("------------------------------------");
		lblBar.setOpaque(false);
		lblBar.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblBar.setBounds(256, 48, 218, 20);
		frame.getContentPane().add(lblBar);
	    
		spinnerCurrentPrice = new JSpinner();
		spinnerCurrentPrice.setToolTipText("Desired price for selected item");
		spinnerCurrentPrice.setEnabled(false);
		spinnerCurrentPrice.setModel(new SpinnerNumberModel(new Long(1), null, null, new Long(1)));
		spinnerCurrentPrice.setBounds(256, 150, 106, 20);
		JComponent comp = spinnerCurrentPrice.getEditor();
		JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
		DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
		formatter.setCommitsOnValidEdit(true);
		formatter.setAllowsInvalid(false);
		spinnerCurrentPrice.addChangeListener(new ChangeListener() {
	        @Override
	        public void stateChanged(ChangeEvent e) {
	        	long amount = Long.parseLong(spinnerCurrentPrice.getValue().toString());
	        	if (amount < 1)
	        		amount = 1;
	        	else if (amount > Integer.MAX_VALUE)
	        		amount = Integer.MAX_VALUE;
	        	spinnerCurrentPrice.setValue(amount);
	        	priceIncrements.clear();
	        	priceIncrements.add((int) amount);
	        }
	    });
		frame.getContentPane().add(spinnerCurrentPrice);
		
		spinnerQuantity = new JSpinner();
		spinnerQuantity.setToolTipText("Desired quantity for selected item");
		spinnerQuantity.setEnabled(false);
		spinnerQuantity.setModel(new SpinnerNumberModel(new Long(1), null, null, new Long(1)));
		spinnerQuantity.setBounds(372, 150, 106, 20);
		JFormattedTextField field2 = (JFormattedTextField) spinnerQuantity.getEditor().getComponent(0);
		DefaultFormatter formatter2 = (DefaultFormatter) field2.getFormatter();
		formatter2.setCommitsOnValidEdit(true);
		formatter2.setAllowsInvalid(false);
		spinnerQuantity.addChangeListener(new ChangeListener() {
	        @Override
	        public void stateChanged(ChangeEvent e) {
	        	long amount = Long.parseLong(spinnerQuantity.getValue().toString());
	        	if (amount < 1)
	        		spinnerQuantity.setValue(1);
	        	else if (amount > Integer.MAX_VALUE)
	        		spinnerQuantity.setValue(Integer.MAX_VALUE);
	        }
	    });
		frame.getContentPane().add(spinnerQuantity);
		
		btnDecreasePrice = new JButton("\u2193");
		btnDecreasePrice.setToolTipText("Decrease price");
		btnDecreasePrice.setEnabled(false);
		btnDecreasePrice.setMargin(new Insets(0, 0, 0, 0));
		btnDecreasePrice.setBounds(266, 181, 26, 22);
		btnDecreasePrice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!spinnerCurrentPrice.isEnabled())
					return;
				int price = Integer.parseInt(spinnerCurrentPrice.getValue().toString());
				spinnerCurrentPrice.setValue(calculatePrice(price, false));
				priceIncrements.add(-2);
			}
		});
		frame.getContentPane().add(btnDecreasePrice);
		
		btnResetPrice = new JButton("\u21BB");
		btnResetPrice.setToolTipText("Reset price");
		btnResetPrice.setEnabled(false);
		btnResetPrice.setMargin(new Insets(0, 0, 0, 0));
		btnResetPrice.setBounds(296, 181, 26, 22);
		btnResetPrice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!spinnerCurrentPrice.isEnabled() || itemId == -1)
					return;
				DisplayItem item = itemList.get(itemId);
				spinnerCurrentPrice.setValue(item.getPrice());
				priceIncrements.clear();
			}
		});
		frame.getContentPane().add(btnResetPrice);
		
		btnIncreasePrice = new JButton("\u2191");
		btnIncreasePrice.setToolTipText("Increase price");
		btnIncreasePrice.setEnabled(false);
		btnIncreasePrice.setMargin(new Insets(0, 0, 0, 0));
		btnIncreasePrice.setBounds(326, 181, 26, 22);
		btnIncreasePrice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!spinnerCurrentPrice.isEnabled())
					return;
				int price = Integer.parseInt(spinnerCurrentPrice.getValue().toString());
				spinnerCurrentPrice.setValue(calculatePrice(price, true));
				priceIncrements.add(-1);
			}
		});
		frame.getContentPane().add(btnIncreasePrice);
		
		JScrollPane scrollOffers = new JScrollPane();
		scrollOffers.setBounds(10, 262, 468, 83);
		frame.getContentPane().add(scrollOffers);
		
		modelOffers = new DefaultListModel<OfferItem>();
		listOffers = new JList<OfferItem>(modelOffers);
		listOffers.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				updateOfferList();
			}
		});
		scrollOffers.setViewportView(listOffers);
		
		btnBuy = new JButton("Buy");
		btnBuy.setEnabled(false);
		btnBuy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (itemId < 0)
					return;
				OfferItem offer = new OfferItem(itemList.get(itemId), Integer.parseInt(spinnerCurrentPrice.getValue().toString()), 
						Integer.parseInt(spinnerQuantity.getValue().toString()), true, priceIncrements, chckbxWUC.isSelected(), 
						chckbxWaitForInstant.isSelected() ? Integer.parseInt(spinnerMaxIncrements.getValue().toString()) : -1);
				priceIncrements.clear();
				modelOffers.addElement(offer);
				listOffers.ensureIndexIsVisible(modelOffers.size() - 1);
				updateOfferList();
			}
		});
		btnBuy.setMargin(new Insets(0, 0, 0, 0));
		btnBuy.setBounds(428, 181, 48, 22);
		frame.getContentPane().add(btnBuy);
		
		btnSell = new JButton("Sell");
		btnSell.setEnabled(false);
		btnSell.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (itemId < 0)
					return;
				OfferItem offer = new OfferItem(itemList.get(itemId), Integer.parseInt(spinnerCurrentPrice.getValue().toString()), 
						Integer.parseInt(spinnerQuantity.getValue().toString()), false, priceIncrements, chckbxWUC.isSelected(), 
						chckbxWaitForInstant.isSelected() ? Integer.parseInt(spinnerMaxIncrements.getValue().toString()) : -1);
				priceIncrements.clear();
				modelOffers.addElement(offer);
				listOffers.ensureIndexIsVisible(modelOffers.size() - 1);
				updateOfferList();
			}
		});
		btnSell.setMargin(new Insets(0, 0, 0, 0));
		btnSell.setBounds(375, 181, 48, 22);
		frame.getContentPane().add(btnSell);
		
		btnMoveDown = new JButton("\u2193");
		btnMoveDown.setToolTipText("Move offer down the list");
		btnMoveDown.setMargin(new Insets(0, 0, 0, 0));
		btnMoveDown.setEnabled(false);
		btnMoveDown.setBounds(14, 356, 26, 22);
		btnMoveDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int index = listOffers.getSelectedIndex();
				if (index < 0 || index + 1 >= modelOffers.size())
					return;
				OfferItem offer1 = modelOffers.getElementAt(index);
				OfferItem offer2 = modelOffers.getElementAt(index + 1);
				modelOffers.setElementAt(offer2, index);
				modelOffers.setElementAt(offer1, index + 1);
				listOffers.setSelectedIndex(index + 1);
				listOffers.ensureIndexIsVisible(index + 1);
			}
		});
		frame.getContentPane().add(btnMoveDown);
		
		btnMoveUp = new JButton("\u2191");
		btnMoveUp.setToolTipText("Move offer up the list");
		btnMoveUp.setMargin(new Insets(0, 0, 0, 0));
		btnMoveUp.setEnabled(false);
		btnMoveUp.setBounds(50, 356, 26, 22);
		btnMoveUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int index = listOffers.getSelectedIndex();
				if (index < 0 || index - 1 < 0)
					return;
				OfferItem offer1 = modelOffers.getElementAt(index);
				OfferItem offer2 = modelOffers.getElementAt(index - 1);
				modelOffers.setElementAt(offer2, index);
				modelOffers.setElementAt(offer1, index - 1);
				listOffers.setSelectedIndex(index - 1);
				listOffers.ensureIndexIsVisible(index - 1);
			}
		});
		frame.getContentPane().add(btnMoveUp);
		
		btnRemove = new JButton("Remove");
		btnRemove.setToolTipText("Remove offer from list");
		btnRemove.setMargin(new Insets(0, 0, 0, 0));
		btnRemove.setEnabled(false);
		btnRemove.setBounds(86, 356, 60, 22);
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int index = listOffers.getSelectedIndex();
				if (index > -1) {
					modelOffers.remove(index);
					if (modelOffers.size() > 0) {
						if (index == modelOffers.size()) {
							listOffers.setSelectedIndex(index - 1);
							listOffers.ensureIndexIsVisible(index - 1);
						} else {
							listOffers.setSelectedIndex(index);
							listOffers.ensureIndexIsVisible(index);
						}
					}
					updateOfferList();
				}
			}
		});
		frame.getContentPane().add(btnRemove);
		
		chckbxWUC = new JCheckBox("Wait until completed");
		chckbxWUC.setEnabled(false);
		chckbxWUC.setToolTipText("<html>Wait until this offer has been sold/bought<br>before preceding to the next offer</html>");
		chckbxWUC.setSelected(true);
		chckbxWUC.setBounds(252, 208, 123, 23);
		frame.getContentPane().add(chckbxWUC);
		
		spinnerMaxIncrements = new JSpinner();
		spinnerMaxIncrements.setModel(new SpinnerNumberModel(1, null, null, 1));
		spinnerMaxIncrements.setToolTipText("Max increments to increase/decrease the price offer by 5%");
		spinnerMaxIncrements.setEnabled(false);
		spinnerMaxIncrements.setBounds(428, 235, 50, 20);
		JFormattedTextField field3 = (JFormattedTextField) spinnerMaxIncrements.getEditor().getComponent(0);
		DefaultFormatter formatter3 = (DefaultFormatter) field3.getFormatter();
		formatter3.setCommitsOnValidEdit(true);
		formatter3.setAllowsInvalid(false);
		spinnerMaxIncrements.addChangeListener(new ChangeListener() {
	        @Override
	        public void stateChanged(ChangeEvent e) {
	        	long amount = Integer.parseInt(spinnerMaxIncrements.getValue().toString());
	        	if (amount < 1)
	        		spinnerMaxIncrements.setValue(1);
	        	else if (amount > 10)
	        		spinnerMaxIncrements.setValue(10);
	        }
	    });
		frame.getContentPane().add(spinnerMaxIncrements);
		
		chckbxWaitForInstant = new JCheckBox("Modify price for instant offer");
		chckbxWaitForInstant.setEnabled(false);
		chckbxWaitForInstant.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (chckbxWaitForInstant.isSelected())
					spinnerMaxIncrements.setEnabled(true);
				else
					spinnerMaxIncrements.setEnabled(false);
			}
		});
		chckbxWaitForInstant.setToolTipText("<html>Wait a few seconds, if offer hasn't instantly sold/bought<br>increase/decrease the price offer</html>");
		chckbxWaitForInstant.setBounds(252, 234, 171, 23);
		frame.getContentPane().add(chckbxWaitForInstant);
		
		btnStart = new JButton("Start");
		btnStart.setToolTipText("Start the script");
		btnStart.setHorizontalTextPosition(SwingConstants.CENTER);
		btnStart.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnStart.setMargin(new Insets(0, 0, 0, 0));
		btnStart.setEnabled(false);
		btnStart.setBounds(418, 356, 60, 22);
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				close();
				isFinished = true;
			}
		});
		frame.getContentPane().add(btnStart);
		
		Thread thread = new Thread() {
			public void run() {
				try {
					f2pIcon = new ImageIcon(new URL("https://static.wikia.nocookie.net/2007scape/images/6/6f/Free-to-play_icon.png"));
					lblLoading.setText("Loading free to play star...");
					membersIcon = new ImageIcon(new URL("https://static.wikia.nocookie.net/2007scape/images/9/9c/Member_icon.png"));
					lblLoading.setText("Loading pay to play star...");
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				getItemListData();
			}
		};
		thread.start();
	}

	private void loadSprites() {
		String text = txtItemName.getText();
		lblLoading.setText("Loading listed item images...");
		for (int i = 0; i < modelItems.size(); i++) {
			DisplayItem item = modelItems.get(i);
			if (item.getSprite() == null) {
				try {
					ImageIcon icon = new ImageIcon(new URL("https://www.osrsbox.com/osrsbox-db/items-icons/" + item.getID() + ".png"));
					item.setSprite(icon);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							listItems.updateUI();
						}
					});
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
			if (!txtItemName.getText().equals(text))
				return;
		}
		lblLoading.setText("");
		//System.out.println(listItems.getFirstVisibleIndex() + ":" + listItems.getLastVisibleIndex());
	}
	
	private void getItemListData() {
		lblLoading.setText("Loading items information...");
		File jsonFile = new File("GEItems.json");
		boolean loaded = false;
		if (jsonFile.exists()) {
			try {
				Gson gson = new Gson();
				FileReader fr = new FileReader("GEItems.json");
				BufferedReader br = new BufferedReader(fr);
				Type type = new TypeToken<Map<Integer, DisplayItem>>() {}.getType();
				Map<Integer, DisplayItem> items = gson.fromJson(br, type);
				itemList = items;
				loaded = true;
				fr.close();
				chckbxF2P.setEnabled(true);
				txtItemName.setEnabled(true);
				lblLoading.setText("");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (!loaded) {
			try {
				Gson gson = new Gson();
				Type type = new TypeToken<HashMap<String, JsonElement>>() {}.getType();
				lblLoading.setText("Downloading item list information...");
				Map<String, JsonElement> map = gson.fromJson(Utilities.getJsonFromURL("https://rsbuddy.com/exchange/summary.json"), type);
				for (Entry<String, JsonElement> entry : map.entrySet()) {
					JsonObject value = (JsonObject) entry.getValue();
					String name = value.get("name").toString();
					name = name.substring(1, name.length() - 1);
					DisplayItem item = new DisplayItem(new Integer(value.get("id").toString()), name, value.get("members").toString().equals("true"));
					itemList.put(item.getID(), item);
				}
				try {
					lblLoading.setText("Saving item list information...");
					FileWriter fw = new FileWriter(jsonFile);
					gson.toJson(itemList, fw);
					fw.close();
					lblLoading.setText("");
				} catch (JsonIOException | IOException e1) {
					lblLoading.setText("Failed to save item list information.");
					e1.printStackTrace();
				}
				chckbxF2P.setEnabled(true);
				txtItemName.setEnabled(true);
			} catch (Exception e) {
				lblLoading.setText("Failed download item list information.");
				e.printStackTrace();
			}
		}
	}
	
	private void getItemInfo(int id) {
		try {
			Gson gson = new Gson();
			JsonElement json = gson.fromJson(Utilities.getJsonFromURL("https://www.osrsbox.com/osrsbox-db/items-json/" + id + ".json"), JsonElement.class);
			JsonObject value = (JsonObject) json;
			DisplayItem item = itemList.get(id);
			String desc = value.get("examine").toString();
			desc = desc.substring(1, desc.length() - 1);
			item.setDescription(desc);
			String buyLimit = value.get("buy_limit").toString();
			item.setLimit(buyLimit.equals("null") ? 0 : Integer.parseInt(buyLimit));
			itemList.put(item.getID(), item);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private int getItemPrice(String pageName) throws IOException {
		URLConnection spoof;
		URL url = new URL(pageName);
		spoof = url.openConnection();
		spoof.setRequestProperty( "User-Agent", "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0;    H010818)" );
		BufferedReader in = new BufferedReader(new InputStreamReader(spoof.getInputStream()));
		String line = "";
		while ((line = in.readLine()) != null){
			if (line != null && line.length() > 3 && line.contains("<h3>Current Guide Price")){
				String price = line.substring(line.indexOf("title=") + 7, line.indexOf("\'>"));
				price = price.replace(",", "");
				//System.out.println(price);
				return Integer.parseInt(price);
			}
		}
		return -1;
	}

	private void handleSelectedItem() {
		if (listItems.getSelectedIndex() < 0) {
			itemId = -1;
			lblItemName.setText("");
			lblItemInfo.setText("");
			spinnerCurrentPrice.setValue(0);
			spinnerCurrentPrice.setEnabled(false);
			spinnerQuantity.setValue(1);
			spinnerQuantity.setEnabled(false);
			btnIncreasePrice.setEnabled(false);
			btnDecreasePrice.setEnabled(false);
			btnResetPrice.setEnabled(false);
			btnBuy.setEnabled(false);
			btnSell.setEnabled(false);
			chckbxWaitForInstant.setEnabled(false);
			chckbxWUC.setEnabled(false);
			return;
		}
		int id = listItems.getSelectedValue().getID();
		if (itemId != id) {
			itemId = id;
			getItemInfo(itemId);
			DisplayItem item = itemList.get(itemId);
			spinnerCurrentPrice.setValue(0);
			spinnerCurrentPrice.setEnabled(true);
			spinnerQuantity.setEnabled(true);
			btnIncreasePrice.setEnabled(true);
			btnDecreasePrice.setEnabled(true);
			btnResetPrice.setEnabled(true);
			btnBuy.setEnabled(true);
			btnSell.setEnabled(true);
			chckbxWaitForInstant.setEnabled(true);
			chckbxWUC.setEnabled(true);
			lblItemName.setText(item.getName());
			lblItemInfo.setText("");
			List<String> lines = new ArrayList<String>();
			lines.add("GE Price:   fetching price...");
			if (item.getLimit() > 0)
				lines.add("Buy Limit:  " + Utilities.insertCommas(item.getLimit()));
			else
				lines.add("Buy Limit:  ---");
			lines.add("Item ID:    " + item.getID());
			lines.add(item.getDescription());
			for (String line : lines) {
				lblItemInfo.append(line + "\n");
			}
			Thread thread = new Thread() {
				public void run() {
					int price;
					try {
						price = getItemPrice("https://secure.runescape.com/m=itemdb_oldschool/" + item.getName().replaceAll(" ", "+") + "/viewitem?obj=" + itemId);
						if (itemId == id) {
							item.setPrice(price);
							lblItemInfo.setText("");
							if (item.getPrice() == -1)
								lines.set(0, "GE Price:   failed to load price");
							else {
								lines.set(0, "GE Price:   " + Utilities.insertCommas(item.getPrice()) + " gp");
								if (Long.parseLong(spinnerCurrentPrice.getValue().toString()) == 1L)
									spinnerCurrentPrice.setValue(item.getPrice());
							}
							for (String line : lines) {
								lblItemInfo.append(line + "\n");
							}
						}
					} catch (IOException e) {
						lblItemInfo.setText("");
						lines.set(0, "GE Price:   failed to load price");
						for (String line : lines) {
							lblItemInfo.append(line + "\n");
						}
						e.printStackTrace();
					}
				}
			};
			thread.start();
			itemImage.repaint();
		}
	}
	
	private void updateOfferList() {
		if (modelOffers.getSize() > 0) {
			btnStart.setEnabled(true);
		} else {
			btnStart.setEnabled(false);
		}
		int index = listOffers.getSelectedIndex();
		if (index != -1) {
			if (index == 0)
				btnMoveUp.setEnabled(false);
			else 
				btnMoveUp.setEnabled(true);
			if (index == modelOffers.size() - 1)
				btnMoveDown.setEnabled(false);
			else
				btnMoveDown.setEnabled(true);	
			btnRemove.setEnabled(true);
		} else {
			btnMoveDown.setEnabled(false);
			btnMoveUp.setEnabled(false);
			btnRemove.setEnabled(false);
		}
	}
	
	private void updateList(String itemName) {
		modelItems.clear();
		for (DisplayItem item : itemList.values()) {
			if (item.getName().toLowerCase().contains(itemName.toLowerCase()) && (chckbxF2P.isSelected() && !item.isMembers() || !chckbxF2P.isSelected()))
				modelItems.addElement(item);
		}
		if (modelItems.size() > 0) {
			if (spriteThread == null || !spriteThread.isAlive()) {
				spriteThread = new Thread("Updating") {
					public void run() {
						while (System.currentTimeMillis() - typeDelay < 500);
						loadSprites();
					}
				};
				spriteThread.start();
			}
		}
	}
	
	private int calculatePrice(int price, boolean increase) {
		double offset = 0.95;
		long difference = (long) (price - price * offset);
		if (difference == 0)
			difference = 1;
		long newPrice = price + (increase ? difference : -1 * difference);
		if (newPrice > Integer.MAX_VALUE)
			newPrice = Integer.MAX_VALUE;
		else if (newPrice < 0)
			newPrice = 0;
		return (int) newPrice;
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
	
	public boolean isFinished() {
		return isFinished;
	}
	
	public ArrayList<OfferItem> getOfferItems() {
		return Collections.list(modelOffers.elements());
	}

	public class ItemImageRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;
		Font font = new Font("helvitica", Font.BOLD, 12);

        @SuppressWarnings("rawtypes")
		@Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        	JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			DisplayItem item = ((DisplayItem) value);
        	ImageIcon itemIcon = item.getSprite();
			if (itemIcon != null && membersIcon != null && f2pIcon != null) {
				ImageIcon starIcon = item.isMembers() ? membersIcon : f2pIcon;
				itemIcon = Utilities.mergeIcons(itemIcon, starIcon, new Point(itemIcon.getIconWidth() - starIcon.getIconWidth(), itemIcon.getIconHeight() - starIcon.getIconHeight()));
			}
			if (itemIcon == null) {//Width:36 Height:32
				itemIcon = new ImageIcon(new BufferedImage(36, 32, BufferedImage.TYPE_INT_ARGB));
			}
			
            label.setIcon(itemIcon);
            label.setHorizontalTextPosition(JLabel.RIGHT);
            label.setFont(font);
            return label;
        }
    }
	
	public class OfferItem {
		private DisplayItem item;
		private long price;
		private long quantity;
		private boolean isBuying;
		private List<Integer> increments = new ArrayList<Integer>();
		private boolean wuc;
		private int priceChanges;
		
		public OfferItem(DisplayItem item, long price, long quantity, boolean isBuying, List<Integer> increments, boolean wuc, int priceChanges) {
			this.setItem(item);
			this.setPrice(price);
			this.setQuantity(quantity);
			this.setBuying(isBuying);
			this.setIncrements(increments);
			this.setWaitUntilCompleted(wuc);
			this.setPriceChanges(priceChanges);
		}

		public DisplayItem getItem() {
			return item;
		}

		public void setItem(DisplayItem item) {
			this.item = item;
		}

		public long getPrice() {
			return price;
		}

		public void setPrice(long price) {
			this.price = price;
		}

		public long getQuantity() {
			return quantity;
		}

		public void setQuantity(long quantity) {
			this.quantity = quantity;
		}

		public boolean isBuying() {
			return isBuying;
		}

		public void setBuying(boolean isBuying) {
			this.isBuying = isBuying;
		}

		public List<Integer> getIncrements() {
			return increments;
		}

		public void setIncrements(List<Integer> increments) {
			this.increments = increments;
		}

		public boolean isWaitUntilCompleted() {
			return wuc;
		}

		public void setWaitUntilCompleted(boolean wuc) {
			this.wuc = wuc;
		}

		public int getPriceChanges() {
			return priceChanges;
		}

		public void setPriceChanges(int priceChanges) {
			this.priceChanges = priceChanges;
		}
		
		@Override
		public String toString() {
			return (isBuying ? "Buying " : "Selling ") + Utilities.insertCommas(quantity) + " " + 
					item.getName() + (quantity > 1 ? "'s" : "") + " | " + Utilities.insertCommas(price) + 
					" gp each | " + Utilities.insertCommas(quantity * price) + " gp total | wait to " + 
					(isBuying ? "buy" : "sell") + ": " + (wuc ? "yes" : "no") + 
					(priceChanges > -1 ? " | " + priceChanges + " extra attempt" + (priceChanges > 1 ? "s" : "") + " for offer" : "");
		}
	}
	
	public class DisplayItem {
		private int id;
		private String name;
		private boolean members;
		private int price;
		private String description;
		private int limit;
		private ImageIcon sprite;
	    
		public DisplayItem(int id, String name, boolean members) {
			this.id = id;
			this.name = name;
			this.members = members;
			price = -1;
			sprite = null;
		}

	    public int getID() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isMembers() {
			return members;
		}

		public void setMembers(boolean members) {
			this.members = members;
		}

		public int getPrice() {
			return price;
		}

		public void setPrice(int price) {
			this.price = price;
		}
		
		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
		
		public int getLimit() {
			return limit;
		}

		public void setLimit(int limit) {
			this.limit = limit;
		}
		
		public ImageIcon getSprite() {
			return sprite;
		}
		
		public void setSprite(ImageIcon sprite) {
			this.sprite = sprite;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
}
