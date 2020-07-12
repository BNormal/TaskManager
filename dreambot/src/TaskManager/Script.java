package TaskManager;

import org.dreambot.api.script.AbstractScript;

public abstract class Script extends AbstractScript implements Cloneable {

	protected boolean running = false;
	protected boolean taskScript = false;

	private Task task = null;

	public Script() {
		
	}

	@Override
	public abstract int onLoop();

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
	
}
