package enemies.glyphid;

import enemies.Enemy;

public class AcidSpitter extends Enemy {
	public AcidSpitter() {
		guessedSpawnProbability = 0.02;
		exactSpawnProbability = 0.01276595745;
		
		enemyName = "Glyphid Acid Spitter";
		baseHealth = 120;
		normalScaling = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 2;
		estimatedProbabilityBulletHitsWeakpoint = 0.4;
		
		// If this number is greater than 0, that means that it takes less damage from that particular element.
		// Conversely, if it's less than 0 it takes extra damage from that particular element
		electricResistance = -0.1;
		
		igniteTemperature = 35; 
		douseTemperature = 5;
		coolingRate = 6;
		freezeTemperature = -50;
		unfreezeTemperature = 0;
		warmingRate = 6;
		
		courage = 0.3;
		// Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
		maxMovespeedWhenFeared = 2.5;
		
		hasLightArmor = true;
		armorStrength = 10;
		// These variables are NOT how many armor plates the enemy has total, but rather how many armor plates will be modeled by ArmorWasting()
		numArmorStrengthPlates = 3;
	}
}
