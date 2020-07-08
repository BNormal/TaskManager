package Mule;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.AdvancedMessageListener;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.utilities.impl.Condition;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.dreambot.core.Instance;

import JewelrySmelter.Utilities;

@ScriptManifest(author = "NumberZ", category = Category.MONEYMAKING, name = "Mule Trader", version = 1.0, description = "The pack dealer who walks for you.")
public class Mule extends AbstractScript implements AdvancedMessageListener {

	private int MAX_DISTANCE = 14;
	private Timer totalTime = new Timer();
	private Timer waitTimer = new Timer();
	private Timer antiBanDelay = new Timer();
	private int timeRemaining = 1000 * 60 * Calculations.random(2, 4) + Calculations.random(100, 700);
	private int walkingDistance = Calculations.random(1, MAX_DISTANCE);
	//private Tile ZAMMY_TILE = new Tile(2939, 3516, 0);
	private Tile bankTile = new Tile(2946, 3369, 0);
	private String username;
	private boolean tradeReady = false;
	private boolean running = false;
	private int hasStarted = 0;
	private ArrayList<OwnedItem> items = new ArrayList<OwnedItem>();
	private Tile destTile;
	private String bankName = "";
	private boolean recordedItems = false;
	private Item[] tradeItems;
	private String tradeOwner;
	private int itemId = 245;
	private boolean isNoting = false;

	private GUI gui;
	
	private Area getDestArea() {
		return new Area(destTile.getX() - 1, destTile.getY() - 1, destTile.getX() + 1, destTile.getY() + 1, destTile.getZ());
	}
	
	private Area getBankArea() {
		switch(bankName.toLowerCase()) {
			case "falador west":
				bankTile = new Tile(2946, 3369, 0);
				return new Area(2943, 3373, 2947, 3368, 0);
			case "falador east":
				bankTile = new Tile(3012, 3356, 0);
				return new Area(3009, 3353, 3018, 3358, 0);
			case "edgeville":
				bankTile = new Tile(3095, 3497, 0);
				return new Area(3091, 3488, 3098, 3499, 0);
			case "lumbridge":
				bankTile = new Tile(3209, 3219, 2);
				return new Area(3207, 3215, 3210, 3220, 0);
			case "varrock west":
				bankTile = new Tile(3186, 3440, 0);
				return new Area(3182, 3433, 3189, 3446, 0);
			case "varrock east":
				bankTile = new Tile(3253, 3421, 0);
				return new Area(3250, 3420, 3423, 3423, 0);
			case "draynor":
				bankTile = new Tile(3093, 3243, 0);
				return new Area(3092, 3240, 3095, 3246, 0);
			case "grand exchange":
				bankTile = new Tile(3165, 3485, 0);
				return new Area(3159, 3484, 3170, 3495, 0);
			case "al-karid":
				bankTile = new Tile(3270, 3167, 0);
				return new Area(3269, 3161, 3272, 3173, 0);
		}
		return new Area(2943, 3373, 2947, 3368, 0);
	}
	
	public boolean containsItems() {
		for (OwnedItem item : items) {
			if (item != null && getInventory().contains(item.getItem().getID()))
				return true;
		}
		return false;
	}
	
	public Item getNextItem(boolean isInventory) {
		for (OwnedItem item : items) {
			if (isInventory) {
				if (item != null && getInventory().contains(item.getItem().getID()))
					return item.getItem();
			} else {
				if (item != null && getBank().contains(item.getItem().getID()))
					return item.getItem();
			}
		}
		return null;
	}
	
	private enum State {
		TRADING, BANKING, WALKING, ANTI;
	}
	
	private State getState() {
		if (getBankArea().contains(getLocalPlayer().getTile()))
			return State.BANKING;
		if (getDestArea().contains(getLocalPlayer().getTile()))
			return State.TRADING;
		if (Calculations.random(434) == 1 && (antiBanDelay.elapsed() / 1000.0) > 180 && !Instance.getInstance().isMouseInputEnabled()) {
			antiBanDelay.reset();
			return State.ANTI;
		}
		return State.WALKING;
	}
	
	@Override
	public void onStart() {
		log("Welcome to Wine Mule Trader 1.0");
	}
	
