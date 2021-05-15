package TaskManager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.dreambot.api.methods.MethodProvider;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.SkillTracker;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Timer;
import org.dreambot.core.Instance;

import TaskManager.utilities.Utilities;

@ScriptManifest(author = "NumberZ", name = "Task Manager", version = 1.0, description = "Allows you to runs a script to do a task then switch to another task or stop completely.", category = Category.MISC)
public class TaskEngine extends AbstractScript implements MouseListener, MouseMotionListener {

	private TaskEngineGUI gui;
	private boolean started = false;
	private boolean hovered = false;
	private boolean loadedStartUp = false;
	private Rectangle detailsShape = new Rectangle(510, 10, 230, 22);
	private Rectangle unhoveredShape = new Rectangle(310, 10, 200, 67);
	private Rectangle hoveredShape = new Rectangle(310, 10, 200, 467);
	private Rectangle dropdownButton = new Rectangle(480, 77, 30, 15);
	private Rectangle openGUIButton = new Rectangle(450, 77, 30, 15);
	private Script currentScript = null;
	private Timer totalTime = new Timer();
	private Color filled = Utilities.HexToColor("004147", 127);//new Color(0.56F, 0.45F, 0.32F, 0.5F);
	private Color border = Utilities.HexToColor("001d20", 204);//new Color(0.27f, 0.19f, 0.09f, 0.8f);
	
	@Override
    public void onStart() {
		SkillTracker.start();
		getRandomManager().disableSolver(RandomEvent.LOGIN);
		if (getRandomManager().getCurrentSolver() != null)
			if (getRandomManager().getCurrentSolver().getEventString().equalsIgnoreCase(RandomEvent.LOGIN.name()))
				getRandomManager().getCurrentSolver().disable();
	}
	
