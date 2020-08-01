package TaskManager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Timer;

import TaskManager.utilities.Utilities;

@ScriptManifest(author = "NumberZ", name = "Task Manager", version = 1.0, description = "Allows you to runs a script to do a task then switch to another task or stop completely.", category = Category.MISC)
public class TaskEngine extends AbstractScript implements MouseListener {

	private TaskEngineGUI gui;
	private boolean started = false;
	private Script currentScript = null;
	private Timer totalTime = new Timer();
	private Color filled = new Color(0.56F, 0.45F, 0.32F, 0.5F);
	private Color border = new Color(0.27f, 0.19f, 0.09f, 0.8f);
	
	@Override
    public void onStart() {
		Point clientLocation = getClient().getInstance().getApplet().getLocationOnScreen();
		Dimension clientDimension = getClient().getInstance().getApplet().getSize();
		int x = (int) (clientLocation.getX() + clientDimension.getWidth() / 2.0);
		int y = (int) (clientLocation.getY() + clientDimension.getHeight() / 2.0);
		getRandomManager().disableSolver(RandomEvent.RESIZABLE_DISABLER);
		if (getRandomManager().getCurrentSolver() != null && getRandomManager().getCurrentSolver().getEventString().equalsIgnoreCase("RESIZABLE_DISABLER"))
			getRandomManager().getCurrentSolver().disable();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					gui = new TaskEngineGUI(x, y);
					gui.open();
					started = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	public int onLoop() {
		if (!started || !gui.isRunning())
			return 0;
		if (currentScript == null) {
			currentScript = gui.getCurrentScript();
			if (currentScript == null) {
				this.stop();
				return 0;
			}
			if (currentScript != null) {
				if (currentScript.getEngine() == null)
					currentScript.setEngine(this);
				currentScript.onStart();
			}
			return 0;
		}
		if (currentScript.getTask() != null && currentScript.taskFinished() || !currentScript.isRunning()) {
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
		gui.exit();
	}
	
	@SuppressWarnings("deprecation")
	@Override
    public void onPaint(Graphics2D g) {
		int x = 310;
		int y = 10;
		g.setColor(filled);//Filled in square
		g.fillRect(x, y, 200, 67);
		g.setColor(border);//Border
		g.setFont(new Font("Arial", 1, 11));
		g.drawRect(x, y, 200, 67);
		g.setColor(Color.WHITE);
		Utilities.drawShadowString(g, "    Total Time Running: " + totalTime.formatTime(), x + 5, y + 13);
		Utilities.drawShadowString(g, "------------------------------------------------", x + 5, y + 23);
		Color gray = Color.GRAY;
		for (int i = 0; i < 4; i++) {
			if (i + gui.getCurrentScriptId() >= gui.getScripts().size())
				break;
			Script script = gui.getScripts().get(i + gui.getCurrentScriptId());
			String scriptTitle = script.getManifest().name() + (script.getTotalTime() != null ? " - " + script.getTotalTime().formatTime() : "");
			if (i + gui.getCurrentScriptId() == gui.getCurrentScriptId())
				Utilities.drawShadowString(g, "> " + scriptTitle, x + 5, y + 33 + i * 10, Color.GREEN, Color.BLACK);
			else {
				Utilities.drawShadowString(g, "   " + scriptTitle, x + 5, y + 33 + i * 10, gray, Color.BLACK);
				gray = gray.darker();
			}
		}
		if (currentScript != null)
			currentScript.onPaint(g);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (!started || !gui.isRunning())
			return;
		if (currentScript != null)
			currentScript.mouseClicked(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (!started || !gui.isRunning())
			return;
		if (currentScript != null)
			currentScript.mouseEntered(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (!started || !gui.isRunning())
			return;
		if (currentScript != null)
			currentScript.mouseExited(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (!started || !gui.isRunning())
			return;
		if (currentScript != null)
			currentScript.mousePressed(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (!started || !gui.isRunning())
			return;
		if (currentScript != null)
			currentScript.mouseReleased(e);
	}
	
}
