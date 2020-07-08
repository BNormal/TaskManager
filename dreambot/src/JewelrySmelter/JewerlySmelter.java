package JewelrySmelter;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.utilities.impl.Condition;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.WidgetChild;

@ScriptManifest(author = "NumberZ", category = Category.CRAFTING, name = "Gold Jewelry Smelter", version = 1.0, description = "Smelt gold into jewerly in Edgeville.")
public class JewerlySmelter extends AbstractScript {

	private Timer totalTime = new Timer();
	private Timer animDelay = new Timer();
	private int MOULD = 1592;//1595 amulet, 1592 ring
	private int GOLD_BAR = 2357;
	private int GOLD_U = 1635;//1673 amulet u, 1635 ring
	private Tile FURNACE_TILE = new Tile(3109, 3499, 0);
	private Tile BANK_TILE = new Tile(3096, 3494, 0);
	private Tile GE_TILE = new Tile(3165, 3487, 0);
	private Area FURNACE_AREA = new Area(3105, 3496, 3110, 3501, 0);
	private Area BANK_AREA = new Area(3093, 3494, 3098, 3497, 0);
	private Area EDGEVILLE_AREA = new Area(3085, 3520, 3119, 3485, 0);
	private Area VARROCK_AREA = new Area(3120, 3400, 3249, 3521, 0);
	private String currentStage = "Identifying stage";
	private int currentLevel = 0;
	private int startLevel = 0;
	private int barsInBank = -1;
	private boolean hasStarted = false;
	private boolean ranOut = false;
	private int sellPrice = -1;
	private int buyPrice = -1;
	WidgetChild child1 = null;
	WidgetChild child2 = null;
	private SkillTracking st;
	private State state;
	private boolean running;

	private enum State {
		BANKDEPOSIT, BANKWITHDRAW, WALK_TO_BANK, WALK_TO_FURNACE, SMELT, ANTIBAN, WALK_TO_VARROCK, //Edgeville Smelting
		WALK_TO_GE, BANK_GE, SELL_BUY, WALK_TO_EDGEVILLE, //Varrock buy/sell
		EDGEVILLE, VARROCK, //Locations
		WALKING_TO_GE, NOTHING;
	}

	private State getLocation() {
		if (EDGEVILLE_AREA.contains(getLocalPlayer().getTile()))
			return State.EDGEVILLE;
		if (VARROCK_AREA.contains(getLocalPlayer().getTile()))
			return State.VARROCK;
		return State.NOTHING;
	}
	
	private State getState() {
		if (animDelay.elapsed() < 2000 && Calculations.random(1, 400 - (getCamera().getYaw() < 200 ? 200 : 0)) == 1 && getLocalPlayer().isMoving()) {
			currentStage = "Anti Ban";
			return State.ANTIBAN;
		}
		switch (getLocation()) {
		case EDGEVILLE:
			if (BANK_AREA.contains(getLocalPlayer().getTile()) && !ranOut
					&& (!getInventory().contains(GOLD_BAR) && getInventory().getEmptySlots() < 27
							|| getInventory().getEmptySlots() == 0 && !getInventory().contains(MOULD) || getInventory().contains(GOLD_U)))
				return State.BANKDEPOSIT;
			if (BANK_AREA.contains(getLocalPlayer())
					&& (getInventory().emptySlotCount() >= 1 && !getInventory().contains(GOLD_BAR)
							|| !getInventory().contains(GOLD_BAR) || !getInventory().contains(MOULD))) {
				return State.BANKWITHDRAW;
			}
			if (getInventory().count(GOLD_BAR) > 0 && !FURNACE_AREA.contains(getLocalPlayer())
					&& getInventory().contains(MOULD)) {
				return State.WALK_TO_FURNACE;
			}
			if (FURNACE_AREA.contains(getLocalPlayer()) && getInventory().contains(GOLD_BAR)
					&& getInventory().contains(MOULD)) {
				if (animDelay.elapsed() < 2000 && Calculations.random(1, 200) == 1) {
					currentStage = "Anti Ban";
					return State.ANTIBAN;
				}
				return State.SMELT;
			}
			return State.WALK_TO_BANK;
		case VARROCK:
			if (getLocalPlayer().distance(GE_TILE) < 6) {
				if (!getGrandExchange().isOpen() && !getInventory().contains(GOLD_U + 1) && !getInventory().contains(MOULD) || getInventory().contains(GOLD_BAR + 1)) {
					return State.BANK_GE;
				}
				if ((getInventory().contains(GOLD_U + 1) || getInventory().count(995) > 10 || getGrandExchange().isOpen()) && !getInventory().contains(GOLD_BAR + 1)) {
					return State.SELL_BUY;
				}
				if (getInventory().contains(MOULD) && getInventory().getEmptySlots() == 27)
					return State.WALK_TO_EDGEVILLE;
			} else if (getInventory().contains(MOULD) && getInventory().getEmptySlots() == 27)
					return State.WALK_TO_EDGEVILLE;
			else
				return State.WALK_TO_GE;
			break;
		default:
			return State.NOTHING;
		}
		return State.NOTHING;
	}

