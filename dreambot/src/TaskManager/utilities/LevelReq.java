package TaskManager.utilities;

import org.dreambot.api.methods.skills.Skill;

public class LevelReq {
	
	private int levelReq;
	private Skill skill;
	
	public LevelReq(Skill skill, int levelReq) {
		this.skill = skill;
		this.levelReq = levelReq;
	}
	
	public int getLevelReq() {
		return levelReq;
	}

	public void setLevelReq(int levelReq) {
		this.levelReq = levelReq;
	}

	public Skill getSkill() {
		return skill;
	}

	public void setSkill(Skill skill) {
		this.skill = skill;
	}
}
