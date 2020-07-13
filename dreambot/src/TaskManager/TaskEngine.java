package TaskManager;

import java.awt.Graphics2D;

import javax.swing.UIManager;

import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

@ScriptManifest(author = "NumberZ", name = "Task Engine", version = 1.0, description = "Allows you to runs a script to do a task then switch to another task or stop completely.", category = Category.MISC)
public class TaskEngine extends AbstractScript {

	private TaskEngineGUI gui = new TaskEngineGUI();
	private boolean started = false;
	private Script currentScript = null;
	
	@Override
    public void onStart() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		gui.open();
		started = true;
	}
	
	@Override
	public int onLoop() {
		if (!started || !gui.isRunning())
			return 0;
		if (currentScript == null) {
			currentScript = gui.getCurrentScript();
			if (currentScript != null) {
				if (currentScript.getEngine() == null)
					currentScript.setEngine(this);
				currentScript.onStart();
			}
			return 0;
		}
		if (currentScript.getTask() != null && currentScript.getTask().isFinished() || !currentScript.isRunning()) {
			currentScript.onExit();
			currentScript = null;
			gui.nextScript();
			return 0;
		}
		currentScript.onLoop();
		return 1;
	}
	
	@Override
    public void onExit() {
		
	}
	
	@SuppressWarnings("deprecation")
	@Override
    public void onPaint(Graphics2D graphics) {
		if (currentScript != null)
			currentScript.onPaint(graphics);
	}
	
}
