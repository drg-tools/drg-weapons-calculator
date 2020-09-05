package guiPieces;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;

import javax.swing.JPanel;

public class AccuracyAnimation extends JPanel {
	private static final long serialVersionUID = 1L;

	private int framesPerSecond;
	private double refreshInterval;
	private double currentTime;
	private int spreadIndex;
	private int recoilIndex;
	
	private boolean drawGeneralAccuracy;
	private double duration;
	
	private Double[] spreadTimestamps;
	private HashMap<Double, Double> spreadValues;
	private double maxSpread;
	
	private Double[] recoilTimestamps;
	private HashMap<Double, Double> recoilValues;
	private double maxRecoil;
	
	public AccuracyAnimation(boolean generalAccuracy, double loopDuration, Double[] sT, HashMap<Double, Double> sKVP, double maxSpreadMeters, Double[] rT, HashMap<Double, Double> rKVP, double maxRecoilMeters) {
		framesPerSecond = 60;
		refreshInterval = 1.0 / ((int) framesPerSecond);
		currentTime = 0.0;
		spreadIndex = 0;
		recoilIndex = 0;
		
		drawGeneralAccuracy = generalAccuracy;
		duration = loopDuration;
		
		spreadTimestamps = sT;
		spreadValues = sKVP;
		maxSpread = maxSpreadMeters;
		
		recoilTimestamps = rT;
		recoilValues = rKVP;
		maxRecoil = maxRecoilMeters;
		
		this.setPreferredSize(new Dimension(300, 900));
	}
	
	@Override
    protected void paintComponent(Graphics g) {
		super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
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
        double maxMetersWidthDifference = 2.0 * maxSpread;
        // I want the max-width crosshair circle to take up 75% width at most, to leave a 12.5% buffer on either side
        double pixelToMeterRatio = 0.75 * width / maxMetersWidthDifference;
        
        // Check to make sure that the height pixels are contained within 90%. If not, downscale the ratio even more.
        if (maxMetersHeightDifference * pixelToMeterRatio > 0.9 * height) {
        	pixelToMeterRatio = 0.9 * height / maxMetersHeightDifference;
        }
        
        int verticalPadding = (int) Math.round(0.05 * height);
        
        /* 
	    	Step 2: draw the target circle at the bottom, and paint it blue
	    */
        int circlesCenterX = (int) Math.round(width / 2.0);
        int drawTargetCenterY = (int) (height - verticalPadding - Math.round(Math.max(maxSpread, targetRadius) * pixelToMeterRatio));
        int drawTargetRadius = (int) Math.round(targetRadius * pixelToMeterRatio);
        
        g2.setPaint(Color.black);
        g2.drawOval(circlesCenterX - drawTargetRadius, drawTargetCenterY - drawTargetRadius, 2*drawTargetRadius, 2*drawTargetRadius);  // Outline
        Color targetBlue = new Color(0.0f, 0.0f, 1.0f, 0.75f);;
        g2.setPaint(targetBlue);
        g2.fillOval(circlesCenterX - drawTargetRadius, drawTargetCenterY - drawTargetRadius, 2*drawTargetRadius, 2*drawTargetRadius);  // Color interior
        
        /*
        	Step 3: use linear interpolation and currentTime to calculate the correct crosshair radius and center displacement
        	
        	This only works if spreadIndex and recoilIndex are NOT the last indexes of their respective arrays!
        */
        double currentTimestamp = spreadTimestamps[spreadIndex];
        double nextTimestamp = spreadTimestamps[spreadIndex + 1];
        double proportionOfTimeElapsed = (currentTime - currentTimestamp) / (nextTimestamp - currentTimestamp);
        double oldValue = spreadValues.get(currentTimestamp);
        double newValue = spreadValues.get(nextTimestamp);
        double interpolatedSpreadValue = oldValue + proportionOfTimeElapsed * (newValue - oldValue);
        
        currentTimestamp = recoilTimestamps[recoilIndex];
        nextTimestamp = recoilTimestamps[recoilIndex + 1];
        proportionOfTimeElapsed = (currentTime - currentTimestamp) / (nextTimestamp - currentTimestamp);
        oldValue = recoilValues.get(currentTimestamp);
        newValue = recoilValues.get(nextTimestamp);
        double interpolatedRecoilValue = oldValue + proportionOfTimeElapsed * (newValue - oldValue);
        
        /*
    		Step 4: draw the crosshair circle and paint it yellow
        */
        int drawCrosshairCenterY = drawTargetCenterY - (int) (Math.round(interpolatedRecoilValue * pixelToMeterRatio));
        int drawCrosshairRadius = (int) Math.round(interpolatedSpreadValue * pixelToMeterRatio);
        
        g2.setPaint(Color.black);
        g2.drawOval(circlesCenterX - drawCrosshairRadius, drawCrosshairCenterY - drawCrosshairRadius, 2*drawCrosshairRadius, 2*drawCrosshairRadius);  // Outline
        Color crosshairYellow = new Color(1.0f, 1.0f, 0.0f, 0.75f);
        g2.setPaint(crosshairYellow);
        g2.fillOval(circlesCenterX - drawCrosshairRadius, drawCrosshairCenterY - drawCrosshairRadius, 2*drawCrosshairRadius, 2*drawCrosshairRadius);  // Color interior
        
        /*
    		Step 5: paint the potential overlap of the two circles green
        */
	}
}
