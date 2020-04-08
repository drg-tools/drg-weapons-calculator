package guiPieces;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import modelPieces.Weapon;

public class AoEVisualizerButton extends JButton implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private Weapon toDisplay;

	public AoEVisualizerButton(String textToDisplay, Weapon weaponWithStats) {
		toDisplay = weaponWithStats;
		
		// Font color will be set by the parent WeaponTab, in constructCalculationsPanel()
		this.setBackground(GuiConstants.drgBackgroundBrown);
		this.setBorder(GuiConstants.orangeLine);
		
		this.setText(textToDisplay);
		this.setFont(GuiConstants.customFont);
		this.setHorizontalAlignment(SwingConstants.LEFT);
		this.addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JOptionPane.showMessageDialog(null, toDisplay.visualizeAoERadius(), "Visualization of how many Glyphid Grunts would be hit", JOptionPane.INFORMATION_MESSAGE);
	}
}
