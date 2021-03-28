package enemies.glyphid;

import enemies.Enemy;

public class Praetorian extends Enemy {
	public Praetorian() {
		guessedSpawnProbability = 0.04;
		exactSpawnProbability = 0.02074096117;
		
		enemyName = "Glyphid Praetorian";
		baseHealth = 750;
		normalScaling = false;
		
		hasExposedBodySomewhere = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 1;
		estimatedProbabilityBulletHitsWeakpoint = 0.4;
		
		igniteTemperature = 100; 
		douseTemperature = 40;
		coolingRate = 10;
		freezeTemperature = -150;
		unfreezeTemperature = -100;
		warmingRate = 10;
		
		courage = 0.5;
		// Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
		maxMovespeedWhenFeared = 2.0;
		
		hasHeavyArmorHealth = true;
		armorBaseHealth = 100;
		// These variables are NOT how many armor plates the enemy has total, but rather how many armor plates will be modeled by ArmorWasting()
		numArmorHealthPlates = 6;
	}
}
