package TaskManager.scripts.misc;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Date;

import org.dreambot.api.ClientSettings;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.input.Keyboard;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.Category;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import TaskManager.utilities.Utilities;
import TaskManager.Script;
import TaskManager.ScriptDetails;

@ScriptDetails(author = "NumberZ", name = "Tutorial Island", version = 1.0, description = "Does tutorial island for you.", category = Category.MISC)
public class TutorialIsle extends Script {
	WidgetChild interfaceItem = null;
	WidgetChild NPCName = null;
	private State state = null;
	
	public TutorialIsle() {
		
	}
	
	private enum State {
		NAMING, CHARACTER_CREATION, FOLLOWING_INSTRUCTION, DIALOGUE, FINISHED, NOTHING
	}
	
	private State getState() {
		interfaceItem = Widgets.getWidgetChild(558, 12);//Setting name for new player
		if (interfaceItem != null && interfaceItem.isVisible()) {
			return State.NAMING;
		}
		interfaceItem = Widgets.getWidgetChild(679, 68, 9);//Character Creation interface
		if (interfaceItem != null && interfaceItem.isVisible()) {
			return State.CHARACTER_CREATION;
		}
		interfaceItem = Widgets.getWidgetChild(263, 1);
		if (interfaceItem != null && interfaceItem.isVisible() && interfaceItem.getChild(0).isVisible())
			return State.FOLLOWING_INSTRUCTION;
		if (Dialogues.inDialogue()) {
			return State.DIALOGUE;
		}
		if (Players.getLocal().getX() > 3200 && Players.getLocal().getX() < 3300 && Players.getLocal().getY() > 3200 && Players.getLocal().getY() < 3300)
			return State.FINISHED;
		return State.NOTHING;
	}
	
	@Override
    public void onStart() {
		super.onStart();
		/*getRandomManager().disableSolver(RandomEvent.RESIZABLE_DISABLER);
		if (getRandomManager().getCurrentSolver() != null && getRandomManager().getCurrentSolver().getEventString().equalsIgnoreCase("RESIZABLE_DISABLER"))
			getRandomManager().getCurrentSolver().disable();*/
	}
	
