package guiPieces.customButtons;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.SwingConstants;

import guiPieces.GuiConstants;
import weapons.Weapon;

public class DPSToggleButton extends JButton implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	private Weapon myWeapon;
	private int myIndex;
	private boolean currentlyEnabled;

	public DPSToggleButton(Weapon weaponWithStats, int toggleIndex, String leftPadding) {
		myWeapon = weaponWithStats;
		myIndex = toggleIndex;
		
		if (myIndex == 0) {
			// Toggle Weakpoints
			currentlyEnabled = myWeapon.getWeakpointDPSEnabled();
		}
		else if (myIndex == 1) {
			// Toggle General Accuracy
			currentlyEnabled = myWeapon.getAccuracyDPSEnabled();
		}
		else if (myIndex == 2) {
			// Toggle Armor Wasting
			currentlyEnabled = myWeapon.getArmorWastingDPSEnabled();
		}
		else {
			currentlyEnabled = false;
		}
		
		this.setBackground(GuiConstants.drgBackgroundBrown);
		this.setBorder(GuiConstants.orangeLine);
		
		if (currentlyEnabled) {
			this.setForeground(GuiConstants.drgOverclockCleanGreen);
			this.setText(leftPadding + "Enabled");
		}
		else {
			this.setForeground(GuiConstants.drgHighlightedYellow);
			this.setText(leftPadding + "Disabled");
		}
		
		this.setFont(GuiConstants.customFontBold);
		this.setHorizontalAlignment(SwingConstants.LEFT);
		this.addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (myIndex == 0) {
			// Toggle Weakpoints
			myWeapon.setWeakpointDPS(!currentlyEnabled, true);
		}
		else if (myIndex == 1) {
			// Toggle General Accuracy
			myWeapon.setAccuracyDPS(!currentlyEnabled, true);
		}
		else if (myIndex == 2) {
			// Toggle Armor Wasting
			myWeapon.setArmorWastingDPS(!currentlyEnabled, true);
		}
	}
}
