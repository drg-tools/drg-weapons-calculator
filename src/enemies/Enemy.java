package enemies;

public class Enemy {
	
	/****************************************************************************************
	* Class Variables
	****************************************************************************************/
	
	protected double guessedSpawnProbability;
	protected double exactSpawnProbability;
	
	// Only the largest/tankiest enemies have this set to false.
	protected boolean calculateBreakpoints = true;
	
	protected String enemyName;
	protected boolean macteraType = false;  // Used for Subata T5.B
	protected double baseHealth;
	protected boolean normalScaling;
	
	protected boolean hasWeakpoint = false;
	protected double weakpointMultiplier;
	protected double estimatedProbabilityBulletHitsWeakpoint;
	// TODO: this could be a good place to model breakable Weakpoints later?
	
	// If this number is greater than 0, that means that it takes less damage from that particular element.
	// Conversely, if it's less than 0 it takes extra damage from that particular element
	protected double explosiveResistance = 0.0, fireResistance = 0.0, frostResistance = 0.0, electricResistance = 0.0;
	
	protected double temperatureUpdateTime = 1.0, temperatureChangeScale = 1.0;
	protected double igniteTemperature, douseTemperature, coolingRate;
	protected double freezeTemperature, unfreezeTemperature, warmingRate;
	// These three variables are currently unused by my modeling
	// protected double onFireHeatRange, warmingCooldown, maxColdSlowdown;
	
	protected double courage = 0.0;  // aka "Fear Resistance"
	protected double maxMovespeedWhenFeared = 0.0;  // Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
	
	protected boolean hasLightArmor = false, hasHeavyArmorRNG = false, hasHeavyArmorHealth = false, heavyArmorCoversWeakpoint = false, hasUnbreakableArmor = false;
	protected double armorStrength = 0.0, armorBaseHealth = 0.0;
	protected int numArmorStrengthPlates = 0, numArmorHealthPlates = 0;  // These variables are NOT how many armor plates the enemy has total, but rather how many armor plates will be modeled by ArmorWasting()
	
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
	public int getNumArmorStrengthPlates() {
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
	public int getNumArmorHealthPlates() {
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
	
	// This method gets used in Breakpoints()
	public boolean hasNeitherWeakpointNorArmor() {
		return !hasWeakpoint && !hasLightArmor && !hasHeavyArmorRNG && !hasHeavyArmorHealth && !hasUnbreakableArmor;
	}
}
