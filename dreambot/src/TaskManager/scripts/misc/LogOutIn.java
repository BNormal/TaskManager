package TaskManager.scripts.misc;

import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.script.Category;
import org.dreambot.core.Instance;

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
}


