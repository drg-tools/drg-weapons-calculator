package modelPieces;

import utilities.MathUtils;

public class EnemyInformation {
	
	private static int hazardLevel = 4;
	private static int playerCount = 1;
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
	
	// These are educated guesses about the enemies' spawn rates. Biome-specific enemies, "hatchling" enemy types, and Dreadnaughts not included.
	// All of these numbers must sum up to exactly 1.0 for it to be a probability vector.
	// TODO: verify these spawn rate numbers; I think there are more grunts and fewer swarmers.
	private static double[] spawnRates = {
		0.165, // Glyphid Swarmer
		0.24,  // Glyphid Grunt
		0.08,  // Glyphid Grunt Guard
		0.08,  // Glyphid Grunt Slasher
		0.04,  // Glyphid Praetorian
		0.08,  // Glyphid Exploder
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
		0.01   // Cave Leech
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
		0.0   // Cave Leech (no weakpoint)
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
		0.0   // Cave Leech (no weakpoint)
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
		100    // Cave Leech
	};
	
	// Resistance/weakness values taken from wiki
	// Positive number means that the creature resists that element; negative means it's weak to that element.
	
	// Weighted Q'Ronar Shellback rolling state at 2/3 and non-rolling state at 1/3
	private static double qronarShellbackRolling = 0.66;
	private static double qronarShellbackUnolled = 0.34;
	private static double[][] enemyResistances = {
		// Explosive, Fire, Frost, Electric
		{0, 0, 0, 0},  				// Glyphid Swarmer
		{0, 0, 0, 0},  				// Glyphid Grunt
		{0.3, 0.3, 0.3, 0.3},  		// Glyphid Grunt Guard
		{-0.3, 0, 0, 0},  			// Glyphid Grunt Slasher
		{0, 0, 0, 0},  				// Glyphid Praetorian
		{0, 0, 0, 0},  				// Glyphid Exploder
		{0.5, 0, -1, 0},  			// Glyphid Bulk Detonator
		{0.5, 0, -1, 0},  			// Glyphid Crassus Detonator
		{0, 0, 0, 0},  				// Glyphid Webspitter
		{0, 0, 0, 0},  				// Glyphid Acidspitter
		{0, 0, 0, 0},  				// Glyphid Menace
		{0, 0, -1, 0},  			// Glyphid Warden
		{0.66, 0.66, 0.66, 0.3},  	// Glyphid Oppressor
		{qronarShellbackRolling*0.8, qronarShellbackRolling*0.3 + qronarShellbackUnolled*-0.5, qronarShellbackRolling*0.3 + qronarShellbackUnolled*-0.7, qronarShellbackRolling*1.0},  // Q'ronar Shellback
		{-1, -1, 0, 0},  			// Mactera Spawn
		{0, 0, 0, 0},  				// Mactera Grabber
		{0, -0.2, 0, 0},  			// Mactera Bomber
		{0, 0, 0, 0},  				// Naedocyte Breeder
		{0, 0, 0, 0},  				// Glyphid Brood Nexus
		{0, -1, 0, 0},  			// Spitball Infector
		{0, 0, 0, 0}   				// Cave Leech
	};
	
