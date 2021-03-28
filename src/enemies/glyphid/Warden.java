package enemies.glyphid;

import enemies.Enemy;

public class Warden extends Enemy {
	public Warden() {
		guessedSpawnProbability = 0.02;
		exactSpawnProbability = 0.00208302336;
		
		enemyName = "Glyphid Warden";
		baseHealth = 800;
		normalScaling = false;
		
		hasExposedBodySomewhere = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 3;
		estimatedProbabilityBulletHitsWeakpoint = 0.5;
		
		igniteTemperature = 50; 
		douseTemperature = 25;
		coolingRate = 6;
		freezeTemperature = -70;
		unfreezeTemperature = -30;
		warmingRate = 6;
		
		courage = 0.5;
		// Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
		maxMovespeedWhenFeared = 2.9;
		
		hasHeavyArmorRNG = true;
		armorStrength = 15;
		// These variables are NOT how many armor plates the enemy has total, but rather how many armor plates will be modeled by ArmorWasting()
		numArmorStrengthPlates = 3;
	}
}
