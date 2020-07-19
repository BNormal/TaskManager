package TaskManager.scripts.woodcutting;

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
import TaskManager.scripts.woodcutting.WoodcutterData.Tree;

@ScriptManifest(author = "NumberZ", category = Category.WOODCUTTING, name = "Woodcutter", version = 1.0, description = "Cuts trees in various areas")
public class Woodcutter extends Script {

	private String pickaxe = "axe";
	private boolean tracking = false;
	private GameObject currentNode;
	private final Color BACKGROUND = new Color(0, 192, 192, 128);
	private WoodcuttingSpot location;
	private Tree selectedTreeType;
	private WoodcutterGUI gui;
	
	public Woodcutter() {
		supportedConditions.add(TaskManager.Condition.Time);
		supportedConditions.add(TaskManager.Condition.Level);
		supportedSkills.add(Skill.MINING);
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
		super.onStart();
		if (engine == null)
			engine = this;
		//location = gui.getMiningArea();
		//selectedRockType = gui.getOreNode();
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
		if (running) {
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
			if (currentNode == null || !currentNode.exists())
				currentNode = engine.getGameObjects().closest(treeFilter());
			if (currentNode != null) {
				currentNode.interact("Chop down");
				sleepWhile(() -> engine.getLocalPlayer().isAnimating(), Calculations.random(12000, 15400));
				sleepWhile(() -> !engine.getLocalPlayer().isAnimating(), Calculations.random(12000, 15400));
			}
		}
		return result;
	}

	private String getBestAxe() {
		int level = engine.getSkills().getRealLevel(Skill.WOODCUTTING);
		if (level >= 71 && engine.getBank().contains("Crystal axe")) {
			return "Crystal axe";
		} else if (level >= 61 && engine.getBank().contains("Infernal axe")) {
			return "Infernal axe";
		} else if (level >= 61 && engine.getBank().contains("3rd age axe")) {
			return "3rd age axe";
		} else if (level >= 61 && engine.getBank().contains("Dragon axe")) {
			return "Dragon axe";
		} else if (level >= 41 && engine.getBank().contains("Gilded axe")) {
			return "Gilded axe";
		} else if (level >= 41 && engine.getBank().contains("Rune axe")) {
			return "Rune axe";
		} else if (level >= 31 && engine.getBank().contains("Adamant axe")) {
			return "Adamant axe";
		} else if (level >= 21 && engine.getBank().contains("Mithril axe")) {
			return "Mithril axe";
		} else if (level >= 11 && engine.getBank().contains("Black axe")) {
			return "Black axe";
		} else if (level >= 6 && engine.getBank().contains("Steel axe")) {
			return "Steel axe";
		} else if (level >= 1 && engine.getBank().contains("Iron axe")) {
			return "Iron axe";
		}
		return "Bronze axe";
	}
	
	private boolean handleBanking() {
		boolean results = false;
		if (ableToBank()) {
			if (engine.getBank().openClosest()) {
				pickaxe = getBestAxe();
				if (!hasAxe()) {
					if (engine.getBank().contains(pickaxe))
						engine.getBank().withdraw(pickaxe);
					else
						running = false;
				}
				if (engine.getInventory().contains(selectedTreeType.getLogFromTree().getLogId())) {
					increaseRunCount();
					engine.getBank().depositAllExcept(pickaxe);
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
			if (gameObject != null && (currentNode == null || (currentNode.getID() == gameObject.getID() && currentNode.getX() == gameObject.getX() && currentNode.getY() == gameObject.getY()))) {
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
		graphics.drawString("Exp Gained: " + engine.getSkillTracker().getGainedExperience(Skill.WOODCUTTING), x1, y1 + 12);
		graphics.drawString("Exp Gained Per Hour: " + engine.getSkillTracker().getGainedExperiencePerHour(Skill.WOODCUTTING), x1, y1 + 12 * 2);
		graphics.drawString("Levels Gained: " + engine.getSkillTracker().getGainedLevels(Skill.WOODCUTTING), x1, y1 + 12 * 3);
		graphics.drawString("Current Level: " + engine.getSkills().getRealLevel(Skill.WOODCUTTING), x1, y1 + 12 * 4);
		graphics.drawString("Has Target: " + (currentNode != null ? currentNode.exists() : "false"), x1, y1 + 12 * 5);
	}
	
	public static enum WoodcuttingSpot {
		VarrockWest(WebBankArea.VARROCK_WEST.getArea(), new Area(3278, 3371, 3291, 3359, 0), Tree.TREE);
		
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

		public Tree[] getRockNodes() {
			return tree;
		}
	}
}

