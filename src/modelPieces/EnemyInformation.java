package modelPieces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import utilities.MathUtils;

// TODO: remove Crassus Detonator from the enemy pool because it spawns more like a BET-C or Korlok than the common "swarm" enemies that this program uses for its models.
public class EnemyInformation {
	
	private static int hazardLevel = 4;
	private static int playerCount = 4;
	public static void setHazardLevel(int newHazLevel) {
		if (newHazLevel > 0 && newHazLevel < 6) {
			hazardLevel = newHazLevel;
		}
	}
	public static void setPlayerCount(int newPlayerCount) {
		if (newPlayerCount > 0 && newPlayerCount < 5) {
			playerCount = newPlayerCount;
		}
	}
	
	// These are the values that I guessed for the proportion of each enemy spawn type. It worked REALLY well for avg TTK-based mods like Cold as the Grave and Battle Cool, but it's not representative of the actual game.
	// All of these numbers must sum up to exactly 1.0 for it to be a probability vector.
	private static double[] guessedSpawnRates = {
		0.165, // Glyphid Swarmer
		0.25,  // Glyphid Grunt
		0.07,  // Glyphid Grunt Guard
		0.07,  // Glyphid Grunt Slasher
		0.04,  // Glyphid Praetorian
		0.04,  // Glyphid Exploder
		0.01,  // Glyphid Bulk Detonator
		0.005, // Glyphid Crassus Detonator
		0.04,  // Glyphid Webspitter
		0.02,  // Glyphid Acidspitter
		0.02,  // Glyphid Menace
		0.02,  // Glyphid Warden
		0.01,  // Glyphid Oppressor
		0.01,  // Q'ronar Shellback
		0.08,  // Mactera Spawn
		0.01,  // Mactera Grabber
		0.03,  // Mactera Bomber
		0.02,  // Naedocyte Breeder
		0.02,  // Glyphid Brood Nexus
		0.01,  // Spitball Infector
		0.01,  // Cave Leech
		0.04,  // Mactera Tri-Jaw
		0.01   // Mactera Brundle
	};
	
	/* 
		When U33 introduced the Tri-Jaw and Brundle common enemies, I had to redo these probabilities. To that end I chose to write down what the current kill counter was for every enemy type,
		and then play vanilla Haz4/5 until I achieved at least 15,000 Grunt kills. In the end it took me about 50 hours of playtime to achieve that, and I ended up with a total of 33,606 kills 
		of all kinds for these probability amounts. It's not as broad as U31's 153,000 kills from 6 players, but I didn't want to ask people to go 50 hours of playtime only on vanilla Haz4/5.
		
		Biome-specific enemies, "hatchling" enemy types, and Dreadnoughts not included.
		All of these numbers must sum up to exactly 1.0 for it to be a probability vector.
	*/
	private static double[] exactSpawnRates = {
		0.2503719574, 	 // Glyphid Swarmer
		0.4661369993,  	 // Glyphid Grunt
		0.05400821282,   // Glyphid Grunt Guard
		0.05838243171,   // Glyphid Grunt Slasher
		0.02074034399,   // Glyphid Praetorian
		0.03895137773,   // Glyphid Exploder
		0.001220020234,  // Glyphid Bulk Detonator
		0.00002975659108,// Glyphid Crassus Detonator
		0.02963756472,   // Glyphid Webspitter
		0.01276557758,   // Glyphid Acidspitter
		0.001577099328,  // Glyphid Menace
		0.002082961376,  // Glyphid Warden
		0.00330298161,   // Glyphid Oppressor
		0.001755638874,  // Q'ronar Shellback
		0.02550139856,   // Mactera Spawn
		0.001934178421,  // Mactera Grabber
		0.005088377076,  // Mactera Bomber
		0.000684401595,  // Naedocyte Breeder
		0.001666369101,  // Glyphid Brood Nexus
		0.003660060703,  // Spitball Infector
		0.004552758436,  // Cave Leech
		0.01282509076,   // Mactera Tri-Jaw
		0.003124442064   // Mactera Brundle
	};
	
	// These numbers are estimates of what percentage of bullets shot at each enemy type will hit the enemy's weakpoints
	private static double[] probabilityBulletHitsWeakpointPerEnemyType = {
		0.0,  // Glyphid Swarmer (no weakpoint)
		0.9,  // Glyphid Grunt
		0.5,  // Glyphid Grunt Guard
		0.9,  // Glyphid Grunt Slasher
		0.4,  // Glyphid Praetorian
		0.1,  // Glyphid Exploder
		0.2,  // Glyphid Bulk Detonator
		0.2,  // Glyphid Crassus Detonator
		0.1,  // Glyphid Webspitter
		0.4,  // Glyphid Acidspitter
		0.7,  // Glyphid Menace
		0.5,  // Glyphid Warden
		1.0,  // Glyphid Oppressor
		0.1,  // Q'ronar Shellback
		0.8,  // Mactera Spawn
		0.2,  // Mactera Grabber
		0.9,  // Mactera Bomber
		0.1,  // Naedocyte Breeder
		0.9,  // Glyphid Brood Nexus
		0.4,  // Spitball Infector
		0.0,  // Cave Leech (no weakpoint)
		0.8,  // Mactera Tri-Jaw
		0.6   // Mactera Brundle
	};

	// These numbers are taken straight from the Wiki
	private static double[] defaultWeakpointDamageBonusPerEnemyType = {
		0.0,  // Glyphid Swarmer (no weakpoint)
		2.0,  // Glyphid Grunt
		2.0,  // Glyphid Grunt Guard
		2.0,  // Glyphid Grunt Slasher
		1.0,  // Glyphid Praetorian (has a weakpoint, but it only takes normal damage without mods/OCs)
		2.0,  // Glyphid Exploder
		3.0,  // Glyphid Bulk Detonator
		3.0,  // Glyphid Crassus Detonator
		2.0,  // Glyphid Webspitter
		2.0,  // Glyphid Acidspitter
		2.0,  // Glyphid Menace
		3.0,  // Glyphid Warden
		1.0,  // Glyphid Oppressor (has a weakpoint, but it only takes normal damage without mods/OCs)
		2.0,  // Q'ronar Shellback
		3.0,  // Mactera Spawn
		3.0,  // Mactera Grabber
		3.0,  // Mactera Bomber
		3.0,  // Naedocyte Breeder
		2.0,  // Glyphid Brood Nexus
		2.0,  // Spitball Infector
		0.0,  // Cave Leech (no weakpoint)
		3.0,  // Mactera Tri-Jaw
		3.0   // Mactera Brundle
	};
	
	// These base values are just taken from the Wiki's default values; Hazard level and player count not factored in. (effectively Haz2, 4 players)
	private static double[] enemyHealthPools = {
		12,    // Glyphid Swarmer
		90,    // Glyphid Grunt
		270,   // Glyphid Grunt Guard
		148,   // Glyphid Grunt Slasher
		750,   // Glyphid Praetorian
		20,    // Glyphid Exploder
		4000,  // Glyphid Bulk Detonator
		6000,  // Glyphid Crassus Detonator
		40,    // Glyphid Webspitter
		120,   // Glyphid Acidspitter
		700,   // Glyphid Menace
		800,   // Glyphid Warden
		900,   // Glyphid Oppressor
		450,   // Q'ronar Shellback
		223,   // Mactera Spawn
		500,   // Mactera Grabber
		800,   // Mactera Bomber
		1500,  // Naedocyte Breeder
		1800,  // Glyphid Brood Nexus
		800,   // Spitball Infector
		100,   // Cave Leech
		350,   // Mactera Tri-Jaw
		600    // Mactera Brundle
	};
	
	// Normal enemies have their health scaled up or down depending on Hazard Level, with the notable exception that the health does not currently increase between Haz4 and haz5
	private static double[] normalEnemyResistances = {
		0.7,  // Haz1
		1.0,  // Haz2
		1.1,  // Haz3
		1.2,  // Haz4
		1.2   // Haz5
	};
	
	// On the other hand, large and extra-large enemies have their health scale by both player count and Hazard Level for all 20 combinations.
	// Currently, it looks like the only extra-large enemy is a Dreadnought which I've chosen not to model for now.
	private static double[][] largeEnemyResistances = {
		{0.45, 0.55, 0.70, 0.85},  // Haz1
		{0.65, 0.75, 0.90, 1.00},  // Haz2
		{0.80, 0.90, 1.00, 1.10},  // Haz3
		{1.00, 1.00, 1.20, 1.30},  // Haz4
		{1.20, 1.20, 1.40, 1.50}   // Haz5
	};
	
	// Resistance/weakness values taken from Elythnwaen's Spreadsheet
	// Positive number means that the creature resists that element; negative means it's weak to that element.
	// None of the enemies I'm modeling resist Poison or Radiation damage
	
