package guiPieces;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JToolTip;

import modelPieces.Weapon;

public class ModButton extends JButton implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private Weapon myWeapon;
	private int myTier;
	private int myIndex;
	private boolean enabled;
	private boolean implemented;
	
	public ModButton(Weapon inputWeapon, int tier, int position, String modName, String modText, boolean modSelected, boolean modImplemented) {
		myWeapon = inputWeapon;
		myTier = tier;
		myIndex = position;
		enabled = modSelected;
		implemented = modImplemented;
		
		this.setText(modName);
		this.setToolTipText(modText);
		this.setOpaque(false);
		this.setContentAreaFilled(false);
		this.setBorderPainted(false);
		
		// Have each ModButton listen to itself for when it gets clicked to simplify the GuiController
		this.addActionListener(this);
	}
	
	private Polygon createBackgroundHexagon() {
		int w = this.getWidth() - 2*GuiConstants.paddingPixels;
		int h = this.getHeight() - GuiConstants.paddingPixels;
		int sixthWidth = (int) Math.round((double) w / 6.0);
		int halfHeight = (int) Math.round((double) this.getHeight() / 2.0);
		Polygon toReturn = new Polygon();
		toReturn.addPoint(sixthWidth + GuiConstants.paddingPixels, GuiConstants.paddingPixels);
		toReturn.addPoint(w - sixthWidth + GuiConstants.paddingPixels, GuiConstants.paddingPixels);
		toReturn.addPoint(w + GuiConstants.paddingPixels, halfHeight);
		toReturn.addPoint(w - sixthWidth + GuiConstants.paddingPixels, h);
		toReturn.addPoint(sixthWidth + GuiConstants.paddingPixels, h);
		toReturn.addPoint(GuiConstants.paddingPixels, halfHeight);
		return toReturn;
	}
	
	public void paintComponent(Graphics g) {
		Polygon p = createBackgroundHexagon();
		
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setStroke(new BasicStroke(GuiConstants.edgeWidth));
		
		// If this mod hasn't yet been implemented in the Weapon, draw its border red.
		if (implemented) {
			g2.setPaint(GuiConstants.drgHighlightedYellow);
		}
		else {
			g2.setPaint(GuiConstants.drgOverclockUnstableRed);
		}
		g2.drawPolygon(p);
		
		// If this Mod isn't enabled, fill the background with black.
		if (enabled) {
			g2.setPaint(GuiConstants.drgHighlightedYellow);
		}
		else {
			g2.setPaint(Color.black);
		}
		g2.fillPolygon(p);
		
		// Write with black text if enabled, or yellow text if not enabled
		if (enabled) {
			g2.setPaint(Color.black);
		}
		else {
			g2.setPaint(GuiConstants.drgHighlightedYellow);
		}
		String myText = this.getText();
		int textWidth = g2.getFontMetrics().stringWidth(myText);
		int horizontalTextDisplacement = (int) Math.round((double)(this.getWidth()-textWidth)/2.0);
		g2.drawString(myText, horizontalTextDisplacement, (int) Math.round((this.getHeight() + GuiConstants.fontHeight) / 2.0));
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
		myWeapon.setSelectedModAtTier(myTier, myIndex);
	}
}
