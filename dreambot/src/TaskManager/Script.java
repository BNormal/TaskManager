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
	
	public Script() {
		supportedConditions.add(TaskManager.Condition.Continually);
	}
	
	@Override
	public void onStart() {
		totalTime = new Timer();
		running = true;
	}
	
	public void init() {
		
	}
	
	public void dispose() {
		
	}
	
	public List<Condition> supportedCondition() {
		return supportedConditions;
	}
	
	public List<Skill> supportedSkills() {
		return supportedSkills;
	}

	public Timer getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(Timer totalTime) {
		this.totalTime = totalTime;
	}
	
	public int getRunCount() {
		return runCount;
	}

	public void setRunCount(int runCount) {
		this.runCount = runCount;
	}
	
	public void increaseRunCount() {
		runCount++;
	}

	public AbstractScript getEngine() {
		return engine;
	}

	public void setEngine(AbstractScript engine) {
		this.engine = engine;
	}
	
	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public boolean isTaskScript() {
		return taskScript;
	}

	public void setTaskScript(boolean taskScript) {
		this.taskScript = taskScript;
	}
	
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
	
	public boolean isRunning() {
		return running;
	}

	public abstract int onLoop();

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
	
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	public int getCenterX() {
		Point clientLocation = getClient().getInstance().getApplet().getLocationOnScreen();
		Dimension clientDimension = getClient().getInstance().getApplet().getSize();
		return (int) (clientLocation.getX() + clientDimension.getWidth() / 2.0);
	}
	
	public int getCenterY() {
		Point clientLocation = getClient().getInstance().getApplet().getLocationOnScreen();
		Dimension clientDimension = getClient().getInstance().getApplet().getSize();
		return (int) (clientLocation.getY() + clientDimension.getHeight() / 2.0);
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		
	}
	
}
