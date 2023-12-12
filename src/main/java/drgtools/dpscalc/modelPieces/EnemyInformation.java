package drgtools.dpscalc.modelPieces;

import drgtools.dpscalc.enemies.Enemy;
import drgtools.dpscalc.enemies.glyphid.*;
import drgtools.dpscalc.enemies.mactera.*;
import drgtools.dpscalc.enemies.other.*;
import drgtools.dpscalc.modelPieces.damage.DamageInstance;
import drgtools.dpscalc.modelPieces.temperature.CreatureTemperatureComponent;
import drgtools.dpscalc.utilities.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
		1.15   // Haz5
	};
	public static double getMovespeedDifficultyScaling() {
		return movespeedDifficultyScaling[hazardLevel - 1];
	}
	
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
	public static Enemy[] enemiesModeled = new Enemy[] {
		new Swarmer(),
		new Grunt(),
		new Guard(),
		new Slasher(),
		new Warden(),
		new Stingtail(),
		new Praetorian(),
		new Oppressor(),
		new AcidSpitter(),
		new WebSpitter(),
		new Spreader(),
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
			igniteTemp = enemiesModeled[i].getTemperatureComponent().getEffectiveBurnTemperature();
			coolingRate = enemiesModeled[i].getTemperatureComponent().getCoolingRate();
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

		CreatureTemperatureComponent aliasTemp;
		double spawnProbability;
		double totalBurnDuration = 0.0;
		double totalProbability = 0.0;
		for (int i = 0; i < enemiesModeled.length; i++) {
			aliasTemp = enemiesModeled[i].getTemperatureComponent();
			if (!aliasTemp.diesIfOnFire()){
				spawnProbability = enemiesModeled[i].getSpawnProbability(true);
				totalBurnDuration += spawnProbability * ((aliasTemp.getEffectiveBurnTemperature() - aliasTemp.getEffectiveDouseTemperature()) / aliasTemp.getCoolingRate());
				totalProbability += spawnProbability;
			}
		}
		
		return totalBurnDuration / totalProbability;
	}
	// This method is currently only used by Gunner/Minigun/Mod/5/Aggressive Venting in maxDamage() and Engineer/GrenadeLauncher/Mod/3/Incendiary Compound single-target DPS
	public static double percentageEnemiesIgnitedBySingleBurstOfHeat(double heatPerBurst) {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		double toReturn = 0.0;
		for (int i = 0; i < enemiesModeled.length; i++) {
			if (enemiesModeled[i].getTemperatureComponent().getEffectiveBurnTemperature() <= heatPerBurst) {
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
			freezeTemp = enemiesModeled[i].getTemperatureComponent().getEffectiveFreezeTemperature();
			
			// Early exit: if Cold/Shot <= -490, then all enemies get frozen instantly since the largest Freeze Temp modeled in this program is -490 (Bulk Detonator).
			if (burstOfCold <= freezeTemp || coldPerShot <= freezeTemp || burstOfCold + coldPerShot <= freezeTemp) {
				// Technically this adds (Exact Spawn Probability * 0.0), but to save some CPU cycles I'm just going to skip to the next enemy.
				continue;
			}
			
			toReturn += enemiesModeled[i].getSpawnProbability(true) * ((freezeTemp - burstOfCold) / (coldPerShot * RoF + coldPerSec));
		}
		
		return toReturn;
	}
	// Because the creatures immediately start warming up after being Frozen (bypasses WarmingCooldown), I'm adding warming rate in the refreeze method
	public static double averageTimeToRefreeze(double coldPerSecond) {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}

		CreatureTemperatureComponent aliasTemp;
		double spawnProbability;
		double totalRefreezeTime = 0.0;
		double totalProbability = 0.0;
		for (int i = 0; i < enemiesModeled.length; i++) {
			aliasTemp = enemiesModeled[i].getTemperatureComponent();
			if (!aliasTemp.diesIfFrozen()) {
				spawnProbability = enemiesModeled[i].getSpawnProbability(true);
				totalRefreezeTime += spawnProbability * ((aliasTemp.getEffectiveFreezeTemperature() - aliasTemp.getEffectiveUnfreezeTemperature()) / (coldPerSecond + aliasTemp.getWarmingRate()));
				totalProbability += spawnProbability;
			}
		}
		
		return totalRefreezeTime / totalProbability;
	}
	public static double averageFreezeDuration() {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}

		CreatureTemperatureComponent aliasTemp;
		double spawnProbability;
		double totalFreezeDuration = 0.0;
		double totalProbability = 0.0;
		for (int i = 0; i < enemiesModeled.length; i++) {
			// When creatures get Frozen, they bypass WarmingCooldown and immediately start warming up. The player can extend the duration by applying more cold,
			// but this models as if they just freeze it and then let it thaw. Don't need to worry about overshooting the estimate because MinTemp == FreezeTemp for all enemies.
			aliasTemp = enemiesModeled[i].getTemperatureComponent();
			if (!aliasTemp.diesIfFrozen()) {
				// Because every Freeze temp is negative and is strictly less than the corresponding Unfreeze temp, subtracting Freeze from Unfreeze guarantees a positive number.
				spawnProbability = enemiesModeled[i].getSpawnProbability(true);
				totalFreezeDuration += spawnProbability * ((aliasTemp.getEffectiveUnfreezeTemperature() - aliasTemp.getEffectiveFreezeTemperature()) / aliasTemp.getWarmingRate());
				totalProbability += spawnProbability;
			}
		}
		
		return totalFreezeDuration / totalProbability;
	}
	// This method is currently only used by Driller/CryoCannon/OC/Snowball in Utility
	public static double percentageEnemiesFrozenBySingleBurstOfCold(double coldPerBurst) {
		if (!verifySpawnRatesTotalIsOne()) {
			return -1.0;
		}
		
		double toReturn = 0;
		for (int i = 0; i < enemiesModeled.length; i++) {
			if (enemiesModeled[i].getTemperatureComponent().getEffectiveFreezeTemperature() >= coldPerBurst) {
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

	public static double averageMovespeed() {
		double averageMovespeed = 0.0;
		for (int i = 0; i < enemiesModeled.length; i++) {
			averageMovespeed += enemiesModeled[i].getSpawnProbability(true) * enemiesModeled[i].getMaxMovespeedWhenFeared();
		}
		return averageMovespeed * movespeedDifficultyScaling[hazardLevel - 1];
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
		double averageFearMovespeed = averageMovespeed();
		
		// This value gathered from internal property TSK_FleeFrom_C.distance
		double fearDistanceGoal = 10.0;
		// 1.5 multiplier comes from DeepPathfinderMovement.FleeSpeedBoostMultiplier
		double compositeAverageEnemyMovespeed = 1.5 * averageFearMovespeed * (1.0 - enemySlowMultiplier);
		
		double rawDuration = fearDistanceGoal / compositeAverageEnemyMovespeed;
		if (enemySlowMultiplier > 0 && rawDuration > slowDuration) {
			// If the slow runs out before the average enemy has finished moving the distance goal, then the rest of the distance will be at normal speed.
			double remainingDistance = fearDistanceGoal - slowDuration * compositeAverageEnemyMovespeed;
			return slowDuration + remainingDistance / (1.5 * averageFearMovespeed);
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
		This method is used to quickly show how many shots it would take for hitscan or projectile weapons to kill the 22 modeled creatures under various conditions.
		It models DamageComponents, Damage Flags, Material Flags, Elemental resistances, Status Effects, Light Armor, Weakpoints (including Brundle's covered by ArmorHealth),
		IFG, and Frozen.

		This method does NOT model Heavy Armor plates except for Mactera Brundle because those Heavy Armor plates cover its weakpoint.
		
		If the weapon can do at least one DoT, this will look ahead to see if up to 4 seconds of DoT damage can kill a creature. If it can, then it will finish on that Breakpoint
		early instead of wasting superfluous ammo.
	*/
	public static int[] calculateBreakpoints(DamageInstance dmgInstance, double RoF, boolean IFG, boolean frozen) {
		ArrayList<Integer> toReturn = new ArrayList<>();
		Enemy alias;
		for (int i = 0; i < enemiesModeled.length; i++) {
			alias = enemiesModeled[i];
			if (alias.shouldHaveBreakpointsCalculated()) {
				toReturn.addAll(alias.calculateBreakpoints(dmgInstance, RoF, IFG, frozen,
					normalEnemyResistances[hazardLevel - 1], largeEnemyResistances[hazardLevel - 1][playerCount - 1]));
			}
		}
				
		return convertArrayListToArray(toReturn);
	}
	
	// Sourced from https://stackoverflow.com/a/718558
	private static int[] convertArrayListToArray(List<Integer> integers) {
	    int[] ret = new int[integers.size()];
	    Iterator<Integer> iterator = integers.iterator();
	    for (int i = 0; i < ret.length; i++) {
	        ret[i] = iterator.next();
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
	public static double[][] percentageDamageWastedByArmor(DamageInstance dmgInstance, double generalAccuracy, double weakpointAccuracy) {
		// Because Scala has made me lazy, lol
		Enemy[] enemiesWithBreakableArmor = Arrays.stream(enemiesModeled).filter(Enemy::hasBreakableArmor).toArray(Enemy[]::new);
		double[][] toReturn = new double[2][enemiesWithBreakableArmor.length];

		Enemy enemyAlias;
		for (int i = 0; i < enemiesWithBreakableArmor.length; i++) {
			enemyAlias = enemiesModeled[i];
			toReturn[0][i] = enemyAlias.getSpawnProbability(true);
			toReturn[1][i] = enemyAlias.calculatePercentageOfDamageWastedByArmor(dmgInstance, generalAccuracy, weakpointAccuracy,
				normalEnemyResistances[hazardLevel - 1], largeEnemyResistances[hazardLevel - 1][playerCount - 1]);
		}
		
		return toReturn;
	}

	/*
		This method intentionally ignores weakpoint damage bonuses because and armor reduction because I don't want to repeat the Breakpoints insanity.
	*/
	public static double[][] overkillPerCreature(DamageInstance dmgInstance){
		int numEnemies = enemiesModeled.length;
		double[][] toReturn = new double[2][numEnemies];
		toReturn[0] = new double[numEnemies];
		toReturn[1] = new double[numEnemies];
		for (int i = 0; i < numEnemies; i++) {
			toReturn[0][i] = 1.0 / ((double) numEnemies);
			toReturn[1][i] = enemiesModeled[i].calculateOverkill(dmgInstance,
				normalEnemyResistances[hazardLevel - 1], largeEnemyResistances[hazardLevel - 1][playerCount - 1]);
		}
		return toReturn;
	}
}
