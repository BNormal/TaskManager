package TaskManager.scripts.mining;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import java.awt.event.ActionListener;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.awt.event.ActionEvent;

import TaskManager.scripts.mining.Miner.MiningSpot;
import TaskManager.scripts.mining.MinerData.OreNode;
import TaskManager.scripts.mining.MinerData.Pickaxe;


public class MinerGUI {

	private transient JFrame frameMiner;
	private boolean isFinished = false;
	private String title;
	private DefaultListModel<Pickaxe> modelDisallowed = new DefaultListModel<Pickaxe>();
	private DefaultListModel<Pickaxe> modelAllowed = new DefaultListModel<Pickaxe>();
	private transient JComboBox<MiningSpot> cbxLocation;
	private transient JComboBox<OreNode> cbxOreNode;
	private DefaultComboBoxModel<OreNode> modelOreNode = new DefaultComboBoxModel<OreNode>();
	private JCheckBox chckbxPowermine;
	

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
					MinerGUI window = new MinerGUI("Test");
					window.frameMiner.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MinerGUI(String title) {
		this.title = title;
		modelAllowed.add(0, Pickaxe.BRONZE_PICKAXE);
		modelAllowed.add(0, Pickaxe.IRON_PICKAXE);
		modelAllowed.add(0, Pickaxe.STEEL_PICKAXE);
		modelAllowed.add(0, Pickaxe.BLACK_PICKAXE);
		modelAllowed.add(0, Pickaxe.MITHRIL_PICKAXE);
		modelAllowed.add(0, Pickaxe.ADAMANT_PICKAXE);
		modelAllowed.add(0, Pickaxe.RUNE_PICKAXE);
		modelAllowed.add(0, Pickaxe.GILDED_PICKAXE);
		modelAllowed.add(0, Pickaxe.DRAGON_PICKAXE);
		modelDisallowed.add(0, Pickaxe.THIRD_AGE_PICKAXE);
		modelDisallowed.add(0, Pickaxe.INFERNAL_PICKAXE);
		modelDisallowed.add(0, Pickaxe.CRYSTAL_PICKAXE);
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frameMiner = new JFrame();
		frameMiner.setTitle(title);
		frameMiner.setBounds(100, 100, 280, 290);
		frameMiner.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frameMiner.setLocationRelativeTo(null);
		frameMiner.getContentPane().setLayout(null);
		frameMiner.setResizable(false);
		
		cbxLocation = new JComboBox<MiningSpot>(MiningSpot.values());//remove this MiningSpot.values() to use in window builder
		cbxLocation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateOre();
			}
		});
		cbxLocation.setFocusable(false);
		cbxLocation.setBounds(61, 11, 193, 20);
		frameMiner.getContentPane().add(cbxLocation);
		
		JLabel lblLocation = new JLabel("Location:");
		lblLocation.setBounds(10, 14, 53, 14);
		frameMiner.getContentPane().add(lblLocation);
		
		JLabel lblOre = new JLabel("Ore:");
		lblOre.setBounds(10, 45, 53, 14);
		frameMiner.getContentPane().add(lblOre);
		
		cbxOreNode = new JComboBox<OreNode>();
		cbxOreNode.setFocusable(false);
		cbxOreNode.setBounds(61, 42, 115, 20);
		cbxOreNode.setModel(modelOreNode);
		frameMiner.getContentPane().add(cbxOreNode);
		
		JScrollPane scrollAllow = new JScrollPane();
		scrollAllow.setBorder(new LineBorder(Color.DARK_GRAY, 1, true));
		scrollAllow.setBounds(10, 100, 105, 105);
		frameMiner.getContentPane().add(scrollAllow);
		
		JList<Pickaxe> listAllow = new JList<Pickaxe>();
		listAllow.setModel(modelAllowed);
		scrollAllow.setViewportView(listAllow);
		
		JScrollPane scrollDisallow = new JScrollPane();
		scrollDisallow.setBorder(new LineBorder(Color.DARK_GRAY, 1, true));
		scrollDisallow.setBounds(149, 100, 105, 105);
		frameMiner.getContentPane().add(scrollDisallow);
		
		JList<Pickaxe> listDisallow = new JList<Pickaxe>();
		listDisallow.setModel(modelDisallowed);
		scrollDisallow.setColumnHeaderView(listDisallow);
		
		JButton btnLeft = new JButton("<");
		btnLeft.setFocusable(false);
		btnLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int index = listDisallow.getSelectedIndex();
				if (index < 0)
					return;
				modelAllowed.add(0, modelDisallowed.get(index));
				modelDisallowed.removeElement(modelDisallowed.get(index));
			}
		});
		btnLeft.setMargin(new Insets(0, 0, 0, 0));
		btnLeft.setBounds(118, 123, 27, 23);
		frameMiner.getContentPane().add(btnLeft);
		
		JButton btnRight = new JButton(">");
		btnRight.setFocusable(false);
		btnRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = listAllow.getSelectedIndex();
				if (index < 0)
					return;
				modelDisallowed.add(0, modelAllowed.get(index));
				modelAllowed.removeElement(modelAllowed.get(index));
			}
		});
		btnRight.setMargin(new Insets(0, 0, 0, 0));
		btnRight.setBounds(118, 154, 27, 23);
		frameMiner.getContentPane().add(btnRight);
		
		JLabel lblAllowed = new JLabel("Use");
		lblAllowed.setHorizontalAlignment(SwingConstants.CENTER);
		lblAllowed.setBounds(10, 83, 93, 14);
		frameMiner.getContentPane().add(lblAllowed);
		
		JLabel lblDisallowed = new JLabel("Don't Use");
		lblDisallowed.setHorizontalAlignment(SwingConstants.CENTER);
		lblDisallowed.setBounds(161, 83, 93, 14);
		frameMiner.getContentPane().add(lblDisallowed);
		
		JLabel lblPickaxe = new JLabel("Pickaxes");
		lblPickaxe.setHorizontalAlignment(SwingConstants.CENTER);
		lblPickaxe.setBounds(10, 70, 244, 14);
		frameMiner.getContentPane().add(lblPickaxe);
		
		JButton btnFinished = new JButton("Finished");
		btnFinished.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frameMiner.setVisible(false);
				isFinished = true;
			}
		});
		btnFinished.setBounds(10, 216, 244, 24);
		frameMiner.getContentPane().add(btnFinished);
		
		chckbxPowermine = new JCheckBox("Powermine");
		chckbxPowermine.setFocusable(false);
		chckbxPowermine.setBounds(182, 42, 80, 20);
		frameMiner.getContentPane().add(chckbxPowermine);
		
		updateOre();
	}

	public void updateOre() {
		if (cbxLocation.getSelectedIndex() < 0)
			return;
		OreNode[] nodes = ((MiningSpot) cbxLocation.getSelectedItem()).getRockNodes();
		modelOreNode.removeAllElements();
		for (int i = 0; i < nodes.length; i++) {
			modelOreNode.addElement(nodes[i]);
		}
	}
	
	public MiningSpot getMiningArea() {
		if (cbxLocation.getSelectedIndex() < 0)
			return null;
		return ((MiningSpot) cbxLocation.getSelectedItem());
	}
	
	public OreNode getOreNode() {
		if (cbxLocation.getSelectedIndex() < 0)
			return null;
		return ((OreNode) cbxOreNode.getSelectedItem());
	}

	class SortByLevel implements Comparator<Pickaxe> 
	{
		@Override
		public int compare(Pickaxe a, Pickaxe b) {
			return b.getPriority() - a.getPriority();
		} 
	}
	
	public List<Pickaxe> getDisallowedPickaxes() {
		List<Pickaxe> pickaxes = new ArrayList<Pickaxe>();
		for (int i = 0; i < modelDisallowed.size(); i++) {
			pickaxes.add(modelDisallowed.get(i));
		}
		Collections.sort(pickaxes, new SortByLevel());
		return pickaxes;
	}

	public List<Pickaxe> getAllowedPickaxes() {
		List<Pickaxe> pickaxes = new ArrayList<Pickaxe>();
		for (int i = 0; i < modelAllowed.size(); i++) {
			pickaxes.add(modelAllowed.get(i));
		}
		Collections.sort(pickaxes, new SortByLevel());
		return pickaxes;
	}

	public boolean isPowerMining() {
		return chckbxPowermine.isSelected();
	}
	
	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}
	
	public void open() {
		frameMiner.setVisible(true);
	}
	
	public void close() {
		frameMiner.setVisible(false);
	}

	public void exit() {
		frameMiner.setVisible(false);
		frameMiner.dispose();
	}
	
	public String getSaveDate() {
		Gson gson = new GsonBuilder().create();
		List<String> settings = new ArrayList<String>();
		settings.add(cbxLocation.getSelectedIndex() + "");
		settings.add(cbxOreNode.getSelectedIndex() + "");
		settings.add(chckbxPowermine.isSelected() + "");
		settings.add(gson.toJson(modelDisallowed));
		settings.add(gson.toJson(modelAllowed));
		return gson.toJson(settings);
	}

	public void loadSaveDate(String json) {
		Gson gson = new Gson();
		List<String> settings = new ArrayList<String>();
		Type type = new TypeToken<List<String>>() {}.getType();
		settings = gson.fromJson(json, type);
		cbxLocation.setSelectedIndex(Integer.parseInt(settings.get(0)));
		cbxOreNode.setSelectedIndex(Integer.parseInt(settings.get(1)));
		chckbxPowermine.setSelected(settings.get(2).equalsIgnoreCase("true"));
		Type type2 = new TypeToken<DefaultListModel<Pickaxe>>() {}.getType();
		modelDisallowed = gson.fromJson(settings.get(3), type2);
		modelAllowed = gson.fromJson(settings.get(4), type2);
	}
	
	public String getSettingsDetails() {
		String settings = "Location: " + cbxLocation.getSelectedItem() + 
				"\nTree: " + cbxOreNode.getSelectedItem() + 
				"\nPower Cuttings: " + (chckbxPowermine.isSelected() ? "Yes" : "No") +
				"\nAllowed Pickaxes: ";
		for (int i = 0; i < modelAllowed.size(); i++) {
			settings += "\n* " + modelAllowed.get(i);
		}
		return settings;
	}
}
