package TaskManager.scripts.quests;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import TaskManager.Script;
import TaskManager.utilities.Utilities;

@ScriptManifest(author = "NumberZ", category = Category.QUEST, name = "Ernest The Chicken", version = 1.0, description = "Completes Ernest The Chicken quest")
public class ErnestTheChicken extends Script {
	private State state;
	private int progressId = -1;
	private int gatherItemsId = -1;
	private int puzzleStageId = 0;
	WidgetChild questStatus;
	private Area startArea = new Area(3108, 3330, 3111, 3327, 0);
	private Area spadeArea = new Area(3120, 3360, 3125, 3355, 0);
	private int[] mansionX = {3091, 3091, 3097, 3097, 3119, 3120, 3126, 3126};
	private int[] mansionY = {3354, 3363, 3363, 3373, 3373, 3360, 3361, 3354};
	private Shape mansionArea = new Polygon(mansionX, mansionY, 8);
	
	private enum State {
		PROGRESS, DIALOGUE, FINISHED, NOTHING;
	}

	private State getState() {
		if (progressId == -1) {
			progressId = getQuestProgress();
		}
		if (progressId == 6)
			return State.FINISHED;
		else if (Dialogues.inDialogue()) {
			return State.DIALOGUE;
		} else if (progressId > -1)
			return State.PROGRESS;
		return State.NOTHING;
	}

	private int getQuestProgress() {
		questStatus = Widgets.getWidgetChild(399, 6, 6);
		if (questStatus != null) {
			if (questStatus.getTextColor() == 16711680) {//not started/red
				return 0;
			} else if (questStatus.getTextColor() == 16776960) {//started/yellow
				return 1;//should be 1 here
			} else if (questStatus.getTextColor() == 901389) {//completed/green
				return 6;
			}
		}
		return -1;
	}

	public String getStatus() {
		switch (progressId) {
		case 0: return "Starting quest";
		case 1: return "Identifying progress...";
		case 2: return "Going into mansion";
		case 3: 
			if (gatherItemsId == 0)
				return "Grabbing the Fish food";
			else if (gatherItemsId == 1)
				return "Getting the Gauge and a key";
			else if (gatherItemsId == 2)
				return "Getting the Rubber tube";
			else if (gatherItemsId == 3)
				if (puzzleStageId >= 0 && puzzleStageId <= 22)
					return "Solving puzzle";
				else
					return "Getting the Oil can";
		case 4: return "Turning in items";
		case 5: return "Waiting for quest ending";
		case 6: return "Quest completed!";
		case 7: return "Teleporting home";
		default: return "Identifying progress...";
		}
	}
	
