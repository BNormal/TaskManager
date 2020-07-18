package TaskManager.scripts.quests;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import TaskManager.utilities.Utilities;
import TaskManager.Condition;
import TaskManager.Script;

@ScriptManifest(author = "NumberZ", category = Category.QUEST, name = "Romeo and Juliet", version = 1.0, description = "Completes Romeo and Juliet quest")
public class RomeoAndJuliet extends Script {
	private State state;
	private int progressId = -1;
	private Area romeosArea = new Area(3207, 3420, 3219, 3434, 0);
	private Area fatherArea = new Area(3252, 3485, 3255, 3481, 0);
	private Area apothecaryArea = new Area(3192, 3406, 3197, 3402, 0);
	private Area dangerArea = new Area(3217, 3380, 3244, 3359, 0);
	WidgetChild interfaceItem; 
	
	private enum State {
		PROGRESS, DIALOGUE, FINISHED, NOTHING;
	}
	
	private State getState() {
		if (progressId == -1) {
			progressId = getQuestProgress();
		}
		if (progressId == 9)
			return State.FINISHED;
		else if (engine.getDialogues().inDialogue()) {
			return State.DIALOGUE;
		} else if (progressId > -1)
			return State.PROGRESS;
		return State.NOTHING;
	}
	
	private int getQuestProgress() {
		interfaceItem = engine.getWidgets().getWidgetChild(399, 6, 14);
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
		GameObject[] objects = engine.getGameObjects().getObjectsOnTile(tile);
		for (int i = 0; i < objects.length; i++) {
			if (objects[i].getName().toLowerCase().contains(name.toLowerCase()))
				return objects[i];
		}
		return null;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if (engine == null)
			engine = this;
	}
	
