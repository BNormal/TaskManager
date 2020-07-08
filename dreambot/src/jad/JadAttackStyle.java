package jad;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.dreambot.api.methods.prayer.Prayer;

public enum JadAttackStyle {

	MAGE(2656, Prayer.PROTECT_FROM_MAGIC, Color.BLUE),
	MELEE(2655, Prayer.PROTECT_FROM_MELEE, Color.RED),
	RANGE(2652, Prayer.PROTECT_FROM_MISSILES, Color.GREEN);

	private int animationId;
	private Prayer prayer;
	private Color textColor;

	private static Map<Integer, JadAttackStyle> styles = new HashMap<Integer, JadAttackStyle>();
	
	public static JadAttackStyle forAnimId(int animId) {
		return styles.get(animId);
	}
	
	static {
		for (final JadAttackStyle style : JadAttackStyle.values()) {
			styles.put(style.animationId, style);
		}
	}
	
	private JadAttackStyle(int animationId, Prayer prayer, Color textColor) {
		this.animationId = animationId;
		this.prayer = prayer;
		this.textColor = textColor;
	}

	public int getAnimationId() {
		return animationId;
	}

	public Prayer getPrayer() {
		return prayer;
	}

	public Color getTextColor() {
		return textColor;
	}
}
