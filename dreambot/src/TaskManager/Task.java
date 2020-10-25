package TaskManager;

import org.dreambot.api.methods.skills.Skill;

public class Task {

	private Condition condition;
	private long amount;
	private Skill conditionItem = null;
	
	public Task(Condition condition, int amount) {
		this.condition = condition;
		this.amount = amount;
	}

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	public long getAmount() {
		return amount;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}
	
	public void setConditionItem(Skill item) {
		this.conditionItem = item;
	}
	
	public Skill getConditionItem() {
		return conditionItem;
	}
}