	@Override
	public int onLoop() {
		state = getState();
		switch (state) {
		case NAMING:
			if (Widgets.getWidgetChild(558, 12).getText().equals("*")) {
				interfaceItem.interact();
				Keyboard.type("zezima", true);
				Sleep.sleepUntil(() -> Widgets.getWidgetChild(558, 13).getText().contains("not available"), Calculations.random(3000, 5000));
			} else if (Widgets.getWidgetChild(558, 13).getText().contains("not available")) {
				WidgetChild typeNameTextField = Widgets.getWidgetChild(558, Calculations.random(14, 16));
				typeNameTextField.interact();
				Sleep.sleepUntil(() -> Widgets.getWidgetChild(558, 13).getText().contains("<col=00ff00>available"), Calculations.random(3000, 5000));
			} else if (Widgets.getWidgetChild(558, 13).getText().contains("<col=00ff00>available")) {
				Widgets.getWidgetChild(558, 18).interact();
				Sleep.sleepUntil(() -> Widgets.getWidgetChild(558, 18) == null || !Widgets.getWidgetChild(558, 18).isVisible(), Calculations.random(3000, 5000));
			}
			break;
		case CHARACTER_CREATION:
			interfaceItem.interact();
			Sleep.sleepUntil(() -> Widgets.getWidgetChild(679, 68, 9) == null || !Widgets.getWidgetChild(679, 68, 9).isVisible(), Calculations.random(3000, 5000));
			break;
		case FOLLOWING_INSTRUCTION:
			if (interfaceItem != null && interfaceItem.isVisible()) {
				String text = interfaceItem.getChild(0).getText();
				if (text.contains("Getting started")) {
					NPCs.closest("Gielinor Guide").interact();
					Sleep.sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Settings menu")) { // Clicks on setting tab and then should continue to go into fixed mode
					if (ClientSettings.isResizableActive()) {
						if (Tabs.isOpen(Tab.OPTIONS)) {
							String currentTab = Widgets.getWidgetChild(116, 2).getText();
							if (currentTab.equals("Controls Settings")) {
								Widgets.getWidgetChild(116, 70).interact();
								Sleep.sleepUntil(() -> Widgets.getWidgetChild(116, 2).getText().equals("Audio Settings"), Calculations.random(3000, 5000));
							} else if (currentTab.equals("Audio Settings")) {
								WidgetChild musicW = Widgets.getWidgetChild(116, 14);//Music
								if (musicW.getActions()[0].equals("Mute")) {
									musicW.interact();
								}
								WidgetChild effectW = Widgets.getWidgetChild(116, 18);//Sound Effects
								if (effectW.getActions()[0].equals("Mute")) {
									effectW.interact();
								}
								WidgetChild areaW = Widgets.getWidgetChild(116, 22);//Area Sounds
								if (areaW.getActions()[0].equals("Mute")) {
									areaW.interact();
								}
								if (musicW.getActions()[0].equals("Unmute") && effectW.getActions()[0].equals("Unmute") && areaW.getActions()[0].equals("Unmute")) {
									Widgets.getWidgetChild(116, 71).interact();
									Sleep.sleepUntil(() -> Widgets.getWidgetChild(116, 2).getText().equals("Display Settings"), Calculations.random(3000, 5000));
								}
							} else if (currentTab.equals("Display Settings")) {
								if (Widgets.getWidgetChild(116, 37) == null || !Widgets.getWidgetChild(116, 37).isVisible()) {//checks if dropdown menu is opened
									Widgets.getWidgetChild(116, 12, 4).interact();//clicked to open dropdown
									Sleep.sleepUntil(() -> Widgets.getWidgetChild(116, 37) != null, Calculations.random(3000, 5000));
								} else {
									Widgets.getWidgetChild(116, 37, 1).interact();
									Sleep.sleepUntil(() -> !ClientSettings.isResizableActive(), Calculations.random(3000, 5000));
								}
							}
						} else {
							Tabs.openWithMouse(Tab.OPTIONS);
							Sleep.sleepUntil(() -> Tabs.isOpen(Tab.OPTIONS), Calculations.random(3000, 5000));
						}
					} else {
						NPCs.closest("Gielinor Guide").interact();
						Sleep.sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Moving on")) {
					if (text.contains("you've just cooked")) {// cooks shrimp on fire
						Walking.walk(new Tile(3093 + Calculations.random(-3, 0), 3092 + Calculations.random(0, 2), 0));
						GameObjects.closest("Gate").interactForceRight("Open");
						Sleep.sleepUntil(() -> Players.getLocal().getX() < 3090, Calculations.random(3000, 5000));
					} else if (text.contains("with the yellow arrow")) {// clicks on door to chef
						Walking.walk(new Tile(3082 + Calculations.random(-3, 0), 3084 + Calculations.random(0, 2), 0));
						Sleep.sleepUntil(() -> Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						GameObjects.closest("Door").interact();
						Sleep.sleepUntil(() -> Players.getLocal().getX() < 3079, Calculations.random(3000, 5000));
					} else if (text.contains("Well done! You've baked")) {
						Walking.walk(new Tile(3074 + Calculations.random(-1, 0), 3089 + Calculations.random(0, 1), 0));
						Sleep.sleepUntil(() -> Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						GameObjects.closest("Door").interact();
						Sleep.sleepUntil(() -> Players.getLocal().getX() < 3072, Calculations.random(3000, 5000));
					} else if (text.contains("When you get there, click")) {
						Walking.walk(new Tile(3086 + Calculations.random(-1, 0), 3127 + Calculations.random(0, 1), 0));
						Sleep.sleepUntil(() -> Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						GameObjects.closest("Door").interact();
						Sleep.sleepUntil(() -> Players.getLocal().getY() < 3125, Calculations.random(3000, 5000));
					} else if (text.contains("It's time to enter")) {
						GameObjects.closest("Ladder").interactForceRight("Climb-down");
						Sleep.sleepUntil(() -> Players.getLocal().getX() > 3094, Calculations.random(3000, 5000));
					} else if (text.contains("made your first weapon")) {
						Walking.walk(new Tile(3094 + Calculations.random(-1, 0), 9501 + Calculations.random(0, 1), 0));
						Sleep.sleepUntil(() -> Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						GameObjects.closest("Gate").interact();
						Sleep.sleepUntil(() -> Players.getLocal().getY() > 9503, Calculations.random(3000, 5000));
					} else if (text.contains("just talk to the combat instructor")) {
						Walking.walk(new Tile(3111 + Calculations.random(-1, 0), 9525 + Calculations.random(0, 1), 0));
						Sleep.sleepUntil(() -> Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						GameObjects.closest("Ladder").interact();
						Sleep.sleepUntil(() -> Players.getLocal().getY() < 3130, Calculations.random(3000, 5000));
					} else if (text.contains("Polls are run")) {
						interfaceItem = Widgets.getWidgetChild(310, 11);
						if (interfaceItem != null && interfaceItem.isVisible()) {
							interfaceItem.interact();
						} else {
							Walking.walk(new Tile(3124, 3124, 0));
							Sleep.sleepUntil(() -> Players.getLocal().isMoving(), Calculations.random(3000, 5000));
							Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), Calculations.random(3000, 5000));
							GameObjects.closest("Door").interact();
							Sleep.sleepUntil(() -> Players.getLocal().getX() > 3124, Calculations.random(3000, 5000));
						}
					} else if (text.contains("Continue through the next door.")) {
						Walking.walk(new Tile(3129, 3124, 0));
						Sleep.sleepUntil(() -> Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						GameObjects.closest("Door").interact();
						Sleep.sleepUntil(() -> Players.getLocal().getX() > 3129, Calculations.random(3000, 5000));
					} else {
						GameObjects.closest("Door").interact();
						Sleep.sleepUntil(() -> Players.getLocal().getX() > 3097, Calculations.random(3000, 5000));
					}
				} else if (text.contains("Moving around")) {
					Walking.walk(new Tile(3103 + Calculations.random(-3, 0), 3096 + Calculations.random(0, 2), 0));
					Sleep.sleepUntil(() -> NPCs.closest("Survival Expert").distance(Players.getLocal()) < 5, Calculations.random(3000, 5000));
					NPCs.closest("Survival Expert").interact();
					Sleep.sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("You've been given an item")) {
					Tabs.openWithMouse(Tab.INVENTORY);
				} else if (text.contains("Fishing")) {
					NPCs.closest("Fishing spot").interactForceRight("Net");
					Sleep.sleepUntil(() -> Players.getLocal().isAnimating(), Calculations.random(3000, 5000));
					Sleep.sleepUntil(() -> !Players.getLocal().isAnimating(), Calculations.random(3000, 5000));
				} else if (text.contains("gained some experience")) {
					Tabs.openWithMouse(Tab.SKILLS);
				} else if (text.contains("Skills and Experience")) {
					NPCs.closest("Survival Expert").interact();
					Sleep.sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Woodcutting")) {
					GameObject tree = GameObjects.closest("Tree");
					tree.interact();
					Sleep.sleepUntil(() -> Players.getLocal().isAnimating(), Calculations.random(3000, 5000));
					Sleep.sleepUntil(() -> !Players.getLocal().isAnimating(), Calculations.random(3000, 5000));
				} else if (text.contains("Firemaking")) {
					Inventory.get("Logs").useOn("Tinderbox");
					Sleep.sleepUntil(() -> Players.getLocal().isAnimating(), Calculations.random(3000, 5000));
					Sleep.sleepUntil(() -> !Players.getLocal().isAnimating(), Calculations.random(3000, 5000));
				} else if (text.contains("Cooking") && !text.contains("dough")) {
					if (text.contains("to the chef")) {
						NPCs.closest("Master Chef").interact();
						Sleep.sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
					} else {
						Inventory.get("Raw shrimps").useOn(GameObjects.closest("Fire"));
						Sleep.sleepUntil(() -> Players.getLocal().isAnimating(), Calculations.random(3000, 5000));
						Sleep.sleepUntil(() -> !Players.getLocal().isAnimating(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Making dough")) {
					Inventory.get("Pot of flour").useOn("Bucket of water");
					Sleep.sleepUntil(() -> Inventory.contains("Bread dough"), Calculations.random(3000, 5000));
				} else if (text.contains("Cooking dough")) {
					Inventory.get("Bread dough").useOn(GameObjects.closest("Range"));
					Sleep.sleepUntil(() -> Players.getLocal().isAnimating(), Calculations.random(3000, 5000));
					Sleep.sleepUntil(() -> !Players.getLocal().isAnimating(), Calculations.random(3000, 5000));
				} else if (text.contains("Fancy a run?")) {
					if (Walking.isRunEnabled()) {
						Walking.walk(new Tile(3086 + Calculations.random(-1, 0), 3127 + Calculations.random(0, 2), 0));
						Walking.toggleRun();
						Sleep.sleepUntil(() -> !Walking.isRunEnabled(), Calculations.random(3000, 5000));
						Walking.toggleRun();
						Sleep.sleepUntil(() -> Walking.isRunEnabled(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Quests")) {
					NPCs.closest("Quest Guide").interact();
					Sleep.sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Quest journal")) {
					Tabs.openWithMouse(Tab.QUEST);
					if (Tabs.isOpen(Tab.QUEST)) {
						NPCs.closest("Quest Guide").interact();
						Sleep.sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Mining and Smithing")) {
					if (Players.getLocal().getY() < 9510) {
						NPCs.closest("Mining Instructor").interact();
						Sleep.sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
					} else {
						Walking.walk(new Tile(3081 + Calculations.random(-3, 0), 9505 + Calculations.random(0, 2), 0));
						Sleep.sleepUntil(() -> Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("you just need some copper")) {
					GameObjects.closest(10079).interact();// copper rock
					Sleep.sleepUntil(() -> Inventory.contains("Copper ore"), Calculations.random(5000, 8000));
				} else if (text.contains("It's quite simple really. To mine a rock")) {
					GameObjects.closest(10080).interact();// tin rock
					Sleep.sleepUntil(() -> Inventory.contains("Tin ore"), Calculations.random(5000, 8000));
				} else if (text.contains("Smelting")) {
					if (text.contains("You've made a bronze")) {
						NPCs.closest("Mining Instructor").interact();
						Sleep.sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
					} else {
						if (Calculations.random(0, 5) > 3)
							Inventory.get("Tin ore").useOn(GameObjects.closest("Furnace"));
						else
							Inventory.get("Copper ore").useOn(GameObjects.closest("Furnace"));
						Sleep.sleepUntil(() -> Inventory.contains("Bronze bar"), Calculations.random(5000, 8000));
					}
				} else if (text.contains("Smithing a dagger")) {
					interfaceItem = Widgets.getWidgetChild(312, 9);
					if (interfaceItem != null && interfaceItem.isVisible()) {
						interfaceItem.interact();
						Sleep.sleepUntil(() -> Players.getLocal().isAnimating(), Calculations.random(3000, 5000));
						Sleep.sleepUntil(() -> !Players.getLocal().isAnimating(), Calculations.random(3000, 5000));
					} else {
						Inventory.get("Bronze bar").useOn(GameObjects.closest("Anvil"));
						Sleep.sleepUntil(() -> Inventory.contains("Bronze bar"), Calculations.random(5000, 8000));
					}
				} else if (text.contains("In this area you will find")) {//Combat
					if (Players.getLocal().getX() < 3100) {
						Walking.walk(new Tile(3106 + Calculations.random(-1, 0), 9509 + Calculations.random(0, 1), 0));
						Sleep.sleepUntil(() -> Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), Calculations.random(3000, 5000));
					} else {
						NPCs.closest("Combat Instructor").interact();
						Sleep.sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
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
						Sleep.sleepUntil(() -> Equipment.contains("Bronze dagger"), Calculations.random(3000, 5000));
					}
					interfaceItem = Widgets.getWidgetChild(84, 3, 11);
					if (interfaceItem != null && interfaceItem.isVisible()) {
						interfaceItem.interact();
					}
					NPCs.closest("Combat Instructor").interact();
					Sleep.sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Unequipping items")) {
					if (Inventory.contains("Bronze sword")) {
						Inventory.get("Bronze sword").interact();
						Sleep.sleepUntil(() -> Equipment.contains("Bronze sword"), Calculations.random(3000, 5000));
					}
					if (Inventory.contains("Wooden shield")) {
						Inventory.get("Wooden shield").interact();
						Sleep.sleepUntil(() -> Equipment.contains("Wooden shield"), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Combat interface")) {
					if (!Tabs.isOpen(Tab.COMBAT)) {
						Tabs.openWithMouse(Tab.COMBAT);
					} else {
						Walking.walk(new Tile(3113 + Calculations.random(-1, 0), 9518 + Calculations.random(0, 1), 0));
						Sleep.sleepUntil(() -> Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						GameObjects.closest("Gate").interact();
						Sleep.sleepUntil(() -> Players.getLocal().getX() < 3111, Calculations.random(3000, 5000));
					}
				} else if (text.contains("to slay some rats!")) {
					NPCs.closest(getFilteredNPCs("Giant rat")).interact("Attack");
					Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), Calculations.random(3000, 5000));
					Sleep.sleepUntil(() -> !Players.getLocal().isInCombat(), Calculations.random(25000, 35000));
				} else if (text.contains("made your first kill!")) {
					if (Players.getLocal().getX() < 3111 && Players.getLocal().getY() > 9512) {
						GameObjects.closest("Gate").interact();
						Sleep.sleepUntil(() -> Players.getLocal().getX() > 3110, Calculations.random(3000, 5000));
					}
					if (Players.getLocal().getY() > 9513) {
						Walking.walk(new Tile(3108 + Calculations.random(-1, 0), 9511 + Calculations.random(0, 1), 0));
						Sleep.sleepUntil(() -> Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), Calculations.random(3000, 5000));
					}
					NPCs.closest("Combat Instructor").interact();
					Sleep.sleepUntil(() -> Dialogues.canContinue(), Calculations.random(8000, 10000));
				} else if (text.contains("Rat ranging")) {
					if (!Tabs.isOpen(Tab.INVENTORY)) {
						if (Calculations.random(0, 5) > 3)
							Tabs.openWithFKey(Tab.INVENTORY);
						else
							Tabs.openWithMouse(Tab.INVENTORY);
						Sleep.sleepUntil(() -> Tabs.isOpen(Tab.INVENTORY), Calculations.random(3000, 5000));
					}
					if (Inventory.contains("Shortbow")) {
						Inventory.get("Shortbow").interact();
						Sleep.sleepUntil(() -> Equipment.contains("Shortbow"), Calculations.random(3000, 5000));
					} else if (Inventory.contains("Bronze arrow")) {
						Inventory.get("Bronze arrow").interact();
						Sleep.sleepUntil(() -> Equipment.contains("Bronze arrow"), Calculations.random(3000, 5000));
					} else {
						if (Players.getLocal().getX() < 3106) {
							Walking.walk(new Tile(3108 + Calculations.random(-1, 0), 9511 + Calculations.random(0, 1), 0));
							Sleep.sleepUntil(() -> Players.getLocal().isMoving(), Calculations.random(3000, 5000));
							Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						}
						NPCs.closest(getFilteredNPCs("Giant rat")).interact("Attack");
						Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), Calculations.random(3000, 5000));
						Sleep.sleepUntil(() -> !Players.getLocal().isInCombat(), Calculations.random(25000, 35000));
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
						Sleep.sleepUntil(() -> Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						GameObjects.closest("Bank booth").interact();
						sleep(1000);
					}
				} else if (text.contains("Account Management") ) {
					if (text.contains("Click on the flashing")) {
						Tabs.openWithMouse(Tab.ACCOUNT_MANAGEMENT);
						sleep(1000);
					} else {
						NPCs.closest("Account Guide").interact();
						Sleep.sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Prayer") && !text.contains("menu")) {
					if (Players.getLocal().getX() > 3124 && Players.getLocal().getY() > 3110) {
						Walking.walk(new Tile(3128 + Calculations.random(-1, 0), 3107 + Calculations.random(-1, 0), 0));
						Sleep.sleepUntil(() -> Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), Calculations.random(3000, 5000));
					} else {
						NPCs.closest("Brother Brace").interact();
						Sleep.sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("Prayer menu")) {
					Tabs.openWithMouse(Tab.PRAYER);
					sleep(1000);
					NPCs.closest("Brother Brace").interact();
					Sleep.sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Friends and Ignore")) {
					Tabs.openWithMouse(Tab.FRIENDS);
					sleep(1000);
					NPCs.closest("Brother Brace").interact();
					Sleep.sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Your final instructor")) {
					if (Players.getLocal().getY() > 3102) {
						Walking.walk(new Tile(3122, 3103, 0));
						Sleep.sleepUntil(() -> Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						GameObjects.closest("Door").interact();
						Sleep.sleepUntil(() -> Players.getLocal().getY() < 3103, Calculations.random(3000, 5000));
					} else {
						if (Players.getLocal().getX() < 3136) {
							Walking.walk(new Tile(3141 + Calculations.random(-1, 0), 3087 + Calculations.random(-1, 0), 0));
							Sleep.sleepUntil(() -> Players.getLocal().isMoving(), Calculations.random(3000, 5000));
							Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), Calculations.random(3000, 5000));
						}
						NPCs.closest("Magic Instructor").interact();
						Sleep.sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
					}
				} else if (text.contains("your final menu")) {
					Tabs.openWithMouse(Tab.MAGIC);
					sleep(1000);
					
				} else if (text.contains("your magic interface. All of your")) {
					NPCs.closest("Magic Instructor").interact();
					Sleep.sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
				} else if (text.contains("Magic casting")) {
					Walking.walk(new Tile(3140 + Calculations.random(-1, 0), 3091, 0));
					Sleep.sleepUntil(() -> Players.getLocal().isMoving(), Calculations.random(3000, 5000));
					Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), Calculations.random(3000, 5000));
					interfaceItem = Widgets.getWidgetChild(218, 6);
					if (interfaceItem != null && interfaceItem.isVisible()) {
						interfaceItem.interact();
						NPC chicken = NPCs.closest(getFilteredNPCs("Chicken"));
						chicken.interact("Cast");
						Sleep.sleepUntil(() -> Players.getLocal().isAnimating(), Calculations.random(1000, 3000));
						Sleep.sleepUntil(() -> !Players.getLocal().isAnimating(), Calculations.random(1000, 3000));
					}
				} else if (text.contains("To the mainland")) {
					NPCs.closest("Magic Instructor").interact();
					Sleep.sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
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
		//getRandomManager().enableSolver(RandomEvent.RESIZABLE_DISABLER);
		super.onExit();
	}

}
