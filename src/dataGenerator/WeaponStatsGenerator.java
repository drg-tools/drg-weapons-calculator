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
		String bestIdealBurstDPSCombination = weaponToTest.getCombination();
		double bestIdealBurstDPS = weaponToTest.calculateIdealBurstDPS();
		
		String bestIdealSustainedDPSCombination = bestIdealBurstDPSCombination;
		double bestIdealSustainedDPS = weaponToTest.calculateIdealSustainedDPS();
		
		String bestWeakpointSustainedDPSCombination = bestIdealBurstDPSCombination;
		double bestWeakpointSustainedDPS = weaponToTest.sustainedWeakpointDPS();
		
		String bestWeakpointAccuracySustainedDPSCombination = bestIdealBurstDPSCombination;
		double bestWeakpointAccuracySustainedDPS = weaponToTest.sustainedWeakpointAccuracyDPS();
		
		String bestAdditionalTargetDPSCombination = bestIdealBurstDPSCombination;
		double bestAdditionalTargetDPS = weaponToTest.calculateAdditionalTargetDPS();
		
		String mostMultiTargetDamageCombination = bestIdealBurstDPSCombination;
		double mostMultiTargetDamage = weaponToTest.calculateMaxMultiTargetDamage();
		
		String mostNumTargetsCombination = bestIdealBurstDPSCombination;
		double mostNumTargets = weaponToTest.calculateMaxNumTargets();
		
		String longestFiringDurationCombination = bestIdealBurstDPSCombination;
		double longestFiringDuration = weaponToTest.calculateFiringDuration();
		
		String fastestTTKCombination = bestIdealBurstDPSCombination;
		double fastestTTK = weaponToTest.averageTimeToKill();
		
		String lowestOverkillCombination = bestIdealBurstDPSCombination;
		double lowestOverkill = weaponToTest.averageOverkill();
		
		String bestAccuracyCombination = bestIdealBurstDPSCombination;
		double bestAccuracy = weaponToTest.estimatedAccuracy(false);
		
		String bestUtilityCombination = bestIdealBurstDPSCombination;
		double bestUtility = weaponToTest.utilityScore();
		
		double currentBurst, currentSustained, currentSustainedWeakpoint, currentSustainedWeakpointAccuracy, currentAdditional, currentMaxDamage, 
				currentNumTargets, currentFiringDuration, currentTTK, currentOverkill, currentAccuracy, currentUtility;
		String forLoopsCombination;
		
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
								
								forLoopsCombination = weaponToTest.getCombination();
								
								// Single-target calculations
								currentBurst = weaponToTest.calculateIdealBurstDPS();
								if (currentBurst > bestIdealBurstDPS) {
									bestIdealBurstDPSCombination = forLoopsCombination;
									bestIdealBurstDPS = currentBurst;
								}
								currentSustained = weaponToTest.calculateIdealSustainedDPS();
								if (currentSustained > bestIdealSustainedDPS) {
									bestIdealSustainedDPSCombination = forLoopsCombination;
									bestIdealSustainedDPS = currentSustained;
								}
								currentSustainedWeakpoint = weaponToTest.sustainedWeakpointDPS();
								if (currentSustainedWeakpoint > bestWeakpointSustainedDPS) {
									bestWeakpointSustainedDPSCombination = forLoopsCombination;
									bestWeakpointSustainedDPS = currentSustainedWeakpoint;
								}
								currentSustainedWeakpointAccuracy = weaponToTest.sustainedWeakpointAccuracyDPS();
								if (currentSustainedWeakpointAccuracy > bestWeakpointAccuracySustainedDPS) {
									bestWeakpointAccuracySustainedDPSCombination = forLoopsCombination;
									bestWeakpointAccuracySustainedDPS = currentSustainedWeakpointAccuracy;
								}
								
								// Multi-target calculations
								currentAdditional = weaponToTest.calculateAdditionalTargetDPS();
								if (currentAdditional > bestAdditionalTargetDPS) {
									bestAdditionalTargetDPSCombination = forLoopsCombination;
									bestAdditionalTargetDPS = currentAdditional;
								}
								currentMaxDamage = weaponToTest.calculateMaxMultiTargetDamage();
								if (currentMaxDamage > mostMultiTargetDamage) {
									mostMultiTargetDamageCombination = forLoopsCombination;
									mostMultiTargetDamage = currentMaxDamage;
								}
								
								// Non-damage calculations
								currentNumTargets = weaponToTest.calculateMaxNumTargets();
								if (currentNumTargets > mostNumTargets) {
									mostNumTargetsCombination = forLoopsCombination;
									mostNumTargets = currentNumTargets;
								}
								currentFiringDuration = weaponToTest.calculateFiringDuration();
								if (currentFiringDuration > longestFiringDuration) {
									longestFiringDurationCombination = forLoopsCombination;
									longestFiringDuration = currentFiringDuration;
								}
								currentTTK = weaponToTest.averageTimeToKill();
								if (currentTTK < fastestTTK) {
									fastestTTKCombination = forLoopsCombination;
									fastestTTK = currentTTK;
								}
								currentOverkill = weaponToTest.averageOverkill();
								if (currentOverkill < lowestOverkill) {
									lowestOverkillCombination = forLoopsCombination;
									lowestOverkill = currentOverkill;
								}
								currentAccuracy = weaponToTest.estimatedAccuracy(false);
								if (currentAccuracy > bestAccuracy) {
									bestAccuracyCombination = forLoopsCombination;
									bestAccuracy = currentAccuracy;
								}
								currentUtility = weaponToTest.utilityScore();
								if (currentUtility > bestUtility) {
									bestUtilityCombination = forLoopsCombination;
									bestUtility = currentUtility;
								}
							}
						}
					}
				}
			}
		}
		
		if (printStatsToConsole) {
			System.out.println("Best build combinations for each category:");
			System.out.println("	Best Ideal Burst DPS: " + bestIdealBurstDPSCombination + " at " + bestIdealBurstDPS  + " DPS");
			System.out.println("	Best Ideal Sustained DPS: " + bestIdealSustainedDPSCombination + " at " + bestIdealSustainedDPS + " DPS");
			System.out.println("	Best Sustained + Weakpoint DPS: " + bestWeakpointSustainedDPSCombination + " at " + bestWeakpointSustainedDPS + " DPS");
			System.out.println("	Best Sustained + Weakpoint + Accuracy DPS: " + bestWeakpointAccuracySustainedDPSCombination + " at " + bestWeakpointAccuracySustainedDPS + " DPS");
			System.out.println("	Best Additional Target DPS: " + bestAdditionalTargetDPSCombination + " at " + bestAdditionalTargetDPS + " extra DPS per additional target");
			System.out.println("	Most damage dealt to multiple targets: " + mostMultiTargetDamageCombination + " at " + mostMultiTargetDamage + " damage");
			System.out.println("	Most number of targets hit per projectile: " + mostNumTargetsCombination + " at " + mostNumTargets + " targets per projectile");
			System.out.println("	Longest time to fire all projectiles: " + longestFiringDurationCombination + " at " + longestFiringDuration + " sec");
			System.out.println("	Shortest average Time To Kill: " + fastestTTKCombination + " at " + fastestTTK + " sec");
			System.out.println("	Lowest average Overkill: " + lowestOverkillCombination + " at " + lowestOverkill + "%");
			System.out.println("	Most Accurate: " + bestAccuracyCombination + " at " + bestAccuracy + "%");
			System.out.println("	Most Utility: " + bestUtilityCombination + " at " + bestUtility);
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
	
	public String getBestIdealBurstDPSCombination() {
		String bestBurstDPSCombination = weaponToTest.getCombination();
		double bestBurstDPS = weaponToTest.calculateIdealBurstDPS();
		double currentBurstDPS;
		
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
								
								currentBurstDPS = weaponToTest.calculateIdealBurstDPS();
								if (currentBurstDPS > bestBurstDPS) {
									bestBurstDPSCombination = weaponToTest.getCombination();
									bestBurstDPS = currentBurstDPS;
								}
							}
						}
					}
				}
			}
		}
		
		return bestBurstDPSCombination;
	}
	
	public String getBestIdealSustainedDPSCombination() {
		String bestSustainedDPSCombination = weaponToTest.getCombination();
		double bestSustainedDPS = weaponToTest.calculateIdealSustainedDPS();
		double currentSustainedDPS;
		
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
								
								currentSustainedDPS = weaponToTest.calculateIdealSustainedDPS();
								if (currentSustainedDPS > bestSustainedDPS) {
									bestSustainedDPSCombination = weaponToTest.getCombination();
									bestSustainedDPS = currentSustainedDPS;
								}
							}
						}
					}
				}
			}
		}
		
		return bestSustainedDPSCombination;
	}
	
	public String getBestSustainedWeakpointDPSCombination() {
		String bestWeakpointSustainedDPSCombination = weaponToTest.getCombination();
		double bestWeakpointSustainedDPS = weaponToTest.sustainedWeakpointDPS();
		double currentWeakpointSustainedDPS;
		
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
								
								currentWeakpointSustainedDPS = weaponToTest.sustainedWeakpointDPS();
								if (currentWeakpointSustainedDPS > bestWeakpointSustainedDPS) {
									bestWeakpointSustainedDPSCombination = weaponToTest.getCombination();
									bestWeakpointSustainedDPS = currentWeakpointSustainedDPS;
								}
							}
						}
					}
				}
			}
		}
		
		return bestWeakpointSustainedDPSCombination;
	}
	
	public String getBestSustainedWeakpointAccuracyDPSCombination() {
		String bestWeakpointAccuracySustainedDPSCombination = weaponToTest.getCombination();
		double bestWeakpointAccuracySustainedDPS = weaponToTest.sustainedWeakpointAccuracyDPS();
		double currentWeakpointAccuracySustainedDPS;
		
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
								
								currentWeakpointAccuracySustainedDPS = weaponToTest.sustainedWeakpointAccuracyDPS();
								if (currentWeakpointAccuracySustainedDPS > bestWeakpointAccuracySustainedDPS) {
									bestWeakpointAccuracySustainedDPSCombination = weaponToTest.getCombination();
									bestWeakpointAccuracySustainedDPS = currentWeakpointAccuracySustainedDPS;
								}
							}
						}
					}
				}
			}
		}
		
		return bestWeakpointAccuracySustainedDPSCombination;
	}
	
	public String getBestIdealAdditionalTargetDPSCombination() {
		String bestAdditionalTargetDPSCombination = weaponToTest.getCombination();
		double bestAdditionalTargetDPS = weaponToTest.calculateAdditionalTargetDPS();
		double currentAdditionalTargetDPS;
		
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
								
								currentAdditionalTargetDPS = weaponToTest.calculateAdditionalTargetDPS();
								if (currentAdditionalTargetDPS > bestAdditionalTargetDPS) {
									bestAdditionalTargetDPSCombination = weaponToTest.getCombination();
									bestAdditionalTargetDPS = currentAdditionalTargetDPS;
								}
							}
						}
					}
				}
			}
		}
		
		return bestAdditionalTargetDPSCombination;
	}
	
	public String getHighestMultiTargetDamageCombination() {
		String mostMultiTargetDamageCombination = weaponToTest.getCombination();
		double mostMultiTargetDamage = weaponToTest.calculateMaxMultiTargetDamage();
		double currentMultiTargetDamage;
		
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
								
								currentMultiTargetDamage = weaponToTest.calculateMaxMultiTargetDamage();
								if (currentMultiTargetDamage > mostMultiTargetDamage) {
									mostMultiTargetDamageCombination = weaponToTest.getCombination();
									mostMultiTargetDamage = currentMultiTargetDamage;
								}
							}
						}
					}
				}
			}
		}
		
		return mostMultiTargetDamageCombination;
	}
	
	public String getMostNumTargetsCombination() {
		String mostNumTargetsCombination = weaponToTest.getCombination();
		double mostNumTargets = weaponToTest.calculateMaxNumTargets();
		double currentNumTargets;
		
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
								
								currentNumTargets = weaponToTest.calculateMaxNumTargets();
								if (currentNumTargets > mostNumTargets) {
									mostNumTargetsCombination = weaponToTest.getCombination();
									mostNumTargets = currentNumTargets;
								}
							}
						}
					}
				}
			}
		}
		
		return mostNumTargetsCombination;
	}
	
	public String getLongestFiringDurationCombination() {
		String longestFiringDurationCombination = weaponToTest.getCombination();
		double longestFiringDuration = weaponToTest.calculateFiringDuration();
		double currentFiringDuration;
		
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
								
								currentFiringDuration = weaponToTest.calculateFiringDuration();
								if (currentFiringDuration > longestFiringDuration) {
									longestFiringDurationCombination = weaponToTest.getCombination();
									longestFiringDuration = currentFiringDuration;
								}
							}
						}
					}
				}
			}
		}
		
		return longestFiringDurationCombination;
	}
	
	public String getShortestTimeToKillCombination() {
		String fastestTTKCombination = weaponToTest.getCombination();
		double fastestTTK = weaponToTest.averageTimeToKill();
		double currentTTK;
		
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
								
								currentTTK = weaponToTest.averageTimeToKill();
								if (currentTTK < fastestTTK) {
									fastestTTKCombination = weaponToTest.getCombination();
									fastestTTK = currentTTK;
								}
							}
						}
					}
				}
			}
		}
		
		return fastestTTKCombination;
	}
	
	public String getLowestOverkillCombination() {
		String lowestOverkillCombination = weaponToTest.getCombination();
		double lowestOverkill = weaponToTest.averageOverkill();
		double currentOverkill;
		
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
								
								currentOverkill = weaponToTest.averageOverkill();
								if (currentOverkill < lowestOverkill) {
									lowestOverkillCombination = weaponToTest.getCombination();
									lowestOverkill = currentOverkill;
								}
							}
						}
					}
				}
			}
		}
		
		return lowestOverkillCombination;
	}
	
	public String getHighestAccuracyCombination() {
		String bestAccuracyCombination = weaponToTest.getCombination();
		double bestAccuracy = weaponToTest.estimatedAccuracy(false);
		double currentAccuracy;
		
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
								
								currentAccuracy = weaponToTest.estimatedAccuracy(false);
								if (currentAccuracy > bestAccuracy) {
									bestAccuracyCombination = weaponToTest.getCombination();
									bestAccuracy = currentAccuracy;
								}
							}
						}
					}
				}
			}
		}
		
		return bestAccuracyCombination;
	}
	
	public String getMostUtilityCombination() {
		String bestUtilityCombination = weaponToTest.getCombination();
		double bestUtility = weaponToTest.utilityScore();
		double currentUtility;
		
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
								
								currentUtility = weaponToTest.utilityScore();
								if (currentUtility > bestUtility) {
									bestUtilityCombination = weaponToTest.getCombination();
									bestUtility = currentUtility;
								}
							}
						}
					}
				}
			}
		}
		
		return bestUtilityCombination;
	}
}
