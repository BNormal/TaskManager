package TaskManager.scripts.misc;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.script.Category;
import org.dreambot.core.Instance;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import TaskManager.Script;
import TaskManager.ScriptDetails;

@ScriptDetails(author = "NumberZ", category = Category.MISC, name = "Log in/out", version = 1.0, description = "Logs in or out of an account")
public class LogOutIn extends Script {
	private LogOutInGUI gui;
	
	public LogOutIn() {
		gui = new LogOutInGUI(getScriptDetails().name());
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void init() {
		gui.open();
	}
	
	@Override
	public int onLoop() {
		if (!getLocalPlayer().isOnScreen())
			return 0;
		if (!gui.isLoggingOut())
			Instance.getInstance().getScriptManager().setAccount(gui.getNickname());
		Tabs.logout();
		onExit();
		return 0;
	}
	
	@Override
	public void onExit() {
		gui.exit();
		super.onExit();
	}
	
	@Override
	public String saveState() {
		String taskData = super.saveState();
		Gson gson = new GsonBuilder().create();
		List<String> preferences = new ArrayList<String>();
		preferences.add(taskData);
		preferences.add(gui.getSaveDate());
		return gson.toJson(preferences);
	}
	
	@Override
	public void loadState(String data) {
		Gson gson = new Gson();
		List<String> preferences = new ArrayList<String>();
		Type type = new TypeToken<List<String>>() {}.getType();
		preferences = gson.fromJson(data, type);
		setTaskScript(true);
		setTask(gson.fromJson(preferences.get(0), TaskManager.Task.class));
		gui.loadSaveDate(preferences.get(1));
	}
}


