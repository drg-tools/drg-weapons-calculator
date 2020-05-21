package guiPieces;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JToolTip;

import guiPieces.ButtonIcons.modIcons;
import modelPieces.Weapon;

public class ModButton extends JButton implements ActionListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	
	private Weapon myWeapon;
	private BufferedImage icon;
	private int myTier;
	private int myIndex;
	private boolean enabled;
	private boolean implemented;
	
	private Polygon border;
	
	public ModButton(Weapon inputWeapon, int tier, int position, String modName, String modText, modIcons iconSelector, boolean modSelected, boolean modImplemented) {
		myWeapon = inputWeapon;
		myTier = tier;
		myIndex = position;
		enabled = modSelected;
		icon = ButtonIcons.getModIcon(iconSelector, enabled);
		implemented = modImplemented;
		
		border = createBackgroundHexagon();
		
		this.setText(modName);
		this.setFont(GuiConstants.customFont);
		this.setToolTipText(HoverText.breakLongToolTipString(modText, 50));
		this.setOpaque(false);
		this.setContentAreaFilled(false);
		this.setBorderPainted(false);
		
		// Have each ModButton listen to itself for when it gets clicked to simplify the GuiController
		this.addActionListener(this);
		
		// Have this button listen to itself for Mouse Movement too to add the question mark to the cursor when within the border
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
		if (implemented) {
			g2.setPaint(GuiConstants.drgHighlightedYellow);
		}
		else {
			g2.setPaint(GuiConstants.drgOverclockUnstableRed);
		}
		g2.drawPolygon(border);
		
		// If this Mod isn't enabled, fill the background with black.
		if (enabled) {
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
		if (enabled) {
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
		
		g2.drawImage(icon, iconHorizontalOffset, iconVerticalOffset, (int) (iconWidth), (int) (iconHeight), null);
		g2.drawString(myText, textHorizontalOffset, textVerticalOffset);
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
