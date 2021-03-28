package enemies.glyphid;

import enemies.Enemy;

public class Exploder extends Enemy {
	public Exploder() {
		guessedSpawnProbability = 0.04;
		exactSpawnProbability = 0.03895253682;
		
		enemyName = "Glyphid Exploder";
		baseHealth = 20;
		normalScaling = true;
		
		hasExposedBodySomewhere = true;
		
		hasWeakpoint = true;
		weakpointMultiplier = 2;
		estimatedProbabilityBulletHitsWeakpoint = 0.1;
		
		igniteTemperature = 10; 
		douseTemperature = 0;
		coolingRate = 6;
		freezeTemperature = -10;
		unfreezeTemperature = 0;
		warmingRate = 12;
		
		// Enemies that fly, can't move on the ground, or can't be feared will have this value set to zero to maintain correct values.
		maxMovespeedWhenFeared = 4.0;
	}
}