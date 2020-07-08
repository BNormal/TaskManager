package SilverMiner;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.MethodProvider;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.utilities.impl.Condition;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import MobHunter.SkillTracking;
import MobHunter.Utilities;

@ScriptManifest(author = "NumberZ", description = "Mines and crafts silver into unholy symbols", name = "Silver Miner", version = 1, category = Category.MINING)

public class SilverMiner extends AbstractScript {

	private Timer totalTime = new Timer();
	public String currentStage = "unavailable";
	public int minedCount = 0;
	private SkillTracking st;
	private boolean started = false;
	private int randomRunStart = 50;
	WidgetChild child1 = null;
	
	private static final Area furnaceArea = new Area(3274, 3184, 3276, 3187, 0);
	private static final Area bankArea = new Area(3267, 3164, 3271, 3170, 0);
	private static final Area mineArea = new Area(3291, 3283, 3304, 3316, 0);
	private static final int[] picks = new int[] {};
	private static final int furnaceId = 24009;

	JEWELLRY_TYPE ourJewellry = JEWELLRY_TYPE.SYMBOL;
	boolean justGotHere;

	public GameObject getRock() {
		MethodProvider.log("Looking for rock");
		int[] ids;
		int count = 0;
		for (@SuppressWarnings("unused") int id : ourJewellry.metal.rockIds)
			count += 1;

		ids = new int[count];
		count = 0;
		for (int id : ourJewellry.metal.rockIds) {
			ids[count] = id;
			count += 1;
		}
		return getGameObjects().closest(new Filter<GameObject>() {
			@Override
			public boolean match(GameObject obj) {
				for (int id : ids)
					if (obj.getID() == id)
						return true;
				return false;
			}

		});
	}

	@Override
	public void onStart() {
		getSkillTracker().start();

	}

	@Override
	public int onLoop() {
		runExtraFunctions();
		if (getInventory().isFull()) {
			if (getInventory().contains(ourJewellry.metal.oredId) || getInventory().contains(ourJewellry.metal.metalId)) {
				if (furnaceArea.contains(getLocalPlayer())) {
					if (getLocalPlayer().isAnimating()) {
						return 600;
					} else {
						if (justGotHere) {
							if (MethodProvider.sleepUntil(new Condition() {
								@Override
								public boolean verify() {
									return getLocalPlayer().isAnimating();
								}
							}, 2200)) {
								MethodProvider.log("okay now we're animating");
							} else {
								MethodProvider.log("waited, still not animating.");
								justGotHere = false;
							}
						} else {
							if (getInventory().contains(ourJewellry.metal.oredId)) {
								getGameObjects().closest(furnaceId).interact("Smelt");
								sleepUntil(new Condition() {//checks to make sure smelting dialog is opened
									@Override
									public boolean verify() {
										child1 = getWidgets().getWidgetChild(270, 16);
										return child1 != null && child1.isVisible();
									}
								}, Calculations.random(4000, 7500));
								getKeyboard().type("" + ourJewellry.metal.keyId, false);
								MethodProvider.sleepUntil(new Condition() {
									@Override
									public boolean verify() {
										return getLocalPlayer().isAnimating();
									}
								}, 2200);
							} else if (getInventory().contains(ourJewellry.metal.metalId)) {
								getInventory().get(ourJewellry.metal.metalId).useOn(getGameObjects().closest(furnaceId));
								sleepUntil(new Condition() {//checks to make sure smelting dialog is opened
									@Override
									public boolean verify() {
										child1 = getWidgets().getWidgetChild(6, 0);
										return child1 != null && child1.isVisible();
									}
								}, Calculations.random(4000, 7500));
								getWidgets().getWidget(6).getChild(6).getChild(ourJewellry.childId).interact("Craft all");
								MethodProvider.sleepUntil(new Condition() {
									@Override
									public boolean verify() {
										return getLocalPlayer().isAnimating();
									}
								}, 2200);
							}
							justGotHere = true;
						}

					}
				} else {
					Tile dest = getWalking().getDestination();
					if (dest == null || dest.distance() < 4 || dest.distance() > 20)
						getWalking().walk(furnaceArea.getRandomTile());
					justGotHere = true;
				}

			} else {
				//Banking procedure
				if (getLocalPlayer().isAnimating()) {
					return 600;
				} else if (bankArea.contains(getLocalPlayer())) {
					if (getBank().isOpen()) {
						getBank().depositAllExcept(new Filter<Item>() {
							@Override
							public boolean match(Item arg0) {
								for (int i : picks) {
									if (i == arg0.getID())
										return true;
								}
								if (arg0.getID() == ourJewellry.moldId)
									return true;
								return false;
							}
						});
						MethodProvider.sleep(600, 1200);
					} else {
						getBank().open();
						MethodProvider.sleepUntil(new Condition() {
							@Override
							public boolean verify() {
								return getBank().isOpen();
							}
						}, 4000);
					}
				} else {
					Tile dest = getWalking().getDestination();
					if (dest == null || dest.distance() < 4 || dest.distance() > 20)
						getWalking().walk(bankArea.getRandomTile());
				}
			}
		} else {//happens if inventory isn't full
			if (mineArea.contains(getLocalPlayer())) {
				if (getRock() != null && !getLocalPlayer().isAnimating()) {
					GameObject rock = getRock();
					if (rock.interact("Mine")) {
						MethodProvider.sleepUntil(new Condition() {
							@Override
							public boolean verify() {
								return getLocalPlayer().isAnimating();
							}
						}, 4000);
					} else {
						MethodProvider.log("Failed interaction");
					}

				}
			} else {// walks to mining area
				Tile dest = getWalking().getDestination();
				if (dest == null || dest.distance() < 4 || dest.distance() > 20)
					getWalking().walk(mineArea.getRandomTile());
			}
		}
		return 0;
	}


