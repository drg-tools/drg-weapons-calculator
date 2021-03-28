package dataGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import weapons.Weapon;

public class MetricsCalculator {
	
	private Weapon weaponToTest;
	private File outputFolder;
	
	public MetricsCalculator(Weapon testingWeapon) {
		weaponToTest = testingWeapon;
		String defaultHomeFolder = System.getProperty("user.home");
		outputFolder = new File(defaultHomeFolder);
	}
	
	public void setOutputFolder(File newDestinationFolder) {
		if (newDestinationFolder.isDirectory()) {
			outputFolder = newDestinationFolder;
		}
	}
	public File getOutputFolder() {
		return outputFolder;
	}
	
	public void writeFile(String lineToWrite, String filename, boolean append) {
		File out = new File(outputFolder, filename);
		
		try {
			FileWriter writer = new FileWriter(out, append);
			writer.append(lineToWrite);
			writer.flush();
			writer.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void writeFile(ArrayList<String> linesToWrite, String filename, boolean append) {
		File out = new File(outputFolder, filename);
		
		try {
			FileWriter writer = new FileWriter(out, append);
			for (String line: linesToWrite) {
				writer.append(line);
			}
			writer.flush();
			writer.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void changeWeapon(Weapon newWeaponToCalculate) {
		weaponToTest = newWeaponToCalculate;
	}
	
	public void exportMetricsToCSV() {
		/*
			Questions I want to answer:
				1. What are the baseline stats for the weapon?
				2. How much does each individual mod increase or decrease those stats?
				3. How much does each individual overclock increase or decrease those stats?
				4. What is the best combination of mods and overclocks?
		*/
		
		// Start by resetting all mods and overclocks to be unselected
		String currentCombination = weaponToTest.getCombination();
		weaponToTest.setSelectedModAtTier(1, -1, false);
		weaponToTest.setSelectedModAtTier(2, -1, false);
		weaponToTest.setSelectedModAtTier(3, -1, false);
		weaponToTest.setSelectedModAtTier(4, -1, false);
		weaponToTest.setSelectedModAtTier(5, -1, false);
		weaponToTest.setSelectedOverclock(-1, false);
		
		// Clear out old data and write the header line.
		String filename = weaponToTest.getDwarfClass() + "_" + weaponToTest.getSimpleName() + ".csv";
		ArrayList<String> csvLinesToWrite = new ArrayList<String>();
		
		String[] headers = new String[] {"Mods/OC", 
				"Ideal Burst DPS", "Burst DPS (+WP)", "Burst DPS (+Acc)", "Burst DPS (+AW)", "Burst DPS (+WP, +Acc)", "Burst DPS (+WP, +AW)", "Burst DPS (+Acc, +AW)", "Burst DPS (+WP, +Acc, +AW)", 
				"Ideal Sustained DPS", "Sustained DPS (+WP)", "Sustained DPS (+Acc)", "Sustained DPS (+AW)", "Sustained DPS (+WP, +Acc)", "Sustained DPS (+WP, +AW)", "Sustained DPS (+Acc, +AW)", "Sustained DPS (+WP, +Acc, +AW)", 
				"Ideal Additional Target DPS", "Max Num Targets", "Max Multi-Target Dmg", "Ammo Efficiency", "Avg Damage Wasted by Armor",
				"General Accuracy", "Weakpoint Accuracy", "Firing Duration", "Avg TTK", "Avg Overkill", "Breakpoints", "Utility", "Avg Time to Ignite/Freeze", 
				"Damage per Magazine/Explosion", "Time to Fire Magazine"};
		String headerLine = String.join(", ", headers) + ",\n";
		// Set append=False so that it clears existing lines
		writeFile(headerLine, filename, false);
		
		// One String for the combination, and then 16 DPS and 13 other metrics
		String format = "%s, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %d, %f, %f, %f, %f, %f, %f, %f, %f, %d, %f, %f, %f, %f,\n";
		
		int[] tier1 = weaponToTest.getModsAtTier(1, false);
		int[] tier2 = weaponToTest.getModsAtTier(2, false);
		int[] tier3 = weaponToTest.getModsAtTier(3, false);
		int[] tier4 = weaponToTest.getModsAtTier(4, false);
		int[] tier5 = weaponToTest.getModsAtTier(5, false);
		int[] overclocks = weaponToTest.getOverclocks(false);
		
		// The overclocks are the outermost loop because they should change last, and tier 1 is the innermost loop since it should change first.
		for (int oc: overclocks) {
			weaponToTest.setSelectedOverclock(oc, false);
			
			for (int t5: tier5) {
				weaponToTest.setSelectedModAtTier(5, t5, false);
				
				for (int t4: tier4) {
					weaponToTest.setSelectedModAtTier(4, t4, false);
					
					for (int t3: tier3) {
						weaponToTest.setSelectedModAtTier(3, t3, false);
						
						for (int t2: tier2) {
							weaponToTest.setSelectedModAtTier(2, t2, false);
							
							for (int t1: tier1) {
								weaponToTest.setSelectedModAtTier(1, t1, false);
								
								csvLinesToWrite.add(String.format(format, weaponToTest.getCombination(),
									weaponToTest.calculateSingleTargetDPS(true, false, false, false), weaponToTest.calculateSingleTargetDPS(true, true, false, false), 
									weaponToTest.calculateSingleTargetDPS(true, false, true, false), weaponToTest.calculateSingleTargetDPS(true, false, false, true), 
									weaponToTest.calculateSingleTargetDPS(true, true, true, false), weaponToTest.calculateSingleTargetDPS(true, true, false, true), 
									weaponToTest.calculateSingleTargetDPS(true, false, true, true), weaponToTest.calculateSingleTargetDPS(true, true, true, true), 
									weaponToTest.calculateSingleTargetDPS(false, false, false, false), weaponToTest.calculateSingleTargetDPS(false, true, false, false), 
									weaponToTest.calculateSingleTargetDPS(false, false, true, false), weaponToTest.calculateSingleTargetDPS(false, false, false, true), 
									weaponToTest.calculateSingleTargetDPS(false, true, true, false), weaponToTest.calculateSingleTargetDPS(false, true, false, true), 
									weaponToTest.calculateSingleTargetDPS(false, false, true, true), weaponToTest.calculateSingleTargetDPS(false, true, true, true), 
									weaponToTest.calculateAdditionalTargetDPS(), weaponToTest.calculateMaxNumTargets(), weaponToTest.calculateMaxMultiTargetDamage(), 
									weaponToTest.ammoEfficiency(), weaponToTest.damageWastedByArmor(), weaponToTest.getGeneralAccuracy(), weaponToTest.getWeakpointAccuracy(),
									weaponToTest.calculateFiringDuration(), weaponToTest.averageTimeToKill(), weaponToTest.averageOverkill(), weaponToTest.breakpoints(), 
									weaponToTest.utilityScore(), weaponToTest.averageTimeToCauterize(), weaponToTest.damagePerMagazine(), weaponToTest.timeToFireMagazine()
								));
								
							}
						}
					}
				}
			}
		}
		
		// Set append=True so that it appends the lines after the header line
		writeFile(csvLinesToWrite, filename, true);
		
		// Finally, set the Weapon back to the mods/oc combination that it started at before the test.
		weaponToTest.buildFromCombination(currentCombination);
	}
	
	public ArrayList<String> dumpMetricsToMySQL() {
		ArrayList<String> toReturn = new ArrayList<String>();
		
		// Grab the current combination for the weapon to restore after this is done
		String currentCombination = weaponToTest.getCombination();
		int dwarfClassID = weaponToTest.getDwarfClassID();
		int weaponID = weaponToTest.getWeaponID();
		String simpleName = weaponToTest.getSimpleName();
		
		int[] tier1 = weaponToTest.getModsAtTier(1, false);
		int[] tier2 = weaponToTest.getModsAtTier(2, false);
		int[] tier3 = weaponToTest.getModsAtTier(3, false);
		int[] tier4 = weaponToTest.getModsAtTier(4, false);
		int[] tier5 = weaponToTest.getModsAtTier(5, false);
		int[] overclocks = weaponToTest.getOverclocks(false);
		
		String format = "INSERT INTO `%s` VALUES(NULL, %d, %d, '%s', '%s', "  			// Identifying this row
				+ "%f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, "  	// 16 primary target DPS calculations
				+ "%f, %d, %f, %f, %f, %f, %f, %f, %f, %f, %d, %f, %f, %f, %f, %d);\n";  	// 15 other metrics and patch ID
		
		// The overclocks are the outermost loop because they should change last, and tier 1 is the innermost loop since it should change first.
		for (int oc: overclocks) {
			weaponToTest.setSelectedOverclock(oc, false);
			
			for (int t5: tier5) {
				weaponToTest.setSelectedModAtTier(5, t5, false);
				
				for (int t4: tier4) {
					weaponToTest.setSelectedModAtTier(4, t4, false);
					
					for (int t3: tier3) {
						weaponToTest.setSelectedModAtTier(3, t3, false);
						
						for (int t2: tier2) {
							weaponToTest.setSelectedModAtTier(2, t2, false);
							
							for (int t1: tier1) {
								weaponToTest.setSelectedModAtTier(1, t1, false);
								
								toReturn.add(String.format(format, 
									// Identifying this row
									DatabaseConstants.statsTableName, dwarfClassID, weaponID, simpleName, weaponToTest.getCombination(),
									// 8 Burst DPS
									weaponToTest.calculateSingleTargetDPS(true, false, false, false), weaponToTest.calculateSingleTargetDPS(true, true, false, false), 
									weaponToTest.calculateSingleTargetDPS(true, false, true, false), weaponToTest.calculateSingleTargetDPS(true, false, false, true), 
									weaponToTest.calculateSingleTargetDPS(true, true, true, false), weaponToTest.calculateSingleTargetDPS(true, true, false, true), 
									weaponToTest.calculateSingleTargetDPS(true, false, true, true), weaponToTest.calculateSingleTargetDPS(true, true, true, true), 
									// 8 Sustained DPS
									weaponToTest.calculateSingleTargetDPS(false, false, false, false), weaponToTest.calculateSingleTargetDPS(false, true, false, false), 
									weaponToTest.calculateSingleTargetDPS(false, false, true, false), weaponToTest.calculateSingleTargetDPS(false, false, false, true), 
									weaponToTest.calculateSingleTargetDPS(false, true, true, false), weaponToTest.calculateSingleTargetDPS(false, true, false, true), 
									weaponToTest.calculateSingleTargetDPS(false, false, true, true), weaponToTest.calculateSingleTargetDPS(false, true, true, true), 
									// 15 Other metrics and patch ID
									weaponToTest.calculateAdditionalTargetDPS(), weaponToTest.calculateMaxNumTargets(), weaponToTest.calculateMaxMultiTargetDamage(), weaponToTest.ammoEfficiency(), weaponToTest.damageWastedByArmor(),
									weaponToTest.getGeneralAccuracy(), weaponToTest.getWeakpointAccuracy(), weaponToTest.calculateFiringDuration(), weaponToTest.averageTimeToKill(), 
									weaponToTest.averageOverkill(), weaponToTest.breakpoints(), weaponToTest.utilityScore(), weaponToTest.averageTimeToCauterize(), 
									weaponToTest.damagePerMagazine(), weaponToTest.timeToFireMagazine(), DatabaseConstants.patchNumberID
								));
							}
						}
					}
				}
			}
		}
		
		// Return the weapon to the combination it had before
		weaponToTest.buildFromCombination(currentCombination);
		
		return toReturn;
	}
	
	public String getBestMetricCombination(int metricIndex, boolean subset) {
		// Currently there are 15 metrics on display in the GUI (with the first 2 technically representing 8 different varieties of DPS each)
		if (metricIndex < 0 || metricIndex > 14) {
			return "------";
		}
		
		// Damage Wasted by Armor, Fastest TTK, Lowest Overkill, Breakpoints, and Cauterize should all be lowest-possible values
		Integer[] indexesThatShouldUseLessThan = new Integer[] {6, 10, 11, 12, 14};
		boolean comparatorShouldBeLessThan = new HashSet<Integer>(Arrays.asList(indexesThatShouldUseLessThan)).contains(metricIndex);
		
		String bestCombination = "------";
		double bestValue, currentValue;
		// To the best of my knowledge, none of these values goes above 200k, so setting the starting "best" value at 1 million should automatically make the first combination tried the new best
		if (comparatorShouldBeLessThan) {
			bestValue = 1000000;
		}
		else {
			bestValue = -1000000;
		}
		
		int[] tier1 = weaponToTest.getModsAtTier(1, subset);
		int[] tier2 = weaponToTest.getModsAtTier(2, subset);
		int[] tier3 = weaponToTest.getModsAtTier(3, subset);
		int[] tier4 = weaponToTest.getModsAtTier(4, subset);
		int[] tier5 = weaponToTest.getModsAtTier(5, subset);
		int[] overclocks = weaponToTest.getOverclocks(subset);
		
		// Set these boolean values once instead of evaluating tier 1 about 3000 times
		boolean onlyOneTier1 = tier1.length == 1;
		boolean onlyOneTier2 = tier2.length == 1;
		boolean onlyOneTier3 = tier3.length == 1;
		boolean onlyOneTier4 = tier4.length == 1;
		boolean onlyOneTier5 = tier5.length == 1;
		boolean onlyOneOC = overclocks.length == 1;
		
		/*
			This is important: because the current Weapon ALREADY has the wanted partial combination pre-selected when the menu for "Best Metric" gets called,
			DO NOT, I repeat, DO NOT set the mod or overclock again, because that just un-sets it.
		*/
		
		// The overclocks are the outermost loop because they should change last, and tier 1 is the innermost loop since it should change first.
		for (int oc: overclocks) {
			if (!onlyOneOC) {
				weaponToTest.setSelectedOverclock(oc, false);
			}
			
			for (int t5: tier5) {
				if (!onlyOneTier5) {
					weaponToTest.setSelectedModAtTier(5, t5, false);
				}
				
				for (int t4: tier4) {
					if (!onlyOneTier4) {
						weaponToTest.setSelectedModAtTier(4, t4, false);
					}
					
					for (int t3: tier3) {
						if (!onlyOneTier3) {
							weaponToTest.setSelectedModAtTier(3, t3, false);
						}
						
						for (int t2: tier2) {
							if (!onlyOneTier2) {
								weaponToTest.setSelectedModAtTier(2, t2, false);
							}
							
							for (int t1: tier1) {
								if (!onlyOneTier1) {
									weaponToTest.setSelectedModAtTier(1, t1, false);
								}
								
								switch (metricIndex) {
									case 0: {
										currentValue = weaponToTest.calculateSingleTargetDPS(true);
										break;
									}
									case 1: {
										currentValue = weaponToTest.calculateSingleTargetDPS(false);
										break;
									}
									case 2: {
										currentValue = weaponToTest.calculateAdditionalTargetDPS();
										break;
									}
									case 3: {
										currentValue = weaponToTest.calculateMaxNumTargets();
										break;
									}
									case 4: {
										currentValue = weaponToTest.calculateMaxMultiTargetDamage();
										break;
									}
									case 5: {
										currentValue = weaponToTest.ammoEfficiency();
										break;
									}
									case 6: {
										currentValue = weaponToTest.damageWastedByArmor();
										break;
									}
									case 7: {
										currentValue = weaponToTest.getGeneralAccuracy();
										break;
									}
									case 8: {
										currentValue = weaponToTest.getWeakpointAccuracy();
										break;
									}
									case 9: {
										currentValue = weaponToTest.calculateFiringDuration();
										break;
									}
									case 10: {
										currentValue = weaponToTest.averageTimeToKill();
										break;
									}
									case 11: {
										currentValue = weaponToTest.averageOverkill();
										break;
									}
									case 12: {
										currentValue = weaponToTest.breakpoints();
										break;
									}
									case 13: {
										currentValue = weaponToTest.utilityScore();
										break;
									}
									case 14: {
										currentValue = weaponToTest.averageTimeToCauterize();
										break;
									}
									default: {
										currentValue = 0;
										break;
									}
								}
								
								if (comparatorShouldBeLessThan) {
									// Adding the >= 0 check just for Cauterize, but it should be safe for all the other metrics too...
									if (currentValue >= 0 && currentValue < bestValue) {
										bestCombination = weaponToTest.getCombination();
										bestValue = currentValue;
									}
								}
								else {
									if (currentValue > bestValue) {
										bestCombination = weaponToTest.getCombination();
										bestValue = currentValue;
									}
								}
							}
						}
					}
				}
			}
		}
		
		return bestCombination;
	}
}