	// TODO: update this chart once Elythnwaen finishes that spreadsheet.
	private static double[][] enemyTemperatures = {
		// Ignite Temp, Douse Temp, Heat Loss Rate, Freeze Temp, Thaw Temp, Heat Gain Rate
		{5, 0, 1, -20, 0, 4},			// Glyphid Swarmer
		{25, 10, 3, -30, 0, 6},			// Glyphid Grunt
		{100, 40, 10, -150, -100, 10},	// Glyphid Grunt Guard
		{25, 10, 3, -30, 0, 6},			// Glyphid Grunt Slasher
		{100, 40, 10, -150, -100, 10},	// Glyphid Praetorian
		{5, 0, 1, -10, 0, 12},			// Glyphid Exploder
		{100, 40, 10, -250, -200, 50},	// Glyphid Bulk Detonator
		{100, 40, 10, -250, -200, 50},	// Glyphid Crassus Detonator
		{25, 10, 3, -30, 0, 6},			// Glyphid Webspitter
		{25, 10, 3, -30, 0, 6},			// Glyphid Acidspitter
		{25, 10, 3, -30, 0, 6},			// Glyphid Menace
		{50, 25, 5, -70, -30, 8},		// Glyphid Warden
		{100, 40, 10, -150, -100, 10},	// Glyphid Oppressor
		{100, 40, 10, -150, -100, 10},	// Q'ronar Shellback
		{25, 10, 3, -30, 0, 0},			// Mactera Spawn
		{25, 10, 3, -180, 0, 0},		// Mactera Grabber
		{25, 10, 3, -30, 0, 0},			// Mactera Bomber
		{60, 30, 6, -150, 1, 0},		// Naedocyte Breeder
		{10, 0, 4, -20, 0, 4},			// Glyphid Brood Nexus
		{25, 10, 3, -30, 0, 6},			// Spitball Infector
		{5, 0, 1, -20, 0, 4}			// Cave Leech
	};
	
	private static boolean verifySpawnRatesTotalIsOne() {
		double sum = 0.0;
		for (int i = 0; i < spawnRates.length; i++) {
			sum += spawnRates[i];
		}
		// Double addition is wonky; round it.
		sum = MathUtils.round(sum, 2);
		return sum == 1.0;
	}
	
	// This gets used in Gunner/Minigun/Mod/5/Cold as the Grave
	public static double dotProductWithSpawnRates(double[] A) {
		return MathUtils.vectorDotProduct(A, spawnRates);
	}
	
	public static double probabilityBulletWillHitWeakpoint() {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		double toReturn = MathUtils.vectorDotProduct(spawnRates, probabilityBulletHitsWeakpointPerEnemyType);
		// System.out.println("Estimated percentage of bullets fired that will hit a weakpoint: " + toReturn);
		return toReturn;
	}
	
	public static double averageWeakpointDamageIncrease() {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		double toReturn = MathUtils.vectorDotProduct(spawnRates, defaultWeakpointDamageBonusPerEnemyType);
		// System.out.println("Average damage multiplier from hitting a weakpoint: " + toReturn);
		return toReturn;
	}
	