	// Weighted Q'Ronar Shellback rolling state at 2/3 and non-rolling state at 1/3
	private static double qronarShellbackRolling = 0.66;
	private static double qronarShellbackUnolled = 0.34;
	private static double[][] enemyResistances = {
		// Explosive, Fire, Frost, Electric
		{0, 0, 0, 0},  				// Glyphid Swarmer
		{0, 0, 0, 0},  				// Glyphid Grunt
		{0.3, 0.25, 0.3, 0},  		// Glyphid Grunt Guard
		{-0.3, 0, 0, 0},  			// Glyphid Grunt Slasher
		{0, 0, 0, 0},  				// Glyphid Praetorian
		{0, 0, 0, 0},  				// Glyphid Exploder
		{0.5, 0, 0, 0},  			// Glyphid Bulk Detonator
		{0.5, 0, 0, 0},  			// Glyphid Crassus Detonator
		{0, 0, 0, 0},  				// Glyphid Webspitter
		{0, 0, 0, -0.1},  			// Glyphid Acidspitter
		{0, 0, 0, 0},  				// Glyphid Menace
		{0, 0, 0, 0},  				// Glyphid Warden
		{0.66, 0.66, 0.5, 0.25},  	// Glyphid Oppressor
		{qronarShellbackRolling*0.8, qronarShellbackRolling*0.3 + qronarShellbackUnolled*-0.5, qronarShellbackRolling*0.3 + qronarShellbackUnolled*-0.7, qronarShellbackRolling*1.0},  // Q'ronar Shellback
		{-1, -1, 0, -0.5},  		// Mactera Spawn
		{0, 0, 0, 0},  				// Mactera Grabber
		{0, -0.2, 0, 0},  			// Mactera Bomber
		{0, 0, 0, 0},  				// Naedocyte Breeder
		{0, 0, 0, 0},  				// Glyphid Brood Nexus
		{0, -1, 0, 0},  			// Spitball Infector
		{0, 0, 0, 0},   			// Cave Leech
		{-1, -1, 0, -0.5},  		// Mactera Tri-Jaw
		{-1, -1, 0, -0.5}   		// Mactera Brundle
	};
	
	// This info comes from Elythnwaen's Temperatures spreadsheet, and many of those values were seeded from MikeGSG giving us the values for the 5 "base" creature types.
	private static double[][] enemyTemperatures = {
		// Ignite Temp, Douse Temp, Heat Loss Rate, Freeze Temp, Thaw Temp, Heat Gain Rate
		{5, 0, 1, -20, 0, 2},			// Glyphid Swarmer
		{30, 10, 6, -30, 0, 6},			// Glyphid Grunt
		{60, 40, 6, -80, -40, 6},		// Glyphid Grunt Guard
		{30, 10, 6, -30, 0, 6},			// Glyphid Grunt Slasher
		{100, 40, 10, -150, -100, 10},	// Glyphid Praetorian
		{10, 0, 6, -10, 0, 12},			// Glyphid Exploder
		{60, 30, 10, -490, -200, 300},	// Glyphid Bulk Detonator
		{60, 30, 10, -490, -200, 300},	// Glyphid Crassus Detonator
		{30, 0, 6, -75, 0, 10},			// Glyphid Webspitter
		{35, 5, 6, -50, 0, 6},			// Glyphid Acidspitter
		{30, 0, 6, -50, 0, 6},			// Glyphid Menace
		{50, 25, 6, -70, -30, 6},		// Glyphid Warden
		{100, 40, 20, -300, -200, 100},	// Glyphid Oppressor
		{100, 70, 10, -120, 0, 10},		// Q'ronar Shellback
		{35, 5, 10, -100, 0, 40},		// Mactera Spawn
		{30, 0, 10, -180, 0, 40},		// Mactera Grabber
		{35, 5, 10, -320, 0, 50},		// Mactera Bomber
		{60, 30, 10, -150, 0, 40},		// Naedocyte Breeder
		{30/4.0, 0, 4, -50/4.0, 0, 4},	// Glyphid Brood Nexus
		{30, 0, 10, -50, 0, 10},		// Spitball Infector
		{30, 0, 10, -50, 0, 10},		// Cave Leech
		{35, 5, 10, -100, 0, 40}, 		// Mactera Tri-Jaw
		{35, 5, 10, -200, 0, 40}  		// Mactera Brundle
	};
	
	// This information comes straight from MikeGSG -- Thanks, Mike!
	private static double[] enemyLightArmorStrengthValues = {
		15,  // Glyphid Grunt
		15,  // Glyphid Grunt Guard
		15,  // Glyphid Grunt Slasher
		10,  // Glyphid Webspitter
		10,  // Glyphid Acidspitter
	};
	
	// This information extracted via UUU
	private static double[] enemyCourageValues = {
		0.0,  // Glyphid Swarmer
		0.5,  // Glyphid Grunt
		0.5,  // Glyphid Grunt Guard
		0.5,  // Glyphid Grunt Slasher
		0.5,  // Glyphid Praetorian
		0.0,  // Glyphid Exploder
		1.0,  // Glyphid Bulk Detonator
		1.0,  // Glyphid Crassus Detonator
		0.3,  // Glyphid Webspitter
		0.3,  // Glyphid Acidspitter
		0.7,  // Glyphid Menace
		0.5,  // Glyphid Warden
		1.0,  // Glyphid Oppressor (technically 100.0 in-game, but I think that's an erroneous value.)
		0.0,  // Q'ronar Shellback
		0.0,  // Mactera Spawn
		0.0,  // Mactera Grabber
		0.0,  // Mactera Bomber
		0.0,  // Naedocyte Breeder
		0.0,  // Glyphid Brood Nexus
		0.0,  // Spitball Infector
		0.0,  // Cave Leech
		0.0,  // Mactera Tri-Jaw
		0.0   // Mactera Brundle
	};
	
	// Used to determine average regular Fear duration. Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
	// Additionally, all creatures that get Feared have a x1.5 speedboost, except for Oppressor (x2) and Bulk/Crassus/Dread (x1) which can only be feared by Field Medic/SYiH/Bosco Revive
	// Values listed as m/sec groundspeed
	private static double[] enemyFearMovespeed = {
		3.5,  // Glyphid Swarmer
		2.9,  // Glyphid Grunt
		2.7,  // Glyphid Grunt Guard
		3.1,  // Glyphid Grunt Slasher
		2.0,  // Glyphid Praetorian
		4.0,  // Glyphid Exploder
		0.0,  // Glyphid Bulk Detonator
		0.0,  // Glyphid Crassus Detonator
		2.5,  // Glyphid Webspitter
		2.5,  // Glyphid Acidspitter
		2.5,  // Glyphid Menace
		2.9,  // Glyphid Warden
		0.0,  // Glyphid Oppressor
		0.0,  // Q'ronar Shellback
		0.0,  // Mactera Spawn
		0.0,  // Mactera Grabber
		0.0,  // Mactera Bomber
		0.0,  // Naedocyte Breeder
		0.0,  // Glyphid Brood Nexus
		0.0,  // Spitball Infector
		0.0,  // Cave Leech
		0.0,  // Mactera Tri-Jaw
		0.0   // Mactera Brundle
	};
	
	private static double[] movespeedDifficultyScaling = {
		0.8,  // Haz1
		0.9,  // Haz2
		1.0,  // Haz3
		1.0,  // Haz4
		1.1   // Haz5
	};
	
	private static boolean verifySpawnRatesTotalIsOne() {
		double sum = 0.0;
		for (int i = 0; i < exactSpawnRates.length; i++) {
			sum += exactSpawnRates[i];
		}
		
		// Double addition is wonky; round it.
		sum = MathUtils.round(sum, 4);
		return sum == 1.0;
	}
	
	public static double probabilityBulletWillHitWeakpoint() {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		double toReturn = MathUtils.vectorDotProduct(exactSpawnRates, probabilityBulletHitsWeakpointPerEnemyType);
		// System.out.println("Estimated percentage of bullets fired that will hit a weakpoint: " + toReturn);
		return toReturn;
	}
	
	public static double averageWeakpointDamageIncrease() {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		double toReturn = MathUtils.vectorDotProduct(exactSpawnRates, defaultWeakpointDamageBonusPerEnemyType);
		// System.out.println("Average damage multiplier from hitting a weakpoint: " + toReturn);
		return toReturn;
	}
	
