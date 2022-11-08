package drgtools.dpscalc.guiPieces;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.coobird.thumbnailator.Thumbnails;

public class StatsRowIconPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private BufferedImage myPic;
	private int sideLength;
	
	public StatsRowIconPanel(BufferedImage iconToDisplay) {
		myPic = iconToDisplay;
		sideLength = 20;
		
		/*
			This is easily the MOST STUPID fix I have ever had to implement for this program. Because this JPanel had no content, it would auto-set its width to 0px.
			I spent hours fighting with BorderLayout, GridbagLayout, and more. Ultimately I just made an empty JLabel with the right number of spaces to display the icons.
			This is SO DUMB. Oh well, whatever works...
		*/
		this.add(new JLabel("      "));
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		
		int iconVerticalOffset = (int) Math.round((this.getHeight() - sideLength) / 2.0);
		
		BufferedImage resizedIcon = myPic;
		try {
			resizedIcon = Thumbnails.of(resizedIcon).size(sideLength, sideLength).asBufferedImage();
		}
		catch (IOException e) {}
		
		g2.drawImage(resizedIcon, 0, iconVerticalOffset, sideLength, sideLength, null);
		
		g2.dispose();
	}
}
