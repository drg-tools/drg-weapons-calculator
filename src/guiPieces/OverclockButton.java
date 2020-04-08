package guiPieces;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;
import javax.swing.JToolTip;

import modelPieces.Overclock;
import modelPieces.Weapon;

public class OverclockButton extends JButton implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private Weapon myWeapon;
	private int myIndex;
	private Overclock.classification overclockType;
	private boolean enabled;
	private boolean implemented;
	
	public OverclockButton(Weapon inputWeapon, int index, String ocName, String ocText, boolean overclockSelected, boolean ocImplemented) {
		myWeapon = inputWeapon;
		myIndex = index;
		overclockType = myWeapon.getOverclocks()[myIndex].getType();
		enabled = overclockSelected;
		implemented = ocImplemented;
		
		this.setText(ocName);
		this.setToolTipText(HoverText.breakLongToolTipString(ocText, 50));
		this.setOpaque(false);
		this.setContentAreaFilled(false);
		this.setBorderPainted(false);
		
		// Have each OverclockButton listen to itself for when it gets clicked to simplify the GuiController
		this.addActionListener(this);
	}
	
	private Polygon createCleanHexagon(int centerX, int centerY, int radius) {
		// First create the square that will contain this Polygon
		// Squish it a little vertically to get the proportions right
		double squish = 0.78/0.88;
		int topBound = (int) Math.round(centerY - (radius * squish));
		int bottomBound = (int) Math.round(centerY + (radius * squish));
		int leftBound = centerX - radius;
		int rightBound = centerX + radius;
		
		int topOffset = (int) Math.round(16.0 * (rightBound - leftBound) / 88.0);
		int bottomOffset = (int) Math.round(28.0 * (rightBound - leftBound) / 88.0);
		int verticalOffset = (int) Math.round(28.0 * (bottomBound - topBound) / 78.0);
		
		Polygon toReturn = new Polygon();
		toReturn.addPoint(leftBound + topOffset, topBound);
		toReturn.addPoint(rightBound - topOffset, topBound);
		toReturn.addPoint(rightBound, topBound + verticalOffset);
		toReturn.addPoint(rightBound - bottomOffset, bottomBound);
		toReturn.addPoint(leftBound + bottomOffset, bottomBound);
		toReturn.addPoint(leftBound, topBound + verticalOffset);
		
		return toReturn;
	}
	
	private Polygon createBalancedHexagon(int centerX, int centerY, int radius) {
		int topBound = centerY - radius;
		int bottomBound = centerY + radius;
		int leftBound = centerX - radius;
		int rightBound = centerX + radius;
		
		int quarterVerticalDistance = (int) Math.round((bottomBound - topBound) / 4.0);
		int halfHorizontalDistance = (int) Math.round((rightBound - leftBound) / 2.0);
		
		Polygon toReturn = new Polygon();
		toReturn.addPoint(leftBound + halfHorizontalDistance, topBound);
		toReturn.addPoint(rightBound, topBound + quarterVerticalDistance);
		toReturn.addPoint(rightBound, bottomBound - quarterVerticalDistance);
		toReturn.addPoint(leftBound + halfHorizontalDistance, bottomBound);
		toReturn.addPoint(leftBound, bottomBound - quarterVerticalDistance);
		toReturn.addPoint(leftBound, topBound + quarterVerticalDistance);
		
		return toReturn;
	}
	
	private Polygon createUnstableSquare(int centerX, int centerY, int radius) {
		// Because it goes to the full corners of the bounds, the square looks bigger than the hexagons. Reduce the radius by 10%.
		radius = (int) Math.round(radius*0.9);
		int topBound = centerY - radius;
		int bottomBound = centerY + radius;
		int leftBound = centerX - radius;
		int rightBound = centerX + radius;
		
		Polygon toReturn = new Polygon();
		toReturn.addPoint(leftBound, topBound);
		toReturn.addPoint(rightBound, topBound);
		toReturn.addPoint(rightBound, bottomBound);
		toReturn.addPoint(leftBound, bottomBound);
		
		return toReturn;
	}
	
	private Polygon getBorderPolygon(Polygon icon, int centerX, int centerY) {
		// This method just draws a vector to each point in the input Polygon from the center, then adds a small percentage onto the 
		// vector length and gets a slightly larger Polygon as a result
		
		Polygon toReturn = new Polygon();
		int currentX, currentY, newX, newY;
		double vectorLength, angle, vectorX, vectorY, yDirection;
		double sizeIncrease = 1.25;
		for (int i = 0; i < icon.npoints; i++) {
			currentX = icon.xpoints[i];
			currentY = icon.ypoints[i];
			vectorLength = Math.hypot((currentX - centerX), (currentY - centerY));  // Math.sqrt(Math.pow((currentX - centerX), 2) + Math.pow((currentY - centerY), 2));
			angle = Math.acos((currentX - centerX)/vectorLength);
			vectorLength = vectorLength * sizeIncrease;
			vectorX = vectorLength * Math.cos(angle);
			vectorY = vectorLength * Math.sin(angle);
			// Now that we have properly scaled X and Y components, they need to be aligned to match the same direction that the originals were.
			// Because we used arccos and cos, x will already be aligned correctly.
			yDirection = (currentY - centerY) / Math.abs(currentY - centerY);  // Should be either 1 or -1
			newX = (int) Math.round(centerX + vectorX);
			newY = (int) Math.round(centerY + yDirection * vectorY);
			toReturn.addPoint(newX, newY);
		}
		return toReturn;
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		
		int bufferPixels = GuiConstants.paddingPixels;
		RoundRectangle2D border = new RoundRectangle2D.Double(bufferPixels, bufferPixels, getWidth() - 2*bufferPixels, getHeight() - 2*bufferPixels, 50, 50);
		
		// If this overclock hasn't been implemented in the model, draw its border red.
		if (implemented) {
			g2.setPaint(GuiConstants.drgHighlightedYellow);
		}
		else {
			g2.setPaint(GuiConstants.drgOverclockUnstableRed);
		}
		g2.setStroke(new BasicStroke(GuiConstants.edgeWidth));
		g2.draw(border);
		
		// If this mod is currently selected, draw its interior as yellow.
		if (enabled) {
			g2.setPaint(GuiConstants.drgHighlightedYellow);
		}
		else {
			g2.setPaint(Color.black);
		}
		g2.fill(border);
		
		// The overclock type icon will take up the left third of the button, and the overclock name will take up the right two-thirds of the button
		// Calculate where the icon should be centered, and the largest "radius" that it can occupy as a "square"
		
		// Subtract 4*bufferPixels to account for the padding both outside and inside the outline, on both sides.
		int width = getWidth() - 4*bufferPixels;
		int height = getHeight() - 4*bufferPixels;

		int thirdWidth = (int) Math.round(width / 3.0);
		
		int iconCenterX = (int) Math.round((thirdWidth - 2*bufferPixels)/ 2.0);
		int iconCenterY = (int) Math.round(height / 2.0);
		int radius = Math.min(iconCenterX, iconCenterY);
		
		// Compress the radius a bit so that it doesn't look too big
		radius = (int) Math.round(radius*0.8);
		
		// Add back the bufferPixels that were subtracted to properly center the icon
		iconCenterX += 3*bufferPixels;
		iconCenterY += 2*bufferPixels;
		
		Polygon icon;
		Color iconColor;
		switch (overclockType) {
			case clean: {
				icon = createCleanHexagon(iconCenterX, iconCenterY, radius);
				iconColor = GuiConstants.drgOverclockCleanGreen;
				break;
			}
			case balanced: {
				icon = createBalancedHexagon(iconCenterX, iconCenterY, radius);
				iconColor = GuiConstants.drgOverclockBalancedYellow;
				break;
			}
			case unstable: {
				icon = createUnstableSquare(iconCenterX, iconCenterY, radius);				
				iconColor = GuiConstants.drgOverclockUnstableRed;
				break;
			}
			default: {
				icon = new Polygon();
				iconColor = Color.red;
				break;
			}
		}
		
		Polygon iconBorder = getBorderPolygon(icon, iconCenterX, iconCenterY);
		g2.setStroke(new BasicStroke(2));
		g2.setPaint(Color.black);
		g2.drawPolygon(iconBorder);
		g2.fillPolygon(iconBorder);
		
		g2.setStroke(new BasicStroke(8));
		g2.setPaint(iconColor);
		g2.drawPolygon(icon);
		
		// Set the font color
		if (enabled) {
			g2.setPaint(Color.black);
		}
		else {
			g2.setPaint(GuiConstants.drgHighlightedYellow);
		}
		g2.drawString(this.getText(), thirdWidth + 3*bufferPixels, (int) Math.round((this.getHeight() + GuiConstants.fontHeight) / 2.0));
		g2.dispose();
		
	}
	
	@Override
	public JToolTip createToolTip() {
		return new HoverText(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Because this button is only listening to itself, I'm skipping the standard "figure out what button got clicked" stuff.
		// When this changes, the underlying Weapon will trigger a refresh of the overall GUI due to the Observable/Observer dynamic
		myWeapon.setSelectedOverclock(myIndex);
	}

}
