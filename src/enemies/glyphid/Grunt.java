package enemies.glyphid;

import enemies.Enemy;

public class Grunt extends Enemy {
	public Grunt() {
		guessedSpawnProbability = 0.25;
		exactSpawnProbability = 0.4661508704;
		
		enemyName = "Glyphid Grunt";
		baseHealth = 90;
		normalScaling = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 2;
		estimatedProbabilityBulletHitsWeakpoint = 0.9;
		
		igniteTemperature = 30; 
		douseTemperature = 10;
		coolingRate = 6;
		freezeTemperature = -30;
		unfreezeTemperature = 0;
		warmingRate = 6;
		
		courage = 0.5;
		// Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
		maxMovespeedWhenFeared = 2.9;
		
		hasLightArmor = true;
		armorStrength = 15;
		// These variables are NOT how many armor plates the enemy has total, but rather how many armor plates will be modeled by ArmorWasting()
		numArmorStrengthPlates = 6;
	}
}