	@Override
	public int onLoop() {
		if (!running && hasStarted == 1) {
			hasStarted = 2;
			gui = new GUI(this);
		}
		if (!running)
			return 0;
		switch (getState()) {
		case BANKING:
			if (getInventory().contains(itemId)) {
				if (isNoting) {
					if (getBank().isOpen()) {
						getBank().depositAll(itemId);
						sleepUntil(new Condition() {
							public boolean verify() {
								return !getInventory().contains(itemId);
							}
						}, Calculations.random(2000, 3200));
					} else {
						if (!getLocalPlayer().isMoving() || getBankArea().contains(getLocalPlayer().getTile())) {
							getBank().open();
							sleepUntil(new Condition() {
								public boolean verify() {
									return getBank().isOpen();
								}
							}, Calculations.random(2000, 3200));
						}
					}
				} else {
					if (getBank().isOpen()) {
						getBank().close();
						sleepUntil(new Condition() {
							public boolean verify() {
								return !getBank().isOpen();
							}
						}, Calculations.random(2000, 3200));
					}
					if (getLocalPlayer().isMoving() && getWalking().getDestinationDistance() > walkingDistance)
						break;
					walkingDistance = Calculations.random(1, MAX_DISTANCE);
					getWalking().walk(
							new Tile(destTile.getX() + Calculations.random(-1, 1), destTile.getY() + Calculations.random(0, 1), 0)
					);
					sleep(300, 500);
				}
			} else {
				if (isNoting) {
					if (getBank().isOpen()) {
						if (getBank().contains(itemId)) {
							if (getBank().getWithdrawMode().equals(BankMode.ITEM)) {
								getBank().setWithdrawMode(BankMode.NOTE);
								sleepUntil(
										() -> getBank().getWithdrawMode().equals(BankMode.NOTE), Calculations.random(3000, 5000)
								);
							}
							getBank().withdrawAll(itemId);
							sleepUntil(new Condition() {
								public boolean verify() {
									return !getBank().contains(itemId);
								}
							}, Calculations.random(2000, 3200));
						} else {
							getBank().close();
							sleepUntil(new Condition() {
								public boolean verify() {
									return !getBank().isOpen();
								}
							}, Calculations.random(2000, 3200));
						}
					} else {
						if (getLocalPlayer().isMoving() && getWalking().getDestinationDistance() > walkingDistance)
							break;
						walkingDistance = Calculations.random(1, MAX_DISTANCE);
						getWalking()
								.walk(new Tile(destTile.getX() + Calculations.random(-1, 1), destTile.getY() + Calculations.random(0, 1), 0));
						sleep(300, 500);
					}
				} else {
					if (getBank().isOpen()) {
						if (getBank().contains(itemId)) {
							if (getBank().getWithdrawMode().equals(BankMode.NOTE)) {
								getBank().setWithdrawMode(BankMode.ITEM);
								sleepUntil(
										() -> getBank().getWithdrawMode().equals(BankMode.ITEM), Calculations.random(3000, 5000)
								);
							}
							getBank().withdraw(itemId, 27);
							sleepUntil(new Condition() {
								public boolean verify() {
									return getInventory().contains(itemId);
								}
							}, Calculations.random(2000, 3200));
						} else {
							getBank().close();
							sleepUntil(new Condition() {
								public boolean verify() {
									return !getBank().isOpen();
								}
							}, Calculations.random(2000, 3200));
						}
					} else {
						if (!getLocalPlayer().isMoving() || getBankArea().contains(getLocalPlayer().getTile())) {
							getBank().open();
							sleepUntil(new Condition() {
								public boolean verify() {
									return getBank().isOpen();
								}
							}, Calculations.random(2000, 3200));
						}
					}
				}
			}
			break;
		case TRADING:
			if (isNoting) {
				if (!getInventory().contains(itemId)) {
					if (getTrade().isOpen()) {
						if (getTrade().isOpen(1)) {//screen 1
							if (getInventory().contains(itemId + 1)) {
								getTrade().addItem(itemId + 1, getInventory().count(itemId + 1));
							} else {
								getTrade().acceptTrade();
								setTradeOwner(getTrade().getTradingWith());
								sleepUntil(new Condition() {
									public boolean verify() {
										return getTrade().isOpen(2);
									}
								}, Calculations.random(2000, 3200));
							}
						} else {//screen 2
							getTrade().acceptTrade();
							if (!recordedItems) {
								recordedItems = true;
								setTradeItems(getTrade().getTheirItems());
							}
							sleepUntil(new Condition() {
								public boolean verify() {
									boolean verified = !getTrade().isOpen();
									if (verified) {
										/*boolean containsTrade = true;
										for (Item item : tradeItems) {
											if (!getInventory().contains(item.getID())) {
												containsTrade = false;
												break;
											}
										}
										if (containsTrade) {
											for (Item item : tradeItems) {
												items.add(new OwnedItem(tradeOwner, item));
											}
										}*/
										setTradeOwner(null);
										setTradeItems(null);
										recordedItems = false;
									}
									return verified;
								}
							}, Calculations.random(2000, 3200));
						}
					} else {
						if (tradeReady) {
							getTrade().tradeWithPlayer(username);
							tradeReady = false;
							sleepUntil(new Condition() {
								public boolean verify() {
									return getTrade().isOpen();
								}
							}, Calculations.random(2000, 3200));
						} else {
							if (waitTimer.elapsed() > timeRemaining) {
								timeRemaining = 1000 * 60 * Calculations.random(2, 4) + Calculations.random(100, 700);
								waitTimer.reset();
								getCamera().rotateToYaw(Calculations.random(0, 360));
								getCamera().rotateToPitch(Calculations.random(0, 360));
							}
						}
					}
				} else {
					if (getInventory().contains(itemId)) {
						if (getLocalPlayer().isMoving() && getWalking().getDestinationDistance() > walkingDistance)
							break;
						walkingDistance = Calculations.random(1, MAX_DISTANCE);
						getWalking()
								.walk(new Tile(bankTile.getX() + Calculations.random(-1, 1), bankTile.getY() + Calculations.random(0, 1), 0));
						sleep(300, 500);
					}
				}
			} else {
				if (getInventory().contains(itemId) || getTrade().isOpen()) {
					if (getTrade().isOpen()) {
						if (getTrade().isOpen(1)) {//screen 1
							if (getInventory().contains(itemId)) {
								getTrade().addItem(itemId, getInventory().count(itemId));
							} else {
								getTrade().acceptTrade();
								setTradeOwner(getTrade().getTradingWith());
								sleepUntil(new Condition() {
									public boolean verify() {
										return getTrade().isOpen(2);
									}
								}, Calculations.random(2000, 3200));
							}
						} else {//screen 2
							getTrade().acceptTrade();
							sleepUntil(new Condition() {
								public boolean verify() {
									return !getTrade().isOpen();
								}
							}, Calculations.random(2000, 3200));
						}
					} else {
						if (tradeReady) {
							getTrade().tradeWithPlayer(username);
							tradeReady = false;
							sleepUntil(new Condition() {
								public boolean verify() {
									return getTrade().isOpen();
								}
							}, Calculations.random(2000, 3200));
						} else {
							if (waitTimer.elapsed() > timeRemaining) {
								timeRemaining = 1000 * 60 * Calculations.random(2, 4) + Calculations.random(100, 700);
								waitTimer.reset();
								getCamera().rotateToYaw(Calculations.random(0, 360));
								getCamera().rotateToPitch(Calculations.random(0, 360));
							}
						}
					}
				} else {
					if (getLocalPlayer().isMoving() && getWalking().getDestinationDistance() > walkingDistance)
						break;
					walkingDistance = Calculations.random(1, MAX_DISTANCE);
					getWalking().walk(new Tile(bankTile.getX() + Calculations.random(-1, 1), bankTile.getY() + Calculations.random(0, 1), 0));
					sleep(300, 500);
				}
			}
			break;
		case WALKING:
			
			if (getLocalPlayer().isMoving() && getWalking().getDestinationDistance() > walkingDistance)
				break;
			walkingDistance = Calculations.random(1, MAX_DISTANCE);
			if (!isNoting) {
				if (getInventory().contains(itemId)) {
					getWalking().walk(new Tile(destTile.getX() + Calculations.random(-1, 1), destTile.getY() + Calculations.random(0, 1), 0));
					sleep(300, 500);
				} else {
					waitTimer.reset();
					getWalking().walk(new Tile(bankTile.getX() + Calculations.random(-1, 1), bankTile.getY() + Calculations.random(0, 1), 0));
					sleep(300, 500);
				}
			} else {
				if (getInventory().contains(itemId)) {
					getWalking().walk(new Tile(bankTile.getX() + Calculations.random(-1, 1), bankTile.getY() + Calculations.random(0, 1), 0));
					sleep(300, 500);
				} else {
					waitTimer.reset();
					getWalking().walk(new Tile(destTile.getX() + Calculations.random(-1, 1), destTile.getY() + Calculations.random(0, 1), 0));
					sleep(300, 500);
				}
			}
			break;
		case ANTI:
			antiBan();
			break;
		}
		return 0;
	}
	
