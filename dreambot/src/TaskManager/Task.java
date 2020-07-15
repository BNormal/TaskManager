package TaskManager;

public class Task {
	
	private Condition condition;
	private int amount;
	private Object conditionItem = null;
	
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

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	public void setConditionItem(Object item) {
		this.conditionItem = item;
	}
	
	public Object getConditionItem() {
		return conditionItem;
	}
}