	public void onStart() {
		running = true;
		Thread thread = new Thread() {
			public void run() {
				while (running) {
					try {
						Thread.sleep(1);
						if (st != null)
							st.refresh();
						if (getLocalPlayer().isAnimating())
							animDelay.reset();
					} catch (InterruptedException e) {
					}
				}
				log("Thread closed.");
			}
		};
		thread.start();
	}
	
	@Override
	public int onLoop() {
		try {
			if (getBank().isOpen())
				barsInBank = getBank().count(GOLD_BAR);
			if (!hasStarted && getLocalPlayer().isOnScreen()) {
				hasStarted = true;
				startLevel = getSkills().getRealLevel(Skill.CRAFTING);
				st = new SkillTracking(this);
			}
			currentLevel = getSkills().getRealLevel(Skill.CRAFTING);
			state = getState();
			switch (state) {
			/****** EDGEVILLE ********/
			case BANKDEPOSIT:
				if (getBank().isOpen()) {
					currentStage = "Depositing into bank";
					getBank().depositAllExcept(MOULD);
					sleepUntil(new Condition() {
						public boolean verify() {
							return getInventory().onlyContains(MOULD);
						}

					}, Calculations.random(2000, 3200));
				} else {
					if (!getLocalPlayer().isMoving() || BANK_AREA.contains(getLocalPlayer().getTile())) {
						currentStage = "Opening bank";
						getBank().open();
						sleepUntil(new Condition() {
							public boolean verify() {
								return getBank().isOpen();
							}
						}, Calculations.random(2000, 3200));
					}
				}
				break;
			case BANKWITHDRAW:
				if (getBank().isOpen()) {
					if (getBank().contains(GOLD_BAR)) {
						currentStage = "Withdrawing from bank";
						if (!getInventory().contains(MOULD)) {
							if (!getBank().contains(MOULD)) {
								currentStage = "No mould - Logging out";
								getBank().close();
								sleep(500, 1000);
								getTabs().logout();
								sleep(1000, 2000);
								stop();
							} else {
								getBank().withdraw(MOULD);
							}
						}
						if (getInventory().contains(MOULD) && getInventory().getEmptySlots() >= 1) {
							getBank().withdrawAll(GOLD_BAR);
							sleepUntil(() -> getInventory().contains(GOLD_BAR), Calculations.random(5000, 7000));
						}
					} else if (!getInventory().contains(GOLD_BAR)) {
						currentStage = "Checking for runes";
						ranOut = true;
						if ((getBank().count("Law rune") >= 1 || getInventory().count("Law rune") >= 1) && (getBank().count("Air rune") >= 3 || getInventory().count("Air rune") >= 3) && (getBank().count("Staff of fire") >= 1 || getInventory().count("Staff of fire") >= 1 || getEquipment().contains("Staff of fire"))) {
							if (getInventory().contains(MOULD)) {
								getBank().deposit(MOULD);
								sleepUntil(() -> getInventory().count(MOULD) == 0, Calculations.random(2000, 3000));
							}
							if (getInventory().count("Law rune") < 1) {
								getBank().withdraw("Law rune");
								sleepUntil(() -> getInventory().count("Law rune") >= 1, Calculations.random(2000, 3000));
							}
							if (getInventory().count("Air rune") < 3) {
								getBank().withdraw("Air rune", 3 - getInventory().count("Air rune"));
								sleepUntil(() -> getInventory().count("Air rune") >= 3, Calculations.random(2000, 3000));
							}
							if (!getEquipment().contains("Staff of fire")) {
								if (getInventory().count("Staff of fire") < 1) {
									getBank().withdraw("Staff of fire");
									sleepUntil(() -> getInventory().count("Staff of fire") >= 1, Calculations.random(2000, 3000));
								}
								if (getInventory().count("Staff of fire") >= 1) {
									if (getBank().isOpen()) {
										getBank().close();
										sleepUntil(() -> !getBank().isOpen(), Calculations.random(3000, 5000));
									}
									getInventory().interact("Staff of fire", "Wield");
									sleepUntil(() -> getEquipment().contains("Staff of fire"), Calculations.random(2000, 3000));
								}
							}
							if (getInventory().count("Law rune") >= 1 && getInventory().count("Air rune") >= 3 && getEquipment().contains("Staff of fire")) {
								currentStage = "Teleporting";
								if (getBank().isOpen()) {
									getBank().close();
									sleepUntil(() -> !getBank().isOpen(), Calculations.random(3000, 5000));
								}
								if (!getTabs().isOpen(Tab.MAGIC)) {
						            getTabs().open(Tab.MAGIC);
						            sleepUntil(() -> getTabs().isOpen(Tab.MAGIC), Calculations.random(3000, 5000));
								}
								if (getTabs().isOpen(Tab.MAGIC)) {
						            getMagic().castSpell(Normal.VARROCK_TELEPORT);
						            ranOut = false;
						            sleep(2000, 3000);
									getTabs().open(Tab.INVENTORY);
						        }
							}
						} else {
							currentStage = "Logging out";
							getBank().close();
							sleep(500, 1000);
							getTabs().logout();
							sleep(1000, 2000);
							stop();
						}
					}
				} else {
					if (!getLocalPlayer().isMoving()) {
						currentStage = "Opening bank";
						getBank().open();
						sleepUntil(new Condition() {
							public boolean verify() {
								return getBank().isOpen();
							}
						}, Calculations.random(900, 1200));
					}
				}
				break;
			case WALK_TO_FURNACE:
				if (getLocalPlayer().isMoving() && getWalking().getDestinationDistance() > 2)
					break;
				if (!FURNACE_AREA.contains(getLocalPlayer())) {
					currentStage = "Walking to furnace";
					getWalking().walk(new Tile(FURNACE_TILE.getX() + Calculations.random(-3, 0), FURNACE_TILE.getY() + Calculations.random(0, 2), 0));
					sleep(300, 500);
				}
				break;
			case SMELT:
				int craft_widget = 7;//34 amulet, 7 ring, 
				child1 = getWidgets().getWidgetChild(446, craft_widget);
				child2 = getWidgets().getWidgetChild(162, 34);
				GameObject furnace = getGameObjects().closest("Furnace");
				if (!isAnimating() && (child1 == null || !child1.isVisible())) {
					currentStage = "Opening smelting options";
					furnace.interact();
					sleepUntil(new Condition() {
						@Override
						public boolean verify() {
							child1 = getWidgets().getWidgetChild(446, craft_widget);
							return child1 != null && child1.isVisible();
						}
					}, Calculations.random(4000, 7500));
					//sleepUntil(() -> child1 != null && child1.isVisible(), Calculations.random(4000, 7500));
				}
				if (child1 != null && child1.isVisible()) {
					currentStage = "Make";
					child1.interact();
					animDelay.reset();
					sleep(300, 950);
					antiBan();
					currentStage = "Smelting";
					sleepUntil(new Condition() {
						@Override
						public boolean verify() {
							child2 = getWidgets().getWidgetChild(162, 34);
							return child2 != null && child2.isVisible();
						}
					}, Calculations.random(4000, 7500));
				}
				/*if (child2 != null && child2.isVisible()) {
					currentStage = "Typing smelt amount";
					String number = Integer.toString(Calculations.random(0, 6) == 1 ? 66 : 33);
					if (Calculations.random(1, 11) == 1)
						number = Integer.toString(Calculations.random(100, 999));
					else if (Calculations.random(1, 10) > 5)
						number = Integer.toString(Calculations.random(3, 9) * 11);
					else if (Calculations.random(1, 7) > 5)
						number = Integer.toString(Calculations.random(33, 99));
					sleep(Calculations.random(500, 1200));
					if (child2 != null && child2.isVisible()) {
						getKeyboard().type(number, true);
						animDelay.reset();
						currentStage = "Smelting";
						sleep(2000, 3950);
					}
					sleep(300, 950);
					antiBan();
				}*/

				break;
			case WALK_TO_BANK:
				if (getLocalPlayer().isMoving() && getWalking().getDestinationDistance() > 2)
					break;
				if (!BANK_AREA.contains(getLocalPlayer())) {
					currentStage = "Walking to bank";
					//getWalking().getClosestTileOnMap(BANK_TILE);
					getWalking().walk(new Tile(BANK_TILE.getX() + Calculations.random(-1, 2), BANK_TILE.getY() + Calculations.random(0, 3)));
					sleep(300, 500);
				}
				break;
				/****** END OF EDGEVILLE ********/
				
				/****** VARROCK ********/
			case BANK_GE:
				if (getGrandExchange().isOpen()) {
					getGrandExchange().close();
					sleepUntil(() -> !getGrandExchange().isOpen(), Calculations.random(3000, 5000));
				}
				if (!getInventory().contains(GOLD_U + 1) || getInventory().contains(GOLD_BAR + 1)) {
					if (getBank().isOpen()) {
						if (getBank().contains(GOLD_U)) {
							currentStage = "Grabbing jewelry";
							if (getBank().getWithdrawMode().equals(BankMode.ITEM)) {
								getBank().setWithdrawMode(BankMode.NOTE);
								sleepUntil(() -> getBank().getWithdrawMode().equals(BankMode.NOTE), Calculations.random(3000, 5000));
							}
							getBank().withdrawAll(GOLD_U);
							sleepUntil(() -> getInventory().contains(GOLD_U + 1), Calculations.random(3000, 5000));
							getBank().close();
						}
						if (getInventory().contains(GOLD_BAR + 1)) {
							currentStage = "Depositting gold bars";
							getBank().depositAllItems();
							sleepUntil(() -> getInventory().getEmptySlots() >= 10, Calculations.random(3000, 5000));
						}
						if (getBank().contains(GOLD_BAR) && !getBank().contains(GOLD_U) && !getInventory().contains(GOLD_BAR + 1) && !getInventory().contains(GOLD_U + 1) && getInventory().count(MOULD) < 1) {
							getBank().withdraw(MOULD, 1);
							sleepUntil(() -> getInventory().count(MOULD) >= 1, Calculations.random(3000, 5000));
							if (getInventory().count(MOULD) >= 1 && getBank().isOpen())
								getBank().close();
						}
					} else {
						currentStage = "Opening bank";
						getBank().open();
						sleepUntil(() -> getBank().isOpen(), Calculations.random(2000, 4000));
					}
				}
				break;
			case SELL_BUY:
				if (getGrandExchange().isOpen()) {
					if (getInventory().contains(GOLD_U + 1)) {
						if (getGrandExchange().isSellOpen()) {
							if (getGrandExchange().getCurrentChosenItem().getID() == GOLD_U) {
								if (sellPrice == -1) {
									currentStage = "Setting price";
									int sellPrice2 = getGrandExchange().getCurrentPrice() - 5;
									getGrandExchange().setPrice(sellPrice2);
									sleepUntil(() -> getGrandExchange().getCurrentPrice() == sellPrice2, Calculations.random(3000, 5000));
									if (getGrandExchange().getCurrentPrice() == sellPrice2)
										sellPrice = sellPrice2;
								}
								if (sellPrice > 0) {
									currentStage = "Selling jewelry";
									getGrandExchange().confirm();
									sleep(500, 700);
									sellPrice = -1;
								}
							} else {
								currentStage = "Selecting jewelry";
								getGrandExchange().addSellItem(getInventory().get(GOLD_U + 1).getName().toLowerCase());
								sleepUntil(() -> getGrandExchange().isSellOpen(), Calculations.random(3000, 5000));
							}
						} else {
							currentStage = "Selecting jewelry";
							getGrandExchange().addSellItem(getInventory().get(GOLD_U + 1).getName().toLowerCase());
							sleepUntil(() -> getGrandExchange().isSellOpen(), Calculations.random(3000, 5000));
						}
					}
					if (getGrandExchange().isReadyToCollect()) {
						currentStage = "Collecting";
						getGrandExchange().collect();
						sleepUntil(() -> !getGrandExchange().isReadyToCollect(), Calculations.random(3000, 5000));
					}
					if (getGrandExchange().isOpen() && !getGrandExchange().slotContainsItem(0) && !getInventory().contains(GOLD_U + 1) && getInventory().contains(995) && !getInventory().contains(GOLD_BAR + 1)) {
						if (getGrandExchange().isBuyOpen()) {
							if (getGrandExchange().getCurrentChosenItemID() == GOLD_BAR) {
								if (buyPrice == -1) {
									currentStage = "Setting price and quantity";
									int buyPrice2 = getGrandExchange().getCurrentPrice() + 5;
									getGrandExchange().setPrice(buyPrice2);
									sleepUntil(() -> getGrandExchange().getCurrentPrice() == buyPrice2, Calculations.random(3000, 5000));
									int amount = (getInventory().count(995) - 10) / buyPrice2;
									if (amount > 0) {
										getGrandExchange().setQuantity(amount);
										sleepUntil(() -> getGrandExchange().getCurrentAmount() == amount, Calculations.random(3000, 5000));
										if (getGrandExchange().getCurrentPrice() == buyPrice2 && getGrandExchange().getCurrentAmount() == amount)
											buyPrice = buyPrice2;
									} else {
										log("Not enough money");
										stop();
									}
								}
								if (buyPrice > 0) {
									currentStage = "Buying gold bars";
									getGrandExchange().confirm();
									sleep(500, 700);
									buyPrice = -1;
								}
							} else {
								currentStage = "Selecting gold bar";
								getGrandExchange().addBuyItem("Gold Bar");
								sleepUntil(() -> getGrandExchange().getCurrentChosenItemID() == GOLD_BAR, Calculations.random(3000, 5000));
							}
						} else {
							currentStage = "Opening search";
							getGrandExchange().openBuyScreen(0);
							sleepUntil(() -> getGrandExchange().isBuyOpen(), Calculations.random(3000, 5000));
						}
					}
				} else {
					currentStage = "Opening GE";
					getGrandExchange().open();
					sleepUntil(() -> getGrandExchange().isOpen(), Calculations.random(10000, 15000));
				}
				break;
			case WALK_TO_GE:
				if (getLocalPlayer().isMoving() && getWalking().getDestinationDistance() > 3)
					break;
				if (getLocalPlayer().distance(GE_TILE) >= 6) {
					currentStage = "Walking to Grand Exchange";
					getWalking().walk(GE_TILE);
					sleep(300, 500);
				}
				break;
			case WALK_TO_EDGEVILLE:
				if (getLocalPlayer().isMoving() && getWalking().getDestinationDistance() > 3)
					break;
				if (getLocalPlayer().distance(BANK_TILE) >= 6) {
					currentStage = "Walking to Edgeville";
					if (getLocalPlayer().getX() < 3133)
						getWalking().walk(BANK_TILE);
					else
						getWalking().walk(new Tile(3120 + Calculations.random(0, 12), 3516 + Calculations.random(0, 2), 0));
					sleep(300, 500);
				}
				break;
				/****** END OF VARROCK ********/
			case NOTHING:
				currentStage = "Doing nothing";
				break;

			case ANTIBAN:
				antiBan();
				break;
			default:
				break;
			}
		} catch (Exception e) {
			StackTraceElement[] elements = e.getStackTrace();
			for (int iterator = 1; iterator <= elements.length; iterator++)
				log("Class:"+elements[iterator-1].getClassName()+" Method:" + elements[iterator - 1].getMethodName() + " Line:"
						+ elements[iterator - 1].getLineNumber());
		}
		return Calculations.random(200, 400);
	}
	
