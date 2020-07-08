package WineGrabber;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.net.InetAddress;
import java.util.GregorianCalendar;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.utilities.impl.Condition;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.core.Instance;

import JewelrySmelter.Utilities;

@ScriptManifest(author = "NumberZ", category = Category.MONEYMAKING, name = "Wine Tele Grabber", version = 1.0, description = "Tele-grabs wines of zamorak.")
public class WineGrabber extends AbstractScript {
	
	private int ping = -1;
	private int TRADE_ITEM = 245;
	private Timer totalTime = new Timer();
	private Timer pingTimer = new Timer();
	private Timer antiBanDelay = new Timer();
	private String username;
	private boolean started = false;
	private boolean running = false;
	private int tradeWithEmptySlots = 0;
	int moveMouse = 0;
	GameObject table;
	GroundItem wine = null;
	Widget levelUp = null;
	private GUI gui;
	private String state = "";
	private int wines = 0;
	private int lawRunes = 0;
	private int casts = 0;
	public PaintThread thread;
	private boolean runningThread = false;
	
	@Override
	public void onStart() {
		log("Welcome to Wine Tele Grabber 1.0");
		setGui(new GUI(this));
		ping = pingTestWorld(getClient().getCurrentWorld());
		getMouse().getMouseSettings().setWordsPerMinute(80);
		wines = getItemCount(TRADE_ITEM + 1) + getItemCount(TRADE_ITEM);
		lawRunes = getItemCount("Law rune") - 1;
		casts = (getInventory().emptySlotCount() - tradeWithEmptySlots);
	}
	
