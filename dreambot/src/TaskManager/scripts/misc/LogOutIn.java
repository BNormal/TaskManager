package TaskManager.scripts.misc;

import java.util.Date;

import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

import TaskManager.Script;

@ScriptManifest(author = "NumberZ", category = Category.MISC, name = "Log in/out", version = 1.0, description = "Logs in or out of an account")
public class LogOutIn extends Script {
	
	private LogOutInGUI gui;
	
	public LogOutIn() {
		gui = new LogOutInGUI(getManifest().name());
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if (engine == null)
			engine = this;
	}
	
	@Override
	public void init() {
		gui.open();
	}
	
	@Override
	public void dispose() {
		gui.exit();
	}
	
	@Override
	public int onLoop() {
		if (!engine.getLocalPlayer().isOnScreen())
			return 0;
		if (!gui.isLoggingOut())
			engine.getClient().getInstance().getScriptManager().setAccount(gui.getNickname());
		engine.getTabs().logout();
		onExit();
		return 0;
	}
	
	@Override
	public void onExit() {
		running = false;
		time = new Date(totalTime.elapsed());
		if (!taskScript) {
			this.stop();
		}
	}
}


