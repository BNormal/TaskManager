package TaskManager.scripts.woodcutting;

import TaskManager.utilities.LevelReq;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;

public class WoodcutterData {
	public enum Tree {
		TREE("Tree"),
		DEAD_TREE("Dead Tree"),
		ACHEY_TREE("Achey"),
		OAK_TREE("Oak"),
		WILLOW_TREE("Willow"),
		TEAK_TREE("Teak"),
		MAPLE_TREE("Maple"),
		MAHOGANY_TREE("Mahogany"),
		ARCTIC_PINE_TREE("Arctic pine"),
		BLISTERWOOD_TREE("Blisterwood"),
		YEW_TREE("Yew"),
		MAGIC_TREE("Magic"),
		REDWOOD_TREE("Redwood");
		
		private String name;
		
		private Tree(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
		
		public boolean hasMatch(String name) {
			if (this.name.equals(name))
				return true;
			return false;
		}
		
		public Log getLogFromTree() {
			switch(this) {
				case OAK_TREE: return Log.OAK_LOGS;
				case ACHEY_TREE: return Log.ACHEY_LOGS;
				case WILLOW_TREE: return Log.WILLOW_LOGS;
				case TEAK_TREE: return Log.TEAK_LOGS;
				case MAPLE_TREE: return Log.MAPLE_LOGS;
				case MAHOGANY_TREE: return Log.MAHOGANY_LOGS;
				case ARCTIC_PINE_TREE: return Log.ARCTIC_PINE_LOGS;
				case BLISTERWOOD_TREE: return Log.BLISTERWOOD_LOGS;
				case YEW_TREE: return Log.YEW_LOGS;
				case MAGIC_TREE: return Log.MAGIC_LOGS;
				case REDWOOD_TREE: return Log.REDWOOD_LOGS;
				default: return Log.LOG;
			}
		}
		
		@Override
		public String toString() {
			String name = name();
			return name.substring(0, 1) + name.substring(1).replaceAll("_", " ").toLowerCase();
		}
	}
	
	public enum Log {
		LOG(1511),
		ACHEY_LOGS(2862),
		OAK_LOGS(1521),
		WILLOW_LOGS(1519),
		TEAK_LOGS(6333),
		MAPLE_LOGS(1517),
		ARCTIC_PINE_LOGS(10810),
		BARK(3239),
		MAHOGANY_LOGS(6332),
		YEW_LOGS(1515),
		BLISTERWOOD_LOGS(24691),
		MAGIC_LOGS(1513),
		REDWOOD_LOGS(19669);
		
		private int logId;
		
		private Log(int logId) {
			this.logId = logId;
		}

		public int getLogId() {
			return logId;
		}
	}
	
	public enum Axe {
		BRONZE_AXE(1351, 0, new LevelReq(Skill.WOODCUTTING, 1), new LevelReq(Skill.ATTACK, 1)),
		IRON_AXE(1349, 1, new LevelReq(Skill.WOODCUTTING, 1), new LevelReq(Skill.ATTACK, 1)),
		STEEL_AXE(1353, 2, new LevelReq(Skill.WOODCUTTING, 6), new LevelReq(Skill.ATTACK, 5)),
		BLACK_AXE(1361, 3, new LevelReq(Skill.WOODCUTTING, 11), new LevelReq(Skill.ATTACK, 10)),
		MITHRIL_AXE(1355, 4, new LevelReq(Skill.WOODCUTTING, 21), new LevelReq(Skill.ATTACK, 20)),
		ADAMANT_AXE(1357, 5, new LevelReq(Skill.WOODCUTTING, 31), new LevelReq(Skill.ATTACK, 30)),
		RUNE_AXE(1359, 6, new LevelReq(Skill.WOODCUTTING, 41), new LevelReq(Skill.ATTACK, 40)),
		GILDED_AXE(23279, 7, new LevelReq(Skill.WOODCUTTING, 41), new LevelReq(Skill.ATTACK, 40)),
		DRAGON_AXE(6739, 8, new LevelReq(Skill.WOODCUTTING, 61), new LevelReq(Skill.ATTACK, 60)),
		THIRD_AGE_AXE(6739, 9, new LevelReq(Skill.WOODCUTTING, 61), new LevelReq(Skill.ATTACK, 65)),
		INFERNAL_AXE(6739, 10, new LevelReq(Skill.WOODCUTTING, 61), new LevelReq(Skill.FIREMAKING, 85), new LevelReq(Skill.ATTACK, 60)),
		CRYSTAL_AXE(23673, 11, new LevelReq(Skill.WOODCUTTING, 71), new LevelReq(Skill.AGILITY, 50), new LevelReq(Skill.ATTACK, 70));
		
		private int axeId;
		private int priority;
		private LevelReq[] levelReq;
		
		private Axe(int axeId, int priority, LevelReq... levelReq) {
			this.axeId = axeId;
			this.priority = priority;
			this.levelReq = levelReq;
		}

		public int getAxeId() {
			return axeId;
		}

		public boolean meetsAllReqsToUse(Skills skills) {
			for (int i = 0; i < levelReq.length - 1; i++) {
				if (skills.getBoostedLevels(levelReq[i].getSkill()) < levelReq[i].getLevelReq())
					return false;
			}
			return true;
		}
		
		public boolean meetsAllReqsToWield(Skills skills) {
			for (int i = 0; i < levelReq.length; i++) {
				if (skills.getBoostedLevels(levelReq[i].getSkill()) < levelReq[i].getLevelReq())
					return false;
			}
			return true;
		}
		
		public int getLevelReqBySkill(Skill skill) {
			for (int i = 0; i < levelReq.length; i++) {
				if (levelReq[i].getSkill() == skill)
					return levelReq[i].getLevelReq();
			}
			return 1;
		}
		
		public LevelReq[] getLevelRequirements() {
			return levelReq;
		}
		
		public int getPriority() {
			return priority;
		}
		
		@Override
		public String toString() {
			String name = name();
			if (name.equals("THIRD_AGE_AXE"))
				return "3rd age axe";
			return name.substring(0, 1) + name.substring(1).replaceAll("_", " ").toLowerCase();
		}
	}
}