	public void interactRandomSlot(int itemID, String option) {
		List<Integer> slots = new ArrayList<Integer>();
		Inventory inv = getInventory();
		for (int i = 0; i < 28; i++) {
			Item item = inv.getItemInSlot(i);
			if (item != null && item.getID() == itemID)
				slots.add(i);
		}
		if (slots.size() > 0) {
			int slot = slots.get(0);
			if (slots.size() > 1)
				slot = slots.get(Calculations.random(0, slots.size() - 1));
			//if (Calculations.random(0, 100) > 15)
				//slot = slots.get(slots.size() - 1);
			getInventory().slotInteract(slot, "Use");
		}
	}
	
	public void antiBan() {
		currentStage = "Anti-ban";
		int Anti1 = Calculations.random(15);
		switch (Anti1) {
		case 1:
			getTabs().open(Tab.SKILLS);
			sleep(Calculations.random(1240, 2500));
			getTabs().open(Tab.INVENTORY);
			break;
		case 2:
			getCamera().rotateTo(getCamera().getYaw() + Calculations.random(-1000, 1000), getCamera().getPitch() + Calculations.random(-300, 300));
			sleep(Calculations.random(1240, 2500));
			break;
		case 3:
			getCamera().rotateToYaw(383);
			sleep(Calculations.random(1240, 2500));
			break;
		case 4:
		case 5:
			break;
		default:
			getMouse().moveMouseOutsideScreen();
			sleep(Calculations.random(1240, 8500));
			break;
		}
	}
	