	public static double averageHealthPool() {
		return averageHealthPool(true);
	}
	public static double averageHealthPool(boolean exact) {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		double[] spawnRates;
		if (exact) {
			spawnRates = exactSpawnRates;
		}
		else {
			spawnRates = guessedSpawnRates;
		}
		
		int i, enemyIndex;

		double normalResistance = normalEnemyResistances[hazardLevel - 1];
		int[] normalEnemyIndexes = {0, 1, 2, 3, 5, 8, 9, 14, 20, 21, 22};
		double normalEnemyHealth = 0;
		for (i = 0; i < normalEnemyIndexes.length; i++) {
			enemyIndex = normalEnemyIndexes[i];
			normalEnemyHealth += spawnRates[enemyIndex] * enemyHealthPools[enemyIndex];
		}
		normalEnemyHealth *= normalResistance;
		
		double largeResistance = largeEnemyResistances[hazardLevel - 1][playerCount - 1];
		int[] largeEnemyIndexes = {4, 6, 7, 10, 11, 12, 13, 15, 16, 17, 18, 19};
		double largeEnemyHealth = 0;
		for (i = 0; i < largeEnemyIndexes.length; i++) {
			enemyIndex = largeEnemyIndexes[i];
			largeEnemyHealth += spawnRates[enemyIndex] * enemyHealthPools[enemyIndex];
		}
		largeEnemyHealth *= largeResistance;
		
		// System.out.println("Average health of an enemy: " + (normalEnemyHealth + largeEnemyHealth));
		return normalEnemyHealth + largeEnemyHealth;
	}
	
	public static double averageTimeToIgnite(double burstOfHeat, double heatPerShot, double RoF, double heatPerSec) {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		int numEnemyTypes = exactSpawnRates.length;
		double[] ignitionTimes = new double[numEnemyTypes];
		double igniteTemp, coolingRate;
		
		for (int i = 0; i < numEnemyTypes; i++) {
			igniteTemp = enemyTemperatures[i][0];
			
			// Early exit: if Heat/Shot >= 100, then all enemies get ignited instantly since the largest Ignite Temp modeled in this program is 100.
			if (burstOfHeat >= igniteTemp || heatPerShot >= igniteTemp || burstOfHeat + heatPerShot >= igniteTemp) {
				ignitionTimes[i] = 0.0;
				continue;
			}
			
			coolingRate = enemyTemperatures[i][2];
			
			ignitionTimes[i] = (igniteTemp - burstOfHeat) / (heatPerShot * RoF + heatPerSec - coolingRate);
		}
		
		return MathUtils.vectorDotProduct(exactSpawnRates, ignitionTimes);
	}
	public static double averageBurnDuration() {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		int numEnemyTypes = exactSpawnRates.length;
		double burnDurations[] = new double[numEnemyTypes];
		double igniteTemp, douseTemp, coolingRate;
		
		for (int i = 0; i < numEnemyTypes; i++) {
			igniteTemp = enemyTemperatures[i][0];
			douseTemp = enemyTemperatures[i][1];
			coolingRate = enemyTemperatures[i][2];
			burnDurations[i] = (igniteTemp - douseTemp) / coolingRate;
		}
		
		return MathUtils.vectorDotProduct(exactSpawnRates, burnDurations);
	}
	// This method is currently only used by Gunner/Minigun/Mod/5/Aggressive Venting in maxDamage() and Engineer/GrenadeLauncher/Mod/3/Incendiary Compound single-target DPS
	public static double percentageEnemiesIgnitedBySingleBurstOfHeat(double heatPerBurst) {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		double sum = 0;
		for (int i = 0; i < exactSpawnRates.length; i++) {
			if (enemyTemperatures[i][0] <= heatPerBurst) {
				sum += exactSpawnRates[i];
			}
		}
		
		return MathUtils.round(sum, 4);
	}
	
	/*
		From what Elythnwaen and I have been able to figure out, creatures with positive temperatures lose Heat constantly. 
		However, when creatures have negative temperatures, they all have 1-2 second "WarmingCooldown" windows before they 
		start gaining Heat. Most of these Freeze temperatures are achieved in less than 2 seconds, so I'm choosing to model 
		this as if the warming rate has no effect on the average Freeze time.
		
		Cold per shot should be a negative number to indicate that the enemy's temperature is being decreased
	*/
	public static double averageTimeToFreeze(double burstOfCold, double coldPerShot, double RoF, double coldPerSec) {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		int numEnemyTypes = exactSpawnRates.length;
		double[] freezeTimes = new double[numEnemyTypes];
		double freezeTemp;
		
		for (int i = 0; i < numEnemyTypes; i++) {
			freezeTemp = enemyTemperatures[i][3];
			
			// Early exit: if Cold/Shot >= 300, then all enemies get ignited instantly since the largest Freeze Temp modeled in this program is 300.
			if (burstOfCold <= freezeTemp || coldPerShot <= freezeTemp || burstOfCold + coldPerShot <= freezeTemp) {
				freezeTimes[i] = 0.0;
				continue;
			}
			
			freezeTimes[i] = (freezeTemp - burstOfCold) / (coldPerShot * RoF + coldPerSec);
		}
		
		return MathUtils.vectorDotProduct(exactSpawnRates, freezeTimes);
	}
	// Because the creatures have had a negative temperature for longer than 2 seconds (due to being Frozen already) I'm keeping warming rate in the refreeze method
	public static double averageTimeToRefreeze(double coldPerSecond) {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		int numEnemyTypes = exactSpawnRates.length;
		double[] refreezeTimes = new double[numEnemyTypes];
		double freezeTemp, thawTemp, warmingRate;
		
		for (int i = 0; i < numEnemyTypes; i++) {
			freezeTemp = enemyTemperatures[i][3];
			thawTemp = enemyTemperatures[i][4];
			warmingRate = enemyTemperatures[i][5];
			
			// Negative Freeze temps divided by negative cold per seconds results in a positive number of seconds
			refreezeTimes[i] = (freezeTemp - thawTemp) / (coldPerSecond + warmingRate);
		}
		
		return MathUtils.vectorDotProduct(exactSpawnRates, refreezeTimes);
	}
	public static double averageFreezeDuration() {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		int numEnemyTypes = exactSpawnRates.length;
		double freezeDurations[] = new double[numEnemyTypes];
		double freezeTemp, thawTemp, warmingRate;
		
		for (int i = 0; i < numEnemyTypes; i++) {
			freezeTemp = enemyTemperatures[i][3];
			thawTemp = enemyTemperatures[i][4];
			warmingRate = enemyTemperatures[i][5];
			
			// Because every Freeze temp is negative and is strictly less than the corresponding Thaw temp, subtracting Freeze from Thaw guarantees a positive number.
			freezeDurations[i] = (thawTemp - freezeTemp) / warmingRate;
		}
		
		return MathUtils.vectorDotProduct(exactSpawnRates, freezeDurations);
	}
	// This method is currently only used by Driller/CryoCannon/OC/Snowball in Utility
	public static double percentageEnemiesFrozenBySingleBurstOfCold(double coldPerBurst) {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		double sum = 0;
		for (int i = 0; i < exactSpawnRates.length; i++) {
			if (enemyTemperatures[i][3] >= coldPerBurst) {
				sum += exactSpawnRates[i];
			}
		}
		
		return MathUtils.round(sum, 4);
	}
	
	public static double averageLightArmorStrength() {
		int[] indexesOfEnemiesWithLightArmor = new int[] {1, 2, 3, 8, 9};
		double[] subsetSpawnRates = new double[indexesOfEnemiesWithLightArmor.length];
		for (int i = 0; i < indexesOfEnemiesWithLightArmor.length; i++) {
			subsetSpawnRates[i] = exactSpawnRates[indexesOfEnemiesWithLightArmor[i]];
		}
		
		return MathUtils.vectorDotProduct(enemyLightArmorStrengthValues, subsetSpawnRates) / MathUtils.sum(subsetSpawnRates);
	}
	public static double lightArmorBreakProbabilityLookup(double damage, double armorBreakingModifier, double armorStrength) {
		// Input sanitization
		if (damage <= 0.0 || armorBreakingModifier <= 0.0 || armorStrength <= 0.0) {
			return 0.0;
		}
		
		// This information comes straight from MikeGSG -- Thanks, Mike!
		double lookupValue = damage * armorBreakingModifier / armorStrength;
		
		if (lookupValue < 1.0) {
			return lookupValue / 2.0;
		}
		else if (lookupValue < 2.0) {
			return 0.5 + (lookupValue - 1.0) / 4.0;
		}
		else if (lookupValue < 4.0) {
			return 0.75 + (lookupValue - 2.0) / 8.0;
		}
		else {
			return 1.0;
		}
	}
	
	public static double averageCourage() {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		return MathUtils.vectorDotProduct(exactSpawnRates, enemyCourageValues);
	}
	
