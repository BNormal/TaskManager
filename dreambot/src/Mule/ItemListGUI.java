package Mule;

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.dreambot.api.wrappers.items.Item;

import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.UIManager;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.border.EtchedBorder;

public class ItemListGUI extends JFrame {

	private static final long serialVersionUID = 3529106012684036718L;
	private JPanel contentPane;
	private JTextField txtName;
	private DefaultListModel<String> modelItems;
	private Mule mule;
	private ArrayList<Item> itemList = new ArrayList<Item>();
	private int itemId = -1;
	private JPanel itemImage;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ItemListGUI frame = new ItemListGUI(null, null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void generateList() {
		for (int i = 0; i < 24287; i ++) {
			itemList.add(new Item(i, 0, mule.getClient().getInstance()));
		}
	}
	
	/**
	 * Create the frame.
	 */
	public ItemListGUI(Mule mule, GUI gui) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		this.mule = mule;
		if (mule != null)
			generateList();
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setLocationRelativeTo(null);
		setBounds(100, 100, 304, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		txtName = new JTextField();
		txtName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				String itemName = txtName.getText();
				if (itemName != null && !itemName.equals("") && itemName.length() > 2) {
					updateList(itemName);
				} else {
					modelItems.clear();
					itemImage.repaint();
				}
			}
		});
		txtName.setBounds(78, 9, 160, 20);
		contentPane.add(txtName);
		txtName.setColumns(10);
		
		JLabel lblItemName = new JLabel("Item Name:");
		lblItemName.setBounds(10, 12, 62, 14);
		contentPane.add(lblItemName);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 40, 268, 184);
		contentPane.add(scrollPane);
		modelItems = new DefaultListModel<>();
		JList<String> listItems = new JList<String>(modelItems);
		listItems.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if (listItems.getSelectedIndex() < 0) {
					itemId = -1;
					return;
				}
				String text[] = listItems.getSelectedValue().split(" ");
				itemId = Integer.parseInt(text[text.length - 1]);
				itemImage.repaint();
				
			}
		});
		
		scrollPane.setViewportView(listItems);
		
		JButton btnSelect = new JButton("Select Item");
		btnSelect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (itemId < 0)
					return;
				if (gui != null) {
					gui.setItemId(itemId);
					setVisible(false);
				}
			}
		});
		btnSelect.setBounds(10, 233, 268, 23);
		contentPane.add(btnSelect);
		
		itemImage = new JPanel() {
			private static final long serialVersionUID = -4793964038601761016L;

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Image sprite = null;
				try {
					if (itemId > -1)
						sprite = ImageIO.read(new URL("https://www.osrsbox.com/osrsbox-db/items-icons/" + itemId + ".png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (sprite != null)
					g.drawImage(sprite, 0, 0, null);
			}
		};
		itemImage.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		itemImage.setBounds(242, 4, 36, 32);
		contentPane.add(itemImage);
	}
	
	public void updateList(String itemName) {
		modelItems.clear();
		if (mule == null) {
			for (int i = 0; i < 10; i++) {
				modelItems.addElement(" - " + i);
			}
		} else {
			for (Item item : itemList) {
				if (item.getName().toLowerCase().contains(itemName.toLowerCase()))
					modelItems.addElement(item.getName() + (item.isNoted() ? " (Noted)" : "") + (item.isStackable() ? "(Stackable)" : "") + " - " + item.getID());
			}
		}
	}
}
