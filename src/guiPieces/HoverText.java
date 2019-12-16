package guiPieces;

import javax.swing.JComponent;
import javax.swing.JToolTip;

import utilities.GuiConstants;

// This class just overrides the default blue/grey ToolTip text for ModButtons and OverclockButtons
public class HoverText extends JToolTip {
	private static final long serialVersionUID = 1L;
	
	public HoverText(JComponent comp) {
		super();
		this.setComponent(comp);
		this.setBackground(GuiConstants.drgBackgroundBiege);
		this.setForeground(GuiConstants.drgRegularOrange);
		this.setBorder(GuiConstants.orangeLine);
	}
}