	/*
		Although at this time this model is unconfirmed, I have some evidence to support this theory.
		
		The regular Fear status effect inflicted by weapons and grenades works like this: for every creature that has the Fear Factor attack applied to them,
		the probability that they will have the Fear status effect inflicted is equal to Fear Factor * (1.0 - Courage). If it is inflicted, then ground-based 
		enemies will move 8m away from the point of Fear at a rate of 1.5 * Max Movespeed * Difficulty Scaling * (1.0 - Movespeed Slow). As a result of this formula,
		Slowing an enemy that is being Feared will increase the duration of the Fear status effect, and it will naturally be shorter at higher hazard levels.
	*/
	public static double averageFearDuration() {
		return averageFearDuration(0.0, 0.0);
	}
	public static double averageFearDuration(double enemySlowMultiplier, double slowDuration) {
		double averageFearMovespeed = MathUtils.vectorDotProduct(exactSpawnRates, enemyFearMovespeed);
		double difficultyScalingMovespeedModifier = movespeedDifficultyScaling[hazardLevel - 1];
		
		// This value gathered from internal property TSK_FleeFrom_C.distance
		double fearDistanceGoal = 10.0;
		// 1.5 multiplier comes from DeepPathfinderMovement.FleeSpeedBoostMultiplier
		double compositeAverageEnemyMovespeed = 1.5 * averageFearMovespeed * difficultyScalingMovespeedModifier * (1.0 - enemySlowMultiplier);
		
		double rawDuration = fearDistanceGoal / compositeAverageEnemyMovespeed;
		if (enemySlowMultiplier > 0 && rawDuration > slowDuration) {
			// If the slow runs out before the average enemy has finished moving the distance goal, then the rest of the distance will be at normal speed.
			double remainingDistance = fearDistanceGoal - slowDuration * compositeAverageEnemyMovespeed;
			return slowDuration + remainingDistance / (averageFearMovespeed * difficultyScalingMovespeedModifier);
		}
		else {
			return rawDuration;
		}
	}
	
	public static double averageDifficultyScalingResistance() {
		int[] normalEnemyIndexes = {0, 1, 2, 3, 5, 8, 9, 14, 20, 21, 22};
		double normalResistance = normalEnemyResistances[hazardLevel - 1];
		int[] largeEnemyIndexes = {4, 6, 7, 10, 11, 12, 13, 15, 16, 17, 18, 19};
		double largeResistance = largeEnemyResistances[hazardLevel - 1][playerCount - 1];
		
		double sum = 0;
		for (int i: normalEnemyIndexes) {
			sum += exactSpawnRates[i] * normalResistance;
		}
		for (int i: largeEnemyIndexes) {
			sum += exactSpawnRates[i] * largeResistance;
		}
		
		return sum;
	}
	
