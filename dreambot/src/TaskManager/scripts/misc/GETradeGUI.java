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
import javax.swing.JTextArea;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JList;
import javax.swing.JCheckBox;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import TaskManager.utilities.Utilities;

public class GETradeGUI {

	private JFrame frame;
	private Map<Integer, DisplayItem> itemList = new HashMap<Integer, DisplayItem>();
	private JTextField txtItemName;
	private JList<DisplayItem> listItems;
	private DefaultListModel<DisplayItem> modelItems;
	private int itemId = -1;
	private JPanel itemImage;
	private JLabel lblItemName;
	private JTextArea lblItemInfo;
	private JCheckBox chckbxF2P;
	private ImageIcon f2pIcon;
	private ImageIcon membersIcon;
	private Thread spriteThread;
	private long typeDelay = 0;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GETradeGUI window = new GETradeGUI("TEST");
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
	public GETradeGUI(String title) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		initialize(title);
	}

	public void loadSprites() {
		String text = txtItemName.getText();
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
				break;
		}
		//System.out.println(listItems.getFirstVisibleIndex() + ":" + listItems.getLastVisibleIndex());
	}
	
	public void getItemListData() {
		try {
			Gson gson = new Gson();
			BufferedReader br = new BufferedReader(new FileReader("GEItems.json"));
			Type type = new TypeToken<Map<Integer, DisplayItem>>() {}.getType();
			Map<Integer, DisplayItem> items = gson.fromJson(br, type);
			itemList = items;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		/*try {
			Gson gson = new Gson();
			Type type = new TypeToken<HashMap<String, JsonElement>>() {}.getType();
			Map<String, JsonElement> map = gson.fromJson(readUrl("https://rsbuddy.com/exchange/summary.json"), type);
			for (Entry<String, JsonElement> entry : map.entrySet()) {
				JsonObject value = (JsonObject) entry.getValue();
				String name = value.get("name").toString();
				name = name.substring(1, name.length() - 1);
				DisplayItem item = new DisplayItem(new Integer(value.get("id").toString()), name, value.get("members").toString().equals("true"));
				itemList.put(item.getID(), item);
			}
			File file = new File("GEItems.json");
			try {
				FileWriter fw = new FileWriter(file);
				gson.toJson(itemList, fw);
				fw.close();
				//System.out.println("done");
			} catch (JsonIOException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}
	
	public void getItemInfo(int id) {
		try {
			Gson gson = new Gson();
			JsonElement json = gson.fromJson(readUrl("https://www.osrsbox.com/osrsbox-db/items-json/" + id + ".json"), JsonElement.class);
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
	
	public int getItemPrice(String pageName) {
		try {
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
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(String title) {
		frame = new JFrame();
		frame.setTitle(title);
		frame.setBounds(100, 100, 500, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		txtItemName = new JTextField();
		txtItemName.setEnabled(false);
		txtItemName.setColumns(10);
		txtItemName.setBounds(314, 16, 160, 20);
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
		lblItemName.setBounds(10, 11, 172, 20);
		frame.getContentPane().add(lblItemName);
		
		JLabel lblName = new JLabel("Item Name:");
		lblName.setBounds(242, 19, 62, 14);
		frame.getContentPane().add(lblName);
		
		JScrollPane scrollPane = new JScrollPane();
		
		modelItems = new DefaultListModel<>();
		
		listItems = new JList<DisplayItem>(modelItems);
		listItems.setCellRenderer(new ItemImageRenderer());
		listItems.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if (listItems.getSelectedIndex() < 0) {
					itemId = -1;
					lblItemName.setText("");
					lblItemInfo.setText("");
					return;
				}
				int id = listItems.getSelectedValue().getID();
				if (itemId != id) {
					itemId = id;
					getItemInfo(itemId);
					DisplayItem item = itemList.get(itemId);
					lblItemName.setText(item.getName());
					lblItemInfo.setText("");
					lblItemInfo.append(
						"------------------------------------------" +
						"\nGE Price:   fetching price..." +
						"\nBuy Limit:  " + Utilities.insertCommas(item.getLimit()) + 
						"\nItem ID:    " + item.getID() +
						"\n" + item.getDescription()
					);
					Thread thread = new Thread() {
						public void run() {
							int price = getItemPrice("https://secure.runescape.com/m=itemdb_oldschool/" + item.getName().replaceAll(" ", "+") + "/viewitem?obj=" + itemId);
							if (itemId == id) {
								item.setPrice(price);
								lblItemInfo.setText("");
								lblItemInfo.append(
									"------------------------------------------" + 
									"\nGE Price:   " + Utilities.insertCommas(item.getPrice()) + 
									" gp\nBuy Limit:  " + Utilities.insertCommas(item.getLimit()) + 
									"\nItem ID:    " + item.getID() +
									"\n" + item.getDescription()
								);
							}
						}
					};
					thread.start();
					itemImage.repaint();
				}
			}
		});
		
		scrollPane.setViewportView(listItems);
		scrollPane.setBounds(242, 70, 232, 161);
		frame.getContentPane().add(scrollPane);
		
		itemImage = new JPanel() {
			private static final long serialVersionUID = -4793964038601761016L;

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
		itemImage.setBounds(192, 7, 40, 36);
		frame.getContentPane().add(itemImage);
		
		chckbxF2P = new JCheckBox("Display F2P items only");
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
		chckbxF2P.setBounds(238, 40, 138, 23);
		frame.getContentPane().add(chckbxF2P);
		
		JLabel lblLoading = new JLabel("Loading data...");
		lblLoading.setBounds(10, 242, 464, 14);
		frame.getContentPane().add(lblLoading);
		
		lblItemInfo = new JTextArea("");
		lblItemInfo.setLineWrap(true);
		lblItemInfo.setOpaque(false);
		lblItemInfo.setEditable(false);
		lblItemInfo.setBounds(10, 32, 222, 102);
		lblItemInfo.setFont(new Font("Tahoma", Font.PLAIN, 11));
		frame.getContentPane().add(lblItemInfo);
		Thread thread = new Thread() {
			public void run() {
				try {
					f2pIcon = new ImageIcon(new URL("https://static.wikia.nocookie.net/2007scape/images/6/6f/Free-to-play_icon.png"));
					membersIcon = new ImageIcon(new URL("https://static.wikia.nocookie.net/2007scape/images/9/9c/Member_icon.png"));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				getItemListData();
				chckbxF2P.setEnabled(true);
				txtItemName.setEnabled(true);
				lblLoading.setVisible(false);
			}
		};
		thread.start();
	}
	
	public void updateList(String itemName) {
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
	
	private static String readUrl(String urlString) throws Exception {
		URL url = new URL(urlString);
	    BufferedReader reader = null;
	    try {
	        reader = new BufferedReader(new InputStreamReader(url.openStream()));
	        StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1) {
	        	buffer.append(chars, 0, read);
	        }
	        return buffer.toString();
	    } finally {
	        if (reader != null)
	            reader.close();
	    }
	}
	
	public class ItemImageRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;
		Font font = new Font("helvitica", Font.BOLD, 12);

        @SuppressWarnings("rawtypes")
		@Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        	JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			DisplayItem item = ((DisplayItem) value);
        	ImageIcon icon = item.getSprite();
			if (icon != null && membersIcon != null && f2pIcon != null)
				icon = mergeIcons(icon, item.isMembers() ? membersIcon : f2pIcon);
			if (icon == null) {//Width:36 Height:32
				icon = new ImageIcon(new BufferedImage(36, 32, BufferedImage.TYPE_INT_ARGB));
			}
			
            label.setIcon(icon);
            label.setHorizontalTextPosition(JLabel.RIGHT);
            label.setFont(font);
            return label;
        }
    }
	
	public ImageIcon mergeIcons(ImageIcon icon1, ImageIcon icon2) {
        // For simplicity we will presume the images are of identical size
        final BufferedImage combinedImage = new BufferedImage(icon1.getIconWidth(), icon1.getIconHeight(), BufferedImage.TYPE_INT_ARGB );
        Graphics2D g = combinedImage.createGraphics();
        g.drawImage(icon1.getImage(), 0, 0, null);
        g.drawImage(icon2.getImage(), icon1.getIconWidth() - icon2.getIconWidth(), icon1.getIconHeight() - icon2.getIconHeight(), null);
        g.dispose();
        return new ImageIcon(combinedImage);
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
