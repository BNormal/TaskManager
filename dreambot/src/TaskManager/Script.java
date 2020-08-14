package TaskManager;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.utilities.Timer;

/**
 * This is a base class for the bare necessities for a script to be used for
 * the task engine script manager
 *
 * @see org.dreambot.api.script.AbstractScript
 * @author NumberZ
 */
public abstract class Script extends AbstractScript implements MouseListener {
	protected Timer totalTime = null;
	protected boolean running = false;
	protected boolean taskScript = false;
	protected Date time;
	protected AbstractScript engine;
	protected int runCount = 0;
	protected List<Condition> supportedConditions = new ArrayList<Condition>();
	protected List<Skill> supportedSkills = new ArrayList<Skill>();
	private Task task = null;
	
	/**
	 * Adds the default conditions for every script.
	 */
	public Script() {
		supportedConditions.add(TaskManager.Condition.Continually);
	}
	
	/**
	 * Starts the timer for each script and turns the script on.
	 */
	@Override
	public void onStart() {
		totalTime = new Timer();
		running = true;
	}
	
	/**
	 * Handles the exiting of the script by stopping the timer
	 * and turning the script off.
	 */
	@Override
	public void onExit() {
		running = false;
		time = new Date(totalTime.elapsed());
		if (!taskScript)
			this.stop();
	}
	
	/**
	 * Initializes the script before turning the script on.
	 */
	public void init() {
		
	}
	
	/**
	 * A list of all the conditions added to the script.
	 * @return a <code> Condition List </code>
	 */
	public List<Condition> supportedCondition() {
		return supportedConditions;
	}
	
	/**
	 * A list of all the skills added to the script.
	 * @return a <code> Skill List </code>
	 */
	public List<Skill> supportedSkills() {
		return supportedSkills;
	}
	
	/**
	 * The timer for the script.
	 * @return a <code> Timer </code>
	 */
	public Timer getTotalTime() {
		return totalTime;
	}
	
	/**
	 * Set a new value to the timer.
	 * 
	 * @param totalTime the new timer for the script.
	 * 
	 */
	public void setTotalTime(Timer totalTime) {
		this.totalTime = totalTime;
	}
	
	/**
	 * The counter for the number of times
	 * a script has completed a task.
	 * @return a <code> Integer </code>
	 */
	public int getRunCount() {
		return runCount;
	}

	/**
	 * Assign a new counter for the number of times
	 * a script has completed a task.
	 */
	public void setRunCount(int runCount) {
		this.runCount = runCount;
	}
	
	/**
	 * Increment the counter representing the number of
	 * times the script has completed a task.
	 */
	public void increaseRunCount() {
		runCount++;
	}
	
	/**
	 * The instance of the current dreambot script.
	 * @return a <code> AbstractScript </code>
	 */
	public AbstractScript getEngine() {
		return engine;
	}
	
	/**
	 * Assign the current dreambot script instance as the
	 * main instance of all the other script.
	 * 
	 * @param engine represents the current dreambot
	 * script instance.
	 * 
	 */
	public void setEngine(AbstractScript engine) {
		this.engine = engine;
	}
	
	/**
	 * The script's task that identifies the 
	 * scripts condition for completion.
	 * @return a <code> Task </code>
	 */
	public Task getTask() {
		return task;
	}
	
	/**
	 * Replaces the old task with the new one.
	 * 
	 * @param task represents the task that identifies the 
	 * scripts condition for completion.
	 */
	public void setTask(Task task) {
		this.task = task;
	}
	
	/**
	 * A boolean representing if the script is ran by the
	 * task manager.
	 * @return a <code> boolean </code>
	 */
	public boolean isTaskScript() {
		return taskScript;
	}

	/**
	 * Set a new value for it to be a task.
	 * 
	 * @param taskScript represents the whether the script
	 * is being used by the task manager.
	 */
	public void setTaskScript(boolean taskScript) {
		this.taskScript = taskScript;
	}
	
