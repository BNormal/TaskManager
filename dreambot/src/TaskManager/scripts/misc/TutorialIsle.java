package TaskManager.scripts.misc;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Date;

import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.Menu;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import TaskManager.utilities.Utilities;
import TaskManager.Script;

@ScriptManifest(author = "NumberZ", name = "Tutorial Island", version = 1.0, description = "Does tutorial island for you.", category = Category.MISC)
public class TutorialIsle extends Script {
	WidgetChild interfaceItem = null;
	WidgetChild NPCName = null;
	private State state = null;
	
	private enum State {
		NAMING, CHARACTER_CREATION, FOLLOWING_INSTRUCTION, DIALOGUE, FINISHED, NOTHING
	}
	
	private State getState() {
		interfaceItem = Widgets.getWidgetChild(558, 11);//Setting name for new player
		if (interfaceItem != null && interfaceItem.isVisible()) {
			return State.NAMING;
		}
		interfaceItem = Widgets.getWidgetChild(269, 100);
		if (interfaceItem != null && interfaceItem.isVisible()) {
			return State.CHARACTER_CREATION;
		}
		interfaceItem = Widgets.getWidgetChild(263, 1);
		if (interfaceItem != null && interfaceItem.isVisible() && interfaceItem.getChild(0).isVisible())
			return State.FOLLOWING_INSTRUCTION;
		if (Dialogues.inDialogue()) {
			return State.DIALOGUE;
		}
		if (getLocalPlayer().getX() > 3200 && getLocalPlayer().getX() < 3300 && getLocalPlayer().getY() > 3200 && getLocalPlayer().getY() < 3300)
			return State.FINISHED;
		return State.NOTHING;
	}
	
	@Override
    public void onStart() {
		super.onStart();
		getRandomManager().disableSolver(RandomEvent.RESIZABLE_DISABLER);
		if (getRandomManager().getCurrentSolver() != null && getRandomManager().getCurrentSolver().getEventString().equalsIgnoreCase("RESIZABLE_DISABLER"))
			getRandomManager().getCurrentSolver().disable();
	}
	
