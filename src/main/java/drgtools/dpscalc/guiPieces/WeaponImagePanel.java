package drgtools.dpscalc.guiPieces;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class WeaponImagePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private BufferedImage myPic;
	
	public WeaponImagePanel(BufferedImage weaponPicToDisplay) {
		myPic = weaponPicToDisplay;
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		
		// Scale the image to take up 80% of the available panel width
		int width = (int) Math.round(this.getWidth() * 0.8);
		int height = (int) Math.round(myPic.getHeight() * width / myPic.getWidth());
		int horizontalOffset = (int) Math.round(this.getWidth() * 0.1);
		int verticalOffset = (this.getHeight() - height) / 2;
		g2.drawImage(myPic, horizontalOffset, verticalOffset, width, height, null);
		
		g2.dispose();
	}
}
