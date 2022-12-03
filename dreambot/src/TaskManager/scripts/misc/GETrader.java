package TaskManager.scripts.misc;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.Category;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.dreambot.core.Instance;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import TaskManager.Script;
import TaskManager.ScriptDetails;
import TaskManager.scripts.misc.GETraderGUI.OfferItem;
import TaskManager.utilities.Utilities;

@ScriptDetails(author = "NumberZ", category = Category.MISC, name = "GE Trader", version = 1.0, description = "Buy and sell items at the Grand Exchange.")
public class GETrader extends Script {
	private State state = null;
	private GETraderGUI gui;
	private ArrayList<OfferItem> offers = null;
	private OfferItem item = null;
	private int bankGP = -1;
	private int slot = -1;
	private int increments = 0;
	private Timer waitTimer;
	private long waitTime = -1;
	private boolean hasCancelled = false;
	private OfferStatus status = OfferStatus.MAKING_OFFER;
	private int COINS = 995;

	public GETrader() {
		gui = new GETraderGUI(getScriptDetails().name());
	}
	
	private enum OfferStatus {
		MAKING_OFFER, WAITING_ON_OFFER
	}
	
	private enum State {
		WALKING, BUYING, SELLING, SETUP, NOTHING
	}
	
	private State getState() {
		if (!BankLocation.GRAND_EXCHANGE.getArea(1).contains(Players.getLocal()))
			return State.WALKING;
		if (item == null)
			return State.SETUP;
		else if (item != null) {
			if (item.isBuying())
				return State.BUYING;
			else
				return State.SELLING;
		}
		return State.NOTHING;
	}
	
	@Override
	public void init() {
		gui.open();
	}
	
	@Override
	public void onStart() {
		if (!taskScript)
			init();
		super.onStart();
	}
	
