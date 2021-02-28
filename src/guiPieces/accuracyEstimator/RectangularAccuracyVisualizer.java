package guiPieces.accuracyEstimator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import utilities.MathUtils;

public class RectangularAccuracyVisualizer  extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private boolean drawGeneralAccuracy;
	private double crosshairWidth, crosshairHeight;
	
	public RectangularAccuracyVisualizer(boolean generalAccuracy, double widthMeters, double heightMeters) {
		drawGeneralAccuracy = generalAccuracy;
		// To be clear: these come into this class already at "half value" for easier comparison to the target radii as well as drawing the rectangle later.
		crosshairWidth = widthMeters;
		crosshairHeight = heightMeters;
		
		this.setPreferredSize(new Dimension(600, 400));
	}
	
	@Override
    protected void paintComponent(Graphics g) {
		super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // This custom compositor adds the two color values together when the shapes overlap
        g2.setComposite(new AdditiveComposite(getBackground()));
        
        double targetRadius;
        if (drawGeneralAccuracy) {
        	targetRadius = 0.4;
        }
        else {
        	targetRadius = 0.2;
        }
        
        /* 
        	Step 1: set up the scaling factors.
        */
        double width = getWidth();
        double height = getHeight();
        
        double maxMetersHeightDifference = 2.0 * Math.max(crosshairHeight, targetRadius);
        double maxMetersWidthDifference = 2.0 * Math.max(crosshairWidth, targetRadius);
        // I want the max-width to take up 75% width at most, to leave a 12.5% buffer on either side
        double pixelToMeterRatio = 0.75 * width / maxMetersWidthDifference;
        
        // Check to make sure that the height pixels are contained within 90%. If not, downscale the ratio even more.
        if (maxMetersHeightDifference * pixelToMeterRatio > 0.9 * height) {
        	pixelToMeterRatio = 0.9 * height / maxMetersHeightDifference;
        }
        
        /* 
	    	Step 2: get the target circle ready to be drawn
	    */
	    int centerX = (int) Math.round(width / 2.0);
	    int centerY = (int) Math.round(height / 2.0);
	    int drawTargetRadius = (int) Math.round(targetRadius * pixelToMeterRatio);
	    
	    /*
	    	Step 3: get the crosshair rectangle ready to be drawn
	    */
	    int crosshairDrawWidth = (int) Math.round(2.0 * crosshairWidth * pixelToMeterRatio);
	    int crosshairDrawHeight = (int) Math.round(2.0 * crosshairHeight * pixelToMeterRatio);

        /*
    		Step 4: draw the target circle and crosshair rectangle
        */
	    Color target = new Color(200, 0, 0);
        Color crosshair = new Color(0, 0, 150);

        g2.setColor(crosshair);
        g2.fillRect(centerX - crosshairDrawWidth / 2, centerY - crosshairDrawHeight / 2, crosshairDrawWidth, crosshairDrawHeight);
        
        g2.setColor(target);
        g2.fillOval(centerX - drawTargetRadius, centerY - drawTargetRadius, 2*drawTargetRadius, 2*drawTargetRadius);
        
        /* 
        	Step 5: overlay the rectangle with a black-to-white that visualizes the probability that a pellet will go there
        	
        	I want it to be at 0% opacity at the outermost corners, and 71.76% opacity in the center of the rectangle
        */
        double minimumProbability = Math.pow(MathUtils.probabilityInNormalDistribution(-2, 4, -2), 2);
        double maximumProbability = Math.pow(MathUtils.probabilityInNormalDistribution(-2, 4, 1), 2);
        
        BufferedImage probabilityOverlay = new BufferedImage(crosshairDrawWidth, crosshairDrawHeight, BufferedImage.TYPE_INT_ARGB);
        
        int i, j;
        double horizontalProbability, verticalProbability, totalProbability, probabilityScalar;
        
        for (i = 0; i < crosshairDrawWidth; i++) {
        	horizontalProbability = MathUtils.probabilityInNormalDistribution(0, crosshairDrawWidth - 1, i);
        	
        	for (j = 0; j < crosshairDrawHeight; j++) {
        		verticalProbability = MathUtils.probabilityInNormalDistribution(0, crosshairDrawHeight - 1, j);
        		totalProbability = horizontalProbability * verticalProbability;
        		probabilityScalar = (totalProbability - minimumProbability) / (maximumProbability - minimumProbability);
        		
        		probabilityOverlay.setRGB(i, j, new Color(255, 255, 255, (int) Math.round(183.0 * probabilityScalar)).getRGB());
        	}
        }
        
        g2.drawImage(probabilityOverlay, centerX - crosshairDrawWidth / 2, centerY - crosshairDrawHeight / 2, null);
	}
}
