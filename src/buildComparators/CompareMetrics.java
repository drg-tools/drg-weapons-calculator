package buildComparators;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import guiPieces.GuiConstants;
import modelPieces.StatsRow;
import weapons.Weapon;

public class CompareMetrics extends Comparator {
	private JCheckBox enableWeakpoints, enableAccuracy, enableArmorWasting;
	private JLabel[][] outputMatrix;
	
	private JButton showStatsPanelsComparison, showBreakpointsComparison;
	private JPanel compareStatsPanels, compareBreakpoints;
	
	public CompareMetrics(Weapon toUse) {
		super(toUse);
	}
	
	@Override
	public JPanel getComparisonPanel() {
		JPanel toReturn = new JPanel();
		toReturn.setLayout(new BorderLayout());
		
		JPanel inputButtons = new JPanel();
		JPanel checkboxes = new JPanel();
		checkboxes.setLayout(new BoxLayout(checkboxes, BoxLayout.Y_AXIS));
		enableWeakpoints = new JCheckBox("Enable Weakpoints in DPS");
		checkboxes.add(enableWeakpoints);
		enableAccuracy = new JCheckBox("Enable Accuracy in DPS");
		checkboxes.add(enableAccuracy);
		enableArmorWasting = new JCheckBox("Enable Armor Wasting in DPS");
		checkboxes.add(enableArmorWasting);
		inputButtons.add(checkboxes);
		
		JPanel compareButtons = new JPanel();
		compareButtons.setLayout(new BoxLayout(compareButtons, BoxLayout.Y_AXIS));
		compareBuilds = new JButton("Compare all metrics");
		compareBuilds.addActionListener(this);
		compareButtons.add(compareBuilds);
		showStatsPanelsComparison = new JButton("Compare the Stat Panels");
		showStatsPanelsComparison.addActionListener(this);
		showStatsPanelsComparison.setEnabled(false);
		compareButtons.add(showStatsPanelsComparison);
		showBreakpointsComparison = new JButton("Compare Breakpoints");
		showBreakpointsComparison.addActionListener(this);
		showBreakpointsComparison.setEnabled(false);
		compareButtons.add(showBreakpointsComparison);
		inputButtons.add(compareButtons);
		
		toReturn.add(inputButtons, BorderLayout.NORTH);
		
		JPanel grid = new JPanel();
		grid.setLayout(new GridLayout(16, 5));
		grid.setBackground(GuiConstants.drgBackgroundBrown);
		
		// First row: empty space and four text fields that user can type builds into
		grid.add(new JLabel());
		
		buildInput1 = new JTextField(build1);
		buildInput1.setFont(new Font("Monospaced", Font.PLAIN, 18));
		grid.add(buildInput1);
		buildInput2 = new JTextField(build2);
		buildInput2.setFont(new Font("Monospaced", Font.PLAIN, 18));
		grid.add(buildInput2);
		buildInput3 = new JTextField(build3);
		buildInput3.setFont(new Font("Monospaced", Font.PLAIN, 18));
		grid.add(buildInput3);
		buildInput4 = new JTextField(build4);
		buildInput4.setFont(new Font("Monospaced", Font.PLAIN, 18));
		grid.add(buildInput4);
		
		// Rows 2-16: metrics
		JLabel[] metricNames = new JLabel[] {
			new JLabel("  Burst DPS:"),
			new JLabel("  Sustained DPS:"),
			new JLabel("  Additional Target DPS:"),
			new JLabel("  Max Num Targets:"),
			new JLabel("  Max Multi-Target Damage:"),
			new JLabel("  Ammo Efficiency:"),
			new JLabel("  % Damage Wasted by Armor:    "),
			new JLabel("  General Accuracy %:"),
			new JLabel("  Weakpoint Accuracy %:"),
			new JLabel("  Firing Duration:"),
			new JLabel("  Avg TTK:"),
			new JLabel("  Avg Overkill %:"),
			new JLabel("  Breakpoints:"),
			new JLabel("  Utility:"),
			new JLabel("  Avg Time to Ignite/Freeze:")
		};
		outputMatrix = new JLabel[15][4];
		for (int row = 0; row < 15; row++) {
			metricNames[row].setFont(GuiConstants.customFont);
			metricNames[row].setForeground(GuiConstants.drgRegularOrange);
			grid.add(metricNames[row]);
			
			outputMatrix[row] = new JLabel[4];
			for (int col = 0; col < 4; col++) {
				outputMatrix[row][col] = new JLabel();
				outputMatrix[row][col].setFont(GuiConstants.customFont);
				outputMatrix[row][col].setBorder(GuiConstants.orangeLine);
				// JLabels are transparent background, so the brown from underlying JPanel should show through
				// Font color will be set later when metrics get plugged in and compared
				grid.add(outputMatrix[row][col]);
			}
		}
		
		toReturn.add(grid, BorderLayout.CENTER);
		
		return toReturn;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object e = arg0.getSource();
		
		if (e == compareBuilds) {
			/*
				First, sanitize the 4 text inputs and return early if any non-empty fields fail validation.
			*/
			if (!areAllBuildsValid()) {
				return;
			}
		
			/*
				Second, squish the inputs all to the left-most columns to simplify logic later. Do nothing if fewer than two columns have a build.
			*/
			ArrayList<String> justifyLeft = new ArrayList<String>();
			justifyLeft.add(buildInput1.getText());
			justifyLeft.add(buildInput2.getText());
			justifyLeft.add(buildInput3.getText());
			justifyLeft.add(buildInput4.getText());
			
			int i = 0, j;
			while (i < justifyLeft.size()) {
				if (justifyLeft.get(i).length() == 0) {
					justifyLeft.remove(i);
				}
				else {
					i++;
				}
			}
			
			int numBuildsToCompare = justifyLeft.size();
			
			if (numBuildsToCompare < 2) {
				// Early exit: if fewer than 2 fields have been filled out when the button gets pressed, do nothing.
				return;
			}
			
			while (justifyLeft.size() < 4) {
				justifyLeft.add("");
			}
			build1 = justifyLeft.get(0);
			buildInput1.setText(build1);
			build2 = justifyLeft.get(1);
			buildInput2.setText(build2);
			build3 = justifyLeft.get(2);
			buildInput3.setText(build3);
			build4 = justifyLeft.get(3);
			buildInput4.setText(build4);
			
			/*
				Third, iterate through all 4 columns and get the metrics (and breakpoints) of each
			*/
			baseModel.setWeakpointDPS(enableWeakpoints.isSelected(), false);
			baseModel.setAccuracyDPS(enableAccuracy.isSelected(), false);
			baseModel.setArmorWastingDPS(enableArmorWasting.isSelected(), false);
			
			// Guaranteed to have at the left-most two columns at this point in the method
			double[][] metricsToCompare = new double[numBuildsToCompare][15];
			StatsRow[][] statsPanelsToCompare = new StatsRow[numBuildsToCompare][baseModel.getStats().length];
			StatsRow[][] breakpointsToCompare = new StatsRow[numBuildsToCompare][baseModel.breakpointsExplanation().length];
			for (i = 0; i < numBuildsToCompare; i++) {
				baseModel.buildFromCombination(justifyLeft.get(i), false);
				metricsToCompare[i] = new double[] {
					baseModel.calculateSingleTargetDPS(true),
					baseModel.calculateSingleTargetDPS(false),
					baseModel.calculateAdditionalTargetDPS(),
					baseModel.calculateMaxNumTargets(),
					baseModel.calculateMaxMultiTargetDamage(),
					baseModel.ammoEfficiency(),
					baseModel.damageWastedByArmor(),
					baseModel.getGeneralAccuracy(),
					baseModel.getWeakpointAccuracy(),
					baseModel.calculateFiringDuration(),
					baseModel.averageTimeToKill(),
					baseModel.averageOverkill(),
					baseModel.breakpoints(),
					baseModel.utilityScore(),
					baseModel.averageTimeToCauterize()
				};
				
				statsPanelsToCompare[i] = baseModel.getStats();
				breakpointsToCompare[i] = baseModel.breakpointsExplanation();
			}
			
			/*
				Fourth, build the Stats Panels and Breakpoints comparison matrices and enable the buttons for user to compare them
			*/
			compareStatsPanels = new JPanel();
			compareStatsPanels.setBackground(GuiConstants.drgBackgroundBrown);
			compareStatsPanels.setBorder(GuiConstants.blackLine);
			compareStatsPanels.setLayout(new GridLayout(statsPanelsToCompare[0].length + 1, numBuildsToCompare + 1));
			
			// Make the first row show the build Strings at the top of each column
			compareStatsPanels.add(new JLabel());
			JLabel statsBuildStrings;
			for (j = 0; j < numBuildsToCompare; j++) {
				statsBuildStrings = new JLabel(justifyLeft.get(j));
				statsBuildStrings.setFont(GuiConstants.customFontBold);
				statsBuildStrings.setForeground(GuiConstants.drgRegularOrange);
				compareStatsPanels.add(statsBuildStrings);
			}
			
			JLabel statName, statValue;
			for (i = 0; i < statsPanelsToCompare[0].length; i++) {
				statName = new JLabel(statsPanelsToCompare[0][i].getName() + "    ");
				statName.setFont(GuiConstants.customFont);
				statName.setForeground(Color.white);
				compareStatsPanels.add(statName);
				
				for (j = 0; j < numBuildsToCompare; j++) {
					if (statsPanelsToCompare[j][i].shouldBeDisplayed()) {
						statValue = new JLabel(statsPanelsToCompare[j][i].getValue());
					}
					else {
						statValue = new JLabel("N/A");
					}
					statValue.setFont(GuiConstants.customFont);
					statValue.setForeground(GuiConstants.drgRegularOrange);
					compareStatsPanels.add(statValue);
				}
			}
			
			compareBreakpoints = new JPanel();
			compareBreakpoints.setBackground(GuiConstants.drgBackgroundBrown);
			compareBreakpoints.setBorder(GuiConstants.blackLine);
			compareBreakpoints.setLayout(new GridLayout(breakpointsToCompare[0].length + 1, numBuildsToCompare + 1));
			
			// Make the first row show the build Strings at the top of each column
			compareBreakpoints.add(new JLabel());
			JLabel breakpointBuildStrings;
			for (j = 0; j < numBuildsToCompare; j++) {
				breakpointBuildStrings = new JLabel(justifyLeft.get(j));
				breakpointBuildStrings.setFont(GuiConstants.customFontBold);
				breakpointBuildStrings.setForeground(GuiConstants.drgRegularOrange);
				compareBreakpoints.add(breakpointBuildStrings);
			}
			
			JLabel breakpointName, breakpointValue;
			for (i = 0; i < breakpointsToCompare[0].length; i++) {
				breakpointName = new JLabel(breakpointsToCompare[0][i].getName());
				breakpointName.setFont(GuiConstants.customFont);
				breakpointName.setForeground(Color.white);
				compareBreakpoints.add(breakpointName);
				
				for (j = 0; j < numBuildsToCompare; j++) {
					breakpointValue = new JLabel(breakpointsToCompare[j][i].getValue());
					breakpointValue.setFont(GuiConstants.customFont);
					breakpointValue.setForeground(GuiConstants.drgRegularOrange);
					compareBreakpoints.add(breakpointValue);
				}
			}
			
			// Now that content can be shown, enable these buttons.
			showStatsPanelsComparison.setEnabled(true);
			// Don't let users compare Breakpoints for weapons that don't calculate Breakpoints
			if (metricsToCompare[0][12] > 0) {
				showBreakpointsComparison.setEnabled(true);
			}
			
			/*
				Fifth, go row by row and assess if the 2-4 numbers are all equal, or if one is greater than all the others. All equal => yellow font; one greater => one green, the rest red
			*/
			HashSet<Double> distinctMetricValuesForThisRow;
			double bestValue;
			int bestIndex;
			for (i = 0; i < 15; i++) {
				// Convert the array to a Set
				distinctMetricValuesForThisRow = new HashSet<Double>();
				for (j = 0; j < numBuildsToCompare; j++) {
					distinctMetricValuesForThisRow.add(metricsToCompare[j][i]);
				}
				
				// Check if all 2-4 values are equal using a Set
				if (distinctMetricValuesForThisRow.size() == 1) {
					// This special case will indicate that all values are equal
					bestIndex = -1;
				}
				// If there are at least 2 distinct values, find which index is the best
				else {
					bestValue = metricsToCompare[0][i];
					bestIndex = 0;
					for (j = 1; j < numBuildsToCompare; j++) {
						// These specific metrics want to find the lowest positive number
						if (i == 6 || i == 10 || i == 11 || i == 12 || i == 14) {
							if (bestValue < 0.0 && metricsToCompare[j][i] >= 0.0) {
								bestValue = metricsToCompare[j][i];
								bestIndex = j;
							}
							else if (bestValue >= 0.0 && metricsToCompare[j][i] >= 0.0 && metricsToCompare[j][i] < bestValue) {
								bestValue = metricsToCompare[j][i];
								bestIndex = j;
							}
						}
						// Everything else wants to find the highest positive
						else {
							if (metricsToCompare[j][i] > bestValue) {
								bestValue = metricsToCompare[j][i];
								bestIndex = j;
							}
						}
					}
				}
				
				/*
					Sixth, fill out the outputMatrix and setForeground() accordingly
				*/
				for (j = 0; j < numBuildsToCompare; j++) {
					outputMatrix[i][j].setText("  " + metricsToCompare[j][i]);
					if (bestIndex > -1 && j == bestIndex) {
						outputMatrix[i][j].setForeground(GuiConstants.drgOverclockCleanGreen);
					}
					else if (bestIndex > -1) {
						outputMatrix[i][j].setForeground(GuiConstants.drgNegativeChangeRed);
					}
					else {
						outputMatrix[i][j].setForeground(GuiConstants.drgHighlightedYellow);
					}
				}
				// If a column goes unused, set its value to nothing
				for (j = 3; j >= numBuildsToCompare; j--) {
					outputMatrix[i][j].setText("");
				}
			}
		}
		else if (e == showStatsPanelsComparison) {
			// Adapted from https://stackoverflow.com/a/13760416 and https://www.tutorialspoint.com/how-to-display-a-jframe-to-the-center-of-a-screen-in-java
			JOptionPane a = new JOptionPane(compareStatsPanels, JOptionPane.INFORMATION_MESSAGE);
			JDialog d = a.createDialog(null, "Compare the Stat Panels of multiple builds");
			d.setVisible(true);
		}
		else if (e == showBreakpointsComparison) {
			// Adapted from https://stackoverflow.com/a/13760416 and https://www.tutorialspoint.com/how-to-display-a-jframe-to-the-center-of-a-screen-in-java
			JOptionPane a = new JOptionPane(compareBreakpoints, JOptionPane.INFORMATION_MESSAGE);
			JDialog d = a.createDialog(null, "Compare the Breakpoints of multiple builds");
			d.setVisible(true);
		}
	}
}
