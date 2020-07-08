package MobHunter;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.MethodProvider;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.core.Instance;

import org.dreambot.api.methods.skills.Skill;


@ScriptManifest(name = "MobHunter", author = "NumberZ", description = "Mob Hunter", version = 1.0, category = Category.COMBAT)
public class MobSlayer extends AbstractScript {
	private Timer totalTime = new Timer();
	private Timer antiBanDelay = new Timer();
	private Timer attackDelay = new Timer();
	private Timer flashDelay = new Timer();
	private SkillTracking st;
	
	private int randomRunStart = 50;
	private boolean started = false;
	private boolean hasStarted = false;
	boolean flash = false;
	
	public int killCount = 0;
	public boolean wasAttackingTarget = false;
	public String currentStage = "None";
	public String lootingStage = "None";

	private String mobName = "Chicken";//Chicken Minotuar

	private NPC target = null;
	private NPC nextTarget = null;
	private ArrayList<Tile> lastDeadTargetTile = new ArrayList<Tile>();
	public GroundItem foundLoot = null;
	public GroundItem nextFoundLoot = null;
	public int ammo = 884;
	private static GUI options;
	private boolean running;
	public static Thread thread;
	public static ArrayList<Item> filteredItems;
	private Item ammoItem;
	private int id = 0;
	private BufferedImage itemImage = null;
	
	public class LootThread extends Thread {
		public void run() {
			try {
				running = true;
				while (running) {
					Thread.sleep(10);
					if (flashDelay.elapsed() > 500) {
						flashDelay.reset();
						flash = !flash;
					}
					if (options.looting() && filteredItems != null)
						findLoot();
					/*if (!currentStage.equals("Anti Ban")) {
						if (st != null)
							st.refresh();
						extras();
						if (nextTarget == null || !newTargetIsAttackable(nextTarget)
								|| getLocalPlayer().distance(nextTarget) >= 3)
							nextTarget = getNpcs().closest(getFilteredNPCs("Chicken"));
					}*/
				}
			} catch (Throwable e) {
				running = false;
				log(e.toString());
			}
			log("Thread closed.");
		}
	}
	
	@Override
	public void onStart() {
		log("Welcome to Mob Hunter 0.4");
		
		filteredItems = new ArrayList<Item>();
		getSkillTracker().start();
		randomRunStart = Calculations.random(30, 99);
	}
	
	@SuppressWarnings("unused")
	private State state;

	private enum State {
		Loot, Kill, Wait, Anti, Inventory;
	}

	private State getState() {
		if (Calculations.random(434) == 1 && (antiBanDelay.elapsed() / 1000.0) > 180 && !Instance.getInstance().isMouseInputEnabled()) {
			antiBanDelay.reset();
			return State.Anti;
		}
		Player player = getLocalPlayer();
		Item item = getEquipment().getItemInSlot(EquipmentSlot.ARROWS.getSlot());
		if (item != null) {
			if (item.getID() != ammo) {
				ammo = item.getID();
			}
		} else {
			ammo = 884;
		}
		if (getEquipment().getItemInSlot(EquipmentSlot.ARROWS.getSlot()) == null && getEquipment().getItemInSlot(EquipmentSlot.WEAPON.getSlot()) != null && getEquipment().getItemInSlot(EquipmentSlot.WEAPON.getSlot()).getName().toLowerCase().contains("bow")) {
			//running = false;
			getEquipment().unequip(EquipmentSlot.WEAPON);
			getTabs().open(Tab.INVENTORY);
			getTabs().logout();
			sleep(1000);
			stop();
			//getEquipment().unequip(EquipmentSlot.WEAPON);
		}
		if (filteredItems != null && (target == null || foundLoot != null) && options.looting() && foundLoot() && !player.isInCombat() && !player.isMoving() && !getInventory().isFull()) {
			return State.Loot;
		}
		if (hasFood() && (getInventory().isFull() || getSkills().getBoostedLevels(Skill.HITPOINTS) < 10)) {
			return State.Inventory;
		}
		if (((hasFood() && (getInventory().isFull() || getSkills().getBoostedLevels(Skill.HITPOINTS) < getSkills().getRealLevel(Skill.HITPOINTS) - 6)) 
		|| hasBones() || getInventory().contains(ammo)) && !player.isInCombat() && (nextFoundLoot == null && foundLoot == null || getInventory().isFull())) {
			return State.Inventory;
		}
		if (player.getHealthPercent() >= 20 && player.canAttack() && (!player.isInCombat() && !player.isMoving() || target != null && (target.getHealthPercent() <= 0 || !target.exists()))) {
			return State.Kill;
		}
		return State.Wait;
	}
	