	@Override
	public void onExit() {
		running = false;
		gui.getFrmMule().setVisible(false);
		gui = null;
	}

	public void antiBan() {
		int Anti1 = Calculations.random(5);
		switch (Anti1) {
		case 1:
			getTabs().open(Tab.SKILLS);
			sleep(Calculations.random(1240, 4500));
			if ((Calculations.random(0, 4) == 1)) {
				if (Calculations.random(1, 2) == 1) {
					getTabs().open(Tab.QUEST);
					sleep(500 + Calculations.random(2000));
				} else {
					getTabs().open(Tab.EQUIPMENT);
					sleep(500 + Calculations.random(500));
				}
			}
			getTabs().open(Tab.INVENTORY);
			sleep(500 + Calculations.random(3000));
			break;
		default:
			getMouse().moveMouseOutsideScreen();
			sleep(Calculations.random(3040, 12500));
			break;
		}
		antiBanDelay.reset();
	}
	
	@Override
	public void onTradeMessage(Message message) {
		if (message.getUsername().equalsIgnoreCase(username)) {
			tradeReady = true;
		}
	}

	@Override
	public void onAutoMessage(Message message) {}

	@Override
	public void onClanMessage(Message message) {}

	@Override
	public void onGameMessage(Message message) {}

	@Override
	public void onPlayerMessage(Message message) {}