	@Override
	public int onLoop() {
		if (!started && getLocalPlayer().isOnScreen()) {
			started = true;
		}
		if (!started || !running) {
			return 0;
		}
		if (!runningThread) {
			thread = new PaintThread();
			thread.start();
		}
		if (Instance.getInstance().isMouseInputEnabled())
			return 0;
		switch (getBotState()) {
		
		/*
		 * Handles the player moving up and down the ladder
		 */
		
		case MOVING:
			GameObject ladder = getGameObjects().closest("Ladder");
			ladder.interact();
			if (getLocalPlayer().getZ() == 1) {
				sleepUntil(() -> getLocalPlayer().getZ() == 0, Calculations.random(3000, 4500));
			} else {
				sleepUntil(() -> getLocalPlayer().getZ() == 1, Calculations.random(3000, 4500));
			}
			break;
			
		/*
		 * Handles how the player will grab wines of zamorak either by
		 * grabbing it manually or using the tele-grab spell to grab it
		 * 
		 */
			
		case TELEGRABBING:
			int playerCount = getPlayers().all().size();
			int npcCount = getNpcs().all().size();
			boolean noTeleGrab = false;
			if (playerCount > 1 && npcCount == 0 && getSkills().getBoostedLevels(Skill.MAGIC) > 36 && getSkills().getBoostedLevels(Skill.HITPOINTS) > 12) {
				noTeleGrab = true;
			}
			wine = getGroundItems().closest(TRADE_ITEM);
			if (table == null) {
				// finds the table
				table = getGameObjects().getTopObjectOnTile(new Tile(2938, 3517, 1));
			}
			if (wine != null && wine.exists() && wine.isOnScreen() && wine.getX() == 2938 && wine.getY() == 3517 && wine.getZ() == 1) {
				if(noTeleGrab) {
					// Grabs wine with no spell (only when other player is around and monk is dead)
					//wine.interact();
					if (getMagic().isSpellSelected())
						getMouse().click(new Point((int) table.getBoundingBox().getCenterX() + Calculations.random(0, 5), (int) table.getBoundingBox().getCenterY() + -30 + Calculations.random(0, 5)));
					getMouse().click(new Point((int) table.getBoundingBox().getCenterX() + Calculations.random(0, 5), (int) table.getBoundingBox().getCenterY() - 5 + Calculations.random(0, 5)));
				} else {
					// Grabs wine with spell (no players around)
					if (!getMagic().isSpellSelected()) {
						getMagic().castSpell(Normal.TELEKINETIC_GRAB);
						sleepUntil(() -> getMagic().isSpellSelected(), Calculations.random(5000, 7500));
					}
					if (playerCount <= 1 && Calculations.random(1, 10) == 2)
						sleep(Calculations.random(500, 4500));
					getMouse().click(new Point((int) table.getBoundingBox().getCenterX() + Calculations.random(0, 5), (int) table.getBoundingBox().getCenterY() - 5 + Calculations.random(0, 5)));
;					//getMagic().castSpellOn(Normal.TELEKINETIC_GRAB, wine);
				}
				sleepUntil(new Condition() {
					public boolean verify() {
						GroundItem item = getGroundItems().closest(TRADE_ITEM);
						return item == null || !item.isOnScreen() || item.getX() != 2938 || item.getY() !=3517 || item.getZ() != 1;
					}
				}, Calculations.random(2000, 3200));
				sleepUntil(() -> getGroundItems().closest(TRADE_ITEM) == null, Calculations.random(5000, 7500));
				sleep(Calculations.random(300, 500));
			} else if (!getMagic().isSpellSelected() && !noTeleGrab) {
				// Selects the tele-grab spell
				getMagic().castSpell(Normal.TELEKINETIC_GRAB);
				moveMouse = 0;
			} else if (getMagic().isSpellSelected() || noTeleGrab) {
				try {
					
					// De-selects the tele-grab spell when there's another player around and monk is dead
					if (noTeleGrab && getMagic().isSpellSelected()) {
						getMouse().click(new Point((int) table.getBoundingBox().getCenterX() + Calculations.random(0, 30), (int) table.getBoundingBox().getCenterY() - 30 + Calculations.random(0, 20)));
						//getMagic().castSpell(Normal.TELEKINETIC_GRAB);
						moveMouse = 0;
					}
					
					// moves the cursor on top the wine table
					if (table != null) {
						if (moveMouse < 2) {
							moveMouse++;
							getMouse().move(new Point((int) table.getBoundingBox().getCenterX() + Calculations.random(0, 5), (int) table.getBoundingBox().getCenterY() - 5 + Calculations.random(0, 5)));
							//getMouse().move(table);
						}
					} else if (Calculations.random(1000) == 1) {
						moveMouse = Calculations.random(1, 10) * -1;
					}
				} catch (Exception e) {
					log(e.getMessage());
				}
			}
			break;
			
		/*
		 * Handles the trading wines of zamorak to the mule
		 */
			
		case TRADING:
			if (getTrade().isOpen()) {
				if (getTrade().isOpen(1)) {//Trade Screen 1
					if (getInventory().contains(TRADE_ITEM)) {
						getTrade().addItem(TRADE_ITEM, getInventory().count(TRADE_ITEM));
					} else {
						getTrade().acceptTrade();
						sleepUntil(new Condition() {
							public boolean verify() {
								return getTrade().isOpen(2);
							}
						}, Calculations.random(2000, 3200));
					}
				} else if (getTrade().isOpen(2)) {//Trade Screen 2
					getTrade().acceptTrade();
					tradeWithEmptySlots = Calculations.random(0, 10) <= 9 ? 0 : Calculations.random(10, 14);
					sleepUntil(new Condition() {
						public boolean verify() {
							return !getTrade().isOpen();
						}
					}, Calculations.random(2000, 3200));
				}
			} else {
				// Searches for the mule
				boolean playerIsThere = false;
				for (Player p : getPlayers().all()) {
					if (p.getName().equalsIgnoreCase(username)){
						playerIsThere = true;
						break;
					}
				}
				// trades with mule if available
				if (playerIsThere) {
					getTrade().tradeWithPlayer(username);
					sleepUntil(new Condition() {
						public boolean verify() {
							return getTrade().isOpen();
						}
					}, Calculations.random(2000, 3200));
				}
			}
			break;
		case WORLDHOP:
			break;
		case LOGOUT:
			break;
		case ANTI:
			antiBan();
			break;
		default:
			break;
		
		}
		return 0;
	}
	
	@Override
	public void onExit() {
		running = false;
	}
	
	private enum State {
		TRADING, MOVING, TELEGRABBING, LOGOUT, WORLDHOP, ANTI;
	}

	private State getBotState() {
		if (getInventory().count("Law rune") <= 1) {
			return State.LOGOUT;
		}
		if (Calculations.random(434) == 1 && (antiBanDelay.elapsed() / 1000.0) > 360 && !Instance.getInstance().isMouseInputEnabled()) {
			antiBanDelay.reset();
			return State.ANTI;
		}
		if (getInventory().emptySlotCount() > tradeWithEmptySlots && !getTrade().isOpen()) {//tele-grabbing
			if (getLocalPlayer().getZ() == 1) {
				//if (getLocalPlayer().getID()) {
					//return State.WORLDHOP;
				//}
				return State.TELEGRABBING;
			} else {
				return State.MOVING;
			}
		} else {//trading
			if (getLocalPlayer().getZ() == 1) {
				return State.MOVING;
			} else {
				return State.TRADING;
			}
		}
	}
	
