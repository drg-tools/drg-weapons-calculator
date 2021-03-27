package enemies.glyphid;

import enemies.Enemy;

public class Slasher extends Enemy {
	public Slasher() {
		guessedSpawnProbability = 0.07;
		exactSpawnProbability = 0.05838416902;
		
		enemyName = "Glyphid Grunt Slasher";
		baseHealth = 148;
		normalScaling = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 2;
		estimatedProbabilityBulletHitsWeakpoint = 0.9;
		
		// If this number is greater than 0, that means that it takes less damage from that particular element.
		// Conversely, if it's less than 0 it takes extra damage from that particular element
		explosiveResistance = -0.3;
		
		igniteTemperature = 30; 
		douseTemperature = 10;
		coolingRate = 6;
		freezeTemperature = -30;
		unfreezeTemperature = 0;
		warmingRate = 6;
		
		courage = 0.5;
		// Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
		maxMovespeedWhenFeared = 3.1;
		
		hasLightArmor = true;
		armorStrength = 15;
		// These variables are NOT how many armor plates the enemy has total, but rather how many armor plates will be modeled by ArmorWasting()
		numArmorStrengthPlates = 6;
	}
}
