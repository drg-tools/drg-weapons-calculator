package dataGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import modelPieces.Weapon;

public class WeaponStatsGenerator {
	
	private Weapon weaponToTest;
	private String csvFolderPath;
	private String csvFilePath;
	private String[] headers;
	private ArrayList<String> csvLinesToWrite;
	
	public WeaponStatsGenerator(Weapon testingWeapon) {
		weaponToTest = testingWeapon;
		csvFolderPath = "";
		
		String weaponPackage = weaponToTest.getClass().getPackageName();
		String weaponClassName = weaponToTest.getClass().getSimpleName();
		csvFilePath = csvFolderPath + "\\" + weaponPackage + "_" + weaponClassName + ".csv";
		
		headers = new String[] {"Mods/OC", "Ideal Burst DPS", "Ideal Sustained DPS", "Sustained DPS (+Weakpoints)", 
								"Sustained DPS (+Weakpoints, +Accuracy)", "Ideal Additional Target DPS", "Max Multi-Target Dmg", 
								"Max Num Targets", "Firing Duration", "Avg TTK", "Avg Overkill", "Accuracy", "Utility"};
		csvLinesToWrite = new ArrayList<String>();
	}
	
	public String getCSVFolderPath() {
		return csvFolderPath;
	}
	public void setCSVFolderPath(String newPath) {
		csvFolderPath = newPath;
		String weaponPackage = weaponToTest.getClass().getPackageName();
		String weaponClassName = weaponToTest.getClass().getSimpleName();
		csvFilePath = csvFolderPath + "\\" + weaponPackage + "_" + weaponClassName + ".csv";
	}
	
	public void changeWeapon(Weapon newWeaponToCalculate) {
		weaponToTest = newWeaponToCalculate;
		String weaponPackage = weaponToTest.getClass().getPackageName();
		String weaponClassName = weaponToTest.getClass().getSimpleName();
		csvFilePath = csvFolderPath + "\\" + weaponPackage + "_" + weaponClassName + ".csv";
		
		// Proactively clear out the old CSV lines, since they won't be applicable to the new Weapon
		csvLinesToWrite = new ArrayList<String>();
	}
	
