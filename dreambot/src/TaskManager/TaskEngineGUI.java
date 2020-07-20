package TaskManager;

import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.JSpinner;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultFormatter;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;

import org.dreambot.api.methods.skills.Skill;

import TaskManager.scripts.WoolSpinner;
import TaskManager.scripts.mining.Miner;
import TaskManager.scripts.misc.LogOutIn;
import TaskManager.scripts.misc.TutorialIsle;
import TaskManager.scripts.quests.CookAssistant;
import TaskManager.scripts.quests.ErnestTheChicken;
import TaskManager.scripts.quests.RomeoAndJuliet;
import TaskManager.scripts.woodcutting.Woodcutter;

import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.awt.event.ActionEvent;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.awt.Font;


public class TaskEngineGUI {

	private List<Script> scripts = new ArrayList<Script>();
	private DefaultListModel<Script> tasksModel;
	private JFrame frmTaskManager;
	private JComboBox<String> cbxScripts;
	private JComboBox<Condition> cbxConditon;
	private DefaultComboBoxModel<Condition> conditionModel;
	private JComboBox<Skill> cbxSkills;
	private DefaultComboBoxModel<Skill> modelSkills = new DefaultComboBoxModel<Skill>();
	private JSpinner spinAmount;
	private JList<Script> listTasks;
	private JButton btnReplace;
	private JLabel lblAmountDescription;
	private boolean running = false;
	private int currentScript = 0;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TaskEngineGUI window = new TaskEngineGUI();
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
	public TaskEngineGUI() {
		initialize();
	}

	class SortByName implements Comparator<Script> 
	{
		@Override
		public int compare(Script a, Script b) {
			return a.getName().compareTo(b.getName());
		} 
	}
	
