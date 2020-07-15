package TaskManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
			if (task.getAmount() == 0)
				return false;
			long time = (long) task.getConditionItem();
			if (totalTime.elapsed() > time)
				return true;
		}
		return false;
	}

	public Script clone() throws CloneNotSupportedException {
		return (Script) super.clone();
	}

	public String toString() {
		if (task != null) {
			if (task.getAmount() > 0) {
				if (task.getCondition() == Condition.Time)
					return getManifest().name() + ": " + task.getCondition().name() + " " + task.getAmount() + " minute(s)";
				return getManifest().name() + ": " + task.getCondition().name() + " " + task.getAmount() + ".";
			} else if (task.getAmount() == 0) {
				return getManifest().name() + ": " + task.getCondition().name() + " - infinitely/completed.";
			}
		} else {
			return getManifest().name();
		}
		return "blank";
	}
	
	public boolean isRunning() {
		return running;
	}

	public abstract int onLoop();
	
}
