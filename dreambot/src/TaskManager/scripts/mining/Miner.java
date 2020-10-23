package TaskManager.scripts.mining;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.dreambot.api.data.GameState;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.SkillTracker;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.walking.web.node.impl.bank.WebBankArea;
import org.dreambot.api.script.Category;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.core.Instance;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import TaskManager.Script;
import TaskManager.ScriptDetails;
import TaskManager.scripts.mining.MinerData.OreNode;
import TaskManager.scripts.mining.MinerData.Pickaxe;
import TaskManager.utilities.Utilities;

@ScriptDetails(author = "NumberZ", category = Category.MINING, name = "Miner", version = 1.0, description = "Mines ores in various areas")
public class Miner extends Script {
	private String pickaxe = "pickaxe";
	private boolean tracking = false;
	private GameObject currentNode;
	private final Color BACKGROUND = new Color(0, 192, 192, 128);
	private MiningSpot location;
	private OreNode selectedRockType;
	private MinerGUI gui;
	private boolean dropping = false;
	
	public Miner() {
		supportedConditions.add(TaskManager.Condition.Time);
		supportedConditions.add(TaskManager.Condition.Level);
		supportedSkills.add(Skill.MINING);
		gui = new MinerGUI(getScriptDetails().name());
	}
	
	@Override
	public void init() {
		gui.open();
	}
	
	@Override
	public void onStart() {
		if (!taskScript)
			init();
		super.onStart();
		location = gui.getMiningArea();
		selectedRockType = gui.getOreNode();
	}

	@Override
	public int onLoop() {
		if (!GameState.values().equals(GameState.LOGGED_IN)) {
			return 0;
		} else if (!tracking && getLocalPlayer().isOnScreen()) {
			tracking = true;
			SkillTracker.reset(Skill.MINING);
			SkillTracker.start(Skill.MINING);
		}
		if (Instance.isMouseInputEnabled())
			return 0;
		if (!gui.isFinished()) {
			location = gui.getMiningArea();
			selectedRockType = gui.getOreNode();
		}
		if (running && gui.isFinished()) {
			if (Dialogues.inDialogue() && Dialogues.continueDialogue())
				Dialogues.spaceToContinue();
			if (dropping) {
				if (Inventory.contains(selectedRockType.getOreFromNode().getOreId())) {
					Inventory.dropAll(selectedRockType.getOreFromNode().getOreId());
				} else {
					if (!Inventory.contains(selectedRockType.getOreFromNode().getOreId()))
						dropping = false;
				}
			} else if (getLocalPlayer().isAnimating()) {
				
			} else if (ableToMine()) {
				handleMining();
			} else if (ableToBank()) {
				handleBanking();
			} else if (needsToBank()) {
				if (gui.isPowerMining())
					dropping = true;
				else {
					Walking.walk(location.getBankArea().getCenter().getRandomizedTile(2));
					if (Calculations.random(0, 20) > 1)
						sleepUntil(() -> Walking.getDestinationDistance() < 6, 6000);
				}
			} else if (readyToMine()) {
				if (Bank.isOpen()) {
					Bank.close();
					sleepUntil(() -> !Bank.isOpen(), Calculations.random(3000, 5000));
				} else {
					Walking.walk(location.getMiningArea().getCenter().getRandomizedTile(2));
					if (Calculations.random(0, 20) > 1)
						sleepUntil(() -> Walking.getDestinationDistance() < 6, 6000);
				}
			}
		}
		return 1;
	}

	private boolean handleMining() {
		boolean result = true;
		if (!getLocalPlayer().isMoving() && !getLocalPlayer().isAnimating()) {
			if (currentNode == null || !currentNode.exists())
				currentNode = GameObjects.closest(rockFilter());
			if (currentNode != null) {
				currentNode.interact("Mine");
				sleepUntil(() -> getLocalPlayer().isAnimating() || Dialogues.inDialogue() || currentNode == null, Calculations.random(12000, 15400));
				sleepUntil(() -> !getLocalPlayer().isAnimating(), Calculations.random(12000, 15400));
			}
		}
		return result;
	}
	
