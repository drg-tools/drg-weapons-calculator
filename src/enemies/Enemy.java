package enemies;

public class Enemy {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	// These are the values that I guessed for the proportion of each enemy spawn type. It worked REALLY well for avg TTK-based mods like Cold as the Grave and Battle Cool, but it's not representative of the actual game.
	// All of these numbers must sum up to exactly 1.0 for it to be a probability vector.
	protected double guessedSpawnProbability;
	/* 
		When U33 introduced the Tri-Jaw and Brundle common enemies, I had to redo these probabilities. To that end I chose to write down what the current kill counter was for every enemy type,
		and then play vanilla Haz4/5 until I achieved at least 15,000 Grunt kills. In the end it took me about 50 hours of playtime to achieve that, and I ended up with a total of 33,606 kills 
		of all kinds for these probability amounts. It's not as broad as U31's 153,000 kills from 6 players, but I didn't want to ask people to go 50 hours of playtime only on vanilla Haz4/5.
		
		Biome-specific enemies, "hatchling" enemy types, and Dreadnoughts not included.
		All of these numbers must sum up to exactly 1.0 for it to be a probability vector.
	*/
	protected double exactSpawnProbability;
	
	// Only the largest/tankiest enemies have this set to false.
	protected boolean calculateBreakpoints = true;
	
	protected String enemyName;
	protected boolean macteraType = false;  // Used for Subata T5.B
	// These base values are just taken from the Wiki's default values; Hazard level and player count not factored in. (effectively Haz2, 4 players)
	protected double baseHealth;
	protected boolean normalScaling;
	
	// a.k.a. you can shoot this enemy somewhere that isn't covered by Armor and isn't a Weakpoint (used in Breakpoints)
	protected boolean hasExposedBodySomewhere = false;
	
	protected boolean hasWeakpoint = false;
	// These numbers are taken straight from the Wiki
	protected double weakpointMultiplier;
	// These numbers are estimates of what percentage of bullets shot at each enemy type will hit the enemy's weakpoints
	protected double estimatedProbabilityBulletHitsWeakpoint;
	// TODO: this could be a good place to model breakable Weakpoints later?
	
	// Resistance/weakness values taken from Elythnwaen's Spreadsheet
	// If this number is greater than 0, that means that it takes less damage from that particular element.
	// Conversely, if it's less than 0 it takes extra damage from that particular element
	// None of the enemies I'm modeling resist Poison or Radiation damage
	protected double explosiveResistance = 0.0, fireResistance = 0.0, frostResistance = 0.0, electricResistance = 0.0;
	
	// This info comes from Elythnwaen's Temperatures spreadsheet, and many of those values were seeded from MikeGSG giving us the values for the 5 "base" creature types.
	protected double temperatureUpdateTime = 1.0, temperatureChangeScale = 1.0;
	protected double igniteTemperature, douseTemperature, coolingRate;
	protected double freezeTemperature, unfreezeTemperature, warmingRate;
	// These three variables are currently unused by my modeling
	// protected double onFireHeatRange, warmingCooldown, maxColdSlowdown;
	
	// This information extracted via UUU
	protected double courage = 0.0;  // aka "Fear Resistance"
	// Used to determine average regular Fear duration. Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
	// Additionally, all creatures that get Feared have a x1.5 speedboost, except for Oppressor (x2) and Bulk/Crassus/Dread (x1) which can only be feared by Field Medic/SYiH/Bosco Revive
	// Values listed as m/sec groundspeed
	protected double maxMovespeedWhenFeared = 0.0;
	
	protected boolean hasLightArmor = false, hasHeavyArmorRNG = false, hasHeavyArmorHealth = false, heavyArmorCoversWeakpoint = false, hasUnbreakableArmor = false;
	protected double armorStrength = 0.0, armorBaseHealth = 0.0;
	protected double numArmorStrengthPlates = 0, numArmorHealthPlates = 0;  // These variables are NOT how many armor plates the enemy has total, but rather how many armor plates will be modeled by ArmorWasting()
	
	/****************************************************************************************
	* Constructors
	****************************************************************************************/
	
	/****************************************************************************************
	* Setters and Getters
	****************************************************************************************/
	
	public double getSpawnProbability(boolean exact) {
		if (exact) {
			return exactSpawnProbability;
		}
		else {
			return guessedSpawnProbability;
		}
	}
	public boolean shouldHaveBreakpointsCalculated() {
		return calculateBreakpoints;
	}
	public String getName() {
		return enemyName;
	}
	public boolean isMacteraType() {
		return macteraType;
	}
	public double getBaseHealth() {
		return baseHealth;
	}
	public boolean usesNormalScaling() {
		return normalScaling;
	}
	public boolean hasExposedBodySomewhere() {
		return hasExposedBodySomewhere;
	}
	public boolean hasWeakpoint() {
		return hasWeakpoint;
	}
	public double getWeakpointMultiplier() {
		if (hasWeakpoint) {
			return weakpointMultiplier;
		}
		else {
			// Returning zero is necessary for some of the vector dot products to return the correct number.
			return 0.0;
		}
	}
	public double getProbabilityBulletHitsWeakpoint() {
		if (hasWeakpoint) {
			return estimatedProbabilityBulletHitsWeakpoint;
		}
		else {
			return 0.0;
		}
	}
	public double getExplosiveResistance() {
		return explosiveResistance;
	}
	public double getFireResistance() {
		return fireResistance;
	}
	public double getFrostResistance() {
		return frostResistance;
	}
	public double getElectricResistance() {
		return electricResistance;
	}
	public double getIgniteTemp() {
		return igniteTemperature / temperatureChangeScale;
	}
	public double getDouseTemp() {
		return douseTemperature / temperatureChangeScale;
	}
	public double getCoolingRate() {
		return coolingRate;
	}
	public double getFreezeTemp() {
		return freezeTemperature / temperatureChangeScale;
	}
	public double getUnfreezeTemp() {
		return unfreezeTemperature / temperatureChangeScale;
	}
	public double getWarmingRate() {
		return warmingRate;
	}
	public double getCourage() {
		return courage;
	}
	public double getMaxMovespeedWhenFeared() {
		return maxMovespeedWhenFeared;
	}
	public boolean hasLightArmor() {
		return hasLightArmor;
	}
	public boolean hasHeavyArmorStrength() {
		return hasHeavyArmorRNG;
	}
	public boolean hasHeavyArmorHealth() {
		return hasHeavyArmorHealth;
	}
	public boolean weakpointIsCoveredByHeavyArmor() {
		return heavyArmorCoversWeakpoint;
	}
	public boolean hasUnbreakableArmor() {
		return hasUnbreakableArmor;
	}
	public double getArmorStrength() {
		if (hasLightArmor || hasHeavyArmorRNG) {
			return armorStrength;
		}
		else {
			return 0.0;
		}
	}
	public double getNumArmorStrengthPlates() {
		if (hasLightArmor || hasHeavyArmorRNG) {
			return numArmorStrengthPlates;
		}
		else {
			return 0;
		}
	}
	public double getArmorBaseHealth() {
		if (hasHeavyArmorHealth) {
			return armorBaseHealth;
		}
		else {
			return 0.0;
		}
	}
	public double getNumArmorHealthPlates() {
		if (hasHeavyArmorHealth) {
			return numArmorHealthPlates;
		}
		else {
			return 0;
		}
	}
	
	/****************************************************************************************
	* Other Methods
	****************************************************************************************/
	
	// This method gets used in ArmorWasting()
	public boolean hasBreakableArmor() {
		return hasLightArmor || hasHeavyArmorRNG || hasHeavyArmorHealth;
	}
}
