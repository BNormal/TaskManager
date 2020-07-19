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
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import TaskManager.Script;
import TaskManager.utilities.Utilities;

@ScriptManifest(author = "Trialander", category = Category.QUEST, name = "Cooks Assistant", version = 1.0, description = "Completes Cooks Assistant")
public class CookAssistant extends Script {
	private Timer totalTime = new Timer();
	private State state;

	private Area kitchen = new Area(3205, 3217, 3212, 3212, 0);
	private Area eggArea = new Area(3171, 3300, 3186, 3288, 0);
	private Area wheatArea = new Area(3155, 3301, 3162, 3293, 0);
	private Area bucketArea = new Area(3225, 3294, 3229, 3289, 0);
	private Area milkArea = new Area(3254, 3270, 3255, 3267, 0);
	private Area flourMill = new Area(3168, 3304, 3165, 3306, 0);

	WidgetChild interfaceItem;

	private enum State {
		TALK_COOK, DIALOGUE, GET_FLOUR, GET_EGG, GET_MILK, TURN_IN, FINISHED, NOTHING;
	}

	private State getState() {
		if (engine.getWidgets().getWidget(277) != null && engine.getWidgets().getWidget(277).isVisible() || getQuestProgress() == 5) {
			return State.FINISHED;
		}
		if (getQuestProgress() == 0 && !engine.getDialogues().inDialogue())
			return State.TALK_COOK;

		if (engine.getDialogues().inDialogue()) {
			return State.DIALOGUE;
		}

		if (getQuestProgress() == 1) {
			log("reached qp 1");
			return State.GET_EGG;
		}

		if (getQuestProgress() == 2) {
			log("reached qp 1");
			return State.GET_FLOUR;
		}

		if (getQuestProgress() == 3) {
			log("reached qp 1");
			return State.GET_MILK;
		}

		if (getQuestProgress() == 4) {
			log("reached qp 1");
			return State.TURN_IN;
		}

		if (getQuestProgress() == 5) {
			log("reached qp 1");
			return State.NOTHING;
		}
		return State.NOTHING;

	}

