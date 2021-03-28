package guiPieces.customButtons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import guiPieces.CustomCursors;
import guiPieces.GuiConstants;
import guiPieces.HoverText;
import modelPieces.Overclock;
import net.coobird.thumbnailator.Thumbnails;
import weapons.Weapon;

public class OverclockButton extends JButton implements MouseInputListener {
	private static final long serialVersionUID = 1L;
	
	private Weapon myWeapon;
	private Overclock myOC;
	private BufferedImage icon;
	
	private RoundRectangle2D border;
	
	public OverclockButton(Weapon inputWeapon, Overclock thisOC) {
		myWeapon = inputWeapon;
		myOC = thisOC;
		icon = ButtonIcons.getOverclockIcon(myOC.getIcon());
		
		int bufferPixels = GuiConstants.paddingPixels;
		border = new RoundRectangle2D.Double(bufferPixels, bufferPixels, getWidth() - 2*bufferPixels, getHeight() - 2*bufferPixels, 50, 50);
		
		this.setText(myOC.getName());
		this.setFont(GuiConstants.customFont);
		this.setToolTipText(HoverText.breakLongToolTipString(myOC.getText(), 50));
		this.setOpaque(false);
		this.setContentAreaFilled(false);
		this.setBorderPainted(false);
		
		// Have this button listen to itself for all MouseEvents (click, drag, move, release, etc) so that it can perform actions without having to throw the events up to GuiController.
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setFont(GuiConstants.customFont);
		
		int bufferPixels = GuiConstants.paddingPixels;
		border = new RoundRectangle2D.Double(bufferPixels, bufferPixels, getWidth() - 2*bufferPixels, getHeight() - 2*bufferPixels, 50, 50);
		
		// If this overclock hasn't been implemented in the model, draw its border red.
		if (myOC.isImplemented()) {
			g2.setPaint(GuiConstants.drgHighlightedYellow);
		}
		else {
			g2.setPaint(GuiConstants.drgOverclockUnstableRed);
		}
		g2.setStroke(new BasicStroke(GuiConstants.edgeWidth));
		g2.draw(border);
		
		// If this overclock is currently selected, draw its interior as yellow.
		if (myOC.isSelected()) {
			g2.setPaint(GuiConstants.drgHighlightedYellow);
		}
		else {
			g2.setPaint(Color.black);
		}
		g2.fill(border);
		
		// The overclock type icon will take up the left third of the button, and the overclock name will take up the right two-thirds of the button
		// Start by getting the correct frame
		BufferedImage frame;
		switch (myOC.getType()) {
			case clean: {
				frame = ButtonIcons.cleanFrame;
				break;
			}
			case balanced: {
				frame = ButtonIcons.balancedFrame;		
				break;
			}
			case unstable: {
				frame = ButtonIcons.unstableFrame;
				break;
			}
			default: {
				frame = null;
				break;
			}
		}
		
		// Draw the Frame in the left-hand third, and then draw the icon inside the frame.
		double frameWidth = 66;
		double frameHeight = (double) frame.getHeight() * frameWidth / (double) frame.getWidth();
		int frameVerticalOffset = (int) Math.round((this.getHeight() - frameHeight) / 2.0);
		
		// Subtract 4*bufferPixels to account for the padding both outside and inside the outline, on both sides.
		int width = getWidth() - 4*bufferPixels;
		int thirdWidth = (int) Math.round(width / 3.0);
		int frameHorizontalOffset = thirdWidth - (int) frameWidth;
		
		BufferedImage resizedFrame = frame;
		try {
			resizedFrame = Thumbnails.of(resizedFrame).size((int) (frameWidth), (int) (frameHeight)).asBufferedImage();
		}
		catch (IOException e) {}
		
		g2.drawImage(resizedFrame, frameHorizontalOffset, frameVerticalOffset, (int) (frameWidth), (int) (frameHeight), null);
		
		double iconWidth = 31;
		double iconHeight = (double) icon.getHeight() * iconWidth / (double) icon.getWidth();
		int iconVerticalOffset = (int) Math.round((this.getHeight() - iconHeight) / 2.0);
		// There's a weird interaction with the Clean Frame that makes the centered icons look too low.
		if (myOC.getType() == Overclock.classification.clean) {
			iconVerticalOffset -= 3;
		}
		// I'm also choosing to move RollControl down a little bit
		if (myOC.getIcon() == ButtonIcons.overclockIcons.rollControl) {
			iconVerticalOffset += 2;
		}
		int iconHorizontalOffset = frameHorizontalOffset + (int) Math.round((frameWidth - iconWidth) / 2.0);
		// The Damage skull and Ricochet icons are a little to the right; I'm going to move them to the left a little bit.
		if (myOC.getIcon() == ButtonIcons.overclockIcons.directDamage || myOC.getIcon() == ButtonIcons.overclockIcons.ricochet) {
			iconHorizontalOffset -= 1;
		}
		
		BufferedImage resizedIcon = icon;
		try {
			resizedIcon = Thumbnails.of(resizedIcon).size((int) (iconWidth), (int) (iconHeight)).asBufferedImage();
		}
		catch (IOException e) {}
		
		g2.drawImage(resizedIcon, iconHorizontalOffset, iconVerticalOffset, (int) (iconWidth), (int) (iconHeight), null);
		
		// Set the font color
		if (myOC.isSelected()) {
			g2.setPaint(Color.black);
		}
		else {
			g2.setPaint(GuiConstants.drgHighlightedYellow);
		}
		g2.drawString(this.getText(), thirdWidth + 3*bufferPixels, (int) Math.round((this.getHeight() + GuiConstants.fontHeight) / 2.0));
		
		// Paint this with a translucent red when it's not eligible for Best Combinations (Subset)
		if (myOC.isIgnored()) {
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
				myWeapon.setSelectedOverclock(myOC.getIndex(), true);
			}
			else if (SwingUtilities.isRightMouseButton(e)) {
				myWeapon.setIgnoredOverclock(myOC.getIndex());
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
				myWeapon.setSelectedOverclock(myOC.getIndex(), true);
			}
			else if (SwingUtilities.isRightMouseButton(e)) {
				myWeapon.setIgnoredOverclock(myOC.getIndex());
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