	public void runTest(boolean printStatsToConsole, boolean exportStatsToCSV) {
		/*
			Questions I want to answer:
				1. What are the baseline stats for the weapon?
				2. How much does each individual mod increase or decrease those stats?
				3. How much does each individual overclock increase or decrease those stats?
				4. What is the best combination of mods and overclocks?
		*/
		
		if (!printStatsToConsole && !exportStatsToCSV) {
			System.out.println("The statistics need an output destination, otherwise this method is useless.");
			return;
		}
		
		// Start by resetting all mods and overclocks to be unselected
		String currentCombination = weaponToTest.getCombination();
		
		if (exportStatsToCSV) {
			// New run; clear out old data and write the header line.
			csvLinesToWrite = new ArrayList<String>();
			try {
				// Set append=False so that it clears existing lines
				FileWriter CSVwriter = new FileWriter(csvFilePath, false);
				String headerLine = String.join(",", headers) + ",\n";
				CSVwriter.append(headerLine);
				CSVwriter.flush();
				CSVwriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (printStatsToConsole) {
			System.out.printf("******** %s ********\n", weaponToTest.getFullName());
			System.out.printf("%s\t\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t\t%s\t%s\t%s\n", headers[0], headers[1], headers[2], headers[3], headers[4], 
							  headers[5], headers[6], headers[7], headers[8], headers[9], headers[10], headers[11], headers[12]);
		
			// Section 1: baseline statistics
			weaponToTest.setSelectedModAtTier(1, -1);
			weaponToTest.setSelectedModAtTier(2, -1);
			weaponToTest.setSelectedModAtTier(3, -1);
			weaponToTest.setSelectedModAtTier(4, -1);
			weaponToTest.setSelectedModAtTier(5, -1);
			weaponToTest.setSelectedOverclock(-1);
			
			calculateStatsAndPrint(printStatsToConsole, false);
			System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
		
			// Section 2: stat changes of individual mods
			int i;
			for (i = 0; i < weaponToTest.getModsAtTier(1).length; i++) {
				weaponToTest.setSelectedModAtTier(1, i);
				calculateStatsAndPrint(printStatsToConsole, false);
			}
			// Unselect the mod at this tier so it doesn't affect the next tier
			weaponToTest.setSelectedModAtTier(1, -1);
			
			for (i = 0; i < weaponToTest.getModsAtTier(2).length; i++) {
				weaponToTest.setSelectedModAtTier(2, i);
				calculateStatsAndPrint(printStatsToConsole, false);	
			}
			// Unselect the mod at this tier so it doesn't affect the next tier
			weaponToTest.setSelectedModAtTier(2, -1);
			
			for (i = 0; i < weaponToTest.getModsAtTier(3).length; i++) {
				weaponToTest.setSelectedModAtTier(3, i);
				calculateStatsAndPrint(printStatsToConsole, false);
			}
			// Unselect the mod at this tier so it doesn't affect the next tier
			weaponToTest.setSelectedModAtTier(3, -1);
			
			for (i = 0; i < weaponToTest.getModsAtTier(4).length; i++) {
				weaponToTest.setSelectedModAtTier(4, i);
				calculateStatsAndPrint(printStatsToConsole, false);
			}
			// Unselect the mod at this tier so it doesn't affect the next tier
			weaponToTest.setSelectedModAtTier(4, -1);
			
			for (i = 0; i < weaponToTest.getModsAtTier(5).length; i++) {
				weaponToTest.setSelectedModAtTier(5, i);
				calculateStatsAndPrint(printStatsToConsole, false);
			}
			// Unselect the mod at this tier so it doesn't affect the next tier
			weaponToTest.setSelectedModAtTier(5, -1);
			System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
		
			// Section 3: stat changes of individual overclocks
			for (i = 0; i < weaponToTest.getOverclocks().length; i++) {
				weaponToTest.setSelectedOverclock(i);
				calculateStatsAndPrint(printStatsToConsole, false);
			}
			// Unselect the overclock so that weaponToTest will be at "baseline" before doing the 6 clone() calls
			weaponToTest.setSelectedOverclock(-1);
			System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
		}
		
		// Section 4: COMBINATORICS
		// Start by cloning the weapon with no mods or overclocks selected to get baselines
		Weapon bestIdealBurstDPS = weaponToTest.clone();
		Weapon bestIdealSustainedDPS = weaponToTest.clone();
		Weapon bestWeakpointSustainedDPS = weaponToTest.clone();
		Weapon bestWeakpointAccuracySustainedDPS = weaponToTest.clone();
		Weapon bestAdditionalTargetDPS = weaponToTest.clone();
		Weapon mostMultiTargetDamage = weaponToTest.clone();
		Weapon mostNumTargets = weaponToTest.clone();
		Weapon longestFiringDuration = weaponToTest.clone();
		Weapon fastestTTK = weaponToTest.clone();
		Weapon lowestOverkill = weaponToTest.clone();
		Weapon bestAccuracy = weaponToTest.clone();
		Weapon bestUtility = weaponToTest.clone();
		
		// The overclocks are the outermost loop because they should change last, and tier 1 is the innermost loop since it should change first.
		for (int oc = -1; oc < weaponToTest.getOverclocks().length; oc++) {
			weaponToTest.setSelectedOverclock(oc);
			
			for (int t5 = -1; t5 < weaponToTest.getModsAtTier(5).length; t5++) {
				weaponToTest.setSelectedModAtTier(5, t5);
				
				for (int t4 = -1; t4 < weaponToTest.getModsAtTier(4).length; t4++) {
					weaponToTest.setSelectedModAtTier(4, t4);
					
					for (int t3 = -1; t3 < weaponToTest.getModsAtTier(3).length; t3++) {
						weaponToTest.setSelectedModAtTier(3, t3);
						
						for (int t2 = -1; t2 < weaponToTest.getModsAtTier(2).length; t2++) {
							weaponToTest.setSelectedModAtTier(2, t2);
							
							for (int t1 = -1; t1 < weaponToTest.getModsAtTier(1).length; t1++) {
								weaponToTest.setSelectedModAtTier(1, t1);
								
								// Because this will generate thousands of lines of data, never print to console.
								calculateStatsAndPrint(false, exportStatsToCSV);
								
								// Single-target calculations
								if (weaponToTest.calculateIdealBurstDPS() > bestIdealBurstDPS.calculateIdealBurstDPS()) {
									bestIdealBurstDPS = weaponToTest.clone();
								}
								if (weaponToTest.calculateIdealSustainedDPS() > bestIdealSustainedDPS.calculateIdealSustainedDPS()) {
									bestIdealSustainedDPS = weaponToTest.clone();
								}
								if (weaponToTest.sustainedWeakpointDPS() > bestWeakpointSustainedDPS.sustainedWeakpointDPS()) {
									bestWeakpointSustainedDPS = weaponToTest.clone();
								}
								if (weaponToTest.sustainedWeakpointAccuracyDPS() > bestWeakpointAccuracySustainedDPS.sustainedWeakpointAccuracyDPS()) {
									bestWeakpointAccuracySustainedDPS = weaponToTest.clone();
								}
								
								// Multi-target calculations
								if (weaponToTest.calculateAdditionalTargetDPS() > bestAdditionalTargetDPS.calculateAdditionalTargetDPS()) {
									bestAdditionalTargetDPS = weaponToTest.clone();
								}
								if (weaponToTest.calculateMaxMultiTargetDamage() > mostMultiTargetDamage.calculateMaxMultiTargetDamage()) {
									mostMultiTargetDamage = weaponToTest.clone();
								}
								
								// Non-damage calculations
								if (weaponToTest.calculateMaxNumTargets() > mostNumTargets.calculateMaxNumTargets()) {
									mostNumTargets = weaponToTest.clone();
								}
								if (weaponToTest.calculateFiringDuration() > longestFiringDuration.calculateFiringDuration()) {
									longestFiringDuration = weaponToTest.clone();
								}
								if (weaponToTest.averageTimeToKill() < fastestTTK.averageTimeToKill()) {
									fastestTTK = weaponToTest.clone();
								}
								if (weaponToTest.averageOverkill() < lowestOverkill.averageOverkill()) {
									lowestOverkill = weaponToTest.clone();
								}
								if (weaponToTest.estimatedAccuracy() > bestAccuracy.estimatedAccuracy()) {
									bestAccuracy = weaponToTest.clone();
								}
								if (weaponToTest.utilityScore() > bestUtility.utilityScore()) {
									bestUtility = weaponToTest.clone();
								}
							}
						}
					}
				}
			}
		}
		
		if (printStatsToConsole) {
			System.out.println("Best build combinations for each category:");
			System.out.println("	Best Ideal Burst DPS: " + bestIdealBurstDPS.getCombination() + " at " + bestIdealBurstDPS.calculateIdealBurstDPS()  + " DPS");
			System.out.println("	Best Ideal Sustained DPS: " + bestIdealSustainedDPS.getCombination() + " at " + bestIdealSustainedDPS.calculateIdealSustainedDPS() + " DPS");
			System.out.println("	Best Sustained + Weakpoint DPS: " + bestWeakpointSustainedDPS.getCombination() + " at " + bestWeakpointSustainedDPS.sustainedWeakpointDPS() + " DPS");
			System.out.println("	Best Sustained + Weakpoint + Accuracy DPS: " + bestWeakpointAccuracySustainedDPS.getCombination() + " at " + bestWeakpointAccuracySustainedDPS.sustainedWeakpointAccuracyDPS() + " DPS");
			System.out.println("	Best Additional Target DPS: " + bestAdditionalTargetDPS.getCombination() + " at " + bestAdditionalTargetDPS.calculateAdditionalTargetDPS() + " extra DPS per additional target");
			System.out.println("	Most damage dealt to multiple targets: " + mostMultiTargetDamage.getCombination() + " at " + mostMultiTargetDamage.calculateMaxMultiTargetDamage() + " damage");
			System.out.println("	Most number of targets hit per projectile: " + mostNumTargets.getCombination() + " at " + mostNumTargets.calculateMaxNumTargets() + " targets per projectile");
			System.out.println("	Longest time to fire all projectiles: " + longestFiringDuration.getCombination() + " at " + longestFiringDuration.calculateFiringDuration() + " sec");
			System.out.println("	Shortest average Time To Kill: " + fastestTTK.getCombination() + " at " + fastestTTK.averageTimeToKill() + " sec");
			System.out.println("	Lowest average Overkill: " + lowestOverkill.getCombination() + " at " + lowestOverkill.averageOverkill() + "%");
			System.out.println("	Most Accurate: " + bestAccuracy.getCombination() + " at " + bestAccuracy.estimatedAccuracy() + "%");
			System.out.println("	Most Utility: " + bestUtility.getCombination() + " at " + bestUtility.utilityScore());
		}
		if (exportStatsToCSV) {
			// Open the CSV file once, then dump the accumulated ArrayList of lines all at once to minimize I/O time
			try {
				// Set append=True so that it appends the lines after the header line
				FileWriter CSVwriter = new FileWriter(csvFilePath, true);
				for (String line: csvLinesToWrite) {
					CSVwriter.append(line);
				}
				CSVwriter.flush();
				CSVwriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// Finally, set the Weapon back to the mods/oc combination that it started at before the test.
		weaponToTest.buildFromCombination(currentCombination);
	}
	
	private void calculateStatsAndPrint(boolean console, boolean csv) {
		// There is a niche where both console and csv can be false; in the 6 nested for-loops in runTest() console will always be false, 
		// and csv could be false because of the run parameters. If they're both false, don't do anything in this method to save some CPU time.
		if (!console && !csv) {
			return;
		}
		
		String combination = weaponToTest.getCombination();
		
		double[] metrics = weaponToTest.getMetrics();
		
		if (console) {
			printStatsToConsole(combination, metrics);
		}
		if (csv) {
			printStatsToCSV(combination, metrics);
		}
	}
	
	private void printStatsToConsole(String combination, double[] metrics) {
		String format = "%s,\t\t%f,\t%f,\t\t%f,\t\t\t%f,\t\t\t\t%f,\t\t\t%f,\t\t%f,\t%f,\t%f,\t%f,\t%f,\t%f\n";
		System.out.printf(format, combination, metrics[0], metrics[1], metrics[2], metrics[3], metrics[4], metrics[5], 
						  metrics[6], metrics[7], metrics[8], metrics[9], metrics[10], metrics[11]);
	}
	
	private void printStatsToCSV(String combination, double[] metrics) {
		String format = "%s, %f, %f, %f, %f, %d, %f, %f, %f, %f, %f, %f, %f,\n";
		csvLinesToWrite.add((String.format(format, combination, metrics[0], metrics[1], metrics[2], metrics[3], metrics[4], metrics[5], 
				  			 metrics[6], metrics[7], metrics[8], metrics[9], metrics[10], metrics[11])));
	}
	
	public String getBestBurstDPSCombination() {
		Weapon bestBurstDPS = weaponToTest.clone();
		
		// The overclocks are the outermost loop because they should change last, and tier 1 is the innermost loop since it should change first.
		for (int oc = -1; oc < weaponToTest.getOverclocks().length; oc++) {
			weaponToTest.setSelectedOverclock(oc);
			
			for (int t5 = -1; t5 < weaponToTest.getModsAtTier(5).length; t5++) {
				weaponToTest.setSelectedModAtTier(5, t5);
				
				for (int t4 = -1; t4 < weaponToTest.getModsAtTier(4).length; t4++) {
					weaponToTest.setSelectedModAtTier(4, t4);
					
					for (int t3 = -1; t3 < weaponToTest.getModsAtTier(3).length; t3++) {
						weaponToTest.setSelectedModAtTier(3, t3);
						
						for (int t2 = -1; t2 < weaponToTest.getModsAtTier(2).length; t2++) {
							weaponToTest.setSelectedModAtTier(2, t2);
							
							for (int t1 = -1; t1 < weaponToTest.getModsAtTier(1).length; t1++) {
								weaponToTest.setSelectedModAtTier(1, t1);
								
								if (weaponToTest.calculateIdealBurstDPS() > bestBurstDPS.calculateIdealBurstDPS()) {
									bestBurstDPS = weaponToTest.clone();
								}
							}
						}
					}
				}
			}
		}
		
		return bestBurstDPS.getCombination();
	}
	
	public String getBestSustainedDPSCombination() {
		Weapon bestSustainedDPS = weaponToTest.clone();
		
		// The overclocks are the outermost loop because they should change last, and tier 1 is the innermost loop since it should change first.
		for (int oc = -1; oc < weaponToTest.getOverclocks().length; oc++) {
			weaponToTest.setSelectedOverclock(oc);
			
			for (int t5 = -1; t5 < weaponToTest.getModsAtTier(5).length; t5++) {
				weaponToTest.setSelectedModAtTier(5, t5);
				
				for (int t4 = -1; t4 < weaponToTest.getModsAtTier(4).length; t4++) {
					weaponToTest.setSelectedModAtTier(4, t4);
					
					for (int t3 = -1; t3 < weaponToTest.getModsAtTier(3).length; t3++) {
						weaponToTest.setSelectedModAtTier(3, t3);
						
						for (int t2 = -1; t2 < weaponToTest.getModsAtTier(2).length; t2++) {
							weaponToTest.setSelectedModAtTier(2, t2);
							
							for (int t1 = -1; t1 < weaponToTest.getModsAtTier(1).length; t1++) {
								weaponToTest.setSelectedModAtTier(1, t1);
								
								if (weaponToTest.calculateIdealSustainedDPS() > bestSustainedDPS.calculateIdealSustainedDPS()) {
									bestSustainedDPS = weaponToTest.clone();
								}
							}
						}
					}
				}
			}
		}
		
		return bestSustainedDPS.getCombination();
	}
	
	public String getBestSustainedWeakpointDPSCombination() {
		Weapon bestWeakpointSustainedDPS = weaponToTest.clone();
		
		// The overclocks are the outermost loop because they should change last, and tier 1 is the innermost loop since it should change first.
		for (int oc = -1; oc < weaponToTest.getOverclocks().length; oc++) {
			weaponToTest.setSelectedOverclock(oc);
			
			for (int t5 = -1; t5 < weaponToTest.getModsAtTier(5).length; t5++) {
				weaponToTest.setSelectedModAtTier(5, t5);
				
				for (int t4 = -1; t4 < weaponToTest.getModsAtTier(4).length; t4++) {
					weaponToTest.setSelectedModAtTier(4, t4);
					
					for (int t3 = -1; t3 < weaponToTest.getModsAtTier(3).length; t3++) {
						weaponToTest.setSelectedModAtTier(3, t3);
						
						for (int t2 = -1; t2 < weaponToTest.getModsAtTier(2).length; t2++) {
							weaponToTest.setSelectedModAtTier(2, t2);
							
							for (int t1 = -1; t1 < weaponToTest.getModsAtTier(1).length; t1++) {
								weaponToTest.setSelectedModAtTier(1, t1);
								
								if (weaponToTest.sustainedWeakpointDPS() > bestWeakpointSustainedDPS.sustainedWeakpointDPS()) {
									bestWeakpointSustainedDPS = weaponToTest.clone();
								}
							}
						}
					}
				}
			}
		}
		
		return bestWeakpointSustainedDPS.getCombination();
	}
	
	public String getBestSustainedWeakpointAccuracyDPSCombination() {
		Weapon bestWeakpointAccuracySustainedDPS = weaponToTest.clone();
		
		// The overclocks are the outermost loop because they should change last, and tier 1 is the innermost loop since it should change first.
		for (int oc = -1; oc < weaponToTest.getOverclocks().length; oc++) {
			weaponToTest.setSelectedOverclock(oc);
			
			for (int t5 = -1; t5 < weaponToTest.getModsAtTier(5).length; t5++) {
				weaponToTest.setSelectedModAtTier(5, t5);
				
				for (int t4 = -1; t4 < weaponToTest.getModsAtTier(4).length; t4++) {
					weaponToTest.setSelectedModAtTier(4, t4);
					
					for (int t3 = -1; t3 < weaponToTest.getModsAtTier(3).length; t3++) {
						weaponToTest.setSelectedModAtTier(3, t3);
						
						for (int t2 = -1; t2 < weaponToTest.getModsAtTier(2).length; t2++) {
							weaponToTest.setSelectedModAtTier(2, t2);
							
							for (int t1 = -1; t1 < weaponToTest.getModsAtTier(1).length; t1++) {
								weaponToTest.setSelectedModAtTier(1, t1);
								
								if (weaponToTest.sustainedWeakpointAccuracyDPS() > bestWeakpointAccuracySustainedDPS.sustainedWeakpointAccuracyDPS()) {
									bestWeakpointAccuracySustainedDPS = weaponToTest.clone();
								}
							}
						}
					}
				}
			}
		}
		
		return bestWeakpointAccuracySustainedDPS.getCombination();
	}
	
	public String getBestAdditionalTargetDPSCombination() {
		Weapon bestAdditionalTargetDPS = weaponToTest.clone();
		
		// The overclocks are the outermost loop because they should change last, and tier 1 is the innermost loop since it should change first.
		for (int oc = -1; oc < weaponToTest.getOverclocks().length; oc++) {
			weaponToTest.setSelectedOverclock(oc);
			
			for (int t5 = -1; t5 < weaponToTest.getModsAtTier(5).length; t5++) {
				weaponToTest.setSelectedModAtTier(5, t5);
				
				for (int t4 = -1; t4 < weaponToTest.getModsAtTier(4).length; t4++) {
					weaponToTest.setSelectedModAtTier(4, t4);
					
					for (int t3 = -1; t3 < weaponToTest.getModsAtTier(3).length; t3++) {
						weaponToTest.setSelectedModAtTier(3, t3);
						
						for (int t2 = -1; t2 < weaponToTest.getModsAtTier(2).length; t2++) {
							weaponToTest.setSelectedModAtTier(2, t2);
							
							for (int t1 = -1; t1 < weaponToTest.getModsAtTier(1).length; t1++) {
								weaponToTest.setSelectedModAtTier(1, t1);
								
								if (weaponToTest.calculateAdditionalTargetDPS() > bestAdditionalTargetDPS.calculateAdditionalTargetDPS()) {
									bestAdditionalTargetDPS = weaponToTest.clone();
								}
							}
						}
					}
				}
			}
		}
		
		return bestAdditionalTargetDPS.getCombination();
	}
	
	public String getHighestMultiTargetDamageCombination() {
		Weapon mostMultiTargetDamage = weaponToTest.clone();
		
		// The overclocks are the outermost loop because they should change last, and tier 1 is the innermost loop since it should change first.
		for (int oc = -1; oc < weaponToTest.getOverclocks().length; oc++) {
			weaponToTest.setSelectedOverclock(oc);
			
			for (int t5 = -1; t5 < weaponToTest.getModsAtTier(5).length; t5++) {
				weaponToTest.setSelectedModAtTier(5, t5);
				
				for (int t4 = -1; t4 < weaponToTest.getModsAtTier(4).length; t4++) {
					weaponToTest.setSelectedModAtTier(4, t4);
					
					for (int t3 = -1; t3 < weaponToTest.getModsAtTier(3).length; t3++) {
						weaponToTest.setSelectedModAtTier(3, t3);
						
						for (int t2 = -1; t2 < weaponToTest.getModsAtTier(2).length; t2++) {
							weaponToTest.setSelectedModAtTier(2, t2);
							
							for (int t1 = -1; t1 < weaponToTest.getModsAtTier(1).length; t1++) {
								weaponToTest.setSelectedModAtTier(1, t1);
								
								if (weaponToTest.calculateMaxMultiTargetDamage() > mostMultiTargetDamage.calculateMaxMultiTargetDamage()) {
									mostMultiTargetDamage = weaponToTest.clone();
								}
							}
						}
					}
				}
			}
		}
		
		return mostMultiTargetDamage.getCombination();
	}
	
	public String getMostNumTargetsCombination() {
		Weapon mostNumTargets = weaponToTest.clone();
		
		// The overclocks are the outermost loop because they should change last, and tier 1 is the innermost loop since it should change first.
		for (int oc = -1; oc < weaponToTest.getOverclocks().length; oc++) {
			weaponToTest.setSelectedOverclock(oc);
			
			for (int t5 = -1; t5 < weaponToTest.getModsAtTier(5).length; t5++) {
				weaponToTest.setSelectedModAtTier(5, t5);
				
				for (int t4 = -1; t4 < weaponToTest.getModsAtTier(4).length; t4++) {
					weaponToTest.setSelectedModAtTier(4, t4);
					
					for (int t3 = -1; t3 < weaponToTest.getModsAtTier(3).length; t3++) {
						weaponToTest.setSelectedModAtTier(3, t3);
						
						for (int t2 = -1; t2 < weaponToTest.getModsAtTier(2).length; t2++) {
							weaponToTest.setSelectedModAtTier(2, t2);
							
							for (int t1 = -1; t1 < weaponToTest.getModsAtTier(1).length; t1++) {
								weaponToTest.setSelectedModAtTier(1, t1);
								
								if (weaponToTest.calculateMaxNumTargets() > mostNumTargets.calculateMaxNumTargets()) {
									mostNumTargets = weaponToTest.clone();
								}
							}
						}
					}
				}
			}
		}
		
		return mostNumTargets.getCombination();
	}
	
	public String getLongestFiringDurationCombination() {
		Weapon longestFiringDuration = weaponToTest.clone();
		
		// The overclocks are the outermost loop because they should change last, and tier 1 is the innermost loop since it should change first.
		for (int oc = -1; oc < weaponToTest.getOverclocks().length; oc++) {
			weaponToTest.setSelectedOverclock(oc);
			
			for (int t5 = -1; t5 < weaponToTest.getModsAtTier(5).length; t5++) {
				weaponToTest.setSelectedModAtTier(5, t5);
				
				for (int t4 = -1; t4 < weaponToTest.getModsAtTier(4).length; t4++) {
					weaponToTest.setSelectedModAtTier(4, t4);
					
					for (int t3 = -1; t3 < weaponToTest.getModsAtTier(3).length; t3++) {
						weaponToTest.setSelectedModAtTier(3, t3);
						
						for (int t2 = -1; t2 < weaponToTest.getModsAtTier(2).length; t2++) {
							weaponToTest.setSelectedModAtTier(2, t2);
							
							for (int t1 = -1; t1 < weaponToTest.getModsAtTier(1).length; t1++) {
								weaponToTest.setSelectedModAtTier(1, t1);
								
								if (weaponToTest.calculateFiringDuration() > longestFiringDuration.calculateFiringDuration()) {
									longestFiringDuration = weaponToTest.clone();
								}
							}
						}
					}
				}
			}
		}
		
		return longestFiringDuration.getCombination();
	}
	
	public String getShortestTimeToKillCombination() {
		Weapon fastestTTK = weaponToTest.clone();
		
		// The overclocks are the outermost loop because they should change last, and tier 1 is the innermost loop since it should change first.
		for (int oc = -1; oc < weaponToTest.getOverclocks().length; oc++) {
			weaponToTest.setSelectedOverclock(oc);
			
			for (int t5 = -1; t5 < weaponToTest.getModsAtTier(5).length; t5++) {
				weaponToTest.setSelectedModAtTier(5, t5);
				
				for (int t4 = -1; t4 < weaponToTest.getModsAtTier(4).length; t4++) {
					weaponToTest.setSelectedModAtTier(4, t4);
					
					for (int t3 = -1; t3 < weaponToTest.getModsAtTier(3).length; t3++) {
						weaponToTest.setSelectedModAtTier(3, t3);
						
						for (int t2 = -1; t2 < weaponToTest.getModsAtTier(2).length; t2++) {
							weaponToTest.setSelectedModAtTier(2, t2);
							
							for (int t1 = -1; t1 < weaponToTest.getModsAtTier(1).length; t1++) {
								weaponToTest.setSelectedModAtTier(1, t1);
								
								if (weaponToTest.averageTimeToKill() < fastestTTK.averageTimeToKill()) {
									fastestTTK = weaponToTest.clone();
								}
							}
						}
					}
				}
			}
		}
		
		return fastestTTK.getCombination();
	}
	
	public String getLowestOverkillCombination() {
		Weapon lowestOverkill = weaponToTest.clone();
		
		// The overclocks are the outermost loop because they should change last, and tier 1 is the innermost loop since it should change first.
		for (int oc = -1; oc < weaponToTest.getOverclocks().length; oc++) {
			weaponToTest.setSelectedOverclock(oc);
			
			for (int t5 = -1; t5 < weaponToTest.getModsAtTier(5).length; t5++) {
				weaponToTest.setSelectedModAtTier(5, t5);
				
				for (int t4 = -1; t4 < weaponToTest.getModsAtTier(4).length; t4++) {
					weaponToTest.setSelectedModAtTier(4, t4);
					
					for (int t3 = -1; t3 < weaponToTest.getModsAtTier(3).length; t3++) {
						weaponToTest.setSelectedModAtTier(3, t3);
						
						for (int t2 = -1; t2 < weaponToTest.getModsAtTier(2).length; t2++) {
							weaponToTest.setSelectedModAtTier(2, t2);
							
							for (int t1 = -1; t1 < weaponToTest.getModsAtTier(1).length; t1++) {
								weaponToTest.setSelectedModAtTier(1, t1);
								
								if (weaponToTest.averageOverkill() < lowestOverkill.averageOverkill()) {
									lowestOverkill = weaponToTest.clone();
								}
							}
						}
					}
				}
			}
		}
		
		return lowestOverkill.getCombination();
	}
	
	public String getHighestAccuracyCombination() {
		Weapon bestAccuracy = weaponToTest.clone();
		
		// The overclocks are the outermost loop because they should change last, and tier 1 is the innermost loop since it should change first.
		for (int oc = -1; oc < weaponToTest.getOverclocks().length; oc++) {
			weaponToTest.setSelectedOverclock(oc);
			
			for (int t5 = -1; t5 < weaponToTest.getModsAtTier(5).length; t5++) {
				weaponToTest.setSelectedModAtTier(5, t5);
				
				for (int t4 = -1; t4 < weaponToTest.getModsAtTier(4).length; t4++) {
					weaponToTest.setSelectedModAtTier(4, t4);
					
					for (int t3 = -1; t3 < weaponToTest.getModsAtTier(3).length; t3++) {
						weaponToTest.setSelectedModAtTier(3, t3);
						
						for (int t2 = -1; t2 < weaponToTest.getModsAtTier(2).length; t2++) {
							weaponToTest.setSelectedModAtTier(2, t2);
							
							for (int t1 = -1; t1 < weaponToTest.getModsAtTier(1).length; t1++) {
								weaponToTest.setSelectedModAtTier(1, t1);
								
								if (weaponToTest.estimatedAccuracy() > bestAccuracy.estimatedAccuracy()) {
									bestAccuracy = weaponToTest.clone();
								}
							}
						}
					}
				}
			}
		}
		
		return bestAccuracy.getCombination();
	}
	
	public String getMostUtilityCombination() {
		Weapon bestUtility = weaponToTest.clone();
		
		// The overclocks are the outermost loop because they should change last, and tier 1 is the innermost loop since it should change first.
		for (int oc = -1; oc < weaponToTest.getOverclocks().length; oc++) {
			weaponToTest.setSelectedOverclock(oc);
			
			for (int t5 = -1; t5 < weaponToTest.getModsAtTier(5).length; t5++) {
				weaponToTest.setSelectedModAtTier(5, t5);
				
				for (int t4 = -1; t4 < weaponToTest.getModsAtTier(4).length; t4++) {
					weaponToTest.setSelectedModAtTier(4, t4);
					
					for (int t3 = -1; t3 < weaponToTest.getModsAtTier(3).length; t3++) {
						weaponToTest.setSelectedModAtTier(3, t3);
						
						for (int t2 = -1; t2 < weaponToTest.getModsAtTier(2).length; t2++) {
							weaponToTest.setSelectedModAtTier(2, t2);
							
							for (int t1 = -1; t1 < weaponToTest.getModsAtTier(1).length; t1++) {
								weaponToTest.setSelectedModAtTier(1, t1);
								
								if (weaponToTest.utilityScore() > bestUtility.utilityScore()) {
									bestUtility = weaponToTest.clone();
								}
							}
						}
					}
				}
			}
		}
		
		return bestUtility.getCombination();
	}
}
