package TaskManager.scripts.woodcutting;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Date;
import java.util.List;

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
import TaskManager.scripts.woodcutting.WoodcutterData.Tree;
import TaskManager.scripts.woodcutting.WoodcutterData.Axe;

@ScriptManifest(author = "NumberZ", category = Category.WOODCUTTING, name = "Woodcutter", version = 1.0, description = "Cuts trees in various areas")
public class Woodcutter extends Script {

	private String Axe = "axe";
	private boolean tracking = false;
	private GameObject currentTree;
	private final Color BACKGROUND = new Color(0, 192, 192, 128);
	private WoodcuttingSpot location;
	private Tree selectedTreeType;
	private WoodcutterGUI gui;
	
	public Woodcutter() {
		supportedConditions.add(TaskManager.Condition.Time);
		supportedConditions.add(TaskManager.Condition.Level);
		supportedSkills.add(Skill.WOODCUTTING);
		gui = new WoodcutterGUI(getManifest().name());
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
		//location = gui.getWoodcuttingArea();
		//selectedRockType = gui.getTree();
	}

	@Override
	public int onLoop() {
		if (!engine.getLocalPlayer().isOnScreen()) {
			return 0;
		} else if (!tracking && engine.getLocalPlayer().isOnScreen()) {
			tracking = true;
			engine.getSkillTracker().reset(Skill.WOODCUTTING);
			engine.getSkillTracker().start(Skill.WOODCUTTING);
		}
		if (Instance.getInstance().isMouseInputEnabled())
			return 0;
		if (running && gui.isFinished()) {
			if (engine.getDialogues().inDialogue() && engine.getDialogues().continueDialogue())
				engine.getDialogues().spaceToContinue();
			if (ableToCut()) {
				handleWoodcutting();
			} else if (ableToBank()) {
				handleBanking();
			} else if (needsToBank()) {
				engine.getWalking().walk(location.getBankArea().getCenter());
				if (Calculations.random(0, 20) > 1)
					sleepUntil(() -> engine.getWalking().getDestinationDistance() < Calculations.random(6, 9), 6000);
			} else if (readyToCut()) {
				engine.getWalking().walk(location.getWoodCuttingArea().getCenter());
				if (Calculations.random(0, 20) > 1)
					sleepUntil(() -> engine.getWalking().getDestinationDistance() < Calculations.random(6, 9), 6000);
			}
		}
		return 1;
	}

	private boolean handleWoodcutting() {
		boolean result = true;
		if (!engine.getLocalPlayer().isMoving() && !engine.getLocalPlayer().isAnimating()) {
			if (currentTree == null || !currentTree.exists())
				currentTree = engine.getGameObjects().closest(treeFilter());
			if (currentTree != null) {
				currentTree.interact("Chop down");
				sleepWhile(() -> engine.getLocalPlayer().isAnimating(), Calculations.random(12000, 15400));
				sleepWhile(() -> !engine.getLocalPlayer().isAnimating(), Calculations.random(12000, 15400));
			}
		}
		return result;
	}

	private String getBestAxe() {
		int level = engine.getSkills().getRealLevel(Skill.WOODCUTTING);
		List<Axe> approvedPickaxes = gui.getAllowedAxes();
		for (Axe axe : approvedPickaxes) {
			if (level >= axe.getLevelReq() && engine.getBank().contains(axe.toString())) {
				return axe.toString();
			}
		}
		return "Bronze axe";
	}
	
	private boolean handleBanking() {
		boolean results = false;
		if (ableToBank()) {
			if (!engine.getBank().isOpen()) {
				engine.getBank().openClosest();
				sleepUntil(() -> engine.getBank().isOpen(), Calculations.random(3000, 5000));
			} else {
				Axe = getBestAxe();
				if (!hasAxe()) {
					if (engine.getBank().contains(Axe)) {
						engine.getBank().withdraw(Axe);
						sleepUntil(() -> engine.getInventory().contains(Axe), Calculations.random(3000, 5000));
					} else if (!engine.getBank().contains(Axe) && !engine.getInventory().contains(Axe)) {
						onExit();
					}
				}
				if (engine.getInventory().contains(selectedTreeType.getLogFromTree().getLogId())) {
					increaseRunCount();
					engine.getBank().depositAllExcept(Axe);
				}
				if (readyToCut() && engine.getBank().close()) {
					results = true;
				}
			}
		}
		return results;
	}

	private Filter<GameObject> treeFilter() {
		return gameObject -> {
			boolean accepted = false;
			if (gameObject != null && (currentTree == null || (currentTree.getID() == gameObject.getID() && currentTree.getX() == gameObject.getX() && currentTree.getY() == gameObject.getY()))) {
				if (selectedTreeType.hasMatch(gameObject.getName()))
					accepted = true;
			}
			return accepted;
		};
	}

	private boolean ableToCut() {
		return readyToCut() && location.getWoodCuttingArea().contains(engine.getLocalPlayer());
	}

	private boolean readyToCut() {
		return !engine.getInventory().isFull() && hasAxe();
	}

	private boolean needsToBank() {
		return engine.getInventory().isFull() || !hasAxe();
	}

	private boolean ableToBank() {
		return needsToBank() && location.getBankArea().contains(engine.getLocalPlayer());
	}

	private boolean hasAxe() {
		boolean hasAxe = false;
		Item weapon = engine.getEquipment().getItemInSlot(EquipmentSlot.WEAPON.getSlot());
		if (weapon != null && weapon.getName() != null) {
			hasAxe = weapon.getName().contains("axe");
		}
		if (!hasAxe) {
			for (Item item : engine.getInventory().all()) {
				if (item != null && item.getName().toLowerCase().contains("axe")) {
					hasAxe = true;
					break;
				}
			}
		}
		return hasAxe;
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
		g.drawString("Exp Gained: " + engine.getSkillTracker().getGainedExperience(Skill.WOODCUTTING), x1, y1 + 12);
		g.drawString("Exp Gained Per Hour: " + engine.getSkillTracker().getGainedExperiencePerHour(Skill.WOODCUTTING), x1, y1 + 12 * 2);
		g.drawString("Levels Gained: " + engine.getSkillTracker().getGainedLevels(Skill.WOODCUTTING), x1, y1 + 12 * 3);
		g.drawString("Current Level: " + engine.getSkills().getRealLevel(Skill.WOODCUTTING), x1, y1 + 12 * 4);
		//g.drawString("Has Target: " + (currentNode != null ? currentNode.exists() : "false"), x1, y1 + 12 * 5);
		if (currentTree != null) {
			g.setColor(Color.WHITE);
			g.drawPolygon(currentTree.getTile().getPolygon());
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
	
	public static enum WoodcuttingSpot {
		VarrockWest(WebBankArea.VARROCK_WEST.getArea(), new Area(3278, 3371, 3291, 3359, 0), Tree.TREE, Tree.OAK_TREE);

		private Area bankArea;
		private Area woodcuttingArea;
		private Tree[] tree;

		private WoodcuttingSpot(Area bankArea, Area woodcuttingArea, Tree... tree) {
			this.bankArea = bankArea;
			this.woodcuttingArea = woodcuttingArea;
			this.tree = tree;
		}

		public Area getBankArea() {
			return bankArea;
		}

		public Area getWoodCuttingArea() {
			return woodcuttingArea;
		}

		public Tree[] getTrees() {
			return tree;
		}
		
		@Override
		public String toString() {
			String name = name();
			return name.replaceAll("_", " ");
		}
	}
}
