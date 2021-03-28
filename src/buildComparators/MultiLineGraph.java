package buildComparators;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.swing.JPanel;

import guiPieces.GuiConstants;
import utilities.MathUtils;

// Adapted from guiPieces.accuracyEstimator.LineGraph
public class MultiLineGraph extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private int padding = 25;
	private int labelPadding = 25;
	private Color gridColor = new Color(200, 200, 200, 200);
	private static final Stroke GRAPH_STROKE = new BasicStroke(1.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
	private int pointWidth = 2;
	
	private double[][] dataToPlot;
	private int numLinesToPlot, numDataPointsPerLine;
	private double minX, maxX, minY, maxY;
	private int numXDivisions, numYDivisions;
	private Color[] lineColors;
	
	private String xUnit, yUnit;
	
	public MultiLineGraph(double mnX, double mxX, double numXIntervals, double mnY, double mxY, double numYIntervals, double[][] data, Color[] colorsForEachLine) {
		minX = mnX;
		maxX = mxX;
		numXDivisions = (int) Math.round(numXIntervals);
		minY = mnY;
		maxY = mxY;
		numYDivisions = (int) Math.round(numYIntervals);
		dataToPlot = data;
		numLinesToPlot = data.length;
		numDataPointsPerLine = data[0].length;
		lineColors = colorsForEachLine;
		
		xUnit = "";
		yUnit = "";
		
		this.setPreferredSize(new Dimension(400, 240));
		this.setOpaque(false);
	}
	
	public void setXUnit(String newUnit) {
		xUnit = newUnit;
	}
	
	public void setYUnit(String newUnit) {
		yUnit = newUnit;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		int i, j, x0, x1, y0, y1;
		
		// draw white background
		g2.setColor(Color.WHITE);
		g2.fillRect(padding + labelPadding, padding, getWidth() - (2 * padding) - labelPadding,
				getHeight() - 2 * padding - labelPadding);
		g2.setColor(Color.BLACK);

		FontMetrics metrics = g2.getFontMetrics();
		// create hatch marks and grid lines for y axis.
		for (i = 0; i < numYDivisions + 1; i++) {
			x0 = padding + labelPadding;
			x1 = pointWidth + padding + labelPadding;
			y0 = getHeight() - ((i * (getHeight() - padding * 2 - labelPadding)) / numYDivisions + padding + labelPadding);
			y1 = y0;

			g2.setColor(gridColor);
			g2.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y1);
			
			g2.setColor(GuiConstants.drgHighlightedYellow);
			String yLabel = MathUtils.round((maxY - minY) * i / (double) numYDivisions, 2) + yUnit;
			int labelWidth = metrics.stringWidth(yLabel);
			g2.setColor(GuiConstants.drgHighlightedYellow);
			g2.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
		}

		// Make gridlines on X-axis
		for (i = 0; i < numXDivisions + 1; i++) {
			x0 = i * (getWidth() - padding * 2 - labelPadding) / numXDivisions + padding + labelPadding;
			x1 = x0;
			y0 = getHeight() - padding - labelPadding;
			y1 = y0 - pointWidth;
	
			g2.setColor(gridColor);
			g2.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);
			
			g2.setColor(GuiConstants.drgHighlightedYellow);
			String xLabel = MathUtils.round(minX + (maxX - minX) * i / (double) numXDivisions, 2) + xUnit;
			int labelWidth = metrics.stringWidth(xLabel);
			g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
		}

		// create x and y axes
		g2.setColor(gridColor);
		g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, padding + labelPadding, padding);
		g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, getWidth() - padding, getHeight() - padding - labelPadding);
		
		// Draw the actual lines
		double averageYvalue1, averageYvalue2;
		for (i = 0; i < numLinesToPlot; i++) {
			g2.setColor(lineColors[i]);
			g2.setStroke(GRAPH_STROKE);
			for (j = 0; j < numDataPointsPerLine - 1; j++) {
				x0 = j * (getWidth() - padding * 2 - labelPadding) / (numDataPointsPerLine - 1) + padding + labelPadding;
				x1 = (j + 1) * (getWidth() - padding * 2 - labelPadding) / (numDataPointsPerLine - 1) + padding + labelPadding;
				// I'm choosing to use the average of the surrounding data points in order to smooth out some of the jagged lines
				if (j == 0) {
					// End point: no averaging.
					averageYvalue1 = dataToPlot[i][0];
					averageYvalue2 = (dataToPlot[i][0] + dataToPlot[i][1] + dataToPlot[i][2]) / 3.0;
				}
				else if (j == 1) {
					// 2nd from edge, average of immediate neighbors
					averageYvalue1 = (dataToPlot[i][0] + dataToPlot[i][1] + dataToPlot[i][2]) / 3.0;
					averageYvalue2 = (dataToPlot[i][0] + dataToPlot[i][1] + dataToPlot[i][2] + dataToPlot[i][3] + dataToPlot[i][4]) / 5.0;
				}
				else if (j == numDataPointsPerLine - 3) {
					// 3rd from edge, val2 can only use its immediate neighbors
					averageYvalue1 = (dataToPlot[i][j-2] + dataToPlot[i][j-1] + dataToPlot[i][j] + dataToPlot[i][j+1] + dataToPlot[i][j+2]) / 5.0;
					averageYvalue2 = (dataToPlot[i][numDataPointsPerLine - 3] + dataToPlot[i][numDataPointsPerLine - 2] + dataToPlot[i][numDataPointsPerLine - 1]) / 3.0;
				}
				else if (j == numDataPointsPerLine - 2) {
					// 2nd from edge, average of immediate neighbors
					averageYvalue1 = (dataToPlot[i][numDataPointsPerLine - 3] + dataToPlot[i][numDataPointsPerLine - 2] + dataToPlot[i][numDataPointsPerLine - 1]) / 3.0;
					averageYvalue2 = dataToPlot[i][numDataPointsPerLine - 1];
				}
				else {
					// Entirely inside, use average of neighbors 2 to left and 2 to right
					averageYvalue1 = (dataToPlot[i][j-2] + dataToPlot[i][j-1] + dataToPlot[i][j] + dataToPlot[i][j+1] + dataToPlot[i][j+2]) / 5.0;
					averageYvalue2 = (dataToPlot[i][j-1] + dataToPlot[i][j] + dataToPlot[i][j+1] + dataToPlot[i][j+2] + dataToPlot[i][j+3]) / 5.0;
				}
				y0 = (int) Math.round((1.0 - (averageYvalue1 - minY) / (maxY - minY)) * (getHeight() - 2*padding - labelPadding)) + padding;
				y1 = (int) Math.round((1.0 - (averageYvalue2 - minY) / (maxY - minY)) * (getHeight() - 2*padding - labelPadding)) + padding;
				
				g2.drawLine(x0, y0, x1, y1);
			}
		}
	}
}
