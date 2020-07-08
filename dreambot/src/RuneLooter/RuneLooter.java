package RuneLooter;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.dreambot.api.methods.MethodProvider;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.world.World;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.core.Instance;

import MobHunter.Utilities;

@ScriptManifest(author = "NumberZ", name = "Rune Looter", version = 1.0, description = "Picks up runes off the ground and switches to different worlds.", category = Category.MONEYMAKING)
public class RuneLooter extends AbstractScript {
	private Timer totalTime = new Timer();
	private Timer hopTimer = new Timer();
	private int paintX = 25;
	private boolean running;
	public static Thread thread;
	private boolean hasStarted = false;
	public GroundItem foundLoot = null;
	public String currentStage = "None";
	int worldIndex = 0;
	List<World> worlds = new ArrayList<World>();

	public class LootThread extends Thread {
		public void run() {
			try {
				running = true;
				while (running) {
					Thread.sleep(100);
					findLoot();
				}
			} catch (Throwable e) {
				running = false;
				log(e.toString());
			}
			log("Thread closed.");
		}
	}
	
	public void findLoot() {
		if (!(getWidgets().getWidgetChild(162, 5) != null && getWidgets().getWidgetChild(162, 5).isVisible()))
			return;
		if (foundLoot != null)
			return;
		if (foundLoot != null && !foundLoot.exists()) {
			foundLoot = null;
			return;
		}
		GroundItem loot = getGroundItems().closest("Cosmic rune");
		foundLoot = loot;
	}
	
	@SuppressWarnings("unused")
	private State state;

	private enum State {
		Loot, Eat, WorldHop, Logout, Run;
	}
	
	private State getState() {
		if (getLocalPlayer().isInteractedWith())
			return State.Run;
		if (!hasFood())
			return State.Logout;
		if (foundLoot == null && hopTimer.elapsed() > 2000)
			return State.WorldHop;
		int healthPercent = (getSkills().getBoostedLevels(Skill.HITPOINTS) * 100) / (getSkills().getRealLevel(Skill.HITPOINTS));
		if (healthPercent < 80 && hasFood())
			return State.Eat;
		return State.Loot;
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
	
	class SortbyWorld implements Comparator<World> 
	{ 
	    // Used for sorting in ascending order of 
	    // roll number 
	    public int compare(World a, World b) 
	    { 
	        return a.getWorld() - b.getWorld(); 
	    } 
	} 
	
	@Override
	public void onStart() {//runs this once at the very beginning
		log("Welcome to " + getManifest().name() + " 1.0");
		for (int i = 0; i < getWorlds().f2p().size(); i++) {
			World world = getWorlds().f2p().get(i);
			if (world.isBountyWorld() || world.isDeadmanMode() || world.isMembers() || world.isLastManStanding() || world.getMinimumLevel() > 1 || world.isTwistedLeague() || world.isTournamentWorld())
				continue;
			worlds.add(world);
		}
		Collections.sort(worlds, new SortbyWorld());
	}
	
	@Override
	public int onLoop() {//run every single time
		if (hasStarted) {
			if (!running) {
				thread = new LootThread();
				thread.start();
			}
		}
		if (!(getWidgets().getWidgetChild(162, 5) != null && getWidgets().getWidgetChild(162, 5).isVisible()))
			return 0;
		switch (getState()) {
		case Loot:
			if (foundLoot != null && (getLocalPlayer().distance(foundLoot) <= 2 || !getLocalPlayer().isMoving()) && getMap().canReach(foundLoot.getTile())) {
				if (foundLoot.exists() && !Instance.getInstance().isMouseInputEnabled()) {
					if (foundLoot.isOnScreen())
						foundLoot.interactForceRight("Take");
					else
						foundLoot.interact("Take");
					foundLoot  = null;
					sleep(500);
				}
				if (getLocalPlayer().distance(foundLoot) <= 1 || !foundLoot.exists())
					foundLoot = null;

			}
			break;
		case WorldHop:
			int current = getClient().getCurrentWorld();
			World nextWorld = getNextWorldIndex(current);
			getWorldHopper().hopWorld(nextWorld);
			break;
		case Eat:
			getInventory().getItemInSlot(getFoodSlot()).interact();
			MethodProvider.sleep(1600, 1900);
			break;
		default:
			break;
		}
		return 0;
	}
	
	World getNextWorldIndex(int current) {
		int nextWorld = current;
		World world = null;
		while (current == nextWorld) {
			worldIndex++;
			if (worldIndex >= worlds.size())
				worldIndex = 0;
			world = worlds.get(worldIndex);
			nextWorld = world.getWorld();
		}
		return world;
	}
	
	@Override
	public void onExit() {//runs this once after stopping your script

	}

	public void onPaint(Graphics2D g) {
		g.setFont(new Font("Arial", 1, 11));
		Utilities.drawShadowString(g, "Time Running: " + totalTime.formatTime(), paintX, 50);
		if (!(getWidgets().getWidgetChild(162, 5) != null && getWidgets().getWidgetChild(162, 5).isVisible())) {
			Utilities.drawShadowString(g, "Stage: Waiting to login", paintX, 60);
			hopTimer.reset();
			return;
		}
		if (!hasStarted)
			hasStarted = true;
		Utilities.drawShadowString(g, "Stage: " + getState().toString(), paintX, 60);
		if (foundLoot != null) {
			g.setColor(Color.decode("#9700FF"));//purple
			g.drawPolygon(foundLoot.getTile().getPolygon());
		}
	}
	
}