	public static double averageHealthPool() {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		/*
		// Seems like Acidspitters spawn in pairs, and Webspitters spawn in groups of 2-4? Swarmers seem like 6-10? Exploders 4-6?
		double numerator = 25;
		double predictedSpawnRate;
		for (int i = 0; i < enemyHealthPools.length; i++) {
			predictedSpawnRate = numerator / enemyHealthPools[i];
			System.out.println("Health Pool: " + enemyHealthPools[i] + " Estimated Spawn Rate: " + spawnRates[i] + " Predicted Spawn Rate: " + predictedSpawnRate);
		}
 		*/
		
		int i, enemyIndex;

		// Normal enemies have their health scaled up or down depending on Hazard Level, with the notable exception that the health does not currently increase between Haz4 and haz5
		double[] normalEnemyResistances = {
			0.7,  // Haz1
			1.0,  // Haz2
			1.1,  // Haz3
			1.2,  // Haz4
			1.2   // Haz5
		};
		double normalResistance = normalEnemyResistances[hazardLevel - 1];
		int[] normalEnemyIndexes = {0, 1, 2, 3, 5, 8, 9, 14, 20};
		double normalEnemyHealth = 0;
		for (i = 0; i < normalEnemyIndexes.length; i++) {
			enemyIndex = normalEnemyIndexes[i];
			normalEnemyHealth += spawnRates[enemyIndex] * enemyHealthPools[enemyIndex];
		}
		normalEnemyHealth *= normalResistance;
		
		// On the other hand, large and extra-large enemies have their health scale by both player count and Hazard Level for all 20 combinations.
		// Currently, it looks like the only extra-large enemy is a Dreadnought which I've chosen not to model for now.
		double[][] largeEnemyResistances = {
			{0.45, 0.55, 0.70, 0.85},  // Haz1
			{0.65, 0.75, 0.90, 1.00},  // Haz2
			{0.80, 0.90, 1.00, 1.10},  // Haz3
			{1.00, 1.00, 1.20, 1.30},  // Haz4
			{1.20, 1.20, 1.40, 1.50}   // Haz5
		};
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
	
	public static double averageResistanceCoefficient(int resistanceIndex) {
		/*
			0. Explosive
			1. Fire
			2. Frost
			3. Electric
		*/
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		if (resistanceIndex < 0 || resistanceIndex > 3) {
			return -1.0;
		}
		
		int vectorLength = enemyResistances.length;
		double[] weightedResistancesVector = new double[vectorLength];
		for (int i = 0; i < vectorLength; i++) {
			weightedResistancesVector[i] = enemyResistances[i][resistanceIndex];
		}
		
		double toReturn = MathUtils.vectorDotProduct(spawnRates, weightedResistancesVector);
		toReturn = MathUtils.round(toReturn, 3);
		// System.out.println("Average resistance/weakness of an enemy to element #" + resistanceIndex + ": " + toReturn);
		// Subtract the value from 1 so that this method returns a static coefficient to multiply damage taken by enemies
		return 1.0 - toReturn;
	}
	
	public static double averageTimeToIgnite(double heatPerShot, double RoF) {
		// Early exit: if Heat/Shot > 100, then all enemies get ignited instantly since the largest Ignite Temp is 100.
		if (heatPerShot >= 100) {
			return 0;
		}
		
		return averageTimeToIgnite(heatPerShot * RoF);
	}
	public static double averageTimeToIgnite(double heatPerSecond) {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		int numEnemyTypes = spawnRates.length;
		double[] igniteTemps = new double[numEnemyTypes];
		
		for (int i = 0; i < numEnemyTypes; i++) {
			igniteTemps[i] = enemyTemperatures[i][0];
		}
		
		double avgIgniteTemp = MathUtils.vectorDotProduct(spawnRates, igniteTemps);
		
		return avgIgniteTemp / heatPerSecond;
	}
	public static double averageBurnDuration() {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		int numEnemyTypes = spawnRates.length;
		double[] igniteTemps = new double[numEnemyTypes];
		double[] douseTemps = new double[numEnemyTypes];
		double[] heatLossRates = new double[numEnemyTypes];
		
		for (int i = 0; i < numEnemyTypes; i++) {
			igniteTemps[i] = enemyTemperatures[i][0];
			douseTemps[i] = enemyTemperatures[i][1];
			heatLossRates[i] = enemyTemperatures[i][2];
		}
		
		double avgIgniteTemp = MathUtils.vectorDotProduct(spawnRates, igniteTemps);
		double avgDouseTemp = MathUtils.vectorDotProduct(spawnRates, douseTemps);
		double avgHeatLossRate = MathUtils.vectorDotProduct(spawnRates, heatLossRates);
		
		return (avgIgniteTemp - avgDouseTemp) / avgHeatLossRate;
	}
	
	// This method is currently only used by Gunner/Minigun/Mod/5/Aggressive Venting in maxDamage() and Engineer/GrenadeLauncher/Mod/3/Incendiary Compound single-target DPS
	public static double percentageEnemiesIgnitedBySingleBurstOfHeat(double heatPerBurst) {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		double sum = 0;
		for (int i = 0; i < spawnRates.length; i++) {
			if (enemyTemperatures[i][0] < heatPerBurst) {
				sum += spawnRates[i];
			}
		}
		
		return MathUtils.round(sum, 4);
	}
	
	// Cold per shot should be a negative number to indicate that the enemy's temperature is being decreased
	public static double averageTimeToFreeze(double coldPerShot, double RoF) {
		// Early exit: if Cold/Sec > 150, then all enemies get frozen instantly since the largest Freeze Temp is 150.
		if (coldPerShot <= -150) {
			return 0;
		}
		
		return averageTimeToFreeze(coldPerShot * RoF);
	}
	public static double averageTimeToFreeze(double coldPerSecond) {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		int numEnemyTypes = spawnRates.length;
		double[] freezeTemps = new double[numEnemyTypes];
		
		for (int i = 0; i < numEnemyTypes; i++) {
			freezeTemps[i] = enemyTemperatures[i][3];
		}
		
		double avgFreezeTemp = MathUtils.vectorDotProduct(spawnRates, freezeTemps);
		
		// Negative Freeze temps divided by negative cold per seconds results in a positive number of seconds
		return avgFreezeTemp / coldPerSecond;
	}
	public static double averageTimeToRefreeze(double coldPerSecond) {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		int numEnemyTypes = spawnRates.length;
		double[] freezeTemps = new double[numEnemyTypes];
		double[] thawTemps = new double[numEnemyTypes];
		
		for (int i = 0; i < numEnemyTypes; i++) {
			freezeTemps[i] = enemyTemperatures[i][3];
			thawTemps[i] = enemyTemperatures[i][4];
		}
		
		double avgFreezeTemp = MathUtils.vectorDotProduct(spawnRates, freezeTemps);
		double avgThawTemp = MathUtils.vectorDotProduct(spawnRates, thawTemps);
		
		// Negative Freeze temps divided by negative cold per seconds results in a positive number of seconds
		return (avgFreezeTemp - avgThawTemp) / coldPerSecond;
	}
	public static double averageFreezeDuration() {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		int numEnemyTypes = spawnRates.length;
		double[] freezeTemps = new double[numEnemyTypes];
		double[] thawTemps = new double[numEnemyTypes];
		double[] heatGainRates = new double[numEnemyTypes];
		
		for (int i = 0; i < numEnemyTypes; i++) {
			freezeTemps[i] = enemyTemperatures[i][3];
			thawTemps[i] = enemyTemperatures[i][4];
			heatGainRates[i] = enemyTemperatures[i][5];
		}
		
		double avgFreezeTemp = MathUtils.vectorDotProduct(spawnRates, freezeTemps);
		double avgThawTemp = MathUtils.vectorDotProduct(spawnRates, thawTemps);
		double avgHeatGainRate = MathUtils.vectorDotProduct(spawnRates, heatGainRates);
		
		// Because every Freeze temp is negative and is strictly less than the corresponding Thaw temp, subtracting Freeze from Thaw guarantees a positive number.
		return (avgThawTemp - avgFreezeTemp) / avgHeatGainRate;
	}
	// This method is currently only used by Driller/CryoCannon/OC/Snowball in Utility
	public static double percentageEnemiesFrozenBySingleBurstOfCold(double coldPerBurst) {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		double sum = 0;
		for (int i = 0; i < spawnRates.length; i++) {
			if (enemyTemperatures[i][3] > coldPerBurst) {
				sum += spawnRates[i];
			}
		}
		
		return MathUtils.round(sum, 4);
	}
	
	/* 
		Dimensions of a Glyphid Grunt used for estimating how many grunts would be hit by AoE damage of a certain radius 
		(see method Weapon.calculateNumGlyphidsInRadius())
		Measured using meters
	*/
	// This is the radius of a Glyphid Grunt's hitbox that shouldn't overlap with other grunts, like the torso
	public static double GlyphidGruntBodyRadius = 0.4;
	// This is the radius of the entire Glyphid Grunt, from its center to the tip of its legs. The legs can overlap with other Grunts' legs.
	public static double GlyphidGruntBodyAndLegsRadius = 0.9;
	
}