	@Override
	public int onLoop() {
		if (!loadedStartUp) {
			loadedStartUp = true;
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						Point clientLocation = Instance.getInstance().getApplet().getLocationOnScreen();
						Dimension clientDimension = Instance.getInstance().getApplet().getSize();
						int x = (int) (clientLocation.getX() + clientDimension.getWidth() / 2.0);
						int y = (int) (clientLocation.getY() + clientDimension.getHeight() / 2.0);
						gui = new TaskEngineGUI(x, y);
						sleep(300);
						gui.open();
					} catch (Exception e) {
						MethodProvider.log(e.getLocalizedMessage());
						e.printStackTrace();
					}
				}
			});
		}
		if (gui == null || !gui.isFinished())
			return 0;
		if (!started) {
			started = true;
			getRandomManager().enableSolver(RandomEvent.LOGIN);
			if (getRandomManager().getCurrentSolver() != null)
				if (getRandomManager().getCurrentSolver().getEventString().equalsIgnoreCase(RandomEvent.LOGIN.name()))
				getRandomManager().getCurrentSolver().enable();
		}
		if (currentScript == null) {
			currentScript = gui.getCurrentScript();
			if (currentScript == null) {
				stop();
				return 0;
			}
			if (currentScript != null) {
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
		if (gui != null)
			gui.exit();
	}
	
	@Override
    public void onPaint(Graphics2D g) {
		if (gui != null) {// && gui.isFinished()
			int x = (int) unhoveredShape.getX();
			int y = (int) unhoveredShape.getY();
			int size = gui.getScripts().size();
			g.setColor(filled);//Filled in square
			if (hovered) {
				int height = 67;
				if (size > 4)
					height += (size - 4) * 10;
				if (height > 467)
					height = 467;
				dropdownButton.setBounds(480, height + 10, 30, 15);
				openGUIButton.setBounds(450, height + 10, 30, 15);
				hoveredShape.setBounds(x, y, (int) hoveredShape.getWidth(), height);
				g.fillRect(x, y, (int) hoveredShape.getWidth(), (int) hoveredShape.getHeight());
				g.setColor(border);//Border
				g.drawRect(x, y, (int) hoveredShape.getWidth(), (int) hoveredShape.getHeight());
			} else {
				dropdownButton.setBounds(480, 77, 30, 15);
				openGUIButton.setBounds(450,77, 30, 15);
				g.fillRect(x, y, (int) unhoveredShape.getWidth(), (int) unhoveredShape.getHeight());
				g.setColor(border);//Border
				g.drawRect(x, y, (int) unhoveredShape.getWidth(), (int) unhoveredShape.getHeight());
			}
			if (currentScript != null) {
				g.setColor(filled);//Filled in square
				g.fillRect(x + 200, y, (int) detailsShape.getWidth(), (int) detailsShape.getHeight());
				g.setColor(border);//Border
				g.drawRect(x + 200, y, (int) detailsShape.getWidth(), (int) detailsShape.getHeight());
				Utilities.drawShadowString(g, currentScript.toString(), x + 205, y + 15, Color.WHITE, Color.BLACK);
			}
			if (size > 4) {
				g.setColor(filled);
				g.fillRect((int) dropdownButton.getX(), (int) dropdownButton.getY(), (int) dropdownButton.getWidth(), (int) dropdownButton.getHeight());
				g.setColor(border);
				g.drawRect((int) dropdownButton.getX(), (int) dropdownButton.getY(), (int) dropdownButton.getWidth(), (int) dropdownButton.getHeight());
				g.setColor(Color.WHITE);
				g.drawString(hovered ? "\u2191" : "\u2193", (int) dropdownButton.getX() + 13, (int) dropdownButton.getY() + 12);
			}
			if (!gui.isVisible()) {
				g.setColor(filled);
				g.fillRect((int) openGUIButton.getX(), (int) openGUIButton.getY(), (int) openGUIButton.getWidth(), (int) openGUIButton.getHeight());
				g.setColor(border);
				g.drawRect((int) openGUIButton.getX(), (int) openGUIButton.getY(), (int) openGUIButton.getWidth(), (int) openGUIButton.getHeight());
				g.setColor(Color.WHITE);
				g.drawString("UI", (int) openGUIButton.getX() + 9, (int) openGUIButton.getY() + 12);
			}
			g.setFont(new Font("Arial", 1, 11));
			g.setColor(Color.WHITE);
			Utilities.drawShadowString(g, "    Total Time Running: " + totalTime.formatTime(), x + 5, y + 13);
			Utilities.drawShadowString(g, "------------------------------------------------", x + 5, y + 23);
			Color gray = Color.GRAY;
			for (int i = 0; i < (hovered ? (size >= 44 ? 44 : size) : 4); i++) {
				if (i + gui.getCurrentScriptId() >= size)
					break;
				Script script = gui.getScripts().get(i + gui.getCurrentScriptId());
				String scriptTitle = script.getScriptDetails().name() + (script.getTotalTime() != null ? " - " + script.getTotalTime().formatTime() : "");
				if (i + gui.getCurrentScriptId() == gui.getCurrentScriptId()) {
					Utilities.drawShadowString(g, "> " + scriptTitle, x + 5, y + 33 + i * 10, Color.GREEN, Color.BLACK);
				} else {
					Utilities.drawShadowString(g, "   " + scriptTitle, x + 5, y + 33 + i * 10, gray, Color.BLACK);
					gray = gray.darker();
				}
			}
		} else {
			if (gui == null || !gui.isVisible()) {
				g.setColor(Color.WHITE);
				Utilities.drawShadowString(g, "Starting Script...", 10, 20);
			}
		}
		if (currentScript != null)
			currentScript.onPaint(g);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (!started || !gui.isFinished())
			return;
		if (currentScript != null)
			currentScript.mouseClicked(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (!started || !gui.isFinished())
			return;
		if (currentScript != null)
			currentScript.mouseEntered(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (!started || !gui.isFinished())
			return;
		if (currentScript != null)
			currentScript.mouseExited(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (!started || !gui.isFinished())
			return;
		if (currentScript != null)
			currentScript.mousePressed(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		Point point = new Point(e.getX(), e.getY());
		if (gui != null) {
			if (gui.getScripts().size() > 4 && dropdownButton.contains(point)) {
				hovered = !hovered;
			}
			if (!gui.isVisible() && openGUIButton.contains(point)) {
				gui.open();
			}
		}
		if (!started || !gui.isFinished())
			return;
		if (currentScript != null)
			currentScript.mouseReleased(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!started || !gui.isFinished())
			return;
		if (currentScript != null)
			currentScript.mouseDragged(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (!started || !gui.isFinished())
			return;
		if (currentScript != null)
			currentScript.mouseMoved(e);
	}
	
}