	@Override
	public int onLoop() {
		if (hasStarted) {
			if (options == null)
				options = new GUI(this);
			//thread = new LootThread();
			//thread.start();
			if (!running) {
				thread = new LootThread();
				thread.start();
			}
		}
		if (options != null)
			if (Instance.getInstance().isKeyboardInputEnabled()) {
				if (!options.frame.isVisible())
					options.frame.setVisible(true);
			} else if (options.frame.isVisible())
				options.frame.setVisible(false);
		if (!getLocalPlayer().isOnScreen())
			return 0;
		if (st != null)
			st.refresh();
		//if (getDialogues().inDialogue() && getDialogues().continueDialogue())
			//getDialogues().spaceToContinue();
		extras();
		if (nextTarget == null || !newTargetIsAttackable(nextTarget) || getLocalPlayer().distance(nextTarget) >= 3)
			nextTarget = getNpcs().closest(getFilteredNPCs(mobName));//Chicken Minotaur
		try {
			switch (getState()) {
			case Loot:
				if (foundLoot != null && (getLocalPlayer().distance(foundLoot) <= 2 || !getLocalPlayer().isMoving()) && getMap().canReach(foundLoot.getTile())) {
					if (foundLoot.exists() && !Instance.getInstance().isMouseInputEnabled()) {
						lootingStage = "Grabbing loot";
						if (foundLoot.isOnScreen())
							foundLoot.interactForceRight("Take");
						else
							foundLoot.interact("Take");
						foundLoot = nextFoundLoot;
						nextFoundLoot = null;
						sleep(500);
					}
					if (getLocalPlayer().distance(foundLoot) <= 1 || !foundLoot.exists())
						foundLoot = null;
					//feathersCurrentAmount = getInventory().count("Feather");
					//getCamera().rotateToTile(foundLoot.getTile());

				}
				break;
			case Kill:
				if (target == null && nextTarget != null) {
					if (wasAttackingTarget) {
						killCount++;
						wasAttackingTarget = false;
					}
					if (foundLoot == null && nextFoundLoot == null) {
						target = nextTarget;
						nextTarget = null;
					} else {
						target = null;
					}
				}
				if (target != null) {
					if (target.getHealthPercent() <= 0 || !target.exists() || !getMap().canReach(target.getTile()) || (!target.isInteracting(getLocalPlayer()) && target.isInteractedWith())) {
						if (wasAttackingTarget && target.getHealthPercent() <= 0) {
							lastDeadTargetTile.add(new Tile(target.getX() - 1, target.getY() - 1, target.getZ()));
							if (lastDeadTargetTile.size() > 3)
								lastDeadTargetTile.remove(0);
							killCount++;
							wasAttackingTarget = false;
						}
						target = null;
					} else {
						if (attackDelay.elapsed() > 500 && !Instance.getInstance().isMouseInputEnabled()) {
							if (target != null)
								currentStage = "Attacking " + target.getName();
							target.interact("Attack");
							attackDelay.reset();
							if (Calculations.random(3) == 1)
								getCamera().rotateToEntity(target);
							sleep(500);
						}
					}
				}
				break;
			case Anti:
				antiBan();
				break;
			case Inventory:
				if (Instance.getInstance().isMouseInputEnabled())
					break;
				if (hasFood() && (getInventory().isFull() || getSkills().getBoostedLevels(Skill.HITPOINTS) < getSkills().getRealLevel(Skill.HITPOINTS) - 6)) {
					currentStage = "Eating";
					getInventory().getItemInSlot(getFoodSlot()).interact();
					MethodProvider.sleep(1600, 1900);
				} else if (hasBones()) {
					currentStage = "Burying bones";
					getInventory().getItemInSlot(getBoneSlot()).interact();
					MethodProvider.sleep(600, 900);
				} else if (getInventory().contains(ammo)) {
					getInventory().get(ammo).interact();
					MethodProvider.sleep(600, 900);
				}
				break;
			default:
				if (getLocalPlayer().isInCombat()) {
					currentStage = "Fighting";
					if (nextTarget != null && !Instance.getInstance().isMouseInputEnabled())
						getMouse().move(nextTarget);
				} else {
					if (nextFoundLoot != null && !Instance.getInstance().isMouseInputEnabled())
						getMouse().move(nextFoundLoot);
					else
						currentStage = "Waiting";
				}
				break;
			}
		} catch (Exception e) {
			StackTraceElement[] elements = e.getStackTrace();
			for (int iterator = 1; iterator <= elements.length; iterator++)
				log("Class Name:" + elements[iterator - 1].getClassName() + " Method Name:"
						+ elements[iterator - 1].getMethodName() + " Line Number:"
						+ elements[iterator - 1].getLineNumber());
		}
		return 0;
	}
	