	/*
		This method is used to quickly show how many shots it would take for projectile-based weapons to kill the 23 modeled creatures under various conditions. It models 
		Elemental resistances, DoTs, Light Armor resistance, Weakpoint bonus damage, Subata's T5.B +20% vs Mactera, IFGs, and Frozen.
		
		The first two arguments are arrays of how much damage is being done of the three types (direct, area, and DoT) split between the elements in this order:
			1. Kinetic
			2. Explosive
			3. Fire
			4. Frost
			5. Electric
			6. Poison
			7. Radiation
			
		Next are 3 arrays that denote the DPS, average duration, and probability to proc of each of these DoTs:
			1. Electrocute
			2. Neurotoxin
			3. Persistent Plasma
			4. Radiation
			
		It should be noted that Direct Damage is never Poison or Radiation and DoTs are never Kinetic, Explosive, or Frost.
		
		This method does NOT model Heavy Armor plates except for Mactera Brundle because those Heavy Armor plates cover its weakpoint.
		
		If the weapon can do at least one DoT, this will look ahead to see if up to 4 seconds of DoT damage can kill a creature. If it can, then it will finish on that Breakpoint early instead of wasting superfluous ammo.
	*/
	public static int[] calculateBreakpoints(double[] directDamageByType, double[] areaDamageByType, double[] DoT_DPS, double[] DoT_durations, double[] DoT_probabilities, 
											 double weakpointModifier, double armorBreaking, double RoF, double heatPerShot, double macteraModifier, 
											 boolean frozen, boolean IFG, boolean flyingNightmare, boolean embeddedDetonators) {
		int[] creaturesToModel = {0, 1, 2, 3, 4, 5, 8, 9, 10, 11, 12, 14, 15, 16, 20, 21, 22};
		
		double normalResistance = normalEnemyResistances[hazardLevel - 1];
		double largeResistance = largeEnemyResistances[hazardLevel - 1][playerCount - 1];
		
		ArrayList<Integer> toReturn = new ArrayList<Integer>();
		
		HashSet<Integer> normalEnemyScalingIndexes = new HashSet<Integer>(Arrays.asList(new Integer[] {0, 1, 2, 3, 5, 8, 9, 14, 20, 21, 22}));
		HashSet<Integer> largeEnemyScalingIndexes = new HashSet<Integer>(Arrays.asList(new Integer[] {4, 6, 7, 10, 11, 12, 13, 15, 16, 17, 18, 19}));
		// Grunts, Guards, Slashers, Web Spitters, and Acid Spitters intentionally neglected from this list since they are entirely covered by Light Armor except for their Weakpoints
		HashSet<Integer> indexesWithNormalHealth = new HashSet<Integer>(Arrays.asList(new Integer[] {0, 4, 5, 6, 7, 10, 11, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22}));
		HashSet<Integer> indexesWithLightArmor = new HashSet<Integer>(Arrays.asList(new Integer[] {1, 2, 3, 8, 9}));
		HashSet<Integer> indexesWithHeavyArmorCoveringWeakpoint = new HashSet<Integer>(Arrays.asList(new Integer[] {22}));
		HashSet<Integer> indexesWithoutWeakpoints = new HashSet<Integer>(Arrays.asList(new Integer[] {0, 20}));
		HashSet<Integer> indexesOfMacteras = new HashSet<Integer>(Arrays.asList(new Integer[] {14, 15, 16, 21, 22}));
		
		// Frozen
		double lightArmorReduction = UtilityInformation.LightArmor_DamageReduction;
		if (frozen) {
			// Removes Weakpoint Bonuses
			weakpointModifier = -1.0;
			
			// Bypasses all Armor types
			lightArmorReduction = 1.0;
			
			// Multiplies Direct Damage by x3 (except for Flying Nightmare)
			if (!flyingNightmare) {
				directDamageByType = MathUtils.vectorScalarMultiply(UtilityInformation.Frozen_Damage_Multiplier, directDamageByType);
			}
		}
		
		// Flying Nightmare is weird... it does the Direct Damage listed but it passes through enemies, ignores armor, and doesn't benefit from Weakpoints like the Breach Cutter.
		if (flyingNightmare) {
			weakpointModifier = -1.0;
			lightArmorReduction = 1.0;
		}
		
		// IFG
		if (IFG) {
			// Increases Direct and Area Damage taken by x1.3
			directDamageByType = MathUtils.vectorScalarMultiply(UtilityInformation.IFG_Damage_Multiplier, directDamageByType);
			areaDamageByType = MathUtils.vectorScalarMultiply(UtilityInformation.IFG_Damage_Multiplier, areaDamageByType);
		}
		
		double creatureHP, creatureWeakpointModifier, aliasHP;
		double rawDirectDamage, modifiedDirectDamage, rawAreaDamage, modifiedAreaDamage;
		double numShotsToProcBurn, numShotsToProcElectrocute, numShotsToProcNeurotoxin, numShotsToProcPersistentPlasma, numShotsToProcRadiation;
		double burnDPS, burnDuration, electrocuteDPS, plasmaDPS;
		double[] creatureResistances;
		int breakpointCounter;
		double fourSecondsDoTDamage;
		double lightArmorStrength, heavyArmorHP, numShotsToBreakArmor;
		for (int creatureIndex: creaturesToModel) {
			if (normalEnemyScalingIndexes.contains(creatureIndex)) {
				creatureHP = enemyHealthPools[creatureIndex] * normalResistance;
			}
			else if (largeEnemyScalingIndexes.contains(creatureIndex)) {
				creatureHP = enemyHealthPools[creatureIndex] * largeResistance;
			}
			else {
				creatureHP = enemyHealthPools[creatureIndex];
			}
			
			creatureResistances = new double[] {
				1.0 - enemyResistances[creatureIndex][0],	// Explosive
				1.0 - enemyResistances[creatureIndex][1],	// Fire
				1.0 - enemyResistances[creatureIndex][2],	// Frost
				1.0 - enemyResistances[creatureIndex][3],	// Electric
			};
			
			creatureWeakpointModifier = defaultWeakpointDamageBonusPerEnemyType[creatureIndex];
			if (weakpointModifier < 0) {
				creatureWeakpointModifier = 1.0;
			}
			else {
				creatureWeakpointModifier *= (1.0 + weakpointModifier);
			}
			
			rawDirectDamage = MathUtils.sum(directDamageByType);
			modifiedDirectDamage = directDamageByType[0] + directDamageByType[1] * creatureResistances[0] + directDamageByType[2] * creatureResistances[1] + directDamageByType[3] * creatureResistances[2] + directDamageByType[4] * creatureResistances[3];
			
			rawAreaDamage = MathUtils.sum(areaDamageByType);
			modifiedAreaDamage = areaDamageByType[0] + areaDamageByType[1] * creatureResistances[0] + areaDamageByType[2] * creatureResistances[1] + areaDamageByType[3] * creatureResistances[2] + areaDamageByType[4] * creatureResistances[3];
			
			// Driller/Subata/Mod/5/B "Mactera Neurotoxin Coating" makes the Subata's damage do x1.2 more to Mactera-type enemies
			if (indexesOfMacteras.contains(creatureIndex)) {
				modifiedDirectDamage *= (1.0 + macteraModifier);
				modifiedAreaDamage *= (1.0 + macteraModifier);
			}
			
			// Neurotoxin does Poison damage -- which no enemy resists -- and Radiation is not resisted by any creatures modeled by the program (but it is technically resisted by enemies in REZ biome)
			burnDPS = DoTInformation.Burn_DPS * creatureResistances[1];
			electrocuteDPS = DoT_DPS[0] * creatureResistances[3];
			plasmaDPS = DoT_DPS[2] * creatureResistances[1];
			
			numShotsToProcBurn = 0;
			burnDuration = 0;
			numShotsToProcElectrocute = 0;
			numShotsToProcNeurotoxin = 0;
			numShotsToProcPersistentPlasma = 0;
			numShotsToProcRadiation = 0;
			if (!frozen && heatPerShot > 0.0) {
				if (heatPerShot > enemyTemperatures[creatureIndex][0]) {
					numShotsToProcBurn = 1;
					burnDuration = (heatPerShot - enemyTemperatures[creatureIndex][1]) / enemyTemperatures[creatureIndex][2];
				}
				else {
					// This is technically an approximation and not precisely how it works in-game, but it's close enough for what I need.
					numShotsToProcBurn = Math.floor((enemyTemperatures[creatureIndex][0] * RoF) / (heatPerShot * RoF - enemyTemperatures[creatureIndex][2]));
					burnDuration = (enemyTemperatures[creatureIndex][0] - enemyTemperatures[creatureIndex][1]) / enemyTemperatures[creatureIndex][2];
				}
			}
			if (DoT_probabilities[0] > 0.0) {
				numShotsToProcElectrocute = Math.round(MathUtils.meanRolls(DoT_probabilities[0]));
			}
			if (DoT_probabilities[1] > 0.0) {
				numShotsToProcNeurotoxin = Math.round(MathUtils.meanRolls(DoT_probabilities[1]));
			}
			if (DoT_probabilities[2] > 0.0) {
				numShotsToProcPersistentPlasma = Math.round(MathUtils.meanRolls(DoT_probabilities[2]));
			}
			if (DoT_probabilities[3] > 0.0) {
				numShotsToProcRadiation = Math.round(MathUtils.meanRolls(DoT_probabilities[3]));
			}
			
			// Normal Damage
			if (indexesWithNormalHealth.contains(creatureIndex)) {
				breakpointCounter = 0;
				aliasHP = creatureHP;
				
				while (aliasHP > 0) {
					breakpointCounter++;
					
					// First, subtract Direct Damage
					aliasHP -= modifiedDirectDamage;
					
					// Second, subtract Area Damage
					aliasHP -= modifiedAreaDamage;
					
					// Third, determine if 4 seconds of DoTs can do enough damage to kill the creature
					fourSecondsDoTDamage = 0;
					if (numShotsToProcBurn > 0 && breakpointCounter >= numShotsToProcBurn) {
						fourSecondsDoTDamage += Math.min(burnDuration, 4.0) * burnDPS;
					}
					if (numShotsToProcElectrocute > 0 && breakpointCounter >= numShotsToProcElectrocute) {
						fourSecondsDoTDamage += Math.min(DoT_durations[0], 4.0) * electrocuteDPS;
					}
					if (numShotsToProcNeurotoxin > 0 && breakpointCounter >= numShotsToProcNeurotoxin) {
						fourSecondsDoTDamage += Math.min(DoT_durations[1], 4.0) * DoT_DPS[1];
					}
					if (numShotsToProcPersistentPlasma > 0 && breakpointCounter >= numShotsToProcPersistentPlasma) {
						fourSecondsDoTDamage += Math.min(DoT_durations[2], 4.0) * plasmaDPS;
					}
					if (numShotsToProcRadiation > 0 && breakpointCounter >= numShotsToProcRadiation) {
						fourSecondsDoTDamage += Math.min(DoT_durations[3], 4.0) * DoT_DPS[3];
					}
					
					if (fourSecondsDoTDamage >= aliasHP) {
						break;
					}
					
					// If not, subtract the damage dealt by DoTs until the next shot at max RoF
					if (numShotsToProcBurn > 0 && breakpointCounter >= numShotsToProcBurn) {
						aliasHP -= burnDPS / RoF;
					}
					if (numShotsToProcElectrocute > 0 && breakpointCounter >= numShotsToProcElectrocute) {
						aliasHP -= electrocuteDPS / RoF;
					}
					if (numShotsToProcNeurotoxin > 0 && breakpointCounter >= numShotsToProcNeurotoxin) {
						aliasHP -=  DoT_DPS[1] / RoF;
					}
					if (numShotsToProcPersistentPlasma > 0 && breakpointCounter >= numShotsToProcPersistentPlasma) {
						aliasHP -= plasmaDPS / RoF;
					}
					if (numShotsToProcRadiation > 0 && breakpointCounter >= numShotsToProcRadiation) {
						aliasHP -= DoT_DPS[3] / RoF;
					}
					
					// This is just a catch-all statement for the rounding errors inherent to double division.
					aliasHP = MathUtils.round(aliasHP, 4);
				}
				
				toReturn.add(breakpointCounter);
			}
			
			// Light Armor
			if (indexesWithLightArmor.contains(creatureIndex)) {
				breakpointCounter = 0;
				aliasHP = creatureHP;
				
				if (creatureIndex == 1 || creatureIndex == 2 || creatureIndex == 3) {
					lightArmorStrength = 15;
				}
				else if (creatureIndex == 8 || creatureIndex == 9) {
					lightArmorStrength = 10;
				}
				else {
					// This is an error case.
					lightArmorStrength = 1;
				}
				
				if (embeddedDetonators) {
					numShotsToBreakArmor = Math.ceil(MathUtils.meanRolls(lightArmorBreakProbabilityLookup(rawDirectDamage, armorBreaking, lightArmorStrength)));
				}
				else {
					numShotsToBreakArmor = Math.ceil(MathUtils.meanRolls(lightArmorBreakProbabilityLookup(rawDirectDamage + rawAreaDamage, armorBreaking, lightArmorStrength)));
				}
				
				while (aliasHP > 0) {
					breakpointCounter++;
					
					// First, subtract Direct Damage
					if (armorBreaking > 1.0 && breakpointCounter >= numShotsToBreakArmor) {
						aliasHP -= modifiedDirectDamage;
					}
					else if (armorBreaking <= 1.0 && breakpointCounter > numShotsToBreakArmor) {
						aliasHP -= modifiedDirectDamage;
					}
					else {
						aliasHP -= modifiedDirectDamage * lightArmorReduction;
					}
					
					// Second, subtract Area Damage
					aliasHP -= modifiedAreaDamage;
					
					// Third, determine if 4 seconds of DoTs can do enough damage to kill the creature
					fourSecondsDoTDamage = 0;
					if (numShotsToProcBurn > 0 && breakpointCounter >= numShotsToProcBurn) {
						fourSecondsDoTDamage += Math.min(burnDuration, 4.0) * burnDPS;
					}
					if (numShotsToProcElectrocute > 0 && breakpointCounter >= numShotsToProcElectrocute) {
						fourSecondsDoTDamage += Math.min(DoT_durations[0], 4.0) * electrocuteDPS;
					}
					if (numShotsToProcNeurotoxin > 0 && breakpointCounter >= numShotsToProcNeurotoxin) {
						fourSecondsDoTDamage += Math.min(DoT_durations[1], 4.0) * DoT_DPS[1];
					}
					if (numShotsToProcPersistentPlasma > 0 && breakpointCounter >= numShotsToProcPersistentPlasma) {
						fourSecondsDoTDamage += Math.min(DoT_durations[2], 4.0) * plasmaDPS;
					}
					if (numShotsToProcRadiation > 0 && breakpointCounter >= numShotsToProcRadiation) {
						fourSecondsDoTDamage += Math.min(DoT_durations[3], 4.0) * DoT_DPS[3];
					}
					
					if (fourSecondsDoTDamage >= aliasHP) {
						break;
					}
					
					// If not, subtract the damage dealt by DoTs until the next shot at max RoF
					if (numShotsToProcBurn > 0 && breakpointCounter >= numShotsToProcBurn) {
						aliasHP -= burnDPS / RoF;
					}
					if (numShotsToProcElectrocute > 0 && breakpointCounter >= numShotsToProcElectrocute) {
						aliasHP -= electrocuteDPS / RoF;
					}
					if (numShotsToProcNeurotoxin > 0 && breakpointCounter >= numShotsToProcNeurotoxin) {
						aliasHP -=  DoT_DPS[1] / RoF;
					}
					if (numShotsToProcPersistentPlasma > 0 && breakpointCounter >= numShotsToProcPersistentPlasma) {
						aliasHP -= plasmaDPS / RoF;
					}
					if (numShotsToProcRadiation > 0 && breakpointCounter >= numShotsToProcRadiation) {
						aliasHP -= DoT_DPS[3] / RoF;
					}
					
					// This is just a catch-all statement for the rounding errors inherent to double division.
					aliasHP = MathUtils.round(aliasHP, 4);
				}
				
				toReturn.add(breakpointCounter);
			}
			
			// Weakpoint
			if (!indexesWithoutWeakpoints.contains(creatureIndex)) {
				breakpointCounter = 0;
				aliasHP = creatureHP;
				
				if (indexesWithHeavyArmorCoveringWeakpoint.contains(creatureIndex)) {
					if (creatureIndex == 22) {
						heavyArmorHP = 80;
					}
					else {
						// Error case
						heavyArmorHP = 1000;
					}
					
					heavyArmorHP *= normalResistance;
					
					if (embeddedDetonators) {
						numShotsToBreakArmor = heavyArmorHP / (rawDirectDamage * armorBreaking);
					}
					else {
						numShotsToBreakArmor = heavyArmorHP / ((rawDirectDamage + rawAreaDamage) * armorBreaking);
					}
				}
				else {
					heavyArmorHP = 0;
					numShotsToBreakArmor = 0;
				}
				
				while (aliasHP > 0) {
					breakpointCounter++;
					
					if (!frozen && heavyArmorHP > 0) {
						// First, subtract Direct Damage (and Explosive Reload/Embedded Detonators)
						if ((armorBreaking > 1.0 && breakpointCounter >= numShotsToBreakArmor) || (armorBreaking <= 1.0 && breakpointCounter > numShotsToBreakArmor)) {
							aliasHP -= modifiedDirectDamage * creatureWeakpointModifier;
							if (embeddedDetonators) {
								aliasHP -= modifiedAreaDamage;
							}
						}
						else {
							continue;
						}
						
						// Second, subtract Area Damage
						if (!embeddedDetonators) {
							aliasHP -= modifiedAreaDamage;
						}
					}
					else {
						aliasHP -= modifiedDirectDamage * creatureWeakpointModifier;
						aliasHP -= modifiedAreaDamage;
					}
					
					// Third, determine if 4 seconds of DoTs can do enough damage to kill the creature
					fourSecondsDoTDamage = 0;
					if (numShotsToProcBurn > 0 && breakpointCounter >= numShotsToProcBurn) {
						fourSecondsDoTDamage += Math.min(burnDuration, 4.0) * burnDPS;
					}
					if (numShotsToProcElectrocute > 0 && breakpointCounter >= numShotsToProcElectrocute) {
						fourSecondsDoTDamage += Math.min(DoT_durations[0], 4.0) * electrocuteDPS;
					}
					if (numShotsToProcNeurotoxin > 0 && breakpointCounter >= numShotsToProcNeurotoxin) {
						fourSecondsDoTDamage += Math.min(DoT_durations[1], 4.0) * DoT_DPS[1];
					}
					if (numShotsToProcPersistentPlasma > 0 && breakpointCounter >= numShotsToProcPersistentPlasma) {
						fourSecondsDoTDamage += Math.min(DoT_durations[2], 4.0) * plasmaDPS;
					}
					if (numShotsToProcRadiation > 0 && breakpointCounter >= numShotsToProcRadiation) {
						fourSecondsDoTDamage += Math.min(DoT_durations[3], 4.0) * DoT_DPS[3];
					}
					
					if (fourSecondsDoTDamage >= aliasHP) {
						break;
					}
					
					// If not, subtract the damage dealt by DoTs until the next shot at max RoF
					if (numShotsToProcBurn > 0 && breakpointCounter >= numShotsToProcBurn) {
						aliasHP -= burnDPS / RoF;
					}
					if (numShotsToProcElectrocute > 0 && breakpointCounter >= numShotsToProcElectrocute) {
						aliasHP -= electrocuteDPS / RoF;
					}
					if (numShotsToProcNeurotoxin > 0 && breakpointCounter >= numShotsToProcNeurotoxin) {
						aliasHP -=  DoT_DPS[1] / RoF;
					}
					if (numShotsToProcPersistentPlasma > 0 && breakpointCounter >= numShotsToProcPersistentPlasma) {
						aliasHP -= plasmaDPS / RoF;
					}
					if (numShotsToProcRadiation > 0 && breakpointCounter >= numShotsToProcRadiation) {
						aliasHP -= DoT_DPS[3] / RoF;
					}
					
					// This is just a catch-all statement for the rounding errors inherent to double division.
					aliasHP = MathUtils.round(aliasHP, 4);
				}
				
				toReturn.add(breakpointCounter);
			}
		}
				
		return convertIntegers(toReturn);
	}
	