	public void runExtraFunctions() {
		if (st != null)
			st.refresh();
		if (getWalking().getRunEnergy() >= randomRunStart && !getWalking().isRunEnabled()) {
			randomRunStart = Calculations.random(30, 99);
			getWalking().toggleRun();
		}
		if (!started && getLocalPlayer().isOnScreen()) {
			started = true;
			st = new SkillTracking(this);
		}
	}
	
	public void onPaint(Graphics2D g) {
		int x = 25;
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", 1, 11));
		g.drawString("Time Running: " + totalTime.formatTime(), x, 50);
		if ((getWidgets().getWidgetChild(162, 5) != null && getWidgets().getWidgetChild(162, 5).isVisible())) {
			g.drawString("Stage: " + currentStage, x, 60);
			//g.drawString("Mined Count: " + Utilities.insertCommas(minedCount), x, 70);

			long[] skills = st.getSkillsTimers();
			int needToDisplay = 0;
			for (int i = 0; i < skills.length; i++) {
				long decay = System.currentTimeMillis() - skills[i];
				if (decay <= 6000 && totalTime.elapsed() >= 4000) {
					Skill skill = Skill.forId(i);
					int level = getSkills().getRealLevel(skill);
					int xpNeeded = Utilities.getXPForLevel(level + 1) - Utilities.getXPForLevel(level);
					int xp = getSkills().getExperience(skill) - Utilities.getXPForLevel(level);
					int alpha = (decay > 5000 ? (int) (255 - ((100.0 / 1000.0 * (decay - 5000)) / 100.0 * 255)) : 255);
					st.drawSkillProgress(g, x - 1, 72 + (needToDisplay * 15) + (needToDisplay * 3), xp, xpNeeded, skill.getId(), level, alpha);
					needToDisplay++;
				}
			}
		} else {
			g.drawString("Stage: Waiting to login", x, 60);
		}
	}
}


enum ORES {
	GOLD(new int[] { 7457 }, 442, 5, 2366), SILVER(new int[] { 7457, 7490, }, 442, 3, 2355);
	int metalId;
	int[] rockIds;
	int oredId;
	Tile[][] spots;
	int keyId;

	ORES(int[] rockIds, int oreId, int keyId, int metalId) {
		this.rockIds = rockIds;
		this.oredId = oreId;
		this.keyId = keyId;
		this.metalId = metalId;
	}
}

enum JEWELLRY_TYPE {
	SYMBOL(ORES.SILVER, 1599, 1, 0), 
	EMBLEM(ORES.SILVER, 1, 1, 1), 
	TIARA(ORES.SILVER, 1, 1, 1), 
	GOLD_RING(ORES.SILVER, 1, 1, 1), 
	GOLD_NECKLACE(ORES.SILVER, 1, 1, 1), 
	GOLD_BRACELET(ORES.SILVER, 1, 1, 1), 
	GOLD_AMULET(ORES.SILVER, 1, 1, 1);
	
	ORES metal;
	int moldId;
	int jewellryId;
	int childId;

	JEWELLRY_TYPE(ORES metal, int moldId, int jewellryId, int childId) {
		this.metal = metal;
		this.moldId = moldId;
		this.jewellryId = jewellryId;
		this.childId = childId;
	}

}
