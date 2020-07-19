package TaskManager.scripts.mining;

public class MinerData {
	
	public enum OreNode {
		RUNE_ESSENCE_NODE(),
		CLAY_NODE(11363, 11362),
		TIN_NODE(11360, 11361),
		COPPER_NODE(11161, 10943),
		BLURITE_NODE(-1),
		LIMESTONE_NODE(-1),
		IRON_NODE(11365, 11364),
		SILVER_NODE(11368, 11369),
		VOLCANIC_ASH_NODE(-1),
		COAL_NODE(11366),
		PURE_ESSENCE_NODE(-1),
		SANDSTONE_NODE(-1),
		GOLD_NODE(11371, 11370),
		VULCANIC_SULPHUR_NODE(-1),
		GRANITE_NODE(-1),
		MITHRIL_NODE(11372, 11373),
		LOVAKITE_NODE(-1),
		ADAMANTITE_NODE(11375, 11374),
		RUNITE_NODE(11376, 11377),
		AMETHYST_NODE(-1);
		
		private int[] rockIds;
		
		private OreNode(int... rockIds) {
			this.rockIds = rockIds;
		}

		public int[] getRockIds() {
			return rockIds;
		}
		
		public boolean hasMatch(int nodeId) {
			for (int rockId : rockIds) {
				if (rockId == nodeId)
					return true;
			}
			return false;
		}
		
		public Ore getOreFromNode() {
			switch(this) {
				case TIN_NODE: return Ore.TIN;
				case COPPER_NODE: return Ore.COPPER;
				case IRON_NODE: return Ore.IRON;
				case SILVER_NODE: return Ore.SILVER;
				default: return Ore.CLAY;
			}
		}
		
		@Override
		public String toString() {
			String name = name();
			if (name.contains("_"))
				name = name.substring(0, name.indexOf("_"));
			return name.substring(0, 1) + name.substring(1).replaceAll("_", " ").toLowerCase();
		}
	}
	
	public enum Ore {
		CLAY(434),
		TIN(438),
		COPPER(436),
		IRON(440),
		SILVER(442),
		GOLD(444),
		PERFECT_GOLD(446),
		MITHRIL(447),
		ADAMANTITE(449),
		RUNITE(451),
		COAL(453),
		BLURITE(668),
		RUNE_ESSENCE(1436),
		ELEMENTAL(2892),
		LIMESTONE(3211),
		PURE_ESSENCE(7936),
		LUNAR(9076),
		DAEYALT(9632),
		PAY_DIRT(12011),
		VOLCANIC_SULPHUR(13571),
		AMETHYST(21347),
		VOLCANIC_ASH(21622);
		
		private int oreId;
		
		private Ore(int oreId) {
			this.oreId = oreId;
		}

		public int getOreId() {
			return oreId;
		}
	}
	
	public enum Pickaxe {
		BRONZE_PICKAXE(1265, 1, 0),
		IRON_PICKAXE(1267, 1, 1),
		STEEL_PICKAXE(1269, 6, 2),
		BLACK_PICKAXE(12297, 11, 3),
		MITHRIL_PICKAXE(1273, 21, 4),
		ADAMANT_PICKAXE(1271, 31, 5),
		RUNE_PICKAXE(1275, 41, 6),
		GILDED_PICKAXE(23276, 41, 7),
		DRAGON_PICKAXE(11920, 61, 8),
		THIRD_AGE_PICKAXE(20014, 61, 9),
		INFERNAL_PICKAXE(13243, 61, 10),
		CRYSTAL_PICKAXE(23680, 71, 11);
		
		private int pickaxeId;
		private int levelReq;
		private int priority;
		
		private Pickaxe(int pickaxeId, int levelReq, int priority) {
			this.pickaxeId = pickaxeId;
			this.levelReq = levelReq;
			this.priority = priority;
		}

		public int getPickaxeId() {
			return pickaxeId;
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
			if (name.equals("THIRD_AGE_PICKAXE"))
				return "3rd age pickaxe";
			return name.substring(0, 1) + name.substring(1).replaceAll("_", " ").toLowerCase();
		}
	}
}
