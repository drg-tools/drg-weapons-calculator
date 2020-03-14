package guiPieces;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import modelPieces.Mod;
import modelPieces.Overclock;
import modelPieces.StatsRow;
import modelPieces.Weapon;
import utilities.MathUtils;

public class WeaponTab extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private Weapon myWeapon;
	private Font customFont;
	private Font customFontBold;
	
	public WeaponTab(Weapon inputWeapon) {
		// Start by initializing the parent JPanel
		super();
		
		myWeapon = inputWeapon;
		customFont = GuiConstants.HKGrotesk();
		customFontBold = GuiConstants.HKGroteskBold();
		
		this.setOpaque(false);
		
		// Set up the percentage-based layout
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		this.setLayout(gbl);
		
		// Place the Stats panel so that it takes up the left third of the GUI
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.33;
		gbc.weighty = 0.75;
		gbc.gridwidth = 1;
		gbc.gridheight = 16;
		JPanel weaponStats = constructWeaponStatsPanel();
		gbl.setConstraints(weaponStats, gbc);
		this.add(weaponStats);
		
		// Place the Mods panel so that it takes up the top 60% of the right two thirds
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 0.67;
		gbc.weighty = 0.45;
		gbc.gridwidth = 2;
		gbc.gridheight = 9;
		JPanel weaponMods = constructModsPanel();
		gbl.setConstraints(weaponMods, gbc);
		this.add(weaponMods);
		
		// Place the Overclocks pane so that it takes up the bottom 40% of the right two thirds
		gbc.gridx = 1;
		gbc.gridy = 10;
		gbc.weightx = 0.67;
		gbc.weighty = 0.3;
		gbc.gridwidth = 2;
		gbc.gridheight = 6;
		JPanel weaponOverclocks = constructOverclocksPanel();
		gbl.setConstraints(weaponOverclocks, gbc);
		this.add(weaponOverclocks);
		
		// Place the calculated values in the bottom one-quarter
		gbc.gridx = 0;
		gbc.gridy = 16;
		gbc.weightx = 1.0;
		gbc.weighty = 0.25;
		gbc.gridwidth = 3;
		gbc.gridheight = 5;
		JPanel weaponCalculations = constructCalculationsPanel();
		gbl.setConstraints(weaponCalculations, gbc);
		this.add(weaponCalculations);
	}
	
	private JPanel constructWeaponStatsPanel() {
		StatsRow[] weaponStats = myWeapon.getStats();
		
		JPanel toReturn = new JPanel();
		toReturn.setBackground(GuiConstants.drgBackgroundBrown);
		toReturn.setBorder(GuiConstants.blackLine);
		toReturn.setLayout(new BoxLayout(toReturn, BoxLayout.Y_AXIS));
		
		JPanel row;
		JLabel statLabel, statValue;
		int paddingPixels = 2*GuiConstants.paddingPixels;
		for (int i = 0; i < weaponStats.length; i++) {
			if (weaponStats[i].shouldBeDisplayed()) {
				row = new JPanel();
				row.setOpaque(false);
				row.setLayout(new BorderLayout());
				
				statLabel = new JLabel(weaponStats[i].getName());
				// statLabel.setFont(customFont);
				statLabel.setForeground(Color.white);
				// Left-pad the label text
				statLabel.setBorder(new EmptyBorder(0, paddingPixels, 0, 0));
				row.add(statLabel, BorderLayout.LINE_START);
				
				statValue = new JLabel(weaponStats[i].getValue());
				// statValue.setFont(customFontBold);
				if (weaponStats[i].shouldValueBeHighlighted()) {
					statValue.setForeground(GuiConstants.drgHighlightedYellow);
				}
				else {
					statValue.setForeground(GuiConstants.drgRegularOrange);
				}
				// Right-pad the value text
				statValue.setBorder(new EmptyBorder(0, 0, 0, paddingPixels));
				row.add(statValue, BorderLayout.LINE_END);
				
				toReturn.add(row);
			}
		}
		
		return toReturn;
	}
	
	private JPanel constructModsPanel() {
		JPanel toReturn = new JPanel();
		toReturn.setBackground(GuiConstants.drgBackgroundBiege);
		toReturn.setBorder(GuiConstants.blackLine);
		toReturn.setLayout(new GridLayout(5, 3));
		
		Mod[] weaponMods;
		ModButton mb;
		int i, j;
		for (i = 1; i < 6; i++) {
			weaponMods = myWeapon.getModsAtTier(i);
			for (j = 0; j < weaponMods.length; j++) {
				mb = new ModButton(myWeapon, i, j, weaponMods[j].getName(), weaponMods[j].getText(), myWeapon.getSelectedModAtTier(i) == j, weaponMods[j].isImplemented());
				toReturn.add(mb);
			}
			// Check to see if there are only two mods at this tier. If so, add an empty JLabel
			if (weaponMods.length == 2) {
				toReturn.add(new JLabel());
			}
		}
		
		return toReturn;
	}
	
	private JPanel constructOverclocksPanel() {
		Overclock[] weaponOverclocks = myWeapon.getOverclocks();
		
		JPanel toReturn = new JPanel();
		toReturn.setBackground(GuiConstants.drgBackgroundBiege);
		toReturn.setBorder(GuiConstants.blackLine);
		
		// Separate the overclocks into two rows
		int rowWidth = (int) Math.ceil(weaponOverclocks.length / 2.0);
		toReturn.setLayout(new GridLayout(2, rowWidth));
		
		OverclockButton ocb;
		for (int i = 0; i < weaponOverclocks.length; i++) {
			ocb = new OverclockButton(myWeapon, i, weaponOverclocks[i].getName(), weaponOverclocks[i].getText(), myWeapon.getSelectedOverclock() == i, weaponOverclocks[i].isImplemented());
			toReturn.add(ocb);
		}
		
		// It's ok if there's an odd number of overclocks; the GridLayout will leave any slot as a transparent blank.
		
		return toReturn;
	}
	
	private JPanel constructCalculationsPanel() {
		JPanel toReturn = new JPanel();
		toReturn.setBackground(GuiConstants.drgBackgroundBrown);
		toReturn.setBorder(GuiConstants.blackLine);
		toReturn.setLayout(new GridLayout(4, 6));
		
		String[] headers = new String[] {
			"Ideal Burst DPS", 
			"Ideal Sustained DPS", 
			"Sustained DPS (+Weakpoints)", 
			"Sustained DPS (+Weakpoints, +Accuracy)", 
			"Ideal Additional Target DPS", 
			"Max Multi-Target Dmg", 
			"Max Num Targets", 
			"Firing Duration (sec)", 
			"Avg TTK (sec)", 
			"Avg Overkill", 
			"Accuracy", 
			"Utility"
		};
		
		int i;
		JLabel header, value;
		String roundedNumber;
		
		// Row 1
		for (i = 0; i < headers.length/2; i++) {
			header = new JLabel(headers[i]);
			header.setForeground(GuiConstants.drgRegularOrange);
			toReturn.add(header);
		}
		
		double[] originalStats = myWeapon.getBaselineStats();
		
		double idealBurstDPS = myWeapon.calculateIdealBurstDPS();
		roundedNumber = "" + MathUtils.round(idealBurstDPS, GuiConstants.numDecimalPlaces);
		value = new JLabel(roundedNumber);
		if (idealBurstDPS < originalStats[0]) {
			value.setForeground(GuiConstants.drgOverclockUnstableRed);
		}
		else if (idealBurstDPS > originalStats[0]) {
			value.setForeground(GuiConstants.drgOverclockCleanGreen);
		}
		else {
			// Implicitly means that they're equal
			value.setForeground(GuiConstants.drgHighlightedYellow);
		}
		toReturn.add(value);
		
		double idealSustainedDPS = myWeapon.calculateIdealSustainedDPS();
		roundedNumber = "" + MathUtils.round(idealSustainedDPS, GuiConstants.numDecimalPlaces);
		value = new JLabel(roundedNumber);
		if (idealSustainedDPS < originalStats[1]) {
			value.setForeground(GuiConstants.drgOverclockUnstableRed);
		}
		else if (idealSustainedDPS > originalStats[1]) {
			value.setForeground(GuiConstants.drgOverclockCleanGreen);
		}
		else {
			// Implicitly means that they're equal
			value.setForeground(GuiConstants.drgHighlightedYellow);
		}
		toReturn.add(value);
		
		double sustainedWeakpointDPS = myWeapon.sustainedWeakpointDPS();
		roundedNumber = "" + MathUtils.round(sustainedWeakpointDPS, GuiConstants.numDecimalPlaces);
		value = new JLabel(roundedNumber);
		if (sustainedWeakpointDPS < originalStats[2]) {
			value.setForeground(GuiConstants.drgOverclockUnstableRed);
		}
		else if (sustainedWeakpointDPS > originalStats[2]) {
			value.setForeground(GuiConstants.drgOverclockCleanGreen);
		}
		else {
			// Implicitly means that they're equal
			value.setForeground(GuiConstants.drgHighlightedYellow);
		}
		toReturn.add(value);
		
		double sustainedWeakpointAccuracyDPS = myWeapon.sustainedWeakpointAccuracyDPS();
		roundedNumber = "" + MathUtils.round(sustainedWeakpointAccuracyDPS, GuiConstants.numDecimalPlaces);
		value = new JLabel(roundedNumber);
		if (sustainedWeakpointAccuracyDPS < originalStats[3]) {
			value.setForeground(GuiConstants.drgOverclockUnstableRed);
		}
		else if (sustainedWeakpointAccuracyDPS > originalStats[3]) {
			value.setForeground(GuiConstants.drgOverclockCleanGreen);
		}
		else {
			// Implicitly means that they're equal
			value.setForeground(GuiConstants.drgHighlightedYellow);
		}
		toReturn.add(value);
		
		double additionalTargetDPS = myWeapon.calculateAdditionalTargetDPS();
		roundedNumber = "" + MathUtils.round(additionalTargetDPS, GuiConstants.numDecimalPlaces);
		value = new JLabel(roundedNumber);
		if (additionalTargetDPS < originalStats[4]) {
			value.setForeground(GuiConstants.drgOverclockUnstableRed);
		}
		else if (additionalTargetDPS > originalStats[4]) {
			value.setForeground(GuiConstants.drgOverclockCleanGreen);
		}
		else {
			// Implicitly means that they're equal
			value.setForeground(GuiConstants.drgHighlightedYellow);
		}
		toReturn.add(value);
		
		double maxMultiDmg = myWeapon.calculateMaxMultiTargetDamage();
		roundedNumber = "" + MathUtils.round(maxMultiDmg, GuiConstants.numDecimalPlaces);
		value = new JLabel(roundedNumber);
		if (maxMultiDmg < originalStats[5]) {
			value.setForeground(GuiConstants.drgOverclockUnstableRed);
		}
		else if (maxMultiDmg > originalStats[5]) {
			value.setForeground(GuiConstants.drgOverclockCleanGreen);
		}
		else {
			// Implicitly means that they're equal
			value.setForeground(GuiConstants.drgHighlightedYellow);
		}
		toReturn.add(value);
		
		// Row 2
		for (i = headers.length/2; i < headers.length; i++) {
			header = new JLabel(headers[i]);
			header.setForeground(GuiConstants.drgRegularOrange);
			toReturn.add(header);
		}
		
		int maxNumTargets = myWeapon.calculateMaxNumTargets();
		int originalNumTargets = (int) originalStats[6];
		if (myWeapon.currentlyDealsSplashDamage()) {
			AoEVisualizerButton valButton = new AoEVisualizerButton("    " + maxNumTargets, myWeapon);
			if (maxNumTargets < originalNumTargets) {
				valButton.setForeground(GuiConstants.drgOverclockUnstableRed);
			}
			else if (maxNumTargets > originalNumTargets) {
				valButton.setForeground(GuiConstants.drgOverclockCleanGreen);
			}
			else {
				// Implicitly means that they're equal
				valButton.setForeground(GuiConstants.drgHighlightedYellow);
			}
			toReturn.add(valButton);
		}
		else {
			value = new JLabel("" + maxNumTargets);
			if (maxNumTargets < originalNumTargets) {
				value.setForeground(GuiConstants.drgOverclockUnstableRed);
			}
			else if (maxNumTargets > originalNumTargets) {
				value.setForeground(GuiConstants.drgOverclockCleanGreen);
			}
			else {
				// Implicitly means that they're equal
				value.setForeground(GuiConstants.drgHighlightedYellow);
			}
			toReturn.add(value);
		}
		
		double firingDuration = myWeapon.calculateFiringDuration();
		roundedNumber = "" + MathUtils.round(firingDuration, GuiConstants.numDecimalPlaces);
		value = new JLabel(roundedNumber);
		if (firingDuration < originalStats[7]) {
			value.setForeground(GuiConstants.drgOverclockUnstableRed);
		}
		else if (firingDuration > originalStats[7]) {
			value.setForeground(GuiConstants.drgOverclockCleanGreen);
		}
		else {
			// Implicitly means that they're equal
			value.setForeground(GuiConstants.drgHighlightedYellow);
		}
		toReturn.add(value);
		
		double timeToKill = myWeapon.averageTimeToKill();
		roundedNumber = "" + MathUtils.round(timeToKill, GuiConstants.numDecimalPlaces);
		value = new JLabel(roundedNumber);
		if (timeToKill > originalStats[8]) {
			value.setForeground(GuiConstants.drgOverclockUnstableRed);
		}
		else if (timeToKill < originalStats[8]) {
			value.setForeground(GuiConstants.drgOverclockCleanGreen);
		}
		else {
			// Implicitly means that they're equal
			value.setForeground(GuiConstants.drgHighlightedYellow);
		}
		toReturn.add(value);
		
		double overkill = myWeapon.averageOverkill();
		roundedNumber = MathUtils.round(overkill, GuiConstants.numDecimalPlaces) + "%";
		value = new JLabel(roundedNumber);
		if (overkill > originalStats[9]) {
			value.setForeground(GuiConstants.drgOverclockUnstableRed);
		}
		else if (overkill < originalStats[9]) {
			value.setForeground(GuiConstants.drgOverclockCleanGreen);
		}
		else {
			// Implicitly means that they're equal
			value.setForeground(GuiConstants.drgHighlightedYellow);
		}
		toReturn.add(value);
		
		double accuracy = myWeapon.estimatedAccuracy();
		if (accuracy < 0) {
			value = new JLabel("Manually Aimed");
			value.setForeground(GuiConstants.drgHighlightedYellow);
		}
		else {
			roundedNumber = MathUtils.round(accuracy, GuiConstants.numDecimalPlaces) + "%";
			value = new JLabel(roundedNumber);
			if (accuracy < originalStats[10]) {
				value.setForeground(GuiConstants.drgOverclockUnstableRed);
			}
			else if (accuracy > originalStats[10]) {
				value.setForeground(GuiConstants.drgOverclockCleanGreen);
			}
			else {
				// Implicitly means that they're equal
				value.setForeground(GuiConstants.drgHighlightedYellow);
			}
		}
		toReturn.add(value);
		
		double utility = myWeapon.utilityScore();
		roundedNumber = "" + MathUtils.round(utility, GuiConstants.numDecimalPlaces);
		UtilityBreakdownButton utilButton = new UtilityBreakdownButton("    " + roundedNumber, myWeapon);
		if (utility < originalStats[11]) {
			utilButton.setForeground(GuiConstants.drgOverclockUnstableRed);
		}
		else if (utility > originalStats[11]) {
			utilButton.setForeground(GuiConstants.drgOverclockCleanGreen);
		}
		else {
			// Implicitly means that they're equal
			utilButton.setForeground(GuiConstants.drgHighlightedYellow);
		}
		toReturn.add(utilButton);
		
		return toReturn;
	}
}