	private boolean hasBones() {
		return getBoneSlot() != -1;
	}
	
	private int getBoneSlot() {
		for (int slot = 0; slot < getInventory().all().size(); slot++) {
			Item item = getInventory().getItemInSlot(slot);
			if (item != null) {
				if (item.hasAction("Bury"))
					return slot;
			}
		}
		return -1;
	}
	
	private boolean hasFood() {
		return getFoodSlot() != -1;
	}
	
	private int getFoodSlot() {
		for (int slot = 0; slot < getInventory().all().size(); slot++) {
			Item item = getInventory().getItemInSlot(slot);
			if (item != null) {
				if (item.hasAction("Eat") || (item.hasAction("Drink") && !item.getName().contains("potion")))
					return slot;
			}
		}
		return -1;
	}
	
	private Filter<NPC> getFilteredNPCs(String name) {
        return npc -> {
            boolean accepted = false;
            if(newTargetIsAttackable(npc) && npc.getName().toLowerCase().contains(name.toLowerCase())){
            	//log("found a chicken");
                accepted = true;
            }
            return accepted;
        };
    }
	
	@SuppressWarnings("unused")
	private Filter<NPC> getFilteredNPCs(int id) {
        return npc -> {
            boolean accepted = false;
            if(newTargetIsAttackable(npc) && npc.getID() == id){
            	//log("found a chicken");
                accepted = true;
            }
            return accepted;
        };
    }
	
	public boolean newTargetIsAttackable(NPC npc) {
		return (npc != null && inSpecificArea(npc) && !npc.isInCombat() && !npc.isInteractedWith() && npc.exists() && getMap().canReach(npc.getTile()) && npc.getHealthPercent() > 0 && npc != target);
	}
	
	public boolean inSpecificArea(NPC npc) {
		if (npc == null || npc.getY() < 3289 && npc.getName().contains("Chicken"))
			return false;
		return true;
	}
	
	public void antiBan() {
		int Anti1 = Calculations.random(5);
		currentStage = "Anti Ban";
		switch (Anti1) {
		case 1:
			getTabs().open(Tab.SKILLS);
			sleep(Calculations.random(1240, 4500));
			if ((Calculations.random(0, 4) == 1)) {
				if (Calculations.random(1, 2) == 1)
					getTabs().open(Tab.QUEST);
				else
					getTabs().open(Tab.EQUIPMENT);
			}
			getTabs().open(Tab.INVENTORY);
			break;
		default:
			getMouse().moveMouseOutsideScreen();
			sleep(Calculations.random(3040, 12500));
			break;
		}
		antiBanDelay.reset();
	}
	
	public void extras() {
		if (getLocalPlayer().isInCombat()) {
			target = (NPC) getLocalPlayer().getCharacterInteractingWithMe();
			wasAttackingTarget = true;
			attackDelay.reset();
		}
		if (getWalking().getRunEnergy() >= randomRunStart && !getWalking().isRunEnabled()) {
			randomRunStart = Calculations.random(30, 99);
			getWalking().toggleRun();
		}
		if (!started && getLocalPlayer().isOnScreen()) {
			started = true;
			st = new SkillTracking(this);
		}
	}
	
	@SuppressWarnings("unused")
	private Filter<GroundItem> groundItem(int id) {
		return groundItem -> {
			boolean accepted = false;
			if (groundItem.getID() == id && getLocalPlayer().distance(groundItem) >= 1 && getLocalPlayer().distance(groundItem) <= 5/* && groundItem.isOnScreen()*/ && getMap().canReach(groundItem.getTile()) && groundItem.getY() >= 3289 && (foundLoot == null || foundLoot != null && groundItem.getTile().getX() != foundLoot.getTile().getX() && groundItem.getTile().getY() != foundLoot.getTile().getY())) {
				accepted = true;
			}
			return accepted;
		};
	}
	
	public boolean isIronmanLoot(GroundItem groundItem) {
		for (Tile tile : lastDeadTargetTile)
			if (groundItem.getTile().getX() == tile.getX() && groundItem.getTile().getY() == tile.getY())
				return true;
		return false;
	}
	
	public boolean isIDKIronmanLoot(GroundItem groundItem) {
		boolean idk = true;
		for (Tile tile : lastDeadTargetTile)
			if (groundItem.getTile().getX() == tile.getX() && groundItem.getTile().getY() == tile.getY())
				idk = false;
		return idk;
	}
	
