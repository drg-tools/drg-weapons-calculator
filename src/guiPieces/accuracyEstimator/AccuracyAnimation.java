package guiPieces.accuracyEstimator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;

import javax.swing.JPanel;

import utilities.Point2D;

public class AccuracyAnimation extends JPanel implements Runnable {
	private static final long serialVersionUID = 1L;

	private double framesPerSecond;
	private long refreshInterval;
	
	private double currentTime;
	private int dataIndex;
	
	private boolean drawGeneralAccuracy;
	private double duration;
	
	private ArrayList<Point2D> spreadValues;
	private double maxSpread;
	
	private ArrayList<Point2D> recoilValues;
	private double maxRecoil;
	
	public AccuracyAnimation(boolean generalAccuracy, double loopDuration, ArrayList<Point2D> spreadMetersData, double maxSpreadMeters, ArrayList<Point2D> recoilMetersData, double maxRecoilMeters) {
		// This FPS should match the sampleRate in AccuracyEstimator
		framesPerSecond = 100;
		refreshInterval = (long) Math.round(1000.0 / framesPerSecond);
		
		currentTime = 0.0;
		dataIndex = 0;
		
		drawGeneralAccuracy = generalAccuracy;
		duration = loopDuration;
		
		spreadValues = spreadMetersData;
		maxSpread = maxSpreadMeters;
		
		recoilValues = recoilMetersData;
		maxRecoil = maxRecoilMeters;
		
		// Using this height to make it match the three linegraphs stacked on each other
		this.setPreferredSize(new Dimension(228, 684));
	}
	
	@Override
	public void run() {
		while (true) {
			repaint();
			
			// Now that the last frame has been displayed, update variables accordingly to make it animate.
			currentTime += 1.0 / framesPerSecond;
			if (currentTime >= duration) {
				currentTime = 0;
				dataIndex = 0;
			}
			else {
				dataIndex++;
			}
			
			try {
				Thread.sleep(refreshInterval);
			} 
			catch (Exception e) {
			
			}
		}
	}
	
	@Override
    protected void paintComponent(Graphics g) {
		super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // This custom compositor adds the two color values together when the circles overlap
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
        
        double maxMetersHeightDifference = Math.max(maxSpread, targetRadius) + maxRecoil + Math.max(maxSpread, targetRadius);
        double maxMetersWidthDifference = 2.0 * Math.max(maxSpread / 2.0, targetRadius);
        // I want the max-width crosshair circle to take up 75% width at most, to leave a 12.5% buffer on either side
        double pixelToMeterRatio = 0.75 * width / maxMetersWidthDifference;
        
        // Check to make sure that the height pixels are contained within 90%. If not, downscale the ratio even more.
        if (maxMetersHeightDifference * pixelToMeterRatio > 0.9 * height) {
        	pixelToMeterRatio = 0.9 * height / maxMetersHeightDifference;
        }
        
        int verticalPadding = (int) Math.round(0.05 * height);
        
        /* 
	    	Step 2: get the target circle ready to be drawn at the bottom
	    */
        int circlesCenterX = (int) Math.round(width / 2.0);
        int drawTargetCenterY = (int) (height - verticalPadding - Math.round(Math.max(maxSpread, targetRadius) * pixelToMeterRatio));
        int drawTargetRadius = (int) Math.round(targetRadius * pixelToMeterRatio);
        
        /*
        	Step 3: fetch the current crosshair radius and recoil offset values
        */
        double spreadValue = spreadValues.get(dataIndex).y();
        double recoilValue = recoilValues.get(dataIndex).y();
        
        /*
    		Step 4: determine crosshair size and location
        */
        int drawCrosshairCenterY = drawTargetCenterY - (int) (Math.round(recoilValue * pixelToMeterRatio));
        int drawCrosshairRadius = (int) Math.round(spreadValue * pixelToMeterRatio);
        
        /*
    		Step 5: draw the two circles, smaller one first (doing bigger first causes some aliasing where they overlap)
        */
        // TODO: I'm not satisfied with these colors, would like to change them before finalizing.
        Color target = new Color(200, 0, 0);
        Color crosshair = new Color(0, 0, 150);
        if (drawCrosshairRadius < drawTargetRadius) {
            g2.setColor(target);
            g2.fillOval(circlesCenterX - drawTargetRadius, drawTargetCenterY - drawTargetRadius, 2*drawTargetRadius, 2*drawTargetRadius);
            
            g2.setColor(crosshair);
            g2.fillOval(circlesCenterX - drawCrosshairRadius, drawCrosshairCenterY - drawCrosshairRadius, 2*drawCrosshairRadius, 2*drawCrosshairRadius);
        }
        else {
        	g2.setColor(crosshair);
            g2.fillOval(circlesCenterX - drawCrosshairRadius, drawCrosshairCenterY - drawCrosshairRadius, 2*drawCrosshairRadius, 2*drawCrosshairRadius);
            
        	g2.setColor(target);
            g2.fillOval(circlesCenterX - drawTargetRadius, drawTargetCenterY - drawTargetRadius, 2*drawTargetRadius, 2*drawTargetRadius);
        }
	}
}
