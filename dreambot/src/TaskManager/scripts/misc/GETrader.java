package TaskManager.scripts.misc;

import java.util.Date;

import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

import TaskManager.Script;

@ScriptManifest(author = "NumberZ", category = Category.MISC, name = "GE Trader (unf)", version = 1.0, description = "Buy and sell items at the Grand Exchange")
public class GETrader extends Script {
	private State state = null;
	private GETraderGUI gui;

	private enum State {
		BUYING, SELLING, NOTHING
	}
	
	private State getState() {
		return State.NOTHING;
	}
	
	@Override
	public void init() {
		gui = new GETraderGUI(getManifest().name());
		gui.open();
	}
	
	@Override
	public void onStart() {
		if (!taskScript)
			init();
		super.onStart();
		if (engine == null)
			engine = this;
	}
	
	@Override
	public int onLoop() {
		state = getState();
		switch (state) {
		default:
			break;
			
		}
		return 0;
	}

	@Override
	public void onExit() {
		running = false;
		time = new Date(totalTime.elapsed());
		gui.exit();
		if (!taskScript) {
			this.stop();
		}
	}
	
}
