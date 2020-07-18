package TaskManager.scripts.mining;

import java.awt.EventQueue;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import TaskManager.scripts.mining.Miner.MiningSpot;
import TaskManager.scripts.mining.MinerData.OreNode;
import TaskManager.scripts.mining.MinerData.Pickaxe;


public class MinerGUI {

	private JFrame frameMiner;
	private String title;
	private DefaultListModel<Pickaxe> modelDisallowed = new DefaultListModel<Pickaxe>();
	private DefaultListModel<Pickaxe> modelAllowed = new DefaultListModel<Pickaxe>();
	private JComboBox<MiningSpot> cbxLocation;
	private JComboBox<OreNode> cbxOreNode;
	private DefaultComboBoxModel<OreNode> modelOreNode = new DefaultComboBoxModel<OreNode>();
	

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
		frameMiner.getContentPane().setLayout(null);
		
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
		cbxOreNode.setBounds(61, 42, 193, 20);
		cbxOreNode.setModel(modelOreNode);
		frameMiner.getContentPane().add(cbxOreNode);
		
		JScrollPane scrollAllow = new JScrollPane();
		scrollAllow.setBounds(10, 100, 105, 105);
		frameMiner.getContentPane().add(scrollAllow);
		
		JList<Pickaxe> listAllow = new JList<Pickaxe>();
		listAllow.setModel(modelAllowed);
		scrollAllow.setViewportView(listAllow);
		
		JScrollPane scrollDisallow = new JScrollPane();
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
			}
		});
		btnFinished.setBounds(10, 216, 244, 24);
		frameMiner.getContentPane().add(btnFinished);
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
}
