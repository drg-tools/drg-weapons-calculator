package guiPieces.accuracyEstimator;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import guiPieces.GuiConstants;
import utilities.MathUtils;
import utilities.Point2D;

// Adapted from this StackOverflow answer: https://stackoverflow.com/a/18413639
public class LineGraph extends JPanel implements Runnable {
	private static final long serialVersionUID = 1L;
	
	private int padding = 25;
	private int labelPadding = 25;
	private Color lineColor = new Color(44, 102, 230, 180);
	private Color gridColor = new Color(200, 200, 200, 200);
	private static final Stroke GRAPH_STROKE = new BasicStroke(1.8f);
	private int pointWidth = 2;
	private int numberYDivisions = 10;
	
	private ArrayList<Point2D> values;
	private double maxX, maxY;
	
	private double currentTime, framesPerSecond;
	private long refreshInterval;
	private boolean animate;
	
	public LineGraph(ArrayList<Point2D> v, double mX, double mY) {
		values = v;
		maxX = mX;
		maxY = mY;
		
		currentTime = 0.0;
		// This FPS should match the sampleRate in AccuracyEstimator
		framesPerSecond = 100;
		refreshInterval = (long) Math.round(1000.0 / framesPerSecond);
		animate = true;
		
		this.setPreferredSize(new Dimension(350, 210));
	}
	
	public void setGraphAnimation(boolean newValue) {
		animate = newValue;
	}
	
	@Override
	public void run() {
		while (animate) {
			repaint();
			
			// Now that the last frame has been displayed, update variables accordingly to make it animate.
			currentTime += 1.0 / framesPerSecond;
			if (currentTime >= maxX) {
				currentTime = 0;
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

        double xScale = ((double) getWidth() - 2 * padding - labelPadding) / (maxX);
        double yScale = ((double) getHeight() - 2 * padding - labelPadding) / (maxY);

        List<Point> graphPoints = new ArrayList<>();
        int i, x0, x1, x2, y0, y1, y2;
        for (i = 0; i < values.size(); i++) {
            x1 = (int) (values.get(i).x() * xScale + padding + labelPadding);
            y1 = (int) ((maxY - values.get(i).y()) * yScale + padding);
            graphPoints.add(new Point(x1, y1));
            
            // Special case: if the graph is supposed to extend beyond the last timestamp, add a Point at (maxX, last value) to draw at the end.
            if (i == values.size() - 1 && values.get(i).x() < maxX) {
            	x1 = (int) (maxX * xScale + padding + labelPadding);
            	graphPoints.add(new Point(x1, y1));
            }
        }

        // draw white background
        g2.setColor(Color.WHITE);
        g2.fillRect(padding + labelPadding, padding, getWidth() - (2 * padding) - labelPadding, getHeight() - 2 * padding - labelPadding);
        g2.setColor(Color.BLACK);

        FontMetrics metrics = g2.getFontMetrics();
        // create hatch marks and grid lines for y axis.
        for (i = 0; i < numberYDivisions + 1; i++) {
            x0 = padding + labelPadding;
            x1 = pointWidth + padding + labelPadding;
            y0 = getHeight() - ((i * (getHeight() - padding * 2 - labelPadding)) / numberYDivisions + padding + labelPadding);
            y1 = y0;
            
            g2.setColor(gridColor);
            g2.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y1);
            g2.setColor(Color.BLACK);
            String yLabel = MathUtils.round(maxY * (i / (double) numberYDivisions), 2) + "";
            int labelWidth = metrics.stringWidth(yLabel);
            g2.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
            g2.drawLine(x0, y0, x1, y1);
        }
        
        // Make gridlines on X-axis every 0.5 seconds
        int numXGridlines = (int) Math.floor(maxX / 0.5);
        if (numXGridlines > 0) {
	        double excessTime = maxX - numXGridlines * 0.5;
	        double proportionThatFitsGridlines = (maxX - excessTime) / maxX;
	        for (i = 1; i < numXGridlines + 1; i++) {
	            x0 = i * ((int) (getWidth()*proportionThatFitsGridlines) - padding * 2 - labelPadding) / numXGridlines + padding + labelPadding;
	            x1 = x0;
	            y0 = getHeight() - padding - labelPadding;
	            y1 = y0 - pointWidth;
	            
	            g2.setColor(gridColor);
	            g2.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);
	            g2.setColor(Color.BLACK);
	            String xLabel = i*0.5 + "";
	            int labelWidth = metrics.stringWidth(xLabel);
	            g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
	            g2.drawLine(x0, y0, x1, y1);
	        }
        }
        else {
        	// Special case: if there's less than 0.5 seconds of data to display, just add one tick at the far right with when this ends (Zhukovs' Recoil per Shot caused this)
        	x0 = ((int) getWidth() - padding * 2 - labelPadding) + padding + labelPadding;
        	x1 = x0;
        	y0 = getHeight() - padding - labelPadding;
            y1 = y0 - pointWidth;
            
            g2.setColor(gridColor);
            g2.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);
            g2.setColor(Color.BLACK);
            String xLabel = MathUtils.round(maxX, GuiConstants.numDecimalPlaces) + "";
            int labelWidth = metrics.stringWidth(xLabel);
            g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
            g2.drawLine(x0, y0, x1, y1);
        }
        
        if (animate) {
	        // Add a thin, red line that moves left-to-right to show the passage of time
	        x0 = (int) Math.round((getWidth() - padding * 2 - labelPadding) * currentTime / maxX) + padding + labelPadding;
	        x1 = x0;
	        y0 = getHeight() - padding - labelPadding;
	        y1 = padding;
	        g2.setColor(Color.red);
	        g2.drawLine(x0, y0, x1, y1);
        }

        // create x and y axes 
        g2.setColor(gridColor);
        g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, padding + labelPadding, padding);
        g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, getWidth() - padding, getHeight() - padding - labelPadding);

        g2.setColor(lineColor);
        g2.setStroke(GRAPH_STROKE);
        for (i = 0; i < graphPoints.size() - 1; i++) {
            x1 = graphPoints.get(i).x;
            y1 = graphPoints.get(i).y;
            x2 = graphPoints.get(i + 1).x;
            y2 = graphPoints.get(i + 1).y;
            g2.drawLine(x1, y1, x2, y2);
        }
    }
}
