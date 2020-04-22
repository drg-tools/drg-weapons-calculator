package guiPieces;

import java.awt.image.BufferedImage;

import utilities.ResourceLoader;

public class ButtonIcons {
	// Start by loading all of the images once at the start, so that they don't have to be loaded every time repaint() gets called.
	private static BufferedImage aimIcon = ResourceLoader.loadImage("images/Icon_Upgrade_Aim.png");
	private static BufferedImage aimIconBlack = ResourceLoader.loadImage("images/Icon_Upgrade_Aim_Black.png");
	
	// Use a large enum variable to keep track of which icon each Mod or OC needs
	public enum drgIcons {
		aim
	};
	
	public static BufferedImage getModIcon(drgIcons iconSelection, boolean getBlackVersion) {
		switch (iconSelection) {
			case aim: {
				if (getBlackVersion) {
					return aimIconBlack;
				}
				else {
					return aimIcon;
				}
			}
			default: {
				return null;
			}
		}
	}
}
