package TaskManager.scripts.woodcutting;

import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import TaskManager.scripts.woodcutting.Woodcutter.WoodcuttingSpot;
import TaskManager.scripts.woodcutting.WoodcutterData.Axe;
import TaskManager.scripts.woodcutting.WoodcutterData.Tree;
import javax.swing.JCheckBox;

public class WoodcutterGUI {

	private JFrame frameWoodcutter;
	private boolean isFinished = false;
	private DefaultListModel<Axe> modelDisallowed = new DefaultListModel<Axe>();
	private DefaultListModel<Axe> modelAllowed = new DefaultListModel<Axe>();
	private JComboBox<WoodcuttingSpot> cbxLocation;
	private JComboBox<Tree> cbxTree;
	private DefaultComboBoxModel<Tree> modelTree = new DefaultComboBoxModel<Tree>();
	private JCheckBox chckbxPowercut;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WoodcutterGUI window = new WoodcutterGUI("TEST");
					window.frameWoodcutter.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public WoodcutterGUI(String title) {
		modelAllowed.add(0, Axe.BRONZE_AXE);
		modelAllowed.add(0, Axe.IRON_AXE);
		modelAllowed.add(0, Axe.STEEL_AXE);
		modelAllowed.add(0, Axe.BLACK_AXE);
		modelAllowed.add(0, Axe.MITHRIL_AXE);
		modelAllowed.add(0, Axe.ADAMANT_AXE);
		modelAllowed.add(0, Axe.RUNE_AXE);
		modelAllowed.add(0, Axe.GILDED_AXE);
		modelAllowed.add(0, Axe.DRAGON_AXE);
		modelDisallowed.add(0, Axe.THIRD_AGE_AXE);
		modelDisallowed.add(0, Axe.INFERNAL_AXE);
		modelDisallowed.add(0, Axe.CRYSTAL_AXE);
		initialize(title);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(String title) {
		frameWoodcutter = new JFrame();
		frameWoodcutter.setTitle(title);
		frameWoodcutter.setBounds(100, 100, 280, 290);
		frameWoodcutter.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frameWoodcutter.getContentPane().setLayout(null);
		frameWoodcutter.setResizable(false);
		
		cbxLocation = new JComboBox<WoodcuttingSpot>(WoodcuttingSpot.values());//remove this WoodcuttingSpot.values() to use in window builder
		cbxLocation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateLog();
			}
		});
		cbxLocation.setFocusable(false);
		cbxLocation.setBounds(61, 11, 193, 20);
		frameWoodcutter.getContentPane().add(cbxLocation);
		
		JLabel lblLocation = new JLabel("Location:");
		lblLocation.setBounds(10, 14, 53, 14);
		frameWoodcutter.getContentPane().add(lblLocation);
		
		JLabel lblLog = new JLabel("Tree:");
		lblLog.setBounds(10, 45, 53, 14);
		frameWoodcutter.getContentPane().add(lblLog);
		
		cbxTree = new JComboBox<Tree>();
		cbxTree.setFocusable(false);
		cbxTree.setBounds(61, 42, 115, 20);
		cbxTree.setModel(modelTree);
		frameWoodcutter.getContentPane().add(cbxTree);
		
		JScrollPane scrollAllow = new JScrollPane();
		scrollAllow.setBounds(10, 100, 105, 105);
		frameWoodcutter.getContentPane().add(scrollAllow);
		
		JList<Axe> listAllow = new JList<Axe>();
		listAllow.setModel(modelAllowed);
		scrollAllow.setViewportView(listAllow);
		
		JScrollPane scrollDisallow = new JScrollPane();
		scrollDisallow.setBounds(149, 100, 105, 105);
		frameWoodcutter.getContentPane().add(scrollDisallow);
		
		JList<Axe> listDisallow = new JList<Axe>();
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
		frameWoodcutter.getContentPane().add(btnLeft);
		
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
		frameWoodcutter.getContentPane().add(btnRight);
		
		JLabel lblAllowed = new JLabel("Use");
		lblAllowed.setHorizontalAlignment(SwingConstants.CENTER);
		lblAllowed.setBounds(10, 83, 93, 14);
		frameWoodcutter.getContentPane().add(lblAllowed);
		
		JLabel lblDisallowed = new JLabel("Don't Use");
		lblDisallowed.setHorizontalAlignment(SwingConstants.CENTER);
		lblDisallowed.setBounds(161, 83, 93, 14);
		frameWoodcutter.getContentPane().add(lblDisallowed);
		
		JLabel lblAxe = new JLabel("Axes");
		lblAxe.setHorizontalAlignment(SwingConstants.CENTER);
		lblAxe.setBounds(10, 70, 244, 14);
		frameWoodcutter.getContentPane().add(lblAxe);
		
		JButton btnFinished = new JButton("Finished");
		btnFinished.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frameWoodcutter.setVisible(false);
				isFinished = true;
			}
		});
		btnFinished.setBounds(10, 216, 244, 24);
		frameWoodcutter.getContentPane().add(btnFinished);
		
		chckbxPowercut = new JCheckBox("Powercut");
		chckbxPowercut.setFocusable(false);
		chckbxPowercut.setBounds(182, 42, 80, 20);
		frameWoodcutter.getContentPane().add(chckbxPowercut);
		updateLog();
	}

	public void updateLog() {
		if (cbxLocation.getSelectedIndex() < 0)
			return;
		Tree[] trees = ((WoodcuttingSpot) cbxLocation.getSelectedItem()).getTrees();
		modelTree.removeAllElements();
		for (int i = 0; i < trees.length; i++) {
			modelTree.addElement(trees[i]);
		}
	}
	
	public WoodcuttingSpot getWoodcuttingArea() {
		if (cbxLocation.getSelectedIndex() < 0)
			return null;
		return ((WoodcuttingSpot) cbxLocation.getSelectedItem());
	}
	
	public Tree getTree() {
		if (cbxLocation.getSelectedIndex() < 0)
			return null;
		return ((Tree) cbxTree.getSelectedItem());
	}

	class SortByLevel implements Comparator<Axe> 
	{
		@Override
		public int compare(Axe a, Axe b) {
			return b.getPriority() - a.getPriority();
		} 
	}
	
	public List<Axe> getDisallowedAxes() {
		List<Axe> axes = new ArrayList<Axe>();
		for (int i = 0; i < modelDisallowed.size(); i++) {
			axes.add(modelDisallowed.get(i));
		}
		Collections.sort(axes, new SortByLevel());
		return axes;
	}

	public List<Axe> getAllowedAxes() {
		List<Axe> Axes = new ArrayList<Axe>();
		for (int i = 0; i < modelAllowed.size(); i++) {
			Axes.add(modelAllowed.get(i));
		}
		Collections.sort(Axes, new SortByLevel());
		return Axes;
	}
	
	public boolean isPowerCutting() {
		return chckbxPowercut.isSelected();
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}
	
	public void open() {
		frameWoodcutter.setVisible(true);
	}
	
	public void close() {
		frameWoodcutter.setVisible(false);
	}

	public void exit() {
		frameWoodcutter.setVisible(false);
		frameWoodcutter.dispose();
	}

}
