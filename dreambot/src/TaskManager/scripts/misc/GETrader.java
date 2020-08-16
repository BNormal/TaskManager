package TaskManager.scripts.misc;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.walking.web.node.impl.bank.WebBankArea;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.dreambot.core.Instance;

import TaskManager.Script;
import TaskManager.scripts.misc.GETraderGUI.OfferItem;
import TaskManager.utilities.Utilities;

@ScriptManifest(author = "NumberZ", category = Category.MISC, name = "GE Trader (unf)", version = 1.0, description = "Buy and sell items at the Grand Exchange")
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

	private enum OfferStatus {
		MAKING_OFFER, WAITING_ON_OFFER
	}
	
	private enum State {
		WALKING, BUYING, SELLING, SETUP, NOTHING
	}
	
	private State getState() {
		if (!WebBankArea.GRAND_EXCHANGE.getArea().contains(engine.getLocalPlayer()))
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
		gui = new GETraderGUI(getManifest().name());
		gui.open();
	}
	
	@Override
	public void onStart() {
		if (!taskScript)
			init();
		super.onStart();
		if (engine == null)
			engine = this;
	}
	
	@Override
	public int onLoop() {
		if (!running || !gui.isFinished() || !engine.getLocalPlayer().isOnScreen() || Instance.getInstance().isMouseInputEnabled())
			return 0;
		if (offers == null)
			offers = gui.getOfferItems();
		state = getState();
		switch (state) {
		case WALKING:
			engine.getWalking().walk(WebBankArea.GRAND_EXCHANGE.getArea().getCenter().getRandomizedTile(2));
			if (Calculations.random(0, 20) > 2)
				sleepUntil(() -> engine.getWalking().getDestinationDistance() < Calculations.random(6, 9), 6000);
			break;
		case SETUP:
			if (offers.size() == 0) {
				if (engine.getGrandExchange().isOpen()) {
					engine.getGrandExchange().close();
					sleepUntil(() -> !engine.getGrandExchange().isOpen(), 6000);
				} else if (!engine.getBank().isOpen()) {
					engine.getBank().openClosest();
					sleepUntil(() -> engine.getBank().isOpen(), 6000);
				} else if (engine.getBank().isOpen()) {
					engine.getBank().depositAllItems();
					sleepUntil(() -> engine.getInventory().emptySlotCount() == 28, 6000);
					onExit();
				}
			}
			else {
				item = offers.get(0);
			}
			break;
			
		/********************************************BUYING****************************************************/	
			
		case BUYING:
			if (engine.getInventory().count(COINS) < item.getPrice() && !engine.getGrandExchange().isBuyOpen())  {
				if (bankGP == -1) {
					if (engine.getGrandExchange().isOpen()) {
						engine.getGrandExchange().close();
						sleepUntil(() -> !engine.getGrandExchange().isOpen(), 6000);
					} else if (!engine.getBank().isOpen()) {
						engine.getBank().openClosest();
						sleepUntil(() -> engine.getBank().isOpen(), 6000);
					} else if (engine.getBank().isOpen() && engine.getBank().contains(COINS)) {
						bankGP = engine.getBank().count(COINS);
						if (bankGP + engine.getInventory().count(COINS) > item.getPrice()) {
							if (engine.getInventory().contains(COINS) || engine.getInventory().emptySlotCount() > 0) {
								engine.getBank().getChild(COINS).interact("Withdraw-All-but-1");
								bankGP = 1;
								sleepUntil(() -> engine.getBank().count(COINS) <= 1, 6000);
							} else {
								bankGP = -1;
								engine.getBank().depositAllItems();
								sleepUntil(() -> engine.getInventory().emptySlotCount() == 28, 6000);
							}
						} else {//not enough money
							//do nothing here, code will fix itself
						}
					}
				} else {//failed to have enough money for this item
					if (engine.getInventory().count(COINS) + bankGP < item.getPrice()) {
						offers.remove(0);
						reset();
					} else {
						bankGP = -1;//reset so the bot will grab the money
					}
				}
			} else if (engine.getBank().isOpen()) {
				engine.getBank().close();
				sleepUntil(() -> !engine.getBank().isOpen(), 6000);
			} else if (!engine.getGrandExchange().isOpen()) {
				engine.getGrandExchange().open();
				sleepUntil(() -> engine.getGrandExchange().isOpen(), 6000);
			} else if (status == OfferStatus.MAKING_OFFER) {
				if (engine.getGrandExchange().isGeneralOpen()) {
					slot = engine.getGrandExchange().getFirstOpenSlot();
					if (slot > -1) {
						engine.getGrandExchange().openBuyScreen(slot);
						sleepUntil(() -> engine.getGrandExchange().isBuyOpen(), 6000);
					} else {//there's no open slots
						if (engine.getGrandExchange().isReadyToCollect()) {
							engine.getGrandExchange().collect();
							sleep(Calculations.random(1000, 2000));
						}
					}
				} else if (engine.getGrandExchange().isBuyOpen()) {
					if (engine.getGrandExchange().getCurrentChosenItem() == null) {
						engine.getGrandExchange().addBuyItem(item.getItem().getName());
						sleepUntil(() -> engine.getGrandExchange().getCurrentChosenItemID() == item.getItem().getID(), 6000);
					} else if (engine.getGrandExchange().getCurrentChosenItemID() == item.getItem().getID()) {
						if (engine.getGrandExchange().getCurrentAmount() != (int) item.getQuantity()) {
							engine.getGrandExchange().setQuantity((int) item.getQuantity());
							sleepUntil(() -> engine.getGrandExchange().getCurrentAmount() == (int) item.getQuantity(), 6000);
						}
						if (item.getIncrements().size() > 0) {
							for (int increment : item.getIncrements()) {
								switch (increment) {
								case -1://increase
									WidgetChild plusFive = engine.getWidgets().getWidgetChild(465, 24, 13);
									if (plusFive != null) {
										plusFive.interact();
										sleep(50, 150);
									}
									break;
								case -2://decrease
									WidgetChild minusFive = engine.getWidgets().getWidgetChild(465, 24, 10);
									if (minusFive != null) {
										minusFive.interact();
										sleep(50, 150);
									}
									break;
								case -3://reset
									WidgetChild resetPrice = engine.getWidgets().getWidgetChild(465, 24, 11);
									if (resetPrice != null) {
										resetPrice.interact();
										sleep(50, 150);
									}
									break;
								}
								if (increment > 0) {//custom price
									engine.getGrandExchange().setPrice(increment);
									sleepUntil(() -> engine.getGrandExchange().getCurrentPrice() == increment, 6000);
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
							engine.getGrandExchange().setPrice((int) price);
							final long finalPrice = price;
							sleepUntil(() -> engine.getGrandExchange().getCurrentPrice() == finalPrice, 6000);
							
							
							
							WidgetChild plusFive = engine.getWidgets().getWidgetChild(465, 24, 13);
							for (int i = 0; i < increments; i++) {
								if (plusFive != null) {
									plusFive.interact();
									sleep(50, 150);
								}
							}
						}
						if (engine.getGrandExchange().getCurrentPrice() != (int) item.getPrice() && increments == 0) {
							engine.getGrandExchange().setPrice((int) item.getPrice());
							sleepUntil(() -> engine.getGrandExchange().getCurrentPrice() == (int) item.getPrice(), 6000);
						}
						engine.getGrandExchange().confirm();
						sleepUntil(() -> engine.getGrandExchange().isGeneralOpen(), 6000);
						if (engine.getGrandExchange().slotContainsItem(slot)) {
							status = OfferStatus.WAITING_ON_OFFER;
							waitTimer = new Timer();
						}
					}
				}
			} else if (status == OfferStatus.WAITING_ON_OFFER) {
				if (!hasCancelled) {
					if (engine.getGrandExchange().isReadyToCollect(slot)) {
						engine.getGrandExchange().collect();
						sleep(1000, 2000);
						if (!engine.getGrandExchange().slotContainsItem(slot)) {//completed order
							status = OfferStatus.MAKING_OFFER;
							offers.remove(0);
							reset();
						}
					}
					if (item != null && !item.isWaitUntilCompleted()) {//skip to next purchase
						status = OfferStatus.MAKING_OFFER;
						offers.remove(0);
						reset();
					} else if (increments <= item.getPriceChanges()) {
						if (waitTime <= 0)
							waitTime = Calculations.random(3000, 6000);
						if (waitTimer.elapsed() > waitTime) {
							increments++;
							waitTime = -1;
							engine.getGrandExchange().cancelOffer(slot);
							hasCancelled = true;
							sleepUntil(() -> engine.getWidgets().getWidgetChild(465, 23, 2) != null && engine.getWidgets().getWidgetChild(465, 23, 2).getItemStack() > 0, 6000);
						}
					} else {//just waiting for item to buy
						if (waitTimer.elapsed() > 60000) {//cancel this order and move on
							waitTime = -1;
							engine.getGrandExchange().cancelOffer(slot);
							hasCancelled = true;
							sleepUntil(() -> engine.getWidgets().getWidgetChild(465, 23, 2) != null && engine.getWidgets().getWidgetChild(465, 23, 2).getItemStack() > 0, 6000);
						}
					}
				} else {
					WidgetChild collectItem = engine.getWidgets().getWidgetChild(465, 23, 2);
					WidgetChild back = engine.getWidgets().getWidgetChild(465, 4);
					if (collectItem != null && collectItem.isVisible()) {
						if (collectItem.getItemStack() > 0) {
							sleep(1000, 1500);
							collectItem.interact("Collect");
							sleepUntil(() -> !engine.getGrandExchange().isBuyOpen(), 2000);
							if (!engine.getGrandExchange().isBuyOpen()) {
								hasCancelled = false;
								status = OfferStatus.MAKING_OFFER;
								if (increments > item.getPriceChanges()) {
									offers.remove(0);
									reset();
								}
							} else {
								hasCancelled = false;
								back.interact();
								sleepUntil(() -> !engine.getGrandExchange().isBuyOpen(), 2000);
							}
						}
					}
				}
			}
			break;
			
		/********************************************SELLING****************************************************/	
			
		case SELLING:
			if (engine.getEquipment().contains(item.getItem().getID()) && !engine.getGrandExchange().isOpen()) {//remove equipment
				engine.getTabs().open(Tab.EQUIPMENT);
				sleepUntil(() -> engine.getTabs().isOpen(Tab.EQUIPMENT), 2000);
				if (engine.getTabs().isOpen(Tab.EQUIPMENT)) {
					for (EquipmentSlot slot : EquipmentSlot.values()) {
						if (engine.getEquipment().getIdForSlot(slot.getSlot()) == item.getItem().getID()) {
							engine.getEquipment().unequip(slot);
							sleepUntil(() -> engine.getInventory().contains(item.getItem().getID()), 2000);
							break;
						}
					}
				}
				engine.getTabs().open(Tab.INVENTORY);
				sleepUntil(() -> engine.getTabs().isOpen(Tab.INVENTORY), 2000);
			} else if (engine.getInventory().count(item.getItem().getID()) < item.getQuantity() && !engine.getGrandExchange().isOpen())  {
				if (engine.getGrandExchange().isOpen()) {//open GE
					engine.getGrandExchange().close();
					sleepUntil(() -> !engine.getGrandExchange().isOpen(), 6000);
				} else if (!engine.getBank().isOpen()) {//open bank
					engine.getBank().openClosest();
					sleepUntil(() -> engine.getBank().isOpen(), 6000);
				} else if (engine.getBank().isOpen()) {//bank is open
					int inventory = engine.getInventory().count(item.getItem().getID());
					int bank = engine.getBank().count(item.getItem().getID());
					int quantity = inventory + bank;
					if (quantity < item.getQuantity())
						item.setQuantity(quantity);
					if (inventory < item.getQuantity() && bank > 0) {
						engine.getBank().withdraw(item.getItem().getID(), (int) (item.getQuantity() - inventory));
						sleepUntil(() -> engine.getInventory().count(item.getItem().getID()) == (int) item.getQuantity(), 6000);
					}
				}
			} else if (engine.getBank().isOpen()) {
				engine.getBank().close();
				sleepUntil(() -> !engine.getBank().isOpen(), 6000);
			} else if (!engine.getGrandExchange().isOpen()) {
				engine.getGrandExchange().open();
				sleepUntil(() -> engine.getGrandExchange().isOpen(), 6000);
			} else if (engine.getInventory().count(item.getItem().getID()) >= item.getQuantity() || slot > -1 && engine.getGrandExchange().slotContainsItem(slot)) {
				if (status == OfferStatus.MAKING_OFFER) {
					if (engine.getGrandExchange().isGeneralOpen()) {
						slot = engine.getGrandExchange().getFirstOpenSlot();
						if (slot > -1) {
							engine.getGrandExchange().openSellScreen(slot);
							sleepUntil(() -> engine.getGrandExchange().isSellOpen(), 6000);
						} else {//there's no open slots
							if (engine.getGrandExchange().isReadyToCollect()) {
								engine.getGrandExchange().collect();
								sleep(Calculations.random(1000, 2000));
							}
						}
					} else if (engine.getGrandExchange().isSellOpen()) {
						if (engine.getGrandExchange().getCurrentChosenItem() == null) {
							engine.getGrandExchange().addSellItem(item.getItem().getName());
							sleepUntil(() -> engine.getGrandExchange().getCurrentChosenItemID() == item.getItem().getID(), 6000);
						} else if (engine.getGrandExchange().getCurrentChosenItemID() == item.getItem().getID()) {
							if (engine.getGrandExchange().getCurrentAmount() != (int) item.getQuantity()) {
								engine.getGrandExchange().setQuantity((int) item.getQuantity());
								sleepUntil(() -> engine.getGrandExchange().getCurrentAmount() == (int) item.getQuantity(), 6000);
							}
							if (item.getIncrements().size() > 0 && increments == 0) {
								for (int increment : item.getIncrements()) {
									switch (increment) {
									case -1://increase
										WidgetChild plusFive = engine.getWidgets().getWidgetChild(465, 24, 13);
										if (plusFive != null) {
											plusFive.interact();
											sleep(50, 150);
										}
										break;
									case -2://decrease
										WidgetChild minusFive = engine.getWidgets().getWidgetChild(465, 24, 10);
										if (minusFive != null) {
											minusFive.interact();
											sleep(50, 150);
										}
										break;
									case -3://reset
										WidgetChild resetPrice = engine.getWidgets().getWidgetChild(465, 24, 11);
										if (resetPrice != null) {
											resetPrice.interact();
											sleep(50, 150);
										}
										break;
									}
									if (increment > 0) {//custom price
										engine.getGrandExchange().setPrice(increment);
										sleepUntil(() -> engine.getGrandExchange().getCurrentPrice() == increment, 6000);
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
								engine.getGrandExchange().setPrice((int) price);
								final long finalPrice = price;
								sleepUntil(() -> engine.getGrandExchange().getCurrentPrice() == finalPrice, 6000);
							}
							if (engine.getGrandExchange().getCurrentPrice() != (int) item.getPrice() && increments == 0) {
								engine.getGrandExchange().setPrice((int) item.getPrice());
								sleepUntil(() -> engine.getGrandExchange().getCurrentPrice() == (int) item.getPrice(), 6000);
							}
							engine.getGrandExchange().confirm();
							sleepUntil(() -> engine.getGrandExchange().isGeneralOpen(), 6000);
							if (engine.getGrandExchange().slotContainsItem(slot)) {
								status = OfferStatus.WAITING_ON_OFFER;
								waitTimer = new Timer();
							}
						}
					}
				} else if (status == OfferStatus.WAITING_ON_OFFER) {
					if (!hasCancelled) {
						if (engine.getGrandExchange().isReadyToCollect(slot)) {
							engine.getGrandExchange().collect();
							sleep(1000, 2000);
							if (!engine.getGrandExchange().slotContainsItem(slot)) {//completed order
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
								engine.getGrandExchange().cancelOffer(slot);
								hasCancelled = true;
								sleepUntil(() -> engine.getWidgets().getWidgetChild(465, 23, 2) != null && engine.getWidgets().getWidgetChild(465, 23, 2).getItemStack() > 0, 6000);
							}
						} else {//just waiting for item to sell
							if (waitTimer.elapsed() > 60000) {//cancel this order and move on
								waitTime = -1;
								engine.getGrandExchange().cancelOffer(slot);
								hasCancelled = true;
								sleepUntil(() -> engine.getWidgets().getWidgetChild(465, 23, 2) != null && engine.getWidgets().getWidgetChild(465, 23, 2).getItemStack() > 0, 6000);
							} else {
								
							}
						}
					} else {
						WidgetChild collectItem = engine.getWidgets().getWidgetChild(465, 23, 2);
						WidgetChild back = engine.getWidgets().getWidgetChild(465, 4);
						if (collectItem != null && collectItem.isVisible()) {
							if (collectItem.getItemStack() > 0) {
								sleep(1000, 1500);
								if (collectItem.getItemStack() > 1)
									collectItem.interact("Collect-note");
								else
									collectItem.interact("Collect-item");
								sleepUntil(() -> !engine.getGrandExchange().isSellOpen(), 2000);
								if (!engine.getGrandExchange().isSellOpen()) {
									hasCancelled = false;
									status = OfferStatus.MAKING_OFFER;
									if (increments > item.getPriceChanges()) {
										offers.remove(0);
										reset();
									}
								} else {
									hasCancelled = false;
									back.interact();
									sleepUntil(() -> !engine.getGrandExchange().isSellOpen(), 2000);
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
		//Utilities.drawShadowString(g, "Stage: " + engine.getGrandExchange().isBuyOpen() + " : " + engine.getGrandExchange().isGeneralOpen(), x, 60);
		//Utilities.drawShadowString(g, "Stage: ", x, 60);
		
	}

	@Override
	public void onExit() {
		gui.exit();
		super.onExit();
	}
	
}
