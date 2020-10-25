package TaskManager.scripts.misc;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.walking.web.node.impl.bank.WebBankArea;
import org.dreambot.api.script.Category;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;

import TaskManager.Script;
import TaskManager.ScriptDetails;
import TaskManager.utilities.Utilities;

@ScriptDetails(author = "NumberZ", category = Category.MISC, name = "Wool Shearer", version = 1.0, description = "Shears the sheep at the lumbridge farms.")
public class WoolShearer extends Script {
	private Area FARMERS_HOUSE = new Area(3188, 3274, 3191, 3271, 0);
	private Area SHEEP_AREA = new Area(3194, 3275, 3212, 3258, 0);
	private Area CASTLE = new Area(3205, 3228, 3216, 3208, 0);
	private int SHEARS = 1735;
	private String state;
	private int bankStatus = -1;

	public WoolShearer() {
		supportedConditions.add(TaskManager.Condition.Time);
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public int onLoop() {
		if (!getLocalPlayer().isOnScreen())
			return 0;
		if (!Inventory.contains(SHEARS)) {
			grabShears();
		} else if (Inventory.contains(SHEARS)) {
			if (Inventory.isFull()) {
				handleBanking();
			} else {
				handleShearing();
			}
		}
		return 0;
	}

	public boolean inAreaIgnoreZ(Area a, Tile b) {
		a.setZ(b.getZ());
		return a.contains(b);
	}
	
	public void handleBanking() {
		Area bank = WebBankArea.LUMBRIDGE.getArea();
		bank.setZ(2);
		if (inAreaIgnoreZ(CASTLE, getLocalPlayer().getTile())) {
			if (bank.contains(getLocalPlayer())) {
				if (!Bank.isOpen()) {
					Bank.openClosest();
					sleepUntil(() -> Bank.isOpen(), Calculations.random(3000, 5000));
				} else {
					state = "Banking";
					if (!Inventory.onlyContains(SHEARS) && !Inventory.isEmpty()) {
						Bank.depositAllExcept(SHEARS);
						sleepUntil(() -> Inventory.onlyContains(SHEARS) || Inventory.isEmpty(), Calculations.random(3000, 5000));
					}
					if (!Inventory.contains(SHEARS) && Bank.contains(SHEARS)) {
						Bank.withdraw(SHEARS);
						sleepUntil(() -> Inventory.contains(SHEARS), Calculations.random(3000, 5000));
					}
					if (Bank.contains(SHEARS)) {
						bankStatus = 1;
					} else {
						bankStatus = 0;
					}
				}
			} else {
				state = "Going to bank";
				Walking.walk(bank.getCenter());
				sleepUntil(() -> Walking.getDestination().distance(getLocalPlayer()) < 6, 6000);
			}
		} else {
			state = "Going to bank";
			Walking.walk(new Tile(3208, 3210, 0));//lumbridge castle
			sleepUntil(() -> Walking.getDestination().distance(getLocalPlayer()) < 6, 6000);
		}
	}
	
	public void handleShearing() {
		if (inAreaIgnoreZ(CASTLE, getLocalPlayer().getTile()) && getLocalPlayer().getZ() > 1) {
			if (getLocalPlayer().getZ() == 2) {
				state = "Going to sheep";
				if (Bank.isOpen()) {
					Bank.close();
					sleepUntil(() -> !Bank.isOpen(), Calculations.random(3000, 5000));
				} else {
					Walking.walk(SHEEP_AREA.getCenter());
					sleepUntil(() -> Walking.getDestination().distance(getLocalPlayer()) < 6, 6000);
				}
			}
		} else if (SHEEP_AREA.contains(getLocalPlayer())) {
			state = "Shearing the sheep";
			NPC sheep = NPCs.closest(npcFilter());
			if (sheep != null) {
				Camera.mouseRotateToEntity(sheep);
				int id = sheep.getID();
				sheep.interactForceRight("Shear");
				sleepUntil(() -> getLocalPlayer().isAnimating() || sheep.getID() != id, 6000);
				sleepUntil(() -> !getLocalPlayer().isAnimating(), 6000);
			}
		} else {
			state = "Going to sheep";
			Walking.walk(SHEEP_AREA.getCenter());
			sleepUntil(() -> Walking.getDestination().distance(getLocalPlayer()) < 6, 6000);
		}
	}
	
	public void grabShears() {
		if (CASTLE.getCenter().distance(getLocalPlayer()) < FARMERS_HOUSE.getCenter().distance(getLocalPlayer()) && (bankStatus == -1 || bankStatus == 1)) {
			state = "Checking bank for shears";
			handleBanking();
		} else {
			state = "Getting a pair of shears";
			if (FARMERS_HOUSE.contains(getLocalPlayer())) {
				GroundItem shears = GroundItems.closest(SHEARS);
				if (shears != null && shears.exists()) {
					shears.interactForceRight("Take");
					sleepUntil(() -> Inventory.contains(SHEARS), Calculations.random(3000, 5000));
				}
			} else {
				if (Bank.isOpen()) {
					Bank.close();
					sleepUntil(() -> !Bank.isOpen(), Calculations.random(3000, 5000));
				}
				Walking.walk(FARMERS_HOUSE.getCenter());
				sleepUntil(() -> Walking.getDestination().distance(getLocalPlayer()) < 6, 6000);
			}
		}
	}
	
	private Filter<NPC> npcFilter() {//Sheep
		return npc -> {
			boolean accepted = false;
			if (npc != null && (npc.getID() == 2693 || npc.getID() == 2694 || npc.getID() == 2699 || npc.getID() == 2786 || npc.getID() == 2787) && Map.canReach(npc) && SHEEP_AREA.contains(npc)) {
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
		super.onExit();
	}
}
