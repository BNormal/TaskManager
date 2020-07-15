package TaskManager.scripts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.utilities.impl.Condition;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import TaskManager.Script;

@ScriptManifest(author = "NumberZ", category = Category.CRAFTING, name = "Wool Spinner", version = 1.0, description = "Spins wool at Lumbridge Castle Does not gather wool.")
public class WoolSpinner extends Script {
	private Timer totalTime = new Timer();
	private Timer animDelay = new Timer();
	private int WOOL = 1737;
	private int BALL_OF_WOOL = 1759;
	private Tile BANK_TILE = new Tile(3209, 3220, 2);
	private Tile SPINNER_TILE = new Tile(3209, 3213, 1);
	private Area BANK_AREA = new Area(3207, 3216, 3210, 3221, 2);
	private Area SPINNER_AREA = new Area(3208, 3212, 3210, 3213, 1);
	private Area TOP_STAIRS = new Area(3205, 3208, 3207, 3210, 2);
	private String currentStage = "None";
	WidgetChild child1 = null;
	WidgetChild child2 = null;
	WidgetChild levelUp = null;
	private State state;

	
	@Override
	public void onStart() {
		super.onStart();
		if (engine == null)
			engine = this;
	}
	
	private enum State {
		BANKDEPOSIT, BANKWITHDRAW, WALK_TO_BANK, WALK_TO_SPINNER, SPIN_WOOL, ANTIBAN;
	}
	
	private State getState() {
		if (BANK_AREA.contains(engine.getLocalPlayer().getTile()) && !engine.getInventory().contains(WOOL)
				&& !engine.getInventory().contains(BALL_OF_WOOL) && engine.getInventory().emptySlotCount() < 28
				|| BANK_AREA.contains(engine.getLocalPlayer().getTile()) && !engine.getInventory().contains(WOOL)
						&& engine.getInventory().contains(BALL_OF_WOOL)) {
			return State.BANKDEPOSIT;
		}

		if (BANK_AREA.contains(engine.getLocalPlayer()) && engine.getInventory().emptySlotCount() == 28) {
			return State.BANKWITHDRAW;
		}

		if (engine.getInventory().count(WOOL) > 0 && !SPINNER_AREA.contains(engine.getLocalPlayer())) {
			return State.WALK_TO_SPINNER;
		}
		if (SPINNER_AREA.contains(engine.getLocalPlayer()) && engine.getInventory().contains(WOOL)) {
			levelUp = engine.getWidgets().getWidgetChild(233, 3);
			if (levelUp != null && levelUp.isVisible()) {
				levelUp.interact();
			}
			if (animDelay.elapsed() < 2000 && Calculations.random(1, 200) == 1) {
				currentStage = "Anti Ban";
				return State.ANTIBAN;
			}
			return State.SPIN_WOOL;
		}
		return State.WALK_TO_BANK;
	}
	
