package TaskManager.scripts.quests;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.Category;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import TaskManager.Script;
import TaskManager.ScriptDetails;
import TaskManager.utilities.Utilities;

@ScriptDetails(author = "Trialander", category = Category.QUEST, name = "Cooks Assistant", version = 1.0, description = "Completes the Cook's Assistant quest.")
public class CookAssistant extends Script implements Serializable {
	private static final long serialVersionUID = 6969736550720908670L;
	private State state;
	private Area kitchen = new Area(3205, 3217, 3212, 3212, 0);
	private Area eggArea = new Area(3171, 3300, 3186, 3288, 0);
	private Area wheatArea = new Area(3155, 3301, 3162, 3293, 0);
	private Area bucketArea = new Area(3225, 3294, 3229, 3289, 0);
	private Area milkArea = new Area(3254, 3270, 3255, 3267, 0);
	private Area flourMill = new Area(3168, 3304, 3165, 3306, 0);
	private WidgetChild interfaceItem;

	public CookAssistant() {
		
	}
	
	private enum State {
		TALK_COOK, DIALOGUE, GET_FLOUR, GET_EGG, GET_MILK, TURN_IN, FINISHED, NOTHING;
	}

	private State getState() {
		if (Widgets.getWidget(277) != null && Widgets.getWidget(277).isVisible() || getQuestProgress() == 5) {
			return State.FINISHED;
		}
		if (getQuestProgress() == 0 && !Dialogues.inDialogue())
			return State.TALK_COOK;

		if (Dialogues.inDialogue()) {
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
		interfaceItem = Widgets.getWidgetChild(399, 6, 1);
		if (interfaceItem != null) {
			if (interfaceItem.getTextColor() == 16711680)// not started/red
				return 0;
			else if (interfaceItem.getTextColor() == 16776960 && !Inventory.contains("Egg"))// Get egg
				return 1;
			else if (interfaceItem.getTextColor() == 16776960 && !Inventory.contains("Pot of flour"))// Get flour
				return 2;
			else if (interfaceItem.getTextColor() == 16776960 && !Inventory.contains("Bucket of milk"))// Get milk
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
	}

	@Override
	public int onLoop() {
		state = getState();

		switch (state) {
		case DIALOGUE:
			if (Dialogues.getOptions() != null && Dialogues.getOptions().length > 0) {
				List<String> options = Arrays.asList(Dialogues.getOptions());

				if (options.size() == 4) {
					if (options.contains("What's wrong?"))
						Dialogues.chooseOption(1);
					else if (options.contains("How about milk?")) {
						Dialogues.chooseOption(4);
					} 
				}

				if (options.size() == 2) {
					if (options.contains("I can't right now, Maybe later."))
						Dialogues.chooseOption(1);
					else if (options.contains("I'll get right on it.")) {
						Dialogues.chooseOption(1);
					}
				}

			} else {
				Dialogues.spaceToContinue();
			}

			break;

		case TALK_COOK:
			if (kitchen.contains(getLocalPlayer())) {
				NPCs.closest("Cook").interact();
				sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
			} else {
				Walking.walk(kitchen.getRandomTile());
				sleepUntil(() -> Walking.getDestinationDistance() < 6, 6000);
			}

			break;

		case GET_EGG:
			if (!Inventory.contains("Pot"))
				GroundItems.closest("pot").interact();

			if (!Inventory.contains("Egg")) {
				if (eggArea.contains(getLocalPlayer())) {
					GroundItems.closest("Egg").interactForceRight("Take");
					sleepUntil(() -> Inventory.contains("Egg"), 3000);
				} else {
					Walking.walk(eggArea.getRandomTile());
					sleepUntil(() -> Walking.getDestinationDistance() < 6, 6000);
					if (!Map.canReach(new Tile(3180, 3291, 0))) {
						GameObjects.closest("Gate").interactForceRight("Open");
					}
				}

			}

			break;

		case GET_FLOUR:
			if (!Inventory.contains("Grain")) {
				if (wheatArea.contains(getLocalPlayer())) {
					GameObjects.closest(15506).interactForceRight("Pick");
					sleepUntil(() -> Inventory.contains("Grain"), 3000);
				} else {
					if (!Inventory.contains("Grain")) {
						Walking.walk(wheatArea.getRandomTile());
						sleepUntil(() -> Walking.getDestinationDistance() < 6, 6000);
						if (!Map.canReach(new Tile(3161, 3293, 0))) {
							GameObjects.closest("Gate").interactForceRight("Open");
						}
					}
				}

			}
			if (Inventory.contains("Grain")) { // Changes grain to flour
				Walking.walk(flourMill.getRandomTile());
				sleepUntil(() -> Walking.getDestinationDistance() < 6, 6000);
				if (!Map.canReach(new Tile(3167, 3304, 0))) {
					GameObjects.closest("Large door").interactForceRight("Open");
					sleepUntil(() -> GameObjects.closest("Large door").interactForceRight("Open") , 6000);
				}
				GameObjects.closest(12964).interact();
				sleep(3000);
				GameObjects.closest(12965).interactForceRight("Climb-up");
				sleep(3000);
				GameObjects.closest(24961).interact();
				sleep(3000);
				GameObjects.closest(24964).interact();
				sleep(3000);
				GameObjects.closest(12966).interactForceRight("Climb-down");
				sleep(3000);
				GameObjects.closest(12965).interactForceRight("Climb-down");
				sleep(3000);
				GameObjects.closest(1781).interact();
				sleep(3000);

			}

			break;

		case GET_MILK:

			if (!Inventory.contains("Bucket")) {
				if (bucketArea.contains(getLocalPlayer())) {
					GroundItems.closest("Bucket").interactForceRight("Take");
					sleepUntil(() -> Inventory.contains("Bucket"), 3000);
				} else {
					if (!Inventory.contains("Bucket")) {
						Walking.walk(bucketArea.getRandomTile());
						sleepUntil(() -> Walking.getDestinationDistance() < 6, 6000);
						if (!Map.canReach(new Tile(3236, 3295, 0))) {
							GameObjects.getTopObjectOnTile(new Tile(3236, 3295, 0))
									.interactForceRight("Open");// door
							sleepUntil(() -> Map.canReach(new Tile(3236, 3295, 0)), 6000);
						}
						if (!Map.canReach(new Tile(3229, 3291, 0))) {
							GameObjects.getTopObjectOnTile(new Tile(3230, 3291, 0))
									.interactForceRight("Open");// door
							sleepUntil(() -> Map.canReach(new Tile(3229, 3291, 0)), 6000);
						}
					}
				}
			}

			if (Inventory.contains("Bucket")) { // Changes from bucket to milk
				if (milkArea.contains(getLocalPlayer())) {
					GameObjects.closest(8689).interactForceRight("Milk");
					sleepUntil(() -> Inventory.contains("Bucket of milk"), 3000);
				} else {
					Walking.walk(milkArea.getRandomTile());
					sleepUntil(() -> Walking.getDestinationDistance() < 6, 6000);
					if (!Map.canReach(new Tile(3254, 3266, 0))) {
						GameObjects.getTopObjectOnTile(new Tile(3253, 3267, 0)).interactForceRight("Open");// door
						sleepUntil(() -> Map.canReach(new Tile(3254, 3266, 0)), 6000);
					}
				}

			}

			break;

		case TURN_IN:
			if (kitchen.contains(getLocalPlayer())) {
				NPCs.closest("Cook").interact();
				sleepUntil(() -> Dialogues.canContinue(), Calculations.random(3000, 5000));
			} else {
				Walking.walk(kitchen.getRandomTile());
				sleepUntil(() -> Walking.getDestinationDistance() < 6, 6000);
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
		Utilities.drawShadowString(g, "Stage: " + state, x, 60);
	}

}