	private Filter<GroundItem> groundItemsInclusive(int... ids) {// pick up specific items
		return groundItem -> {
			boolean accepted = false;
			boolean isClose = false;
			if (foundLoot != null) {
				if (foundLoot.distance(groundItem) >= 0 && foundLoot.distance(groundItem) <= options.getDistance())
					isClose = true;
			} else if (getLocalPlayer().distance(groundItem) >= 0 && getLocalPlayer().distance(groundItem) <= options.getDistance())
				isClose = true;
			
			if (isClose && groundItem.isOnScreen() && getMap().canReach(groundItem.getTile()) && (foundLoot == null || foundLoot != null /*&& groundItem.getTile().getX() != foundLoot.getTile().getX() && groundItem.getTile().getY() != foundLoot.getTile().getY()*/ && groundItem.getID() != foundLoot.getID())) {
				for (Item item : filteredItems) {
					if (!options.isIronman() && groundItem.getID() == item.getID() || options.isIronman() && lastDeadTargetTile.size() > 0 && isIronmanLoot(groundItem) && groundItem.getID() == item.getID())
						accepted = true;
				}
				/*for (int id : ids) {
					if (groundItem.getID() == id)
						accepted = true;
				}*/
			}
			return accepted;
		};
	}
	
	private Filter<GroundItem> groundItemsExclusive(int... ids) {// don't pick up specific items
		return groundItem -> {
			boolean accepted = false;
			boolean isClose = false;
			//lastDeadTargetTile
			if (foundLoot != null) {
				if (foundLoot.distance(groundItem) >= 0 && foundLoot.distance(groundItem) <= options.getDistance())
					isClose = true;
			} else if (getLocalPlayer().distance(groundItem) >= 0 && getLocalPlayer().distance(groundItem) <= options.getDistance())
				isClose = true;
			
			if (isClose/* && groundItem.isOnScreen()*/ && getMap().canReach(groundItem.getTile()) && (foundLoot == null || foundLoot != null /*&& groundItem.getTile().getX() != foundLoot.getTile().getX() && groundItem.getTile().getY() != foundLoot.getTile().getY()*/ && groundItem.getID() != foundLoot.getID())) {
				Boolean isBlackListed = true;
				/*
				1: 10, 13
				2: 16, 14
				3: 7, 4
				
				2: 16, 14
				3: 7, 4
				*/
				for (Item item : filteredItems) {
					if (groundItem.getID() == item.getID() || options.isIronman() && isIDKIronmanLoot(groundItem))
						isBlackListed = false;
				}
				/*for (int id : ids) {
					if (groundItem.getID() == id)
						isExclusive = false;
				}*/
				if (isBlackListed)
					accepted = true;
			}
			return accepted;
		};
	}
	
	public boolean foundLoot() {
		if (foundLoot != null) {
			if (!foundLoot.exists())
				foundLoot = null;
			else
				return true;
		}
		return false;
	}
	
	
	public void findLoot() {
		if (nextFoundLoot != null) {
			if (!nextFoundLoot.exists())
				nextFoundLoot = null;
		}
		if (nextFoundLoot == null) {
			if (!currentStage.equals("Anti Ban"))
				lootingStage = "Searching for new loot";
			//GroundItem loot = getGroundItems().closest(groundItemsExclusive(9007, 9008, 9009, 9010, 9011, 1155, 1205));//feather
			GroundItem loot = getGroundItems().closest(options.getTglbtnFilter().isSelected() ? groundItemsExclusive(1): groundItemsInclusive(1));
			if (loot != null) {
				lootingStage = "Found loot.";
				nextFoundLoot = loot;
			}
		}
		if (foundLoot == null && nextFoundLoot != null) {
			foundLoot = nextFoundLoot;
			nextFoundLoot = null;
		}
		if (foundLoot != null && nextFoundLoot != null) {
			if (getLocalPlayer().distance(nextFoundLoot) < getLocalPlayer().distance(foundLoot)) {
				GroundItem loot = foundLoot;
				foundLoot = nextFoundLoot;
				nextFoundLoot = loot;
			}
		}
	}

	public String getMobName() {
		return mobName;
	}

	public void setMobName(String mobName) {
		this.mobName = mobName;
	}

