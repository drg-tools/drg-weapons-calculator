package modelPieces;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import enemies.Enemy;
import enemies.glyphid.*;
import enemies.mactera.*;
import enemies.other.*;
import utilities.MathUtils;

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
	
	private static double[] movespeedDifficultyScaling = {
		0.8,  // Haz1
		0.9,  // Haz2
		1.0,  // Haz3
		1.0,  // Haz4
		1.1   // Haz5
	};
	
	/* 
		Dimensions of a Glyphid Grunt used for estimating how many grunts would be hit by AoE damage of a certain radius 
		(see method Weapon.calculateNumGlyphidsInRadius())
		Measured using meters
	*/
	// This is the radius of a Glyphid Grunt's hitbox that shouldn't overlap with other grunts, like the torso
	// Calculated as average width (Torso width 0.733m + Back legs width 1.4m)/4
	public static double GlyphidGruntBodyRadius = 0.53325;
	// This is the radius of the entire Glyphid Grunt, from its center to the tip of its legs. The legs can overlap with other Grunts' legs.
	// Calculated as SqRt[(Full Length 2.73m / 2)*((Front legs 2.166m + Back legs 1.4m)/4)]
	public static double GlyphidGruntBodyAndLegsRadius = 1.10313;
	
	// Organized in same order as in-game Miner's Manual
	private static Enemy[] enemiesModeled = new Enemy[] {
		new Swarmer(),
		new Grunt(),
		new Guard(),
		new Slasher(),
		new Warden(),
		new Praetorian(),
		new Oppressor(),
		new AcidSpitter(),
		new WebSpitter(),
		new Menace(),
		new Exploder(),
		new BulkDetonator(),
		new BroodNexus(),
		new Spawn(),
		new Brundle(),
		new TriJaw(),
		new GooBomber(),
		new Grabber(),
		new NaedocyteBreeder(),
		new QronarShellback(),
		new SpitballInfector(),
		new CaveLeech()
	};
	
	private static boolean verifySpawnRatesTotalIsOne() {
		double sum = 0.0;
		for (int i = 0; i < enemiesModeled.length; i++) {
			sum += enemiesModeled[i].getSpawnProbability(true);
		}
		
		// Double addition is wonky; round it.
		sum = MathUtils.round(sum, 4);
		return sum == 1.0;
	}
	
	public static double probabilityBulletWillHitWeakpoint() {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		double toReturn = 0.0;
		for (int i = 0; i < enemiesModeled.length; i++) {
			toReturn += enemiesModeled[i].getSpawnProbability(true) * enemiesModeled[i].getProbabilityBulletHitsWeakpoint();
		}
		
		return toReturn;
	}
	
	public static double averageWeakpointDamageIncrease() {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		double toReturn = 0.0;
		for (int i = 0; i < enemiesModeled.length; i++) {
			toReturn += enemiesModeled[i].getSpawnProbability(true) * enemiesModeled[i].getWeakpointMultiplier();
		}
		
		return toReturn;
	}
	
	public static double averageHealthPool() {
		return averageHealthPool(true);
	}
	public static double averageHealthPool(boolean exact) {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		double normalResistance = normalEnemyResistances[hazardLevel - 1];
		double largeResistance = largeEnemyResistances[hazardLevel - 1][playerCount - 1];
		
		double toReturn = 0.0;
		Enemy alias;
		for (int i = 0; i < enemiesModeled.length; i++) {
			alias = enemiesModeled[i];
			if (alias.usesNormalScaling()) {
				toReturn += alias.getSpawnProbability(exact) * alias.getBaseHealth() * normalResistance;
			}
			else {
				toReturn += alias.getSpawnProbability(exact) * alias.getBaseHealth() * largeResistance;
			}
		}
		
		return toReturn;
	}
	
	public static double averageTimeToIgnite(double burstOfHeat, double heatPerShot, double RoF, double heatPerSec) {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		double igniteTemp, coolingRate, spawnProbability;
		
		double totalIgniteTime = 0.0;
		double totalProbability = 0.0;
		for (int i = 0; i < enemiesModeled.length; i++) {
			igniteTemp = enemiesModeled[i].getIgniteTemp();
			coolingRate = enemiesModeled[i].getCoolingRate();
			spawnProbability = enemiesModeled[i].getSpawnProbability(true);
			
			// Early exit: if Heat/Shot >= igniteTemp, then this enemy gets ignited instantly.
			if (burstOfHeat >= igniteTemp || heatPerShot >= igniteTemp || burstOfHeat + heatPerShot >= igniteTemp) {
				// Technically this adds (Exact Spawn Probability * 0.0) to totalIgniteTime, but to save some CPU cycles I'm just going to skip to the next enemy.
				totalProbability += spawnProbability;
				continue;
			}
			
			// Early exit: if the heat/sec of the weapon is <= cooling rate of an enemy, it will never ignite. Skip this enemy to avoid negative ignition times, or Infinity when divided by zero.
			if (heatPerShot * RoF + heatPerSec <= coolingRate) {
				continue;
			}
			
			totalIgniteTime += spawnProbability * ((igniteTemp - burstOfHeat) / (heatPerShot * RoF + heatPerSec - coolingRate));
			totalProbability += spawnProbability;
		}
		
		return totalIgniteTime / totalProbability;
	}
	public static double averageBurnDuration() {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		double toReturn = 0.0;
		for (int i = 0; i < enemiesModeled.length; i++) {
			toReturn += enemiesModeled[i].getSpawnProbability(true) * ((enemiesModeled[i].getIgniteTemp() - enemiesModeled[i].getDouseTemp()) / enemiesModeled[i].getCoolingRate());
		}
		
		return toReturn;
	}
	// This method is currently only used by Gunner/Minigun/Mod/5/Aggressive Venting in maxDamage() and Engineer/GrenadeLauncher/Mod/3/Incendiary Compound single-target DPS
	public static double percentageEnemiesIgnitedBySingleBurstOfHeat(double heatPerBurst) {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		double toReturn = 0.0;
		for (int i = 0; i < enemiesModeled.length; i++) {
			if (enemiesModeled[i].getIgniteTemp() <= heatPerBurst) {
				toReturn += enemiesModeled[i].getSpawnProbability(true);
			}
		}
		
		return MathUtils.round(toReturn, 4);
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
		
		double freezeTemp;
		
		double toReturn = 0.0;
		for (int i = 0; i < enemiesModeled.length; i++) {
			freezeTemp = enemiesModeled[i].getFreezeTemp();
			
			// Early exit: if Cold/Shot <= -490, then all enemies get frozen instantly since the largest Freeze Temp modeled in this program is -490 (Bulk Detonator).
			if (burstOfCold <= freezeTemp || coldPerShot <= freezeTemp || burstOfCold + coldPerShot <= freezeTemp) {
				// Technically this adds (Exact Spawn Probability * 0.0), but to save some CPU cycles I'm just going to skip to the next enemy.
				continue;
			}
			
			toReturn += enemiesModeled[i].getSpawnProbability(true) * ((freezeTemp - burstOfCold) / (coldPerShot * RoF + coldPerSec));
		}
		
		return toReturn;
	}
	// Because the creatures have had a negative temperature for longer than 2 seconds (due to being Frozen already) I'm keeping warming rate in the refreeze method
	public static double averageTimeToRefreeze(double coldPerSecond) {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		double toReturn = 0.0;
		for (int i = 0; i < enemiesModeled.length; i++) {
			toReturn += enemiesModeled[i].getSpawnProbability(true) * ((enemiesModeled[i].getFreezeTemp() - enemiesModeled[i].getUnfreezeTemp()) / (coldPerSecond + enemiesModeled[i].getWarmingRate()));
		}
		
		return toReturn;
	}
	public static double averageFreezeDuration() {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		double toReturn = 0.0;
		for (int i = 0; i < enemiesModeled.length; i++) {
			// Because every Freeze temp is negative and is strictly less than the corresponding Unfreeze temp, subtracting Freeze from Unfreeze guarantees a positive number.
			toReturn += enemiesModeled[i].getSpawnProbability(true) * ((enemiesModeled[i].getUnfreezeTemp() - enemiesModeled[i].getFreezeTemp()) / enemiesModeled[i].getWarmingRate());
		}
		
		return toReturn;
	}
	// This method is currently only used by Driller/CryoCannon/OC/Snowball in Utility
	public static double percentageEnemiesFrozenBySingleBurstOfCold(double coldPerBurst) {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		double toReturn = 0;
		for (int i = 0; i < enemiesModeled.length; i++) {
			if (enemiesModeled[i].getFreezeTemp() >= coldPerBurst) {
				toReturn += enemiesModeled[i].getSpawnProbability(true);
			}
		}
		
		return MathUtils.round(toReturn, 4);
	}
	
	public static double averageLightArmorStrength() {
		double totalLightArmorStrength = 0.0;
		double totalSpawnPercentage = 0.0;
		Enemy alias;
		for (int i = 0; i < enemiesModeled.length; i++) {
			alias = enemiesModeled[i];
			if (alias.hasLightArmor()) {
				totalLightArmorStrength += alias.getArmorStrength() * alias.getSpawnProbability(true);
				totalSpawnPercentage += alias.getSpawnProbability(true);
			}
		}
		
		return totalLightArmorStrength / totalSpawnPercentage;
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
		
		double toReturn = 0.0;
		for (int i = 0; i < enemiesModeled.length; i++) {
			toReturn += enemiesModeled[i].getSpawnProbability(true) * enemiesModeled[i].getCourage();
		}
		
		return toReturn;
	}
	
	/*
		The regular Fear status effect inflicted by weapons and grenades works like this: for every creature that has the Fear Factor attack applied to them,
		the probability that they will have the Fear status effect inflicted is equal to Fear Factor * (1.0 - Courage). If it is inflicted, then ground-based 
		enemies will move 10m away from the point of Fear at a rate of 1.5 * Max Movespeed * Difficulty Scaling * (1.0 - Movespeed Slow). As a result of this formula,
		Slowing an enemy that is being Feared will increase the duration of the Fear status effect, and it will naturally be shorter at higher hazard levels.
	*/
	public static double averageFearDuration() {
		return averageFearDuration(0.0, 0.0);
	}
	public static double averageFearDuration(double enemySlowMultiplier, double slowDuration) {
		double averageFearMovespeed = 0.0;
		for (int i = 0; i < enemiesModeled.length; i++) {
			averageFearMovespeed += enemiesModeled[i].getSpawnProbability(true) * enemiesModeled[i].getMaxMovespeedWhenFeared();
		}
		
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
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		double normalResistance = normalEnemyResistances[hazardLevel - 1];
		double largeResistance = largeEnemyResistances[hazardLevel - 1][playerCount - 1];
		
		double toReturn = 0.0;
		Enemy alias;
		for (int i = 0; i < enemiesModeled.length; i++) {
			alias = enemiesModeled[i];
			if (alias.usesNormalScaling()) {
				toReturn += alias.getSpawnProbability(true) * normalResistance;
			}
			else {
				toReturn += alias.getSpawnProbability(true) * largeResistance;
			}
		}
		
		return toReturn;
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
		ArrayList<Integer> toReturn = new ArrayList<Integer>();
		
		double normalResistance = normalEnemyResistances[hazardLevel - 1];
		double largeResistance = largeEnemyResistances[hazardLevel - 1][playerCount - 1];
		
		// Frozen
		double lightArmorReduction = UtilityInformation.LightArmor_DamageReduction;
		if (frozen) {
			// Removes Weakpoint Bonuses
			weakpointModifier = -1.0;
			
			// Bypasses all Armor types
			lightArmorReduction = 1.0;
			
			// Multiplies Direct Damage by x3 (including Flying Nightmare as of U34)
			directDamageByType = MathUtils.vectorScalarMultiply(UtilityInformation.Frozen_Damage_Multiplier, directDamageByType);
		}
		
		// Flying Nightmare is weird... it does the Direct Damage listed but it passes through enemies and ignores armor like the Breach Cutter, but doesn't benefit from Weakpoints.
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
		Enemy alias;
		for (int i = 0; i < enemiesModeled.length; i++) {
			alias = enemiesModeled[i];
			
			// If this enemy shouldn't be modeled in breakpoints, skip it.
			if (!alias.shouldHaveBreakpointsCalculated()) {
				continue;
			}
			
			if (alias.usesNormalScaling()) {
				creatureHP = alias.getBaseHealth() * normalResistance;
			}
			else {
				creatureHP = alias.getBaseHealth() * largeResistance;
			}
			
			creatureResistances = new double[] {
				1.0 - alias.getExplosiveResistance(),
				1.0 - alias.getFireResistance(),
				1.0 - alias.getFrostResistance(),
				1.0 - alias.getElectricResistance()
			};
			
			creatureWeakpointModifier = alias.getWeakpointMultiplier();
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
			if (alias.isMacteraType()) {
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
				if (heatPerShot >= alias.getIgniteTemp()) {
					numShotsToProcBurn = 1;
					burnDuration = (heatPerShot - alias.getDouseTemp()) / alias.getCoolingRate();
				}
				else {
					// This is technically an approximation and not precisely how it works in-game, but it's close enough for what I need.
					numShotsToProcBurn = Math.floor((alias.getIgniteTemp() * RoF) / (heatPerShot * RoF - alias.getCoolingRate()));
					burnDuration = (alias.getIgniteTemp() - alias.getDouseTemp()) / alias.getCoolingRate();
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
			if (alias.hasExposedBodySomewhere()) {
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
			if (alias.hasLightArmor()) {
				breakpointCounter = 0;
				aliasHP = creatureHP;
				
				lightArmorStrength = alias.getArmorStrength();
				
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
			if (alias.hasWeakpoint()) {
				breakpointCounter = 0;
				aliasHP = creatureHP;
				
				if (alias.weakpointIsCoveredByHeavyArmor()) {
					heavyArmorHP = alias.getArmorBaseHealth() * normalResistance;
					
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
		// I have not thought of an elegant way to look ahead and count how many enemies have Light or Heavy Armor. For now I'm going to "cheat" because I know in advance that the answer is 10.
		double[][] toReturn = new double[2][10];
		
		double normalResistance = normalEnemyResistances[hazardLevel - 1];
		double largeResistance = largeEnemyResistances[hazardLevel - 1][playerCount - 1];
		
		int creatureIndex = 0, i, j;
		double baseHealth, heavyArmorPlateHealth;
		double damageDealtPerPellet, proportionOfDamageThatHitsArmor, proportionOfDamageThatHitsWeakpoint;
		int avgNumHitsToBreakArmorStrengthPlate, numHitsOnArmorStrengthPlate;
		double totalDamageSpent, actualDamageDealt;
		Enemy alias;
		for (i = 0; i < enemiesModeled.length; i++) {
			alias = enemiesModeled[i];
			
			// Skip any enemy that either has no Armor or Unbreakable Armor
			if (!alias.hasBreakableArmor()) {
				continue;
			}
			
			baseHealth = alias.getBaseHealth();
			
			if (alias.hasHeavyArmorHealth()) {
				// All Heavy Armor plates with healthbars have their health scale with normal resistance.
				heavyArmorPlateHealth = alias.getArmorBaseHealth() * normalResistance;
			}
			else {
				heavyArmorPlateHealth = 0;
			}
			
			if (alias.getName().equals("Glyphid Praetorian")) {
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
			else if (alias.getName().equals("Q'ronar Shellback")) {
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
			else if (alias.getName().equals("Mactera Brundle")) {
				baseHealth *= normalResistance;
				
				double theoreticalDamagePerPellet;
				if (weakpointModifier < 0.0) {
					theoreticalDamagePerPellet = directDamage;
				}
				else {
					theoreticalDamagePerPellet = directDamage * (1.0 + weakpointModifier) * alias.getWeakpointMultiplier();
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
				if (alias.usesNormalScaling()) {
					baseHealth *= normalResistance;
				}
				else {
					baseHealth *= largeResistance;
				}
				
				proportionOfDamageThatHitsArmor = (100.0 - weakpointAccuracy) / 100.0;
				proportionOfDamageThatHitsWeakpoint = weakpointAccuracy / 100.0;
				
				if (alias.hasLightArmor() || alias.hasHeavyArmorStrength()) {
					if (embeddedDetonators || (areaDamage > 0 && numPellets > 1)) {
						// Boomstick special case -- I'm choosing to model it as if the Blastwave doesn't break Light Armor Plates for simplicity later in the method
						avgNumHitsToBreakArmorStrengthPlate = (int) Math.ceil(MathUtils.meanRolls(lightArmorBreakProbabilityLookup(directDamage, armorBreaking, alias.getArmorStrength())));
					}
					else {
						avgNumHitsToBreakArmorStrengthPlate = (int) Math.ceil(MathUtils.meanRolls(lightArmorBreakProbabilityLookup(directDamage + areaDamage, armorBreaking, alias.getArmorStrength())));
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
							totalDamageSpent += directDamage * proportionOfDamageThatHitsWeakpoint * (1.0 + weakpointModifier) * alias.getWeakpointMultiplier() + directDamage * proportionOfDamageThatHitsArmor;
							damageDealtPerPellet = directDamage * proportionOfDamageThatHitsWeakpoint * (1.0 + weakpointModifier) * alias.getWeakpointMultiplier();
						}
						
						// 1. Light Armor plates (always Armor Strength, mixes with Heavy Armor plates on Guards)
						if (alias.hasLightArmor()) {
							numHitsOnArmorStrengthPlate++;
							if (numHitsOnArmorStrengthPlate > avgNumHitsToBreakArmorStrengthPlate || (armorBreaking > 1.0 && numHitsOnArmorStrengthPlate == avgNumHitsToBreakArmorStrengthPlate)) {
								damageDealtPerPellet += directDamage * proportionOfDamageThatHitsArmor * alias.getNumArmorStrengthPlates() / (alias.getNumArmorStrengthPlates() + alias.getNumArmorHealthPlates());
							}
							else {
								damageDealtPerPellet += directDamage * proportionOfDamageThatHitsArmor * UtilityInformation.LightArmor_DamageReduction * alias.getNumArmorStrengthPlates() / (alias.getNumArmorStrengthPlates() + alias.getNumArmorHealthPlates());
							}
						}
						
						// 2. Heavy Armor Plates with health (mixes with Light Armor plates on Guards)
						if (alias.hasHeavyArmorHealth()) { 
							if (heavyArmorPlateHealth > 0) {
								if (armorBreaking > 1.0) {
									if (directDamage * armorBreaking > heavyArmorPlateHealth) {
										damageDealtPerPellet += directDamage * proportionOfDamageThatHitsArmor * alias.getNumArmorHealthPlates() / (alias.getNumArmorStrengthPlates() + alias.getNumArmorHealthPlates());
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
								damageDealtPerPellet += proportionOfDamageThatHitsArmor * directDamage * alias.getNumArmorHealthPlates() / (alias.getNumArmorStrengthPlates() + alias.getNumArmorHealthPlates());
							}
						}
						
						// 3. Heavy Armor plates with Armor Strength (mutually exclusive with Light Armor plates)
						if (alias.hasHeavyArmorStrength()) {
							numHitsOnArmorStrengthPlate++;
							if (numHitsOnArmorStrengthPlate > avgNumHitsToBreakArmorStrengthPlate || (armorBreaking > 1.0 && numHitsOnArmorStrengthPlate == avgNumHitsToBreakArmorStrengthPlate)) {
								damageDealtPerPellet += directDamage * proportionOfDamageThatHitsArmor;
							}
						}
						
						actualDamageDealt += damageDealtPerPellet;
						baseHealth -= damageDealtPerPellet;
					}
					
					// Second, Area Damage
					totalDamageSpent += areaDamage;
					if (embeddedDetonators) {
						// Case 1: Guards' front leg plates have HP and block Embedded Detonators' damage until they're broken
						if (alias.hasHeavyArmorHealth()) {
							if (heavyArmorPlateHealth == 0) {
								actualDamageDealt += areaDamage;
								baseHealth -= areaDamage;
							}
						}
						// Case 2: Wardens and Menaces have Heavy Armor that uses Armor Strength
						else if (alias.hasHeavyArmorStrength()) {
							// Detonators aren't placed until after the Heavy Armor plate is broken
							if (numHitsOnArmorStrengthPlate > avgNumHitsToBreakArmorStrengthPlate) {
								actualDamageDealt += areaDamage;
								baseHealth -= areaDamage;
							}
						}
						// Case 3: Light Armor plates don't stop the embedded detonators from dealing damage
						else if (alias.hasLightArmor()) {
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
			
			toReturn[0][creatureIndex] = alias.getSpawnProbability(true);
			toReturn[1][creatureIndex] = 1.0 - actualDamageDealt / totalDamageSpent;
			creatureIndex++;
		}
		
		return toReturn;
	}
	
	/*
		This method intentionally ignores elemental resistances/weaknesses and weakpoint damage bonuses because I don't want to repeat the Breakpoints insanity.
	*/
	public static double[][] overkillPerCreature(double totalDamagePerShot){
		int numEnemies = enemiesModeled.length;
		double[][] toReturn = new double[2][numEnemies];
		toReturn[0] = new double[numEnemies];
		toReturn[1] = new double[numEnemies];
		
		double normalResistance = normalEnemyResistances[hazardLevel - 1];
		double largeResistance = largeEnemyResistances[hazardLevel - 1][playerCount - 1];
		
		double creatureHP;
		for (int i = 0; i < enemiesModeled.length; i++) {
			if (enemiesModeled[i].usesNormalScaling()) {
				creatureHP = enemiesModeled[i].getBaseHealth() * normalResistance;
			}
			else {
				creatureHP = enemiesModeled[i].getBaseHealth() * largeResistance;
			}
			
			toReturn[0][i] = 1.0 / ((double) numEnemies);
			toReturn[1][i] = ((Math.ceil(creatureHP / totalDamagePerShot) * totalDamagePerShot) / creatureHP - 1.0) * 100.0;
		}
		
		return toReturn;
	}
}
