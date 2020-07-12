package TaskManager.tasks;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.randoms.RandomEvent;
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
	
	private enum State {
		NAMING, CHARACTER_CREATION, FOLLOWING_INSTRUCTION, DIALOGUE, FINISHED, NOTHING
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
		if (getLocalPlayer().getX() > 3200 && getLocalPlayer().getX() < 3300 && getLocalPlayer().getY() > 3200 && getLocalPlayer().getY() < 3300)
			return State.FINISHED;
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
					NPC guide = getNpcs().closest("Gielinor Guide");
					Point p = guide.getModel().calculateCenterPoint();
	                Rectangle GAME_SCREEN = new Rectangle(5, 5, 511, 333);
	                Rectangle CANVAS = new Rectangle(0, 0, 765, 503);
	                int heightAdjust = 0;
	                int widthAdjust = 0;
	                p.setLocation(p.getX() * (CANVAS.getWidth() / GAME_SCREEN.getWidth())+widthAdjust, p.getY() * (CANVAS.getHeight() / GAME_SCREEN.getHeight())+heightAdjust);
	                getMouse().hop(p);
	                sleepUntil(() -> getClient().getMenu().contains("Talk-to"), 2000);
	                if (getClient().getMenu().contains("Talk-to")) {
	                    getMouse().click();
	                    sleepUntil(() -> getDialogues().canContinue(), 10000);
	                }
				} else if (text.contains("Options menu")) { // Clicks on setting tab and then should continue to go into fixed mode
					if (!getTabs().isOpen(Tab.OPTIONS)) {
						getTabs().openWithMouse(Tab.OPTIONS);
						getRandomManager().enableSolver(RandomEvent.RESIZABLE_DISABLER);
					} else {
						if (getTabs().isOpen(Tab.OPTIONS)) {
							interfaceItem = getWidgets().getWidgetChild(261, 1).getChild(2);
							if (interfaceItem.getTextureId() != 762) {
								interfaceItem.interact();
								sleepUntil(() -> interfaceItem.getTextureId() == 762, Calculations.random(3000, 5000));
								getWidgets().getWidgetChild(261, 38).interact();
								getWidgets().getWidgetChild(261, 44).interact();
								getWidgets().getWidgetChild(261, 50).interact();
							}
						}
						getNpcs().closest("Gielinor Guide").interact();
						sleepUntil(() -> getDialogues().canContinue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Moving on")) {
					if (text.contains("you've just cooked")) {// cooks shrimp on fire
						getWalking().walk(new Tile(3093 + Calculations.random(-3, 0), 3092 + Calculations.random(0, 2), 0));
						getGameObjects().closest("Gate").interactForceRight("Open");
						sleepUntil(() -> getLocalPlayer().getX() < 3090, Calculations.random(3000, 5000));
					} else if (text.contains("with the yellow arrow")) {// clicks on door to chef
						getWalking().walk(new Tile(3082 + Calculations.random(-3, 0), 3084 + Calculations.random(0, 2), 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						getGameObjects().closest("Door").interact();
						sleepUntil(() -> getLocalPlayer().getX() < 3079, Calculations.random(3000, 5000));
					} else if (text.contains("Well done! You've baked")) {
						getWalking().walk(new Tile(3074 + Calculations.random(-1, 0), 3089 + Calculations.random(0, 1), 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						getGameObjects().closest("Door").interact();
						sleepUntil(() -> getLocalPlayer().getX() < 3072, Calculations.random(3000, 5000));
					} else if (text.contains("When you get there, click")) {
						getWalking().walk(new Tile(3086 + Calculations.random(-1, 0), 3127 + Calculations.random(0, 1), 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						getGameObjects().closest("Door").interact();
						sleepUntil(() -> getLocalPlayer().getY() < 3125, Calculations.random(3000, 5000));
					} else if (text.contains("It's time to enter")) {
						getGameObjects().closest("Ladder").interact();
						sleepUntil(() -> getLocalPlayer().getX() > 3094, Calculations.random(3000, 5000));
					} else if (text.contains("made your first weapon")) {
						getWalking().walk(new Tile(3094 + Calculations.random(-1, 0), 9501 + Calculations.random(0, 1), 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						getGameObjects().closest("Gate").interact();
						sleepUntil(() -> getLocalPlayer().getY() > 9503, Calculations.random(3000, 5000));
					} else if (text.contains("just talk to the combat instructor")) {
						getWalking().walk(new Tile(3111 + Calculations.random(-1, 0), 9525 + Calculations.random(0, 1), 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						getGameObjects().closest("Ladder").interact();
						sleepUntil(() -> getLocalPlayer().getY() < 3130, Calculations.random(3000, 5000));
					} else if (text.contains("Polls are run")) {
						interfaceItem = getWidgets().getWidgetChild(310, 11);
						if (interfaceItem != null && interfaceItem.isVisible()) {
							interfaceItem.interact();
						} else {
							getWalking().walk(new Tile(3124, 3124, 0));
							sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
							sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
							getGameObjects().closest("Door").interact();
							sleepUntil(() -> getLocalPlayer().getX() > 3124, Calculations.random(3000, 5000));
						}
					} else if (text.contains("Continue through the next door.")) {
						getWalking().walk(new Tile(3129, 3124, 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						getGameObjects().closest("Door").interact();
						sleepUntil(() -> getLocalPlayer().getX() > 3129, Calculations.random(3000, 5000));
					} else {
						getGameObjects().closest("Door").interact();
						sleepUntil(() -> getLocalPlayer().getX() > 3097, Calculations.random(3000, 5000));
					}
				} else if (text.contains("Moving around")) {
					getWalking().walk(new Tile(3103 + Calculations.random(-3, 0), 3096 + Calculations.random(0, 2), 0));
					sleepUntil(() -> getNpcs().closest("Survival Expert").distance(getLocalPlayer()) < 5, Calculations.random(3000, 5000));
					getNpcs().closest("Survival Expert").interact();
					sleepUntil(() -> getDialogues().canContinue(), Calculations.random(3000, 5000));
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
					sleepUntil(() -> getDialogues().canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Woodcutting")) {
					GameObject tree = getGameObjects().closest("Tree");
					tree.interact();
					sleepUntil(() -> getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					sleepUntil(() -> !getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
				} else if (text.contains("Firemaking")) {
					getInventory().get("Logs").useOn("Tinderbox");
					sleepUntil(() -> getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					sleepUntil(() -> !getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
				} else if (text.contains("Cooking") && !text.contains("dough")) {
					if (text.contains("to the chef")) {
						getNpcs().closest("Master Chef").interact();
						sleepUntil(() -> getDialogues().canContinue(), Calculations.random(3000, 5000));
					} else {
						getInventory().get("Raw shrimps").useOn(getGameObjects().closest("Fire"));
						sleepUntil(() -> getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Making dough")) {
					getInventory().get("Pot of flour").useOn("Bucket of water");
					sleepUntil(() -> getInventory().contains("Bread dough"), Calculations.random(3000, 5000));
				} else if (text.contains("Cooking dough")) {
					getInventory().get("Bread dough").useOn(getGameObjects().closest("Range"));
					sleepUntil(() -> getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					sleepUntil(() -> !getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
				} else if (text.contains("Fancy a run?")) {
					if (getWalking().isRunEnabled()) {
						getWalking().walk(new Tile(3086 + Calculations.random(-1, 0), 3127 + Calculations.random(0, 2), 0));
						getWalking().toggleRun();
						sleepUntil(() -> !getWalking().isRunEnabled(), Calculations.random(3000, 5000));
						getWalking().toggleRun();
						sleepUntil(() -> getWalking().isRunEnabled(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Quests")) {
					getNpcs().closest("Quest Guide").interact();
					sleepUntil(() -> getDialogues().canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Quest journal")) {
					getTabs().openWithMouse(Tab.QUEST);
					if (getTabs().isOpen(Tab.QUEST)) {
						getNpcs().closest("Quest Guide").interact();
						sleepUntil(() -> getDialogues().canContinue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Mining and Smithing")) {
					if (getLocalPlayer().getY() < 9510) {
						getNpcs().closest("Mining Instructor").interact();
						sleepUntil(() -> getDialogues().canContinue(), Calculations.random(3000, 5000));
					} else {
						getWalking().walk(new Tile(3081 + Calculations.random(-3, 0), 9505 + Calculations.random(0, 2), 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("you just need some copper")) {
					getGameObjects().closest(10079).interact();// copper rock
					sleepUntil(() -> getInventory().contains("Copper ore"), Calculations.random(5000, 8000));
				} else if (text.contains("It's quite simple really. To mine a rock")) {
					getGameObjects().closest(10080).interact();// tin rock
					sleepUntil(() -> getInventory().contains("Tin ore"), Calculations.random(5000, 8000));
				} else if (text.contains("Smelting")) {
					if (text.contains("You've made a bronze")) {
						getNpcs().closest("Mining Instructor").interact();
						sleepUntil(() -> getDialogues().canContinue(), Calculations.random(3000, 5000));
					} else {
						if (Calculations.random(0, 5) > 3)
							getInventory().get("Tin ore").useOn(getGameObjects().closest("Furnace"));
						else
							getInventory().get("Copper ore").useOn(getGameObjects().closest("Furnace"));
						sleepUntil(() -> getInventory().contains("Bronze bar"), Calculations.random(5000, 8000));
					}
				} else if (text.contains("Smithing a dagger")) {
					interfaceItem = getWidgets().getWidgetChild(312, 9);
					if (interfaceItem != null && interfaceItem.isVisible()) {
						interfaceItem.interact();
						sleepUntil(() -> getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					} else {
						getInventory().get("Bronze bar").useOn(getGameObjects().closest("Anvil"));
						sleepUntil(() -> getInventory().contains("Bronze bar"), Calculations.random(5000, 8000));
					}
				} else if (text.contains("In this area you will find")) {//Combat
					if (getLocalPlayer().getX() < 3100) {
						getWalking().walk(new Tile(3106 + Calculations.random(-1, 0), 9509 + Calculations.random(0, 1), 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
					} else {
						getNpcs().closest("Combat Instructor").interact();
						sleepUntil(() -> getDialogues().canContinue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Equipping items")) {
					getTabs().openWithMouse(Tab.EQUIPMENT);
				} else if (text.contains("Worn inventory")) {
					interfaceItem = getWidgets().getWidgetChild(387, 1);
					if (interfaceItem != null && interfaceItem.isVisible()) {
						interfaceItem.interact();
					}
				} else if (text.contains("Equipment stats")) {
					if (getInventory().contains("Bronze dagger")) {
						getInventory().get("Bronze dagger").interact();
						sleepUntil(() -> getEquipment().contains("Bronze dagger"), Calculations.random(3000, 5000));
					}
					interfaceItem = getWidgets().getWidgetChild(84, 4);
					if (interfaceItem != null && interfaceItem.isVisible()) {
						interfaceItem.interact();
					}
					getNpcs().closest("Combat Instructor").interact();
					sleepUntil(() -> getDialogues().canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Unequipping items")) {
					if (getInventory().contains("Bronze sword")) {
						getInventory().get("Bronze sword").interact();
						sleepUntil(() -> getEquipment().contains("Bronze sword"), Calculations.random(3000, 5000));
					}
					if (getInventory().contains("Wooden shield")) {
						getInventory().get("Wooden shield").interact();
						sleepUntil(() -> getEquipment().contains("Wooden shield"), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Combat interface")) {
					if (!getTabs().isOpen(Tab.COMBAT)) {
						getTabs().openWithMouse(Tab.COMBAT);
					} else {
						getWalking().walk(new Tile(3113 + Calculations.random(-1, 0), 9518 + Calculations.random(0, 1), 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						getGameObjects().closest("Gate").interact();
						sleepUntil(() -> getLocalPlayer().getX() < 3111, Calculations.random(3000, 5000));
					}
				} else if (text.contains("to slay some rats!")) {
					getNpcs().closest(getFilteredNPCs("Giant rat")).interact("Attack");
					sleepUntil(() -> getLocalPlayer().isInCombat(), Calculations.random(3000, 5000));
					sleepUntil(() -> !getLocalPlayer().isInCombat(), Calculations.random(25000, 35000));
				} else if (text.contains("made your first kill!")) {
					if (getLocalPlayer().getX() < 3111 && getLocalPlayer().getY() > 9512) {
						getGameObjects().closest("Gate").interact();
						sleepUntil(() -> getLocalPlayer().getX() > 3110, Calculations.random(3000, 5000));
					}
					getNpcs().closest("Combat Instructor").interact();
					sleepUntil(() -> getDialogues().canContinue(), Calculations.random(8000, 10000));
				} else if (text.contains("Rat ranging")) {
					if (!getTabs().isOpen(Tab.INVENTORY)) {
						if (Calculations.random(0, 5) > 3)
							getTabs().openWithFKey(Tab.INVENTORY);
						else
							getTabs().openWithMouse(Tab.INVENTORY);
						sleepUntil(() -> getTabs().isOpen(Tab.INVENTORY), Calculations.random(3000, 5000));
					}
					if (getInventory().contains("Shortbow")) {
						getInventory().get("Shortbow").interact();
						sleepUntil(() -> getEquipment().contains("Shortbow"), Calculations.random(3000, 5000));
					} else if (getInventory().contains("Bronze arrow")) {
						getInventory().get("Bronze arrow").interact();
						sleepUntil(() -> getEquipment().contains("Bronze arrow"), Calculations.random(3000, 5000));
					} else {
						if (getLocalPlayer().getX() < 3106) {
							getWalking().walk(new Tile(3108 + Calculations.random(-1, 0), 9511 + Calculations.random(0, 1), 0));
							sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
							sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						}
						getNpcs().closest(getFilteredNPCs("Giant rat")).interact("Attack");
						sleepUntil(() -> getLocalPlayer().isInCombat(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isInCombat(), Calculations.random(25000, 35000));
					}
				} else if (text.contains("Banking")) {
					if (getWidgets().getWidgetChild(12, 2) != null && getWidgets().getWidgetChild(12, 2).isVisible()) {
						interfaceItem = getWidgets().getWidgetChild(12, 2).getChild(11);
						if (interfaceItem != null && interfaceItem.isVisible()) {
							interfaceItem.interact();
							getGameObjects().closest("Poll booth").interact();
							sleep(4000);
						}
					} else {
						getWalking().walk(new Tile(3122 + Calculations.random(-1, 0), 3123 + Calculations.random(0, 1), 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						getGameObjects().closest("Bank booth").interact();
						sleep(1000);
					}
				} else if (text.contains("Account Management") ) {
					if (text.contains("Click on the flashing")) {
						getTabs().openWithMouse(Tab.ACCOUNT_MANAGEMENT);
						sleep(1000);
					} else {
						getNpcs().closest("Account Guide").interact();
						sleepUntil(() -> getDialogues().canContinue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Prayer") && !text.contains("menu")) {
					if (getLocalPlayer().getX() > 3124 && getLocalPlayer().getY() > 3110) {
						getWalking().walk(new Tile(3128 + Calculations.random(-1, 0), 3107 + Calculations.random(-1, 0), 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
					} else {
						getNpcs().closest("Brother Brace").interact();
						sleepUntil(() -> getDialogues().canContinue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Prayer menu")) {
					getTabs().openWithMouse(Tab.PRAYER);
					sleep(1000);
					getNpcs().closest("Brother Brace").interact();
					sleepUntil(() -> getDialogues().canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Friends and Ignore")) {
					getTabs().openWithMouse(Tab.FRIENDS);
					sleep(1000);
					getNpcs().closest("Brother Brace").interact();
					sleepUntil(() -> getDialogues().canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Your final instructor")) {
					if (getLocalPlayer().getY() > 3102) {
						getWalking().walk(new Tile(3122, 3103, 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						getGameObjects().closest("Door").interact();
						sleepUntil(() -> getLocalPlayer().getY() < 3103, Calculations.random(3000, 5000));
					} else {
						if (getLocalPlayer().getX() < 3136) {
							getWalking().walk(new Tile(3141 + Calculations.random(-1, 0), 3087 + Calculations.random(-1, 0), 0));
							sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
							sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						}
						getNpcs().closest("Magic Instructor").interact();
						sleepUntil(() -> getDialogues().canContinue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("your final menu")) {
					getTabs().openWithMouse(Tab.MAGIC);
					sleep(1000);
					
				} else if (text.contains("your magic interface. All of your")) {
					getNpcs().closest("Magic Instructor").interact();
					sleepUntil(() -> getDialogues().canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Magic casting")) {
					getWalking().walk(new Tile(3140 + Calculations.random(-1, 0), 3091, 0));
					sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
					sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
					interfaceItem = getWidgets().getWidgetChild(218, 6);
					if (interfaceItem != null && interfaceItem.isVisible()) {
						interfaceItem.interact();
						getNpcs().closest(getFilteredNPCs("Chicken")).interact();
						sleepUntil(() -> getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("To the mainland")) {
					getNpcs().closest("Magic Instructor").interact();
					sleepUntil(() -> getDialogues().canContinue(), Calculations.random(3000, 5000));
				}
			}
			break;
		case DIALOGUE:
			if (getDialogues().getOptions() != null && getDialogues().getOptions().length > 0) {
				if (getDialogues().getOptions().length > 2 && getDialogues().getOptions()[2].contains("No, I'm not"))
					getDialogues().chooseOption(3);
				else
					getDialogues().chooseOption(1);
			} else {
				getDialogues().spaceToContinue();
			}
			break;
		case FINISHED:
			onExit();
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
		if (!taskScript) {
			getTabs().logout();
			this.stop();
		}
	}

}
