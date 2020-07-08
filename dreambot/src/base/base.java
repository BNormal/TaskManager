package base;

import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;


@ScriptManifest(author = "Author", name = "Script Name (unf)", version = 1.0, description = "Description of the bot", category = Category.RUNECRAFTING)
public class base extends AbstractScript {

	@Override
	public void onStart() {//runs this once at the very beginning
		log("Welcome to " + getManifest().name() + " 1.0");
	}
	
	@Override
	public int onLoop() {//run every single time
		
		return 0;
	}
	
	@Override
	public void onExit() {//runs this once after stopping your script

	}

}
