package TaskManager.scripts.woodcutting;

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
		BRONZE_AXE(1351, 1, 0),
		IRON_AXE(1349, 1, 1),
		STEEL_AXE(1353, 6, 2),
		BLACK_AXE(1361, 11, 3),
		MITHRIL_AXE(1355, 21, 4),
		ADAMANT_AXE(1357, 31, 5),
		RUNE_AXE(1359, 41, 6),
		GILDED_AXE(23279, 41, 7),
		DRAGON_AXE(6739, 61, 8),
		THIRD_AGE_AXE(6739, 61, 9),
		INFERNAL_AXE(6739, 61, 10),
		CRYSTAL_AXE(23673, 71, 11);
		
		private int axeId;
		private int levelReq;
		private int priority;
		
		private Axe(int axeId, int levelReq, int priority) {
			this.axeId = axeId;
			this.levelReq = levelReq;
			this.priority = priority;
		}

		public int getAxeId() {
			return axeId;
		}

		public int getLevelReq() {
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
