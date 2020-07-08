package MobHunter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.NumberFormatter;

import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ActionEvent;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.border.EtchedBorder;
import javax.swing.JToggleButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class GUI {

	public JFrame frame;
	private MobSlayer ms;
	private File itemListFile;
	private JLabel lblItemName;
	private JCheckBox chckbxLoot;
	private JCheckBox chckbxIronman;
	private JList <String> listLootItems;
	private JList <String> listGroundItems;
	private DefaultListModel<String> defaultListModel;
	private DefaultListModel<String> defaultListGround;
	private JFormattedTextField txtItemID;
	private JToggleButton tglbtnFilter;
	private JLabel lblMobName;
	private JSpinner spnDistance;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		new GUI(null);
	}

	public JToggleButton getTglbtnFilter() {
		return tglbtnFilter;
	}

	public void setTglbtnFilter(JToggleButton tglbtnFilter) {
		this.tglbtnFilter = tglbtnFilter;
	}

	public int getDistance() {
		return (int) spnDistance.getValue();
	}

	public void setDistance(int value) {
		this.spnDistance.setValue(value);;
	}
	
	/**
	 * Create the application.
	 */
	public GUI(MobSlayer mobSlayer) {
		this.ms = mobSlayer;
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Throwable e) {
			e.printStackTrace();
		}
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setResizable(false);
        frame.setBounds(0, 0, 535, 351);
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.setUndecorated(true);
		frame.setBackground(new Color(1.0F, 1.0F, 1.0F, 0.00F));
        FrameDragListener frameDragListener = new FrameDragListener(frame);
        frame.addMouseListener(frameDragListener);
        frame.addMouseMotionListener(frameDragListener);

        frame.setLocationRelativeTo(null);
        frame.getContentPane().setLayout(new BorderLayout(0, 0));
		JPanel panel = new JPanel();
		/*try {
			Image bgImage = ImageIO.read(GUI.class.getResource("bg.png"));
			if (bgImage != null) {
				panel = new JPanel() {
					@Override
					protected void paintComponent(java.awt.Graphics g) {
						super.paintComponent(g);
						g.drawImage(bgImage, 0, 0, null);
					}
				};
			}
		} catch (IOException e) {
		}*/
        panel.setBackground(new Color(0.114f, 0.137f, 0.2f, 1.0f));//#1d2333 or 29, 35, 51 - dark navy blue
        frame.getContentPane().add(panel);
        panel.setLayout(null);
        
        
        tglbtnFilter = new JToggleButton("White-list");
        tglbtnFilter.setOpaque(false);
        tglbtnFilter.setForeground(new Color(0, 204, 255));
        tglbtnFilter.setContentAreaFilled(false);
        tglbtnFilter.setBorder(new LineBorder(new Color(0, 204, 255), 1, true));
        tglbtnFilter.setBounds(10, 90, 115, 20);
        tglbtnFilter.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if (tglbtnFilter.isSelected())
        			tglbtnFilter.setText("Black-list");
        		else
        			tglbtnFilter.setText("White-list");
			}
        });
        panel.add(tglbtnFilter);

        lblMobName = new JLabel("");
        lblMobName.setForeground(new Color(0, 204, 255));
        lblMobName.setBounds(297, 312, 222, 20);
        panel.add(lblMobName);
        
        chckbxIronman = new JCheckBox("Ironman");
        chckbxIronman.setSelected(true);
        chckbxIronman.setOpaque(false);
        chckbxIronman.setForeground(new Color(0, 204, 255));
        chckbxIronman.setFont(new Font("Tahoma", Font.PLAIN, 12));
        chckbxIronman.setBounds(137, 65, 127, 23);
        panel.add(chckbxIronman);
        
        if (ms != null)
        	loadDefaultSettings();
        
        chckbxLoot = new JCheckBox("Loot ground items");
        chckbxLoot.setFont(new Font("Tahoma", Font.PLAIN, 12));
        chckbxLoot.setForeground(new Color(0, 204, 255));
        chckbxLoot.setOpaque(false);
        chckbxLoot.setBounds(6, 65, 127, 23);
        chckbxLoot.setSelected(true);
        /*chckbxLoot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});*/
        panel.add(chckbxLoot);
        
        JLabel lblTitle = new JLabel("MobHunter");
        lblTitle.setHorizontalAlignment(SwingConstants.LEFT);
        lblTitle.setFont(new Font("The Wild Breath of Zelda", Font.ITALIC, 40));
        lblTitle.setForeground(new Color(0, 204, 255)); // 0, 204, 255 light neon blue
        lblTitle.setBounds(10, 11, 254, 47);
        panel.add(lblTitle);
        
        defaultListModel = new DefaultListModel<String>();

        JViewport viewport = new JViewport();
        viewport.setBorder(null);
        viewport.setOpaque(false);
        
        JScrollPane scrollLootItems = new JScrollPane();
        scrollLootItems.setBorder(null);
        scrollLootItems.setOpaque(false);
        scrollLootItems.setViewport(viewport);
        scrollLootItems.setBounds(10, 122, 254, 149);
        panel.add(scrollLootItems);
        
        listLootItems = new JList<String>(defaultListModel);
        listLootItems.setSelectionBackground(new Color(0, 204, 255));
        listLootItems.setSelectionForeground(new Color(29, 35, 51));
        listLootItems.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = -3371876270702444474L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setOpaque(isSelected);
				return this;
			}
		});
        listLootItems.setRequestFocusEnabled(false);
        listLootItems.setBorder(new LineBorder(new Color(0, 204, 255)));
        listLootItems.setOpaque(false);
        listLootItems.setFont(new Font("Tahoma", Font.BOLD, 11));
        listLootItems.setForeground(new Color(0, 204, 255));
        scrollLootItems.setViewportView(listLootItems);
        
        defaultListGround = new DefaultListModel<String>();
        
        JViewport viewportGround = new JViewport();
        viewportGround.setBorder(null);
        viewportGround.setOpaque(false);
        
        JScrollPane scrollGroundItems = new JScrollPane();
        scrollGroundItems.setOpaque(false);
        scrollGroundItems.setBorder(null);
        scrollGroundItems.setViewport(viewportGround);
        scrollGroundItems.setBounds(271, 122, 254, 149);
        panel.add(scrollGroundItems);
        
        listGroundItems = new JList<String>(defaultListGround);
        listGroundItems.setSelectionBackground(new Color(0, 204, 255));
        listGroundItems.setSelectionForeground(new Color(29, 35, 51));
        listGroundItems.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = -3371876270702444474L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setOpaque(isSelected);
				return this;
			}
		});
        listGroundItems.setRequestFocusEnabled(false);
        listGroundItems.setBorder(new LineBorder(new Color(0, 204, 255)));
        listGroundItems.setOpaque(false);
        listGroundItems.setFont(new Font("Tahoma", Font.BOLD, 11));
        listGroundItems.setForeground(new Color(0, 204, 255));
        scrollGroundItems.setViewportView(listGroundItems);
        
        if (itemListFile != null)
        	read(itemListFile);
        
        JLabel lblExit = new JLabel("X");
        lblExit.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mousePressed(MouseEvent arg0) {
        		lblExit.setForeground(new Color(181, 240, 255));
        		lblExit.setBorder(new LineBorder(new Color(181, 240, 255), 2, true));
        	}
        	
        	@Override
        	public void mouseReleased(MouseEvent arg0) {
        		lblExit.setForeground(new Color(0, 204, 255));
        		lblExit.setBorder(new LineBorder(new Color(0, 204, 255), 1, true));
        		if (ms != null) {
        			ms.getClient().getInstance().setKeyboardInputEnabled(false);
        			frame.setVisible(false);
        		} else {
        			System.exit(0);
        		}
        	}
        });

        lblExit.setBorder(new LineBorder(new Color(0, 204, 255), 1, true));
        lblExit.setFont(new Font("Arial", Font.BOLD, 12));
        lblExit.setHorizontalAlignment(SwingConstants.CENTER);
        lblExit.setForeground(new Color(0, 204, 255));
        lblExit.setBounds(499, 11, 20, 20);
        panel.add(lblExit);

        lblItemName = new JLabel("");
        lblItemName.setForeground(new Color(0, 204, 255));
        lblItemName.setBounds(174, 283, 135, 20);
        panel.add(lblItemName);
        
        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(false);
        NumberFormatter formatter = new NumberFormatter(format) {
			private static final long serialVersionUID = -8254510110172992562L;

			@Override
		    public void install(final JFormattedTextField ftf) {
		        int prevLen = ftf.getDocument().getLength();
		        int savedCaretPos = ftf.getCaretPosition();
		        super.install(ftf);
		        if (ftf.getDocument().getLength() == prevLen) {
		            ftf.setCaretPosition(savedCaretPos);
		        }
		    }
			
			@Override
        	public Object stringToValue(String string) throws ParseException {
                    if (string == null || string.length() == 0) {
                        return null;
                    }
                    return super.stringToValue(string);
                }
        };
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(0);
        formatter.setMaximum(Integer.MAX_VALUE);
        formatter.setAllowsInvalid(false);
        // If you want the value to be committed on each keystroke instead of focus lost
        formatter.setCommitsOnValidEdit(true);
        txtItemID = new JFormattedTextField(formatter);
        txtItemID.setHorizontalAlignment(SwingConstants.CENTER);
        txtItemID.setForeground(new Color(0, 204, 255));
        txtItemID.setBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(0, 0, 0), new Color(0, 204, 255)));
        txtItemID.setOpaque(false);
        txtItemID.setBounds(104, 282, 60, 23);
        panel.add(txtItemID);
        //txtItemID.setColumns(10);
        txtItemID.addKeyListener(new KeyListener() {

			@SuppressWarnings("unused")
			@Override
			public void keyReleased(KeyEvent arg0) {
				if (ms != null) {
					if (arg0.getKeyCode() == 10) {
						if (txtItemID.getText() != null)
							addItemToList(new Item(Integer.parseInt(txtItemID.getText()), 0, ms.getClient().getInstance()));
					} else {
						if (txtItemID.getText() != null && txtItemID.getText() != "") {
							Item item = new Item(Integer.parseInt(txtItemID.getText()), 0, ms.getClient().getInstance());
							if (item != null)
								lblItemName.setText(item.getName() + (item.isNoted() ? " (noted)" : ""));
							else
								lblItemName.setText("");
						} else {
							lblItemName.setText("");
						}
					}
				}
			}

			@Override
			public void keyPressed(KeyEvent arg0) {
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
			}
		});
        
        JLabel lblEnterID = new JLabel("Enter Item ID:");
        lblEnterID.setForeground(new Color(0, 204, 255));
        lblEnterID.setBounds(10, 286, 69, 14);
        panel.add(lblEnterID);
        
        JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		if (ms != null) {
        			if (txtItemID.getText() != null)
        				addItemToList(new Item(Integer.parseInt(txtItemID.getText()), 0, ms.getClient().getInstance()));
        			//MobSlayer.filteredGroundItems.add(new GroundItem(ms.getClient(), new ItemLayer(ms.getClient(), null), null));
        		}
        	}
        });
        btnAdd.setForeground(new Color(0, 204, 255));
        btnAdd.setBorder(new LineBorder(new Color(0, 204, 255), 1, true));
        btnAdd.setContentAreaFilled(false);
        btnAdd.setOpaque(false);
        btnAdd.setBounds(319, 282, 60, 22);
        panel.add(btnAdd);
        
        JButton btnRemove = new JButton("Remove");
        btnRemove.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		removeItemToList(listLootItems.getSelectedIndex());
        	}
        });
        btnRemove.setOpaque(false);
        btnRemove.setForeground(new Color(0, 204, 255));
        btnRemove.setContentAreaFilled(false);
        btnRemove.setBorder(new LineBorder(new Color(0, 204, 255), 1, true));
        btnRemove.setBounds(389, 282, 60, 22);
        panel.add(btnRemove);
        
        JLabel lblFileLocation = new JLabel("...");
        if (itemListFile != null)
        	lblFileLocation.setText(itemListFile.getName());
        lblFileLocation.setHorizontalAlignment(SwingConstants.TRAILING);
        lblFileLocation.setForeground(new Color(0, 204, 255));
        lblFileLocation.setBounds(164, 66, 229, 20);
        panel.add(lblFileLocation);
        
        JButton btnLoad = new JButton("Load");
        btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();
				fc.addChoosableFileFilter(new FileNameExtensionFilter("Normal text file (*.txt)", "txt"));
				fc.setFileFilter(fc.getChoosableFileFilters()[1]);
				if (itemListFile != null) {
					fc.setCurrentDirectory(itemListFile);
					try {
						fc.setDialogTitle((itemListFile).getCanonicalPath());
					} catch (IOException e) {
						MobSlayer.log(e.toString());
					}
				}
				int returnVal = fc.showOpenDialog(fc);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					defaultListModel.clear();
					MobSlayer.filteredItems.clear();
					read(file);
					itemListFile = file;
					lblFileLocation.setText(file.getName());
					saveDefaultSettings();
				}
			}
        });
        btnLoad.setOpaque(false);
        btnLoad.setForeground(new Color(0, 204, 255));
        btnLoad.setContentAreaFilled(false);
        btnLoad.setBorder(new LineBorder(new Color(0, 204, 255), 1, true));
        btnLoad.setBounds(403, 65, 53, 22);
        panel.add(btnLoad);
        
        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();
				fc.addChoosableFileFilter(new FileNameExtensionFilter("Normal text file (*.txt)", "txt"));
				fc.setFileFilter(fc.getChoosableFileFilters()[1]);
				if (itemListFile != null) {
					fc.setCurrentDirectory(itemListFile);
					try {
						fc.setDialogTitle((itemListFile).getCanonicalPath());
					} catch (IOException e) {
						MobSlayer.log(e.toString());
					}
				}
				int returnVal = fc.showSaveDialog(fc);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					if (file.isDirectory())
						return;
					String extension = fc.getFileFilter().getDescription();
					if(extension.equals("Normal text file (*.txt)")) {
				          if (!file.getName().toLowerCase().endsWith(".txt"))
				        	  file = new File(file.getAbsolutePath() + ".txt");
					}
					save(file);
					lblFileLocation.setText(file.getName());
				}
			}
        });
        btnSave.setOpaque(false);
        btnSave.setForeground(new Color(0, 204, 255));
        btnSave.setContentAreaFilled(false);
        btnSave.setBorder(new LineBorder(new Color(0, 204, 255), 1, true));
        btnSave.setBounds(466, 65, 53, 22);
        panel.add(btnSave);
        
        JButton btnEmpty = new JButton("Empty");
        btnEmpty.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		defaultListModel.clear();
        		MobSlayer.filteredItems.clear();
        	}
        });
        btnEmpty.setOpaque(false);
        btnEmpty.setForeground(new Color(0, 204, 255));
        btnEmpty.setContentAreaFilled(false);
        btnEmpty.setBorder(new LineBorder(new Color(0, 204, 255), 1, true));
        btnEmpty.setBounds(459, 282, 60, 22);
        panel.add(btnEmpty);
        
        JLabel lblEnterMobName = new JLabel("Enter Mob Name:");
        lblEnterMobName.setForeground(new Color(0, 204, 255));
        lblEnterMobName.setBounds(10, 313, 84, 14);
        panel.add(lblEnterMobName);
        
        JTextField txtMobName = new JTextField();
        txtMobName.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		if (arg0.getID() == 1001) {
        			String name = txtMobName.getText();
        			if (name != null && !name.equals("")) {
        				ms.setMobName(name);
        				updateMobName();
        			}
        		}
        	}
        });
        txtMobName.setOpaque(false);
        txtMobName.setHorizontalAlignment(SwingConstants.CENTER);
        txtMobName.setForeground(new Color(0, 204, 255));
        txtMobName.setBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(0, 0, 0), new Color(0, 204, 255)));
        txtMobName.setBounds(104, 309, 115, 23);
        panel.add(txtMobName);
        
        JButton btnAssign = new JButton("Assign");
        btnAssign.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		String name = txtMobName.getText();
        		if (name != null && !name.equals("")) {
        			ms.setMobName(name);
        			updateMobName();
        		}
        	}
        });
        btnAssign.setOpaque(false);
        btnAssign.setForeground(new Color(0, 204, 255));
        btnAssign.setContentAreaFilled(false);
        btnAssign.setBorder(new LineBorder(new Color(0, 204, 255), 1, true));
        btnAssign.setBounds(227, 309, 60, 22);
        panel.add(btnAssign);
        
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		refreshGroundItem();
        	}
        });
        btnRefresh.setOpaque(false);
        btnRefresh.setForeground(new Color(0, 204, 255));
        btnRefresh.setContentAreaFilled(false);
        btnRefresh.setBorder(new LineBorder(new Color(0, 204, 255), 1, true));
        btnRefresh.setBounds(466, 93, 53, 22);
        panel.add(btnRefresh);
        
        spnDistance = new JSpinner();
        spnDistance.setModel(new SpinnerNumberModel(7, 1, 30, 1));
        spnDistance.setBounds(256, 91, 38, 20);
        panel.add(spnDistance);
        
        JLabel lblDistance = new JLabel("Pickup Distance:");
        lblDistance.setFont(new Font("Tahoma", Font.PLAIN, 12));
        lblDistance.setForeground(new Color(0, 204, 255));
        lblDistance.setBounds(159, 93, 99, 14);
        panel.add(lblDistance);
        
        if (ms == null)
        	frame.setVisible(true);
	}
	
	public void loadDefaultSettings() {
		try {
			File file = new File("properties.ini");
			if (!file.exists())
				return;
			String line = null;
			FileReader reader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(reader);
			while((line = bufferedReader.readLine()) != null) {
                String words[] = line.split("=");
                switch (words[0]) {
                	case "lastListUsed": itemListFile = new File(words[1]); break;
                }
            }
			bufferedReader.close();
			reader.close();
		} catch (IOException e) {
			MobSlayer.log(e.toString());
		}
	}
	
	public void saveDefaultSettings() {
		try {
			File file = new File("properties.ini");
			if (!file.exists())
				file.createNewFile();
			FileWriter writer;
			writer = new FileWriter(file);
			if (itemListFile != null)
				writer.write("lastListUsed=" + itemListFile.getAbsolutePath());
			writer.close();
		} catch (IOException e) {
			MobSlayer.log(e.toString());
		}
	}
	
	public void save(File file) {
		try {
			FileWriter writer;
			writer = new FileWriter(file);
			writer.write((tglbtnFilter.isSelected() ? "black-list" : "white-list") + System.lineSeparator());
			writer.write(ms.getMobName() + System.lineSeparator());
			writer.write((chckbxIronman.isSelected() ? "ironman" : "normal") + System.lineSeparator());
			for (Item item : MobSlayer.filteredItems) {
				writer.write(item.getID() + System.lineSeparator());
			}
			writer.close();
		} catch (IOException e) {
			MobSlayer.log(e.toString());
		}
	}
	
	public void read(File file) {
		try {
			String line = null;
			FileReader reader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(reader);
			line = bufferedReader.readLine();
			if (line.equals("black-list")) {
				tglbtnFilter.setSelected(true);
				tglbtnFilter.setText("Black-list");
			} else {
				tglbtnFilter.setSelected(false);
				tglbtnFilter.setText("White-list");
			}
			line = bufferedReader.readLine();
			ms.setMobName(line);
			updateMobName();
			line = bufferedReader.readLine();
			if (line.equals("ironman")) {
				chckbxIronman.setSelected(true);
			} else {
				chckbxIronman.setSelected(false);
			}
			while((line = bufferedReader.readLine()) != null) {
                int id = Integer.parseInt(line);
                addItemToList(new Item(id, 0, ms.getClient().getInstance()));
            }
			
			bufferedReader.close();
			reader.close();
		} catch (IOException e) {
			MobSlayer.log(e.toString());
		}
	}
	
	public void updateMobName() {
		lblMobName.setText("Fighting " + ms.getMobName());
	}
	
	public boolean isIronman() {
		return chckbxIronman.isSelected();
	}
	
	public boolean looting() {
		return chckbxLoot.isSelected();
	}
	
	public static class FrameDragListener extends MouseAdapter {

        private final JFrame frame;
        private Point mouseDownCompCoords = null;

        public FrameDragListener(JFrame frame) {
            this.frame = frame;
        }

        public void mouseReleased(MouseEvent e) {
            mouseDownCompCoords = null;
        }

        public void mousePressed(MouseEvent e) {
            mouseDownCompCoords = e.getPoint();
        }

        public void mouseDragged(MouseEvent e) {
            Point currCoords = e.getLocationOnScreen();
            frame.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
        }
    }
	
	public void refreshGroundItem() {
		defaultListGround.clear();
		for (GroundItem gi : ms.getGroundItems().all()) {
			defaultListGround.addElement(gi.getName() + " - (" + gi.getID() + ") - Distance:" + ((int) gi.distance()));
		}
	}
	
	public void addItemToList(Item item) {
		if (!MobSlayer.filteredItems.contains(item)) {
			MobSlayer.filteredItems.add(item);
			defaultListModel.addElement(item.getName() + (item.isNoted() ? " (noted)" : "") + " - (" + item.getID() + ")");
		}
	}
	
	public void removeItemToList(int index) {
		if (index >= 0 && index < defaultListModel.getSize()) {
			defaultListModel.remove(index);
			MobSlayer.filteredItems.remove(index);
		}
	}
}
