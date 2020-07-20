package TaskManager.scripts.misc;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Date;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.web.node.impl.bank.WebBankArea;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;

import TaskManager.Script;
import TaskManager.utilities.Utilities;

@ScriptManifest(author = "NumberZ", category = Category.MISC, name = "Wool Shearer", version = 1.0, description = "Shears the sheep at the lumbridge farms")
public class WoolShearer extends Script {

	private Area FARMERS_HOUSE = new Area(3188, 3274, 3191, 3271, 0);
	private Area SHEEP_AREA = new Area(3194, 3275, 3212, 3258, 0);
	private Area CASTLE = new Area(3205, 3228, 3216, 3208, 0);
	private int SHEARS = 1735;
	private String state;
	private int bankStatus = -1;

	@Override
	public void onStart() {
		super.onStart();
		if (engine == null)
			engine = this;
	}

	@Override
	public int onLoop() {
		if (!engine.getLocalPlayer().isOnScreen())
			return 0;
		if (!engine.getInventory().contains(SHEARS)) {
			grabShears();
		} else if (engine.getInventory().contains(SHEARS)) {
			if (engine.getInventory().isFull()) {
				handleBanking();
			} else {
				handleShearing();
			}
		}
		return 0;
	}

	public GameObject getObject(Tile tile, String name, String option) {
		GameObject[] objects = engine.getGameObjects().getObjectsOnTile(tile);
		if (objects == null)
			return null;
		for (int i = 0; i < objects.length; i++) {
			if (objects[i].getName().toLowerCase().contains(name.toLowerCase())) {
				String[] actions = objects[i].getActions();
				for (int j = 0; j < actions.length; j++) {
					if (actions[j].contains(option))
						return objects[i];
				}
			}
		}
		return null;
	}

	public boolean inAreaIgnoreZ(Area a, Tile b) {
		a.setZ(b.getZ());
		return a.contains(b);
	}
	
	public void handleBanking() {
		Area bank = WebBankArea.LUMBRIDGE.getArea();
		bank.setZ(2);
		if (inAreaIgnoreZ(CASTLE, engine.getLocalPlayer().getTile())) {
			if (bank.contains(engine.getLocalPlayer())) {
				if (!engine.getBank().isOpen()) {
					engine.getBank().openClosest();
					sleepUntil(() -> engine.getBank().isOpen(), Calculations.random(3000, 5000));
				} else {
					state = "Banking";
					if (!engine.getInventory().onlyContains(SHEARS) && !engine.getInventory().isEmpty()) {
						engine.getBank().depositAllExcept(SHEARS);
						sleepUntil(() -> engine.getInventory().onlyContains(SHEARS) || engine.getInventory().isEmpty(), Calculations.random(3000, 5000));
					}
					if (!engine.getInventory().contains(SHEARS) && engine.getBank().contains(SHEARS)) {
						engine.getBank().withdraw(SHEARS);
						sleepUntil(() -> engine.getInventory().contains(SHEARS), Calculations.random(3000, 5000));
					}
					if (engine.getBank().contains(SHEARS)) {
						bankStatus = 1;
					} else {
						bankStatus = 0;
					}
				}
			} else {
				state = "Going to bank";
				engine.getWalking().walk(bank.getCenter());
				sleepUntil(() -> engine.getWalking().getDestination().distance(engine.getLocalPlayer()) < 6, 6000);
			}
		} else {
			state = "Going to bank";
			engine.getWalking().walk(new Tile(3208, 3210, 0));//lumbridge castle
			sleepUntil(() -> engine.getWalking().getDestination().distance(engine.getLocalPlayer()) < 6, 6000);
		}
	}
	
	public void handleShearing() {
		if (inAreaIgnoreZ(CASTLE, engine.getLocalPlayer().getTile()) && engine.getLocalPlayer().getZ() > 1) {
			if (engine.getLocalPlayer().getZ() == 2) {
				state = "Going to sheep";
				if (engine.getBank().isOpen()) {
					engine.getBank().close();
					sleepUntil(() -> !engine.getBank().isOpen(), Calculations.random(3000, 5000));
				} else {
					engine.getWalking().walk(SHEEP_AREA.getCenter());
					sleepUntil(() -> engine.getWalking().getDestination().distance(engine.getLocalPlayer()) < 6, 6000);
				}
			}
		} else if (SHEEP_AREA.contains(engine.getLocalPlayer())) {
			state = "Shearing the sheep";
			NPC sheep = engine.getNpcs().closest(npcFilter());
			if (sheep != null) {
				engine.getCamera().mouseRotateToEntity(sheep);
				int id = sheep.getID();
				sheep.interactForceRight("Shear");
				sleepUntil(() -> engine.getLocalPlayer().isAnimating() || sheep.getID() != id, 6000);
				sleepUntil(() -> !engine.getLocalPlayer().isAnimating(), 6000);
			}
		} else {
			state = "Going to sheep";
			engine.getWalking().walk(SHEEP_AREA.getCenter());
			sleepUntil(() -> engine.getWalking().getDestination().distance(engine.getLocalPlayer()) < 6, 6000);
		}
	}
	
	public void grabShears() {
		if (CASTLE.getCenter().distance(engine.getLocalPlayer()) < FARMERS_HOUSE.getCenter().distance(engine.getLocalPlayer()) && (bankStatus == -1 || bankStatus == 1)) {
			state = "Checking bank for shears";
			handleBanking();
		} else {
			state = "Getting a pair of shears";
			if (FARMERS_HOUSE.contains(engine.getLocalPlayer())) {
				GroundItem shears = engine.getGroundItems().closest(SHEARS);
				if (shears != null && shears.exists()) {
					shears.interactForceRight("Take");
					sleepUntil(() -> engine.getInventory().contains(SHEARS), Calculations.random(3000, 5000));
				}
			} else {
				if (engine.getBank().isOpen()) {
					engine.getBank().close();
					sleepUntil(() -> !engine.getBank().isOpen(), Calculations.random(3000, 5000));
				}
				engine.getWalking().walk(FARMERS_HOUSE.getCenter());
				sleepUntil(() -> engine.getWalking().getDestination().distance(engine.getLocalPlayer()) < 6, 6000);
			}
		}
	}
	
	private Filter<NPC> npcFilter() {//Sheep
		return npc -> {
			boolean accepted = false;
			if (npc != null && (npc.getID() == 2693 || npc.getID() == 2694 || npc.getID() == 2699 || npc.getID() == 2786 || npc.getID() == 2787) && engine.getMap().canReach(npc) && SHEEP_AREA.contains(npc)) {
					accepted = true;
			}
			return accepted;
		};
	}
	
	@Override
	public void onPaint(Graphics2D g) {
		int x = 25;
		g.setColor(new Color(0.0F, 0.0F, 0.0F, 0.2F));
		g.fillRect(20, 37, 200, 27);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", 1, 11));
		g.drawRect(20, 37, 200, 27);
		Utilities.drawShadowString(g, "Time Running: " + totalTime.formatTime(), x, 50);
		Utilities.drawShadowString(g, "Stage: " + state, x, 60);
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
