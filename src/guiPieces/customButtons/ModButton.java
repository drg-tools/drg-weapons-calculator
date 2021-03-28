package guiPieces.customButtons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import guiPieces.CustomCursors;
import guiPieces.GuiConstants;
import guiPieces.HoverText;
import modelPieces.Mod;
import net.coobird.thumbnailator.Thumbnails;
import weapons.Weapon;

public class ModButton extends JButton implements MouseInputListener {
	private static final long serialVersionUID = 1L;
	
	private Weapon myWeapon;
	private Mod myMod;
	private BufferedImage icon;
	
	private Polygon border;
	
	public ModButton(Weapon inputWeapon, Mod thisMod) {
		myWeapon = inputWeapon;
		myMod = thisMod;
		icon = ButtonIcons.getModIcon(myMod.getIcon(), myMod.isSelected());
		
		border = createBackgroundHexagon();
		
		this.setText(myMod.getName());
		this.setFont(GuiConstants.customFont);
		this.setToolTipText(HoverText.breakLongToolTipString(myMod.getText(), 50));
		this.setOpaque(false);
		this.setContentAreaFilled(false);
		this.setBorderPainted(false);
		
		// Have this button listen to itself for all MouseEvents (click, drag, move, release, etc) so that it can perform actions without having to throw the events up to GuiController.
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
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
		border = createBackgroundHexagon();
		
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setFont(GuiConstants.customFont);
		g2.setStroke(new BasicStroke(GuiConstants.edgeWidth));
		
		// If this mod hasn't yet been implemented in the Weapon, draw its border red.
		if (myMod.isImplemented()) {
			g2.setPaint(GuiConstants.drgHighlightedYellow);
		}
		else {
			g2.setPaint(GuiConstants.drgOverclockUnstableRed);
		}
		g2.drawPolygon(border);
		
		// If this Mod isn't enabled, fill the background with black.
		if (myMod.isSelected()) {
			g2.setPaint(GuiConstants.drgHighlightedYellow);
		}
		else {
			g2.setPaint(Color.black);
		}
		g2.fillPolygon(border);
		
		// The icon and text have to be added at the same time since their position needs to be centered horizontally together
		// Set this number to dynamically scale the icons to be the same size in all the buttons
		double iconWidth = 32;
		double iconHeight = (double) icon.getHeight() * iconWidth / (double) icon.getWidth();
		int iconVerticalOffset = (int) Math.round((this.getHeight() - iconHeight) / 2.0);
		
		// Write with black text if enabled, or yellow text if not enabled
		if (myMod.isSelected()) {
			g2.setPaint(Color.black);
		}
		else {
			g2.setPaint(GuiConstants.drgHighlightedYellow);
		}
		String myText = this.getText();
		int textWidth = g2.getFontMetrics().stringWidth(myText);
		int textVerticalOffset = (int) Math.round((this.getHeight() + GuiConstants.fontHeight) / 2.0);
		
		int textHorizontalOffset = (this.getWidth() - textWidth + (int) iconWidth) / 2;
		int iconHorizontalOffset = textHorizontalOffset - GuiConstants.paddingPixels - (int) iconWidth;
		
		BufferedImage resizedIcon = icon;
		try {
			resizedIcon = Thumbnails.of(resizedIcon).size((int) (iconWidth), (int) (iconHeight)).asBufferedImage();
		}
		catch (IOException e) {}
		
		g2.drawImage(resizedIcon, iconHorizontalOffset, iconVerticalOffset, (int) (iconWidth), (int) (iconHeight), null);
		g2.drawString(myText, textHorizontalOffset, textVerticalOffset);
		
		// Paint this with a translucent red when it's not eligible for Best Combinations (Subset)
		if (myMod.isIgnored()) {
			Color translucentRed = new Color(156.0f/255.0f, 20.0f/255.0f, 20.0f/255.0f, 0.5f);
			g2.setPaint(translucentRed);
			g2.fill(border);
		}
		
		g2.dispose();
	}
	
	@Override
	public JToolTip createToolTip() {
		return new HoverText(this);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Point cursorHotspotLocation = e.getPoint();
		
		if (cursorHotspotLocation != null && border.contains(cursorHotspotLocation)) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				myWeapon.setSelectedModAtTier(myMod.getTier(), myMod.getIndex(), true);
			}
			else if (SwingUtilities.isRightMouseButton(e)) {
				myWeapon.setIgnoredModAtTier(myMod.getTier(), myMod.getIndex());
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// Do nothing if mouse is held down
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// Whenever a user drag-clicks across a button, it should just register like a normal click
		Point cursorHotspotLocation = e.getPoint();
		
		if (cursorHotspotLocation != null && border.contains(cursorHotspotLocation)) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				myWeapon.setSelectedModAtTier(myMod.getTier(), myMod.getIndex(), true);
			}
			else if (SwingUtilities.isRightMouseButton(e)) {
				myWeapon.setIgnoredModAtTier(myMod.getTier(), myMod.getIndex());
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// Do nothing if mouse enters boundaries
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// Do nothing if mouse leaves button boundaries
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// Do nothing if it's dragged
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		Point cursorHotspotLocation = e.getPoint();
		
		if (cursorHotspotLocation != null && border.contains(cursorHotspotLocation)) {
			this.setCursor(CustomCursors.defaultCursorPlusQuestionMark);
		}
		else {
			this.setCursor(CustomCursors.defaultCursor);
		}
	}
}