	public void antiBan() {
		int Anti1 = Calculations.random(5);
		switch (Anti1) {
		case 1:
			getTabs().open(Tab.INVENTORY);
			sleep(Calculations.random(1240, 4500));
			if ((Calculations.random(0, 4) == 1)) {
				if (Calculations.random(1, 2) == 1) {
					getTabs().open(Tab.SKILLS);
					sleep(500 + Calculations.random(2000));
				} else {
					getTabs().open(Tab.EQUIPMENT);
					sleep(500 + Calculations.random(500));
				}
			}
			getTabs().open(Tab.MAGIC);
			sleep(500 + Calculations.random(3000));
			break;
		default:
			getMouse().moveMouseOutsideScreen();
			sleep(Calculations.random(3040, 12500));
			break;
		}
		antiBanDelay.reset();
	}
	
	public int pingTestWorld(int id) {
		id = id -300;
		int time = -1;
		try {
			String ipAddress = "oldschool" + id + ".runescape.com";
			InetAddress inet = InetAddress.getByName(ipAddress);
			//System.out.println("Sending Ping Request to " + ipAddress);

			long finish = 0;
			long start = new GregorianCalendar().getTimeInMillis();

			if (inet.isReachable(5000)) {
				finish = new GregorianCalendar().getTimeInMillis();
				time = (int) (finish - start);
			}
		} catch (Exception e) {
			log("Exception:" + e.getMessage());
		}
		return time;
	}
	
	public void onPaint(Graphics2D g) {
		if (!started)
			return;
		int x = 25;
		g.setColor(new Color(0.0F, 0.0F, 0.0F, 0.2F));
		g.fillRect(20, 37, 260, 48);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", 1, 11));//tradeWithEmptySlots
		g.drawRect(20, 37, 260, 48);
		int playerCount = getPlayers().all().size();
		int npcCount = getNpcs().all().size();
		Utilities.drawShadowString(g, "Time Running: " + totalTime.formatTime(), x, 50);
		Utilities.drawShadowString(g, "RRT: " + ping + " ms" + ", World: " + getClient().getCurrentWorld() + ", State: " + state, x, 60);
		Utilities.drawShadowString(g, "Wines: " + wines + " | " + casts + " more casts | Grabs Left " + lawRunes, x, 70);
		Utilities.drawShadowString(g, "Player Count: " + playerCount + ", NPC Count: " + npcCount, x, 80);
		int count = 0;
		for (Player player : getPlayers().all()) {
			if (player.getName().equals(getLocalPlayer().getName()))
				Utilities.drawShadowString(g, player.getName() + " PID: " + player.getIndex(), x, 100 + count, Color.GREEN, Color.BLACK);
			else
				Utilities.drawShadowString(g, player.getName() + " PID: " + player.getIndex(), x, 100 + count, Color.RED, Color.BLACK);
			count += 12;
		}
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public int getItemCount(int id) {
		int count = 0;
		for (Item item : getInventory().all())
			if (item != null && item.getID() == id)
				count += item.getAmount();
		return count;
	}
	
	public int getItemCount(String name) {
		int count = 0;
		for (Item item : getInventory().all())
			if (item != null && item.getName().toLowerCase().equals(name.toLowerCase()))
				count += item.getAmount();
		return count;
	}
	
	public GUI getGui() {
		return gui;
	}

	public void setGui(GUI gui) {
		this.gui = gui;
	}

	public class PaintThread extends Thread {
		public void run() {
			try {
				runningThread = true;
				while (runningThread && running) {
					Thread.sleep(100);
					state = getBotState().name();
					wines = getItemCount(TRADE_ITEM + 1) + getItemCount(TRADE_ITEM);
					lawRunes = getItemCount("Law rune") - 1;
					casts = (getInventory().emptySlotCount() - tradeWithEmptySlots);
					if ((pingTimer.elapsed() / 1000.0) > 120) {
						pingTimer.reset();
						ping = pingTestWorld(getClient().getCurrentWorld());
					}
				}
			} catch (Throwable e) {
				runningThread = false;
				log(e.toString());
			}
			runningThread = false;
			log("Thread closed.");
		}
	}
	
}
