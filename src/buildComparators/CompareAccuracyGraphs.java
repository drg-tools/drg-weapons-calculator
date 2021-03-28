package buildComparators;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import guiPieces.GuiConstants;
import modelPieces.Overclock;
import weapons.Weapon;

public class CompareAccuracyGraphs extends Comparator {
	
	private double minDistance, maxDistance;
	private int numIntervalsPerLine;
	
	private JButton saveToPNG;
	private JTextField minDistanceInput, maxDistanceInput;
	private JCheckBox[] enableMetricsToGraph;
	
	private JPanel generatedGraphs;
	private Color line1, line2, line3, line4;
	
	private JFileChooser saveLocation;
	
	public CompareAccuracyGraphs(Weapon toUse) {
		super(toUse);
		
		minDistance = 1.0;  // 1m
		maxDistance = 19.0;  // 19m
		numIntervalsPerLine = 100;
		
		line1 = new Color(232, 0, 0);  // Red
		line2 = new Color(0, 89, 232);  // Blue
		line3 = new Color(232, 193, 0);  // Yellow
		line4 = new Color(4, 97, 26);  // Green
		
		saveLocation = new JFileChooser();
		saveLocation.setFileSelectionMode(JFileChooser.FILES_ONLY);
	}
	
	@Override
	public JPanel getComparisonPanel() {
		JPanel toReturn = new JPanel();
		toReturn.setLayout(new BorderLayout());
		
		JPanel textFields = new JPanel();
		textFields.setLayout(new BoxLayout(textFields, BoxLayout.Y_AXIS));
		
		buildInput1 = new JTextField(build1);
		buildInput1.setFont(new Font("Monospaced", Font.PLAIN, 18));
		textFields.add(buildInput1);
		buildInput2 = new JTextField(build2);
		buildInput2.setFont(new Font("Monospaced", Font.PLAIN, 18));
		textFields.add(buildInput2);
		buildInput3 = new JTextField(build3);
		buildInput3.setFont(new Font("Monospaced", Font.PLAIN, 18));
		textFields.add(buildInput3);
		buildInput4 = new JTextField(build4);
		buildInput4.setFont(new Font("Monospaced", Font.PLAIN, 18));
		textFields.add(buildInput4);
		
		compareBuilds = new JButton("Generate the graphs");
		compareBuilds.addActionListener(this);
		textFields.add(compareBuilds);
		
		saveToPNG = new JButton("Save graphs as a .png");
		saveToPNG.addActionListener(this);
		saveToPNG.setEnabled(false);
		textFields.add(saveToPNG);
		
		toReturn.add(textFields, BorderLayout.WEST);
		
		JPanel checkboxes = new JPanel();
		checkboxes.setLayout(new BoxLayout(checkboxes, BoxLayout.Y_AXIS));
		
		int numCharactersWidth = 6;
		JPanel minDistRow = new JPanel();
		minDistRow.add(new JLabel("Minimum distance (meters):"));
		minDistanceInput = new JTextField(minDistance + "", numCharactersWidth);
		minDistRow.add(minDistanceInput);
		checkboxes.add(minDistRow);
		
		JPanel maxDistRow = new JPanel();
		maxDistRow.add(new JLabel("Maximum distance (meters):"));
		maxDistanceInput = new JTextField(maxDistance + "", numCharactersWidth);
		maxDistRow.add(maxDistanceInput);
		checkboxes.add(maxDistRow);
		
		enableMetricsToGraph = new JCheckBox[6];
		enableMetricsToGraph[0] = new JCheckBox("Graph General Accuracy");
		enableMetricsToGraph[0].setSelected(true);
		checkboxes.add(enableMetricsToGraph[0]);
		enableMetricsToGraph[1] = new JCheckBox("Graph Weakpoint Accuracy");
		enableMetricsToGraph[1].setSelected(true);
		checkboxes.add(enableMetricsToGraph[1]);
		enableMetricsToGraph[2] = new JCheckBox("Graph Burst DPS affected by Accuracy");
		enableMetricsToGraph[2].setSelected(true);
		checkboxes.add(enableMetricsToGraph[2]);
		enableMetricsToGraph[3] = new JCheckBox("Graph Sustained DPS affected by Accuracy");
		enableMetricsToGraph[3].setSelected(true);
		checkboxes.add(enableMetricsToGraph[3]);
		enableMetricsToGraph[4] = new JCheckBox("Graph Burst DPS w/ Accuracy & Weakpoints");
		enableMetricsToGraph[4].setSelected(true);
		checkboxes.add(enableMetricsToGraph[4]);
		enableMetricsToGraph[5] = new JCheckBox("Graph Sustained DPS w/ Accuracy & Weakpoints");
		enableMetricsToGraph[5].setSelected(true);
		checkboxes.add(enableMetricsToGraph[5]);
		
		toReturn.add(checkboxes, BorderLayout.CENTER);
		
		return toReturn;
	}

