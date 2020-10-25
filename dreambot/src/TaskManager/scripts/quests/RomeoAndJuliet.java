package TaskManager.scripts.quests;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.Category;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import TaskManager.utilities.Utilities;
import TaskManager.Script;
import TaskManager.ScriptDetails;

@ScriptDetails(author = "NumberZ", category = Category.QUEST, name = "Romeo and Juliet", version = 1.0, description = "Completes the Romeo and Juliet quest.")
public class RomeoAndJuliet extends Script {
	private State state;
	private int progressId = -1;
	private Area romeosArea = new Area(3207, 3420, 3219, 3434, 0);
	private Area fatherArea = new Area(3252, 3485, 3255, 3481, 0);
	private Area apothecaryArea = new Area(3192, 3406, 3197, 3402, 0);
	private Area dangerArea = new Area(3217, 3380, 3244, 3359, 0);
	WidgetChild interfaceItem; 
	
	public RomeoAndJuliet() {
		
	}
	
	private enum State {
		PROGRESS, DIALOGUE, FINISHED, NOTHING;
	}
	
	private State getState() {
		if (progressId == -1) {
			progressId = getQuestProgress();
		}
		if (progressId == 9)
			return State.FINISHED;
		else if (Dialogues.inDialogue()) {
			return State.DIALOGUE;
		} else if (progressId > -1)
			return State.PROGRESS;
		return State.NOTHING;
	}
	
	private int getQuestProgress() {
		interfaceItem = Widgets.getWidgetChild(399, 6, 14);
		if (interfaceItem != null) {
			if (interfaceItem.getTextColor() == 16711680)//not started/red
				return 0;
			else if (interfaceItem.getTextColor() == 16776960)//started/yellow
				return 1;
			else if (interfaceItem.getTextColor() == 901389)//completed/green
				return 9;
		}
		return -1;
	}
	
	public String getStatus() {
		switch (progressId) {
		case 0: return "Starting quest";
		case 1: return "Identifying progress...";
		case 2: return "Looking for Juliet";
		case 3: return "Looking for Romeo";
		case 4: return "Looking for Father Lawrence";
		case 5: return "Looking for Apothecary";
		case 6: return "Making potion";
		case 7: return "Giving potion to Juliet";
		case 8: return "Giving news to Romeo";
		case 9: return "Quest completed!";
		default: return "Identifying progress...";
		}
	}
	
