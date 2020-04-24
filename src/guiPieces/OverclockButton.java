package guiPieces;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JToolTip;

import guiPieces.ButtonIcons.overclockIcons;
import modelPieces.Overclock;
import modelPieces.Weapon;

public class OverclockButton extends JButton implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private Weapon myWeapon;
	private BufferedImage icon;
	private int myIndex;
	private Overclock.classification overclockType;
	private boolean enabled;
	private boolean implemented;
	
	public OverclockButton(Weapon inputWeapon, int index, String ocName, String ocText, overclockIcons iconSelector, boolean overclockSelected, boolean ocImplemented) {
		myWeapon = inputWeapon;
		myIndex = index;
		overclockType = myWeapon.getOverclocks()[myIndex].getType();
		enabled = overclockSelected;
		icon = ButtonIcons.getOverclockIcon(iconSelector);
		implemented = ocImplemented;
		
		this.setText(ocName);
		this.setFont(GuiConstants.customFont);
		this.setToolTipText(HoverText.breakLongToolTipString(ocText, 50));
		this.setOpaque(false);
		this.setContentAreaFilled(false);
		this.setBorderPainted(false);
		this.setCursor(CustomCursors.defaultCursorPlusQuestionMark);
		
		// Have each OverclockButton listen to itself for when it gets clicked to simplify the GuiController
		this.addActionListener(this);
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setFont(GuiConstants.customFont);
		
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
		
		// If this overclock is currently selected, draw its interior as yellow.
		if (enabled) {
			g2.setPaint(GuiConstants.drgHighlightedYellow);
		}
		else {
			g2.setPaint(Color.black);
		}
		g2.fill(border);
		
		// The overclock type icon will take up the left third of the button, and the overclock name will take up the right two-thirds of the button
		// Start by getting the correct frame
		BufferedImage frame;
		switch (overclockType) {
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
		g2.drawImage(frame, frameHorizontalOffset, frameVerticalOffset, (int) (frameWidth), (int) (frameHeight), null);
		
		double iconWidth = 31;
		double iconHeight = (double) icon.getHeight() * iconWidth / (double) icon.getWidth();
		int iconVerticalOffset = (int) Math.round((this.getHeight() - iconHeight) / 2.0);
		// There's a weird interaction with the Clean Frame that makes the centered icons look too low.
		if (overclockType == Overclock.classification.clean) {
			iconVerticalOffset -= 3;
		}
		int iconHorizontalOffset = frameHorizontalOffset + (int) Math.round((frameWidth - iconWidth) / 2.0);
		g2.drawImage(icon, iconHorizontalOffset, iconVerticalOffset, (int) (iconWidth), (int) (iconHeight), null);
		
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
