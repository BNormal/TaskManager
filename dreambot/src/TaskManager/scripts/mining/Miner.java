package TaskManager.scripts.mining;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Date;
import java.util.List;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.web.node.impl.bank.WebBankArea;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.core.Instance;

import TaskManager.Script;
import TaskManager.scripts.mining.MinerData.OreNode;
import TaskManager.scripts.mining.MinerData.Pickaxe;
import TaskManager.utilities.Utilities;

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
		if (!taskScript)
			init();
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
		if (!gui.isFinished()) {
			location = gui.getMiningArea();
			selectedRockType = gui.getOreNode();
		}
		if (running && gui.isFinished()) {
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
				if (engine.getBank().isOpen()) {
					engine.getBank().close();
					sleepUntil(() -> !engine.getBank().isOpen(), Calculations.random(3000, 5000));
				} else {
					engine.getWalking().walk(location.getMiningArea().getCenter());
					if (Calculations.random(0, 20) > 1)
						sleepUntil(() -> engine.getWalking().getDestinationDistance() < Calculations.random(6, 9), 6000);
				}
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
				sleepUntil(() -> engine.getLocalPlayer().isAnimating() && !engine.getDialogues().inDialogue(), Calculations.random(12000, 15400));
				sleepUntil(() -> !engine.getLocalPlayer().isAnimating(), Calculations.random(12000, 15400));
			}
		}
		return result;
	}
	
	private String getBestPickaxe() {
		List<Pickaxe> approvedPickaxes = gui.getAllowedPickaxes();
		for (Pickaxe pickaxe : approvedPickaxes) {
			if (pickaxe.meetsAllReqsToUse(engine.getSkills()) && engine.getBank().contains(pickaxe.toString())) {
				return pickaxe.toString();
			}
		}
		return "Bronze pickaxe";
	}
	
	private boolean handleBanking() {
		boolean results = false;
		if (ableToBank()) {
			if (!engine.getBank().isOpen()) {
				engine.getBank().openClosest();
				sleepUntil(() -> engine.getBank().isOpen(), Calculations.random(3000, 5000));
			} else {
				pickaxe = getBestPickaxe();
				if (!hasPickaxe()) {
					if (engine.getBank().contains(pickaxe)) {
						engine.getBank().withdraw(pickaxe);
						sleepUntil(() -> engine.getInventory().contains(pickaxe), Calculations.random(3000, 5000));
					} else if (!engine.getBank().contains(pickaxe) && !engine.getInventory().contains(pickaxe)) {
						onExit();
					}
				}
				if (!engine.getInventory().onlyContains(pickaxe)) {
					if (engine.getInventory().contains(selectedRockType.getOreFromNode().getOreId()))
						increaseRunCount();
					engine.getBank().depositAllExcept(pickaxe);
					sleepUntil(() -> engine.getInventory().onlyContains(pickaxe), Calculations.random(3000, 5000));
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
	public void onPaint(Graphics2D g) {
		int x = 10;
		int y = 25;
		int width = 200;
		int height = 83;
		int x1 = x + 5;
		int y1 = y + 15;
		g.setColor(BACKGROUND);
		g.fillRect(x + 1, y + 1, width - 2, height - 2);
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(2));
		g.drawRect(x, y, width, height);
		g.setColor(Color.WHITE);
		g.drawString("Total Time: " + totalTime.formatTime(), x1, y1);
		g.drawString("Exp Gained: " + engine.getSkillTracker().getGainedExperience(Skill.MINING), x1, y1 + 12);
		g.drawString("Exp Gained Per Hour: " + engine.getSkillTracker().getGainedExperiencePerHour(Skill.MINING), x1, y1 + 12 * 2);
		g.drawString("Levels Gained: " + engine.getSkillTracker().getGainedLevels(Skill.MINING), x1, y1 + 12 * 3);
		g.drawString("Current Level: " + engine.getSkills().getRealLevel(Skill.MINING), x1, y1 + 12 * 4);
		//g.drawString("Has Target: " + (currentNode != null ? currentNode.exists() : "false"), x1, y1 + 12 * 5);
		if (currentNode != null) {
			g.setColor(Color.WHITE);
			g.drawPolygon(currentNode.getTile().getPolygon());
		}
	}
	
	@Override
	public void onExit() {
		running = false;
		time = new Date(totalTime.elapsed());
		if (!taskScript) {
			this.stop();
		}
	}
	
	public static enum MiningSpot {
		Varrock_East(WebBankArea.VARROCK_EAST.getArea(), new Area(3278, 3371, 3291, 3359, 0), OreNode.TIN_NODE, OreNode.COPPER_NODE, OreNode.IRON_NODE),
		Varrock_West(WebBankArea.VARROCK_WEST.getArea(), new Area(new Tile(3181, 3381, 0), new Tile(3176, 3374, 0), new Tile(3171, 3369, 0), new Tile(3171, 3364, 0), new Tile(3177, 3361, 0), new Tile(3185, 3367, 0), new Tile(3186, 3379, 0)), OreNode.CLAY_NODE, OreNode.TIN_NODE, OreNode.IRON_NODE, OreNode.SILVER_NODE),
		Al_Kharid_North_High(WebBankArea.AL_KHARID.getArea(), new Area(new Tile(3298, 3319, 0), new Tile(3302, 3319, 0), new Tile(3305, 3314, 0), new Tile(3305, 3306, 0), new Tile(3307, 3303, 0), new Tile(3304, 3297, 0), new Tile(3291, 3298, 0), new Tile(3295, 3307, 0), new Tile(3293, 3310, 0), new Tile(3296, 3317, 0)), OreNode.TIN_NODE, OreNode.COPPER_NODE, OreNode.IRON_NODE, OreNode.SILVER_NODE, OreNode.COAL_NODE, OreNode.MITHRIL_NODE, OreNode.ADAMANTITE_NODE),
		A_lKharid_North_Low(WebBankArea.AL_KHARID.getArea(), new Area(3293, 3289, 3304, 3283, 0), OreNode.IRON_NODE, OreNode.GOLD_NODE),
		Lumbridge_East(Utilities.getLumbridgeBank(), new Area(3222, 3149, 3230, 3144, 0), OreNode.TIN_NODE, OreNode.COPPER_NODE),
		Lumbridge_West(Utilities.getLumbridgeBank(), new Area(3144, 3154, 3148, 3144, 0), OreNode.COAL_NODE, OreNode.MITHRIL_NODE, OreNode.ADAMANTITE_NODE);
		
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
		
		@Override
		public String toString() {
			String name = name();
			return name.replaceAll("_", " ");
		}
	}
}
