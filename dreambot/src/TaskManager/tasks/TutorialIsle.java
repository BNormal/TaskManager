package TaskManager.tasks;

import java.awt.Graphics2D;

import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

import TaskManager.Script;

@ScriptManifest(author = "NumberZ", name = "Tutorial Island (unf)", version = 1.0, description = "Does tutorial island for you.", category = Category.MISC)
public class TutorialIsle extends Script {

	@Override
    public void onStart() {
		running = true;
	}
	
	@Override
	public int onLoop() {
		
		return 0;
	}
	
	@Override
    public void onPaint(Graphics2D graphics) {
		
	}
	
	@Override
    public void onExit() {
		running = false;
	}

}
