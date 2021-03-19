package guiPieces.buildComparators;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import guiPieces.GuiConstants;
import modelPieces.StatsRow;
import modelPieces.Weapon;

public class CompareMetrics implements ActionListener {
	private Weapon baseModel;
	private String build1, build2, build3, build4;
	private JTextField buildInput1, buildInput2, buildInput3, buildInput4;

	private JButton compareBuilds;
	private JCheckBox enableWeakpoints, enableAccuracy, enableArmorWasting;
	private JLabel[] metricNames;
	private JLabel[][] outputMatrix;
	
	public CompareMetrics(Weapon toUse) {
		baseModel = toUse.clone();
		
		build1 = "";
		build2 = "";
		build3 = "";
		build4 = "";
	}
	
	public void changeWeapon(Weapon toUse) {
		// Check if the new weapon is different than the old one. If they're the same model, do nothing.
		if (baseModel.getFullName().equals(toUse.getFullName())) {
			return;
		}
		
		baseModel = toUse.clone();

		// Because a different weapon will have different build string validation, I'm choosing to clear out all old values.
		build1 = "";
		build2 = "";
		build3 = "";
		build4 = "";
	}
	
	public void setNewBuildAtIndex(int index, String newCombination) {
		// Because this method will only be transferring valid combinations from the GUI into this object, I'm choosing to skip input validation
		switch(index) {
			case 0:{
				build1 = newCombination;
				break;
			}
			case 1:{
				build2 = newCombination;	
				break;
			}
			case 2:{
				build3 = newCombination;
				break;
			}
			case 3:{
				build4 = newCombination;
				break;
			}
		}
	}
	
	public JPanel getMetricComparisonPanel() {
		JPanel toReturn = new JPanel();
		toReturn.setLayout(new GridLayout(17, 5));
		toReturn.setBackground(GuiConstants.drgBackgroundBrown);
		
		// Top row: 3 checkboxes on right side
		// Empty JLabel to push checkboxes flush with right side
		toReturn.add(new JLabel());
		toReturn.add(new JLabel());
		
		enableWeakpoints = new JCheckBox("Enable Weakpoints");
		toReturn.add(enableWeakpoints);
		enableAccuracy = new JCheckBox("Enable Accuracy");
		toReturn.add(enableAccuracy);
		enableArmorWasting = new JCheckBox("Enable Armor Wasting");
		toReturn.add(enableArmorWasting);
		
		// Second row: "Compare" button and four text fields that user can type builds into
		compareBuilds = new JButton("Compare");
		compareBuilds.addActionListener(this);
		toReturn.add(compareBuilds);
		
		buildInput1 = new JTextField(build1);
		toReturn.add(buildInput1);
		buildInput2 = new JTextField(build2);
		toReturn.add(buildInput2);
		buildInput3 = new JTextField(build3);
		toReturn.add(buildInput3);
		buildInput4 = new JTextField(build4);
		toReturn.add(buildInput4);
		
		// Rows 3-17: metrics
		metricNames = new JLabel[] {
			new JLabel("Burst DPS:"),
			new JLabel("Sustained DPS:"),
			new JLabel("Additional Target DPS:"),
			new JLabel("Max Num Targets:"),
			new JLabel("Max Multi-Target Damage:"),
			new JLabel("Ammo Efficiency:"),
			new JLabel("% Damage Wasted by Armor:"),
			new JLabel("General Accuracy %:"),
			new JLabel("Weakpoint Accuracy %:"),
			new JLabel("Firing Duration:"),
			new JLabel("Avg TTK:"),
			new JLabel("Avg Overkill %:"),
			new JLabel("Breakpoints:"),
			new JLabel("Utility:"),
			new JLabel("Avg Time to Ignite/Freeze:")
		};
		outputMatrix = new JLabel[15][4];
		for (int row = 0; row < 15; row++) {
			metricNames[row].setFont(GuiConstants.customFont);
			metricNames[row].setForeground(GuiConstants.drgRegularOrange);
			toReturn.add(metricNames[row]);
			
			outputMatrix[row] = new JLabel[4];
			for (int col = 0; col < 4; col++) {
				outputMatrix[row][col] = new JLabel();
				outputMatrix[row][col].setFont(GuiConstants.customFont);
				outputMatrix[row][col].setBorder(GuiConstants.orangeLine);
				// JLabels are transparent background, so the brown from underlying JPanel should show through
				// Font color will be set later when metrics get plugged in and compared
				toReturn.add(outputMatrix[row][col]);
			}
		}
		
		return toReturn;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object e = arg0.getSource();
		
		if (e == compareBuilds) {
			/*
				First, sanitize the 4 text inputs and return early if any non-empty fields fail validation.
			*/
			String build1ErrorMsg="", build2ErrorMsg="", build3ErrorMsg="", build4ErrorMsg="";
			if (!buildInput1.getText().equals("") && !baseModel.isCombinationValid(buildInput1.getText())) {
				build1ErrorMsg = baseModel.getInvalidCombinationErrorMessage();
			}
			if (!buildInput2.getText().equals("") && !baseModel.isCombinationValid(buildInput2.getText())) {
				build2ErrorMsg = baseModel.getInvalidCombinationErrorMessage();
			}
			if (!buildInput3.getText().equals("") && !baseModel.isCombinationValid(buildInput3.getText())) {
				build3ErrorMsg = baseModel.getInvalidCombinationErrorMessage();
			}
			if (!buildInput4.getText().equals("") && !baseModel.isCombinationValid(buildInput4.getText())) {
				build4ErrorMsg = baseModel.getInvalidCombinationErrorMessage();
			}
			
			if (build1ErrorMsg.length() > 0 || build2ErrorMsg.length() > 0 || build3ErrorMsg.length() > 0 || build4ErrorMsg.length() > 0) {
				// Send a pop-up with the error message(s) and then return early for failure state.
				// TODO: make popup
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
			StatsRow[][] breakpointsToCompare = new StatsRow[numBuildsToCompare][31];
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
				
				breakpointsToCompare[i] = baseModel.breakpointsExplanation();
			}
			
			/*
				Fourth, go row by row and assess if the 2-4 numbers are all equal, or if one is greater than all the others. All equal => yellow font; one greater => one green, the rest red
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
						// TODO: I'd like to improve how these specific metrics are checked for this if statement
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
					Fifth, fill out the outputMatrix and setForeground() accordingly
				*/
				for (j = 0; j < numBuildsToCompare; j++) {
					outputMatrix[i][j].setText(metricsToCompare[j][i] + "");
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
	}
}