	/**
	 * A boolean representing if the task assigned to
	 * the script has been completed.
	 * @return a <code> boolean </code>
	 */
	public boolean taskFinished() {
		if (task.getCondition() == Condition.Time) {
			long time = task.getAmount();
			if (totalTime.elapsed() > time)
				return true;
		} else if (task.getCondition() == Condition.Continually) {
			return false;
		} else if (task.getCondition() == Condition.Level) {
			int goalLevel = (int) task.getAmount();
			int realLevel = engine.getSkills().getRealLevel((Skill) task.getConditionItem());
			if (realLevel >= goalLevel)
				return true;
			return false;
		}
		return false;
	}
	
	/**
	 * A boolean representing whether the script is currently on or off.
	 * @return a <code> boolean </code>
	 */
	public boolean isRunning() {
		return running;
	}
	
	/**
	 * Handles the updates for the script.
	 * @return a <code> Integer </code>
	 */
	public abstract int onLoop();
	
	/**
	 * Represents the script as a String.
	 * @return a <code> String </code>
	 */
	@Override
	public String toString() {
		if (task != null) {
			if (task.getCondition() == Condition.Time) {
				int time = (int) (task.getAmount() / 60000);
				if (time / 60 > 0)
					return getManifest().name() + ": " + task.getCondition().name() + " - " + (time / 60) + " Hour" + (time / 60 > 1 ? "s" : "") + ", " + (time % 60) + " Minute" + (time % 60 > 1 ? "s" : "");
				else
					return getManifest().name() + ": " + task.getCondition().name() + " - " + time + " Minute" + (time > 1 ? "s" : "");
			} else if (task.getCondition() == Condition.Continually) {
				return getManifest().name() + ": " + task.getCondition().name() + " - Infinitely/Completed.";
			} else if (task.getCondition() == Condition.Level) {
				String skill = task.getConditionItem().toString();
				skill = skill.substring(0, 1).toUpperCase() + skill.substring(1).toLowerCase();
				return getManifest().name() + ": " + task.getCondition().name() + " - Level " + task.getAmount() + " " + skill + ".";
			} else {
				return getManifest().name() + ": " + task.getCondition().name() + " " + task.getAmount() + ".";
			}
		} else {
			return getManifest().name();
		}
	}
	
	/**
	 * Represents the name of the script.
	 * @return a <code> String </code>
	 */
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	/**
	 * Represents the center of the game screen horizontally.
	 * @return a <code> Integer </code>
	 */
	public int getCenterX() {
		Point clientLocation = getClient().getInstance().getApplet().getLocationOnScreen();
		Dimension clientDimension = getClient().getInstance().getApplet().getSize();
		return (int) (clientLocation.getX() + clientDimension.getWidth() / 2.0);
	}
	
	/**
	 * Represents the center of the game screen vertically.
	 * @return a <code> Integer </code>
	 */
	public int getCenterY() {
		Point clientLocation = getClient().getInstance().getApplet().getLocationOnScreen();
		Dimension clientDimension = getClient().getInstance().getApplet().getSize();
		return (int) (clientLocation.getY() + clientDimension.getHeight() / 2.0);
	}
	
	/**
	 * Handles any mouse clicks on the game screen.
	 * @param event represents the event of the mouse click.
	 */
	@Override
	public void mouseClicked(MouseEvent event) {
		
	}
	
	/**
	 * Handles the event of the mouse entering the game screen.
	 * @param event represents the event of the mouse entering
	 * the game screen.
	 */
	@Override
	public void mouseEntered(MouseEvent event) {
		
	}
	
	/**
	 * Handles the event of the mouse exiting the game screen.
	 * @param event represents the event of the mouse exiting
	 * the game screen.
	 */
	@Override
	public void mouseExited(MouseEvent event) {
		
	}
	
	/**
	 * Handles the mouse event of when the a button on their
	 * mouse has been pressed.
	 * @param event represents the event of the mouse press.
	 */
	@Override
	public void mousePressed(MouseEvent event) {
		
	}

	
	/**
	 * Handles the mouse event of when the a button on their
	 * mouse has been released.
	 * @param event represents the event of the mouse released.
	 */
	@Override
	public void mouseReleased(MouseEvent event) {
		
	}
	
}