	public GameObject getObject(Tile tile, String name) {
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
		switch (state) {
		case PROGRESS:
			if (progressId == 0) {//walk to starting spot and talk to Veronica
				if (startArea.contains(getLocalPlayer())) {
					NPCs.closest("Veronica").interact();
					sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
				} else {
					Walking.walk(startArea.getRandomTile());
					sleepUntil(() -> Walking.getDestinationDistance() < 6, 6000);
				}
			} else if (progressId == 2) {//walk into the mansion
				if (getLocalPlayer().getY() <= 3353) {
					if (getLocalPlayer().distance(new Tile(3109, 3353, 0)) < 8) {
						GameObjects.closest("Large door").interact();
						sleepUntil(() -> getLocalPlayer().getY() >= 3354, 6000);
					} else {
						Walking.walk(new Tile(3109 + Calculations.random(-1, 1), 3351, 0));
						sleepUntil(() -> Walking.getDestinationDistance() < 6, 6000);
					}
				} else if (getLocalPlayer().getY() >= 3354 && getLocalPlayer().getZ() == 0) {
					if (!Map.canReach(new Tile(3109, 3359, 0))) {
						getObject(new Tile(3109, 3358, 0), "Door").interactForceRight("Open");//rc
						sleepUntil(() -> Map.canReach(new Tile(3109, 3359, 0)), 6000);
					} else {
						if (getLocalPlayer().distance(GameObjects.closest("Staircase")) < 4) {
							Walking.walk(GameObjects.closest("Staircase"));
							sleepUntil(() -> getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> getLocalPlayer().distance(GameObjects.closest("Staircase")) < 6, 6000);
						} else {
							GameObjects.closest("Staircase").interactForceRight("Climb-up");//rc
							sleepUntil(() -> getLocalPlayer().getZ() == 1, 6000);
						}
					}
					
				} else if (getLocalPlayer().getY() >= 3354 && getLocalPlayer().getZ() == 1) {
					getObject(new Tile(3104, 3362, 1), "Staircase").interact();//lc
					sleepUntil(() -> getLocalPlayer().getZ() == 2, 6000);
				} else if (getLocalPlayer().getY() >= 3354 && getLocalPlayer().getZ() == 2) {//floor with ernest
					if (!Map.canReach(new Tile(3109, 3364, 2))) {
						getObject(new Tile(3108, 3364, 2), "Door").interact("Open");//lc
						sleepUntil(() -> Map.canReach(new Tile(3109, 3364, 2)), 6000);
					} else {
						NPCs.closest("Professor Oddenstein").interact();
						sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
					}
				}
			} else if (progressId == 3) {//look for stuff
				if (getLocalPlayer().getZ() == 2) {
					if (!Map.canReach(new Tile(3107, 3364, 2))) {
						getObject(new Tile(3108, 3364, 2), "Door").interact("Open");//lc
						sleepUntil(() -> Map.canReach(new Tile(3107, 3364, 2)), 6000);
					} else {
						GameObjects.closest("Staircase").interact();
						sleepUntil(() -> getLocalPlayer().getZ() == 1, 6000);
					}
				}
				if (gatherItemsId == 0) {//grabbing fish food
					 if (getLocalPlayer().getZ() == 1) {//fish food
						if (!Map.canReach(new Tile(3116, 3361, 1))) {
							Walking.walk(new Tile(3116, 3364, 1));
							sleepUntil(() -> getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> !getLocalPlayer().isMoving(), 6000);
							getObject(new Tile(3116, 3361, 1), "Door").interact("Open");//lc
							sleepUntil(() -> Map.canReach(new Tile(3116, 3361, 1)), 6000);
						} else {
							GroundItems.closest("Fish food").interactForceRight("Take");
							sleepUntil(() -> Inventory.contains("Fish food"), 6000);
							if (Inventory.contains("Fish food"))
								gatherItemsId = 1;
						}
					}
				} else if (gatherItemsId == 1) {//go get the gauge
					if (Inventory.contains("Pressure gauge"))
						gatherItemsId = 2;
					else if (getLocalPlayer().getZ() == 1) {
						if (!Map.canReach(new Tile(3116, 3362, 1))) {
							getObject(new Tile(3116, 3361, 1), "Door").interact("Open");//lc
							sleepUntil(() -> Map.canReach(new Tile(3116, 3362, 1)), 6000);
						} else {
							getObject(new Tile(3108, 3364, 1), "Staircase").interact();//lc
							sleepUntil(() -> getLocalPlayer().getZ() == 0, 6000);
						}
					} else if (getLocalPlayer().getZ() == 0) {
						if (!Inventory.contains("Poison") && !Inventory.contains("Poisoned fish food") && getLocalPlayer().distance(new Tile(3089, 3337, 0)) > 6) {
							if (!Map.canReach(new Tile(3106, 3369, 0))) {
								getObject(new Tile(3106, 3368, 0), "Door").interactForceRight("Open");//rc
								sleepUntil(() -> Map.canReach(new Tile(3106, 3369, 0)), 6000);
							} else if (!Map.canReach(new Tile(3101, 3371, 0))) {
								getObject(new Tile(3101, 3371, 0), "Door").interact("Open");//lc
								sleepUntil(() -> Map.canReach(new Tile(3101, 3371, 0)), 6000);
							} else if (!Map.canReach(new Tile(3099, 3366, 0))) {
								getObject(new Tile(3099, 3366, 0), "Door").interact("Open");//lc
								sleepUntil(() -> Map.canReach(new Tile(3099, 3366, 0)), 6000);
							} else {
								GroundItems.closest("Poison").interactForceRight("Take");
								sleepUntil(() -> Inventory.contains("Poison"), 6000);
							}
						} else {
							if (mansionArea.contains(getLocalPlayer().getX(), getLocalPlayer().getY())) {
								if (spadeArea.contains(getLocalPlayer()) && !Inventory.contains("Spade")) {
									GroundItems.closest("Spade").interactForceRight("Take");
									sleepUntil(() -> Inventory.contains("Spade"), 6000);
								}
								if (!Map.canReach(new Tile(3099, 3367, 0))) {
									getObject(new Tile(3099, 3366, 0), "Door").interact("Open");//lc
									sleepUntil(() -> Map.canReach(new Tile(3099, 3367, 0)), 6000);
								} else if (!Map.canReach(new Tile(3102, 3371, 0))) {
									getObject(new Tile(3101, 3371, 0), "Door").interact("Open");//lc
									sleepUntil(() -> Map.canReach(new Tile(3102, 3371, 0)), 6000);
								} else if (getLocalPlayer().distance(new Tile(3119, 3356, 0)) < 8 && !Map.canReach(new Tile(3120, 3356, 0))) {
									getObject(new Tile(3120, 3356, 0), "Door").interactForceRight("Open");//rc
									sleepUntil(() -> Map.canReach(new Tile(3120, 3356, 0)), 6000);
								} else if (Map.canReach(new Tile(3123, 3360, 0)) && Inventory.contains("Spade")) {
									getObject(new Tile(3123, 3361, 0), "Door").interact("Open");//lc
									sleepUntil(() -> !mansionArea.contains(getLocalPlayer().getX(), getLocalPlayer().getY()), 6000);
								} else if (Map.canReach(new Tile(3123, 3360, 0)) && !Inventory.contains("Spade")) {
									Walking.walk(new Tile(3122 + Calculations.random(-1, 2), 3358 + Calculations.random(-1, 2), 0));
									sleepUntil(() -> spadeArea.contains(getLocalPlayer()), 6000);
								} else {
									Walking.walk(new Tile(3118, 3356, 0));
									sleepUntil(() -> Walking.getDestinationDistance() < 6, 6000);
								}
							} else {
								if (!Inventory.contains("Poisoned fish food") && Inventory.contains("Fish food") && Inventory.contains("Poison")) {
									if (Calculations.random(0, 10) > 4)
										Inventory.get("Fish food").useOn("Poison");
									else
										Inventory.get("Poison").useOn("Fish food");
									sleepUntil(() -> Inventory.contains("Poisoned fish food"), 6000);
								} else if (getLocalPlayer().distance(new Tile(3089, 3337, 0)) < 6) {
									if (Inventory.contains("Poisoned fish food")) {//getting the gauge here
										Inventory.get("Poisoned fish food").useOn(GameObjects.closest("Fountain"));
										sleepUntil(() -> !Inventory.contains("Poisoned fish food"), 6000);
									} else {
										GameObjects.closest("Fountain").interactForceRight("Search");
										sleepUntil(() -> Dialogues.inDialogue(), 6000);
									}
								} else {
									if (Inventory.contains("Spade") && !Inventory.contains("Key")) {//we getting the key here
										if (getLocalPlayer().distance(new Tile(3086, 3360 , 0)) < 8) {
											Inventory.get("Spade").useOn(GameObjects.closest("Compost heap"));
											sleepUntil(() -> Inventory.contains("Key"), 6000);
										} else {
											Walking.walk(new Tile(3086 + Calculations.random(0, 2), 3360 + Calculations.random(-2, 2), 0));
											sleepUntil(() -> Walking.getDestinationDistance() < 6, 6000);
										}
									} else {
										Walking.walk(new Tile(3089, 3337, 0));
										sleepUntil(() -> Walking.getDestinationDistance() < 6, 6000);
									}
								}
							}
						}
					}
					
				} else if (gatherItemsId == 2) {//go get the Rubber tube
					if (Inventory.contains("Rubber tube"))
						gatherItemsId = 3;
					else if (!mansionArea.contains(getLocalPlayer().getX(), getLocalPlayer().getY())) {
						if (getLocalPlayer().getY() <= 3353) {
							if (getLocalPlayer().distance(new Tile(3109, 3353, 0)) < 8) {
								GameObjects.closest("Large door").interact();
								sleepUntil(() -> getLocalPlayer().getY() >= 3354, 6000);
							} else {
								Walking.walk(new Tile(3109 + Calculations.random(-1, 1), 3351, 0));
								sleepUntil(() -> Walking.getDestinationDistance() < 6, 6000);
							}
						}
					} else {//in the mansion
						GroundItem rubberTube = GroundItems.closest("Rubber tube");
						if (Map.canReach(new Tile(3110, 3367, 0))) {
							rubberTube.interactForceRight("Take");
							sleepUntil(() -> Inventory.contains("Rubber tube"), 6000);
						} else if (!Map.canReach(new Tile(3109, 3359, 0))) {
							getObject(new Tile(3109, 3358, 0), "Door").interactForceRight("Open");//rc
							sleepUntil(() -> Map.canReach(new Tile(3109, 3359, 0)), 6000);
						} else if (!Map.canReach(new Tile(3108, 3368, 0)) && !Inventory.contains("Rubber tube")) {
							if (getLocalPlayer().distance(new Tile(3106, 3367, 0)) > 3) {
								Walking.walk(new Tile(3106 + Calculations.random(-1, 1), 3367 + Calculations.random(-1, 1), 0));
								sleepUntil(() -> getLocalPlayer().isMoving(), 6000);
								sleepUntil(() -> !getLocalPlayer().isMoving(), 6000);
							} else {
								if (Walking.getRunEnergy() >= 2 && !Walking.isRunEnabled()) {
									Walking.toggleRun();
								} else if (rubberTube != null && rubberTube.exists()) {
									getObject(new Tile(3107, 3367, 0), "Door").interactForceRight("Open");//rc
									sleepUntil(() -> Map.canReach(new Tile(3108, 3368, 0)), 6000);
								}
							}
						}
					}
				} else if (gatherItemsId == 3) {//go get the Oil can
					if (mansionArea.contains(getLocalPlayer().getX(), getLocalPlayer().getY())) {
						if (getLocalPlayer().getY() < 9000) {
							if (getLocalPlayer().getX() >= 3097) {
								if (Map.canReach(new Tile(3108, 3367, 0))) {
									getObject(new Tile(3107, 3367, 0), "Door").interactForceRight("Open");//rc
									sleepUntil(() -> !Map.canReach(new Tile(3108, 3367, 0)), 6000);
								} else if (!Map.canReach(new Tile(3106, 3369, 0))) {
									getObject(new Tile(3106, 3368, 0), "Door").interact("Open");//lc
									sleepUntil(() -> Map.canReach(new Tile(3106, 3369, 0)), 6000);
								} else if (!Map.canReach(new Tile(3103, 3363, 0))) {
									getObject(new Tile(3103, 3364, 0), "Door").interact("Open");//lc
									sleepUntil(() -> Map.canReach(new Tile(3103, 3363, 0)), 6000);
								} else if (!Map.canReach(new Tile(3103, 3363, 0))) {
									getObject(new Tile(3103, 3364, 0), "Door").interact("Open");//lc
									sleepUntil(() -> Map.canReach(new Tile(3103, 3363, 0)), 6000);
								} else if (Map.canReach(GameObjects.closest("Bookcase"))) {
									GameObjects.closest("Bookcase").interactForceRight("Search");
									sleepUntil(() -> getLocalPlayer().getX() < 3097, 6000);
								}
							} else {
								GameObjects.closest("Ladder").interactForceRight("Climb-down");
								sleepUntil(() -> getLocalPlayer().getY() > 9000, 6000);
							}
						}
					} else {//basement puzzle
						boolean pulledLever = false;
						if (puzzleStageId == 0) {
							pulledLever = GameObjects.closest("Lever B").interact();
							sleepUntil(() -> !getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> getLocalPlayer().isAnimating(), 6000);
							sleepUntil(() -> !getLocalPlayer().isAnimating(), 6000);
							if (pulledLever)
								puzzleStageId = 1;
						} else if (puzzleStageId == 1) {
							Walking.walk(new Tile(3109, 9748, 0));
							sleepUntil(() -> getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> !getLocalPlayer().isMoving(), 6000);
							pulledLever = GameObjects.closest("Lever A").interact();
							sleepUntil(() -> !getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> getLocalPlayer().isAnimating(), 6000);
							sleepUntil(() -> !getLocalPlayer().isAnimating(), 6000);
							if (pulledLever)
								puzzleStageId = 2;
						} else if (puzzleStageId == 2) {
							pulledLever = GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3108, 9758, 0)).interactForceRight("Open");//door 1
							sleepUntil(() -> getLocalPlayer().getY() > 9758, 12000);
							if (pulledLever)
								puzzleStageId = 3;
						} else if (puzzleStageId == 3) {
							pulledLever = GameObjects.closest("Lever D").interact();
							sleepUntil(() -> !getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> getLocalPlayer().isAnimating(), 6000);
							sleepUntil(() -> !getLocalPlayer().isAnimating(), 6000);
							if (pulledLever)
								puzzleStageId = 4;
						} else if (puzzleStageId == 4) {
							pulledLever = GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3105, 9760, 0)).interactForceRight("Open");//door 2
							sleepUntil(() -> getLocalPlayer().getX() < 3105, 6000);
							if (pulledLever)
								puzzleStageId = 5;
						} else if (puzzleStageId == 5) {
							pulledLever = GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3102, 9758, 0)).interactForceRight("Open");//door 3
							sleepUntil(() -> getLocalPlayer().getY() < 9758, 6000);
							puzzleStageId = 6;
						} else if (puzzleStageId == 6) {
							Walking.walk(new Tile(3109, 9748, 0));
							sleepUntil(() -> getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> !getLocalPlayer().isMoving(), 6000);
							pulledLever = GameObjects.closest("Lever A").interact();
							sleepUntil(() -> !getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> getLocalPlayer().isAnimating(), 6000);
							sleepUntil(() -> !getLocalPlayer().isAnimating(), 6000);
							if (pulledLever)
								puzzleStageId = 7;
						} else if (puzzleStageId == 7) {
							Walking.walk(new Tile(3116, 9753, 0));
							sleepUntil(() -> getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> !getLocalPlayer().isMoving(), 6000);
							pulledLever = GameObjects.closest("Lever B").interact();
							sleepUntil(() -> !getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> getLocalPlayer().isAnimating(), 6000);
							sleepUntil(() -> !getLocalPlayer().isAnimating(), 6000);
							if (pulledLever)
								puzzleStageId = 8;
						} else if (puzzleStageId == 8) {
							pulledLever = GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3102, 9758, 0)).interactForceRight("Open");//door 3
							sleepUntil(() -> getLocalPlayer().getY() > 9758, 12000);
							if (pulledLever)
								puzzleStageId = 9;
						} else if (puzzleStageId == 9) {
							pulledLever = GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3100, 9760, 0)).interactForceRight("Open");//door 4
							sleepUntil(() -> getLocalPlayer().getX() < 3100, 6000);
							if (pulledLever)
								puzzleStageId = 10;
						} else if (puzzleStageId == 10) {
							pulledLever = GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3097, 9763, 0)).interactForceRight("Open");//door 5
							sleepUntil(() -> getLocalPlayer().getY() > 9763, 6000);
							if (pulledLever)
								puzzleStageId = 11;
						} else if (puzzleStageId == 11) {
							pulledLever = GameObjects.closest("Lever F").interact();
							sleepUntil(() -> !getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> getLocalPlayer().isAnimating(), 6000);
							sleepUntil(() -> !getLocalPlayer().isAnimating(), 6000);
							if (pulledLever)
								puzzleStageId = 12;
						} else if (puzzleStageId == 12) {
							pulledLever = GameObjects.closest("Lever E").interact();
							sleepUntil(() -> !getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> getLocalPlayer().isAnimating(), 6000);
							sleepUntil(() -> !getLocalPlayer().isAnimating(), 6000);
							if (pulledLever)
								puzzleStageId = 13;
						} else if (puzzleStageId == 13) {
							pulledLever = GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3100, 9765, 0)).interactForceRight("Open");//door 6
							sleepUntil(() -> getLocalPlayer().getX() > 3100, 6000);
							if (pulledLever)
								puzzleStageId = 14;
						} else if (puzzleStageId == 14) {
							pulledLever = GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3105, 9765, 0)).interactForceRight("Open");//door 7
							sleepUntil(() -> getLocalPlayer().getX() > 3105, 6000);
							if (pulledLever)
								puzzleStageId = 15;
						} else if (puzzleStageId == 15) {
							pulledLever = GameObjects.closest("Lever C").interact();
							sleepUntil(() -> !getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> getLocalPlayer().isAnimating(), 6000);
							sleepUntil(() -> !getLocalPlayer().isAnimating(), 6000);
							if (pulledLever)
								puzzleStageId = 16;
						} else if (puzzleStageId == 16) {
							pulledLever = GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3105, 9765, 0)).interactForceRight("Open");//door 7
							sleepUntil(() -> getLocalPlayer().getX() < 3105, 6000);
							if (pulledLever)
								puzzleStageId = 17;
						} else if (puzzleStageId == 17) {
							pulledLever = GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3100, 9765, 0)).interactForceRight("Open");//door 6
							sleepUntil(() -> getLocalPlayer().getX() < 3100, 6000);
							if (pulledLever)
								puzzleStageId = 18;
						} else if (puzzleStageId == 18) {
							pulledLever = GameObjects.closest("Lever E").interactForceRight("Pull");
							sleepUntil(() -> !getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> getLocalPlayer().isAnimating(), 6000);
							sleepUntil(() -> !getLocalPlayer().isAnimating(), 6000);
							if (pulledLever)
								puzzleStageId = 19;
						} else if (puzzleStageId == 19) {
							pulledLever = GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3100, 9765, 0)).interactForceRight("Open");//door 6
							sleepUntil(() -> getLocalPlayer().getX() > 3100, 6000);
							if (pulledLever)
								puzzleStageId = 20;
						} else if (puzzleStageId == 20) {
							pulledLever = GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3102, 9763, 0)).interactForceRight("Open");//door 8
							sleepUntil(() -> getLocalPlayer().getY() < 9763, 6000);
							if (pulledLever)
								puzzleStageId = 21;
						} else if (puzzleStageId == 21) {
							pulledLever = GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3102, 9758, 0)).interactForceRight("Open");//door 3
							sleepUntil(() -> getLocalPlayer().getY() < 9758, 6000);
							if (pulledLever)
								puzzleStageId = 22;
						} else if (puzzleStageId == 22) {
							pulledLever = GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3100, 9755, 0)).interactForceRight("Open");//door 9
							sleepUntil(() -> getLocalPlayer().getX() < 3100, 6000);
							if (pulledLever)
								puzzleStageId = 23;
						} else if (puzzleStageId == 23) {
							pulledLever = GroundItems.closest("Oil can").interact();
							sleepUntil(() -> Inventory.contains("Oil can"), 6000);
							puzzleStageId = 24;
						} else if (puzzleStageId == 24) {
							pulledLever = GameObjects.getGameObjects().getTopObjectOnTile(new Tile(3100, 9755, 0)).interactForceRight("Open");//door 9
							sleepUntil(() -> getLocalPlayer().getX() > 3100, 6000);
							if (pulledLever)
								puzzleStageId = 25;
						} else if (puzzleStageId == 25) {
							Walking.walk(new Tile(3116 + Calculations.random(-2, 2), 9755 + Calculations.random(-2, 0), 0));
							sleepUntil(() -> getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> getLocalPlayer().distance(GameObjects.closest("Ladder")) < 6, 6000);
							pulledLever = GameObjects.closest("Ladder").interact();//ladder
							sleepUntil(() -> getLocalPlayer().getY() < 4000, 6000);
							progressId = 4;
						}
					}
				}
			} else if (progressId == 4) {//give items to stein
				if (getLocalPlayer().getZ() == 0) {
					if (Map.canReach(new Tile(3096, 3359, 0)) && getLocalPlayer().getX() < 3095) {
						GameObjects.closest("Lever").interactForceRight("Pull");
						sleepUntil(() -> Map.canReach(new Tile(3098, 3359, 0)), 6000);
					} else if (!Map.canReach(new Tile(3103, 3365, 0))) {
						getObject(new Tile(3103, 3364, 0), "Door").interactForceRight("Open");//rc
						sleepUntil(() -> !Map.canReach(new Tile(3103, 3365, 0)), 6000);
					} else if (!Map.canReach(new Tile(3106, 3367, 0))) {
						if (getLocalPlayer().distance(new Tile(3106, 3368, 0)) < 4) {
							Walking.walk(new Tile(3106 + Calculations.random(-2, 2), 3368 + Calculations.random(-1, 1), 0));
							sleepUntil(() -> getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> getLocalPlayer().distance(new Tile(3106, 3368, 0)) < 6, 6000);
						} else {
							getObject(new Tile(3106, 3368, 0), "Door").interactForceRight("Open");//rc
							sleepUntil(() -> !Map.canReach(new Tile(3106, 3367, 0)), 6000);
						}
					} else {
						GameObjects.closest("Staircase").interactForceRight("Climb-up");//rc
						sleepUntil(() -> getLocalPlayer().getZ() == 1, 6000);
					}
				} else if (getLocalPlayer().getZ() == 1) {
					getObject(new Tile(3104, 3362, 1), "Staircase").interact();//lc
					sleepUntil(() -> getLocalPlayer().getZ() == 2, 6000);
				} else if (getLocalPlayer().getZ() == 2) {//floor with ernest
					if (!Map.canReach(new Tile(3107, 3364, 2))) {
						getObject(new Tile(3108, 3364, 2), "Door").interact("Open");//lc
						sleepUntil(() -> Map.canReach(new Tile(3107, 3364, 2)), 6000);
					} else if (Map.canReach(NPCs.closest("Professor Oddenstein"))) {
						NPCs.closest("Professor Oddenstein").interact();
						sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
						if (Dialogues.canContinue())
							progressId = 5;
					}
				}
			} else if (progressId == 5) {//cutscene
				if (Widgets.getWidget(277) != null && Widgets.getWidget(277).isVisible()) {
					Widgets.getWidget(277).close();
					sleepUntil(() -> Widgets.getWidget(277) == null, Calculations.random(3000, 5000));
					if (taskScript)
						progressId = 7;
					else
						progressId = 6;
				}
			} else if (progressId == 7) {
				if (getLocalPlayer().distance(new Tile(3221, 3217, 0)) < 10) {
					progressId = 6;
				} else if (!Tabs.isOpen(Tab.MAGIC)) {
					Tabs.open(Tab.MAGIC);
					sleepUntil(() -> Tabs.isOpen(Tab.MAGIC), Calculations.random(3000, 5000));
				} else if (!getLocalPlayer().isAnimating()) {
					Magic.castSpell(Normal.HOME_TELEPORT);
					sleepUntil(() -> getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					sleepUntil(() -> !getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
				}
			}
			break;
		case DIALOGUE:
			if (Dialogues.getOptions() != null && Dialogues.getOptions().length > 0) {
				List<String> options = Arrays.asList(Dialogues.getOptions());
				if (options.size() == 2) {
					if (options.contains("Aha, sounds like a quest. I'll help.")) {
						Dialogues.chooseOption(1);
					} else if (progressId == 2 && options.contains("Change him back this instant!")) {
						Dialogues.chooseOption(2);
						progressId = 3;//time to go look for stuff
						gatherItemsId = 0;
					}
				} else if (options.size() == 3) {
					if (progressId == 2 && options.contains("I'm looking for a guy called Ernest.")) {
						Dialogues.chooseOption(1);
					}
				} else if (options.size() == 4) {

				}
			} else {
				if (Dialogues.getNPCDialogue() != null) {
					if (progressId == 0 && Dialogues.getNPCDialogue()
							.contains("Thank you, thank you. I'm very grateful."))
						progressId = 2;//go into the mansion

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
