package TaskManager.scripts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.Category;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.utilities.impl.Condition;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import TaskManager.Script;
import TaskManager.ScriptDetails;

@ScriptDetails(author = "NumberZ", category = Category.CRAFTING, name = "Wool Spinner", version = 1.0, description = "Spins wool at Lumbridge Castle Does not gather wool.")
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

	public WoolSpinner() {
		supportedConditions.add(TaskManager.Condition.Time);
		supportedConditions.add(TaskManager.Condition.Level);
		supportedSkills.add(Skill.CRAFTING);
	}
	
	@Override
	public void onStart() {
		if (!taskScript)
			init();
		super.onStart();
	}
	
	private enum State {
		BANKDEPOSIT, BANKWITHDRAW, WALK_TO_BANK, WALK_TO_SPINNER, SPIN_WOOL, ANTIBAN;
	}
	
	private State getState() {
		if (BANK_AREA.contains(getLocalPlayer().getTile()) && !Inventory.contains(WOOL)
				&& !Inventory.contains(BALL_OF_WOOL) && Inventory.emptySlotCount() < 28
				|| BANK_AREA.contains(getLocalPlayer().getTile()) && !Inventory.contains(WOOL)
						&& Inventory.contains(BALL_OF_WOOL)) {
			return State.BANKDEPOSIT;
		}

		if (BANK_AREA.contains(getLocalPlayer()) && Inventory.emptySlotCount() == 28) {
			return State.BANKWITHDRAW;
		}

		if (Inventory.count(WOOL) > 0 && !SPINNER_AREA.contains(getLocalPlayer())) {
			return State.WALK_TO_SPINNER;
		}
		if (SPINNER_AREA.contains(getLocalPlayer()) && Inventory.contains(WOOL)) {
			levelUp = Widgets.getWidgetChild(233, 3);
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
		if (getLocalPlayer().isAnimating())
			animDelay.reset();
		state = getState();
		switch (state) {
		case BANKDEPOSIT:
			if (Bank.isOpen()) {
				currentStage = "Depositing into bank.";
				Bank.depositAllItems();
				sleepUntil(new Condition() {
					public boolean verify() {
						return Inventory.onlyContains(i -> Inventory.isEmpty());
					}

				}, Calculations.random(900, 1200));
				increaseRunCount();
			} else {
				currentStage = "Opening bank.";
				Bank.open();
				sleepUntil(new Condition() {
					public boolean verify() {
						return Bank.isOpen();
					}
				}, Calculations.random(900, 1200));

			}
			break;
		case BANKWITHDRAW:
			if (Bank.isOpen() && !Inventory.contains(WOOL)) {
				if (Bank.contains(WOOL)) {
					currentStage = "Withdrawing from bank.";
					Bank.withdrawAll(WOOL);

					sleepUntil(new Condition() {
						public boolean verify() {
							return Inventory.contains(WOOL);
						}

					}, Calculations.random(900, 1200));
				} else {
					running = false;
					if (!taskScript) {
						currentStage = "Logging out.";
						Bank.close();
						sleep(500, 1000);
						Tabs.logout();
						stop();
					}
				}
			} else {
				currentStage = "Opening bank.";
				Bank.open();
				sleepUntil(new Condition() {
					public boolean verify() {
						return Bank.isOpen();
					}
				}, Calculations.random(900, 1200));

			}
			break;
		case WALK_TO_SPINNER:
			if (getLocalPlayer().isMoving() && Walking.getDestinationDistance() > 2)
				break;
			if (getLocalPlayer().getTile().getZ() == 2) {
				if (TOP_STAIRS.contains(getLocalPlayer().getTile())) {
					currentStage = "Climbing down stairs.";
					GameObjects.closest("Staircase").interact("Climb-down");
				} else {
					currentStage = "Walking to stairs.";
					Walking.walk(new Tile(3205, 3209, 2));//Top Stairs
				}
			} else if (getLocalPlayer().getTile().getZ() == 1) {
				if (!Map.canReach(SPINNER_TILE)) {
					if (Calculations.random(1, 2) == 1) {
						currentStage = "Opening door.";
						Walking.walk(new Tile(3207, 3214, 1));
						sleep(300, 500);
					}
					GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3207, 3214, 1)).interact("Open");
					sleep(500, 1000);
				} else {
					Camera.rotateToYaw(383 + Calculations.random(0, 5));
					GameObject spinner = GameObjects.closest("Spinning wheel");
					currentStage = "Walking to spinner.";
					Walking.walk(new Tile(spinner.getX() + Calculations.random(0, 1), spinner.getY() + Calculations.random(0, 2), spinner.getZ()));
					sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(1000, 1500));
					sleepWhile(() -> getLocalPlayer().isMoving(), Calculations.random(4000, 6000));
					if (!getLocalPlayer().isMoving()) {
						currentStage = "Opening spinning options.";
						if (Calculations.random(0, 1) == 1)
							spinner.interactForceRight("Spin");
						else
							spinner.interact();
					}
					sleepUntil(new Condition() {

						public boolean verify() {

							return getLocalPlayer().distance(SPINNER_TILE) < 4;
						}
					}, Calculations.random(3000, 6000));
				}
			}
			break;
		case SPIN_WOOL:
			child1 = Widgets.getWidgetChild(270, 14);
			child2 = Widgets.getWidgetChild(162, 34);
			if (!isAnimating() && child1 == null && !getLocalPlayer().isMoving()) {
				currentStage = "Opening spinning options.";
				GameObject spinner = GameObjects.closest("Spinning wheel");
				//if (Calculations.random(1, 3) <= 1)
					//Camera.rotateToEntity(spinner);
				spinner.interactForceRight("Spin");
				sleep(1000, 2000);
				child1 = Widgets.getWidgetChild(270, 14);
				sleepWhile(() -> child1 == null || !child1.isVisible(), Calculations.random(1000, 2000));
			}
			if (child1 != null && child1.isVisible()) {
				currentStage = "Make All.";
				WidgetChild child3 = Widgets.getWidgetChild(270, 12);
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
			if (getLocalPlayer().isMoving() && Walking.getDestinationDistance() > 2)
				break;
			if (getLocalPlayer().getTile().getZ() == 2) {
				currentStage = "Walking to bank.";
				Walking.walk(BANK_TILE);
				if (Calculations.random(0, 5) != 1)
					Camera.rotateToYaw(383 + Calculations.random(0, 5));
				sleepUntil(new Condition() {

					public boolean verify() {

						return getLocalPlayer().distance(BANK_TILE) < 4;
					}
				}, Calculations.random(3000, 6000));
			} else if (getLocalPlayer().getTile().getZ() <= 1) {
				if (!Map.canReach(new Tile(3205, 3209, 1))) {
					if (Calculations.random(1, 2) == 1) {
						currentStage = "Walking to door.";
						Walking.walk(new Tile(3207, 3214, 1));
						sleep(500, 1000);
					}
					currentStage = "Opening door.";
					GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3207, 3214, 1)).interact("Open");//door
					sleep(500, 1000);
				} else {
					GameObject stairs = GameObjects.closest("Staircase");
					if (getLocalPlayer().distance(stairs) > 3)
					{
						currentStage = "Walking to stairs.";
						Walking.walk(new Tile(3205 + Calculations.random(0, 1), 3209 + Calculations.random(0, 1), 1));
						sleep(1000, 2000);
					}
					currentStage = "Climbing stairs.";
					stairs.interact("Climb-up");
					sleepUntil(new Condition() {

						public boolean verify() {

							return getLocalPlayer().getTile().getZ() == 2;
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
	
	@Override
	public void onExit() {
		super.onExit();
	}
	
	public void antiBan(boolean isSpinning) {
		currentStage = "Anti-ban";
		int Anti1 = Calculations.random(10);
		switch (Anti1) {
		case 1:
			Tabs.open(Tab.SKILLS);
			sleep(Calculations.random(1240, 2500));
			Tabs.open(Tab.INVENTORY);
			break;
		case 2:
			//Camera.rotateTo(Camera.getYaw() + Calculations.random(-1000, 1000), Camera.getPitch() + Calculations.random(-300, 300));
			sleep(Calculations.random(1240, 2500));
			break;
		case 3:
			Camera.rotateToYaw(383 + Calculations.random(0, 5));
			sleep(Calculations.random(1240, 2500));
			break;
		case 4:
		case 5:
			break;
		case 6:
		case 7:
			if (Map.canReach(GameObjects.closest("Staircase")))
				Mouse.move(GameObjects.closest("Staircase"));
			else
				Mouse.move(new Tile(3207, 3214, 1));
			break;
		default:
			Mouse.moveMouseOutsideScreen();
			sleep(Calculations.random(1240, 8500));
			break;
		}
	}
	
	public boolean isAnimating() {
		return (getLocalPlayer().isAnimating() || animDelay.elapsed() < 3000);
	}

	public void onPaint(Graphics g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", 1, 11));
		g.drawString("Time Running: " + totalTime.formatTime(), 25, 50);
		g.drawString("Stage: " + currentStage, 25, 60);
	}
}
