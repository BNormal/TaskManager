package TaskManager.scripts.mining;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.web.node.impl.bank.WebBankArea;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.core.Instance;

import TaskManager.Script;
import TaskManager.scripts.mining.MinerData.OreNode;

@ScriptManifest(author = "NumberZ", category = Category.MINING, name = "Miner", version = 1.0, description = "Mines ores in various areas")
public class Miner extends Script {

	private String pickaxe = "pickaxe";
	private boolean tracking = false;
	private GameObject currentNode;
	private final Color BACKGROUND = new Color(0, 192, 192, 128);
	private MiningSpot location;
	private OreNode selectedRockType;
	private MinerGUI gui;
	
	public Miner() {
		supportedConditions.add(TaskManager.Condition.Time);
		supportedConditions.add(TaskManager.Condition.Level);
		supportedSkills.add(Skill.MINING);
		gui = new MinerGUI(getManifest().name());
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
	public void onStart() {
		super.onStart();
		if (engine == null)
			engine = this;
		location = gui.getMiningArea();
		selectedRockType = gui.getOreNode();
	}

	@Override
	public int onLoop() {
		if (!engine.getLocalPlayer().isOnScreen()) {
			return 0;
		} else if (!tracking && engine.getLocalPlayer().isOnScreen()) {
			tracking = true;
			engine.getSkillTracker().reset(Skill.MINING);
			engine.getSkillTracker().start(Skill.MINING);
		}
		if (Instance.getInstance().isMouseInputEnabled())
			return 0;
		if (running) {
			if (engine.getDialogues().inDialogue() && engine.getDialogues().continueDialogue())
				engine.getDialogues().spaceToContinue();
			if (ableToMine()) {
				handleMining();
			} else if (ableToBank()) {
				handleBanking();
			} else if (needsToBank()) {
				engine.getWalking().walk(location.getBankArea().getCenter());
				if (Calculations.random(0, 20) > 1)
					sleepUntil(() -> engine.getWalking().getDestinationDistance() < Calculations.random(6, 9), 6000);
			} else if (readyToMine()) {
				engine.getWalking().walk(location.getMiningArea().getCenter());
				if (Calculations.random(0, 20) > 1)
					sleepUntil(() -> engine.getWalking().getDestinationDistance() < Calculations.random(6, 9), 6000);
			}
		}
		return 1;
	}

	private boolean handleMining() {
		boolean result = true;
		if (!engine.getLocalPlayer().isMoving() && !engine.getLocalPlayer().isAnimating()) {
			if (currentNode == null || !currentNode.exists())
				currentNode = engine.getGameObjects().closest(rockFilter());
			if (currentNode != null) {
				currentNode.interact("Mine");
				sleepWhile(() -> engine.getLocalPlayer().isAnimating(), Calculations.random(12000, 15400));
				sleepWhile(() -> !engine.getLocalPlayer().isAnimating(), Calculations.random(12000, 15400));
			}
		}
		return result;
	}

	private String getBestPickaxe() {
		int level = engine.getSkills().getRealLevel(Skill.MINING);
		if (level >= 71 && engine.getBank().contains("Crystal pickaxe")) {
			return "Crystal pickaxe";
		} else if (level >= 61 && engine.getBank().contains("Infernal pickaxe")) {
			return "Infernal pickaxe";
		} else if (level >= 61 && engine.getBank().contains("3rd age pickaxe")) {
			return "3rd age pickaxe";
		} else if (level >= 61 && engine.getBank().contains("Dragon pickaxe")) {
			return "Dragon pickaxe";
		} else if (level >= 41 && engine.getBank().contains("Gilded pickaxe")) {
			return "Gilded pickaxe";
		} else if (level >= 41 && engine.getBank().contains("Rune pickaxe")) {
			return "Rune pickaxe";
		} else if (level >= 31 && engine.getBank().contains("Adamant pickaxe")) {
			return "Adamant pickaxe";
		} else if (level >= 21 && engine.getBank().contains("Mithril pickaxe")) {
			return "Mithril pickaxe";
		} else if (level >= 11 && engine.getBank().contains("Black pickaxe")) {
			return "Black pickaxe";
		} else if (level >= 6 && engine.getBank().contains("Steel pickaxe")) {
			return "Steel pickaxe";
		} else if (level >= 1 && engine.getBank().contains("Iron pickaxe")) {
			return "Iron pickaxe";
		}
		return "Bronze pickaxe";
	}
	
	private boolean handleBanking() {
		boolean results = false;
		if (ableToBank()) {
			if (engine.getBank().openClosest()) {
				pickaxe = getBestPickaxe();
				if (!hasPickaxe()) {
					if (engine.getBank().contains(pickaxe))
						engine.getBank().withdraw(pickaxe);
					else
						running = false;
				}
				if (engine.getInventory().contains(selectedRockType.getOreFromNode().getOreId())) {
					increaseRunCount();
					engine.getBank().depositAllExcept(pickaxe);
				}
				if (readyToMine() && engine.getBank().close()) {
					results = true;
				}
			}
		}
		return results;
	}

	private Filter<GameObject> rockFilter() {
		return gameObject -> {
			boolean accepted = false;
			if (gameObject != null && (currentNode == null || (currentNode.getID() == gameObject.getID() && currentNode.getX() == gameObject.getX() && currentNode.getY() == gameObject.getY()))) {
				if (selectedRockType.hasMatch(gameObject.getID()))
					accepted = true;
			}
			return accepted;
		};
	}

	private boolean ableToMine() {
		return readyToMine() && location.getMiningArea().contains(engine.getLocalPlayer());
	}

	private boolean readyToMine() {
		return !engine.getInventory().isFull() && hasPickaxe();
	}

	private boolean needsToBank() {
		return engine.getInventory().isFull() || !hasPickaxe();
	}

	private boolean ableToBank() {
		return needsToBank() && location.getBankArea().contains(engine.getLocalPlayer());
	}

	private boolean hasPickaxe() {
		boolean hasPickaxe = false;
		Item weapon = engine.getEquipment().getItemInSlot(EquipmentSlot.WEAPON.getSlot());
		if (weapon != null && weapon.getName() != null) {
			hasPickaxe = weapon.getName().contains("pickaxe");
		}
		if (!hasPickaxe) {
			for (Item item : engine.getInventory().all()) {
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
		graphics.drawString("Total Time: " + totalTime.formatTime(), x1, y1);
		graphics.drawString("Exp Gained: " + engine.getSkillTracker().getGainedExperience(Skill.MINING), x1, y1 + 12);
		graphics.drawString("Exp Gained Per Hour: " + engine.getSkillTracker().getGainedExperiencePerHour(Skill.MINING), x1, y1 + 12 * 2);
		graphics.drawString("Levels Gained: " + engine.getSkillTracker().getGainedLevels(Skill.MINING), x1, y1 + 12 * 3);
		graphics.drawString("Current Level: " + engine.getSkills().getRealLevel(Skill.MINING), x1, y1 + 12 * 4);
		graphics.drawString("Has Target: " + (currentNode != null ? currentNode.exists() : "false"), x1, y1 + 12 * 5);
	}
	
	public static enum MiningSpot {
		VarrockEast(WebBankArea.VARROCK_EAST.getArea(), new Area(3278, 3371, 3291, 3359, 0), OreNode.TIN_NODE, OreNode.COPPER_NODE, OreNode.IRON_NODE),
		VarrockWest(WebBankArea.VARROCK_WEST.getArea(), new Area(3169, 3380, 3183, 3366, 0), OreNode.CLAY_NODE, OreNode.TIN_NODE, OreNode.IRON_NODE, OreNode.SILVER_NODE);
		//West Mining area = new Area(new Tile(3181, 3381, 0), new Tile(3176, 3374, 0), new Tile(3171, 3369, 0), new Tile(3171, 3364, 0), new Tile(3177, 3361, 0), new Tile(3185, 3367, 0), new Tile(3186, 3379, 0));
		
		
		private Area bankArea;
		private Area miningArea;
		private OreNode[] rockIds;

		private MiningSpot(Area bankArea, Area miningArea, OreNode... rockIds) {
			this.bankArea = bankArea;
			this.miningArea = miningArea;
			this.rockIds = rockIds;
		}

		public Area getBankArea() {
			return bankArea;
		}

		public Area getMiningArea() {
			return miningArea;
		}

		public OreNode[] getRockNodes() {
			return rockIds;
		}
	}
}