package TaskManager.utilities;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.text.DecimalFormat;

import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.SkillTracker;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Timer;

public class SkillTracking {
	
	private long[] skillDelays = new long[23];
	
	public SkillTracking() {
		for (int i = 0; i < skillDelays.length; i++)
			SkillTracker.start(Skill.forId(i));
	}
	
	public void refresh() {
		for (int i = 0; i < skillDelays.length; i++) {
			if (SkillTracker.getGainedExperience(Skill.forId(i)) > 0) {
				SkillTracker.reset(Skill.forId(i));
				skillDelays[i] = System.currentTimeMillis();
			}
		}
	}
	
	public long[] getSkillsTimers() {
		return skillDelays;
	}
	
	public static void drawShadowString(Graphics2D g, String s, int x, int y) {
		drawShadowString(g, s, x, y, Color.WHITE, Color.BLACK);
	}
	
	public static void drawShadowString(Graphics2D g, String s, int x, int y, Color face, Color shadow) {
		g.setColor(shadow);
		g.drawString(s, x + 1, y + 1);
		g.setColor(face);
		g.drawString(s, x, y);
	}
	
	public Color getSkillBaseColor(int skillID) {
		switch (skillID) {
		case 0: return Color.decode("#4c0f0a"); // attack
		case 1: return Color.decode("#4b5887"); // defence
		case 2: return Color.decode("#0b4e31"); // strength
		case 3: return Color.decode("#921e14"); // hitpoints
		case 4: return Color.decode("#46591b"); // range
		case 5: return Color.decode("#e7c720"); // prayer
		case 6: return Color.decode("#313270"); // magic
		case 7: return Color.decode("#3b1e44"); // cooking
		case 8: return Color.decode("#725b32"); // woodcutting
		case 9: return Color.decode("#1f4547"); // fletching
		case 10: return Color.decode("#5c748b"); // fishing
		case 11: return Color.decode("#ac5518"); // firemaking
		case 12: return Color.decode("#605349"); // crafting
		case 13: return Color.decode("#605349"); // smithing
		case 14: return Color.decode("#44443b"); // mining
		case 15: return Color.decode("#0a4c0c"); // herblore
		case 16: return Color.decode("#1a1b55"); // agility
		case 17: return Color.decode("#552f47"); // thieving
		case 18: return Color.decode("#1d1c1c"); // slayer
		case 19: return Color.decode("#1e441e"); // farming
		case 20: return Color.decode("#90908a"); // runecrafting
		case 21: return Color.decode("#4c483a"); // hunter
		case 22: return Color.decode("#7f796e"); // construction
		}
		return new Color(1, 1, 1);
	}
	
	public Color getSkillTrimColor(int skillID) {
		switch (skillID) {
		case 0: return Color.decode("#b78519"); // attack
		case 1: return Color.decode("#b4b39b"); // defence
		case 2: return Color.decode("#721710"); // strength
		case 3: return Color.decode("#921e14"); // hitpoints
		case 4: return Color.decode("#6e3f21"); // range
		case 5: return Color.decode("#e7c720"); // prayer
		case 6: return Color.decode("#313270"); // magic
		case 7: return Color.decode("#4e100b"); // cooking
		case 8: return Color.decode("#1f472c"); // woodcutting
		case 9: return Color.decode("#b79e19"); // fletching
		case 10: return Color.decode("#b79e19"); // fishing
		case 11: return Color.decode("#b79e19"); // firemaking
		case 12: return Color.decode("#b59c19"); // crafting
		case 13: return Color.decode("#b59c19"); // smithing
		case 14: return Color.decode("#507d90"); // mining
		case 15: return Color.decode("#bea31a"); // herblore
		case 16: return Color.decode("#6a2520"); // agility
		case 17: return Color.decode("#242222"); // thieving
		case 18: return Color.decode("#59120c"); // slayer
		case 19: return Color.decode("#879e58"); // farming
		case 20: return Color.decode("#a58e17"); // runecrafting
		case 21: return Color.decode("#302c29"); // hunter
		case 22: return Color.decode("#7c5011"); // construction
		}
		return new Color(1, 1, 1);
	}
	
	public void drawSkillProgress(Graphics2D g, int x, int y, double value, double maxValue, int skill, int currentLevel, int alpha) {
		if (alpha > 255)
			alpha = 255;
		double percentage = 100.0 / maxValue * value / 100.0;
		DecimalFormat df2 = new DecimalFormat("#.##");
		Color originalC = g.getColor();
		Font originalF = g.getFont();
		Color base = new Color(getSkillBaseColor(skill).getRGB());
		Color trimmed = Color.WHITE;
		Color shadow = Color.BLACK;
		trimmed = new Color(trimmed.getRed(), trimmed.getGreen(), trimmed.getBlue(), (alpha - 55 < 0 ? 0 : alpha));
		shadow = new Color(shadow.getRed(), shadow.getGreen(), shadow.getBlue(), (alpha - 55 < 0 ? 0 : alpha));
		base = new Color(base.getRed(), base.getGreen(), base.getBlue(), (alpha - 55 < 0 ? 0 : alpha - 55));
		base = base.brighter();
		g.setColor(base);
		g.fillRect(x + 1, y + 1, (int) (124 * percentage), 14);
		g.setColor(trimmed);
		g.setFont(new Font("Arial", 1, 11));
		g.drawRect(x, y, 125, 15);
		drawShadowString(g, "% " + df2.format(percentage * 100.0), x + 43, y + 12, trimmed, shadow);
		drawShadowString(g, Integer.toString(currentLevel), x + 4, y + 12, trimmed, shadow);
		drawShadowString(g, Integer.toString(currentLevel + 1), x + 110, y + 12, trimmed, shadow);
		/*g.drawString("% " + df2.format(percentage * 100.0), x + 43, y + 12);
		g.drawString(currentLevel + "", x + 4, y + 12);
		g.drawString((currentLevel + 1) + "", x + 110, y + 12);*/
		g.setFont(originalF);
		g.setColor(originalC);
	}
	
	public void onPaint(Graphics2D g, Timer totalTime, int xPosition) {
		long[] skills = skillDelays;
		int needToDisplay = 0;
		for (int i = 0; i < skills.length; i++) {
			long decay = System.currentTimeMillis() - skills[i];
			if (decay <= 6000 && totalTime.elapsed() >= 4000) {
				Skill skill = Skill.forId(i);
				int level = Skills.getRealLevel(skill);
				int xpNeeded = Utilities.getXPForLevel(level + 1) - Utilities.getXPForLevel(level);
				int xp = Skills.getExperience(skill) - Utilities.getXPForLevel(level);
				int alpha = (decay > 5000 ? (int) (255 - ((100.0 / 1000.0 * (decay - 5000)) / 100.0 * 255)) : 255);
				drawSkillProgress(
					g, xPosition - 1, 72 + (needToDisplay * 15) + (needToDisplay * 3), xp, xpNeeded, skill.getId(),
					level, alpha
				);
				needToDisplay++;
			}
		}
	}
}