	public void loadSctipts() {//Add your scripts here for now
		scripts.add(new TutorialIsle());
		scripts.add(new WoolSpinner());
		scripts.add(new Miner());
		scripts.add(new RomeoAndJuliet());
		scripts.add(new CookAssistant());
		scripts.add(new ErnestTheChicken());
		scripts.add(new LogOutIn());
		scripts.add(new Woodcutter());
		Collections.sort(scripts, new SortByName());
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
		frmTaskManager = new JFrame();
		frmTaskManager.setTitle("Task Manager");
		frmTaskManager.setBounds(100, 100, 300, 345);
		frmTaskManager.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmTaskManager.getContentPane().setLayout(null);
		frmTaskManager.setResizable(false);
		
		loadSctipts();
		
		DefaultComboBoxModel<String> scriptModel = new DefaultComboBoxModel<String>();
		cbxScripts = new JComboBox<String>(scriptModel);
		for (Script script : scripts) {
			scriptModel.addElement(script.toString());
		}
		cbxScripts.setFocusable(false);
		cbxScripts.setBounds(69, 11, 205, 20);
		
		conditionModel = new DefaultComboBoxModel<Condition>();
		cbxScripts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateConditions();
				updateSkills();
			}
		});
		frmTaskManager.getContentPane().add(cbxScripts);

		cbxSkills = new JComboBox<Skill>(modelSkills);
		cbxSkills.setFocusable(false);
		cbxSkills.setBounds(159, 39, 115, 20);
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
		cbxConditon.setBounds(69, 39, 80, 20);
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
		btnStart.setBounds(10, 252, 264, 43);
		frmTaskManager.getContentPane().add(btnStart);
		
		JLabel lblCondition = new JLabel("Condition:");
		lblCondition.setBounds(10, 42, 49, 14);
		frmTaskManager.getContentPane().add(lblCondition);
		
		JLabel lblTask = new JLabel("Task:");
		lblTask.setBounds(10, 14, 49, 14);
		frmTaskManager.getContentPane().add(lblTask);
		
		JLabel lblAmount = new JLabel("Amount:");
		lblAmount.setBounds(10, 67, 49, 14);
		frmTaskManager.getContentPane().add(lblAmount);

		lblAmountDescription = new JLabel("");
		lblAmountDescription.setBounds(159, 67, 115, 14);
		frmTaskManager.getContentPane().add(lblAmountDescription);
		
		spinAmount = new JSpinner();
		JComponent comp = spinAmount.getEditor();
		JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
		DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
		formatter.setCommitsOnValidEdit(true);
		spinAmount.addChangeListener(new ChangeListener() {

	        @Override
	        public void stateChanged(ChangeEvent e) {
	        	updateAmountDescription();
	        }
	    });
		spinAmount.setBounds(69, 64, 80, 20);
		frmTaskManager.getContentPane().add(spinAmount);
		
		JScrollPane scrollTasksPane = new JScrollPane();
		scrollTasksPane.setBounds(10, 126, 264, 118);
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
                	} else {
                		btnReplace.setVisible(false);
                	}
                }
            }
        });
		scrollTasksPane.setViewportView(listTasks);
		
		JButton btnAdd = new JButton("Add");
		btnAdd.setFocusable(false);
		btnAdd.setBounds(10, 92, 80, 23);
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addScript(-1);
			}
		});
		frmTaskManager.getContentPane().add(btnAdd);
		
		JButton btnRemove = new JButton("Remove");
		btnRemove.setFocusable(false);
		btnRemove.setBounds(194, 92, 80, 23);
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int index = listTasks.getSelectedIndex();
				if (index >= 0 && index < tasksModel.size()) {
					tasksModel.remove(index);
				}
			}
		});
		frmTaskManager.getContentPane().add(btnRemove);
		
		btnReplace = new JButton("Replace");
		btnReplace.setVisible(false);
		btnReplace.setFocusable(false);
		btnReplace.setBounds(102, 92, 80, 23);
		btnReplace.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int replaceIndex = listTasks.getSelectedIndex();
				if (replaceIndex < 0)
					return;
				addScript(replaceIndex);
				
			}
		});
		frmTaskManager.getContentPane().add(btnReplace);
		updateConditions();
		updateSkills();
		updateAmountDescription();
	}
	
	public void addScript(int slot) {
		int index = cbxScripts.getSelectedIndex();
		if (index >= 0 && index < scripts.size()) {
			Task task = new Task((Condition) cbxConditon.getSelectedItem(), (int) spinAmount.getValue());
			if (cbxConditon.getSelectedItem() == Condition.Time) {
				task.setAmount(Long.valueOf((int) spinAmount.getValue() * 60000));
			} else if (cbxConditon.getSelectedItem() == Condition.Continually) {
				task.setAmount(0);
			} else if (cbxConditon.getSelectedItem() == Condition.Level) {
				task.setAmount(Long.valueOf((int) spinAmount.getValue()));
				task.setConditionItem(cbxSkills.getSelectedItem());
			}
			Script script = null;
			try {
				Class<?> clazz = Class.forName(scripts.get(index).getClass().getName());
				Constructor<?> ctor = clazz.getConstructor();
				Object object = ctor.newInstance();
				script = (Script) object;
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				
			}
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
		Script script = scripts.get(cbxScripts.getSelectedIndex());
		if (script != null) {
			List<Condition> supportedConditions = script.supportedCondition();
			if (supportedConditions.size() < 1)
				return;
			conditionModel.removeAllElements();
			for (Condition c : supportedConditions) {
				conditionModel.addElement(c);
			}
		}
	}
	
	public void updateSkills() {
		Script script = scripts.get(cbxScripts.getSelectedIndex());
		if (script != null) {
			List<Skill> supportedSkills = script.supportedSkills();
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
		if (cbxConditon.getSelectedItem() == Condition.Continually)
			spinAmount.setEnabled(false);
		else
			spinAmount.setEnabled(true);
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
				if (amount / 60 > 0)
					lblAmountDescription.setText((amount / 60) + " Hour" + (amount / 60 > 1 ? "s" : "") + ", " + (amount % 60) + " Minute" + (amount % 60 > 1 ? "s" : ""));
				else
					lblAmountDescription.setText(amount + " Minute" + (amount > 1 ? "s" : ""));
			}else if (cbxConditon.getSelectedItem() == Condition.Continually)
				lblAmountDescription.setText("Infinitely/Completed");
			else if (cbxConditon.getSelectedItem() == Condition.Level) {
				if (amount > 99) {
					spinAmount.setValue(99);
					amount = 99;
				}
				lblAmountDescription.setText("Level " + amount);
			}else
				lblAmountDescription.setText(amount + " time(s).");
		} else if (amount == 0) {
			lblAmountDescription.setText("Infinitely/Completed.");
		}
	}
	
	public DefaultListModel<Script> getScripts() {
		return tasksModel;
	}
	
	public void open() {
		frmTaskManager.setVisible(true);
	}
	
	public void start() {
		frmTaskManager.setVisible(false);
		running = true;
	}
	
	public boolean isRunning() {
		return running;
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
