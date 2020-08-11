package TaskManager.scripts.misc;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.walking.web.node.impl.bank.WebBankArea;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.core.Instance;

import TaskManager.Script;

@ScriptManifest(author = "NumberZ", category = Category.MISC, name = "GE Trader (unf)", version = 1.0, description = "Buy and sell items at the Grand Exchange")
public class GETrader extends Script {
	private State state = null;
	private GETraderGUI gui;

	private enum State {
		WALKING, BUYING, SELLING, NOTHING
	}
	
	private State getState() {
		if (!WebBankArea.GRAND_EXCHANGE.getArea().contains(getLocalPlayer()))
			return State.WALKING;
		return State.NOTHING;
	}
	
	@Override
	public void init() {
		gui = new GETraderGUI(getManifest().name());
		gui.open();
	}
	
	@Override
	public void onStart() {
		if (!taskScript)
			init();
		super.onStart();
		if (engine == null)
			engine = this;
	}
	
	@Override
	public int onLoop() {
		if (!running || !gui.isFinished() || !engine.getLocalPlayer().isOnScreen() || Instance.getInstance().isMouseInputEnabled())
			return 0;
		state = getState();
		switch (state) {
		case WALKING:
			engine.getWalking().walk(WebBankArea.GRAND_EXCHANGE.getArea().getRandomTile());
			if (Calculations.random(0, 20) > 2)
				sleepUntil(() -> engine.getWalking().getDestinationDistance() < Calculations.random(6, 9), 6000);
			break;
		default:
			break;
			
		}
		return 0;
	}

	@Override
	public void onExit() {
		gui.exit();
		super.onExit();
	}
	
}