	@Override
	public void onPrivateInMessage(Message message) {}

	@Override
	public void onPrivateInfoMessage(Message message) {}

	@Override
	public void onPrivateOutMessage(Message message) {}

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
	
	public void onPaint(Graphics2D g) {
		if ((getWidgets().getWidgetChild(162, 5) != null && getWidgets().getWidgetChild(162, 5).isVisible())) {
			if (hasStarted == 0)
				hasStarted = 1;
			if (!running)
				return;
			int x = 25;
			g.setColor(new Color(0.0F, 0.0F, 0.0F, 0.2F));
			g.fillRect(20, 37, 220, 28);
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", 1, 11));
			g.drawRect(20, 37, 220, 28);
			Timestamp remainingTime = new Timestamp(timeRemaining - waitTimer.elapsed());
			Utilities.drawShadowString(g, "Time Running: " + totalTime.formatTime() + ", State: " + getState(), x, 50);
			Utilities.drawShadowString(g, "Time remaining until anti-log out: " + (new SimpleDateFormat("mm:ss").format(remainingTime)), x, 60);
		}
	}

	public Tile getDestTile() {
		return destTile;
	}

	public void setDestTile(Tile destTile) {
		this.destTile = destTile;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public boolean isNoting() {
		return isNoting;
	}

	public void setNoting(boolean isNoting) {
		this.isNoting = isNoting;
	}

	public String getItemName() {
		return new Item(itemId, 0, getClient().getInstance()).getName();
	}
	
	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public Item[] getTradeItems() {
		return tradeItems;
	}

	public void setTradeItems(Item[] tradeItems) {
		this.tradeItems = tradeItems;
	}

	public String getTradeOwner() {
		return tradeOwner;
	}

	public void setTradeOwner(String tradeOwner) {
		this.tradeOwner = tradeOwner;
	}
}
