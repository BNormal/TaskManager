package TaskManager.tasks;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Date;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import JewelrySmelter.Utilities;
import TaskManager.Script;

@ScriptManifest(author = "NumberZ", name = "Tutorial Island", version = 1.0, description = "Does tutorial island for you.", category = Category.MISC)
public class TutorialIsle extends Script {

	WidgetChild interfaceItem = null;
	WidgetChild NPCName = null;
	private State state = null;
	
	public TutorialIsle() {
		//supportedConditions.add(Condition.RunOnce);
	}
	
	private enum State {
		NAMING, CHARACTER_CREATION, FOLLOWING_INSTRUCTION, DIALOGUE, FINISHED, NOTHING
	}
	
	private State getState() {
		interfaceItem = engine.getWidgets().getWidgetChild(558, 11);//Setting name for new player
		if (interfaceItem != null && interfaceItem.isVisible()) {
			return State.NAMING;
		}
		interfaceItem = engine.getWidgets().getWidgetChild(269, 100);
		if (interfaceItem != null && interfaceItem.isVisible()) {
			return State.CHARACTER_CREATION;
		}
		interfaceItem = engine.getWidgets().getWidgetChild(263, 1);
		if (interfaceItem != null && interfaceItem.isVisible() && interfaceItem.getChild(0).isVisible())
			return State.FOLLOWING_INSTRUCTION;
		if (engine.getDialogues().inDialogue()) {
			return State.DIALOGUE;
		}
		if (engine.getLocalPlayer().getX() > 3200 && engine.getLocalPlayer().getX() < 3300 && engine.getLocalPlayer().getY() > 3200 && engine.getLocalPlayer().getY() < 3300)
			return State.FINISHED;
		return State.NOTHING;
	}
	
	@Override
    public void onStart() {
		super.onStart();
		if (engine == null)
			engine = this;
		running = true;
		engine.getRandomManager().disableSolver(RandomEvent.RESIZABLE_DISABLER);
		if (engine.getRandomManager().getCurrentSolver() != null && engine.getRandomManager().getCurrentSolver().getEventString().equalsIgnoreCase("RESIZABLE_DISABLER"))
			engine.getRandomManager().getCurrentSolver().disable();
	}
	