	public void onPaint(Graphics2D g) {
		//Instance.getInstance().isMouseInputEnabled();
		int x = 25;
		//g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", 1, 11));
		Utilities.drawShadowString(g, "Time Running: " + totalTime.formatTime(), x, 50);
		//g.drawString("Time Running: " + totalTime.formatTime(), x, 50);
		if ((getWidgets().getWidgetChild(162, 5) != null && getWidgets().getWidgetChild(162, 5).isVisible())) {
			if (!hasStarted)
				hasStarted = true;
			Utilities.drawShadowString(g, "Stage: " + currentStage + " | " + lootingStage, x, 60);
			Utilities.drawShadowString(g, "Kill Count: " + Utilities.insertCommas(killCount), x, 70);
			Utilities.drawShadowString(g, "Found Loot: " + (foundLoot != null ? foundLoot.getName() : "none"), x + 80, 70);
			/*for (int i = 0; i < lastDeadTargetTile.size(); i++)
				Utilities.drawShadowString(g, "X: " + lastDeadTargetTile.get(i).getX() + ", Y: " + lastDeadTargetTile.get(i).getY(), x + 250, 70 + (i * 10));*/
			/*g.drawString("Stage: " + currentStage + " | " + lootingStage, x, 60);
			g.drawString("Kill Count: " + Utilities.insertCommas(killCount), x, 70);
			g.drawString("Found Loot: " + (foundLoot != null ? foundLoot.getName() : "none"), x + 80, 70);*/
			/*String groundItemsS = "";
			for (GroundItem gi : getGroundItems().all(groundItemsExclusive(9007, 9008, 9009, 9010, 9011, 1155, 1205))) {
				groundItemsS += "(" + gi.getName() + " - X: " + gi.getX() + ", Y: " + gi.getY() + "), ";
			}
			g.drawString("Ground Items: " + groundItemsS, x, 80);*/
			st.onPaint(g, getSkills(), totalTime, x);
			/*long[] skills = st.getSkillsTimers();
			int needToDisplay = 0;
			for (int i = 0; i < skills.length; i++) {
				long decay = System.currentTimeMillis() - skills[i];
				if (decay <= 6000 && totalTime.elapsed() >= 4000) {
					Skill skill = Skill.forId(i);
					int level = getSkills().getRealLevel(skill);
					int xpNeeded = Utilities.getXPForLevel(level + 1) - Utilities.getXPForLevel(level);
					int xp = getSkills().getExperience(skill) - Utilities.getXPForLevel(level);
					int alpha = (decay > 5000 ? (int) (255 - ((100.0 / 1000.0 * (decay - 5000)) / 100.0 * 255)) : 255);
					Utilities.drawSkillProgress(g, x - 1, 72 + (needToDisplay * 15) + (needToDisplay * 3), xp, xpNeeded, skill.getId(), level, alpha);
					needToDisplay++;
				}
			}*/
			if (target != null) {
				if (target.getHealthPercent() > 0)
					g.setColor(Color.decode("#00FF00"));//green
				else
					g.setColor(Color.RED);
				g.drawPolygon(target.getTile().getPolygon());
			}
			if (nextTarget != null) {
				if (flash)
					g.setColor(Color.decode("#82FF82"));//light green
				else
					g.setColor(Color.decode("#E6FFE6"));//lighter green
				g.drawPolygon(nextTarget.getTile().getPolygon());
			}
			if (foundLoot != null) {
				g.setColor(Color.decode("#9700FF"));//purple
				g.drawPolygon(foundLoot.getTile().getPolygon());
			}
			if (nextFoundLoot != null) {
				if (flash)
					g.setColor(Color.decode("#CB81FF"));//light purple
				else
					g.setColor(Color.decode("#F5E6FF"));//lighter purple
				g.drawPolygon(nextFoundLoot.getTile().getPolygon());
			}
			ammoItem = getEquipment().getItemInSlot(EquipmentSlot.ARROWS.getSlot());
			if (ammoItem == null)
				itemImage = null;
			else if (ammoItem != null && (itemImage == null || id != ammoItem.getID())) {
				try {
					id = ammoItem.getID();
					itemImage = ImageIO.read(new URL("https://www.osrsbox.com/osrsbox-db/items-icons/" + ammoItem.getID() + ".png"));
				} catch (IOException e) {
					log(e.getMessage());
				}
			}
			if (itemImage != null) {
				Utilities.drawItem(g, itemImage, ammoItem.getID(), ammoItem.getAmount(), 350, 6);
			}
		} else {
			Utilities.drawShadowString(g, "Stage: Waiting to login", x, 60);
			//g.drawString("Stage: Waiting to login", x, 60);
		}
	}
	
	@Override
	public void onExit() {
		running = false;
		options.frame.setVisible(false);
		options.frame.dispose();
		options = null;
		log("Bye Bye!");
	}
	
}
