package guiPieces;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class StatsRowIconPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private BufferedImage myPic;
	private int sideLength;
	private int leftPad;
	
	public StatsRowIconPanel(BufferedImage iconToDisplay) {
		myPic = iconToDisplay;
		sideLength = 20;
		leftPad = GuiConstants.paddingPixels;
		
		System.out.println(this.getWidth() + ", " + this.getHeight());
		this.setPreferredSize(new Dimension(leftPad + sideLength, this.getHeight()));
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		
		g2.drawImage(myPic, leftPad, 0, sideLength, sideLength, null);
		
		g2.dispose();
	}
}
