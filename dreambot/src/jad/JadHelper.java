package jad;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.interactive.NPC;

import JewelrySmelter.Utilities;

@ScriptManifest(author = "NumberZ", name = "Jad Helper", version = 1.0, description = "Displays the prayer protection needed or Jad.", category = Category.COMBAT)
public class JadHelper extends AbstractScript {

	private JadAttackStyle style = null;
	private Timer displayImage = new Timer();
	private Timer displayDescription = new Timer();
	private int paintXPosition = 30;
	private int paintYPosition = 80;
	private NPC jad;
	private int mobId = 2745;
	private ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
	private Map<Integer, Boolean> keys = new HashMap<Integer,Boolean>();
	private KeyListener listener;
	
	public BufferedImage loadImage(String fileName) {
		try {
			File file = new File(JadHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			return ImageIO.read(new File(file.getParent() + "/images/" + fileName));
		} catch (IOException | URISyntaxException e) {
			log(e.toString());
		}
		return null;
	}
	
	@Override
	public void onStart() {
		log("Welcome to Jad Helper 1.0");
		getRandomManager().disableSolver(RandomEvent.ZOOM_SOLVER);
		getRandomManager().disableSolver(RandomEvent.RESIZABLE_DISABLER);
		images.add(loadImage("Protect_from_Magic.png"));
		images.add(loadImage("Protect_from_Missiles.png"));
		images.add(loadImage("Protect_from_Melee.png"));
		listener = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				keys.put(arg0.getKeyCode(), true);
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				keys.put(arg0.getKeyCode(), false);
				
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				
			}
		};
		getClient().getInstance().addEventListener(listener);
	}
	
	@Override
	public int onLoop() {
		if (keys.get(16) != null && keys.get(16) && keys.get(17) != null && keys.get(17)) {
			paintXPosition = getMouse().getX() - 15;
			paintYPosition = getMouse().getY() - 15;
		}
		if (jad == null)
			jad = getNpcs().closest(mobId);
		if (jad != null && jad.getHealthPercent() <= 0)
			jad = null;
		if (jad != null) {
			JadAttackStyle attackStyle = JadAttackStyle.forAnimId(jad.getAnimation());
			if (attackStyle != null) {
				displayImage.reset();
				style = attackStyle;
			}
		} else {
			style = null;
		}
		getClient().getInstance().setKeyboardInputEnabled(true);
		getClient().getInstance().setMouseInputEnabled(true);
		return 0;
	}
	
	@Override
	public void onExit() {//runs this once after stopping your script
		getRandomManager().enableSolver(RandomEvent.ZOOM_SOLVER);
		getRandomManager().enableSolver(RandomEvent.RESIZABLE_DISABLER);
	}
	
	public void onPaint(Graphics2D g) {
		g.setColor(new Color(0.0F, 0.0F, 0.0F, 0.8F));
		g.fillRect(paintXPosition, paintYPosition, 32, 32);
		g.setColor(Color.MAGENTA.darker());
		g.drawRect(paintXPosition, paintYPosition, 32, 32);
		g.setFont(new Font("Arial", 1, 11));
		if (displayDescription.elapsed() < 5 * 1000) {
			Utilities.drawShadowString(g, "Hold", paintXPosition + 5, paintYPosition + 45, Color.WHITE, Color.BLACK);
			Utilities.drawShadowString(g, "CTRL + SHIFT", paintXPosition - 18, paintYPosition + 55, Color.WHITE, Color.BLACK);
			Utilities.drawShadowString(g, "to move", paintXPosition - 3, paintYPosition + 65, Color.WHITE, Color.BLACK);
		}
		if (style != null) {
			if (images.size() > 0 && displayImage.elapsed() < 5 * 1000) {
				BufferedImage image = null;
				if (style == JadAttackStyle.MAGE)
					image = images.get(0);
				else if (style == JadAttackStyle.RANGE)
					image = images.get(1);
				else if (style == JadAttackStyle.MELEE)
					image = images.get(2);
				if (image != null) {
					g.drawImage(image, paintXPosition + 1, paintYPosition + 1, null);
				}
			}
		}
	}
}
