package TaskManager.utilities;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.ImageIcon;

import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.walking.web.node.impl.bank.WebBankArea;

public class Utilities {
	
	public static Area getLumbridgeBank() {
		Area lumbridgeBank = WebBankArea.LUMBRIDGE.getArea();
		lumbridgeBank.setZ(2);
		return lumbridgeBank;
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
	
	public static String insertCommas(long number){
		return insertCommas(number + "");
	}
	
	public static String insertCommas(String str){
		if(str.length() < 4){
			return str;
		}
		return insertCommas(str.substring(0, str.length() - 3)) + "," + str.substring(str.length() - 3, str.length());
	}

	public static String getJsonFromURL(String urlString) throws Exception {
		return getJsonFromURL(new URL(urlString));
	}
	
	public static String getJsonFromURL(URL url) throws Exception {
		BufferedReader reader = null;
	    try {
	        reader = new BufferedReader(new InputStreamReader(url.openStream()));
	        StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1) {
	        	buffer.append(chars, 0, read);
	        }
	        return buffer.toString();
	    } finally {
	        if (reader != null)
	            reader.close();
	    }
	}
	
	public static ImageIcon mergeIcons(ImageIcon background, ImageIcon foreground, Point location) {
        // For simplicity we will presume the images are of identical size
        final BufferedImage combinedImage = new BufferedImage(background.getIconWidth(), background.getIconHeight(), BufferedImage.TYPE_INT_ARGB );
        Graphics2D g = combinedImage.createGraphics();
        g.drawImage(background.getImage(), 0, 0, null);
        g.drawImage(foreground.getImage(), (int) location.getX(), (int) location.getY(), null);
        g.dispose();
        return new ImageIcon(combinedImage);
	}
}
