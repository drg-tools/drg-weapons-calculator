package guiPieces;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JToolTip;

import guiPieces.ButtonIcons.statusEffectIcons;
import modelPieces.Weapon;

public class StatusEffectButton extends JButton implements ActionListener  {
	private static final long serialVersionUID = 1L;
	
	private Weapon myWeapon;
	private BufferedImage icon;
	private int myIndex;
	private boolean enabled;
	
	public StatusEffectButton(Weapon inputWeapon, int effectIndex, String effectName, String effectText, statusEffectIcons iconSelector, boolean effectEnabled) {
		myWeapon = inputWeapon;
		myIndex = effectIndex;
		icon = ButtonIcons.getStatusEffectIcon(iconSelector);
		enabled = effectEnabled;
		
		this.setText(effectName);
		this.setFont(GuiConstants.customFont);
		this.setToolTipText(HoverText.breakLongToolTipString(effectText, 50));
		this.setOpaque(false);
		this.setContentAreaFilled(false);
		this.setBorderPainted(false);
		this.setCursor(CustomCursors.defaultCursorPlusQuestionMark);
		
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
	
	private Polygon createIconHexagon(int desiredHeightOfHexagonPixels, int horizontalOffset, int verticalOffset) {
		double tanThirty = Math.tan(Math.PI/6.0);
		double lengthOfEdges = Math.sqrt((Math.pow(desiredHeightOfHexagonPixels, 2.0) / 4.0) * (1.0 + Math.pow(tanThirty, 2.0)));
		double edgeOffset = desiredHeightOfHexagonPixels * tanThirty / 2.0;
		double calculatedWidth = lengthOfEdges + 2 * edgeOffset;
		
		int roundedEdgeLength = (int) Math.round(lengthOfEdges);
		int roundedWidth = (int) Math.round(calculatedWidth);
		int roundedOffset = (int) Math.round(edgeOffset);
		int halfHeight = (int) Math.round((double)desiredHeightOfHexagonPixels / 2.0);
		
		Polygon toReturn = new Polygon();
		toReturn.addPoint(horizontalOffset + roundedOffset, verticalOffset);
		toReturn.addPoint(horizontalOffset + roundedOffset + roundedEdgeLength, verticalOffset);
		toReturn.addPoint(horizontalOffset + roundedWidth, verticalOffset + halfHeight);
		toReturn.addPoint(horizontalOffset + roundedOffset + roundedEdgeLength, verticalOffset + desiredHeightOfHexagonPixels);
		toReturn.addPoint(horizontalOffset + roundedOffset, verticalOffset + desiredHeightOfHexagonPixels);
		toReturn.addPoint(horizontalOffset, verticalOffset + halfHeight);
		
		return toReturn;
	}
	
	public void paintComponent(Graphics g) {
		/*
			TODO: I'm not satisfied with how the icon hexagon turned out -- it draws fine but the Electricity icon doesn't look good with it 
			and I had to use a lot of "static offset" numbers instead of figuring out the actual relationship. I want to refactor it so that the 
			hexagon width gets set, and then the hexagon's height gets calculated, and finally the icon's height/width gets scaled accordingly.
			It's ok for now, but it should be done sooner rather than later.
		 */
		Polygon p = createBackgroundHexagon();
		
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setFont(GuiConstants.customFont);
		g2.setStroke(new BasicStroke(GuiConstants.edgeWidth));
		
		g2.setPaint(GuiConstants.drgHighlightedYellow);
		g2.drawPolygon(p);
		
		// If this Mod isn't enabled, fill the background with black.
		if (enabled) {
			g2.setPaint(GuiConstants.drgHighlightedYellow);
		}
		else {
			g2.setPaint(Color.black);
		}
		g2.fillPolygon(p);
		
		// The icon and text have to be added at the same time since their position needs to be centered horizontally together
		// Set this number to dynamically scale the icons to be the same size in all the buttons
		double iconWidth = 24;
		double iconHeight = (double) icon.getHeight() * iconWidth / (double) icon.getWidth();
		int iconVerticalOffset = (int) Math.round((this.getHeight() - iconHeight) / 2.0);
		
		String myText = this.getText();
		int textWidth = g2.getFontMetrics().stringWidth(myText);
		int textVerticalOffset = (int) Math.round((this.getHeight() + GuiConstants.fontHeight) / 2.0);
		
		int textHorizontalOffset = (this.getWidth() - textWidth + (int) iconWidth) / 2;
		int iconHorizontalOffset = textHorizontalOffset - GuiConstants.paddingPixels - (int) iconWidth - 4;
		
		// Draw a black regular hexagon around the colored status effect icons so that when the button gets pressed, the yellow background doesn't have bad contrast.
		p = createIconHexagon((int) iconHeight + 6, iconHorizontalOffset - 6, iconVerticalOffset - 3);
		g2.setPaint(Color.black);
		g2.fillPolygon(p);
				
		g2.drawImage(icon, iconHorizontalOffset, iconVerticalOffset, (int) (iconWidth), (int) (iconHeight), null);
		
		// Write with black text if enabled, or yellow text if not enabled
		if (enabled) {
			g2.setPaint(Color.black);
		}
		else {
			g2.setPaint(GuiConstants.drgHighlightedYellow);
		}
		g2.drawString(myText, textHorizontalOffset, textVerticalOffset);
		g2.dispose();
		
		
		
		/*
		String myText = this.getText();
		int textWidth = g2.getFontMetrics().stringWidth(myText);
		int textVerticalOffset = (int) Math.round((this.getHeight() + GuiConstants.fontHeight) / 2.0);
		int textHorizontalOffset = (this.getWidth() - textWidth) / 2;
		g2.drawString(myText, textHorizontalOffset, textVerticalOffset);
		g2.dispose();
		*/
	}
	
	@Override
	public JToolTip createToolTip() {
		return new HoverText(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Because this button is only listening to itself, I'm skipping the standard "figure out what button got clicked" stuff.
		// When this changes, the underlying Weapon will trigger a refresh of the overall GUI due to the Observable/Observer dynamic
		myWeapon.setStatusEffect(myIndex, !enabled);
	}
}
