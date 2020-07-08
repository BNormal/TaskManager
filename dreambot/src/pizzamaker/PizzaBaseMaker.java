package pizzamaker;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import javax.swing.JOptionPane;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.utilities.impl.Condition;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.dreambot.core.Instance;

import JewelrySmelter.Utilities;

@ScriptManifest(author = "NumberZ", description = "Makes Pizza Bases", name = "Pizza Base Maker", version = 1, category = Category.MONEYMAKING)

public class PizzaBaseMaker extends AbstractScript {
	
	private Timer totalTime = new Timer();
	private int bankedWaters = -1;
	private int bankedFlours = -1;
	private int createdAmount = 0;//limit 13000
	private int sellPrice = -1;
	private int buyAmount = -1;
	private int bankedPots = -1;
	private int buyPriceWaterAmount = -1;
	private int buyPriceFlourAmount = -1;
	private int buyPriceWater = -1;
	private int buyPriceFlour = -1;
	private final int JUG_OF_WATER = 1937;
	private final int POT_OF_FLOUR = 1933;
	private final int PIZZA_BASE = 2283;
	private State state;
	WidgetChild make_option = null;
	
	private enum State {
		INIT, SMACKING, BANKING, TRADING, LOGOUT, NOTHING
	}

	@SuppressWarnings("static-access")
	@Override
	public void onStart() {
		log("Welcome to " + getManifest().name() + " version " + getManifest().version());
		getMouse().getMouseSettings().setSpeed(10);
		buyPriceWater = Integer.parseInt(JOptionPane.showInputDialog("Enter the buy price for Jug of water:"));
		buyPriceFlour = Integer.parseInt(JOptionPane.showInputDialog("Enter the buy price for Pot of flour:"));
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void onExit() {
		getMouse().getMouseSettings().setSpeed(5);
	}

	@Override
	public int onLoop() {
		if (!getLocalPlayer().isOnScreen() || Instance.getInstance().isMouseInputEnabled())
			return 0;
		state = getState();
		switch (state) {
		case INIT:
			if (bankedWaters == -1 || bankedFlours == -1) {
				if (getGrandExchange().isOpen()) {
					getGrandExchange().close();
					sleepUntil(() -> !getGrandExchange().isOpen(), Calculations.random(3000, 5000));
				} else if (getBank().isOpen()) {
					bankedWaters = getBank().count(JUG_OF_WATER);
					bankedFlours = getBank().count(POT_OF_FLOUR);
					bankedPots = getBank().count(1931);//empty pot
				} else {
					getBank().open();
					sleepUntil(() -> getBank().isOpen(), Calculations.random(2000, 5000));
				}
			}
			break;
		case BANKING:
			if (getInventory().getSelectedItemIndex() > -1)
				getInventory().deselect();
			if (getBank().isOpen()) {
				if (getInventory().contains(PIZZA_BASE)) {
					getBank().depositAllItems();
					sleep(1000, 2000);
					//getBank().depositAllExcept(995);
					//getBank().depositAll(PIZZA_BASE);
					bankedPots = getBank().count(1931);
				} else if (getInventory().contains(JUG_OF_WATER + 1)) {
					if (Calculations.random(0, 10) <= 8) {
						getBank().depositAllItems();
						sleep(1000, 2000);
					} else
						getBank().depositAll(JUG_OF_WATER + 1);
				} else if (getInventory().contains(POT_OF_FLOUR + 1)) {
					getBank().depositAll(POT_OF_FLOUR + 1);
				} else if (getInventory().count(JUG_OF_WATER) <= 0 && getBank().contains(JUG_OF_WATER)) {
					if (getBank().getWithdrawMode().equals(BankMode.NOTE)) {
						getBank().setWithdrawMode(BankMode.ITEM);
					}
					getBank().withdraw(JUG_OF_WATER, 9);
					sleepUntil(() -> getInventory().contains(JUG_OF_WATER), Calculations.random(2000, 5000));
				} else if (getInventory().count(POT_OF_FLOUR) <= 0 && getBank().contains(POT_OF_FLOUR)) {
					if (getBank().getWithdrawMode().equals(BankMode.NOTE)) {
						getBank().setWithdrawMode(BankMode.ITEM);
					}
					getBank().withdraw(POT_OF_FLOUR, 9);
					sleepUntil(() -> getInventory().contains(POT_OF_FLOUR), Calculations.random(2000, 5000));
				} else if ((!getInventory().contains(JUG_OF_WATER) && !getBank().contains(JUG_OF_WATER) || !getInventory().contains(POT_OF_FLOUR) && !getBank().contains(POT_OF_FLOUR)) && getBank().contains(PIZZA_BASE)) {
					if (getBank().getWithdrawMode().equals(BankMode.ITEM)) {
						getBank().setWithdrawMode(BankMode.NOTE);
					}
					getBank().withdrawAll(995);
					getBank().withdrawAll(PIZZA_BASE);
					sleepUntil(() -> getInventory().contains(PIZZA_BASE + 1), Calculations.random(2000, 5000));
				}
			} else {
				if (getGrandExchange().isOpen()) {
					getGrandExchange().close();
					sleepUntil(() -> !getGrandExchange().isOpen(), Calculations.random(3000, 5000));
				}
				getBank().open();
				sleepUntil(() -> getBank().isOpen(), Calculations.random(2000, 5000));
			}
			break;
		case SMACKING:
			if (!getTabs().isOpen(Tab.INVENTORY))
				getTabs().open(Tab.INVENTORY);
			if (getBank().isOpen()) {
				getBank().close();
				sleepUntil(() -> !getBank().isOpen(), Calculations.random(2000, 5000));
			} else {
				Item flour = getInventory().get(POT_OF_FLOUR);
				if (flour != null) {
					Item item = getLastItem();
					if (item != null) {
						flour.useOn(item);
						sleepUntil(new Condition() {
							@Override
							public boolean verify() {
								make_option = getWidgets().getWidgetChild(270, 16);
								return make_option != null && make_option.isVisible();
							}
						}, Calculations.random(4000, 7500));
						make_option.interact();
						//if (getInventory().count(JUG_OF_WATER) == 1) {
							sleepUntil(() -> getInventory().count(JUG_OF_WATER) == 0, Calculations.random(9500, 12000));
							createdAmount += getInventory().count(PIZZA_BASE);
							if (Calculations.random(0, 10) <= 7) {
								if (Calculations.random(0, 5) <= 1)
									sleep(Calculations.random(1000, 9000));
								else
									sleep(Calculations.random(500, 2000));
							}
						//}
					}
				}
			}
			break;
		case TRADING:
			if (getBank().isOpen()) {
				getBank().close();
				sleepUntil(() -> !getBank().isOpen(), Calculations.random(2000, 5000));
			} else if (getGrandExchange().isOpen()) { // Checks if the grand exchange interface is opened
				if (getInventory().contains(PIZZA_BASE + 1)) { // Checks if there's noted pizza base's in your inventory
					if (getGrandExchange().isSellOpen()) { // Checks if selling interface is opened
						if (getGrandExchange().getCurrentChosenItem().getID() == PIZZA_BASE) {
							if (sellPrice == -1) {
								//int price = getGrandExchange().getCurrentPrice();
								int sellPrice2 = buyPriceWater + buyPriceFlour + 30;//(int) (price * 0.1);
								getGrandExchange().setPrice(sellPrice2);
								sleepUntil(() -> getGrandExchange().getCurrentPrice() == sellPrice2, Calculations.random(3000, 5000));
								if (getGrandExchange().getCurrentPrice() == sellPrice2)
									sellPrice = sellPrice2;
							}
							if (sellPrice > 0) {
								getGrandExchange().confirm();
								sleep(500, 700);
								sellPrice = -1;
							}
						} else {
							getGrandExchange().addSellItem(getInventory().get(PIZZA_BASE + 1).getName().toLowerCase());
							sleepUntil(() -> getGrandExchange().isSellOpen(), Calculations.random(3000, 5000));
						}
					} else { // opens the selling interface
						getGrandExchange().addSellItem(getInventory().get(PIZZA_BASE + 1).getName().toLowerCase());
						sleepUntil(() -> getGrandExchange().isSellOpen(), Calculations.random(3000, 5000));
					}
				} else if (getGrandExchange().isReadyToCollect()) { // Checks if there's items to be collected
					getGrandExchange().collect();
					sleepUntil(() -> !getGrandExchange().isReadyToCollect(), Calculations.random(3000, 5000));
				} else if (!getGrandExchange().slotContainsItem(0) && !getInventory().contains(PIZZA_BASE + 1) && getInventory().contains(995) && (!getInventory().contains(JUG_OF_WATER + 1) || !getInventory().contains(POT_OF_FLOUR + 1))) {
					if (getGrandExchange().isBuyOpen()) {
						if (getGrandExchange().getCurrentChosenItemID() == JUG_OF_WATER || getGrandExchange().getCurrentChosenItemID() == POT_OF_FLOUR) {
							if (buyPriceWaterAmount == -1 || buyPriceWaterAmount == -1) {
								//int price = getGrandExchange().getCurrentPrice();
								int buyPrice2 = getGrandExchange().getCurrentChosenItemID() == POT_OF_FLOUR ? buyPriceFlour : buyPriceWater;//price + (int) (price * 0.12);
								getGrandExchange().setPrice(buyPrice2);
								sleepUntil(() -> getGrandExchange().getCurrentPrice() == buyPrice2, Calculations.random(3000, 5000));
								if (!getInventory().contains(JUG_OF_WATER + 1) && !getInventory().contains(POT_OF_FLOUR + 1))
									buyAmount = getInventory().count(995) / (buyPriceWater + buyPriceFlour);
								int amount = buyAmount;//(getInventory().count(995) - 10) / buyPrice2;
								if (amount > 0) {
									getGrandExchange().setQuantity(amount);
									sleepUntil(() -> getGrandExchange().getCurrentAmount() == amount, Calculations.random(3000, 5000));
									if (getGrandExchange().getCurrentPrice() == buyPrice2 && getGrandExchange().getCurrentAmount() == amount)
										if (getGrandExchange().getCurrentChosenItemID() == JUG_OF_WATER)
											buyPriceWaterAmount = buyPrice2;
										else
											buyPriceFlourAmount = buyPrice2;
								} else {
									log("Not enough money");
									stop();
								}
							}
							if (buyPriceWaterAmount > 0 || buyPriceFlourAmount > 0) {
								getGrandExchange().confirm();
								sleep(500, 700);
								buyPriceWaterAmount = -1;
								buyPriceWaterAmount = -1;
							}
						} else {
							if (!getInventory().contains(JUG_OF_WATER + 1)) {
								getGrandExchange().addBuyItem("Jug of water");
								sleepUntil(() -> getGrandExchange().getCurrentChosenItemID() == JUG_OF_WATER, Calculations.random(3000, 5000));
							} else if (!getInventory().contains(POT_OF_FLOUR + 1)) {
								getGrandExchange().addBuyItem("Pot of flour");
								sleepUntil(() -> getGrandExchange().getCurrentChosenItemID() == POT_OF_FLOUR, Calculations.random(3000, 5000));
							}
						}
					} else {
						getGrandExchange().openBuyScreen(0);
						sleepUntil(() -> getGrandExchange().isBuyOpen(), Calculations.random(3000, 5000));
					}
				}
			} else if (!getGrandExchange().isOpen()) {
				getGrandExchange().open();
				sleepUntil(() -> getGrandExchange().isOpen(), Calculations.random(3000, 5000));
			}
			break;
		case LOGOUT:
			getTabs().logout();
			sleep(1000);
			stop();
			break;
		default:
			break;
		}
		return 0;
	}
	
	public void onPaint(Graphics2D g) {
		int x = 25;
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", 1, 11));
		Utilities.drawShadowString(g, "Time Running: " + totalTime.formatTime(), x, 50);
		Utilities.drawShadowString(g, "Stage: " + state, x, 60);
		Utilities.drawShadowString(g, "Pizzas made: " + createdAmount, x, 70);
	}
	
	private State getState() {
		if (bankedPots > 13000)
			return State.LOGOUT;
		if ((bankedWaters == -1 || bankedFlours == -1) && !getGrandExchange().isOpen())
			return State.INIT;
		if (getInventory().contains(PIZZA_BASE + 1) || getGrandExchange().isOpen() && (!getInventory().contains(JUG_OF_WATER + 1) || !getInventory().contains(POT_OF_FLOUR + 1)))
			return State.TRADING;
		if (!getInventory().contains(JUG_OF_WATER) || getInventory().contains(JUG_OF_WATER + 1) || !getInventory().contains(POT_OF_FLOUR) || getInventory().contains(POT_OF_FLOUR + 1))
			if (!getBank().contains(PIZZA_BASE) && !getInventory().contains(PIZZA_BASE) && (!getInventory().contains(JUG_OF_WATER + 1) && !getInventory().contains(JUG_OF_WATER) && !getBank().contains(JUG_OF_WATER) || !getInventory().contains(POT_OF_FLOUR + 1) && !getInventory().contains(POT_OF_FLOUR) && !getBank().contains(POT_OF_FLOUR)) && getInventory().contains(995))
				return State.TRADING;
			else
				return State.BANKING;
		if (getInventory().contains(JUG_OF_WATER) && getInventory().contains(POT_OF_FLOUR))
			return State.SMACKING;
		return State.NOTHING;
	}
	
	public Item getLastItem() {
		for (int i = getInventory().size() - 1; i >= 0; i--) {
			if (getInventory().getIdForSlot(i) == JUG_OF_WATER)
				return getInventory().getItemInSlot(i);
		}
		return null;
	}
}