	public boolean isAnimating() {
		/*WidgetChild child3 = getWidgets().getWidgetChild(233, 3);
		if (child3 != null && child3.getActions() != null && child3.getActions().length > 0) {
			return false;
		}*/
		return (getLocalPlayer().isAnimating() || animDelay.elapsed() < 1000);
	}

	public void onPaint(Graphics2D g) {
		int x = 25;
		g.setColor(new Color(0.0F, 0.0F, 0.0F, 0.2F));
		g.fillRect(20, 37, 200, 47);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", 1, 11));
		g.drawRect(20, 37, 200, 47);
		Utilities.drawShadowString(g, "Time Running: " + totalTime.formatTime(), x, 50);
		Utilities.drawShadowString(g, "Stage: " + currentStage, x, 60);
		int levelUps = currentLevel - startLevel;
		Utilities.drawShadowString(g, "Current Level: " + startLevel + " (" + (levelUps > 0 ? ("+" + levelUps) : "0") + ")" + (levelUps > 0 ? " = " + getSkills().getRealLevel(Skill.CRAFTING) : ""), x, 70);
		Utilities.drawShadowString(g, "Bars Remaining: " + (barsInBank == -1 ? "--" : (barsInBank + getInventory().count(GOLD_BAR))), x, 80);
		st.onPaint(g, getSkills(), totalTime, x);
	}

	@Override
	public void onExit() {
		running = false;
		log("Bye bye!");
	}

}
