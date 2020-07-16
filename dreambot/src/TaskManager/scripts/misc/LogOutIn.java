package TaskManager.scripts.misc;

import java.util.Date;

import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

import TaskManager.Script;

@ScriptManifest(author = "NumberZ", category = Category.MISC, name = "Log in/out (unf)", version = 1.0, description = "Logs an account in or out")
public class LogOutIn extends Script {
	@Override
	public void onStart() {
		super.onStart();
		if (engine == null)
			engine = this;
	}
	
	@Override
	public int onLoop() {
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