	@Override
	public int onLoop() {
		if (!running)
			running = true;
		if (engine.getLocalPlayer().isAnimating())
			animDelay.reset();
		state = getState();
		switch (state) {
		case BANKDEPOSIT:
			if (engine.getBank().isOpen()) {
				currentStage = "Depositing into bank.";
				engine.getBank().depositAllItems();
				sleepUntil(new Condition() {
					public boolean verify() {
						return engine.getInventory().onlyContains(i -> engine.getInventory().isEmpty());
					}

				}, Calculations.random(900, 1200));
				increaseRunCount();
			} else {
				currentStage = "Opening bank.";
				engine.getBank().open();
				sleepUntil(new Condition() {
					public boolean verify() {
						return engine.getBank().isOpen();
					}
				}, Calculations.random(900, 1200));

			}
			break;
		case BANKWITHDRAW:
			if (engine.getBank().isOpen() && !engine.getInventory().contains(WOOL)) {
				if (engine.getBank().contains(WOOL)) {
					currentStage = "Withdrawing from bank.";
					engine.getBank().withdrawAll(WOOL);

					sleepUntil(new Condition() {
						public boolean verify() {
							return engine.getInventory().contains(WOOL);
						}

					}, Calculations.random(900, 1200));
				} else {
					running = false;
					if (!taskScript) {
						currentStage = "Logging out.";
						engine.getBank().close();
						sleep(500, 1000);
						engine.getTabs().logout();
						stop();
					}
				}
			} else {
				currentStage = "Opening bank.";
				engine.getBank().open();
				sleepUntil(new Condition() {
					public boolean verify() {
						return engine.getBank().isOpen();
					}
				}, Calculations.random(900, 1200));

			}
			break;
		case WALK_TO_SPINNER:
			if (engine.getLocalPlayer().isMoving() && engine.getWalking().getDestinationDistance() > 2)
				break;
			if (engine.getLocalPlayer().getTile().getZ() == 2) {
				if (TOP_STAIRS.contains(engine.getLocalPlayer().getTile())) {
					currentStage = "Climbing down stairs.";
					engine.getGameObjects().closest("Staircase").interact("Climb-down");
				} else {
					currentStage = "Walking to stairs.";
					engine.getWalking().walk(new Tile(3205, 3209, 2));//Top Stairs
				}
			} else if (engine.getLocalPlayer().getTile().getZ() == 1) {
				if (!engine.getMap().canReach(SPINNER_TILE)) {
					if (Calculations.random(1, 2) == 1) {
						currentStage = "Opening door.";
						engine.getWalking().walk(new Tile(3207, 3214, 1));
						sleep(300, 500);
					}
					engine.getGameObjects().getTopObjectOnTile(new Tile(3207, 3214, 1)).interact("Open");
					sleep(500, 1000);
				} else {
					engine.getCamera().rotateToYaw(383 + Calculations.random(0, 5));
					GameObject spinner = engine.getGameObjects().closest("Spinning wheel");
					currentStage = "Walking to spinner.";
					engine.getWalking().walk(new Tile(spinner.getX() + Calculations.random(0, 1), spinner.getY() + Calculations.random(0, 2), spinner.getZ()));
					sleepUntil(() -> engine.getLocalPlayer().isMoving(), Calculations.random(1000, 1500));
					sleepWhile(() -> engine.getLocalPlayer().isMoving(), Calculations.random(4000, 6000));
					if (!engine.getLocalPlayer().isMoving()) {
						currentStage = "Opening spinning options.";
						if (Calculations.random(0, 1) == 1)
							spinner.interactForceRight("Spin");
						else
							spinner.interact();
					}
					sleepUntil(new Condition() {

						public boolean verify() {

							return engine.getLocalPlayer().distance(SPINNER_TILE) < 4;
						}
					}, Calculations.random(3000, 6000));
				}
			}
			break;
		case SPIN_WOOL:
			child1 = engine.getWidgets().getWidgetChild(270, 14);
			child2 = engine.getWidgets().getWidgetChild(162, 34);
			if (!isAnimating() && child1 == null && !engine.getLocalPlayer().isMoving()) {
				currentStage = "Opening spinning options.";
				GameObject spinner = engine.getGameObjects().closest("Spinning wheel");
				//if (Calculations.random(1, 3) <= 1)
					//getCamera().rotateToEntity(spinner);
				spinner.interactForceRight("Spin");
				sleep(1000, 2000);
				child1 = engine.getWidgets().getWidgetChild(270, 14);
				sleepWhile(() -> child1 == null || !child1.isVisible(), Calculations.random(1000, 2000));
			}
			if (child1 != null && child1.isVisible()) {
				currentStage = "Make All.";
				WidgetChild child3 = engine.getWidgets().getWidgetChild(270, 12);
				if (child3.getActions() == null || child3.getActions().length == 0) {
					child1.interact();
					currentStage = "Spinning wool.";
					animDelay.reset();
					sleep(300, 950);
					antiBan(true);
					//sleepWhile(() -> !child3.isVisible(), Calculations.random(4000, 6000));
				} else {
					child3.interact();
				}
			}
			/*if (child2.isVisible()) {
				currentStage = "Selecting spin amount.";
				String number = Integer.toString(Calculations.random(31, 99));
				if (Calculations.random(1, 8) == 1)
					number = Integer.toString(Calculations.random(100, 999));
				else if (Calculations.random(1, 8) > 2)
					number = Integer.toString(Calculations.random(3, 9) * 11);
				sleep(Calculations.random(500, 1200));
				getKeyboard().type(number, true);
				animDelay.reset();
				currentStage = "Spinning wool.";
				sleep(300, 950);
				antiBan(true);
			}*/

			break;
		case WALK_TO_BANK:
			if (engine.getLocalPlayer().isMoving() && engine.getWalking().getDestinationDistance() > 2)
				break;
			if (engine.getLocalPlayer().getTile().getZ() == 2) {
				currentStage = "Walking to bank.";
				engine.getWalking().walk(BANK_TILE);
				if (Calculations.random(0, 5) != 1)
					engine.getCamera().rotateToYaw(383 + Calculations.random(0, 5));
				sleepUntil(new Condition() {

					public boolean verify() {

						return engine.getLocalPlayer().distance(BANK_TILE) < 4;
					}
				}, Calculations.random(3000, 6000));
			} else if (engine.getLocalPlayer().getTile().getZ() <= 1) {
				if (!engine.getMap().canReach(new Tile(3205, 3209, 1))) {
					if (Calculations.random(1, 2) == 1) {
						currentStage = "Walking to door.";
						engine.getWalking().walk(new Tile(3207, 3214, 1));
						sleep(500, 1000);
					}
					currentStage = "Opening door.";
					engine.getGameObjects().getTopObjectOnTile(new Tile(3207, 3214, 1)).interact("Open");//door
					sleep(500, 1000);
				} else {
					GameObject stairs = engine.getGameObjects().closest("Staircase");
					if (engine.getLocalPlayer().distance(stairs) > 3)
					{
						currentStage = "Walking to stairs.";
						engine.getWalking().walk(new Tile(3205 + Calculations.random(0, 1), 3209 + Calculations.random(0, 1), 1));
						sleep(1000, 2000);
					}
					currentStage = "Climbing stairs.";
					stairs.interact("Climb-up");
					sleepUntil(new Condition() {

						public boolean verify() {

							return engine.getLocalPlayer().getTile().getZ() == 2;
						}
					}, Calculations.random(600, 1000));
				}
			}

			break;
		case ANTIBAN:
			antiBan(true);
			break;
		}
		return Calculations.random(200, 400);
	}
	
