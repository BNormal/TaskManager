package MobHunter;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Utilities {

	public static void drawItem(Graphics2D g, BufferedImage item, int id, int amount, int x, int y) {
		if (item != null) {
			g.setColor(new Color(0.353f, 0.322f, 0.271f, 0.55F));
			g.fillRect(x, y, 38, 37);
			g.setColor(new Color(0.22f, 0.188f, 0.137f, 1.0F));
			g.drawRect(x, y, 38, 37);
			g.setColor(new Color(0.353f, 0.322f, 0.271f, 1.0F));
			g.drawRect(x + 1, y + 1, 36, 35);
			g.drawImage(item, x + 2, y + 7, null);
			g.setColor(Color.BLACK);
			g.drawString(amount + "", x + 4, y + 13);
			g.setColor(Color.WHITE);
			g.drawString(amount + "", x + 3, y + 12);
		}
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
	
	public static int getXPForLevel(int level) {
        int points = 0;
        int output = 0;
        for (int lvl = 1; lvl <= level; lvl++) {
            points += Math.floor((double) lvl + 300.0 * Math.pow(2.0, (double) lvl / 7.0));
            if (lvl >= level)
                return output;
            output = (int) Math.floor(points / 4);
       }
       return 0;
    }
	
	public static Color getContrastColor(Color color) {
		double y = (299 * color.getRed() + 587 * color.getGreen() + 114 * color.getBlue()) / 1000;
		return y >= 128 ? Color.black : Color.white;
	}
	
	public static String insertCommas(int number){
		return insertCommas(number + "");
	}
	
	public static String insertCommas(String str){
		if(str.length() < 4){
			return str;
		}
		return insertCommas(str.substring(0, str.length() - 3)) + "," + str.substring(str.length() - 3, str.length());
	}
	
}