	// Sourced from https://stackoverflow.com/a/718558
	private static int[] convertIntegers(List<Integer> integers) {
	    int[] ret = new int[integers.size()];
	    Iterator<Integer> iterator = integers.iterator();
	    for (int i = 0; i < ret.length; i++) {
	        ret[i] = iterator.next().intValue();
	    }
	    return ret;
	}
	
	/*
		There's no succinct or clever way to write this method. It's going to be a beast, and iterate over several creatures individually. I apologize to anyone that has to
		read over this method after it's done...
		
		Creatures with either Light or Heavy Armor:
			Glyphid Grunt
			Glyphid Grunt Guard
			Glyphid Grunt Slasher
			Glyphid Praetorian
			Glyphid Webspitter
			Glyphid Acidspitter
			Glyphid Menace
			Glyphid Warden
			Q'ronar Shellback
			Mactera Brundle
			
		For most enemies in the list, I'm going to model it as if every shot fired has Weakpoint Accuracy percent of the Direct Damage hit the Weakpoint, 
		(General Accuracy - Weakpoint Accuracy) percent hit up to 6 armor plates simultaneously and have its damage reduced accordingly, and 
		remove (100% - General Accuracy) % of Direct Damage to account for missed shots. Area Damage will be applied normally, thankfully. This "superimposition" of
		Direct Damage is the only way I can think of to produce consistent, repeatable results from this type of mechanic. If I didn't use this method, it would be a 
		lot of RNG rolls to model and that would produce different results even for the same build different times.
		
		There will be a couple exceptions to this pattern: Praetorian, Shellback, and Brundle. Praetorian will have General Accuracy percent of Direct Damage hit its mouth, and 
		(100% - General Accuracy) percent of Direct Damage hit the Heavy Armor plates around the mouth. Shellbacks will have General Accuracy percentage of Direct Damage
		hit its plates until they're broken. Brundles take no damage until their Heavy Armor is broken, and then they take 3x Weakpoint damage.
		
		I'm choosing to let Overkill damage be counted as damage dealt. Too complicated to keep track of while simultaneously doing Armor stuff.
	*/
	public static double[][] percentageDamageWastedByArmor(double directDamage, int numPellets, double areaDamage, double armorBreaking, double weakpointModifier, double generalAccuracy, double weakpointAccuracy) {
		return percentageDamageWastedByArmor(directDamage, numPellets, areaDamage, armorBreaking, weakpointModifier, generalAccuracy, weakpointAccuracy, false);
	}
	public static double[][] percentageDamageWastedByArmor(double directDamage, int numPellets, double areaDamage, double armorBreaking, double weakpointModifier, double generalAccuracy, double weakpointAccuracy, boolean embeddedDetonators) {
		double[][] creaturesArmorMatrix = {
			// Creature Index, Number of Light Armor plates, Avg Armor Strength, Number of Heavy Armor plates, Avg Armor Plate HP
			{1, 6, 15, 0, 0},  					// Glyphid Grunt
			{2, 2, 15, 4, 60},  				// Glyphid Guard
			{3, 6, 15, 0, 0},  					// Glyphid Slasher
			{4, 0, 0, 6, 100},  				// Glyphid Praetorian
			{8, 3, 10, 0, 0},  					// Glyphid Web Spitter
			{9, 3, 10, 0, 0},  					// Glyphid Acid Spitter
			{10, 0, (1*1 + 2*10)/3.0, 3, 0},  	// Glyphid Menace
			{11, 0, 15, 3, 0},  				// Glyphid Warden
			{13, 0, 0, 6, (6*70 + 14*30)/20},  	// Q'ronar Shellback
			{22, 0, 0, 2, 80}					// Mactera Brundle
		};
		
		double[][] toReturn = new double[2][creaturesArmorMatrix.length];
		
		
		double normalResistance = normalEnemyResistances[hazardLevel - 1];
		double largeResistance = largeEnemyResistances[hazardLevel - 1][playerCount - 1];
		
		int creatureIndex, i, j;
		double baseHealth, heavyArmorPlateHealth;
		double damageDealtPerPellet, proportionOfDamageThatHitsArmor, proportionOfDamageThatHitsWeakpoint;
		int avgNumHitsToBreakArmorStrengthPlate, numHitsOnArmorStrengthPlate;
		double totalDamageSpent, actualDamageDealt;
		for (i = 0; i < creaturesArmorMatrix.length; i++) {
			creatureIndex = (int) creaturesArmorMatrix[i][0];
			baseHealth = enemyHealthPools[creatureIndex];
			
			if (creaturesArmorMatrix[i][4] > 0) {
				// All Heavy Armor plates with healthbars have their health scale with normal resistance.
				heavyArmorPlateHealth = creaturesArmorMatrix[i][4] * normalResistance;
			}
			else {
				heavyArmorPlateHealth = 0;
			}
			
			if (i == 3) {
				// Special case: Glyphid Praetorian
				baseHealth *= largeResistance;
				
				proportionOfDamageThatHitsArmor = (100.0 - generalAccuracy) / 100.0;
				double proportionOfDamageThatHitsMouth = generalAccuracy / 100.0;
				
				totalDamageSpent = 0;
				actualDamageDealt = 0;
				while (baseHealth > 0) {
					// First, Direct Damage
					for (j = 0; j < numPellets; j++) {
						totalDamageSpent += directDamage;
						damageDealtPerPellet = proportionOfDamageThatHitsMouth * directDamage;
						if (heavyArmorPlateHealth > 0) {
							if (armorBreaking > 1.0) {
								if (directDamage * armorBreaking > heavyArmorPlateHealth) {
									damageDealtPerPellet += proportionOfDamageThatHitsArmor * directDamage;
									heavyArmorPlateHealth = 0;
								}
								else {
									// Direct Damage insufficient to break the Heavy Armor Plate
									heavyArmorPlateHealth -= directDamage * proportionOfDamageThatHitsArmor * armorBreaking;
								}
							}
							else {
								if (directDamage * proportionOfDamageThatHitsArmor * armorBreaking > heavyArmorPlateHealth) {
									heavyArmorPlateHealth = 0;
								}
								else {
									// Direct Damage insufficient to break the Heavy Armor Plate
									heavyArmorPlateHealth -= directDamage * proportionOfDamageThatHitsArmor * armorBreaking;
								}
							}
						}
						else {
							damageDealtPerPellet += proportionOfDamageThatHitsArmor * directDamage;
						}
						
						actualDamageDealt += damageDealtPerPellet;
						baseHealth -= damageDealtPerPellet;
					}
					
					// Second, Area Damage
					totalDamageSpent += areaDamage;
					if (embeddedDetonators) {
						if (heavyArmorPlateHealth == 0) {
							actualDamageDealt += areaDamage;
							baseHealth -= areaDamage;
						}
					}
					else {
						if (heavyArmorPlateHealth > 0) {
							heavyArmorPlateHealth = Math.max(heavyArmorPlateHealth - areaDamage * armorBreaking, 0);
						}
						
						actualDamageDealt += areaDamage;
						baseHealth -= areaDamage;
					}
				}
			}
			else if (i == 8) {
				// Special case: Q'ronar Shellback
				baseHealth *= largeResistance;
				
				totalDamageSpent = 0;
				actualDamageDealt = 0;
				while (baseHealth > 0) {
					// First, Direct Damage
					for (j = 0; j < numPellets; j++) {
						totalDamageSpent += directDamage;
						damageDealtPerPellet = 0;
						if (heavyArmorPlateHealth > 0) {
							if (armorBreaking > 1.0) {
								if (directDamage * armorBreaking > heavyArmorPlateHealth) {
									damageDealtPerPellet += directDamage;
									heavyArmorPlateHealth = 0;
								}
								else {
									// Direct Damage insufficient to break the Heavy Armor Plate
									heavyArmorPlateHealth -= directDamage * armorBreaking;
								}
							}
							else {
								if (directDamage * armorBreaking > heavyArmorPlateHealth) {
									heavyArmorPlateHealth = 0;
								}
								else {
									// Direct Damage insufficient to break the Heavy Armor Plate
									heavyArmorPlateHealth -= directDamage * armorBreaking;
								}
							}
						}
						else {
							damageDealtPerPellet += directDamage;
						}
						
						actualDamageDealt += damageDealtPerPellet;
						baseHealth -= damageDealtPerPellet;
					}
					
					// Second, Area Damage
					totalDamageSpent += areaDamage;
					if (embeddedDetonators) {
						if (heavyArmorPlateHealth == 0) {
							actualDamageDealt += areaDamage;
							baseHealth -= areaDamage;
						}
					}
					else {
						if (heavyArmorPlateHealth > 0) {
							heavyArmorPlateHealth = Math.max(heavyArmorPlateHealth - areaDamage * armorBreaking, 0);
						}
						
						actualDamageDealt += areaDamage;
						baseHealth -= areaDamage;
					}
				}
			}
			else if (i == 9) {
				// Special case: Mactera Brundle
				baseHealth *= normalResistance;
				
				double theoreticalDamagePerPellet;
				if (weakpointModifier < 0.0) {
					theoreticalDamagePerPellet = directDamage;
				}
				else {
					theoreticalDamagePerPellet = directDamage * (1.0 + weakpointModifier) * defaultWeakpointDamageBonusPerEnemyType[creatureIndex];
				}
				
				totalDamageSpent = 0;
				actualDamageDealt = 0;
				while (baseHealth > 0) {
					// First, Direct Damage
					for (j = 0; j < numPellets; j++) {
						totalDamageSpent += theoreticalDamagePerPellet;
						damageDealtPerPellet = 0;
						if (heavyArmorPlateHealth > 0) {
							if (armorBreaking > 1.0) {
								if (directDamage * armorBreaking > heavyArmorPlateHealth) {
									damageDealtPerPellet += theoreticalDamagePerPellet;
									heavyArmorPlateHealth = 0;
								}
								else {
									// Direct Damage insufficient to break the Heavy Armor Plate
									heavyArmorPlateHealth -= directDamage * armorBreaking;
								}
							}
							else {
								if (directDamage * armorBreaking > heavyArmorPlateHealth) {
									heavyArmorPlateHealth = 0;
								}
								else {
									// Direct Damage insufficient to break the Heavy Armor Plate
									heavyArmorPlateHealth -= directDamage * armorBreaking;
								}
							}
						}
						else {
							damageDealtPerPellet += theoreticalDamagePerPellet;
						}
						
						actualDamageDealt += damageDealtPerPellet;
						baseHealth -= damageDealtPerPellet;
					}
					
					// Second, Area Damage
					totalDamageSpent += areaDamage;
					if (embeddedDetonators) {
						if (heavyArmorPlateHealth == 0) {
							actualDamageDealt += areaDamage;
							baseHealth -= areaDamage;
						}
					}
					else {
						if (heavyArmorPlateHealth > 0) {
							heavyArmorPlateHealth = Math.max(heavyArmorPlateHealth - areaDamage * armorBreaking, 0);
						}
						
						actualDamageDealt += areaDamage;
						baseHealth -= areaDamage;
					}
				}
			}
			else {
				// General case
				if (i == 6 || i == 7) {
					// Menaces and Wardens get large HP scaling
					baseHealth *= largeResistance;
				}
				else {
					// All the other enemies get normal HP scaling
					baseHealth *= normalResistance;
				}
				
				proportionOfDamageThatHitsArmor = (100.0 - weakpointAccuracy) / 100.0;
				proportionOfDamageThatHitsWeakpoint = weakpointAccuracy / 100.0;
				
				if (creaturesArmorMatrix[i][2] > 0) {
					if (embeddedDetonators || (areaDamage > 0 && numPellets > 1)) {
						// Boomstick special case -- I'm choosing to model it as if the Blastwave doesn't break Light Armor Plates for simplicity later in the method
						avgNumHitsToBreakArmorStrengthPlate = (int) Math.ceil(MathUtils.meanRolls(lightArmorBreakProbabilityLookup(directDamage, armorBreaking, creaturesArmorMatrix[i][2])));
					}
					else {
						avgNumHitsToBreakArmorStrengthPlate = (int) Math.ceil(MathUtils.meanRolls(lightArmorBreakProbabilityLookup(directDamage + areaDamage, armorBreaking, creaturesArmorMatrix[i][2])));
					}
				}
				else {
					avgNumHitsToBreakArmorStrengthPlate = 0;
				}
				numHitsOnArmorStrengthPlate = 0;
				
				totalDamageSpent = 0;
				actualDamageDealt = 0;
				while (baseHealth > 0) {
					// First, Direct Damage
					for (j = 0; j < numPellets; j++) {
						if (weakpointModifier < 0) {
							totalDamageSpent += directDamage;
							damageDealtPerPellet = directDamage * proportionOfDamageThatHitsWeakpoint;
						}
						else {
							totalDamageSpent += directDamage * proportionOfDamageThatHitsWeakpoint * (1.0 + weakpointModifier) * defaultWeakpointDamageBonusPerEnemyType[creatureIndex] + directDamage * proportionOfDamageThatHitsArmor;
							damageDealtPerPellet = directDamage * proportionOfDamageThatHitsWeakpoint * (1.0 + weakpointModifier) * defaultWeakpointDamageBonusPerEnemyType[creatureIndex];
						}
						
						// 1. Light Armor plates (always Armor Strength, mixes with Heavy Armor plates on Guards)
						if (creaturesArmorMatrix[i][1] > 0) {
							numHitsOnArmorStrengthPlate++;
							if (numHitsOnArmorStrengthPlate > avgNumHitsToBreakArmorStrengthPlate || (armorBreaking > 1.0 && numHitsOnArmorStrengthPlate == avgNumHitsToBreakArmorStrengthPlate)) {
								damageDealtPerPellet += directDamage * proportionOfDamageThatHitsArmor * creaturesArmorMatrix[i][1] / (creaturesArmorMatrix[i][1] + creaturesArmorMatrix[i][3]);
							}
							else {
								damageDealtPerPellet += directDamage * proportionOfDamageThatHitsArmor * UtilityInformation.LightArmor_DamageReduction * creaturesArmorMatrix[i][1] / (creaturesArmorMatrix[i][1] + creaturesArmorMatrix[i][3]);
							}
						}
						
						if (creaturesArmorMatrix[i][3] > 0) {
							// 2. Heavy Armor Plates with health (mixes with Light Armor plates on Guards)
							if (creaturesArmorMatrix[i][4] > 0) { 
								if (heavyArmorPlateHealth > 0) {
									if (armorBreaking > 1.0) {
										if (directDamage * armorBreaking > heavyArmorPlateHealth) {
											damageDealtPerPellet += directDamage * proportionOfDamageThatHitsArmor * creaturesArmorMatrix[i][3] / (creaturesArmorMatrix[i][1] + creaturesArmorMatrix[i][3]);
											heavyArmorPlateHealth = 0;
										}
										else {
											// Direct Damage insufficient to break the Heavy Armor Plate
											heavyArmorPlateHealth -= directDamage * proportionOfDamageThatHitsArmor * armorBreaking;
										}
									}
									else {
										if (directDamage * proportionOfDamageThatHitsArmor * armorBreaking > heavyArmorPlateHealth) {
											heavyArmorPlateHealth = 0;
										}
										else {
											// Direct Damage insufficient to break the Heavy Armor Plate
											heavyArmorPlateHealth -= directDamage * proportionOfDamageThatHitsArmor * armorBreaking;
										}
									}
								}
								else {
									damageDealtPerPellet += proportionOfDamageThatHitsArmor * directDamage * creaturesArmorMatrix[i][3] / (creaturesArmorMatrix[i][1] + creaturesArmorMatrix[i][3]);
								}
							}
							// 3. Heavy Armor plates with Armor Strength (mutually exclusive with Light Armor plates)
							else if (creaturesArmorMatrix[i][1] == 0 && creaturesArmorMatrix[i][2] > 0) {
								numHitsOnArmorStrengthPlate++;
								if (numHitsOnArmorStrengthPlate > avgNumHitsToBreakArmorStrengthPlate || (armorBreaking > 1.0 && numHitsOnArmorStrengthPlate == avgNumHitsToBreakArmorStrengthPlate)) {
									damageDealtPerPellet += directDamage * proportionOfDamageThatHitsArmor;
								}
							}
						}
						
						actualDamageDealt += damageDealtPerPellet;
						baseHealth -= damageDealtPerPellet;
					}
					
					// Second, Area Damage
					totalDamageSpent += areaDamage;
					if (embeddedDetonators) {
						// Case 1: Guards' front leg plates have HP and block Embedded Detonators' damage until they're broken
						if (creaturesArmorMatrix[i][4] > 0) {
							if (heavyArmorPlateHealth == 0) {
								actualDamageDealt += areaDamage;
								baseHealth -= areaDamage;
							}
						}
						// Case 2: Wardens and Menaces have Heavy Armor that uses Armor Strength
						else if (creaturesArmorMatrix[i][1] == 0 && creaturesArmorMatrix[i][2] > 0) {
							// Detonators aren't placed until after the Heavy Armor plate is broken
							if (numHitsOnArmorStrengthPlate > avgNumHitsToBreakArmorStrengthPlate) {
								actualDamageDealt += areaDamage;
								baseHealth -= areaDamage;
							}
						}
						// Case 3: Light Armor plates don't stop the embedded detonators from dealing damage
						else if (creaturesArmorMatrix[i][1] > 0) {
							actualDamageDealt += areaDamage;
							baseHealth -= areaDamage;
						}
					}
					else {
						if (heavyArmorPlateHealth > 0) {
							heavyArmorPlateHealth = Math.max(heavyArmorPlateHealth - areaDamage * armorBreaking, 0);
						}
						
						actualDamageDealt += areaDamage;
						baseHealth -= areaDamage;
					}
				}
			}
			
			toReturn[0][i] = exactSpawnRates[creatureIndex];
			toReturn[1][i] = 1.0 - actualDamageDealt / totalDamageSpent;
		}
		
		return toReturn;
	}
	
