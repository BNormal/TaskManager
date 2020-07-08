package TaskManager.tasks;

import java.awt.Graphics2D;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import TaskManager.Script;

@ScriptManifest(author = "NumberZ", name = "Tutorial Island (unf)", version = 1.0, description = "Does tutorial island for you.", category = Category.MISC)
public class TutorialIsle extends Script {

	WidgetChild interfaceItem = null;
	private State state = null;
	
	private enum State {
		NAMING, CHARACTER_CREATION, TALK_TO_GIELINOR, NOTHING
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
		return State.NOTHING;
	}
	
	@Override
    public void onStart() {
		running = true;
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
		case TALK_TO_GIELINOR:
			if (!getDialogues().inDialogue()) {
				NPC gielinor = getNpcs().closest("Gielinor Guide");
				gielinor.interact();
			} else {
				log(getDialogues().getNPCDialogue());//this should display the text for dialogue
				//check dialgoue text for which tab you need to open
				//getTabs().open(Tab.OPTIONS);
				if (getDialogues().canContinue()) {
					getDialogues().clickContinue();
				} else {
					getDialogues().chooseOption(1);
				}
			}
			break;
		}
		this.stop();
		return 0;
	}
	
	@Override
    public void onPaint(Graphics2D graphics) {
		
	}
	
	@Override
    public void onExit() {
		running = false;
	}

}
