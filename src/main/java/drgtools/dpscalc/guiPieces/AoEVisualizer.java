package drgtools.dpscalc.guiPieces;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

import javax.swing.JPanel;

import drgtools.dpscalc.utilities.MathUtils;
import drgtools.dpscalc.utilities.Point2D;

public class AoEVisualizer extends JPanel  {
	private static final long serialVersionUID = 1L;
	
	private double glyphidBodyRadius;
	private double glyphidBodyAndLegsRadius;
	private double aoeRadius;
	private ArrayList<Point2D> glyphidCenters;
	
	public AoEVisualizer(double bodyRadius, double bodyLegsRadius, double radius, ArrayList<Point2D> centers) {
		glyphidBodyRadius = bodyRadius;
		glyphidBodyAndLegsRadius = bodyLegsRadius;
		aoeRadius = radius;
		glyphidCenters = centers;
		
		this.setPreferredSize(new Dimension(600, 300));
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		
		// Calculate the proportions
		int panelWidth = this.getWidth();
		int panelHeight = this.getHeight();
		double estimatedWidthOfDrawing = 2.0 * (aoeRadius + 2.0 * glyphidBodyAndLegsRadius);
		double estimatedHeightOfDrawing = aoeRadius + 3.0 * glyphidBodyAndLegsRadius;
		
		int maxDrawingHeight, maxDrawingWidth, verticalOffset, horizontalOffset;
		double pixelsPerMeter;
		
		if (panelWidth > 2 * panelHeight) {
			maxDrawingHeight = panelHeight;
			maxDrawingWidth = 2 * panelHeight;
			verticalOffset = 0;
			horizontalOffset = (int) Math.round((panelWidth - maxDrawingWidth) / 2.0);
			pixelsPerMeter = maxDrawingHeight / estimatedHeightOfDrawing;
		}
		else {
			maxDrawingHeight = (int) Math.round(panelWidth / 2.0);
			maxDrawingWidth = panelWidth;
			verticalOffset = (int) Math.round((panelHeight - maxDrawingHeight) / 2.0);
			horizontalOffset = 0;
			pixelsPerMeter = maxDrawingWidth / estimatedWidthOfDrawing;
		}
		
		g2.setStroke(new BasicStroke(2));
		g2.setPaint(Color.black);
		
		// Draw the AoE radius semicircle
		double aoeCircleCenterX = horizontalOffset + maxDrawingWidth / 2.0;
		double aoeCircleCenterY = verticalOffset + maxDrawingHeight;
		Shape aoeCircle = new Ellipse2D.Double(aoeCircleCenterX - aoeRadius * pixelsPerMeter, aoeCircleCenterY - aoeRadius * pixelsPerMeter, 2.0 * aoeRadius * pixelsPerMeter, 2.0 * aoeRadius * pixelsPerMeter);
		
		g2.setStroke(new BasicStroke(2));
		g2.setPaint(Color.black);
		g2.draw(aoeCircle);
		g2.setPaint(Color.red);
		g2.fill(aoeCircle);
		
		// Iterate over the Point2D centers and draw circles to show Glyphids
		Shape glyphidCircle;
		int centerX, centerY;
		Color fillColor;
		for (Point2D center: glyphidCenters) {
			centerX = (int) Math.round(aoeCircleCenterX + center.x() * pixelsPerMeter);
			centerY = (int) Math.round(aoeCircleCenterY + center.y() * pixelsPerMeter);
			glyphidCircle = new Ellipse2D.Double(centerX - glyphidBodyAndLegsRadius * pixelsPerMeter, centerY - glyphidBodyAndLegsRadius * pixelsPerMeter, 2.0 * glyphidBodyAndLegsRadius * pixelsPerMeter, 2.0 * glyphidBodyAndLegsRadius * pixelsPerMeter);
			
			// Again, rounded to 2 decimal points
			if (MathUtils.round((center.vectorLength() - glyphidBodyAndLegsRadius), 2) < aoeRadius) {
				fillColor = Color.blue;
			}
			else {
				fillColor = Color.yellow;
			}
			
			g2.setPaint(Color.black);
			g2.draw(glyphidCircle);
			
			glyphidCircle = new Ellipse2D.Double(centerX - glyphidBodyRadius * pixelsPerMeter, centerY - glyphidBodyRadius * pixelsPerMeter, 2.0 * glyphidBodyRadius * pixelsPerMeter, 2.0 * glyphidBodyRadius * pixelsPerMeter);
			g2.draw(glyphidCircle);
			
			g2.setPaint(fillColor);
			g2.fill(glyphidCircle);
		}
		
		g2.dispose();
	}
	
	public String toString() {
		String toReturn = "";
		toReturn += String.format("%f %f %f ", glyphidBodyRadius, glyphidBodyAndLegsRadius, aoeRadius);
		
		toReturn += "[";
		for (Point2D center: glyphidCenters) {
			toReturn += center.toString() + ", ";
		}
		toReturn += "]";
		
		return toReturn;
	}
}
