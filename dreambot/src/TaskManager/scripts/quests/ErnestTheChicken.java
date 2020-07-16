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
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.tabs.Tab;
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
		else if (engine.getDialogues().inDialogue()) {
			return State.DIALOGUE;
		} else if (progressId > -1)
			return State.PROGRESS;
		return State.NOTHING;
	}

	private int getQuestProgress() {
		questStatus = engine.getWidgets().getWidgetChild(399, 6, 6);
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
		switch (state) {
		case PROGRESS:
			if (progressId == 0) {//walk to starting spot and talk to Veronica
				if (startArea.contains(engine.getLocalPlayer())) {
					engine.getNpcs().closest("Veronica").interact();
					sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
				} else {
					engine.getWalking().walk(startArea.getRandomTile());
					sleepUntil(() -> engine.getWalking().getDestinationDistance() < 6, 6000);
				}
			} else if (progressId == 2) {//walk into the mansion
				if (engine.getLocalPlayer().getY() <= 3353) {
					if (engine.getLocalPlayer().distance(new Tile(3109, 3353, 0)) < 8) {
						engine.getGameObjects().closest("Large door").interact();
						sleepUntil(() -> engine.getLocalPlayer().getY() >= 3354, 6000);
					} else {
						engine.getWalking().walk(new Tile(3109 + Calculations.random(-1, 1), 3351, 0));
						sleepUntil(() -> engine.getWalking().getDestinationDistance() < 6, 6000);
					}
				} else if (engine.getLocalPlayer().getY() >= 3354 && engine.getLocalPlayer().getZ() == 0) {
					if (!engine.getMap().canReach(new Tile(3109, 3359, 0))) {
						getObject(new Tile(3109, 3358, 0), "Door").interactForceRight("Open");//rc
						sleepUntil(() -> engine.getMap().canReach(new Tile(3109, 3359, 0)), 6000);
					} else {
						if (engine.getLocalPlayer().distance(engine.getGameObjects().closest("Staircase")) < 4) {
							engine.getWalking().walk(engine.getGameObjects().closest("Staircase"));
							sleepUntil(() -> engine.getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> engine.getLocalPlayer().distance(engine.getGameObjects().closest("Staircase")) < 6, 6000);
						} else {
							engine.getGameObjects().closest("Staircase").interactForceRight("Climb-up");//rc
							sleepUntil(() -> engine.getLocalPlayer().getZ() == 1, 6000);
						}
					}
					
				} else if (engine.getLocalPlayer().getY() >= 3354 && engine.getLocalPlayer().getZ() == 1) {
					getObject(new Tile(3104, 3362, 1), "Staircase").interact();//lc
					sleepUntil(() -> engine.getLocalPlayer().getZ() == 2, 6000);
				} else if (engine.getLocalPlayer().getY() >= 3354 && engine.getLocalPlayer().getZ() == 2) {//floor with ernest
					if (!engine.getMap().canReach(new Tile(3109, 3364, 2))) {
						getObject(new Tile(3108, 3364, 2), "Door").interact("Open");//lc
						sleepUntil(() -> engine.getMap().canReach(new Tile(3109, 3364, 2)), 6000);
					} else {
						engine.getNpcs().closest("Professor Oddenstein").interact();
						sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
					}
				}
			} else if (progressId == 3) {//look for stuff
				if (engine.getLocalPlayer().getZ() == 2) {
					if (!engine.getMap().canReach(new Tile(3107, 3364, 2))) {
						getObject(new Tile(3108, 3364, 2), "Door").interact("Open");//lc
						sleepUntil(() -> engine.getMap().canReach(new Tile(3107, 3364, 2)), 6000);
					} else {
						engine.getGameObjects().closest("Staircase").interact();
						sleepUntil(() -> engine.getLocalPlayer().getZ() == 1, 6000);
					}
				}
				if (gatherItemsId == 0) {//grabbing fish food
					 if (engine.getLocalPlayer().getZ() == 1) {//fish food
						if (!engine.getMap().canReach(new Tile(3116, 3361, 1))) {
							engine.getWalking().walk(new Tile(3116, 3364, 1));
							sleepUntil(() -> engine.getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> !engine.getLocalPlayer().isMoving(), 6000);
							getObject(new Tile(3116, 3361, 1), "Door").interact("Open");//lc
							sleepUntil(() -> engine.getMap().canReach(new Tile(3116, 3361, 1)), 6000);
						} else {
							engine.getGroundItems().closest("Fish food").interactForceRight("Take");
							sleepUntil(() -> engine.getInventory().contains("Fish food"), 6000);
							if (engine.getInventory().contains("Fish food"))
								gatherItemsId = 1;
						}
					}
				} else if (gatherItemsId == 1) {//go get the gauge
					if (engine.getInventory().contains("Pressure gauge"))
						gatherItemsId = 2;
					else if (engine.getLocalPlayer().getZ() == 1) {
						if (!engine.getMap().canReach(new Tile(3116, 3362, 1))) {
							getObject(new Tile(3116, 3361, 1), "Door").interact("Open");//lc
							sleepUntil(() -> engine.getMap().canReach(new Tile(3116, 3362, 1)), 6000);
						} else {
							getObject(new Tile(3108, 3364, 1), "Staircase").interact();//lc
							sleepUntil(() -> engine.getLocalPlayer().getZ() == 0, 6000);
						}
					} else if (engine.getLocalPlayer().getZ() == 0) {
						if (!engine.getInventory().contains("Poison") && !engine.getInventory().contains("Poisoned fish food") && engine.getLocalPlayer().distance(new Tile(3089, 3337, 0)) > 6) {
							if (!engine.getMap().canReach(new Tile(3106, 3369, 0))) {
								getObject(new Tile(3106, 3368, 0), "Door").interactForceRight("Open");//rc
								sleepUntil(() -> engine.getMap().canReach(new Tile(3106, 3369, 0)), 6000);
							} else if (!engine.getMap().canReach(new Tile(3101, 3371, 0))) {
								getObject(new Tile(3101, 3371, 0), "Door").interact("Open");//lc
								sleepUntil(() -> engine.getMap().canReach(new Tile(3101, 3371, 0)), 6000);
							} else if (!engine.getMap().canReach(new Tile(3099, 3366, 0))) {
								getObject(new Tile(3099, 3366, 0), "Door").interact("Open");//lc
								sleepUntil(() -> engine.getMap().canReach(new Tile(3099, 3366, 0)), 6000);
							} else {
								engine.getGroundItems().closest("Poison").interactForceRight("Take");
								sleepUntil(() -> engine.getInventory().contains("Poison"), 6000);
							}
						} else {
							if (mansionArea.contains(engine.getLocalPlayer().getX(), engine.getLocalPlayer().getY())) {
								if (spadeArea.contains(engine.getLocalPlayer()) && !engine.getInventory().contains("Spade")) {
									engine.getGroundItems().closest("Spade").interactForceRight("Take");
									sleepUntil(() -> engine.getInventory().contains("Spade"), 6000);
								}
								if (!engine.getMap().canReach(new Tile(3099, 3367, 0))) {
									getObject(new Tile(3099, 3366, 0), "Door").interact("Open");//lc
									sleepUntil(() -> engine.getMap().canReach(new Tile(3099, 3367, 0)), 6000);
								} else if (!engine.getMap().canReach(new Tile(3102, 3371, 0))) {
									getObject(new Tile(3101, 3371, 0), "Door").interact("Open");//lc
									sleepUntil(() -> engine.getMap().canReach(new Tile(3102, 3371, 0)), 6000);
								} else if (engine.getLocalPlayer().distance(new Tile(3119, 3356, 0)) < 8 && !engine.getMap().canReach(new Tile(3120, 3356, 0))) {
									getObject(new Tile(3120, 3356, 0), "Door").interactForceRight("Open");//rc
									sleepUntil(() -> engine.getMap().canReach(new Tile(3120, 3356, 0)), 6000);
								} else if (engine.getMap().canReach(new Tile(3123, 3360, 0)) && engine.getInventory().contains("Spade")) {
									getObject(new Tile(3123, 3361, 0), "Door").interact("Open");//lc
									sleepUntil(() -> !mansionArea.contains(engine.getLocalPlayer().getX(), engine.getLocalPlayer().getY()), 6000);
								} else if (engine.getMap().canReach(new Tile(3123, 3360, 0)) && !engine.getInventory().contains("Spade")) {
									engine.getWalking().walk(new Tile(3122 + Calculations.random(-1, 2), 3358 + Calculations.random(-1, 2), 0));
									sleepUntil(() -> spadeArea.contains(engine.getLocalPlayer()), 6000);
								} else {
									engine.getWalking().walk(new Tile(3118, 3356, 0));
									sleepUntil(() -> engine.getWalking().getDestinationDistance() < 6, 6000);
								}
							} else {
								if (!engine.getInventory().contains("Poisoned fish food") && engine.getInventory().contains("Fish food") && engine.getInventory().contains("Poison")) {
									if (Calculations.random(0, 10) > 4)
										engine.getInventory().get("Fish food").useOn("Poison");
									else
										engine.getInventory().get("Poison").useOn("Fish food");
									sleepUntil(() -> engine.getInventory().contains("Poisoned fish food"), 6000);
								} else if (engine.getLocalPlayer().distance(new Tile(3089, 3337, 0)) < 6) {
									if (engine.getInventory().contains("Poisoned fish food")) {//getting the gauge here
										engine.getInventory().get("Poisoned fish food").useOn(engine.getGameObjects().closest("Fountain"));
										sleepUntil(() -> !engine.getInventory().contains("Poisoned fish food"), 6000);
									} else {
										engine.getGameObjects().closest("Fountain").interactForceRight("Search");
										sleepUntil(() -> engine.getDialogues().inDialogue(), 6000);
									}
								} else {
									if (engine.getInventory().contains("Spade") && !engine.getInventory().contains("Key")) {//we getting the key here
										if (engine.getLocalPlayer().distance(new Tile(3086, 3360 , 0)) < 8) {
											engine.getInventory().get("Spade").useOn(engine.getGameObjects().closest("Compost heap"));
											sleepUntil(() -> engine.getInventory().contains("Key"), 6000);
										} else {
											engine.getWalking().walk(new Tile(3086 + Calculations.random(0, 2), 3360 + Calculations.random(-2, 2), 0));
											sleepUntil(() -> engine.getWalking().getDestinationDistance() < 6, 6000);
										}
									} else {
										engine.getWalking().walk(new Tile(3089, 3337, 0));
										sleepUntil(() -> engine.getWalking().getDestinationDistance() < 6, 6000);
									}
								}
							}
						}
					}
					
				} else if (gatherItemsId == 2) {//go get the Rubber tube
					if (engine.getInventory().contains("Rubber tube"))
						gatherItemsId = 3;
					else if (!mansionArea.contains(engine.getLocalPlayer().getX(), engine.getLocalPlayer().getY())) {
						if (engine.getLocalPlayer().getY() <= 3353) {
							if (engine.getLocalPlayer().distance(new Tile(3109, 3353, 0)) < 8) {
								engine.getGameObjects().closest("Large door").interact();
								sleepUntil(() -> engine.getLocalPlayer().getY() >= 3354, 6000);
							} else {
								engine.getWalking().walk(new Tile(3109 + Calculations.random(-1, 1), 3351, 0));
								sleepUntil(() -> engine.getWalking().getDestinationDistance() < 6, 6000);
							}
						}
					} else {//in the mansion
						GroundItem rubberTube = engine.getGroundItems().closest("Rubber tube");
						if (engine.getMap().canReach(new Tile(3110, 3367, 0))) {
							rubberTube.interactForceRight("Take");
							sleepUntil(() -> engine.getInventory().contains("Rubber tube"), 6000);
						} else if (!engine.getMap().canReach(new Tile(3109, 3359, 0))) {
							getObject(new Tile(3109, 3358, 0), "Door").interactForceRight("Open");//rc
							sleepUntil(() -> engine.getMap().canReach(new Tile(3109, 3359, 0)), 6000);
						} else if (!engine.getMap().canReach(new Tile(3108, 3368, 0)) && !engine.getInventory().contains("Rubber tube")) {
							if (engine.getLocalPlayer().distance(new Tile(3106, 3367, 0)) > 3) {
								engine.getWalking().walk(new Tile(3106 + Calculations.random(-1, 1), 3367 + Calculations.random(-1, 1), 0));
								sleepUntil(() -> engine.getLocalPlayer().isMoving(), 6000);
								sleepUntil(() -> !engine.getLocalPlayer().isMoving(), 6000);
							} else {
								if (engine.getWalking().getRunEnergy() >= 2 && !engine.getWalking().isRunEnabled()) {
									engine.getWalking().toggleRun();
								} else if (rubberTube != null && rubberTube.exists()) {
									getObject(new Tile(3107, 3367, 0), "Door").interactForceRight("Open");//rc
									sleepUntil(() -> engine.getMap().canReach(new Tile(3108, 3368, 0)), 6000);
								}
							}
						}
					}
				} else if (gatherItemsId == 3) {//go get the Oil can
					if (mansionArea.contains(engine.getLocalPlayer().getX(), engine.getLocalPlayer().getY())) {
						if (engine.getLocalPlayer().getY() < 9000) {
							if (engine.getLocalPlayer().getX() >= 3097) {
								if (engine.getMap().canReach(new Tile(3108, 3367, 0))) {
									getObject(new Tile(3107, 3367, 0), "Door").interactForceRight("Open");//rc
									sleepUntil(() -> !engine.getMap().canReach(new Tile(3108, 3367, 0)), 6000);
								} else if (!engine.getMap().canReach(new Tile(3106, 3369, 0))) {
									getObject(new Tile(3106, 3368, 0), "Door").interact("Open");//lc
									sleepUntil(() -> engine.getMap().canReach(new Tile(3106, 3369, 0)), 6000);
								} else if (!engine.getMap().canReach(new Tile(3103, 3363, 0))) {
									getObject(new Tile(3103, 3364, 0), "Door").interact("Open");//lc
									sleepUntil(() -> engine.getMap().canReach(new Tile(3103, 3363, 0)), 6000);
								} else if (!engine.getMap().canReach(new Tile(3103, 3363, 0))) {
									getObject(new Tile(3103, 3364, 0), "Door").interact("Open");//lc
									sleepUntil(() -> engine.getMap().canReach(new Tile(3103, 3363, 0)), 6000);
								} else if (engine.getMap().canReach(engine.getGameObjects().closest("Bookcase"))) {
									engine.getGameObjects().closest("Bookcase").interactForceRight("Search");
									sleepUntil(() -> engine.getLocalPlayer().getX() < 3097, 6000);
								}
							} else {
								engine.getGameObjects().closest("Ladder").interactForceRight("Climb-down");
								sleepUntil(() -> engine.getLocalPlayer().getY() > 9000, 6000);
							}
						}
					} else {//basement puzzle
						boolean pulledLever = false;
						if (puzzleStageId == 0) {
							pulledLever = engine.getGameObjects().closest("Lever B").interact();
							sleepUntil(() -> !engine.getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> engine.getLocalPlayer().isAnimating(), 6000);
							sleepUntil(() -> !engine.getLocalPlayer().isAnimating(), 6000);
							if (pulledLever)
								puzzleStageId = 1;
						} else if (puzzleStageId == 1) {
							engine.getWalking().walk(new Tile(3109, 9748, 0));
							sleepUntil(() -> engine.getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> !engine.getLocalPlayer().isMoving(), 6000);
							pulledLever = engine.getGameObjects().closest("Lever A").interact();
							sleepUntil(() -> !engine.getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> engine.getLocalPlayer().isAnimating(), 6000);
							sleepUntil(() -> !engine.getLocalPlayer().isAnimating(), 6000);
							if (pulledLever)
								puzzleStageId = 2;
						} else if (puzzleStageId == 2) {
							pulledLever = engine.getGameObjects().getTopObjectOnTile(new Tile(3108, 9758, 0)).interactForceRight("Open");//door 1
							sleepUntil(() -> engine.getLocalPlayer().getY() > 9758, 12000);
							if (pulledLever)
								puzzleStageId = 3;
						} else if (puzzleStageId == 3) {
							pulledLever = engine.getGameObjects().closest("Lever D").interact();
							sleepUntil(() -> !engine.getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> engine.getLocalPlayer().isAnimating(), 6000);
							sleepUntil(() -> !engine.getLocalPlayer().isAnimating(), 6000);
							if (pulledLever)
								puzzleStageId = 4;
						} else if (puzzleStageId == 4) {
							pulledLever = engine.getGameObjects().getTopObjectOnTile(new Tile(3105, 9760, 0)).interactForceRight("Open");//door 2
							sleepUntil(() -> engine.getLocalPlayer().getX() < 3105, 6000);
							if (pulledLever)
								puzzleStageId = 5;
						} else if (puzzleStageId == 5) {
							pulledLever = engine.getGameObjects().getTopObjectOnTile(new Tile(3102, 9758, 0)).interactForceRight("Open");//door 3
							sleepUntil(() -> engine.getLocalPlayer().getY() < 9758, 6000);
							puzzleStageId = 6;
						} else if (puzzleStageId == 6) {
							engine.getWalking().walk(new Tile(3109, 9748, 0));
							sleepUntil(() -> engine.getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> !engine.getLocalPlayer().isMoving(), 6000);
							pulledLever = engine.getGameObjects().closest("Lever A").interact();
							sleepUntil(() -> !engine.getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> engine.getLocalPlayer().isAnimating(), 6000);
							sleepUntil(() -> !engine.getLocalPlayer().isAnimating(), 6000);
							if (pulledLever)
								puzzleStageId = 7;
						} else if (puzzleStageId == 7) {
							engine.getWalking().walk(new Tile(3116, 9753, 0));
							sleepUntil(() -> engine.getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> !engine.getLocalPlayer().isMoving(), 6000);
							pulledLever = engine.getGameObjects().closest("Lever B").interact();
							sleepUntil(() -> !engine.getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> engine.getLocalPlayer().isAnimating(), 6000);
							sleepUntil(() -> !engine.getLocalPlayer().isAnimating(), 6000);
							if (pulledLever)
								puzzleStageId = 8;
						} else if (puzzleStageId == 8) {
							pulledLever = engine.getGameObjects().getTopObjectOnTile(new Tile(3102, 9758, 0)).interactForceRight("Open");//door 3
							sleepUntil(() -> engine.getLocalPlayer().getY() > 9758, 12000);
							if (pulledLever)
								puzzleStageId = 9;
						} else if (puzzleStageId == 9) {
							pulledLever = engine.getGameObjects().getTopObjectOnTile(new Tile(3100, 9760, 0)).interactForceRight("Open");//door 4
							sleepUntil(() -> engine.getLocalPlayer().getX() < 3100, 6000);
							if (pulledLever)
								puzzleStageId = 10;
						} else if (puzzleStageId == 10) {
							pulledLever = engine.getGameObjects().getTopObjectOnTile(new Tile(3097, 9763, 0)).interactForceRight("Open");//door 5
							sleepUntil(() -> engine.getLocalPlayer().getY() > 9763, 6000);
							if (pulledLever)
								puzzleStageId = 11;
						} else if (puzzleStageId == 11) {
							pulledLever = engine.getGameObjects().closest("Lever F").interact();
							sleepUntil(() -> !engine.getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> engine.getLocalPlayer().isAnimating(), 6000);
							sleepUntil(() -> !engine.getLocalPlayer().isAnimating(), 6000);
							if (pulledLever)
								puzzleStageId = 12;
						} else if (puzzleStageId == 12) {
							pulledLever = engine.getGameObjects().closest("Lever E").interact();
							sleepUntil(() -> !engine.getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> engine.getLocalPlayer().isAnimating(), 6000);
							sleepUntil(() -> !engine.getLocalPlayer().isAnimating(), 6000);
							if (pulledLever)
								puzzleStageId = 13;
						} else if (puzzleStageId == 13) {
							pulledLever = engine.getGameObjects().getTopObjectOnTile(new Tile(3100, 9765, 0)).interactForceRight("Open");//door 6
							sleepUntil(() -> engine.getLocalPlayer().getX() > 3100, 6000);
							if (pulledLever)
								puzzleStageId = 14;
						} else if (puzzleStageId == 14) {
							pulledLever = engine.getGameObjects().getTopObjectOnTile(new Tile(3105, 9765, 0)).interactForceRight("Open");//door 7
							sleepUntil(() -> engine.getLocalPlayer().getX() > 3105, 6000);
							if (pulledLever)
								puzzleStageId = 15;
						} else if (puzzleStageId == 15) {
							pulledLever = engine.getGameObjects().closest("Lever C").interact();
							sleepUntil(() -> !engine.getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> engine.getLocalPlayer().isAnimating(), 6000);
							sleepUntil(() -> !engine.getLocalPlayer().isAnimating(), 6000);
							if (pulledLever)
								puzzleStageId = 16;
						} else if (puzzleStageId == 16) {
							pulledLever = engine.getGameObjects().getTopObjectOnTile(new Tile(3105, 9765, 0)).interactForceRight("Open");//door 7
							sleepUntil(() -> engine.getLocalPlayer().getX() < 3105, 6000);
							if (pulledLever)
								puzzleStageId = 17;
						} else if (puzzleStageId == 17) {
							pulledLever = engine.getGameObjects().getTopObjectOnTile(new Tile(3100, 9765, 0)).interactForceRight("Open");//door 6
							sleepUntil(() -> engine.getLocalPlayer().getX() < 3100, 6000);
							if (pulledLever)
								puzzleStageId = 18;
						} else if (puzzleStageId == 18) {
							pulledLever = engine.getGameObjects().closest("Lever E").interactForceRight("Pull");
							sleepUntil(() -> !engine.getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> engine.getLocalPlayer().isAnimating(), 6000);
							sleepUntil(() -> !engine.getLocalPlayer().isAnimating(), 6000);
							if (pulledLever)
								puzzleStageId = 19;
						} else if (puzzleStageId == 19) {
							pulledLever = engine.getGameObjects().getTopObjectOnTile(new Tile(3100, 9765, 0)).interactForceRight("Open");//door 6
							sleepUntil(() -> engine.getLocalPlayer().getX() > 3100, 6000);
							if (pulledLever)
								puzzleStageId = 20;
						} else if (puzzleStageId == 20) {
							pulledLever = engine.getGameObjects().getTopObjectOnTile(new Tile(3102, 9763, 0)).interactForceRight("Open");//door 8
							sleepUntil(() -> engine.getLocalPlayer().getY() < 9763, 6000);
							if (pulledLever)
								puzzleStageId = 21;
						} else if (puzzleStageId == 21) {
							pulledLever = engine.getGameObjects().getTopObjectOnTile(new Tile(3102, 9758, 0)).interactForceRight("Open");//door 3
							sleepUntil(() -> engine.getLocalPlayer().getY() < 9758, 6000);
							if (pulledLever)
								puzzleStageId = 22;
						} else if (puzzleStageId == 22) {
							pulledLever = engine.getGameObjects().getTopObjectOnTile(new Tile(3100, 9755, 0)).interactForceRight("Open");//door 9
							sleepUntil(() -> engine.getLocalPlayer().getX() < 3100, 6000);
							if (pulledLever)
								puzzleStageId = 23;
						} else if (puzzleStageId == 23) {
							pulledLever = engine.getGroundItems().closest("Oil can").interact();
							sleepUntil(() -> engine.getInventory().contains("Oil can"), 6000);
							puzzleStageId = 24;
						} else if (puzzleStageId == 24) {
							pulledLever = engine.getGameObjects().getTopObjectOnTile(new Tile(3100, 9755, 0)).interactForceRight("Open");//door 9
							sleepUntil(() -> engine.getLocalPlayer().getX() > 3100, 6000);
							if (pulledLever)
								puzzleStageId = 25;
						} else if (puzzleStageId == 25) {
							engine.getWalking().walk(new Tile(3116 + Calculations.random(-2, 2), 9755 + Calculations.random(-2, 0), 0));
							sleepUntil(() -> engine.getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> engine.getLocalPlayer().distance(engine.getGameObjects().closest("Ladder")) < 6, 6000);
							pulledLever = engine.getGameObjects().closest("Ladder").interact();//ladder
							sleepUntil(() -> engine.getLocalPlayer().getY() < 4000, 6000);
							progressId = 4;
						}
					}
				}
			} else if (progressId == 4) {//give items to stein
				if (engine.getLocalPlayer().getZ() == 0) {
					if (engine.getMap().canReach(new Tile(3096, 3359, 0)) && engine.getLocalPlayer().getX() < 3095) {
						engine.getGameObjects().closest("Lever").interactForceRight("Pull");
						sleepUntil(() -> engine.getMap().canReach(new Tile(3098, 3359, 0)), 6000);
					} else if (!engine.getMap().canReach(new Tile(3103, 3365, 0))) {
						getObject(new Tile(3103, 3364, 0), "Door").interactForceRight("Open");//rc
						sleepUntil(() -> !engine.getMap().canReach(new Tile(3103, 3365, 0)), 6000);
					} else if (!engine.getMap().canReach(new Tile(3106, 3367, 0))) {
						if (engine.getLocalPlayer().distance(new Tile(3106, 3368, 0)) < 4) {
							engine.getWalking().walk(new Tile(3106 + Calculations.random(-2, 2), 3368 + Calculations.random(-1, 1), 0));
							sleepUntil(() -> engine.getLocalPlayer().isMoving(), 6000);
							sleepUntil(() -> engine.getLocalPlayer().distance(new Tile(3106, 3368, 0)) < 6, 6000);
						} else {
							getObject(new Tile(3106, 3368, 0), "Door").interactForceRight("Open");//rc
							sleepUntil(() -> !engine.getMap().canReach(new Tile(3106, 3367, 0)), 6000);
						}
					} else {
						engine.getGameObjects().closest("Staircase").interactForceRight("Climb-up");//rc
						sleepUntil(() -> engine.getLocalPlayer().getZ() == 1, 6000);
					}
				} else if (engine.getLocalPlayer().getZ() == 1) {
					getObject(new Tile(3104, 3362, 1), "Staircase").interact();//lc
					sleepUntil(() -> engine.getLocalPlayer().getZ() == 2, 6000);
				} else if (engine.getLocalPlayer().getZ() == 2) {//floor with ernest
					if (!engine.getMap().canReach(new Tile(3107, 3364, 2))) {
						getObject(new Tile(3108, 3364, 2), "Door").interact("Open");//lc
						sleepUntil(() -> engine.getMap().canReach(new Tile(3107, 3364, 2)), 6000);
					} else {
						engine.getNpcs().closest("Professor Oddenstein").interact();
						sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
						progressId = 5;
					}
				}
			} else if (progressId == 5) {//cutscene
				if (engine.getWidgets().getWidget(277) != null && engine.getWidgets().getWidget(277).isVisible()) {
					engine.getWidgets().getWidget(277).close();
					sleepUntil(() -> engine.getWidgets().getWidget(277) == null, Calculations.random(3000, 5000));
					if (taskScript)
						progressId = 7;
					else
						progressId = 6;
				}
			} else if (progressId == 7) {
				if (engine.getLocalPlayer().distance(new Tile(3221, 3217, 0)) < 10) {
					progressId = 6;
				} else if (!engine.getTabs().isOpen(Tab.MAGIC)) {
					engine.getTabs().open(Tab.MAGIC);
					sleepUntil(() -> engine.getTabs().isOpen(Tab.MAGIC), Calculations.random(3000, 5000));
				} else if (!engine.getLocalPlayer().isAnimating()) {
					engine.getMagic().castSpell(Normal.HOME_TELEPORT);
					sleepUntil(() -> engine.getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
					sleepUntil(() -> !engine.getLocalPlayer().isAnimating(), Calculations.random(3000, 5000));
				}
			}
			break;
		case DIALOGUE:
			if (engine.getDialogues().getOptions() != null && engine.getDialogues().getOptions().length > 0) {
				List<String> options = Arrays.asList(engine.getDialogues().getOptions());
				if (options.size() == 2) {
					if (options.contains("Aha, sounds like a quest. I'll help.")) {
						engine.getDialogues().chooseOption(1);
					} else if (progressId == 2 && options.contains("Change him back this instant!")) {
						engine.getDialogues().chooseOption(2);
						progressId = 3;//time to go look for stuff
						gatherItemsId = 0;
					}
				} else if (options.size() == 3) {
					if (progressId == 2 && options.contains("I'm looking for a guy called Ernest.")) {
						engine.getDialogues().chooseOption(1);
					}
				} else if (options.size() == 4) {

				}
			} else {
				if (engine.getDialogues().getNPCDialogue() != null) {
					if (progressId == 0 && engine.getDialogues().getNPCDialogue()
							.contains("Thank you, thank you. I'm very grateful."))
						progressId = 2;//go into the mansion

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
