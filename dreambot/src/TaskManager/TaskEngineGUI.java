package TaskManager;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

import TaskManager.tasks.TutorialIsle;

import javax.swing.event.ChangeEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

public class TaskEngineGUI {

	private ArrayList<Script> scripts = new ArrayList<Script>();
	private JFrame frmTaskManager;
	private JComboBox<Condition> cbxConditon;
	private JSpinner spinAmount;
	private JLabel lblAmountDescription;
	private boolean running = false;
	private int currentScript = 0;

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

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmTaskManager = new JFrame();
		frmTaskManager.setTitle("Task Manager");
		frmTaskManager.setBounds(100, 100, 300, 345);
		frmTaskManager.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmTaskManager.getContentPane().setLayout(null);
		
		loadSctipts();
		
		DefaultComboBoxModel<String> scriptModel = new DefaultComboBoxModel<String>();
		JComboBox<String> cbxScripts = new JComboBox<String>(scriptModel);

		for (Script script : scripts) {
			scriptModel.addElement(script.toString());
		}
		cbxScripts.setFocusable(false);
		cbxScripts.setBounds(69, 11, 205, 20);
		frmTaskManager.getContentPane().add(cbxScripts);
		
		DefaultComboBoxModel<Condition> conditionModel = new DefaultComboBoxModel<Condition>();
		for (Condition condition : Condition.values())
			conditionModel.addElement(condition);
		cbxConditon = new JComboBox<Condition>(conditionModel);
		cbxConditon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateAmountDescription();
			}
		});
		cbxConditon.setBounds(69, 39, 205, 20);
		cbxConditon.setFocusable(false);
		frmTaskManager.getContentPane().add(cbxConditon);
		
		JButton btnStart = new JButton("Start");
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
		spinAmount.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				updateAmountDescription();
			}
		});
		spinAmount.setModel(new SpinnerNumberModel(1, 0, 100000, 1));
		spinAmount.setBounds(69, 64, 80, 20);
		frmTaskManager.getContentPane().add(spinAmount);
		
		JScrollPane scrollTasksPane = new JScrollPane();
		scrollTasksPane.setBounds(10, 126, 264, 118);
		frmTaskManager.getContentPane().add(scrollTasksPane);
		
		DefaultListModel<Script> tasksModel = new DefaultListModel<Script>();
		JList<Script> listTasks = new JList<Script>(tasksModel);
		listTasks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollTasksPane.setViewportView(listTasks);
		
		JButton btnAdd = new JButton("Add");
		btnAdd.setBounds(10, 92, 89, 23);
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int index = cbxScripts.getSelectedIndex();
				if (index >= 0 && index < scripts.size()) {
					Task task = new Task((Condition) cbxConditon.getSelectedItem(), (int) spinAmount.getValue());
					if (cbxConditon.getSelectedItem() == Condition.Time) {
						task.setConditionItem(System.currentTimeMillis() + ((int) spinAmount.getValue() * 60000));
					}
					Script script = null;
					try {
						script = scripts.get(index).clone();
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
					if (script != null) {
						script.setTask(task);
						tasksModel.addElement(script);
						scripts.add(script);
					}
				}
			}
		});
		frmTaskManager.getContentPane().add(btnAdd);
		
		JButton btnRemove = new JButton("Remove");
		btnRemove.setBounds(185, 92, 89, 23);
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int index = listTasks.getSelectedIndex();
				if (index >= 0 && index < scripts.size()) {
					tasksModel.remove(index);
					scripts.remove(index);
				}
			}
		});
		frmTaskManager.getContentPane().add(btnRemove);
		updateAmountDescription();
	}

	public void loadSctipts() {
		scripts.add(new TutorialIsle());
	}
	
	public Task getTaskFromScript(int index) {
		if (index < 0 || index >= scripts.size())
			return null;
		return scripts.get(index).getTask();
	}
	
	public void updateAmountDescription() {
		int amount = (Integer) spinAmount.getValue();
		if (amount > 0) {
			if (cbxConditon.getSelectedItem() == Condition.Time)
				lblAmountDescription.setText(amount + " minute(s).");
			else
				lblAmountDescription.setText(amount + " time(s).");
		} else if (amount == 0) {
			lblAmountDescription.setText("Infinitely/Completed.");
		}
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

	public Script getCurrentScript() {
		if (currentScript >= 0 && currentScript < scripts.size())
			return scripts.get(currentScript);
		return null;
	}

	public void nextScript() {
		currentScript++;
	}
}
