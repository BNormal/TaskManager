package TaskManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.utilities.Timer;

public abstract class Script extends AbstractScript implements Cloneable {
	protected Timer totalTime = null;
	protected boolean running = false;
	protected boolean taskScript = false;
	protected Date time;
	protected AbstractScript engine;
	protected int runCount = 0;
	protected List<Condition> supportedConditions = new ArrayList<Condition>();

	private Task task = null;

	public Script() {
		
	}
	
	@Override
	public void onStart() {
		totalTime = new Timer();
		running = true;
	}
	
	public void init() {
		
	}
	
	public List<Condition> supportedCondition() {
		return supportedConditions;
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

	public Script clone() throws CloneNotSupportedException {
		return (Script) super.clone();
	}

	public String toString() {
		if (task != null) {
			if (task.getCondition() == Condition.Time) {
				if (task.getAmount() / 60 > 0)
					return getManifest().name() + ": " + task.getCondition().name() + " - " + (task.getAmount() / 60) + " Hour" + (task.getAmount() / 60 > 1 ? "s" : "") + ", " + (task.getAmount() % 60) + " Minute" + (task.getAmount() % 60 > 1 ? "s" : "");
				else
					return getManifest().name() + ": " + task.getCondition().name() + " - " + task.getAmount() + " Minute" + (task.getAmount() > 1 ? "s" : "");
				
			} else if (task.getCondition() == Condition.Continually) {
				return getManifest().name() + ": " + task.getCondition().name() + " - Infinitely/Completed.";
			} else if (task.getCondition() == Condition.Level) {
				return getManifest().name() + ": " + task.getCondition().name() + " - Level " + task.getAmount() + ".";
			} else {
				return getManifest().name() + ": " + task.getCondition().name() + " " + task.getAmount() + ".";
			}
		} else {
			return getManifest().name();
		}
	}
	
	public boolean isRunning() {
		return running;
	}

	public abstract int onLoop();
	
}