	public void antiBan(boolean isSpinning) {
		currentStage = "Anti-ban";
		int Anti1 = Calculations.random(10);
		switch (Anti1) {
		case 1:
			engine.getTabs().open(Tab.SKILLS);
			sleep(Calculations.random(1240, 2500));
			engine.getTabs().open(Tab.INVENTORY);
			break;
		case 2:
			//getCamera().rotateTo(getCamera().getYaw() + Calculations.random(-1000, 1000), getCamera().getPitch() + Calculations.random(-300, 300));
			sleep(Calculations.random(1240, 2500));
			break;
		case 3:
			engine.getCamera().rotateToYaw(383 + Calculations.random(0, 5));
			sleep(Calculations.random(1240, 2500));
			break;
		case 4:
		case 5:
			break;
		case 6:
		case 7:
			if (engine.getMap().canReach(engine.getGameObjects().closest("Staircase")))
				engine.getMouse().move(engine.getGameObjects().closest("Staircase"));
			else
				engine.getMouse().move(new Tile(3207, 3214, 1));
			break;
		default:
			engine.getMouse().moveMouseOutsideScreen();
			sleep(Calculations.random(1240, 8500));
			break;
		}
	}
	
	public boolean isAnimating() {
		return (engine.getLocalPlayer().isAnimating() || animDelay.elapsed() < 3000);
	}

	public void onPaint(Graphics g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", 1, 11));
		g.drawString("Time Running: " + totalTime.formatTime(), 25, 50);
		g.drawString("Stage: " + currentStage, 25, 60);
	}
}
