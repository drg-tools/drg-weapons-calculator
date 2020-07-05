package guiPieces;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JPanel;

import net.coobird.thumbnailator.Thumbnails;

public class StatsRowIconPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private BufferedImage myPic;
	private int sideLength;
	private int leftPad;
	
	public StatsRowIconPanel(BufferedImage iconToDisplay) {
		myPic = iconToDisplay;
		sideLength = 20;
		leftPad = GuiConstants.paddingPixels;
		
		this.setPreferredSize(new Dimension(leftPad + sideLength, this.getHeight()));
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		
		BufferedImage resizedIcon = myPic;
		try {
			resizedIcon = Thumbnails.of(resizedIcon).size(sideLength, sideLength).asBufferedImage();
		}
		catch (IOException e) {}
		
		g2.drawImage(resizedIcon, leftPad, 0, sideLength, sideLength, null);
		
		g2.dispose();
	}
}