	private MultiLineGraph constructMultiLineGraph(double maxYValue, double numYIntervals, double[][] dataToPlot) {
		Color[] colors = new Color[] {
			line1,
			line2,
			line3,
			line4
		};
		
		return new MultiLineGraph(minDistance, maxDistance, 6.0, 0.0, maxYValue, numYIntervals, dataToPlot, colors);
	}
	
	private BufferedImage getScreenshot() {
		// Sourced from https://stackoverflow.com/a/44019372
		BufferedImage img = new BufferedImage(generatedGraphs.getWidth(), generatedGraphs.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = img.createGraphics();
		generatedGraphs.printAll(g2d);
		g2d.dispose();
		return img;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object e = arg0.getSource();
		
		if (e == compareBuilds) {
			/*
				First, sanitize the inputs:
					the 4 build input fields in case the user did any editing to the build combinations; also make sure at least 1 is filled out before doing anything
					the 2 distance input fields, making sure 1 <= min < max <= 19
					the 6 checkboxes, making sure that at least one is selected
			*/
			if (!areAllBuildsValid()) {
				return;
			}
			
			try {
				minDistance = Double.parseDouble(minDistanceInput.getText());
			}
			catch(Exception err) {
				JOptionPane.showMessageDialog(null, "The input provided to Min Distance is not a number.", "Please enter a number into Min Distance", JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				maxDistance = Double.parseDouble(maxDistanceInput.getText());
			}
			catch(Exception err) {
				JOptionPane.showMessageDialog(null, "The input provided to Max Distance is not a number.", "Please enter a number into Max Distance", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (minDistance >= maxDistance) {
				JOptionPane.showMessageDialog(null, "The Minimum distance must be strictly less than the Maximum distance.", "Shame on you for trying to break this code", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (minDistance < 1.0) {
				JOptionPane.showMessageDialog(null, "The Minimum distance cannot be less than 1m.", "Please keep distances between 1-19m", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (maxDistance > 19.0) {
				JOptionPane.showMessageDialog(null, "The Maximum distance cannot be greater than 19m.", "Please keep distances between 1-19m", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			int i, j;
			boolean atLeastOneMetricIsSelected = false;
			for (i = 0; i < enableMetricsToGraph.length; i++) {
				atLeastOneMetricIsSelected = atLeastOneMetricIsSelected || enableMetricsToGraph[i].isSelected();
				
				// No need to evaluate all 6 checkboxes if this value is already true. 
				if (atLeastOneMetricIsSelected) {
					break;
				}
			}
			if (!atLeastOneMetricIsSelected) {
				JOptionPane.showMessageDialog(null, "At least one of the 6 checkboxes must be selected.", "Please select a metric", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			/*
				Second, move the build combinations upwards toward the top as necessary (like how CompareMetrics does Left-Justifying)
			*/
			ArrayList<String> justifyTop = new ArrayList<String>();
			justifyTop.add(buildInput1.getText());
			justifyTop.add(buildInput2.getText());
			justifyTop.add(buildInput3.getText());
			justifyTop.add(buildInput4.getText());
			
			i = 0;
			while (i < justifyTop.size()) {
				if (justifyTop.get(i).length() == 0) {
					justifyTop.remove(i);
				}
				else {
					i++;
				}
			}
			
			int numBuildsToCompare = justifyTop.size();
			
			if (numBuildsToCompare < 1) {
				// Early exit: if fewer than 1 field has been filled out when the button gets pressed, do nothing.
				return;
			}
			
			while (justifyTop.size() < 4) {
				justifyTop.add("");
			}
			build1 = justifyTop.get(0);
			buildInput1.setText(build1);
			build2 = justifyTop.get(1);
			buildInput2.setText(build2);
			build3 = justifyTop.get(2);
			buildInput3.setText(build3);
			build4 = justifyTop.get(3);
			buildInput4.setText(build4);
			
			/*
				Third, generate the matrices of data -- one matrix for each of the six metrics, with 1-4 rows for each build and as many columns as necessary for the datapoints
			*/
			double xInterval = (maxDistance - minDistance) / ((double) numIntervalsPerLine);
			
			double[][] generalAccuracy, weakpointAccuracy, burstAccDPS, sustainedAccDPS, burstAccWpDPS, sustainedAccWpDPS;
			generalAccuracy = new double[numBuildsToCompare][numIntervalsPerLine + 1];
			weakpointAccuracy = new double[numBuildsToCompare][numIntervalsPerLine + 1];
			burstAccDPS = new double[numBuildsToCompare][numIntervalsPerLine + 1];
			sustainedAccDPS = new double[numBuildsToCompare][numIntervalsPerLine + 1];
			burstAccWpDPS = new double[numBuildsToCompare][numIntervalsPerLine + 1];
			sustainedAccWpDPS = new double[numBuildsToCompare][numIntervalsPerLine + 1];
			
			double largestBurstAcc = 0, largestSustainedAcc = 0, largestBurstAccWP = 0, largestSustainedAccWP = 0;
			
			baseModel.setAccuracyDPS(true, false);
			baseModel.setWeakpointDPS(false, false);
			baseModel.setArmorWastingDPS(false, false);
			for (i = 0; i < numBuildsToCompare; i++) {
				baseModel.buildFromCombination(justifyTop.get(i), false);
				
				for (j = 0; j < numIntervalsPerLine + 1; j++) {
					baseModel.setAccuracyDistance(minDistance + j * xInterval, false);
					
					if (enableMetricsToGraph[0].isSelected()) {
						generalAccuracy[i][j] = baseModel.getGeneralAccuracy();
					}
					if (enableMetricsToGraph[1].isSelected()) {
						weakpointAccuracy[i][j] = baseModel.getWeakpointAccuracy();
					}
					
					if (enableMetricsToGraph[2].isSelected()) {
						burstAccDPS[i][j] = baseModel.calculateSingleTargetDPS(true);
						
						if (burstAccDPS[i][j] > largestBurstAcc) {
							largestBurstAcc = burstAccDPS[i][j];
						}
					}
					if (enableMetricsToGraph[3].isSelected()) {
						sustainedAccDPS[i][j] = baseModel.calculateSingleTargetDPS(false);
						
						if (sustainedAccDPS[i][j] > largestSustainedAcc) {
							largestSustainedAcc = sustainedAccDPS[i][j];
						}
					}
					
					baseModel.setWeakpointDPS(true, false);
					if (enableMetricsToGraph[4].isSelected()) {
						burstAccWpDPS[i][j] = baseModel.calculateSingleTargetDPS(true);
						
						if (burstAccWpDPS[i][j] > largestBurstAccWP) {
							largestBurstAccWP = burstAccWpDPS[i][j];
						}
					}
					if (enableMetricsToGraph[5].isSelected()) {
						sustainedAccWpDPS[i][j] = baseModel.calculateSingleTargetDPS(false);
						
						if (sustainedAccWpDPS[i][j] > largestSustainedAccWP) {
							largestSustainedAccWP = sustainedAccWpDPS[i][j];
						}
					}
					baseModel.setWeakpointDPS(false, false);
				}
			}
			
			/*
				Fourth, use those matrices and generate as many "fancy graphs" as needed. Arrange them in a GridLayout with the appropriate labels
			*/
			// Start by rounding up the 4 DPS metrics to the next highest multiple of 50
			double desiredMultiple = 50.0;
			largestBurstAcc = desiredMultiple * Math.ceil(largestBurstAcc / desiredMultiple); 
			largestSustainedAcc = desiredMultiple * Math.ceil(largestSustainedAcc / desiredMultiple); 
			largestBurstAccWP = desiredMultiple * Math.ceil(largestBurstAccWP / desiredMultiple); 
			largestSustainedAccWP = desiredMultiple * Math.ceil(largestSustainedAccWP / desiredMultiple); 
			
			generatedGraphs = new JPanel();
			//generatedGraphs.setPreferredSize(new Dimension(800, 1200));
			generatedGraphs.setLayout(new BorderLayout());
			generatedGraphs.setBackground(GuiConstants.drgBackgroundBrown);
			
			JPanel titleAndLegend = new JPanel();
			titleAndLegend.setLayout(new BoxLayout(titleAndLegend, BoxLayout.Y_AXIS));
			titleAndLegend.setOpaque(false);
			titleAndLegend.setBorder(GuiConstants.orangeLine);
			
			JLabel title = new JLabel(baseModel.getFullName());
			title.setFont(GuiConstants.customFontTitle);
			title.setForeground(GuiConstants.drgRegularOrange);
			title.setAlignmentX(Component.CENTER_ALIGNMENT);
			titleAndLegend.add(title);
			
			JPanel legend = new JPanel();
			legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
			legend.setOpaque(false);
			
			Overclock[] OCs = baseModel.getOverclocks();
			JPanel buildRow;
			JLabel colorCode = new JLabel(), buildName = new JLabel();
			char ocShortcut;
			String overclockName;
			for (i = 0; i < numBuildsToCompare; i++) {
				buildRow = new JPanel();
				buildRow.setOpaque(false);
				
				colorCode = new JLabel("      ");
				colorCode.setOpaque(true);
				colorCode.setFont(GuiConstants.customFont);
				colorCode.setBorder(GuiConstants.blackLine);
				if (i == 0) {
					colorCode.setBackground(line1);
					ocShortcut = build1.charAt(5);
					if (ocShortcut != '-') {
						overclockName = OCs[Integer.parseInt(ocShortcut + "") - 1].getName();
						buildName = new JLabel(build1.substring(0, 5) + " + \"" + overclockName + "\"");
					}
					else {
						buildName = new JLabel(build1.substring(0, 5) + " (no OC equipped)");
					}
				}
				else if (i == 1) {
					colorCode.setBackground(line2);
					ocShortcut = build2.charAt(5);
					if (ocShortcut != '-') {
						overclockName = OCs[Integer.parseInt(ocShortcut + "") - 1].getName();
						buildName = new JLabel(build2.substring(0, 5) + " + \"" + overclockName + "\"");
					}
					else {
						buildName = new JLabel(build2.substring(0, 5) + " (no OC equipped)");
					}
				}
				else if (i == 2) {
					colorCode.setBackground(line3);
					ocShortcut = build3.charAt(5);
					if (ocShortcut != '-') {
						overclockName = OCs[Integer.parseInt(ocShortcut + "") - 1].getName();
						buildName = new JLabel(build3.substring(0, 5) + " + \"" + overclockName + "\"");
					}
					else {
						buildName = new JLabel(build3.substring(0, 5) + " (no OC equipped)");
					}
				}
				else if (i == 3) {
					colorCode.setBackground(line4);
					ocShortcut = build4.charAt(5);
					if (ocShortcut != '-') {
						overclockName = OCs[Integer.parseInt(ocShortcut + "") - 1].getName();
						buildName = new JLabel(build4.substring(0, 5) + " + \"" + overclockName + "\"");
					}
					else {
						buildName = new JLabel(build4.substring(0, 5) + " (no OC equipped)");
					}
				}
				buildRow.add(colorCode);
				
				buildName.setFont(GuiConstants.customFontHeader);
				buildName.setForeground(GuiConstants.drgRegularOrange);
				buildRow.add(buildName);
				
				legend.add(buildRow);
			}
			legend.setAlignmentX(Component.CENTER_ALIGNMENT);
			titleAndLegend.add(legend);
			titleAndLegend.setAlignmentX(Component.CENTER_ALIGNMENT);
			generatedGraphs.add(titleAndLegend, BorderLayout.NORTH);
			
			JPanel graphsGrid = new JPanel();
			graphsGrid.setLayout(new GridLayout(3, 2));
			graphsGrid.setOpaque(false);
			graphsGrid.setBorder(GuiConstants.orangeLine);
			
			JPanel container;
			MultiLineGraph mlg = null;
			JLabel nameOfGraph = new JLabel();
			for (i = 0; i < enableMetricsToGraph.length; i++) {
				container = new JPanel();
				container.setLayout(new BorderLayout());
				container.setOpaque(false);
				
				if (enableMetricsToGraph[i].isSelected()) {
					if (i == 0) {
						nameOfGraph = new JLabel("General Accuracy");
						mlg = constructMultiLineGraph(100.0, 100.0/25.0, generalAccuracy);
						mlg.setYUnit("%");
					}
					else if (i == 1) {
						nameOfGraph = new JLabel("Weakpoint Accuracy");
						mlg = constructMultiLineGraph(100.0, 100.0/25.0, weakpointAccuracy);
						mlg.setYUnit("%");
					}
					else if (i == 2) {
						nameOfGraph = new JLabel("Burst DPS affected by Accuracy");
						mlg = constructMultiLineGraph(largestBurstAcc, largestBurstAcc/desiredMultiple, burstAccDPS);
					}
					else if (i == 3) {
						nameOfGraph = new JLabel("Sustained DPS affected by Accuracy");
						mlg = constructMultiLineGraph(largestSustainedAcc, largestSustainedAcc/desiredMultiple, sustainedAccDPS);
					}
					else if (i == 4) {
						nameOfGraph = new JLabel("Burst DPS w/ Accuracy & Weakpoints");
						mlg = constructMultiLineGraph(largestBurstAccWP, largestBurstAccWP/desiredMultiple, burstAccWpDPS);
					}
					else if (i == 5) {
						nameOfGraph = new JLabel("Sustained DPS w/ Accuracy & Weakpoints");
						mlg = constructMultiLineGraph(largestSustainedAccWP, largestSustainedAccWP/desiredMultiple, sustainedAccWpDPS);
					}
					mlg.setXUnit("m");
					
					nameOfGraph.setFont(GuiConstants.customFontHeader);
					nameOfGraph.setForeground(GuiConstants.drgRegularOrange);
					nameOfGraph.setHorizontalAlignment(JLabel.CENTER);
					container.add(nameOfGraph, BorderLayout.NORTH);
					container.add(mlg, BorderLayout.CENTER);
					//container.setAlignmentX(Component.CENTER_ALIGNMENT);
					graphsGrid.add(container);
				}
			}
			generatedGraphs.add(graphsGrid, BorderLayout.CENTER);
			
			saveToPNG.setEnabled(true);
			
			// Adapted from https://stackoverflow.com/a/13760416 and https://www.tutorialspoint.com/how-to-display-a-jframe-to-the-center-of-a-screen-in-java
			JOptionPane a = new JOptionPane(generatedGraphs, JOptionPane.INFORMATION_MESSAGE);
			JDialog d = a.createDialog(null, "Graphs that compare various metrics affected by Accuracy and distance");
			d.setVisible(true);
		}
		else if (e == saveToPNG) {
			int returnVal = saveLocation.showOpenDialog(null);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File pngOut = saveLocation.getSelectedFile();
				
				// Input sanitization; make sure the file type extension can only be .png
				String userChosenName = pngOut.getName();
				if (userChosenName.contains(".")) {
					userChosenName = userChosenName.replaceFirst("\\..*$", ".png");
				}
				else {
					userChosenName += ".png";
				}

				pngOut = new File(pngOut.getParent(), userChosenName);
				
				// Sourced from https://stackoverflow.com/a/44019372
				BufferedImage screenshot = this.getScreenshot();
				try {
					ImageIO.write(screenshot, "png", pngOut);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}