	@Override
	public int onLoop() {
		state = getState();
		switch (state) {
		case NAMING:
			interfaceItem.interact();
			Keyboard.type("zezima", true);
			
			sleepUntil(() -> Widgets.getWidgetChild(558, 12) != null && Widgets.getWidgetChild(558, 12).getText().contains("not available"), Calculations.random(3000, 5000));
			WidgetChild typeNameTextField = Widgets.getWidgetChild(558, Calculations.random(14, 16));
			typeNameTextField.interact();
			
			sleepUntil(() -> Widgets.getWidgetChild(558, 12) != null && Widgets.getWidgetChild(558, 12).getText().contains("<col=00ff00>available"), Calculations.random(3000, 5000));
			WidgetChild setNameButton = Widgets.getWidgetChild(558, 18);
			setNameButton.interact();
			break;
		case CHARACTER_CREATION:
			interfaceItem.interact();
			sleepUntil(() -> Widgets.getWidgetChild(269, 100) == null || !Widgets.getWidgetChild(269, 100).isVisible(), Calculations.random(3000, 5000));
			break;
		case FOLLOWING_INSTRUCTION:
			if (interfaceItem != null && interfaceItem.isVisible()) {
				String text = interfaceItem.getChild(0).getText();
				if (text.contains("Getting started")) {
					NPC guide = NPCs.closest("Gielinor Guide");
					Point p = guide.getModel().calculateCenterPoint();
	                Rectangle GAME_SCREEN = new Rectangle(5, 5, 511, 333);
	                Rectangle CANVAS = new Rectangle(0, 0, 765, 503);
	                int heightAdjust = 0;
	                int widthAdjust = 0;
	                p.setLocation(p.getX() * (CANVAS.getWidth() / GAME_SCREEN.getWidth()) + widthAdjust, p.getY() * (CANVAS.getHeight() / GAME_SCREEN.getHeight()) + heightAdjust);
	                Mouse.hop(p);
	                sleepUntil(() -> Menu.contains("Talk-to"), 2000);
	                if (Menu.contains("Talk-to")) {
	                	Mouse.click();
	                    sleepUntil(() -> Dialogues.canContinue(), 10000);
	                }
				} else if (text.contains("Options menu")) { // Clicks on setting tab and then should continue to go into fixed mode
					if (!Tabs.isOpen(Tab.OPTIONS)) {
						Tabs.openWithMouse(Tab.OPTIONS);
						getRandomManager().enableSolver(RandomEvent.RESIZABLE_DISABLER);
					} else {
						if (Tabs.isOpen(Tab.OPTIONS)) {
							interfaceItem = Widgets.getWidgetChild(261, 1).getChild(2);
							if (interfaceItem.getTextureId() != 762) {
								interfaceItem.interact();
								sleepUntil(() -> interfaceItem.getTextureId() == 762, Calculations.random(3000, 5000));
								Widgets.getWidgetChild(261, 38).interact();
								Widgets.getWidgetChild(261, 44).interact();
								Widgets.getWidgetChild(261, 50).interact();
							}
						}
						NPCs.closest("Gielinor Guide").interact();
						sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Moving on")) {
					if (text.contains("you've just cooked")) {// cooks shrimp on fire
						Walking.walk(new Tile(3093 + Calculations.random(-3, 0), 3092 + Calculations.random(0, 2), 0));
						GameObjects.closest("Gate").interactForceRight("Open");
						sleepUntil(() -> getLocalPlayer().getX() < 3090, Calculations.random(3000, 5000));
					} else if (text.contains("with the yellow arrow")) {// clicks on door to chef
						Walking.walk(new Tile(3082 + Calculations.random(-3, 0), 3084 + Calculations.random(0, 2), 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						GameObjects.closest("Door").interact();
						sleepUntil(() -> getLocalPlayer().getX() < 3079, Calculations.random(3000, 5000));
					} else if (text.contains("Well done! You've baked")) {
						Walking.walk(new Tile(3074 + Calculations.random(-1, 0), 3089 + Calculations.random(0, 1), 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						GameObjects.closest("Door").interact();
						sleepUntil(() -> getLocalPlayer().getX() < 3072, Calculations.random(3000, 5000));
					} else if (text.contains("When you get there, click")) {
						Walking.walk(new Tile(3086 + Calculations.random(-1, 0), 3127 + Calculations.random(0, 1), 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						GameObjects.closest("Door").interact();
						sleepUntil(() -> getLocalPlayer().getY() < 3125, Calculations.random(3000, 5000));
					} else if (text.contains("It's time to enter")) {
						GameObjects.closest("Ladder").interactForceRight("Climb-down");
						sleepUntil(() -> getLocalPlayer().getX() > 3094, Calculations.random(3000, 5000));
					} else if (text.contains("made your first weapon")) {
						Walking.walk(new Tile(3094 + Calculations.random(-1, 0), 9501 + Calculations.random(0, 1), 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						GameObjects.closest("Gate").interact();
						sleepUntil(() -> getLocalPlayer().getY() > 9503, Calculations.random(3000, 5000));
					} else if (text.contains("just talk to the combat instructor")) {
						Walking.walk(new Tile(3111 + Calculations.random(-1, 0), 9525 + Calculations.random(0, 1), 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						GameObjects.closest("Ladder").interact();
						sleepUntil(() -> getLocalPlayer().getY() < 3130, Calculations.random(3000, 5000));
					} else if (text.contains("Polls are run")) {
						interfaceItem = Widgets.getWidgetChild(310, 11);
						if (interfaceItem != null && interfaceItem.isVisible()) {
							interfaceItem.interact();
						} else {
							Walking.walk(new Tile(3124, 3124, 0));
							sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
							sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
							GameObjects.closest("Door").interact();
							sleepUntil(() -> getLocalPlayer().getX() > 3124, Calculations.random(3000, 5000));
						}
					} else if (text.contains("Continue through the next door.")) {
						Walking.walk(new Tile(3129, 3124, 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						GameObjects.closest("Door").interact();
						sleepUntil(() -> getLocalPlayer().getX() > 3129, Calculations.random(3000, 5000));
					} else {
						GameObjects.closest("Door").interact();
						sleepUntil(() -> getLocalPlayer().getX() > 3097, Calculations.random(3000, 5000));
					}
				} else if (text.contains("Moving around")) {
					Walking.walk(new Tile(3103 + Calculations.random(-3, 0), 3096 + Calculations.random(0, 2), 0));
					sleepUntil(() -> NPCs.closest("Survival Expert").distance(getLocalPlayer()) < 5, Calculations.random(3000, 5000));
					NPCs.closest("Survival Expert").interact();
					sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("You've been given an item")) {
					Tabs.openWithMouse(Tab.INVENTORY);
				} else if (text.contains("Fishing")) {
					NPCs.closest("Fishing spot").interactForceRight("Net");
					sleepUntil(() -> getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					sleepUntil(() -> !getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
				} else if (text.contains("gained some experience")) {
					Tabs.openWithMouse(Tab.SKILLS);
				} else if (text.contains("Skills and Experience")) {
					NPCs.closest("Survival Expert").interact();
					sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Woodcutting")) {
					GameObject tree = GameObjects.closest("Tree");
					tree.interact();
					sleepUntil(() -> getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					sleepUntil(() -> !getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
				} else if (text.contains("Firemaking")) {
					Inventory.get("Logs").useOn("Tinderbox");
					sleepUntil(() -> getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					sleepUntil(() -> !getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
				} else if (text.contains("Cooking") && !text.contains("dough")) {
					if (text.contains("to the chef")) {
						NPCs.closest("Master Chef").interact();
						sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
					} else {
						Inventory.get("Raw shrimps").useOn(GameObjects.closest("Fire"));
						sleepUntil(() -> getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Making dough")) {
					Inventory.get("Pot of flour").useOn("Bucket of water");
					sleepUntil(() -> Inventory.contains("Bread dough"), Calculations.random(3000, 5000));
				} else if (text.contains("Cooking dough")) {
					Inventory.get("Bread dough").useOn(GameObjects.closest("Range"));
					sleepUntil(() -> getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					sleepUntil(() -> !getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
				} else if (text.contains("Fancy a run?")) {
					if (Walking.isRunEnabled()) {
						Walking.walk(new Tile(3086 + Calculations.random(-1, 0), 3127 + Calculations.random(0, 2), 0));
						Walking.toggleRun();
						sleepUntil(() -> !Walking.isRunEnabled(), Calculations.random(3000, 5000));
						Walking.toggleRun();
						sleepUntil(() -> Walking.isRunEnabled(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Quests")) {
					NPCs.closest("Quest Guide").interact();
					sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Quest journal")) {
					Tabs.openWithMouse(Tab.QUEST);
					if (Tabs.isOpen(Tab.QUEST)) {
						NPCs.closest("Quest Guide").interact();
						sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Mining and Smithing")) {
					if (getLocalPlayer().getY() < 9510) {
						NPCs.closest("Mining Instructor").interact();
						sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
					} else {
						Walking.walk(new Tile(3081 + Calculations.random(-3, 0), 9505 + Calculations.random(0, 2), 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("you just need some copper")) {
					GameObjects.closest(10079).interact();// copper rock
					sleepUntil(() -> Inventory.contains("Copper ore"), Calculations.random(5000, 8000));
				} else if (text.contains("It's quite simple really. To mine a rock")) {
					GameObjects.closest(10080).interact();// tin rock
					sleepUntil(() -> Inventory.contains("Tin ore"), Calculations.random(5000, 8000));
				} else if (text.contains("Smelting")) {
					if (text.contains("You've made a bronze")) {
						NPCs.closest("Mining Instructor").interact();
						sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
					} else {
						if (Calculations.random(0, 5) > 3)
							Inventory.get("Tin ore").useOn(GameObjects.closest("Furnace"));
						else
							Inventory.get("Copper ore").useOn(GameObjects.closest("Furnace"));
						sleepUntil(() -> Inventory.contains("Bronze bar"), Calculations.random(5000, 8000));
					}
				} else if (text.contains("Smithing a dagger")) {
					interfaceItem = Widgets.getWidgetChild(312, 9);
					if (interfaceItem != null && interfaceItem.isVisible()) {
						interfaceItem.interact();
						sleepUntil(() -> getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					} else {
						Inventory.get("Bronze bar").useOn(GameObjects.closest("Anvil"));
						sleepUntil(() -> Inventory.contains("Bronze bar"), Calculations.random(5000, 8000));
					}
				} else if (text.contains("In this area you will find")) {//Combat
					if (getLocalPlayer().getX() < 3100) {
						Walking.walk(new Tile(3106 + Calculations.random(-1, 0), 9509 + Calculations.random(0, 1), 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
					} else {
						NPCs.closest("Combat Instructor").interact();
						sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Equipping items")) {
					Tabs.openWithMouse(Tab.EQUIPMENT);
				} else if (text.contains("Worn inventory")) {
					interfaceItem = Widgets.getWidgetChild(387, 1);
					if (interfaceItem != null && interfaceItem.isVisible()) {
						interfaceItem.interact();
					}
				} else if (text.contains("Equipment stats")) {
					if (Inventory.contains("Bronze dagger")) {
						Inventory.get("Bronze dagger").interact();
						sleepUntil(() -> Equipment.contains("Bronze dagger"), Calculations.random(3000, 5000));
					}
					interfaceItem = Widgets.getWidgetChild(84, 3, 11);
					if (interfaceItem != null && interfaceItem.isVisible()) {
						interfaceItem.interact();
					}
					NPCs.closest("Combat Instructor").interact();
					sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Unequipping items")) {
					if (Inventory.contains("Bronze sword")) {
						Inventory.get("Bronze sword").interact();
						sleepUntil(() -> Equipment.contains("Bronze sword"), Calculations.random(3000, 5000));
					}
					if (Inventory.contains("Wooden shield")) {
						Inventory.get("Wooden shield").interact();
						sleepUntil(() -> Equipment.contains("Wooden shield"), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Combat interface")) {
					if (!Tabs.isOpen(Tab.COMBAT)) {
						Tabs.openWithMouse(Tab.COMBAT);
					} else {
						Walking.walk(new Tile(3113 + Calculations.random(-1, 0), 9518 + Calculations.random(0, 1), 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						GameObjects.closest("Gate").interact();
						sleepUntil(() -> getLocalPlayer().getX() < 3111, Calculations.random(3000, 5000));
					}
				} else if (text.contains("to slay some rats!")) {
					NPCs.closest(getFilteredNPCs("Giant rat")).interact("Attack");
					sleepUntil(() -> getLocalPlayer().isInCombat(), Calculations.random(3000, 5000));
					sleepUntil(() -> !getLocalPlayer().isInCombat(), Calculations.random(25000, 35000));
				} else if (text.contains("made your first kill!")) {
					if (getLocalPlayer().getX() < 3111 && getLocalPlayer().getY() > 9512) {
						GameObjects.closest("Gate").interact();
						sleepUntil(() -> getLocalPlayer().getX() > 3110, Calculations.random(3000, 5000));
					}
					if (getLocalPlayer().getY() > 9513) {
						Walking.walk(new Tile(3108 + Calculations.random(-1, 0), 9511 + Calculations.random(0, 1), 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
					}
					NPCs.closest("Combat Instructor").interact();
					sleepUntil(() -> Dialogues.canContinue(), Calculations.random(8000, 10000));
				} else if (text.contains("Rat ranging")) {
					if (!Tabs.isOpen(Tab.INVENTORY)) {
						if (Calculations.random(0, 5) > 3)
							Tabs.openWithFKey(Tab.INVENTORY);
						else
							Tabs.openWithMouse(Tab.INVENTORY);
						sleepUntil(() -> Tabs.isOpen(Tab.INVENTORY), Calculations.random(3000, 5000));
					}
					if (Inventory.contains("Shortbow")) {
						Inventory.get("Shortbow").interact();
						sleepUntil(() -> Equipment.contains("Shortbow"), Calculations.random(3000, 5000));
					} else if (Inventory.contains("Bronze arrow")) {
						Inventory.get("Bronze arrow").interact();
						sleepUntil(() -> Equipment.contains("Bronze arrow"), Calculations.random(3000, 5000));
					} else {
						if (getLocalPlayer().getX() < 3106) {
							Walking.walk(new Tile(3108 + Calculations.random(-1, 0), 9511 + Calculations.random(0, 1), 0));
							sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
							sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						}
						NPCs.closest(getFilteredNPCs("Giant rat")).interact("Attack");
						sleepUntil(() -> getLocalPlayer().isInCombat(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isInCombat(), Calculations.random(25000, 35000));
					}
				} else if (text.contains("Banking")) {
					if (Widgets.getWidgetChild(12, 2) != null && Widgets.getWidgetChild(12, 2).isVisible()) {
						interfaceItem = Widgets.getWidgetChild(12, 2).getChild(11);
						if (interfaceItem != null && interfaceItem.isVisible()) {
							interfaceItem.interact();
							GameObjects.closest("Poll booth").interact();
							sleep(4000);
						}
					} else {
						Walking.walk(new Tile(3122 + Calculations.random(-1, 0), 3123 + Calculations.random(0, 1), 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						GameObjects.closest("Bank booth").interact();
						sleep(1000);
					}
				} else if (text.contains("Account Management") ) {
					if (text.contains("Click on the flashing")) {
						Tabs.openWithMouse(Tab.ACCOUNT_MANAGEMENT);
						sleep(1000);
					} else {
						NPCs.closest("Account Guide").interact();
						sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Prayer") && !text.contains("menu")) {
					if (getLocalPlayer().getX() > 3124 && getLocalPlayer().getY() > 3110) {
						Walking.walk(new Tile(3128 + Calculations.random(-1, 0), 3107 + Calculations.random(-1, 0), 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
					} else {
						NPCs.closest("Brother Brace").interact();
						sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Prayer menu")) {
					Tabs.openWithMouse(Tab.PRAYER);
					sleep(1000);
					NPCs.closest("Brother Brace").interact();
					sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Friends and Ignore")) {
					Tabs.openWithMouse(Tab.FRIENDS);
					sleep(1000);
					NPCs.closest("Brother Brace").interact();
					sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Your final instructor")) {
					if (getLocalPlayer().getY() > 3102) {
						Walking.walk(new Tile(3122, 3103, 0));
						sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						GameObjects.closest("Door").interact();
						sleepUntil(() -> getLocalPlayer().getY() < 3103, Calculations.random(3000, 5000));
					} else {
						if (getLocalPlayer().getX() < 3136) {
							Walking.walk(new Tile(3141 + Calculations.random(-1, 0), 3087 + Calculations.random(-1, 0), 0));
							sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
							sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
						}
						NPCs.closest("Magic Instructor").interact();
						sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("your final menu")) {
					Tabs.openWithMouse(Tab.MAGIC);
					sleep(1000);
					
				} else if (text.contains("your magic interface. All of your")) {
					NPCs.closest("Magic Instructor").interact();
					sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Magic casting")) {
					Walking.walk(new Tile(3140 + Calculations.random(-1, 0), 3091, 0));
					sleepUntil(() -> getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
					sleepUntil(() -> !getLocalPlayer().isMoving(), Calculations.random(3000, 5000));
					interfaceItem = Widgets.getWidgetChild(218, 6);
					if (interfaceItem != null && interfaceItem.isVisible()) {
						interfaceItem.interact();
						NPCs.closest(getFilteredNPCs("Chicken")).interact();
						sleepUntil(() -> getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
						sleepUntil(() -> !getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("To the mainland")) {
					NPCs.closest("Magic Instructor").interact();
					sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
				}
			}
			break;
		case DIALOGUE:
			if (Dialogues.getOptions() != null && Dialogues.getOptions().length > 0) {
				if (Dialogues.getOptions().length > 2 && Dialogues.getOptions()[2].contains("No, I'm not"))
					Dialogues.chooseOption(3);
				else
					Dialogues.chooseOption(1);
			} else {
				Dialogues.spaceToContinue();
			}
			break;
		case FINISHED:
			running = false;
			time = new Date(totalTime.elapsed());
			if (!taskScript) {
				Tabs.logout();
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
		getRandomManager().enableSolver(RandomEvent.RESIZABLE_DISABLER);
		super.onExit();
	}

}