	@Override
	public int onLoop() {
		if (!running || !gui.isFinished() || !Players.getLocal().isOnScreen() || Instance.isMouseInputEnabled())
			return 0;
		if (offers == null)
			offers = gui.getOfferItems();
		state = getState();
		switch (state) {
		case WALKING:
			Walking.walk(BankLocation.GRAND_EXCHANGE.getArea(1).getCenter().getRandomizedTile(2));
			if (Calculations.random(0, 20) > 2)
				Sleep.sleepUntil(() -> Walking.getDestinationDistance() < Calculations.random(6, 9), 6000);
			break;
		case SETUP:
			if (offers.size() == 0) {
				if (GrandExchange.isOpen()) {
					GrandExchange.close();
					Sleep.sleepUntil(() -> !GrandExchange.isOpen(), 6000);
				} else if (!Bank.isOpen()) {
					Bank.open();
					Sleep.sleepUntil(() -> Bank.isOpen(), 6000);
				} else if (Bank.isOpen()) {
					Bank.depositAllItems();
					Sleep.sleepUntil(() -> Inventory.emptySlotCount() == 28, 6000);
					onExit();
				}
			}
			else {
				item = offers.get(0);
			}
			break;
			
		/********************************************BUYING****************************************************/	
			
		case BUYING:
			if (Inventory.count(COINS) < item.getPrice() && !GrandExchange.isBuyOpen())  {
				if (bankGP == -1) {
					if (GrandExchange.isOpen()) {
						GrandExchange.close();
						Sleep.sleepUntil(() -> !GrandExchange.isOpen(), 6000);
					} else if (!Bank.isOpen()) {
						Bank.open();
						Sleep.sleepUntil(() -> Bank.isOpen(), 6000);
					} else if (Bank.isOpen() && Bank.contains(COINS)) {
						bankGP = Bank.count(COINS);
						if (bankGP + Inventory.count(COINS) > item.getPrice()) {
							if (Inventory.contains(COINS) || Inventory.emptySlotCount() > 0) {
								Bank.getChild(COINS).interact("Withdraw-All-but-1");
								bankGP = 1;
								Sleep.sleepUntil(() -> Bank.count(COINS) <= 1, 6000);
							} else {
								bankGP = -1;
								Bank.depositAllItems();
								Sleep.sleepUntil(() -> Inventory.emptySlotCount() == 28, 6000);
							}
						} else {//not enough money
							//do nothing here, code will fix itself
						}
					}
				} else {//failed to have enough money for this item
					if (Inventory.count(COINS) + bankGP < item.getPrice()) {
						offers.remove(0);
						reset();
					} else {
						bankGP = -1;//reset so the bot will grab the money
					}
				}
			} else if (Bank.isOpen()) {
				Bank.close();
				Sleep.sleepUntil(() -> !Bank.isOpen(), 6000);
			} else if (!GrandExchange.isOpen()) {
				GrandExchange.open();
				Sleep.sleepUntil(() -> GrandExchange.isOpen(), 6000);
			} else if (status == OfferStatus.MAKING_OFFER) {
				if (GrandExchange.isGeneralOpen()) {
					slot = GrandExchange.getFirstOpenSlot();
					if (slot > -1) {
						GrandExchange.openBuyScreen(slot);
						Sleep.sleepUntil(() -> GrandExchange.isBuyOpen(), 6000);
					} else {//there's no open slots
						if (GrandExchange.isReadyToCollect()) {
							GrandExchange.collect();
							sleep(Calculations.random(1000, 2000));
						}
					}
				} else if (GrandExchange.isBuyOpen()) {
					if (GrandExchange.getCurrentChosenItem() == null) {
						GrandExchange.addBuyItem(item.getItem().getName());
						Sleep.sleepUntil(() -> GrandExchange.getCurrentChosenItemID() == item.getItem().getID(), 6000);
					} else if (GrandExchange.getCurrentChosenItemID() == item.getItem().getID()) {
						if (GrandExchange.getCurrentAmount() != (int) item.getQuantity()) {
							GrandExchange.setQuantity((int) item.getQuantity());
							Sleep.sleepUntil(() -> GrandExchange.getCurrentAmount() == (int) item.getQuantity(), 6000);
						}
						if (item.getIncrements().size() > 0) {
							for (int increment : item.getIncrements()) {
								switch (increment) {
								case -1://increase
									WidgetChild plusFive = Widgets.getWidgetChild(465, 24, 13);
									if (plusFive != null) {
										plusFive.interact();
										sleep(50, 150);
									}
									break;
								case -2://decrease
									WidgetChild minusFive = Widgets.getWidgetChild(465, 24, 10);
									if (minusFive != null) {
										minusFive.interact();
										sleep(50, 150);
									}
									break;
								case -3://reset
									WidgetChild resetPrice = Widgets.getWidgetChild(465, 24, 11);
									if (resetPrice != null) {
										resetPrice.interact();
										sleep(50, 150);
									}
									break;
								}
								if (increment > 0) {//custom price
									GrandExchange.setPrice(increment);
									Sleep.sleepUntil(() -> GrandExchange.getCurrentPrice() == increment, 6000);
								}
							}
						}
						if (item.getPriceChanges() > -1 && increments <= item.getPriceChanges() && increments != 0) {
							//handle price increases here
							long price = item.getPrice();
							for (int i = 0; i < increments; i++) {
								price += price * Calculations.random(0.03, 0.05);
							}
							if (price < 1)
								price = 1;
							else if (price > Integer.MAX_VALUE)
								price = Integer.MAX_VALUE;
							GrandExchange.setPrice((int) price);
							final long finalPrice = price;
							Sleep.sleepUntil(() -> GrandExchange.getCurrentPrice() == finalPrice, 6000);
							
							
							
							WidgetChild plusFive = Widgets.getWidgetChild(465, 24, 13);
							for (int i = 0; i < increments; i++) {
								if (plusFive != null) {
									plusFive.interact();
									sleep(50, 150);
								}
							}
						}
						if (GrandExchange.getCurrentPrice() != (int) item.getPrice() && increments == 0) {
							GrandExchange.setPrice((int) item.getPrice());
							Sleep.sleepUntil(() -> GrandExchange.getCurrentPrice() == (int) item.getPrice(), 6000);
						}
						GrandExchange.confirm();
						Sleep.sleepUntil(() -> GrandExchange.isGeneralOpen(), 6000);
						if (GrandExchange.slotContainsItem(slot)) {
							status = OfferStatus.WAITING_ON_OFFER;
							waitTimer = new Timer();
						}
					}
				}
			} else if (status == OfferStatus.WAITING_ON_OFFER) {
				if (!hasCancelled) {
					if (GrandExchange.isReadyToCollect(slot)) {
						GrandExchange.collect();
						sleep(1000, 2000);
						if (!GrandExchange.slotContainsItem(slot)) {//completed order
							status = OfferStatus.MAKING_OFFER;
							offers.remove(0);
							reset();
						}
					}
					if (item != null && !item.isWaitUntilCompleted()) {//skip to next purchase
						status = OfferStatus.MAKING_OFFER;
						offers.remove(0);
						reset();
					} else if (item != null && increments <= item.getPriceChanges()) {
						if (waitTime <= 0)
							waitTime = Calculations.random(3000, 6000);
						if (waitTimer.elapsed() > waitTime) {
							increments++;
							waitTime = -1;
							GrandExchange.cancelOffer(slot);
							hasCancelled = true;
							Sleep.sleepUntil(() -> Widgets.getWidgetChild(465, 23, 2) != null && Widgets.getWidgetChild(465, 23, 2).getItemStack() > 0, 6000);
						}
					} else {//just waiting for item to buy
						if (waitTimer.elapsed() > 60000) {//cancel this order and move on
							waitTime = -1;
							GrandExchange.cancelOffer(slot);
							hasCancelled = true;
							Sleep.sleepUntil(() -> Widgets.getWidgetChild(465, 23, 2) != null && Widgets.getWidgetChild(465, 23, 2).getItemStack() > 0, 6000);
						}
					}
				} else {
					WidgetChild collectItem = Widgets.getWidgetChild(465, 23, 2);
					WidgetChild back = Widgets.getWidgetChild(465, 4);
					if (collectItem != null && collectItem.isVisible()) {
						if (collectItem.getItemStack() > 0) {
							sleep(1000, 1500);
							collectItem.interact("Collect");
							Sleep.sleepUntil(() -> !GrandExchange.isBuyOpen(), 2000);
							if (!GrandExchange.isBuyOpen()) {
								hasCancelled = false;
								status = OfferStatus.MAKING_OFFER;
								if (increments > item.getPriceChanges()) {
									offers.remove(0);
									reset();
								}
							} else {
								hasCancelled = false;
								back.interact();
								Sleep.sleepUntil(() -> !GrandExchange.isBuyOpen(), 2000);
							}
						}
					}
				}
			}
			break;
			
		/********************************************SELLING****************************************************/	
			
		case SELLING:
			if (Equipment.contains(item.getItem().getID()) && !GrandExchange.isOpen()) {//remove equipment
				Tabs.open(Tab.EQUIPMENT);
				Sleep.sleepUntil(() -> Tabs.isOpen(Tab.EQUIPMENT), 2000);
				if (Tabs.isOpen(Tab.EQUIPMENT)) {
					for (EquipmentSlot slot : EquipmentSlot.values()) {
						if (Equipment.getIdForSlot(slot.getSlot()) == item.getItem().getID()) {
							Equipment.unequip(slot);
							Sleep.sleepUntil(() -> Inventory.contains(item.getItem().getID()), 2000);
							break;
						}
					}
				}
				Tabs.open(Tab.INVENTORY);
				Sleep.sleepUntil(() -> Tabs.isOpen(Tab.INVENTORY), 2000);
			} else if (Inventory.count(item.getItem().getID()) < item.getQuantity() && !GrandExchange.isOpen())  {
				if (GrandExchange.isOpen()) {//open GE
					GrandExchange.close();
					Sleep.sleepUntil(() -> !GrandExchange.isOpen(), 6000);
				} else if (!Bank.isOpen()) {//open bank
					Bank.open();
					Sleep.sleepUntil(() -> Bank.isOpen(), 6000);
				} else if (Bank.isOpen()) {//bank is open
					int inventory = Inventory.count(item.getItem().getID());
					int bank = Bank.count(item.getItem().getID());
					int quantity = inventory + bank;
					if (quantity < item.getQuantity())
						item.setQuantity(quantity);
					if (inventory < item.getQuantity() && bank > 0) {
						Bank.withdraw(item.getItem().getID(), (int) (item.getQuantity() - inventory));
						Sleep.sleepUntil(() -> Inventory.count(item.getItem().getID()) == (int) item.getQuantity(), 6000);
					}
				}
			} else if (Bank.isOpen()) {
				Bank.close();
				Sleep.sleepUntil(() -> !Bank.isOpen(), 6000);
			} else if (!GrandExchange.isOpen()) {
				GrandExchange.open();
				Sleep.sleepUntil(() -> GrandExchange.isOpen(), 6000);
			} else if (Inventory.count(item.getItem().getID()) >= item.getQuantity() || slot > -1 && GrandExchange.slotContainsItem(slot)) {
				if (status == OfferStatus.MAKING_OFFER) {
					if (GrandExchange.isGeneralOpen()) {
						slot = GrandExchange.getFirstOpenSlot();
						if (slot > -1) {
							GrandExchange.openSellScreen(slot);
							Sleep.sleepUntil(() -> GrandExchange.isSellOpen(), 6000);
						} else {//there's no open slots
							if (GrandExchange.isReadyToCollect()) {
								GrandExchange.collect();
								sleep(Calculations.random(1000, 2000));
							}
						}
					} else if (GrandExchange.isSellOpen()) {
						if (GrandExchange.getCurrentChosenItem() == null) {
							GrandExchange.addSellItem(item.getItem().getName());
							Sleep.sleepUntil(() -> GrandExchange.getCurrentChosenItemID() == item.getItem().getID(), 6000);
						} else if (GrandExchange.getCurrentChosenItemID() == item.getItem().getID()) {
							if (GrandExchange.getCurrentAmount() != (int) item.getQuantity()) {
								GrandExchange.setQuantity((int) item.getQuantity());
								Sleep.sleepUntil(() -> GrandExchange.getCurrentAmount() == (int) item.getQuantity(), 6000);
							}
							if (item.getIncrements().size() > 0 && increments == 0) {
								for (int increment : item.getIncrements()) {
									switch (increment) {
									case -1://increase
										WidgetChild plusFive = Widgets.getWidgetChild(465, 24, 13);
										if (plusFive != null) {
											plusFive.interact();
											sleep(50, 150);
										}
										break;
									case -2://decrease
										WidgetChild minusFive = Widgets.getWidgetChild(465, 24, 10);
										if (minusFive != null) {
											minusFive.interact();
											sleep(50, 150);
										}
										break;
									case -3://reset
										WidgetChild resetPrice = Widgets.getWidgetChild(465, 24, 11);
										if (resetPrice != null) {
											resetPrice.interact();
											sleep(50, 150);
										}
										break;
									}
									if (increment > 0) {//custom price
										GrandExchange.setPrice(increment);
										Sleep.sleepUntil(() -> GrandExchange.getCurrentPrice() == increment, 6000);
									}
								}
							}
							if (item.getPriceChanges() > -1 && increments <= item.getPriceChanges() && increments != 0) {
								//handle price decreases here
								long price = item.getPrice();
								for (int i = 0; i < increments; i++) {
									price -= price * Calculations.random(0.03, 0.05);
								}
								if (price < 1)
									price = 1;
								else if (price > Integer.MAX_VALUE)
									price = Integer.MAX_VALUE;
								GrandExchange.setPrice((int) price);
								final long finalPrice = price;
								Sleep.sleepUntil(() -> GrandExchange.getCurrentPrice() == finalPrice, 6000);
							}
							if (GrandExchange.getCurrentPrice() != (int) item.getPrice() && increments == 0) {
								GrandExchange.setPrice((int) item.getPrice());
								Sleep.sleepUntil(() -> GrandExchange.getCurrentPrice() == (int) item.getPrice(), 6000);
							}
							GrandExchange.confirm();
							Sleep.sleepUntil(() -> GrandExchange.isGeneralOpen(), 6000);
							if (GrandExchange.slotContainsItem(slot)) {
								status = OfferStatus.WAITING_ON_OFFER;
								waitTimer = new Timer();
							}
						}
					}
				} else if (status == OfferStatus.WAITING_ON_OFFER) {
					if (!hasCancelled) {
						if (GrandExchange.isReadyToCollect(slot)) {
							GrandExchange.collect();
							sleep(1000, 2000);
							if (!GrandExchange.slotContainsItem(slot)) {//completed order
								status = OfferStatus.MAKING_OFFER;
								offers.remove(0);
								reset();
							}
						} else if (!item.isWaitUntilCompleted()) {//skip to sell
							status = OfferStatus.MAKING_OFFER;
							offers.remove(0);
							reset();
						} else if (increments <= item.getPriceChanges()) {
							if (waitTime <= 0)
								waitTime = Calculations.random(3000, 6000);
							if (waitTimer.elapsed() > waitTime) {
								increments++;
								waitTime = -1;
								GrandExchange.cancelOffer(slot);
								hasCancelled = true;
								Sleep.sleepUntil(() -> Widgets.getWidgetChild(465, 23, 2) != null && Widgets.getWidgetChild(465, 23, 2).getItemStack() > 0, 6000);
							}
						} else {//just waiting for item to sell
							if (waitTimer.elapsed() > 60000) {//cancel this order and move on
								waitTime = -1;
								GrandExchange.cancelOffer(slot);
								hasCancelled = true;
								Sleep.sleepUntil(() -> Widgets.getWidgetChild(465, 23, 2) != null && Widgets.getWidgetChild(465, 23, 2).getItemStack() > 0, 6000);
							} else {
								
							}
						}
					} else {
						WidgetChild collectItem = Widgets.getWidgetChild(465, 23, 2);
						WidgetChild back = Widgets.getWidgetChild(465, 4);
						if (collectItem != null && collectItem.isVisible()) {
							if (collectItem.getItemStack() > 0) {
								sleep(1000, 1500);
								if (collectItem.getItemStack() > 1)
									collectItem.interact("Collect-note");
								else
									collectItem.interact("Collect-item");
								Sleep.sleepUntil(() -> !GrandExchange.isSellOpen(), 2000);
								if (!GrandExchange.isSellOpen()) {
									hasCancelled = false;
									status = OfferStatus.MAKING_OFFER;
									if (increments > item.getPriceChanges()) {
										offers.remove(0);
										reset();
									}
								} else {
									hasCancelled = false;
									back.interact();
									Sleep.sleepUntil(() -> !GrandExchange.isSellOpen(), 2000);
								}
							}
						}
					}
				}
			}
			break;
		default:
			break;
			
		}
		return 0;
	}
	
