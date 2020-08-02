package TaskManager.scripts.misc;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFrame;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JList;
import javax.swing.JCheckBox;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GETradeGUI {

	private JFrame frame;
	private Map<Integer, DisplayItem> itemList = new HashMap<Integer, DisplayItem>();
	private JTextField txtItemName;
	private DefaultListModel<DisplayItem> modelItems;
	private int itemId = -1;
	private JPanel itemImage;
	private JCheckBox chckbxF2P;

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
		parseJson();
		initialize(title);
	}

	public void parseJson() {
		try {
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
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		txtItemName.setColumns(10);
		txtItemName.setBounds(274, 16, 160, 20);
		txtItemName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				String itemName = txtItemName.getText();
				if (itemName != null && !itemName.equals("") && itemName.length() > 2) {
					updateList(itemName);
				} else {
					modelItems.clear();
					itemImage.repaint();
				}
			}
		});
		frame.getContentPane().add(txtItemName);
		
		JLabel lblItemName = new JLabel("Item Name:");
		lblItemName.setBounds(206, 19, 62, 14);
		frame.getContentPane().add(lblItemName);
		
		JScrollPane scrollPane = new JScrollPane();
		
		modelItems = new DefaultListModel<>();
		
		JList<DisplayItem> listItems = new JList<DisplayItem>(modelItems);
		listItems.setCellRenderer(new ItemImageRenderer());
		listItems.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if (listItems.getSelectedIndex() < 0) {
					itemId = -1;
					return;
				}
				itemId = listItems.getSelectedValue().getID();
				itemImage.repaint();
				
			}
		});
		
		scrollPane.setViewportView(listItems);
		scrollPane.setBounds(206, 70, 268, 161);
		frame.getContentPane().add(scrollPane);
		
		JButton btnSelect = new JButton("Select Item");
		btnSelect.setBounds(206, 240, 268, 23);
		frame.getContentPane().add(btnSelect);
		
		itemImage = new JPanel() {
			private static final long serialVersionUID = -4793964038601761016L;

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				BufferedImage sprite = null;
				try {
					if (itemId > -1) {
						sprite = ImageIO.read(new URL("https://www.osrsbox.com/osrsbox-db/items-icons/" + itemId + ".png"));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (sprite != null) {
					g.drawImage(sprite, (itemImage.getWidth() - sprite.getWidth()) / 2 + 1, (itemImage.getHeight() - sprite.getHeight()) / 2, null);
				}
			}
		};
		itemImage.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		itemImage.setBounds(438, 11, 40, 36);
		frame.getContentPane().add(itemImage);
		
		chckbxF2P = new JCheckBox("Display F2P items only");
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
		chckbxF2P.setBounds(206, 40, 138, 23);
		frame.getContentPane().add(chckbxF2P);
	}

	public void updateList(String itemName) {
		modelItems.clear();
		for (DisplayItem item : itemList.values()) {
			if (item.getName().toLowerCase().contains(itemName.toLowerCase()) && (chckbxF2P.isSelected() && !item.isMembers() || !chckbxF2P.isSelected()))
				modelItems.addElement(item);
		}
	}
	
	private static String readUrl(String urlString) throws Exception {
	    BufferedReader reader = null;
	    try {
	        URL url = new URL(urlString);
	        reader = new BufferedReader(new InputStreamReader(url.openStream()));
	        StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	            buffer.append(chars, 0, read); 

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
			ImageIcon icon = null;
			try {
				icon = new ImageIcon(new URL("https://www.osrsbox.com/osrsbox-db/items-icons/" + ((DisplayItem) value).getID() + ".png"));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
            label.setIcon(icon);
            label.setHorizontalTextPosition(JLabel.RIGHT);
            label.setFont(font);
            return label;
        }
    }
	
	public class DisplayItem {
		private int id;
		private String name;
		private boolean members;
	    
		public DisplayItem(int id, String name, boolean members) {
			this.id = id;
			this.name = name;
			this.members = members;
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
		
		@Override
		public String toString() {
			return name + " - " + id;
		}
	}
}
