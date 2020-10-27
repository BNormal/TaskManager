package TaskManager;

import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.JSpinner;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultFormatter;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.awt.event.ActionEvent;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Color;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.script.Category;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import TaskManager.utilities.Node;
import TaskManager.utilities.SaveData;
import TaskManager.utilities.TreeFilter;
import TaskManager.utilities.Utilities;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class TaskEngineGUI {

	private List<Script> scripts = new ArrayList<Script>();
	private DefaultListModel<Script> tasksModel;
	private JFrame frmTaskManager;
	//private JComboBox<String> cbxScripts;
	private JLabel lblAmount;
	private JComboBox<Condition> cbxConditon;
	private DefaultComboBoxModel<Condition> conditionModel;
	private JComboBox<Skill> cbxSkills;
	private DefaultComboBoxModel<Skill> modelSkills = new DefaultComboBoxModel<Skill>();
	private JSpinner spinAmount;
	private JList<Script> listTasks;
	private JButton btnReplace;
	private JButton btnMoveUp;
	private JButton btnMoveDown;
	private JLabel lblAmountDescription;
	private JLabel lblFileName;
	private Script selectedScript = null;
	private boolean isFinished = false;
	private File scriptList = null;
	private int currentScript = 0;
	private JPanel panelDetails;
	private JTextArea txtDescription;
	private TitledBorder scriptDetailsBorder;
	private JTextField txtSearch;
	private JTextArea textSettingsDetails;

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
					TaskEngineGUI window = new TaskEngineGUI(-1, -1);
					window.frmTaskManager.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public TaskEngineGUI(int x, int y) {
		initialize(x, y);
	}

	public static File[] getJars() {
		File[] jarFiles = (new File(System.getProperty("user.home") + "\\DreamBot\\Scripts\\")).listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isFile() && file.getName().toLowerCase().endsWith(".jar");
			}
		});
		return jarFiles;
	}
	
	@SuppressWarnings("resource")
	public void addScriptsFromJar(String pathToJar) {
		try {
			JarFile jarFile;
			jarFile = new JarFile(pathToJar);
			Enumeration<JarEntry> e = jarFile.entries();
			while (e.hasMoreElements()) {
				JarEntry je = e.nextElement();
				if (je.isDirectory() || !je.getName().endsWith(".class")) {
					continue;
				}
				String className = je.getName().substring(0, je.getName().length() - 6);
				className = className.replace('/', '.');
				Class<?> clazz = Class.forName(className);
				if (!Script.class.isAssignableFrom(clazz))
					continue;
				Script script = Utilities.getScriptFromName(className);
				if (script != null)
					scripts.add(script);
				/*Constructor<?> ctor = clazz.getDeclaredConstructor();
				Object object = ctor.newInstance();
				if (object == null)
					continue;
				if (object instanceof Script) {
					Script script = (Script) object;
					scripts.add(script);
					//System.out.println("Loaded script: " + script);
				}*/
			}
		} catch (IOException | ClassNotFoundException | SecurityException | IllegalArgumentException e1) {
		}
	}
	
	class SortByName implements Comparator<Script> 
	{
		@Override
		public int compare(Script a, Script b) {
			return a.getName().compareTo(b.getName());
		} 
	}
	
	public void loadSctipts() {
		File[] jars = getJars();
		for (int i = 0; i < jars.length; i++) {
			try {
				addScriptsFromJar(jars[i].getCanonicalPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Collections.sort(scripts, new SortByName());
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(int x, int y) {
		frmTaskManager = new JFrame();
		frmTaskManager.setTitle("Task Manager");
		frmTaskManager.setBounds(0, 0, 650, 400);//320, 400
		int width = frmTaskManager.getWidth();
		int height = frmTaskManager.getHeight();
		if (x == -1 && y == -1)
			frmTaskManager.setLocationRelativeTo(null);
		else
			frmTaskManager.setBounds(x - width / 2, y - height / 2, width, height);
		frmTaskManager.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frmTaskManager.getContentPane().setLayout(null);//new MigLayout()
		frmTaskManager.setResizable(false);
		frmTaskManager.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				currentScript = -1;
				isFinished = true;
			}
		});
		
		loadSctipts();
		
		JScrollPane spScripts = new JScrollPane();
		spScripts.setBorder(new LineBorder(Color.DARK_GRAY, 1, true));
		spScripts.setBounds(10, 42, 170, 283);
		frmTaskManager.getContentPane().add(spScripts);
		
		Node root = new Node("Scripts");
		Map<String, Node> uniqueCategories = new TreeMap<String, Node>();
		for (int i = 0; i < scripts.size(); i++) {
			Category c = scripts.get(i).getScriptDetails().category();
			Node treeNode = new Node(c);
			if (!uniqueCategories.containsKey(c.name()))
				uniqueCategories.put(c.name(), treeNode);
		}
		for (Node node : uniqueCategories.values()) {
			root.add(node);
		}
		for (Script script : scripts)
			uniqueCategories.get(script.getScriptDetails().category().name()).add(new Node(script));
		
		DefaultTreeModel treeScriptsModel = new DefaultTreeModel(root);
		TreeFilter treeScripts = new TreeFilter(treeScriptsModel);
		treeScripts.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent arg0) {
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) treeScripts.getLastSelectedPathComponent();
				if (selectedNode != null) {
					if (selectedNode.getUserObject() instanceof Script) {
						selectedScript = (Script) selectedNode.getUserObject();
						scriptDetailsBorder.setTitle(selectedScript.toString());
						ScriptDetails sd = selectedScript.getScriptDetails();
						txtDescription.setText("Author: " + sd.author() + 
								", Version: " + sd.version() + 
								"\nDescription: " + sd.description());
						panelDetails.repaint();
						updateConditions();
						updateSkills();
					}
				}
			}
		});
		spScripts.setViewportView(treeScripts);
		
		txtSearch = new JTextField();
		txtSearch.setBounds(10, 11, 170, 20);
		txtSearch.setColumns(10);
		txtSearch.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				handle();
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				handle();
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				handle();
			}
			public void handle() {
				String f = txtSearch.getText().trim();
				Node currentRoot = (Node) treeScripts.getModel().getRoot();
				Enumeration<TreePath> en = currentRoot != null ?
					treeScripts.getExpandedDescendants(new TreePath(currentRoot.getPath())) : null;
				List<TreePath> pl = en != null ? Collections.list(en) : null;
				if (f.length() > 0) {
					treeScripts.setModel(new DefaultTreeModel(treeScripts.createFilteredTree(root, f)));
					treeScripts.expandAllNodes();
					if (en != null) {
						Node r = (Node) treeScripts.getModel().getRoot();
						if (r != null) {
							treeScripts.restoreExpandedState(r, pl, treeScripts);
						}
					}
				} else {
					treeScripts.setModel(treeScriptsModel);
					treeScripts.collapseAllNodes();
				}
				treeScripts.repaint();
			}
		});
		frmTaskManager.getContentPane().add(txtSearch);
		
		/*DefaultComboBoxModel<String> scriptModel = new DefaultComboBoxModel<String>();
		cbxScripts = new JComboBox<String>(scriptModel);
		for (Script script : scripts) {
			scriptModel.addElement(script.toString());
		}
		cbxScripts.setFocusable(false);
		cbxScripts.setBounds(94, 11, 210, 20);
		
		cbxScripts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateConditions();
				updateSkills();
			}
		});
		frmTaskManager.getContentPane().add(cbxScripts);*/
		
		conditionModel = new DefaultComboBoxModel<Condition>();

		cbxSkills = new JComboBox<Skill>(modelSkills);
		cbxSkills.setFocusable(false);
		cbxSkills.setBounds(369, 114, 115, 20);
		cbxSkills.setVisible(false);
		frmTaskManager.getContentPane().add(cbxSkills);
		
		//for (Condition condition : Condition.values())
			//conditionModel.addElement(condition);
		cbxConditon = new JComboBox<Condition>(conditionModel);
		cbxConditon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateOptions();
				updateAmountDescription();
			}
		});
		cbxConditon.setBounds(249, 114, 110, 20);
		cbxConditon.setFocusable(false);
		frmTaskManager.getContentPane().add(cbxConditon);
		
		JButton btnStart = new JButton("Start");
		btnStart.setFocusable(false);
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				start();
			}
		});
		btnStart.setFont(new Font("Tahoma", Font.BOLD, 20));
		btnStart.setBounds(494, 332, 142, 30);
		frmTaskManager.getContentPane().add(btnStart);
		
		JLabel lblCondition = new JLabel("Condition:");
		lblCondition.setBounds(190, 117, 65, 14);
		frmTaskManager.getContentPane().add(lblCondition);
		
		lblAmount = new JLabel("Amount:");
		lblAmount.setVisible(false);
		lblAmount.setBounds(190, 148, 49, 14);
		frmTaskManager.getContentPane().add(lblAmount);

		lblAmountDescription = new JLabel("");
		lblAmountDescription.setVisible(false);
		lblAmountDescription.setHorizontalAlignment(SwingConstants.LEFT);
		lblAmountDescription.setBounds(319, 148, 165, 14);
		frmTaskManager.getContentPane().add(lblAmountDescription);
		
		spinAmount = new JSpinner();
		spinAmount.setVisible(false);
		JComponent comp = spinAmount.getEditor();
		JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
		DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
		formatter.setCommitsOnValidEdit(true);
		formatter.setAllowsInvalid(false);
		spinAmount.addChangeListener(new ChangeListener() {

	        @Override
	        public void stateChanged(ChangeEvent e) {
	        	updateAmountDescription();
	        }
	    });
		spinAmount.setBounds(249, 145, 60, 20);
		frmTaskManager.getContentPane().add(spinAmount);
		
		JScrollPane scrollTasksPane = new JScrollPane();
		scrollTasksPane.setBorder(new LineBorder(Color.DARK_GRAY, 1, true));
		scrollTasksPane.setBounds(220, 207, 416, 118);
		frmTaskManager.getContentPane().add(scrollTasksPane);
		
		tasksModel = new DefaultListModel<Script>();
		listTasks = new JList<Script>(tasksModel);
		listTasks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listTasks.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                if (!arg0.getValueIsAdjusting()) {
                	int index = listTasks.getSelectedIndex();
                	if (index > -1) {
                    	btnReplace.setVisible(true);
						textSettingsDetails.setText(tasksModel.get(index).getSettingsDetails());
                	} else {
                		textSettingsDetails.setText("");
                		btnReplace.setVisible(false);
                	}
                }
            }
        });
		scrollTasksPane.setViewportView(listTasks);
		
		JButton btnAdd = new JButton("Add");
		btnAdd.setFocusable(false);
		btnAdd.setBounds(190, 176, 89, 23);
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addScript(-1);
			}
		});
		frmTaskManager.getContentPane().add(btnAdd);
		
		JButton btnRemove = new JButton("Remove");
		btnRemove.setFocusable(false);
		btnRemove.setBounds(294, 176, 89, 23);
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int index = listTasks.getSelectedIndex();
				if (index >= 0 && index < tasksModel.size()) {
					tasksModel.remove(index);
					int newIndex = index;
					if (newIndex > tasksModel.getSize() - 1)
						newIndex -= 1;
					if (newIndex > -1)
						listTasks.setSelectedIndex(newIndex);
				}
			}
		});
		frmTaskManager.getContentPane().add(btnRemove);
		
		btnReplace = new JButton("Replace");
		btnReplace.setVisible(false);
		btnReplace.setFocusable(false);
		btnReplace.setBounds(395, 176, 89, 23);
		btnReplace.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int replaceIndex = listTasks.getSelectedIndex();
				if (replaceIndex < 0)
					return;
				addScript(replaceIndex);
				
			}
		});
		frmTaskManager.getContentPane().add(btnReplace);
		btnMoveUp = new JButton("\u2191");
		btnMoveUp.setMargin(new Insets(0, 0, 0, 0));
		btnMoveUp.setBounds(191, 205, 20, 40);
		btnMoveUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int replaceIndex = listTasks.getSelectedIndex();
				if (replaceIndex < 0 || replaceIndex - 1 < 0)
					return;
				Script script = tasksModel.getElementAt(replaceIndex);
				Script script2 = tasksModel.getElementAt(replaceIndex - 1);
				tasksModel.setElementAt(script2, replaceIndex);
				tasksModel.setElementAt(script, replaceIndex - 1);
				listTasks.setSelectedIndex(replaceIndex - 1);
			}
		});
		frmTaskManager.getContentPane().add(btnMoveUp);
		
		btnMoveDown = new JButton("\u2193");
		btnMoveDown.setMargin(new Insets(0, 0, 0, 0));
		btnMoveDown.setBounds(191, 285, 20, 40);
		btnMoveDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int replaceIndex = listTasks.getSelectedIndex();
				if (replaceIndex < 0 || replaceIndex + 1 >= tasksModel.size())
					return;
				Script script = tasksModel.getElementAt(replaceIndex);
				Script script2 = tasksModel.getElementAt(replaceIndex + 1);
				tasksModel.setElementAt(script2, replaceIndex);
				tasksModel.setElementAt(script, replaceIndex + 1);
				listTasks.setSelectedIndex(replaceIndex + 1);
			}
		});
		frmTaskManager.getContentPane().add(btnMoveDown);
		
		JButton btnSaveList = new JButton("Save List");
		btnSaveList.setFocusable(false);
		btnSaveList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (tasksModel.size() > 0) {
					if (scriptList == null && browseForFile(true))
						return;
					Script script = null;
					SaveData data = null;
					List<SaveData> scriptSettings = new ArrayList<SaveData>();
					for (int i = 0; i < tasksModel.size(); i++) {
						script = tasksModel.get(i);
						data = new SaveData(script.getClass().getName(), script.saveState());
						scriptSettings.add(data);
					}
					try {
						Gson gson = new GsonBuilder().create();
						FileWriter writer = new FileWriter(scriptList);
						gson.toJson(scriptSettings, writer);
						writer.flush();
						writer.close();
						System.out.println("wrote file");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
			}
		});
		btnSaveList.setBounds(294, 337, 89, 23);
		frmTaskManager.getContentPane().add(btnSaveList);
		
		JButton btnLoadList = new JButton("Load List");
		btnLoadList.setFocusable(false);
		btnLoadList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (scriptList == null || !scriptList.exists())
					browseForFile(false);
				if (scriptList != null && scriptList.exists()) {
			        try {
						Gson gson = new Gson();
						Reader reader = Files.newBufferedReader(Paths.get(scriptList.getAbsolutePath()));
						Type type = new TypeToken<List<SaveData>>() {}.getType();
						List<SaveData> scriptSettings = gson.fromJson(reader, type);
						SaveData sd = null;
						for (int i = 0; i < scriptSettings.size(); i++) {
							sd = scriptSettings.get(i);
							Script script = Utilities.getScriptFromName(sd.getName());
							if (script != null) {
								script.loadState(sd.getData());
								tasksModel.addElement(script);
							}
							/*try {
								sd = scriptSettings.get(i);
								Class<?> clazz = Class.forName(sd.getName());
								if (!Script.class.isAssignableFrom(clazz)) {
									System.out.println("class not found");
									continue;
								}
								Constructor<?> ctor = clazz.getDeclaredConstructor();
								Object object = ctor.newInstance();
								if (object == null) {
									System.out.println("null object");
									continue;
								}
								if (object instanceof Script) {
									Script script = (Script) object;
									script.loadState(sd.getData());
									System.out.println("worked");
									tasksModel.addElement(script);
								}
							} catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException | ClassNotFoundException e2) {
							}*/
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		btnLoadList.setBounds(395, 337, 89, 23);
		frmTaskManager.getContentPane().add(btnLoadList);
		
		JButton btnBrowse = new JButton("Select");
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				browseForFile(false);
			}
		});
		btnBrowse.setFocusable(false);
		btnBrowse.setBounds(190, 337, 89, 23);
		frmTaskManager.getContentPane().add(btnBrowse);
		
		lblFileName = new JLabel("");
		lblFileName.setBounds(10, 339, 174, 17);
		frmTaskManager.getContentPane().add(lblFileName);
		
		panelDetails = new JPanel();
		scriptDetailsBorder = new TitledBorder(new LineBorder(Color.DARK_GRAY, 1, true), "---", TitledBorder.LEADING, TitledBorder.TOP, null, Color.WHITE);
		panelDetails.setBorder(scriptDetailsBorder);
		
		//new TitledBorder(new LineBorder(Color.DARK_GRAY, 1, true), "Script Details", TitledBorder.LEADING, TitledBorder.TOP, null, Color.WHITE)
		panelDetails.setBounds(190, 5, 294, 100);
		frmTaskManager.getContentPane().add(panelDetails);
		panelDetails.setLayout(null);
		
		txtDescription = new JTextArea();
		txtDescription.setLineWrap(true);
		txtDescription.setWrapStyleWord(true);
		txtDescription.setFont(new Font("Tahoma", Font.PLAIN, 11));
		txtDescription.setOpaque(false);
		txtDescription.setBounds(11, 17, 273, 76);
		panelDetails.add(txtDescription);
		
		JScrollPane scrollSettingDetails = new JScrollPane();
		scrollSettingDetails.setBounds(494, 11, 140, 187);
		scrollSettingDetails.setBorder(new LineBorder(Color.DARK_GRAY, 1, true));
		frmTaskManager.getContentPane().add(scrollSettingDetails);
		
		textSettingsDetails = new JTextArea();
		textSettingsDetails.setOpaque(false);
		textSettingsDetails.setFont(new Font("Tahoma", Font.PLAIN, 11));
		scrollSettingDetails.setViewportView(textSettingsDetails);
		
		updateConditions();
		updateSkills();
		updateAmountDescription();
	}
	
	public boolean browseForFile(boolean save) {
		final JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(new FileNameExtensionFilter("JavaScript Object Notation .json", "json"));
		fc.setAcceptAllFileFilterUsed(false);
		int returnVal = (save ? fc.showSaveDialog(frmTaskManager) : fc.showOpenDialog(frmTaskManager));
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (file != null) {
            	if (!file.getName().toLowerCase().endsWith(".json")) {
            		file = new File(file.getAbsolutePath() + ".json");
            	}
                lblFileName.setText("Selected file: " + file.getName());
                scriptList = file;
            }
            return false;
        }
        return true;
	}
	
	public void addScript(int slot) {
		if (selectedScript != null) {
			Task task = new Task((Condition) cbxConditon.getSelectedItem(), (int) spinAmount.getValue());
			if (cbxConditon.getSelectedItem() == Condition.Time) {
				task.setAmount(Long.valueOf((int) spinAmount.getValue() * 60000 + Calculations.random(0, 60000)));
			} else if (cbxConditon.getSelectedItem() == Condition.Continually) {
				task.setAmount(0);
			} else if (cbxConditon.getSelectedItem() == Condition.Level) {
				task.setAmount(Long.valueOf((int) spinAmount.getValue()));
				task.setConditionItem((Skill) cbxSkills.getSelectedItem());
			}
			Script script = null;
			/*try {
				script = (Script) selectedScript.clone();
			} catch (CloneNotSupportedException e1) {
				e1.printStackTrace();
			}*/
			script = Utilities.getScriptFromName(selectedScript.getClass().getName());
			if (script != null) {
				script.setTask(task);
				script.setTaskScript(true);
				if (slot == -1)
					tasksModel.addElement(script);
				else
					tasksModel.setElementAt(script, slot);
				script.init();
			}
		}
	}
	
	protected void updateConditions() {
		if (selectedScript != null) {
			List<Condition> supportedConditions = selectedScript.supportedCondition();
			if (supportedConditions.size() < 1)
				return;
			conditionModel.removeAllElements();
			for (Condition c : supportedConditions) {
				conditionModel.addElement(c);
			}
		}
	}
	
	public void updateSkills() {
		if (selectedScript != null) {
			List<Skill> supportedSkills = selectedScript.supportedSkills();
			modelSkills.removeAllElements();
			if (supportedSkills.size() < 1)
				return;
			for (Skill s : supportedSkills) {
				modelSkills.addElement(s);
			}
		}
	}

	public Task getTaskFromScript(int index) {
		if (index < 0 || index >= scripts.size())
			return null;
		return scripts.get(index).getTask();
	}
	
	private void updateOptions() {
		if (cbxConditon.getSelectedItem() == Condition.Level) {
			int amount = (Integer) spinAmount.getValue();
			if (amount > 99)
				spinAmount.setValue(99);
			cbxSkills.setVisible(true);
		} else {
			cbxSkills.setVisible(false);
		}
		if (cbxConditon.getSelectedItem() == Condition.Continually) {
			spinAmount.setEnabled(false);
			spinAmount.setVisible(false);
			lblAmount.setVisible(false);
			lblAmountDescription.setVisible(false);
		} else {
			spinAmount.setEnabled(true);
			spinAmount.setVisible(true);
			lblAmount.setVisible(true);
			lblAmountDescription.setVisible(true);
		}
	}
	
	public void updateAmountDescription() {
		int amount = (Integer) spinAmount.getValue();
		if (amount < 1) {
			spinAmount.setValue(1);
			amount = 1;
		} else if (amount > 90000) {
			spinAmount.setValue(90000);
			amount = 90000;
		}
		if (amount > 0) {
			if (cbxConditon.getSelectedItem() == Condition.Time) {
				/*int days = (amount / 60) / 24;
				int hours = (amount / 60) % 24;
				int minutes = amount % 60;
				String time = (days > 0 ? days + " Days, " : "") + (hours > 0 ? hours + " Hours, " : "") + (minutes > 0 ? minutes + " Minutes" : "");
				lblAmountDescription.setText(time);*/
				if (amount / 60 > 0) {
					lblAmountDescription.setText((amount / 60) + " Hour" + (amount / 60 > 1 ? "s" : "") + ", " + (amount % 60) + " Minute" + (amount % 60 > 1 ? "s" : ""));
				} else
					lblAmountDescription.setText(amount + " Minute" + (amount > 1 ? "s" : ""));
			} else if (cbxConditon.getSelectedItem() == Condition.Continually) {
				lblAmountDescription.setText("Infinitely/Completed");
			} else if (cbxConditon.getSelectedItem() == Condition.Level) {
				if (amount > 99) {
					spinAmount.setValue(99);
					amount = 99;
				}
				lblAmountDescription.setText("Level " + amount);
			} else {
				lblAmountDescription.setText(amount + " time(s).");
			}
		} else if (amount == 0) {
			lblAmountDescription.setText("Infinitely/Completed.");
		}
	}
	
	public DefaultListModel<Script> getScripts() {
		return tasksModel;
	}
	
	public void open() {
		frmTaskManager.setVisible(true);
		frmTaskManager.revalidate();
		frmTaskManager.repaint();
	}
	
	public void setAlwaysOnTop(boolean top) {
		frmTaskManager.setAlwaysOnTop(top);
	}
	
	public boolean isVisible() {
		return frmTaskManager.isVisible();
	}
	
	public boolean isActive() {
		return frmTaskManager.isActive();
	}
	
	public void start() {
		frmTaskManager.setVisible(false);
		isFinished = true;
	}
	
	public boolean isFinished() {
		return isFinished;
	}

	public int getCurrentScriptId() {
		return currentScript;
	}
	
	public Script getCurrentScript() {
		if (currentScript >= 0 && currentScript < tasksModel.size())
			return tasksModel.get(currentScript);
		return null;
	}

	public void nextScript() {
		currentScript++;
	}
	
	public void exit() {
		frmTaskManager.setVisible(false);
		frmTaskManager.dispose();
	}
}