	private String getBestPickaxe() {
		List<Pickaxe> approvedPickaxes = gui.getAllowedPickaxes();
		for (Pickaxe pickaxe : approvedPickaxes) {
			if (pickaxe.meetsAllReqsToUse() && (Bank.contains(pickaxe.toString()) || Inventory.contains(pickaxe.toString()))) {
				return pickaxe.toString();
			}
		}
		return "Bronze pickaxe";
	}
	
	private boolean handleBanking() {
		boolean results = false;
		if (ableToBank()) {
			if (!Bank.isOpen()) {
				Bank.openClosest();
				sleepUntil(() -> Bank.isOpen(), Calculations.random(3000, 5000));
			} else {
				pickaxe = getBestPickaxe();
				if (!hasPickaxe()) {
					if (Bank.contains(pickaxe)) {
						Bank.withdraw(pickaxe);
						sleepUntil(() -> Inventory.contains(pickaxe), Calculations.random(3000, 5000));
					} else if (!Bank.contains(pickaxe) && !Inventory.contains(pickaxe)) {
						onExit();
					}
				}
				if (!Inventory.onlyContains(pickaxe)) {
					if (Inventory.contains(selectedRockType.getOreFromNode().getOreId()))
						increaseRunCount();
					Bank.depositAllExcept(pickaxe);
					sleepUntil(() -> Inventory.onlyContains(pickaxe), Calculations.random(3000, 5000));
				}
				if (readyToMine() && Bank.close()) {
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
		return readyToMine() && location.getMiningArea().contains(getLocalPlayer());
	}

	private boolean readyToMine() {
		return !Inventory.isFull() && hasPickaxe();
	}

	private boolean needsToBank() {
		return Inventory.isFull() || !hasPickaxe();
	}

	private boolean ableToBank() {
		return needsToBank() && location.getBankArea().contains(getLocalPlayer());
	}

	private boolean hasPickaxe() {
		boolean hasPickaxe = false;
		Item weapon = Equipment.getItemInSlot(EquipmentSlot.WEAPON.getSlot());
		if (weapon != null && weapon.getName() != null) {
			hasPickaxe = weapon.getName().contains("pickaxe");
		}
		if (!hasPickaxe) {
			for (Item item : Inventory.all()) {
				if (item != null && item.getName().toLowerCase().contains("pickaxe")) {
					hasPickaxe = true;
					break;
				}
			}
		}
		return hasPickaxe;
	}

	@Override
	public String saveState() {
		String taskData = super.saveState();
		Gson gson = new GsonBuilder().create();
		List<String> preferences = new ArrayList<String>();
		preferences.add(taskData);
		preferences.add(gui.getSaveDate());
		return gson.toJson(preferences);
	}
	
	@Override
	public void loadState(String data) {
		Gson gson = new Gson();
		List<String> preferences = new ArrayList<String>();
		Type type = new TypeToken<List<String>>() {}.getType();
		preferences = gson.fromJson(data, type);
		setTaskScript(true);
		setTask(gson.fromJson(preferences.get(0), TaskManager.Task.class));
		gui.loadSaveDate(preferences.get(1));
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
		g.drawString("Exp Gained: " + SkillTracker.getGainedExperience(Skill.MINING), x1, y1 + 12);
		g.drawString("Exp Gained Per Hour: " + SkillTracker.getGainedExperiencePerHour(Skill.MINING), x1, y1 + 12 * 2);
		g.drawString("Levels Gained: " + SkillTracker.getGainedLevels(Skill.MINING), x1, y1 + 12 * 3);
		g.drawString("Current Level: " + Skills.getRealLevel(Skill.MINING), x1, y1 + 12 * 4);
		//g.drawString("Has Target: " + (currentNode != null ? currentNode.exists() : "false"), x1, y1 + 12 * 5);
		if (currentNode != null) {
			g.setColor(Color.WHITE);
			g.drawPolygon(currentNode.getTile().getPolygon());
		}
	}
	
	@Override
	public void onExit() {
		gui.exit();
		super.onExit();
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
