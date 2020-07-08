package Mining;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.web.node.impl.bank.WebBankArea;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.core.Instance;

import java.awt.*;

/**
 * Created with IntelliJ IDEA. User: NotoriousPP Date: 12/13/2014 Time: 6:46 PM
 */
@ScriptManifest(author = "NumberZ", name = "Miner", version = 1.0, description = "Basic Miner", category = Category.MINING)
public class Miner extends AbstractScript {

	private Timer timer = new Timer(0);

	private String pickaxe = "pickaxe";
	private boolean running = false;
	private GameObject currentNode;

	//tin node 11360, 11361, silver node 11369, 11368
	private final Color BACKGROUND = new Color(0, 192, 192, 128);
	private int oreID = 434;
	private Area MINING_AREA = new Area(
			new Tile(3181, 3381, 0), new Tile(3176, 3374, 0), new Tile(3171, 3369, 0), new Tile(3171, 3364, 0), new Tile(3177, 3361, 0), new Tile(3185, 3367, 0), new Tile(3186, 3379, 0)
	);

	@Override
	public void onStart() {
		timer = new Timer(0);
	}

	@Override
	public int onLoop() {
		if (!getLocalPlayer().isOnScreen()) {
			return 0;
		} else if (!running && getLocalPlayer().isOnScreen()) {
			running = true;
			getSkillTracker().start(Skill.MINING);
		}
		if (Instance.getInstance().isMouseInputEnabled())
			return 0;
		if (running) {
			if (oreID == 434 && getSkills().getRealLevel(Skill.MINING) < 31) {
				oreID = 438;//tin ore
			} else if (oreID == 438 && getSkills().getRealLevel(Skill.MINING) >= 31) {
				oreID = 434;//clay
			}
			if (getDialogues().inDialogue() && getDialogues().continueDialogue())
				getDialogues().spaceToContinue();
			if (ableToMine()) {
				handleMining();
			} else if (ableToBank()) {
				handleBanking();
			} else if (needsToBank()) {
				getWalking().walk(WebBankArea.VARROCK_WEST.getArea().getCenter());
			} else if (readyToMine()) {
				getWalking().walk(MINING_AREA.getCenter());
			}
		}
		return 1;
	}

	private boolean handleMining() {
		boolean result = true;
		if (!getLocalPlayer().isMoving() && !getLocalPlayer().isAnimating()) {
			if (currentNode == null || !currentNode.exists())
				currentNode = getGameObjects().closest(clayRockFilter());
			if (currentNode != null) {
				currentNode.interact("Mine");
				sleep(2000);
				sleepWhile(() -> getLocalPlayer().isAnimating(), Calculations.random(12000, 10400));
			}
		}
		return result;
	}

	private String getBestPickaxe() {
		int level = getSkills().getRealLevel(Skill.MINING);
		if (level >= 71 && getBank().contains("Crystal pickaxe")) {
			return "Crystal pickaxe";
		} else if (level >= 61 && getBank().contains("Infernal pickaxe")) {
			return "Infernal pickaxe";
		} else if (level >= 61 && getBank().contains("3rd age pickaxe")) {
			return "3rd age pickaxe";
		} else if (level >= 61 && getBank().contains("Dragon pickaxe")) {
			return "Dragon pickaxe";
		} else if (level >= 41 && getBank().contains("Gilded pickaxe")) {
			return "Gilded pickaxe";
		} else if (level >= 41 && getBank().contains("Rune pickaxe")) {
			return "Rune pickaxe";
		} else if (level >= 31 && getBank().contains("Adamant pickaxe")) {
			return "Adamant pickaxe";
		} else if (level >= 21 && getBank().contains("Mithril pickaxe")) {
			return "Mithril pickaxe";
		} else if (level >= 11 && getBank().contains("Black pickaxe")) {
			return "Black pickaxe";
		} else if (level >= 6 && getBank().contains("Steel pickaxe")) {
			return "Steel pickaxe";
		} else if (level >= 1 && getBank().contains("Iron pickaxe")) {
			return "Iron pickaxe";
		}
		return "Bronze pickaxe";
	}
	
	private boolean handleBanking() {
		boolean results = false;
		if (ableToBank()) {
			if (getBank().openClosest()) {
				pickaxe = getBestPickaxe();
				if (!hasPickaxe()) {
					if (getBank().contains(pickaxe))
						getBank().withdraw(pickaxe);
					else
						running = false;
				}
				if (getInventory().contains(oreID)) {
					getBank().depositAllExcept(pickaxe);
				}
				if (readyToMine() && getBank().close()) {
					results = true;
				}
			}
		}
		return results;
	}

	private Filter<GameObject> clayRockFilter() {
		return gameObject -> {
			boolean accepted = false;
			if (gameObject != null && (currentNode == null || (currentNode.getID() == gameObject.getID()
					&& currentNode.getX() == gameObject.getX() && currentNode.getY() == gameObject.getY()))) {
				if (oreID == 434 && (gameObject.getID() == 11362 || gameObject.getID() == 11363) || oreID == 438 && (gameObject.getID() == 11360 || gameObject.getID() == 11361)) {
					accepted = true;
				}
			}
			return accepted;
		};
	}

	private boolean ableToMine() {
		return readyToMine() && MINING_AREA.contains(getLocalPlayer());
	}

	private boolean readyToMine() {
		return !getInventory().isFull() && hasPickaxe();
	}

	private boolean needsToBank() {
		return getInventory().isFull() || !hasPickaxe();
	}

	private boolean ableToBank() {
		return needsToBank() && WebBankArea.VARROCK_WEST.getArea().contains(getLocalPlayer());
	}

	private boolean hasPickaxe() {
		boolean hasPickaxe = false;
		Item weapon = getEquipment().getItemInSlot(EquipmentSlot.WEAPON.getSlot());
		if (weapon != null && weapon.getName() != null) {
			hasPickaxe = weapon.getName().contains("pickaxe");
		}
		if (!hasPickaxe) {
			for (Item item : getInventory().all()) {
				if (item != null && item.getName().toLowerCase().contains("pickaxe")) {
					hasPickaxe = true;
					break;
				}
			}
		}
		return hasPickaxe;
	}

	@Override
	public void onPaint(Graphics2D graphics) {
		int x = 10;
		int y = 25;
		int width = 200;
		int height = 83;
		int x1 = x + 5;
		int y1 = y + 15;
		graphics.setColor(BACKGROUND);
		graphics.fillRect(x + 1, y + 1, width - 2, height - 2);
		graphics.setColor(Color.BLACK);
		graphics.setStroke(new BasicStroke(2));
		graphics.drawRect(x, y, width, height);
		graphics.setColor(Color.WHITE);
		graphics.drawString("Total Time: " + timer.formatTime(), x1, y1);
		graphics.drawString("Exp Gained: " + getSkillTracker().getGainedExperience(Skill.MINING), x1, y1 + 12);
		graphics.drawString(
				"Exp Gained Per Hour: " + getSkillTracker().getGainedExperiencePerHour(Skill.MINING), x1, y1 + 12 * 2
		);
		graphics.drawString("Levels Gained: " + getSkillTracker().getGainedLevels(Skill.MINING), x1, y1 + 12 * 3);
		graphics.drawString("Current Level: " + getSkills().getRealLevel(Skill.MINING), x1, y1 + 12 * 4);
		graphics.drawString("Has Target: " + (currentNode != null ? currentNode.exists() : "false"), x1, y1 + 12 * 5);
	}

}