	@Override
	public int onLoop() {
		state = getState();
		if (dangerArea.contains(engine.getLocalPlayer())) {
			if (engine.getWalking().getRunEnergy() >= 2 && !engine.getWalking().isRunEnabled()) {
				engine.getWalking().toggleRun();
			}
		}
		switch (state) {
		case PROGRESS:
			if (progressId == 0) {//quest not started
				if (romeosArea.contains(engine.getLocalPlayer())) {
					engine.getNpcs().closest("Romeo").interact();
					sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
				} else {
					engine.getWalking().walk(romeosArea.getRandomTile());
					sleepUntil(() -> engine.getWalking().getDestinationDistance() < 6, 6000);
				}
			} else if (progressId == 2 || progressId == 7) {//go to juliet for the first time or giving her potion
				if (engine.getLocalPlayer().getX() > 10000)
					;//cutscene, don't do anything
				else if (engine.getLocalPlayer().getZ() == 0) {//first floor
					if (engine.getLocalPlayer().getX() > 3168) {
						engine.getWalking().walk(new Tile(3165 + Calculations.random(1, 2), 3433 + Calculations.random(1, 2), 0));
						sleepUntil(() -> engine.getWalking().getDestinationDistance() < 4, 6000);
					} else if(engine.getLocalPlayer().getX() < 3172) {
						if (!engine.getMap().canReach(new Tile(3164, 3433, 0))) {
							engine.getGameObjects().closest("Door").interactForceRight("Open");
							sleepUntil(() -> engine.getMap().canReach(new Tile(3164, 3433, 0)), 6000);
						} else {
							engine.getWalking().walk(new Tile(3160 + Calculations.random(1, 2), 3435 + Calculations.random(1, 2), 0));
							sleepUntil(() -> engine.getWalking().getDestinationDistance() < 4, 6000);
							engine.getGameObjects().closest("Staircase").interactForceRight("Climb-up");
							sleepUntil(() -> engine.getLocalPlayer().getZ() == 1, 6000);
						}
					}
				} else if (engine.getLocalPlayer().getZ() == 1) {//second floor
					if (!engine.getMap().canReach(new Tile(3158, 3429, 1))) {
						engine.getGameObjects().getTopObjectOnTile(new Tile(3157, 3430, 1)).interactForceRight("Open");
						//getObject(new Tile(3157, 3430, 1), "Door").interactForceRight("Open");
						sleepUntil(() -> engine.getMap().canReach(new Tile(3158, 3429, 1)), 6000);
					} else {
						if (!engine.getMap().canReach(new Tile(3158, 3428, 1))) {
							engine.getGameObjects().getTopObjectOnTile(new Tile(3158, 3426, 1)).interactForceRight("Open");
							//getObject(new Tile(3158, 3426, 1), "Door").interactForceRight("Open");
							sleepUntil(() -> engine.getMap().canReach(new Tile(3158, 3428, 1)), 6000);
						} else {
							if (progressId == 2 && !engine.getInventory().contains("Message") || progressId == 7 && engine.getInventory().contains("Cadava potion")) {
								engine.getWalking().walk(new Tile(3158 + Calculations.random(-1, 2), 3428, 1));
								sleepUntil(() -> engine.getWalking().getDestinationDistance() < 4, 6000);
								engine.getNpcs().closest("Juliet").interact();
								sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
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
				if (engine.getLocalPlayer().getX() > 10000)
					;//cutscene, don't do anything
				else if (engine.getWidgets().getWidget(277) != null && engine.getWidgets().getWidget(277).isVisible())
					progressId = 9;//Quest complete!
				else if (engine.getLocalPlayer().getZ() == 1) {
					if (!engine.getMap().canReach(new Tile(3158, 3429, 1))) {
						engine.getGameObjects().getTopObjectOnTile(new Tile(3158, 3426, 1)).interactForceRight("Open");
						//getObject(new Tile(3158, 3426, 1), "Door").interactForceRight("Open");
						sleepUntil(() -> engine.getMap().canReach(new Tile(3158, 3429, 1)), 6000);
					} else {
						if (!engine.getMap().canReach(new Tile(3155, 3433, 1))) {
							engine.getGameObjects().getTopObjectOnTile(new Tile(3157, 3430, 1)).interactForceRight("Open");
							//getObject(new Tile(3157, 3430, 1), "Door").interactForceRight("Open");
							sleepUntil(() -> engine.getMap().canReach(new Tile(3155, 3433, 1)), 6000);
						} else {
							engine.getWalking().walk(new Tile(3155 + Calculations.random(-1, 2), 3433, 1));
							sleepUntil(() -> engine.getWalking().getDestinationDistance() < 4, 6000);
							engine.getGameObjects().closest("Staircase").interactForceRight("Climb-down");
							sleepUntil(() -> engine.getLocalPlayer().getZ() == 0, 6000);
						}
					}
				} else if (engine.getLocalPlayer().getZ() == 0) {
					 if (engine.getLocalPlayer().getX() < 3165) {
						if (!engine.getMap().canReach(new Tile(3166, 3433, 0))) {
							engine.getGameObjects().getTopObjectOnTile(new Tile(3165, 3433, 0)).interactForceRight("Open");
							//getObject(new Tile(3165, 3433, 0), "Door").interactForceRight("Open");
							sleepUntil(() -> engine.getMap().canReach(new Tile(3165, 3433, 0)), 6000);
						} else {
							engine.getWalking().walk(new Tile(3211 + Calculations.random(1, 2), 3424 + Calculations.random(1, 2), 0));
							sleepUntil(() -> engine.getWalking().getDestinationDistance() < 4, 6000);
						}
					 } else if (romeosArea.contains(engine.getLocalPlayer())) {
						engine.getNpcs().closest("Romeo").interact();
						sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
					} else {
						engine.getWalking().walk(new Tile(3211 + Calculations.random(1, 2), 3424 + Calculations.random(1, 2), 0));
						sleepUntil(() -> engine.getWalking().getDestinationDistance() < 4, 6000);
					}
				}
			} else if (progressId == 4) {//going talk to father lawrence
				if (fatherArea.contains(engine.getLocalPlayer())) {
					engine.getNpcs().closest("Father Lawrence").interact();
					sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
				} else {
					engine.getWalking().walk(fatherArea.getRandomTile());
					sleepUntil(() -> engine.getWalking().getDestinationDistance() < 6, 6000);
				}
			} else if (progressId == 5) {//talk to the potion maker guy
				if (apothecaryArea.contains(engine.getLocalPlayer())) {
					engine.getNpcs().closest("Apothecary").interact();
					sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
				} else {
					if ((new Tile(3192, 3403, 0)).distance(engine.getLocalPlayer()) < 7) {
						if (!engine.getMap().canReach(new Tile(3192, 3403, 0))) {
							engine.getGameObjects().getTopObjectOnTile(new Tile(3192, 3403, 0)).interactForceRight("Open");
							//getObject(new Tile(3192, 3403, 0), "Door").interactForceRight("Open");
							sleepUntil(() -> engine.getMap().canReach(new Tile(3192, 3403, 0)), 6000);
						}
					}
					engine.getWalking().walk(apothecaryArea.getRandomTile());
					sleepUntil(() -> engine.getWalking().getDestinationDistance() < 6, 6000);
				}
			} else if (progressId == 6) {//grabbing some berries
				if (engine.getInventory().contains("Cadava potion")) {
					progressId = 7;
				} else if (!engine.getInventory().contains("Cadava berries")) {
				
					if ((new Tile(3267, 3368, 0)).distance(engine.getLocalPlayer()) > 7) {
						engine.getWalking().walk(new Tile(3267 + Calculations.random(-1, 3), 3368 + Calculations.random(-1, 3), 0));
						sleepUntil(() -> engine.getWalking().getDestinationDistance() < 4, 6000);
					} else {
						engine.getGameObjects().closest(33183).interact();//berry bush
						sleepUntil(() -> engine.getInventory().contains("Cadava berries"), 6000);
					}
				} else {
					if (apothecaryArea.contains(engine.getLocalPlayer())) {
						engine.getNpcs().closest("Apothecary").interact();
						sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
					} else {
						if ((new Tile(3192, 3403, 0)).distance(engine.getLocalPlayer()) < 7) {
							if (!engine.getMap().canReach(new Tile(3192, 3403, 0))) {
								engine.getGameObjects().getTopObjectOnTile(new Tile(3192, 3403, 0)).interactForceRight("Open");
								//getObject(new Tile(3192, 3403, 0), "Door").interactForceRight("Open");
								sleepUntil(() -> engine.getMap().canReach(new Tile(3192, 3403, 0)), 6000);
							}
						}
						engine.getWalking().walk(apothecaryArea.getRandomTile());
						sleepUntil(() -> engine.getWalking().getDestinationDistance() < 6, 6000);
					}
				}
			}
			break;
		case DIALOGUE:
			if (engine.getDialogues().getOptions() != null && engine.getDialogues().getOptions().length > 0) {
				List<String> options = Arrays.asList(engine.getDialogues().getOptions());
				if (options.size() == 2) {
					if (options.contains("Talk about something else."))
						engine.getDialogues().chooseOption(2);
					else if (options.contains("Yes, ok, I'll let her know."))
						engine.getDialogues().chooseOption(1);
					
				} else if (options.size() == 3) {
					if (options.contains("Yes, I have seen her actually!"))
						engine.getDialogues().chooseOption(1);
					else if (options.contains("Ok, thanks.")) {
						engine.getDialogues().chooseOption(3);
						if (progressId == 0)
							progressId = 2;//quest started
					}
				} else if (options.size() == 4) {
					if (options.contains("Ok, thanks.")) {
						engine.getDialogues().chooseOption(4);
						if (progressId == 3)
							progressId = 4;//go find father lawrence
					} else if (options.contains("Talk about Romeo & Juliet.")) {
						engine.getDialogues().chooseOption(1);
					}
				}
			} else {
				if (engine.getDialogues().getNPCDialogue() != null) {
					if (progressId == 4 && engine.getDialogues().getNPCDialogue().contains("Apart from the strong"))
						progressId = 5;//go to the potion maker dude
					else if (progressId == 5 && engine.getDialogues().getNPCDialogue().contains("I have all that, but I need some"))
						progressId = 6;//go grab some berries
				}
				engine.getDialogues().spaceToContinue();
			}
			break;
		case FINISHED:
			running = false;
			time = new Date(totalTime.elapsed());
			if (!taskScript) {
				log("logged out");
				engine.getTabs().logout();
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
		running = false;
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