	public GameObject getObject(Tile tile, String name) {//Not being used
		GameObject[] objects = GameObjects.getGameObjects().getObjectsOnTile(tile);
		for (int i = 0; i < objects.length; i++) {
			if (objects[i].getName().toLowerCase().contains(name.toLowerCase()))
				return objects[i];
		}
		return null;
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public int onLoop() {
		state = getState();
		if (dangerArea.contains(getLocalPlayer())) {
			if (Walking.getRunEnergy() >= 2 && !Walking.isRunEnabled()) {
				Walking.toggleRun();
			}
		}
		switch (state) {
		case PROGRESS:
			if (progressId == 0) {//quest not started
				if (romeosArea.contains(getLocalPlayer())) {
					NPCs.closest("Romeo").interact();
					sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
				} else {
					Walking.walk(romeosArea.getRandomTile());
					sleepUntil(() -> Walking.getDestinationDistance() < 6, 6000);
				}
			} else if (progressId == 2 || progressId == 7) {//go to juliet for the first time or giving her potion
				if (getLocalPlayer().getX() > 10000)
					;//cutscene, don't do anything
				else if (getLocalPlayer().getZ() == 0) {//first floor
					if (getLocalPlayer().getX() > 3168) {
						Walking.walk(new Tile(3165 + Calculations.random(1, 2), 3433 + Calculations.random(1, 2), 0));
						sleepUntil(() -> Walking.getDestinationDistance() < 4, 6000);
					} else if(getLocalPlayer().getX() < 3172) {
						if (!Map.canReach(new Tile(3164, 3433, 0))) {
							GameObjects.closest("Door").interactForceRight("Open");
							sleepUntil(() -> Map.canReach(new Tile(3164, 3433, 0)), 6000);
						} else {
							Walking.walk(new Tile(3160 + Calculations.random(1, 2), 3435 + Calculations.random(1, 2), 0));
							sleepUntil(() -> Walking.getDestinationDistance() < 4, 6000);
							GameObjects.closest("Staircase").interactForceRight("Climb-up");
							sleepUntil(() -> getLocalPlayer().getZ() == 1, 6000);
						}
					}
				} else if (getLocalPlayer().getZ() == 1) {//second floor
					if (!Map.canReach(new Tile(3158, 3429, 1))) {
						GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3157, 3430, 1)).interactForceRight("Open");
						//getObject(new Tile(3157, 3430, 1), "Door").interactForceRight("Open");
						sleepUntil(() -> Map.canReach(new Tile(3158, 3429, 1)), 6000);
					} else {
						if (!Map.canReach(new Tile(3158, 3428, 1))) {
							GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3158, 3426, 1)).interactForceRight("Open");
							//getObject(new Tile(3158, 3426, 1), "Door").interactForceRight("Open");
							sleepUntil(() -> Map.canReach(new Tile(3158, 3428, 1)), 6000);
						} else {
							if (progressId == 2 && !Inventory.contains("Message") || progressId == 7 && Inventory.contains("Cadava potion")) {
								Walking.walk(new Tile(3158 + Calculations.random(-1, 2), 3428, 1));
								sleepUntil(() -> Walking.getDestinationDistance() < 4, 6000);
								NPCs.closest("Juliet").interact();
								sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
							} else {
								if (progressId == 2)
									progressId = 3;//Message for Romeo
								else if (progressId == 7)
									progressId = 8;//no more potion, talk to Romeo
							}
						}
					}
				}
			} else if (progressId == 3 || progressId == 8) {//has Message for Romeo or doesn't have potion anymore
				if (getLocalPlayer().getX() > 10000)
					;//cutscene, don't do anything
				else if (Widgets.getWidget(277) != null && Widgets.getWidget(277).isVisible())
					progressId = 9;//Quest complete!
				else if (getLocalPlayer().getZ() == 1) {
					if (!Map.canReach(new Tile(3158, 3429, 1))) {
						GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3158, 3426, 1)).interactForceRight("Open");
						//getObject(new Tile(3158, 3426, 1), "Door").interactForceRight("Open");
						sleepUntil(() -> Map.canReach(new Tile(3158, 3429, 1)), 6000);
					} else {
						if (!Map.canReach(new Tile(3155, 3433, 1))) {
							GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3157, 3430, 1)).interactForceRight("Open");
							//getObject(new Tile(3157, 3430, 1), "Door").interactForceRight("Open");
							sleepUntil(() -> Map.canReach(new Tile(3155, 3433, 1)), 6000);
						} else {
							Walking.walk(new Tile(3155 + Calculations.random(-1, 2), 3433, 1));
							sleepUntil(() -> Walking.getDestinationDistance() < 4, 6000);
							GameObjects.closest("Staircase").interactForceRight("Climb-down");
							sleepUntil(() -> getLocalPlayer().getZ() == 0, 6000);
						}
					}
				} else if (getLocalPlayer().getZ() == 0) {
					 if (getLocalPlayer().getX() < 3165) {
						if (!Map.canReach(new Tile(3166, 3433, 0))) {
							GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3165, 3433, 0)).interactForceRight("Open");
							//getObject(new Tile(3165, 3433, 0), "Door").interactForceRight("Open");
							sleepUntil(() -> Map.canReach(new Tile(3165, 3433, 0)), 6000);
						} else {
							Walking.walk(new Tile(3211 + Calculations.random(1, 2), 3424 + Calculations.random(1, 2), 0));
							sleepUntil(() -> Walking.getDestinationDistance() < 4, 6000);
						}
					 } else if (romeosArea.contains(getLocalPlayer())) {
						NPCs.closest("Romeo").interact();
						sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
					} else {
						Walking.walk(new Tile(3211 + Calculations.random(1, 2), 3424 + Calculations.random(1, 2), 0));
						sleepUntil(() -> Walking.getDestinationDistance() < 4, 6000);
					}
				}
			} else if (progressId == 4) {//going talk to father lawrence
				if (fatherArea.contains(getLocalPlayer())) {
					NPCs.closest("Father Lawrence").interact();
					sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
				} else {
					Walking.walk(fatherArea.getRandomTile());
					sleepUntil(() -> Walking.getDestinationDistance() < 6, 6000);
				}
			} else if (progressId == 5) {//talk to the potion maker guy
				if (apothecaryArea.contains(getLocalPlayer())) {
					NPCs.closest("Apothecary").interact();
					sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
				} else {
					if ((new Tile(3192, 3403, 0)).distance(getLocalPlayer()) < 7) {
						if (!Map.canReach(new Tile(3192, 3403, 0))) {
							GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3192, 3403, 0)).interactForceRight("Open");
							//getObject(new Tile(3192, 3403, 0), "Door").interactForceRight("Open");
							sleepUntil(() -> Map.canReach(new Tile(3192, 3403, 0)), 6000);
						}
					}
					Walking.walk(apothecaryArea.getRandomTile());
					sleepUntil(() -> Walking.getDestinationDistance() < 6, 6000);
				}
			} else if (progressId == 6) {//grabbing some berries
				if (Inventory.contains("Cadava potion")) {
					progressId = 7;
				} else if (!Inventory.contains("Cadava berries")) {
				
					if ((new Tile(3267, 3368, 0)).distance(getLocalPlayer()) > 7) {
						Walking.walk(new Tile(3267 + Calculations.random(-1, 3), 3368 + Calculations.random(-1, 3), 0));
						sleepUntil(() -> Walking.getDestinationDistance() < 4, 6000);
					} else {
						GameObjects.closest(33183).interact();//berry bush
						sleepUntil(() -> Inventory.contains("Cadava berries"), 6000);
					}
				} else {
					if (apothecaryArea.contains(getLocalPlayer())) {
						NPCs.closest("Apothecary").interact();
						sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
					} else {
						if ((new Tile(3192, 3403, 0)).distance(getLocalPlayer()) < 7) {
							if (!Map.canReach(new Tile(3192, 3403, 0))) {
								GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3192, 3403, 0)).interactForceRight("Open");
								//getObject(new Tile(3192, 3403, 0), "Door").interactForceRight("Open");
								sleepUntil(() -> Map.canReach(new Tile(3192, 3403, 0)), 6000);
							}
						}
						Walking.walk(apothecaryArea.getRandomTile());
						sleepUntil(() -> Walking.getDestinationDistance() < 6, 6000);
					}
				}
			}
			break;
		case DIALOGUE:
			if (Dialogues.getOptions() != null && Dialogues.getOptions().length > 0) {
				List<String> options = Arrays.asList(Dialogues.getOptions());
				if (options.size() == 2) {
					if (options.contains("Talk about something else."))
						Dialogues.chooseOption(2);
					else if (options.contains("Yes, ok, I'll let her know."))
						Dialogues.chooseOption(1);
					
				} else if (options.size() == 3) {
					if (options.contains("Yes, I have seen her actually!"))
						Dialogues.chooseOption(1);
					else if (options.contains("Ok, thanks.")) {
						Dialogues.chooseOption(3);
						if (progressId == 0)
							progressId = 2;//quest started
					}
				} else if (options.size() == 4) {
					if (options.contains("Ok, thanks.")) {
						Dialogues.chooseOption(4);
						if (progressId == 3)
							progressId = 4;//go find father lawrence
					} else if (options.contains("Talk about Romeo & Juliet.")) {
						Dialogues.chooseOption(1);
					}
				}
			} else {
				if (Dialogues.getNPCDialogue() != null) {
					if (progressId == 4 && Dialogues.getNPCDialogue().contains("Apart from the strong"))
						progressId = 5;//go to the potion maker dude
					else if (progressId == 5 && Dialogues.getNPCDialogue().contains("I have all that, but I need some"))
						progressId = 6;//go grab some berries
				}
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
		default:
			break;
		}
		return 0;
	}
	
	@Override
    public void onExit() {
		super.onExit();
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
		Utilities.drawShadowString(g, "Stage: " + getStatus(), x, 60);
	}
}