	private int getQuestProgress() {
		interfaceItem = getWidgets().getWidgetChild(399, 6, 1);
		if (interfaceItem != null) {
			if (interfaceItem.getTextColor() == 16711680)// not started/red
				return 0;
			else if (interfaceItem.getTextColor() == 16776960 && !engine.getInventory().contains("Egg"))// Get egg
				return 1;
			else if (interfaceItem.getTextColor() == 16776960 && !engine.getInventory().contains("Pot of flour"))// Get flour
				return 2;
			else if (interfaceItem.getTextColor() == 16776960 && !engine.getInventory().contains("Bucket of milk"))// Get milk
				return 3;
			else if (interfaceItem.getTextColor() == 16776960) // Turn in the quest
				return 4;
			else if (interfaceItem.getTextColor() == 901389) // Finished/Green
				return 5;
		}
		return -1;
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
		case DIALOGUE:
			if (engine.getDialogues().getOptions() != null && engine.getDialogues().getOptions().length > 0) {
				List<String> options = Arrays.asList(engine.getDialogues().getOptions());

				if (options.size() == 4) {
					if (options.contains("What's wrong?"))
						engine.getDialogues().chooseOption(1);
					else if (options.contains("How about milk?")) {
						engine.getDialogues().chooseOption(4);
					} 
				}

				if (options.size() == 2) {
					if (options.contains("I can't right now, Maybe later."))
						engine.getDialogues().chooseOption(1);
					else if (options.contains("I'll get right on it.")) {
						engine.getDialogues().chooseOption(1);
					}
				}

			} else {
				engine.getDialogues().spaceToContinue();
			}

			break;

		case TALK_COOK:
			if (kitchen.contains(engine.getLocalPlayer())) {
				engine.getNpcs().closest("Cook").interact();
				sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
			} else {
				engine.getWalking().walk(kitchen.getRandomTile());
				sleepUntil(() -> engine.getWalking().getDestinationDistance() < 6, 6000);
			}

			break;

		case GET_EGG:
			if (!engine.getInventory().contains("Pot"))
				engine.getGroundItems().closest("pot").interact();

			if (!engine.getInventory().contains("Egg")) {
				if (eggArea.contains(engine.getLocalPlayer())) {
					engine.getGroundItems().closest("Egg").interactForceRight("Take");
					sleepUntil(() -> engine.getInventory().contains("Egg"), 3000);
				} else {
					engine.getWalking().walk(eggArea.getRandomTile());
					sleepUntil(() -> engine.getWalking().getDestinationDistance() < 6, 6000);
					if (!engine.getMap().canReach(new Tile(3180, 3291, 0))) {
						engine.getGameObjects().closest("Gate").interactForceRight("Open");
					}
				}

			}

			break;

		case GET_FLOUR:
			if (!engine.getInventory().contains("Grain")) {
				if (wheatArea.contains(engine.getLocalPlayer())) {
					engine.getGameObjects().closest(15506).interactForceRight("Pick");
					sleepUntil(() -> engine.getInventory().contains("Grain"), 3000);
				} else {
					if (!engine.getInventory().contains("Grain")) {
						engine.getWalking().walk(wheatArea.getRandomTile());
						sleepUntil(() -> engine.getWalking().getDestinationDistance() < 6, 6000);
						if (!engine.getMap().canReach(new Tile(3161, 3293, 0))) {
							engine.getGameObjects().closest("Gate").interactForceRight("Open");
						}
					}
				}

			}
			if (engine.getInventory().contains("Grain")) { // Changes grain to flour
				engine.getWalking().walk(flourMill.getRandomTile());
				sleepUntil(() -> engine.getWalking().getDestinationDistance() < 6, 6000);
				if (!engine.getMap().canReach(new Tile(3167, 3304, 0))) {
					engine.getGameObjects().closest("Large door").interactForceRight("Open");
					sleepUntil(() -> engine.getGameObjects().closest("Large door").interactForceRight("Open") , 6000);
				}
				engine.getGameObjects().closest(12964).interact();
				sleep(3000);
				engine.getGameObjects().closest(12965).interactForceRight("Climb-up");
				sleep(3000);
				engine.getGameObjects().closest(24961).interact();
				sleep(3000);
				engine.getGameObjects().closest(24964).interact();
				sleep(3000);
				engine.getGameObjects().closest(12966).interactForceRight("Climb-down");
				sleep(3000);
				engine.getGameObjects().closest(12965).interactForceRight("Climb-down");
				sleep(3000);
				engine.getGameObjects().closest(1781).interact();
				sleep(3000);

			}

			break;

		case GET_MILK:

			if (!engine.getInventory().contains("Bucket")) {
				if (bucketArea.contains(engine.getLocalPlayer())) {
					engine.getGroundItems().closest("Bucket").interactForceRight("Take");
					sleepUntil(() -> engine.getInventory().contains("Bucket"), 3000);
				} else {
					if (!engine.getInventory().contains("Bucket")) {
						engine.getWalking().walk(bucketArea.getRandomTile());
						sleepUntil(() -> engine.getWalking().getDestinationDistance() < 6, 6000);
						if (!engine.getMap().canReach(new Tile(3236, 3295, 0))) {
							engine.getGameObjects().getTopObjectOnTile(new Tile(3236, 3295, 0))
									.interactForceRight("Open");// door
							sleepUntil(() -> engine.getMap().canReach(new Tile(3236, 3295, 0)), 6000);
						}
						if (!engine.getMap().canReach(new Tile(3229, 3291, 0))) {
							engine.getGameObjects().getTopObjectOnTile(new Tile(3230, 3291, 0))
									.interactForceRight("Open");// door
							sleepUntil(() -> engine.getMap().canReach(new Tile(3229, 3291, 0)), 6000);
						}
					}
				}
			}

			if (engine.getInventory().contains("Bucket")) { // Changes from bucket to milk
				if (milkArea.contains(engine.getLocalPlayer())) {
					engine.getGameObjects().closest(8689).interactForceRight("Milk");
					sleepUntil(() -> engine.getInventory().contains("Bucket of milk"), 3000);
				} else {
					engine.getWalking().walk(milkArea.getRandomTile());
					sleepUntil(() -> engine.getWalking().getDestinationDistance() < 6, 6000);
					if (!engine.getMap().canReach(new Tile(3254, 3266, 0))) {
						engine.getGameObjects().getTopObjectOnTile(new Tile(3253, 3267, 0)).interactForceRight("Open");// door
						sleepUntil(() -> engine.getMap().canReach(new Tile(3254, 3266, 0)), 6000);
					}
				}

			}

			break;

		case TURN_IN:
			if (kitchen.contains(engine.getLocalPlayer())) {
				engine.getNpcs().closest("Cook").interact();
				sleepUntil(() -> engine.getDialogues().canContinue(), Calculations.random(3000, 5000));
			} else {
				engine.getWalking().walk(kitchen.getRandomTile());
				sleepUntil(() -> engine.getWalking().getDestinationDistance() < 6, 6000);
			}
			break;
		case FINISHED:
			running = false;
			time = new Date(totalTime.elapsed());
			if (!taskScript) {
				engine.getTabs().logout();
				this.stop();
			}
		default:
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
		Utilities.drawShadowString(g, "Stage: " + state, x, 60);
	}

}
