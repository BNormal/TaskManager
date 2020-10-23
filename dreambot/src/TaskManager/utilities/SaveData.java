package TaskManager.utilities;

public class SaveData {
	private String name;
	private String data;
	
	public SaveData(String name, String data) {
		this.name = name;
		this.data = data;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	
}
