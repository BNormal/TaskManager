package TaskManager.tasks;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.randoms.RandomSolver;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import JewelrySmelter.Utilities;
import TaskManager.Script;

@ScriptManifest(author = "NumberZ", name = "Tutorial Island (unf)", version = 1.0, description = "Does tutorial island for you.", category = Category.MISC)
public class TutorialIsle extends Script {

	WidgetChild interfaceItem = null;
	WidgetChild NPCName = null;
	private State state = null;
	private Timer totalTime = new Timer();
	private boolean needsInput = false;
	
	private enum State {
		NAMING, CHARACTER_CREATION, FOLLOWING_INSTRUCTION, DIALOGUE, NOTHING
	}
	
	private State getState() {
		interfaceItem = getWidgets().getWidgetChild(558, 11);//Setting name for new player
		if (interfaceItem != null && interfaceItem.isVisible()) {
			return State.NAMING;
		}
		interfaceItem = getWidgets().getWidgetChild(269, 100);
		if (interfaceItem != null && interfaceItem.isVisible()) {
			return State.CHARACTER_CREATION;
		}
		interfaceItem = getWidgets().getWidgetChild(263, 1);
		if (interfaceItem != null && interfaceItem.isVisible() && interfaceItem.getChild(0).isVisible())
			return State.FOLLOWING_INSTRUCTION;
		if (getDialogues().inDialogue()) {
			return State.DIALOGUE;
		}
		return State.NOTHING;
	}
	
	@Override
    public void onStart() {
		running = true;
		getRandomManager().disableSolver(RandomEvent.RESIZABLE_DISABLER);
		if (getRandomManager().getCurrentSolver().getEventString().equalsIgnoreCase("RESIZABLE_DISABLER"))
			getRandomManager().getCurrentSolver().disable();
	}
	
	@Override
	public int onLoop() {
		state = getState();
		switch (state) {
		case NAMING:
			interfaceItem.interact();
			getKeyboard().type("zezima", true);
			
			sleepUntil(() -> getWidgets().getWidgetChild(558, 12) != null && getWidgets().getWidgetChild(558, 12).getText().contains("not available"), Calculations.random(3000, 5000));
			WidgetChild typeNameTextField = getWidgets().getWidgetChild(558, Calculations.random(14, 16));
			typeNameTextField.interact();
			
			sleepUntil(() -> getWidgets().getWidgetChild(558, 12) != null && getWidgets().getWidgetChild(558, 12).getText().contains("<col=00ff00>available"), Calculations.random(3000, 5000));
			WidgetChild setNameButton = getWidgets().getWidgetChild(558, 18);
			setNameButton.interact();
			break;
		case CHARACTER_CREATION:
			interfaceItem.interact();
			sleepUntil(() -> getWidgets().getWidgetChild(269, 100) == null || !getWidgets().getWidgetChild(269, 100).isVisible(), Calculations.random(3000, 5000));
			break;
		case FOLLOWING_INSTRUCTION:
			if (interfaceItem != null && interfaceItem.isVisible()) {
				String text = interfaceItem.getChild(0).getText();
				if (text.contains("Getting started")) {
					needsInput = true;
					//getNpcs().closest("Gielinor Guide").interact();
					//sleepUntil(() -> getDialogues().inDialogue(), Calculations.random(3000, 5000));
				} else if (text.contains("Options menu")) {
					if (!getTabs().isOpen(Tab.OPTIONS)) {
						needsInput = false;
						getTabs().openWithMouse(Tab.OPTIONS);
						getRandomManager().enableSolver(RandomEvent.RESIZABLE_DISABLER);
					} else {
						getNpcs().closest("Gielinor Guide").interact();
						sleepUntil(() -> getDialogues().inDialogue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Moving on")) {
					if (text.contains("you've just cooked")) {
						getWalking().walk(new Tile(3093 + Calculations.random(-3, 0), 3092 + Calculations.random(0, 2), 0));
						getGameObjects().closest("Gate").interact();
						sleepUntil(() -> getLocalPlayer().getX() < 3090, Calculations.random(3000, 5000));
					} else if (text.contains("with the yellow arrow")) {
						getWalking().walk(new Tile(3082 + Calculations.random(-3, 0), 3084 + Calculations.random(0, 2), 0));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						getGameObjects().closest("Door").interact();
						sleepUntil(() -> getLocalPlayer().getX() < 3079, Calculations.random(3000, 5000));
					} else {
						getGameObjects().closest("Door").interact();
						sleepUntil(() -> getLocalPlayer().getX() > 3097, Calculations.random(3000, 5000));
					}
				} else if (text.contains("Moving around")) {
					getWalking().walk(new Tile(3103 + Calculations.random(-3, 0), 3096 + Calculations.random(0, 2), 0));
					sleepUntil(() -> getNpcs().closest("Survival Expert").distance(getLocalPlayer()) < 5, Calculations.random(3000, 5000));
					getNpcs().closest("Survival Expert").interact();
					sleepUntil(() -> getDialogues().inDialogue(), Calculations.random(3000, 5000));
				} else if (text.contains("You've been given an item")) {
					getTabs().openWithMouse(Tab.INVENTORY);
				} else if (text.contains("Fishing")) {
					getNpcs().closest("Fishing spot").interact();
					sleepUntil(() -> getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					sleepUntil(() -> !getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
				} else if (text.contains("gained some experience")) {
					getTabs().openWithMouse(Tab.SKILLS);
				} else if (text.contains("Skills and Experience")) {
					getNpcs().closest("Survival Expert").interact();
					sleepUntil(() -> getDialogues().inDialogue(), Calculations.random(3000, 5000));
				} else if (text.contains("Woodcutting")) {
					GameObject tree = getGameObjects().closest("Tree");
					tree.interact();
					sleepUntil(() -> getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					sleepUntil(() -> !getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
				} else if (text.contains("Firemaking")) {
					getInventory().get("Logs").useOn("Tinderbox");
					sleepUntil(() -> getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					sleepUntil(() -> !getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
				} else if (text.contains("Cooking")) {
					if (text.contains("to the chef")) {
						
					} else {
						getInventory().get("Raw shrimps").useOn(getGameObjects().closest("Fire"));
						sleepUntil(() -> getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					}
				}
			}
			break;
		case DIALOGUE:
			if (getDialogues().getOptions() != null && getDialogues().getOptions().length > 0) {
				getDialogues().chooseOption(1);
			} else {
				getDialogues().clickContinue();
			}
			break;
		case NOTHING:
			break;
		}
		return 0;
	}
	
	@Override
    public void onPaint(Graphics2D g) {
		int x = 25;
		g.setColor(new Color(0.0F, 0.0F, 0.0F, 0.2F));
		g.fillRect(20, 37, 200, 47);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", 1, 11));
		g.drawRect(20, 37, 200, 47);
		Utilities.drawShadowString(g, "Time Running: " + totalTime.formatTime(), x, 50);
		Utilities.drawShadowString(g, "Stage: " + state, x, 60);
		if (needsInput) {
			g.setFont(new Font("Arial", 1, 20));
			Utilities.drawShadowString(g, "NEEDS YOUR INPUT HERE", 400, 200);
		}
	}
	
	@Override
    public void onExit() {
		running = false;
	}

}