	@Override
	public String saveState() {
		String taskData = super.saveState();
		Gson gson = new GsonBuilder().create();
		List<String> preferences = new ArrayList<String>();
		preferences.add(taskData);
		preferences.add(gui.getSaveDate());
		return gson.toJson(preferences);
	}
	
	@Override
	public void loadState(String data) {
		Gson gson = new Gson();
		List<String> preferences = new ArrayList<String>();
		Type type = new TypeToken<List<String>>() {}.getType();
		preferences = gson.fromJson(data, type);
		setTaskScript(true);
		setTask(gson.fromJson(preferences.get(0), TaskManager.Task.class));
		gui.loadSaveDate(preferences.get(1));
	}
	
	public void reset() {
		item = null;
		slot = -1;
		increments = 0;
		status = OfferStatus.MAKING_OFFER;
	}
	@Override
	public void onPaint(Graphics2D g) {
		int x = 25;
		//int y = 25;
		g.setColor(new Color(0.0F, 0.0F, 0.0F, 0.2F));
		g.fillRect(20, 37, 200, 47);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", 1, 11));
		g.drawRect(20, 37, 200, 47);
		Utilities.drawShadowString(g, "Time Running: " + totalTime.formatTime(), x, 50);
		if (waitTimer != null)
			Utilities.drawShadowString(g, "status: " + status + " : " + waitTimer.elapsed(), x, 60);
		if (item != null)
			g.drawImage(item.getItem().getSprite().getImage(), x, 70, null);
		//Utilities.drawShadowString(g, "Stage: " + GrandExchange.isBuyOpen() + " : " + GrandExchange.isGeneralOpen(), x, 60);
		//Utilities.drawShadowString(g, "Stage: ", x, 60);
		
	}

	@Override
	public void onExit() {
		gui.exit();
		super.onExit();
	}
	
	@Override
	public String getSettingsDetails() {
		return gui.getSettingsDetails();
	}
	
}