	/*
		This method intentionally ignores elemental resistances/weaknesses and weakpoint damage bonuses because I don't want to repeat the Breakpoints insanity.
	*/
	public static double[][] overkillPerCreature(double totalDamagePerShot){
		double[][] toReturn = new double[2][exactSpawnRates.length];
		toReturn[0] = new double[exactSpawnRates.length];
		toReturn[1] = new double[exactSpawnRates.length];
		
		double normalResistance = normalEnemyResistances[hazardLevel - 1];
		double largeResistance = largeEnemyResistances[hazardLevel - 1][playerCount - 1];
		
		HashSet<Integer> normalEnemyScalingIndexes = new HashSet<Integer>(Arrays.asList(new Integer[] {0, 1, 2, 3, 5, 8, 9, 14, 20, 21, 22}));
		HashSet<Integer> largeEnemyScalingIndexes = new HashSet<Integer>(Arrays.asList(new Integer[] {4, 6, 7, 10, 11, 12, 13, 15, 16, 17, 18, 19}));
		
		double creatureHP;
		for (int i = 0; i < exactSpawnRates.length; i++) {
			if (normalEnemyScalingIndexes.contains(i)) {
				creatureHP = enemyHealthPools[i] * normalResistance;
			}
			else if (largeEnemyScalingIndexes.contains(i)) {
				creatureHP = enemyHealthPools[i] * largeResistance;
			}
			else {
				creatureHP = enemyHealthPools[i];
			}
			
			toReturn[0][i] = 1.0 / ((double) exactSpawnRates.length);
			toReturn[1][i] = ((Math.ceil(creatureHP / totalDamagePerShot) * totalDamagePerShot) / creatureHP - 1.0) * 100.0;
		}
		
		return toReturn;
	}
	
	/* 
		Dimensions of a Glyphid Grunt used for estimating how many grunts would be hit by AoE damage of a certain radius 
		(see method Weapon.calculateNumGlyphidsInRadius())
		Measured using meters
	*/
	// This is the radius of a Glyphid Grunt's hitbox that shouldn't overlap with other grunts, like the torso
	public static double GlyphidGruntBodyRadius = 0.41;
	// This is the radius of the entire Glyphid Grunt, from its center to the tip of its legs. The legs can overlap with other Grunts' legs.
	public static double GlyphidGruntBodyAndLegsRadius = 0.97;
	
}