	@Override
	public int onLoop() {
		state = getState();
		switch (state) {
		case NAMING:
			interfaceItem.interact();
			engine.getKeyboard().type("zezima", true);
			
			sleepUntil(() -> engine.getWidgets().getWidgetChild(558, 12) != null && engine.getWidgets().getWidgetChild(558, 12).getText().contains("not available"), Calculations.random(3000, 5000));
			WidgetChild typeNameTextField = engine.getWidgets().getWidgetChild(558, Calculations.random(14, 16));
			typeNameTextField.interact();
			
			sleepUntil(() -> engine.getWidgets().getWidgetChild(558, 12) != null && engine.getWidgets().getWidgetChild(558, 12).getText().contains("<col=00ff00>available"), Calculations.random(3000, 5000));
			WidgetChild setNameButton = engine.getWidgets().getWidgetChild(558, 18);
			setNameButton.interact();
			break;
		case CHARACTER_CREATION:
			interfaceItem.interact();
			sleepUntil(() -> engine.getWidgets().getWidgetChild(269, 100) == null || !engine.getWidgets().getWidgetChild(269, 100).isVisible(), Calculations.random(3000, 5000));
			break;
		case FOLLOWING_INSTRUCTION:
			if (interfaceItem != null && interfaceItem.isVisible()) {
				String text = interfaceItem.getChild(0).getText();
				if (text.contains("Getting started")) {
					NPC guide = engine.getNpcs().closest("Gielinor Guide");
					Point p = guide.getModel().calculateCenterPoint();
	                Rectangle GAME_SCREEN = new Rectangle(5, 5, 511, 333);
	                Rectangle CANVAS = new Rectangle(0, 0, 765, 503);
	                int heightAdjust = 0;
	                int widthAdjust = 0;
	                p.setLocation(p.getX() * (CANVAS.getWidth() / GAME_SCREEN.getWidth()) + widthAdjust, p.getY() * (CANVAS.getHeight() / GAME_SCREEN.getHeight()) + heightAdjust);
	                engine.getMouse().hop(p);
	                sleepUntil(() -> engine.getClient().getMenu().contains("Talk-to"), 2000);
	                if (engine.getClient().getMenu().contains("Talk-to")) {
	                	engine.getMouse().click();
	                    sleepUntil(() -> engine.getDialogues().canContinue(), 10000);
	                }
				} else if (text.contains("Options menu")) { // Clicks on setting tab and then should continue to go into fixed mode
					if (!engine.getTabs().isOpen(Tab.OPTIONS)) {
						engine.getTabs().openWithMouse(Tab.OPTIONS);
						engine.getRandomManager().enableSolver(RandomEvent.RESIZABLE_DISABLER);
					} else {
						if (engine.getTabs().isOpen(Tab.OPTIONS)) {
							interfaceItem = engine.getWidgets().getWidgetChild(261, 1).getChild(2);
							if (interfaceItem.getTextureId() != 762) {
								interfaceItem.interact();
								sleepUntil(() -> interfaceItem.getTextureId() == 762, Calculations.random(3000, 5000));
								engine.getWidgets().getWidgetChild(261, 38).interact();
								engine.getWidgets().getWidgetChild(261, 44).interact();
								engine.getWidgets().getWidgetChild(261, 50).interact();
							}
						}
						engine.getNpcs().closest("Gielinor Guide").interact();
						sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Moving on")) {
					if (text.contains("you've just cooked")) {// cooks shrimp on fire
						engine.getWalking().walk(new Tile(3093 + Calculations.random(-3, 0), 3092 + Calculations.random(0, 2), 0));
						engine.getGameObjects().closest("Gate").interactForceRight("Open");
						sleepUntil(() -> engine.getLocalPlayer().getX() < 3090, Calculations.random(3000, 5000));
					} else if (text.contains("with the yellow arrow")) {// clicks on door to chef
						engine.getWalking().walk(new Tile(3082 + Calculations.random(-3, 0), 3084 + Calculations.random(0, 2), 0));
						sleepUntil(() -> engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						engine.getGameObjects().closest("Door").interact();
						sleepUntil(() -> engine.getLocalPlayer().getX() < 3079, Calculations.random(3000, 5000));
					} else if (text.contains("Well done! You've baked")) {
						engine.getWalking().walk(new Tile(3074 + Calculations.random(-1, 0), 3089 + Calculations.random(0, 1), 0));
						sleepUntil(() -> engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						engine.getGameObjects().closest("Door").interact();
						sleepUntil(() -> engine.getLocalPlayer().getX() < 3072, Calculations.random(3000, 5000));
					} else if (text.contains("When you get there, click")) {
						engine.getWalking().walk(new Tile(3086 + Calculations.random(-1, 0), 3127 + Calculations.random(0, 1), 0));
						sleepUntil(() -> engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						engine.getGameObjects().closest("Door").interact();
						sleepUntil(() -> engine.getLocalPlayer().getY() < 3125, Calculations.random(3000, 5000));
					} else if (text.contains("It's time to enter")) {
						engine.getGameObjects().closest("Ladder").interactForceRight("Climb-down");
						sleepUntil(() -> engine.getLocalPlayer().getX() > 3094, Calculations.random(3000, 5000));
					} else if (text.contains("made your first weapon")) {
						engine.getWalking().walk(new Tile(3094 + Calculations.random(-1, 0), 9501 + Calculations.random(0, 1), 0));
						sleepUntil(() -> engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						engine.getGameObjects().closest("Gate").interact();
						sleepUntil(() -> engine.getLocalPlayer().getY() > 9503, Calculations.random(3000, 5000));
					} else if (text.contains("just talk to the combat instructor")) {
						engine.getWalking().walk(new Tile(3111 + Calculations.random(-1, 0), 9525 + Calculations.random(0, 1), 0));
						sleepUntil(() -> engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						engine.getGameObjects().closest("Ladder").interact();
						sleepUntil(() -> engine.getLocalPlayer().getY() < 3130, Calculations.random(3000, 5000));
					} else if (text.contains("Polls are run")) {
						interfaceItem = engine.getWidgets().getWidgetChild(310, 11);
						if (interfaceItem != null && interfaceItem.isVisible()) {
							interfaceItem.interact();
						} else {
							engine.getWalking().walk(new Tile(3124, 3124, 0));
							sleepUntil(() -> engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
							sleepUntil(() -> !engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
							engine.getGameObjects().closest("Door").interact();
							sleepUntil(() -> engine.getLocalPlayer().getX() > 3124, Calculations.random(3000, 5000));
						}
					} else if (text.contains("Continue through the next door.")) {
						engine.getWalking().walk(new Tile(3129, 3124, 0));
						sleepUntil(() -> engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						engine.getGameObjects().closest("Door").interact();
						sleepUntil(() -> engine.getLocalPlayer().getX() > 3129, Calculations.random(3000, 5000));
					} else {
						engine.getGameObjects().closest("Door").interact();
						sleepUntil(() -> engine.getLocalPlayer().getX() > 3097, Calculations.random(3000, 5000));
					}
				} else if (text.contains("Moving around")) {
					engine.getWalking().walk(new Tile(3103 + Calculations.random(-3, 0), 3096 + Calculations.random(0, 2), 0));
					sleepUntil(() -> engine.getNpcs().closest("Survival Expert").distance(engine.getLocalPlayer()) < 5, Calculations.random(3000, 5000));
					engine.getNpcs().closest("Survival Expert").interact();
					sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("You've been given an item")) {
					engine.getTabs().openWithMouse(Tab.INVENTORY);
				} else if (text.contains("Fishing")) {
					engine.getNpcs().closest("Fishing spot").interactForceRight("Net");
					sleepUntil(() -> engine.getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					sleepUntil(() -> !engine.getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
				} else if (text.contains("gained some experience")) {
					engine.getTabs().openWithMouse(Tab.SKILLS);
				} else if (text.contains("Skills and Experience")) {
					engine.getNpcs().closest("Survival Expert").interact();
					sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Woodcutting")) {
					GameObject tree = engine.getGameObjects().closest("Tree");
					tree.interact();
					sleepUntil(() -> engine.getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					sleepUntil(() -> !engine.getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
				} else if (text.contains("Firemaking")) {
					engine.getInventory().get("Logs").useOn("Tinderbox");
					sleepUntil(() -> engine.getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					sleepUntil(() -> !engine.getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
				} else if (text.contains("Cooking") && !text.contains("dough")) {
					if (text.contains("to the chef")) {
						engine.getNpcs().closest("Master Chef").interact();
						sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
					} else {
						engine.getInventory().get("Raw shrimps").useOn(engine.getGameObjects().closest("Fire"));
						sleepUntil(() -> engine.getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
						sleepUntil(() -> !engine.getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Making dough")) {
					engine.getInventory().get("Pot of flour").useOn("Bucket of water");
					sleepUntil(() -> engine.getInventory().contains("Bread dough"), Calculations.random(3000, 5000));
				} else if (text.contains("Cooking dough")) {
					engine.getInventory().get("Bread dough").useOn(engine.getGameObjects().closest("Range"));
					sleepUntil(() -> engine.getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					sleepUntil(() -> !engine.getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
				} else if (text.contains("Fancy a run?")) {
					if (engine.getWalking().isRunEnabled()) {
						engine.getWalking().walk(new Tile(3086 + Calculations.random(-1, 0), 3127 + Calculations.random(0, 2), 0));
						engine.getWalking().toggleRun();
						sleepUntil(() -> !engine.getWalking().isRunEnabled(), Calculations.random(3000, 5000));
						engine.getWalking().toggleRun();
						sleepUntil(() -> engine.getWalking().isRunEnabled(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Quests")) {
					engine.getNpcs().closest("Quest Guide").interact();
					sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Quest journal")) {
					engine.getTabs().openWithMouse(Tab.QUEST);
					if (engine.getTabs().isOpen(Tab.QUEST)) {
						engine.getNpcs().closest("Quest Guide").interact();
						sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Mining and Smithing")) {
					if (engine.getLocalPlayer().getY() < 9510) {
						engine.getNpcs().closest("Mining Instructor").interact();
						sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
					} else {
						engine.getWalking().walk(new Tile(3081 + Calculations.random(-3, 0), 9505 + Calculations.random(0, 2), 0));
						sleepUntil(() -> engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("you just need some copper")) {
					engine.getGameObjects().closest(10079).interact();// copper rock
					sleepUntil(() -> engine.getInventory().contains("Copper ore"), Calculations.random(5000, 8000));
				} else if (text.contains("It's quite simple really. To mine a rock")) {
					engine.getGameObjects().closest(10080).interact();// tin rock
					sleepUntil(() -> engine.getInventory().contains("Tin ore"), Calculations.random(5000, 8000));
				} else if (text.contains("Smelting")) {
					if (text.contains("You've made a bronze")) {
						engine.getNpcs().closest("Mining Instructor").interact();
						sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
					} else {
						if (Calculations.random(0, 5) > 3)
							engine.getInventory().get("Tin ore").useOn(engine.getGameObjects().closest("Furnace"));
						else
							engine.getInventory().get("Copper ore").useOn(engine.getGameObjects().closest("Furnace"));
						sleepUntil(() -> engine.getInventory().contains("Bronze bar"), Calculations.random(5000, 8000));
					}
				} else if (text.contains("Smithing a dagger")) {
					interfaceItem = engine.getWidgets().getWidgetChild(312, 9);
					if (interfaceItem != null && interfaceItem.isVisible()) {
						interfaceItem.interact();
						sleepUntil(() -> engine.getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
						sleepUntil(() -> !engine.getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					} else {
						engine.getInventory().get("Bronze bar").useOn(engine.getGameObjects().closest("Anvil"));
						sleepUntil(() -> engine.getInventory().contains("Bronze bar"), Calculations.random(5000, 8000));
					}
				} else if (text.contains("In this area you will find")) {//Combat
					if (engine.getLocalPlayer().getX() < 3100) {
						engine.getWalking().walk(new Tile(3106 + Calculations.random(-1, 0), 9509 + Calculations.random(0, 1), 0));
						sleepUntil(() -> engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
					} else {
						engine.getNpcs().closest("Combat Instructor").interact();
						sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Equipping items")) {
					engine.getTabs().openWithMouse(Tab.EQUIPMENT);
				} else if (text.contains("Worn inventory")) {
					interfaceItem = engine.getWidgets().getWidgetChild(387, 1);
					if (interfaceItem != null && interfaceItem.isVisible()) {
						interfaceItem.interact();
					}
				} else if (text.contains("Equipment stats")) {
					if (engine.getInventory().contains("Bronze dagger")) {
						engine.getInventory().get("Bronze dagger").interact();
						sleepUntil(() -> engine.getEquipment().contains("Bronze dagger"), Calculations.random(3000, 5000));
					}
					interfaceItem = engine.getWidgets().getWidgetChild(84, 4);
					if (interfaceItem != null && interfaceItem.isVisible()) {
						interfaceItem.interact();
					}
					engine.getNpcs().closest("Combat Instructor").interact();
					sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Unequipping items")) {
					if (engine.getInventory().contains("Bronze sword")) {
						engine.getInventory().get("Bronze sword").interact();
						sleepUntil(() -> engine.getEquipment().contains("Bronze sword"), Calculations.random(3000, 5000));
					}
					if (engine.getInventory().contains("Wooden shield")) {
						engine.getInventory().get("Wooden shield").interact();
						sleepUntil(() -> engine.getEquipment().contains("Wooden shield"), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Combat interface")) {
					if (!engine.getTabs().isOpen(Tab.COMBAT)) {
						engine.getTabs().openWithMouse(Tab.COMBAT);
					} else {
						engine.getWalking().walk(new Tile(3113 + Calculations.random(-1, 0), 9518 + Calculations.random(0, 1), 0));
						sleepUntil(() -> engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						engine.getGameObjects().closest("Gate").interact();
						sleepUntil(() -> engine.getLocalPlayer().getX() < 3111, Calculations.random(3000, 5000));
					}
				} else if (text.contains("to slay some rats!")) {
					engine.getNpcs().closest(getFilteredNPCs("Giant rat")).interact("Attack");
					sleepUntil(() -> engine.getLocalPlayer().isInCombat(), Calculations.random(3000, 5000));
					sleepUntil(() -> !engine.getLocalPlayer().isInCombat(), Calculations.random(25000, 35000));
				} else if (text.contains("made your first kill!")) {
					if (engine.getLocalPlayer().getX() < 3111 && engine.getLocalPlayer().getY() > 9512) {
						engine.getGameObjects().closest("Gate").interact();
						sleepUntil(() -> engine.getLocalPlayer().getX() > 3110, Calculations.random(3000, 5000));
					}
					if (engine.getLocalPlayer().getY() > 9513) {
						engine.getWalking().walk(new Tile(3108 + Calculations.random(-1, 0), 9511 + Calculations.random(0, 1), 0));
						sleepUntil(() -> engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
					}
					engine.getNpcs().closest("Combat Instructor").interact();
					sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(8000, 10000));
				} else if (text.contains("Rat ranging")) {
					if (!engine.getTabs().isOpen(Tab.INVENTORY)) {
						if (Calculations.random(0, 5) > 3)
							engine.getTabs().openWithFKey(Tab.INVENTORY);
						else
							engine.getTabs().openWithMouse(Tab.INVENTORY);
						sleepUntil(() -> engine.getTabs().isOpen(Tab.INVENTORY), Calculations.random(3000, 5000));
					}
					if (engine.getInventory().contains("Shortbow")) {
						engine.getInventory().get("Shortbow").interact();
						sleepUntil(() -> engine.getEquipment().contains("Shortbow"), Calculations.random(3000, 5000));
					} else if (engine.getInventory().contains("Bronze arrow")) {
						engine.getInventory().get("Bronze arrow").interact();
						sleepUntil(() -> engine.getEquipment().contains("Bronze arrow"), Calculations.random(3000, 5000));
					} else {
						if (engine.getLocalPlayer().getX() < 3106) {
							engine.getWalking().walk(new Tile(3108 + Calculations.random(-1, 0), 9511 + Calculations.random(0, 1), 0));
							sleepUntil(() -> engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
							sleepUntil(() -> !engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						}
						engine.getNpcs().closest(getFilteredNPCs("Giant rat")).interact("Attack");
						sleepUntil(() -> engine.getLocalPlayer().isInCombat(), Calculations.random(3000, 5000));
						sleepUntil(() -> !engine.getLocalPlayer().isInCombat(), Calculations.random(25000, 35000));
					}
				} else if (text.contains("Banking")) {
					if (engine.getWidgets().getWidgetChild(12, 2) != null && engine.getWidgets().getWidgetChild(12, 2).isVisible()) {
						interfaceItem = engine.getWidgets().getWidgetChild(12, 2).getChild(11);
						if (interfaceItem != null && interfaceItem.isVisible()) {
							interfaceItem.interact();
							engine.getGameObjects().closest("Poll booth").interact();
							sleep(4000);
						}
					} else {
						engine.getWalking().walk(new Tile(3122 + Calculations.random(-1, 0), 3123 + Calculations.random(0, 1), 0));
						sleepUntil(() -> engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						engine.getGameObjects().closest("Bank booth").interact();
						sleep(1000);
					}
				} else if (text.contains("Account Management") ) {
					if (text.contains("Click on the flashing")) {
						engine.getTabs().openWithMouse(Tab.ACCOUNT_MANAGEMENT);
						sleep(1000);
					} else {
						engine.getNpcs().closest("Account Guide").interact();
						sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Prayer") && !text.contains("menu")) {
					if (engine.getLocalPlayer().getX() > 3124 && engine.getLocalPlayer().getY() > 3110) {
						engine.getWalking().walk(new Tile(3128 + Calculations.random(-1, 0), 3107 + Calculations.random(-1, 0), 0));
						sleepUntil(() -> engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
					} else {
						engine.getNpcs().closest("Brother Brace").interact();
						sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Prayer menu")) {
					engine.getTabs().openWithMouse(Tab.PRAYER);
					sleep(1000);
					engine.getNpcs().closest("Brother Brace").interact();
					sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Friends and Ignore")) {
					engine.getTabs().openWithMouse(Tab.FRIENDS);
					sleep(1000);
					engine.getNpcs().closest("Brother Brace").interact();
					sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Your final instructor")) {
					if (engine.getLocalPlayer().getY() > 3102) {
						engine.getWalking().walk(new Tile(3122, 3103, 0));
						sleepUntil(() -> engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						engine.getGameObjects().closest("Door").interact();
						sleepUntil(() -> engine.getLocalPlayer().getY() < 3103, Calculations.random(3000, 5000));
					} else {
						if (engine.getLocalPlayer().getX() < 3136) {
							engine.getWalking().walk(new Tile(3141 + Calculations.random(-1, 0), 3087 + Calculations.random(-1, 0), 0));
							sleepUntil(() -> engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
							sleepUntil(() -> !engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						}
						engine.getNpcs().closest("Magic Instructor").interact();
						sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("your final menu")) {
					engine.getTabs().openWithMouse(Tab.MAGIC);
					sleep(1000);
					
				} else if (text.contains("your magic interface. All of your")) {
					engine.getNpcs().closest("Magic Instructor").interact();
					sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Magic casting")) {
					engine.getWalking().walk(new Tile(3140 + Calculations.random(-1, 0), 3091, 0));
					sleepUntil(() -> engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
					sleepUntil(() -> !engine.getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
					interfaceItem = engine.getWidgets().getWidgetChild(218, 6);
					if (interfaceItem != null && interfaceItem.isVisible()) {
						interfaceItem.interact();
						engine.getNpcs().closest(getFilteredNPCs("Chicken")).interact();
						sleepUntil(() -> engine.getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
						sleepUntil(() -> !engine.getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("To the mainland")) {
					engine.getNpcs().closest("Magic Instructor").interact();
					sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
				}
			}
			break;
		case DIALOGUE:
			if (engine.getDialogues().getOptions() != null && engine.getDialogues().getOptions().length > 0) {
				if (engine.getDialogues().getOptions().length > 2 && engine.getDialogues().getOptions()[2].contains("No, I'm not"))
					engine.getDialogues().chooseOption(3);
				else
					engine.getDialogues().chooseOption(1);
			} else {
				engine.getDialogues().spaceToContinue();
			}
			break;
		case FINISHED:
			running = false;
			time = new Date(totalTime.elapsed());
			if (!taskScript) {
				engine.getTabs().logout();
				this.stop();
			}
			break;
		case NOTHING:
			break;
		}
		return 0;
	}
	
	private Filter<NPC> getFilteredNPCs(String name) {
        return npc -> {
            boolean accepted = false;
            if(newTargetIsAttackable(npc) && npc.getName().toLowerCase().contains(name.toLowerCase())){
            	//log("found a chicken");
                accepted = true;
            }
            return accepted;
        };
    }
	
	public boolean newTargetIsAttackable(NPC npc) {
		return (npc != null && !npc.isInCombat() && !npc.isInteractedWith() && npc.exists() && npc.getHealthPercent() > 0);
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
	}
	
	@Override
    public void onExit() {
		running = false;
	}

}
