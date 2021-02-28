package guiPieces;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

import utilities.ResourceLoader;

public class GuiConstants {
	// Custom fonts
	private static Font RobotoCondensed(boolean bold, float fontSize) {
		String fontFile;
		if (bold) {
			fontFile = "/fonts/RobotoCondensed-Bold.ttf";
		}
		else {
			fontFile = "/fonts/RobotoCondensed-Regular.ttf";
		}
		
		Font toReturn = null;
		try {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			toReturn = Font.createFont(Font.TRUETYPE_FONT, ResourceLoader.load(fontFile));
			ge.registerFont(toReturn);
			toReturn = toReturn.deriveFont(fontSize);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (FontFormatException e) {
			e.printStackTrace();
		}
		return toReturn;
	}
	public static Font customFont = RobotoCondensed(false, 15f);
	public static Font customFontBold = RobotoCondensed(true, 15f);
	public static Font customFontHeader = RobotoCondensed(false, 18f);
	public static int fontHeight = 11;  // Estimated
	
	// Custom colors that the GUI uses
	public static Color drgBackgroundBiege = new Color(83, 70, 51);
	public static Color drgBackgroundBrown = new Color(73, 63, 41);
	public static Color drgHighlightedYellow = new Color(255, 210, 0);
	public static Color drgRegularOrange = new Color(255, 156, 0);
	public static Color drgNegativeChangeRed = new Color(225, 50, 10);
	public static Color drgOverclockCleanGreen = new Color(67, 159, 97);
	public static Color drgOverclockBalancedYellow = new Color(231, 201, 71);
	public static Color drgOverclockUnstableRed = new Color(205, 45, 19);
	
	// Number of pixels that pad the edges of ModButton and OverclockButton objects
	public static int paddingPixels = 6;
	
	// This number determines the width of the edges of ModButton and OverclockButton objects
	public static int edgeWidth = 4;
	
	public static int numDecimalPlaces = 4;
	
	// Black border gets used on all 4 panes of the WeaponTab, orange border is used on HoverText and AoEVisualizerButton
	public static Border blackLine = BorderFactory.createLineBorder(Color.black);
	public static Border orangeLine = BorderFactory.createLineBorder(drgRegularOrange);
	public static Border greyLine = BorderFactory.createLineBorder(Color.gray);
}
